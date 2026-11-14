package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.SeveridadAlertaObra;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.UsuarioProyectoRepository;
import com.convecsa.erp.construccion.support.AccesoObraService;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.dto.SomaDashboardDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaMesDTO;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.service.impl.SomaDashboardServiceImpl;
import com.convecsa.erp.soma.support.SomaAcceso;
import com.convecsa.erp.usuario.domain.RolUsuario;
import com.convecsa.erp.usuario.entity.Usuario;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaDashboardServiceImplTest {

    @Mock
    private SomaAcceso somaAcceso;
    @Mock
    private AccesoObraService accesoObraService;
    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private UsuarioProyectoRepository usuarioProyectoRepository;
    @Mock
    private InterfazSomaEstadisticaService somaEstadisticaService;
    @Mock
    private SomaHallazgoRepository somaHallazgoRepository;
    @Mock
    private InterfazSomaAlertaService somaAlertaService;

    @InjectMocks
    private SomaDashboardServiceImpl servicio;

    private Proyecto obraA;
    private Proyecto obraB;

    @BeforeEach
    void preparar() {
        obraA = proyecto(10L, "0000", "Obra piloto");
        obraB = proyecto(11L, "0001", "Obra dos");
    }

    @Test
    void gerenciaUsaEstadisticaCorporativa() {
        autenticar(RolUsuario.SUPERADMIN, 1L);
        when(proyectoRepository.findAllByOrderByCodigoAsc()).thenReturn(List.of(obraA));
        when(somaEstadisticaService.corporativo(2026)).thenReturn(estadistica(100, 2, 45));
        when(somaHallazgoRepository.findAllByOrderByFechaDeteccionDescIdDesc()).thenReturn(List.of());
        when(somaAlertaService.alertas(10L)).thenReturn(List.of());

        SomaDashboardDTO dto = servicio.corporativo(2026);

        assertEquals(100, dto.getEstadistica().getAcumulado().getHht().intValue());
        assertEquals(45, dto.getEstadistica().getDiasSinAccidente());
        verify(somaEstadisticaService).corporativo(2026);
        verify(somaEstadisticaService, never()).porObra(any(), anyInt());
        verify(usuarioProyectoRepository, never()).findProyectoIdsByUsuarioId(any());
    }

    @Test
    void ingenieroSumaPorObraYNoLlamaCorporativo() {
        autenticar(RolUsuario.INGENIERO_SOMA, 8L);
        when(usuarioProyectoRepository.findProyectoIdsByUsuarioId(8L)).thenReturn(List.of(10L, 11L));
        when(proyectoRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(obraA, obraB));
        when(somaEstadisticaService.porObra(10L, 2026)).thenReturn(estadistica(40, 1, 10));
        when(somaEstadisticaService.porObra(11L, 2026)).thenReturn(estadistica(60, 0, 30));
        when(somaHallazgoRepository.findByProyectoIdInOrderByFechaDeteccionDescIdDesc(any())).thenReturn(List.of());
        when(somaAlertaService.alertas(any())).thenReturn(List.of());

        SomaDashboardDTO dto = servicio.corporativo(2026);

        assertEquals(100, dto.getEstadistica().getAcumulado().getHht().intValue());
        assertEquals(1, dto.getEstadistica().getAcumulado().getAccidentes());
        assertEquals(10, dto.getEstadistica().getDiasSinAccidente());
        verify(somaEstadisticaService, never()).corporativo(any());
        verify(somaEstadisticaService).porObra(10L, 2026);
        verify(somaEstadisticaService).porObra(11L, 2026);
    }

    @Test
    void ingenieroSinObrasDevuelveCeros() {
        autenticar(RolUsuario.INGENIERO_SOMA, 8L);
        when(usuarioProyectoRepository.findProyectoIdsByUsuarioId(8L)).thenReturn(List.of());

        SomaDashboardDTO dto = servicio.corporativo(2026);

        assertEquals(0, dto.getEstadistica().getAcumulado().getAccidentes());
        assertEquals(0, dto.getHallazgosAbiertos());
        assertEquals(0, dto.getHallazgosLevantados());
        assertTrue(dto.getObrasConAlertasCriticas().isEmpty());
        verify(somaEstadisticaService, never()).corporativo(any());
        verify(somaEstadisticaService, never()).porObra(any(), anyInt());
        verify(somaHallazgoRepository, never()).findAllByOrderByFechaDeteccionDescIdDesc();
        verify(somaAlertaService, never()).alertas(any());
    }

    @Test
    void cuentaHallazgosAbiertosYLevantados() {
        autenticar(RolUsuario.SUPERADMIN, 1L);
        when(proyectoRepository.findAllByOrderByCodigoAsc()).thenReturn(List.of(obraA));
        when(somaEstadisticaService.corporativo(2026)).thenReturn(estadistica(0, 0, null));
        SomaHallazgo abierto = hallazgo(EstadoHallazgoSoma.ABIERTO);
        SomaHallazgo proceso = hallazgo(EstadoHallazgoSoma.EN_PROCESO);
        SomaHallazgo levantado = hallazgo(EstadoHallazgoSoma.LEVANTADO);
        when(somaHallazgoRepository.findAllByOrderByFechaDeteccionDescIdDesc())
                .thenReturn(List.of(abierto, proceso, levantado));
        when(somaAlertaService.alertas(10L)).thenReturn(List.of());

        SomaDashboardDTO dto = servicio.corporativo(2026);

        assertEquals(2, dto.getHallazgosAbiertos());
        assertEquals(1, dto.getHallazgosLevantados());
    }

    @Test
    void soloIncluyeObrasConAlertaCritica() {
        autenticar(RolUsuario.SUPERADMIN, 1L);
        when(proyectoRepository.findAllByOrderByCodigoAsc()).thenReturn(List.of(obraA, obraB));
        when(somaEstadisticaService.corporativo(2026)).thenReturn(estadistica(0, 0, null));
        when(somaHallazgoRepository.findAllByOrderByFechaDeteccionDescIdDesc()).thenReturn(List.of());
        when(somaAlertaService.alertas(10L)).thenReturn(List.of(
                AlertaObraDTO.builder().codigo("SOMA_EMO").severidad(SeveridadAlertaObra.CRITICAL).mensaje("x").build()));
        when(somaAlertaService.alertas(11L)).thenReturn(List.of(
                AlertaObraDTO.builder().codigo("SOMA_SCTR").severidad(SeveridadAlertaObra.WARNING).mensaje("y").build()));

        SomaDashboardDTO dto = servicio.corporativo(2026);

        assertEquals(1, dto.getObrasConAlertasCriticas().size());
        assertEquals(10L, dto.getObrasConAlertasCriticas().get(0).getProyectoId());
        assertEquals("0000", dto.getObrasConAlertasCriticas().get(0).getCodigo());
    }

    private void autenticar(RolUsuario rol, Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setUsername("user");
        usuario.setRol(rol);
        when(accesoObraService.usuarioActual()).thenReturn(usuario);
    }

    private static Proyecto proyecto(Long id, String codigo, String nombre) {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(id);
        proyecto.setCodigo(codigo);
        proyecto.setNombre(nombre);
        return proyecto;
    }

    private static SomaHallazgo hallazgo(EstadoHallazgoSoma estado) {
        SomaHallazgo item = new SomaHallazgo();
        item.setEstado(estado);
        return item;
    }

    private static SomaEstadisticaAnioDTO estadistica(int hht, int accidentes, Integer diasSinAccidente) {
        List<SomaEstadisticaMesDTO> meses = new java.util.ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            meses.add(SomaEstadisticaMesDTO.builder()
                    .mes(mes)
                    .hht(mes == 11 ? BigDecimal.valueOf(hht) : BigDecimal.ZERO)
                    .accidentes(mes == 11 ? accidentes : 0)
                    .diasPerdidos(0)
                    .indiceFrecuencia(BigDecimal.ZERO)
                    .indiceSeveridad(BigDecimal.ZERO)
                    .indiceAccidentabilidad(BigDecimal.ZERO)
                    .build());
        }
        return SomaEstadisticaAnioDTO.builder()
                .anio(2026)
                .meses(meses)
                .acumulado(SomaEstadisticaMesDTO.builder()
                        .mes(null)
                        .hht(BigDecimal.valueOf(hht))
                        .accidentes(accidentes)
                        .diasPerdidos(0)
                        .indiceFrecuencia(BigDecimal.ZERO)
                        .indiceSeveridad(BigDecimal.ZERO)
                        .indiceAccidentabilidad(BigDecimal.ZERO)
                        .build())
                .diasSinAccidente(diasSinAccidente)
                .build();
    }
}
