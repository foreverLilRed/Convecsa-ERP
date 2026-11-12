package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ProyectoResponsable;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.service.impl.SomaObraServiceImpl;
import java.util.List;
import java.util.Optional;
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

    @InjectMocks
    private SomaObraServiceImpl servicio;

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
