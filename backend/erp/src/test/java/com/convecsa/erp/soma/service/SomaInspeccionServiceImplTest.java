package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.entity.BitacoraObra;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.BitacoraObraRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.SubcontratoObraRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.ClasificacionHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
import com.convecsa.erp.soma.dto.SomaHallazgoDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoLevantarRequest;
import com.convecsa.erp.soma.dto.SomaHallazgoRequest;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.repository.SomaInspeccionRepository;
import com.convecsa.erp.soma.service.impl.SomaInspeccionServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaInspeccionServiceImplTest {

    @Mock
    private SomaInspeccionRepository somaInspeccionRepository;

    @Mock
    private SomaHallazgoRepository somaHallazgoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private SubcontratoObraRepository subcontratoObraRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @Mock
    private BitacoraObraRepository bitacoraObraRepository;

    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private SomaInspeccionServiceImpl servicio;

    private Proyecto proyecto;

    @BeforeEach
    void stubBasico() {
        proyecto = new Proyecto();
        proyecto.setId(10L);
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        lenient().when(somaHallazgoRepository.save(any(SomaHallazgo.class))).thenAnswer(invocation -> {
            SomaHallazgo hallazgo = invocation.getArgument(0);
            if (hallazgo.getId() == null) {
                hallazgo.setId(5L);
            }
            return hallazgo;
        });
        lenient().when(bitacoraObraRepository.save(any(BitacoraObra.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void paralizacionExigeAltoGraveYRegistraBitacora() {
        SomaHallazgoRequest request = requestHallazgo(PotencialPerdidaSoma.MEDIO_MODERADO, true);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> servicio.crearHallazgo(10L, request));
        assertEquals("La paralización de frente solo se permite con potencial ALTO_GRAVE", ex.getMessage());

        SomaHallazgoRequest grave = requestHallazgo(PotencialPerdidaSoma.ALTO_GRAVE, true);
        grave.setUbicacion("Frente A");
        grave.setDescripcion("Andamio sin baranda");
        SomaHallazgoDTO dto = servicio.crearHallazgo(10L, grave);

        assertTrue(dto.isParalizacionFrente());
        ArgumentCaptor<BitacoraObra> captor = ArgumentCaptor.forClass(BitacoraObra.class);
        verify(bitacoraObraRepository).save(captor.capture());
        assertEquals("[SOMA] Paralización de frente: Frente A — Andamio sin baranda", captor.getValue().getTexto());
        verify(auditoriaObraRegistrar)
                .registrar(
                        eq(10L),
                        eq(AccionAuditoriaObra.PARALIZAR_FRENTE_SOMA),
                        eq(AuditoriaObraRegistrar.ENTIDAD_HALLAZGO_SOMA),
                        eq(5L),
                        eq("[SOMA] Paralización de frente: Frente A — Andamio sin baranda"));
    }

    @Test
    void levantarExigeEvidenciaYCierraHoy() {
        SomaHallazgo hallazgo = hallazgoAbierto(LocalDate.now().plusDays(3));
        when(somaHallazgoRepository.findByIdAndProyectoId(5L, 10L)).thenReturn(Optional.of(hallazgo));

        IllegalArgumentException sinEvidencia =
                assertThrows(IllegalArgumentException.class, () -> servicio.levantar(10L, 5L, new SomaHallazgoLevantarRequest()));
        assertEquals("La evidencia de cierre es obligatoria", sinEvidencia.getMessage());

        DocumentoObra evidencia = new DocumentoObra();
        evidencia.setId(9L);
        evidencia.setTipo(TipoDocumentoObra.EVIDENCIA_SOMA);
        when(documentoObraRepository.findByIdAndProyectoId(9L, 10L)).thenReturn(Optional.of(evidencia));

        SomaHallazgoLevantarRequest request = new SomaHallazgoLevantarRequest();
        request.setEvidenciaDespuesDocId(9L);
        SomaHallazgoDTO dto = servicio.levantar(10L, 5L, request);

        assertEquals(EstadoHallazgoSoma.LEVANTADO, dto.getEstado());
        assertEquals(LocalDate.now(), dto.getFechaCierre());
        assertEquals(9L, dto.getEvidenciaDespuesDocId());
        assertFalse(dto.isVencido());
    }

    @Test
    void vencidoDerivado() {
        SomaHallazgo abiertoVencido = hallazgoAbierto(LocalDate.now().minusDays(1));
        abiertoVencido.setId(1L);
        SomaHallazgo levantadoVencido = hallazgoAbierto(LocalDate.now().minusDays(1));
        levantadoVencido.setId(2L);
        levantadoVencido.setEstado(EstadoHallazgoSoma.LEVANTADO);
        levantadoVencido.setFechaCierre(LocalDate.now());
        when(somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(10L))
                .thenReturn(List.of(abiertoVencido, levantadoVencido));

        List<SomaHallazgoDTO> dtos = servicio.listarHallazgos(10L, null, null);

        SomaHallazgoDTO abierto = dtos.stream().filter(item -> item.getId() == 1L).findFirst().orElseThrow();
        SomaHallazgoDTO cerrado = dtos.stream().filter(item -> item.getId() == 2L).findFirst().orElseThrow();
        assertTrue(abierto.isVencido());
        assertFalse(cerrado.isVencido());
    }

    private SomaHallazgoRequest requestHallazgo(PotencialPerdidaSoma potencial, boolean paralizacion) {
        SomaHallazgoRequest request = new SomaHallazgoRequest();
        request.setClasificacion(ClasificacionHallazgoSoma.CONDICION_SUBESTANDAR);
        request.setPotencialPerdida(potencial);
        request.setDescripcion("Hallazgo de prueba");
        request.setAccionCorrectiva("Corregir");
        request.setFechaDeteccion(LocalDate.now());
        request.setFechaLimite(LocalDate.now().plusDays(2));
        request.setParalizacionFrente(paralizacion);
        return request;
    }

    private SomaHallazgo hallazgoAbierto(LocalDate fechaLimite) {
        SomaHallazgo hallazgo = new SomaHallazgo();
        hallazgo.setId(5L);
        hallazgo.setProyecto(proyecto);
        hallazgo.setClasificacion(ClasificacionHallazgoSoma.ACTO_SUBESTANDAR);
        hallazgo.setPotencialPerdida(PotencialPerdidaSoma.BAJO_LEVE);
        hallazgo.setDescripcion("Uso de EPP");
        hallazgo.setAccionCorrectiva("Capacitar");
        hallazgo.setFechaDeteccion(fechaLimite.minusDays(1));
        hallazgo.setFechaLimite(fechaLimite);
        hallazgo.setEstado(EstadoHallazgoSoma.ABIERTO);
        return hallazgo;
    }
}
