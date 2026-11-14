package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaMesDTO;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.service.InterfazSomaEstadisticaService;
import com.convecsa.erp.soma.service.InterfazSomaHabilitacionService;
import com.convecsa.erp.soma.service.InterfazSomaObraService;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaObraServiceImpl implements InterfazSomaObraService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final ProyectoRepository proyectoRepository;
    private final ProyectoResponsableRepository proyectoResponsableRepository;
    private final InterfazSomaHabilitacionService somaHabilitacionService;
    private final InterfazSomaEstadisticaService somaEstadisticaService;

    @Override
    @Transactional(readOnly = true)
    public SomaResumenObraDTO obtenerResumen(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
        String supervisor = proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(proyectoId).stream()
                .filter(fila -> fila.getRol() == RolResponsableObra.SUPERVISOR_SSOMA)
                .map(fila -> nombreCompleto(fila.getPersona()))
                .findFirst()
                .orElse(null);
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        SomaHabilitacionDTO semaforo = somaHabilitacionService.evaluarObra(proyectoId, hoy);
        SomaEstadisticaAnioDTO stats = somaEstadisticaService.porObra(proyectoId, hoy.getYear());
        SomaEstadisticaMesDTO mes = stats.getMeses().get(hoy.getMonthValue() - 1);
        return SomaResumenObraDTO.builder()
                .proyectoId(proyecto.getId())
                .codigoObra(proyecto.getCodigo())
                .nombreObra(proyecto.getNombre())
                .supervisorSsoma(supervisor)
                .habilitados(semaforo.getHabilitados())
                .bloqueados(semaforo.getBloqueados())
                .emosPorVencer30Dias(semaforo.getEmosPorVencer30Dias())
                .hhtMes(mes.getHht())
                .accidentesMes(mes.getAccidentes())
                .diasPerdidosMes(mes.getDiasPerdidos())
                .indiceFrecuencia(mes.getIndiceFrecuencia())
                .indiceSeveridad(mes.getIndiceSeveridad())
                .indiceAccidentabilidad(mes.getIndiceAccidentabilidad())
                .diasSinAccidente(stats.getDiasSinAccidente())
                .build();
    }

    private static String nombreCompleto(Persona persona) {
        if (persona == null) {
            return null;
        }
        String nombres = persona.getNombres() == null ? "" : persona.getNombres().trim();
        String apellidos = persona.getApellidos() == null ? "" : persona.getApellidos().trim();
        String combinado = (nombres + " " + apellidos).trim();
        return combinado.isEmpty() ? null : combinado;
    }
}
