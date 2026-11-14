package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.soma.domain.NivelRiesgoIperc;
import com.convecsa.erp.soma.domain.TipoPeligroIperc;
import com.convecsa.erp.soma.dto.SomaIpercFilaDTO;
import com.convecsa.erp.soma.dto.SomaIpercFilaRequest;
import com.convecsa.erp.soma.entity.SomaIpercFila;
import com.convecsa.erp.soma.repository.SomaIpercFilaRepository;
import com.convecsa.erp.soma.service.impl.SomaIpercServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaIpercServiceImplTest {

    @Mock
    private SomaIpercFilaRepository somaIpercFilaRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;

    @InjectMocks
    private SomaIpercServiceImpl servicio;

    private Proyecto proyecto;
    private PartidaPresupuesto partida;

    @BeforeEach
    void stubBasico() {
        proyecto = new Proyecto();
        proyecto.setId(10L);
        partida = partidaDeObra(10L, 20L);
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        lenient().when(partidaPresupuestoRepository.findById(20L)).thenReturn(Optional.of(partida));
        lenient().when(somaIpercFilaRepository.save(any(SomaIpercFila.class))).thenAnswer(invocation -> {
            SomaIpercFila fila = invocation.getArgument(0);
            if (fila.getId() == null) {
                fila.setId(7L);
            }
            return fila;
        });
    }

    @Test
    void sinControlesRechaza() {
        SomaIpercFilaRequest request = requestValida();
        request.setControlEpp(null);

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));
    }

    @Test
    void residualMayorQueInicialRechaza() {
        SomaIpercFilaRequest request = requestValida();
        request.setProbabilidadInicial(2);
        request.setSeveridadInicial(2);
        request.setProbabilidadResidual(5);
        request.setSeveridadResidual(5);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));
        assertEquals("El nivel residual no puede ser mayor que el inicial", ex.getMessage());
    }

    @Test
    void crearCalculaNiveles() {
        SomaIpercFilaRequest request = requestValida();
        request.setProbabilidadInicial(3);
        request.setSeveridadInicial(3);
        request.setProbabilidadResidual(2);
        request.setSeveridadResidual(2);

        SomaIpercFilaDTO dto = servicio.crear(10L, request);

        assertEquals(NivelRiesgoIperc.MODERADO, dto.getNivelRiesgoInicial());
        assertEquals(NivelRiesgoIperc.TRIVIAL, dto.getNivelRiesgoResidual());
        assertEquals(20L, dto.getPartidaId());
        assertEquals("P-01", dto.getPartidaCodigo());
    }

    @Test
    void desactivarDejaInactivo() {
        SomaIpercFila fila = new SomaIpercFila();
        fila.setId(7L);
        fila.setProyecto(proyecto);
        fila.setPartida(partida);
        fila.setActivo(true);
        when(somaIpercFilaRepository.findByIdAndProyectoId(7L, 10L)).thenReturn(Optional.of(fila));

        SomaIpercFilaDTO dto = servicio.desactivar(10L, 7L);

        assertFalse(dto.isActivo());
        assertFalse(fila.isActivo());
    }

    @Test
    void partidaDeOtraObraRechaza() {
        PartidaPresupuesto ajena = partidaDeObra(99L, 88L);
        when(partidaPresupuestoRepository.findById(88L)).thenReturn(Optional.of(ajena));
        SomaIpercFilaRequest request = requestValida();
        request.setPartidaId(88L);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));
        assertEquals("La partida no pertenece a esta obra", ex.getMessage());
    }

    private static SomaIpercFilaRequest requestValida() {
        SomaIpercFilaRequest request = new SomaIpercFilaRequest();
        request.setPartidaId(20L);
        request.setActividadTarea("Excavación");
        request.setPuestoTrabajo("Operario");
        request.setRutinario(true);
        request.setTipoPeligro(TipoPeligroIperc.MECANICO);
        request.setPeligro("Atrapamiento");
        request.setRiesgo("Lesión por maquinaria");
        request.setConsecuencia("Fractura");
        request.setProbabilidadInicial(3);
        request.setSeveridadInicial(3);
        request.setProbabilidadResidual(2);
        request.setSeveridadResidual(2);
        request.setControlEpp("Casco y guantes");
        return request;
    }

    private static PartidaPresupuesto partidaDeObra(Long proyectoId, Long partidaId) {
        Proyecto obra = new Proyecto();
        obra.setId(proyectoId);
        PresupuestoVersion version = new PresupuestoVersion();
        version.setId(proyectoId);
        version.setProyecto(obra);
        PartidaPresupuesto partida = new PartidaPresupuesto();
        partida.setId(partidaId);
        partida.setCodigo("P-01");
        partida.setDescripcion("Movimiento de tierras");
        partida.setVersion(version);
        return partida;
    }
}
