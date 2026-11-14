package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.construccion.domain.SeveridadAlertaObra;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.UsuarioProyectoRepository;
import com.convecsa.erp.construccion.support.AccesoObraService;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.dto.SomaDashboardDTO;
import com.convecsa.erp.soma.dto.SomaDashboardObraAlertaDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaMesDTO;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.service.InterfazSomaAlertaService;
import com.convecsa.erp.soma.service.InterfazSomaDashboardService;
import com.convecsa.erp.soma.service.InterfazSomaEstadisticaService;
import com.convecsa.erp.soma.support.SomaAcceso;
import com.convecsa.erp.soma.support.SomaEstadisticaCalculator;
import com.convecsa.erp.soma.support.SomaEstadisticaCalculator.IndicesSst;
import com.convecsa.erp.usuario.entity.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaDashboardServiceImpl implements InterfazSomaDashboardService {

    static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final SomaAcceso somaAcceso;
    private final AccesoObraService accesoObraService;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioProyectoRepository usuarioProyectoRepository;
    private final InterfazSomaEstadisticaService somaEstadisticaService;
    private final SomaHallazgoRepository somaHallazgoRepository;
    private final InterfazSomaAlertaService somaAlertaService;

    @Override
    @Transactional(readOnly = true)
    public SomaDashboardDTO corporativo(Integer anio) {
        somaAcceso.exigirSomaCorporativo();
        int anioResuelto = anio == null ? LocalDate.now(ZONA_LIMA).getYear() : anio;
        if (anioResuelto < 2000 || anioResuelto > 2100) {
            throw new IllegalArgumentException("El año debe ser válido");
        }
        List<Proyecto> obras = obrasVisibles();
        SomaEstadisticaAnioDTO estadistica = estadistica(obras, anioResuelto);
        ConteosHallazgo conteos = conteosHallazgo(obras);
        return SomaDashboardDTO.builder()
                .estadistica(estadistica)
                .hallazgosAbiertos(conteos.abiertos)
                .hallazgosLevantados(conteos.levantados)
                .obrasConAlertasCriticas(obrasCriticas(obras))
                .build();
    }

    private List<Proyecto> obrasVisibles() {
        Usuario usuario = accesoObraService.usuarioActual();
        if (AccesoObraService.esGerencia(usuario.getRol())) {
            return proyectoRepository.findAllByOrderByCodigoAsc();
        }
        List<Long> ids = usuarioProyectoRepository.findProyectoIdsByUsuarioId(usuario.getId());
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Proyecto> obras = new ArrayList<>(proyectoRepository.findAllById(ids));
        obras.sort(Comparator.comparing(Proyecto::getCodigo, Comparator.nullsLast(String::compareTo)));
        return obras;
    }

    private SomaEstadisticaAnioDTO estadistica(List<Proyecto> obras, int anio) {
        Usuario usuario = accesoObraService.usuarioActual();
        if (AccesoObraService.esGerencia(usuario.getRol())) {
            return somaEstadisticaService.corporativo(anio);
        }
        if (obras.isEmpty()) {
            return vacia(anio);
        }
        List<SomaEstadisticaAnioDTO> partes = new ArrayList<>();
        for (Proyecto obra : obras) {
            if (obra.getId() == null) {
                continue;
            }
            partes.add(somaEstadisticaService.porObra(obra.getId(), anio));
        }
        return combinar(anio, partes);
    }

    private ConteosHallazgo conteosHallazgo(List<Proyecto> obras) {
        List<SomaHallazgo> hallazgos;
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            hallazgos = somaHallazgoRepository.findAllByOrderByFechaDeteccionDescIdDesc();
        } else if (obras.isEmpty()) {
            hallazgos = List.of();
        } else {
            List<Long> ids = obras.stream().map(Proyecto::getId).toList();
            hallazgos = somaHallazgoRepository.findByProyectoIdInOrderByFechaDeteccionDescIdDesc(ids);
        }
        int abiertos = 0;
        int levantados = 0;
        for (SomaHallazgo hallazgo : hallazgos) {
            if (hallazgo.getEstado() == EstadoHallazgoSoma.LEVANTADO) {
                levantados++;
            } else if (hallazgo.getEstado() == EstadoHallazgoSoma.ABIERTO
                    || hallazgo.getEstado() == EstadoHallazgoSoma.EN_PROCESO) {
                abiertos++;
            }
        }
        return new ConteosHallazgo(abiertos, levantados);
    }

    private List<SomaDashboardObraAlertaDTO> obrasCriticas(List<Proyecto> obras) {
        List<SomaDashboardObraAlertaDTO> resultado = new ArrayList<>();
        for (Proyecto obra : obras) {
            if (obra.getId() == null) {
                continue;
            }
            List<AlertaObraDTO> alertas = somaAlertaService.alertas(obra.getId());
            boolean critica = alertas.stream().anyMatch(alerta -> alerta.getSeveridad() == SeveridadAlertaObra.CRITICAL);
            if (!critica) {
                continue;
            }
            resultado.add(SomaDashboardObraAlertaDTO.builder()
                    .proyectoId(obra.getId())
                    .codigo(obra.getCodigo())
                    .nombre(obra.getNombre())
                    .build());
        }
        return List.copyOf(resultado);
    }

    static SomaEstadisticaAnioDTO combinar(int anio, List<SomaEstadisticaAnioDTO> partes) {
        if (partes == null || partes.isEmpty()) {
            return vacia(anio);
        }
        BigDecimal[] hht = hhtVacio();
        int[] accidentes = new int[13];
        int[] dias = new int[13];
        Integer diasSinAccidente = null;
        for (SomaEstadisticaAnioDTO parte : partes) {
            if (parte.getMeses() != null) {
                for (SomaEstadisticaMesDTO fila : parte.getMeses()) {
                    if (fila.getMes() == null || fila.getMes() < 1 || fila.getMes() > 12) {
                        continue;
                    }
                    int mes = fila.getMes();
                    hht[mes] = hht[mes].add(nuloCero(fila.getHht()));
                    accidentes[mes] += fila.getAccidentes();
                    dias[mes] += fila.getDiasPerdidos();
                }
            }
            Integer valor = parte.getDiasSinAccidente();
            if (valor != null) {
                diasSinAccidente = diasSinAccidente == null ? valor : Math.min(diasSinAccidente, valor);
            }
        }
        List<SomaEstadisticaMesDTO> meses = new ArrayList<>(12);
        BigDecimal hhtAnio = SomaEstadisticaCalculator.cero();
        int accidentesAnio = 0;
        int diasAnio = 0;
        for (int mes = 1; mes <= 12; mes++) {
            meses.add(mesConIndices(mes, hht[mes], accidentes[mes], dias[mes]));
            hhtAnio = hhtAnio.add(hht[mes]);
            accidentesAnio += accidentes[mes];
            diasAnio += dias[mes];
        }
        return SomaEstadisticaAnioDTO.builder()
                .anio(anio)
                .meses(meses)
                .acumulado(mesConIndices(null, hhtAnio, accidentesAnio, diasAnio))
                .diasSinAccidente(diasSinAccidente)
                .build();
    }

    static SomaEstadisticaAnioDTO vacia(int anio) {
        List<SomaEstadisticaMesDTO> meses = new ArrayList<>(12);
        for (int mes = 1; mes <= 12; mes++) {
            meses.add(mesConIndices(mes, SomaEstadisticaCalculator.cero(), 0, 0));
        }
        return SomaEstadisticaAnioDTO.builder()
                .anio(anio)
                .meses(meses)
                .acumulado(mesConIndices(null, SomaEstadisticaCalculator.cero(), 0, 0))
                .diasSinAccidente(null)
                .build();
    }

    private static SomaEstadisticaMesDTO mesConIndices(Integer mes, BigDecimal hht, int accidentes, int diasPerdidos) {
        BigDecimal horas = nuloCero(hht).setScale(SomaEstadisticaCalculator.ESCALA, SomaEstadisticaCalculator.MODO);
        IndicesSst indices = SomaEstadisticaCalculator.indices(accidentes, diasPerdidos, horas);
        return SomaEstadisticaMesDTO.builder()
                .mes(mes)
                .hht(horas)
                .accidentes(accidentes)
                .diasPerdidos(diasPerdidos)
                .indiceFrecuencia(indices.indiceFrecuencia())
                .indiceSeveridad(indices.indiceSeveridad())
                .indiceAccidentabilidad(indices.indiceAccidentabilidad())
                .build();
    }

    private static BigDecimal[] hhtVacio() {
        BigDecimal[] hht = new BigDecimal[13];
        for (int i = 0; i < hht.length; i++) {
            hht[i] = SomaEstadisticaCalculator.cero();
        }
        return hht;
    }

    private static BigDecimal nuloCero(BigDecimal valor) {
        return valor == null ? SomaEstadisticaCalculator.cero() : valor;
    }

    private record ConteosHallazgo(int abiertos, int levantados) {}
}
