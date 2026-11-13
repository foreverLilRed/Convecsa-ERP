package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.CuadrillaObraNoEncontradaException;
import com.convecsa.erp.construccion.entity.CuadrillaMiembro;
import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.CuadrillaMiembroRepository;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
import com.convecsa.erp.soma.dto.SomaCapacitacionAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionAsistenteRequest;
import com.convecsa.erp.soma.dto.SomaCapacitacionDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionRequest;
import com.convecsa.erp.soma.entity.SomaCapacitacion;
import com.convecsa.erp.soma.repository.SomaCapacitacionRepository;
import com.convecsa.erp.soma.service.impl.SomaCapacitacionServiceImpl;
import java.math.BigDecimal;
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
class SomaCapacitacionServiceImplTest {

    @Mock
    private SomaCapacitacionRepository somaCapacitacionRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @Mock
    private CuadrillaObraRepository cuadrillaObraRepository;

    @Mock
    private CuadrillaMiembroRepository cuadrillaMiembroRepository;

    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private SomaCapacitacionServiceImpl servicio;

    @BeforeEach
    void stubSave() {
        lenient().when(somaCapacitacionRepository.save(any(SomaCapacitacion.class))).thenAnswer(invocation -> {
            SomaCapacitacion cap = invocation.getArgument(0);
            if (cap.getId() == null) {
                cap.setId(5L);
            }
            long id = 1L;
            for (var asistente : cap.getAsistentes()) {
                if (asistente.getId() == null) {
                    asistente.setId(id++);
                }
            }
            return cap;
        });
    }

    @Test
    void notaDefineAprobado() {
        stubCrear();
        SomaCapacitacionDTO diez = servicio.crear(10L, requestConNota(new BigDecimal("10"), null));
        assertFalse(diez.getAsistentes().get(0).isAprobado());

        SomaCapacitacionDTO once = servicio.crear(10L, requestConNota(new BigDecimal("11"), null));
        assertTrue(once.getAsistentes().get(0).isAprobado());
    }

    @Test
    void sinNotaRespetaAprobadoEnviado() {
        stubCrear();
        SomaCapacitacionDTO reprobado = servicio.crear(10L, requestConNota(null, false));
        assertFalse(reprobado.getAsistentes().get(0).isAprobado());

        SomaCapacitacionDTO aprobado = servicio.crear(10L, requestConNota(null, true));
        assertTrue(aprobado.getAsistentes().get(0).isAprobado());
    }

    @Test
    void rechazaInduccionDeMenosDeCuatroHoras() {
        stubCrear();
        SomaCapacitacionRequest request = requestBase(TipoCapacitacionSoma.INDUCCION_HOMBRE_NUEVO, new BigDecimal("3.99"));

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));

        assertEquals("La inducción de hombre nuevo debe durar al menos 4 horas", error.getMessage());
        verify(somaCapacitacionRepository, never()).save(any());
    }

    @Test
    void aceptaInduccionDeCuatroHoras() {
        stubCrear();
        SomaCapacitacionDTO dto =
                servicio.crear(10L, requestBase(TipoCapacitacionSoma.INDUCCION_HOMBRE_NUEVO, new BigDecimal("4.00")));

        assertEquals(new BigDecimal("4.00"), dto.getHorasDuracion());
        assertEquals(TipoCapacitacionSoma.INDUCCION_HOMBRE_NUEVO, dto.getTipo());
    }

    @Test
    void precargaMiembrosDeLaCuadrilla() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        CuadrillaObra cuadrilla = cuadrilla(8L, 10L);
        when(cuadrillaObraRepository.findByIdAndProyectoId(8L, 10L)).thenReturn(Optional.of(cuadrilla));
        CuadrillaMiembro miembro = new CuadrillaMiembro();
        miembro.setCuadrilla(cuadrilla);
        miembro.setPersona(persona(20L));
        when(cuadrillaMiembroRepository.findByProyectoId(10L)).thenReturn(List.of(miembro));

        List<SomaCapacitacionAsistenteDTO> filas = servicio.precargarCuadrilla(10L, 8L);

        assertEquals(1, filas.size());
        assertEquals(20L, filas.get(0).getPersonaId());
        assertTrue(filas.get(0).isAprobado());
    }

    @Test
    void precargaCuadrillaDeOtraObraNoEncontrada() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(cuadrillaObraRepository.findByIdAndProyectoId(8L, 10L)).thenReturn(Optional.empty());

        assertThrows(CuadrillaObraNoEncontradaException.class, () -> servicio.precargarCuadrilla(10L, 8L));
    }

    @Test
    void rechazaAsistenteDuplicado() {
        stubCrear();
        SomaCapacitacionRequest request = requestBase(TipoCapacitacionSoma.CAPACITACION_PROGRAMADA, BigDecimal.ONE);
        SomaCapacitacionAsistenteRequest otro = new SomaCapacitacionAsistenteRequest();
        otro.setPersonaId(20L);
        request.setAsistentes(List.of(request.getAsistentes().get(0), otro));

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));

        assertEquals("La persona ya está en la lista de asistentes", error.getMessage());
        verify(somaCapacitacionRepository, never()).save(any());
    }

    private void stubCrear() {
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        Persona persona = persona(20L);
        lenient().when(personaRepository.findAllById(anyCollection())).thenReturn(List.of(persona));
    }

    private static SomaCapacitacionRequest requestConNota(BigDecimal nota, Boolean aprobado) {
        SomaCapacitacionRequest request = requestBase(TipoCapacitacionSoma.CAPACITACION_PROGRAMADA, BigDecimal.ONE);
        request.getAsistentes().get(0).setNota(nota);
        request.getAsistentes().get(0).setAprobado(aprobado);
        return request;
    }

    private static SomaCapacitacionRequest requestBase(TipoCapacitacionSoma tipo, BigDecimal horas) {
        SomaCapacitacionAsistenteRequest asistente = new SomaCapacitacionAsistenteRequest();
        asistente.setPersonaId(20L);
        SomaCapacitacionRequest request = new SomaCapacitacionRequest();
        request.setTipo(tipo);
        request.setTema("Inducción SSOMA");
        request.setFecha(LocalDate.of(2026, 11, 13));
        request.setHorasDuracion(horas);
        request.setExpositorNombre("Ing. SOMA");
        request.setAsistentes(List.of(asistente));
        return request;
    }

    private static Proyecto obra(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(id);
        proyecto.setCodigo("0000");
        proyecto.setNombre("BAMBAS NEGRAS");
        return proyecto;
    }

    private static Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombres("Pedro");
        persona.setApellidos("Rojas");
        persona.setNumeroDocumento("73567960");
        return persona;
    }

    private static CuadrillaObra cuadrilla(Long id, Long proyectoId) {
        CuadrillaObra cuadrilla = new CuadrillaObra();
        cuadrilla.setId(id);
        cuadrilla.setProyecto(obra(proyectoId));
        cuadrilla.setCodigo("C-1");
        cuadrilla.setNombre("Cuadrilla 1");
        return cuadrilla;
    }
}
