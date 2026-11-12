package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.entity.AuditoriaObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.AuditoriaObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditoriaObraRegistrarTest {

    @Mock
    private AuditoriaObraRepository auditoriaObraRepository;
    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private AuditoriaObraRegistrar registrar;

    @Test
    void persisteConUsuarioSistemaSiNoHayAuth() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(7L);
        when(proyectoRepository.getReferenceById(7L)).thenReturn(proyecto);
        when(auditoriaObraRepository.save(any(AuditoriaObra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registrar.registrar(
                7L,
                AccionAuditoriaObra.GUARDAR_TAREO,
                AuditoriaObraRegistrar.ENTIDAD_TAREO,
                4L,
                "detalle");

        ArgumentCaptor<AuditoriaObra> captor = ArgumentCaptor.forClass(AuditoriaObra.class);
        verify(auditoriaObraRepository).save(captor.capture());
        AuditoriaObra fila = captor.getValue();
        assertEquals("GUARDAR_TAREO", fila.getAccion());
        assertEquals(AuditoriaObraRegistrar.ENTIDAD_TAREO, fila.getEntidad());
        assertEquals(4L, fila.getEntidadId());
        assertEquals("detalle", fila.getDetalle());
        assertEquals(AuditoriaObraRegistrar.USUARIO_SISTEMA, fila.getUsuario());
        assertEquals(proyecto, fila.getProyecto());
        assertNull(fila.getCreatedAt());
    }
}
