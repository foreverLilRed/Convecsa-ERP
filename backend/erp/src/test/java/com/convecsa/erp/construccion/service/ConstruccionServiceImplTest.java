package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.ProyectoCodigoDuplicadoException;
import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.ProyectoRequest;
import com.convecsa.erp.construccion.dto.ProyectoResponsableRequest;
import com.convecsa.erp.construccion.entity.CcParametroAnual;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.CcParametroAnualRepository;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.construccion.service.impl.ConstruccionServiceImpl;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConstruccionServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ProyectoResponsableRepository proyectoResponsableRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private CcParametroAnualRepository parametroRepository;

    @Mock
    private ContratoCcRepository contratoCcRepository;

    @InjectMocks
    private ConstruccionServiceImpl servicio;

    @Test
    void listarPersonalExponeFechasDeIngresoYCese() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(3L);
        when(proyectoRepository.findById(3L)).thenReturn(Optional.of(proyecto));

        Persona conCese = new Persona();
        conCese.setId(11L);
        conCese.setNombres("Ana");
        conCese.setApellidos("Rojas");
        conCese.setNumeroDocumento("87654321");
        Contrato contratoConCese = new Contrato();
        contratoConCese.setId(22L);
        contratoConCese.setPersona(conCese);
        contratoConCese.setFechaInicio(LocalDate.of(2026, 3, 10));
        contratoConCese.setFechaFin(LocalDate.of(2026, 11, 15));
        ContratoCc ccConCese = new ContratoCc();
        ccConCese.setContrato(contratoConCese);
        ccConCese.setCategoria(CategoriaObrera.OPERARIO);

        Persona vigente = new Persona();
        vigente.setId(12L);
        vigente.setNombres("Luis");
        vigente.setApellidos("Paredes");
        vigente.setNumeroDocumento("11223344");
        Contrato contratoVigente = new Contrato();
        contratoVigente.setId(23L);
        contratoVigente.setPersona(vigente);
        contratoVigente.setFechaInicio(LocalDate.of(2026, 1, 5));
        contratoVigente.setFechaFin(null);
        ContratoCc ccVigente = new ContratoCc();
        ccVigente.setContrato(contratoVigente);
        ccVigente.setCategoria(CategoriaObrera.PEON);

        when(contratoCcRepository.findActivosByProyectoId(3L)).thenReturn(List.of(ccConCese, ccVigente));

        List<PersonalObraDTO> personal = servicio.listarPersonal(3L);

        assertEquals(2, personal.size());
        assertEquals(LocalDate.of(2026, 3, 10), personal.get(0).getFechaIngreso());
        assertEquals(LocalDate.of(2026, 11, 15), personal.get(0).getFechaCese());
        assertEquals(LocalDate.of(2026, 1, 5), personal.get(1).getFechaIngreso());
        assertNull(personal.get(1).getFechaCese());
    }

    @Test
    void actualizarProyectoCambiaClienteVigenciaYEstado() {
        Proyecto proyecto = proyectoExistente();
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(proyectoRepository.existsByCodigoIgnoreCaseAndIdNot("OB-01", 7L)).thenReturn(false);
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parametroRepository.findByAnio(LocalDate.now().getYear())).thenReturn(Optional.of(parametrosUit()));

        ProyectoRequest request = requestBase();
        request.setNombre("Obra Norte actualizada");
        request.setClienteRazon("Cliente SAC");
        request.setClienteRuc("20123456789");
        request.setFechaInicio(LocalDate.of(2026, 2, 1));
        request.setFechaFin(LocalDate.of(2026, 11, 30));
        request.setEstadoObra(EstadoObra.PARALIZADA);

        ProyectoDTO dto = servicio.actualizarProyecto(7L, request);

        assertEquals("Obra Norte actualizada", dto.getNombre());
        assertEquals("Cliente SAC", dto.getClienteRazon());
        assertEquals("20123456789", dto.getClienteRuc());
        assertEquals(LocalDate.of(2026, 2, 1), dto.getFechaInicio());
        assertEquals(LocalDate.of(2026, 11, 30), dto.getFechaFin());
        assertEquals(EstadoObra.PARALIZADA, dto.getEstadoObra());
        assertTrue(dto.isActivo());
    }

    @Test
    void actualizarProyectoRechazaCodigoDuplicado() {
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyectoExistente()));
        when(proyectoRepository.existsByCodigoIgnoreCaseAndIdNot("OB-02", 7L)).thenReturn(true);

        ProyectoRequest request = requestBase();
        request.setCodigo("ob-02");

        assertThrows(ProyectoCodigoDuplicadoException.class, () -> servicio.actualizarProyecto(7L, request));
    }

    @Test
    void actualizarProyectoInactivaLaObra() {
        Proyecto proyecto = proyectoExistente();
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(proyectoRepository.existsByCodigoIgnoreCaseAndIdNot("OB-01", 7L)).thenReturn(false);
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parametroRepository.findByAnio(LocalDate.now().getYear())).thenReturn(Optional.of(parametrosUit()));

        ProyectoRequest request = requestBase();
        request.setActivo(false);

        ProyectoDTO dto = servicio.actualizarProyecto(7L, request);

        assertFalse(dto.isActivo());
        assertFalse(proyecto.isActivo());
    }

    @Test
    void guardarResponsablesRechazaRolDuplicado() {
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyectoExistente()));

        ProyectoResponsableRequest primero = new ProyectoResponsableRequest();
        primero.setPersonaId(11L);
        primero.setRol(RolResponsableObra.RESIDENTE);
        ProyectoResponsableRequest segundo = new ProyectoResponsableRequest();
        segundo.setPersonaId(12L);
        segundo.setRol(RolResponsableObra.RESIDENTE);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.guardarResponsables(7L, List.of(primero, segundo)));
        assertEquals("Hay roles de responsable duplicados", error.getMessage());
    }

    private static Proyecto proyectoExistente() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setCodigo("OB-01");
        proyecto.setNombre("Obra Norte");
        proyecto.setEstadoObra(EstadoObra.EN_EJECUCION);
        proyecto.setActivo(true);
        return proyecto;
    }

    private static ProyectoRequest requestBase() {
        ProyectoRequest request = new ProyectoRequest();
        request.setCodigo("OB-01");
        request.setNombre("Obra Norte");
        request.setEstadoObra(EstadoObra.EN_EJECUCION);
        request.setActivo(true);
        return request;
    }

    private static CcParametroAnual parametrosUit() {
        CcParametroAnual parametros = new CcParametroAnual();
        parametros.setAnio(LocalDate.now().getYear());
        parametros.setUit(new BigDecimal("5350"));
        return parametros;
    }
}
