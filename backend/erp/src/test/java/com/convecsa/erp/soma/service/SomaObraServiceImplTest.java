package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ProyectoResponsable;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.service.impl.SomaObraServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaObraServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ProyectoResponsableRepository proyectoResponsableRepository;

    @Mock
    private InterfazSomaHabilitacionService somaHabilitacionService;

    @InjectMocks
    private SomaObraServiceImpl servicio;

    @BeforeEach
    void stubHabilitacion() {
        lenient()
                .when(somaHabilitacionService.evaluarObra(eq(10L), any(LocalDate.class)))
                .thenReturn(SomaHabilitacionDTO.builder()
                        .fecha(LocalDate.of(2026, 11, 13))
                        .habilitados(2)
                        .bloqueados(1)
                        .emosPorVencer30Dias(1)
                        .filas(List.of())
                        .build());
    }

    @Test
    void resumenTraeNombreDelSupervisorSsoma() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(10L))
                .thenReturn(List.of(
                        responsable(RolResponsableObra.RESIDENTE, "Luis", "Pérez"),
                        responsable(RolResponsableObra.SUPERVISOR_SSOMA, "Ana", "García")));

        SomaResumenObraDTO resumen = servicio.obtenerResumen(10L);

        assertEquals(10L, resumen.getProyectoId());
        assertEquals("OB10", resumen.getCodigoObra());
        assertEquals("Obra 10", resumen.getNombreObra());
        assertEquals("Ana García", resumen.getSupervisorSsoma());
        assertEquals(2, resumen.getHabilitados());
        assertEquals(1, resumen.getBloqueados());
        assertEquals(1, resumen.getEmosPorVencer30Dias());
    }

    @Test
    void resumenSinSupervisorSsomaDevuelveNull() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(10L))
                .thenReturn(List.of(responsable(RolResponsableObra.RESIDENTE, "Luis", "Pérez")));

        SomaResumenObraDTO resumen = servicio.obtenerResumen(10L);

        assertNull(resumen.getSupervisorSsoma());
    }

    @Test
    void resumenFallaSiLaObraNoExiste() {
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProyectoNoEncontradoException.class, () -> servicio.obtenerResumen(99L));
    }

    private static Proyecto obra(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(id);
        proyecto.setCodigo("OB" + id);
        proyecto.setNombre("Obra " + id);
        return proyecto;
    }

    private static ProyectoResponsable responsable(RolResponsableObra rol, String nombres, String apellidos) {
        Persona persona = new Persona();
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        ProyectoResponsable fila = new ProyectoResponsable();
        fila.setRol(rol);
        fila.setPersona(persona);
        return fila;
    }
}
