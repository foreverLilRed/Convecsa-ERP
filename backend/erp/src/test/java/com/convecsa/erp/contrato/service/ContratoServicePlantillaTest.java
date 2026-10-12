package com.convecsa.erp.contrato.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.PlantillaNoEncontradaException;
import com.convecsa.erp.common.exception.PlantillaPinInvalidoException;
import com.convecsa.erp.contrato.domain.UsoPlantilla;
import com.convecsa.erp.contrato.dto.PlantillaContratoDetalleDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoUpdateRequest;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.contrato.repository.CausaObjetivaRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.repository.JornadaRepository;
import com.convecsa.erp.contrato.repository.MetodoPagoRepository;
import com.convecsa.erp.contrato.repository.PlantillaContratoRepository;
import com.convecsa.erp.contrato.service.impl.ContratoServiceImpl;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.nomina.repository.AfpRepository;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ContratoServicePlantillaTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private JornadaRepository jornadaRepository;

    @Mock
    private CausaObjetivaRepository causaObjetivaRepository;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @Mock
    private PlantillaContratoRepository plantillaContratoRepository;

    @Mock
    private AfpRepository afpRepository;

    @Mock
    private ContratoPlantillaService contratoPlantillaService;

    @InjectMocks
    private ContratoServiceImpl servicio;

    private PlantillaContrato plantilla;
    private PlantillaContratoUpdateRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(servicio, "plantillasPin", "6283");

        plantilla = new PlantillaContrato();
        plantilla.setId(1L);
        plantilla.setCodigo("CSE");
        plantilla.setNombre("Servicio específico");
        plantilla.setUso(UsoPlantilla.CONTRATO);
        plantilla.setPlantilla("<html>{{trabajador_nombres}}</html>");
        plantilla.setActivo(true);

        request = new PlantillaContratoUpdateRequest();
        request.setNombre("Servicio específico v2");
        request.setPlantilla("<html>{{meta.lugar_firma}}</html>");
        request.setUso(UsoPlantilla.ADENDA);
        request.setPin("6283");
    }

    @Test
    void actualizarConPinIncorrectoFalla() {
        request.setPin("0000");

        assertThrows(PlantillaPinInvalidoException.class, () -> servicio.actualizarPlantilla(1L, request));
        verify(plantillaContratoRepository, never()).save(any());
    }

    @Test
    void actualizarConPinCorrectoGuardaCambiosYRecalculaMetadata() {
        when(plantillaContratoRepository.findById(1L)).thenReturn(Optional.of(plantilla));
        when(plantillaContratoRepository.save(plantilla)).thenReturn(plantilla);

        PlantillaContratoDetalleDTO detalle = servicio.actualizarPlantilla(1L, request);

        assertEquals("Servicio específico v2", detalle.getNombre());
        assertEquals(UsoPlantilla.ADENDA, detalle.getUso());
        assertEquals("<html>{{meta.lugar_firma}}</html>", detalle.getPlantilla());
        assertEquals(List.of("lugar_firma"), detalle.getMetadata().stream().map(c -> c.getClave()).toList());
        verify(plantillaContratoRepository).save(plantilla);
    }

    @Test
    void obtenerPlantillaInexistenteFalla() {
        when(plantillaContratoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PlantillaNoEncontradaException.class, () -> servicio.obtenerPlantilla(99L));
    }
}
