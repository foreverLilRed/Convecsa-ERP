package com.convecsa.erp.contrato.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.contrato.domain.UsoPlantilla;
import com.convecsa.erp.contrato.dto.AdendaDTO;
import com.convecsa.erp.contrato.dto.AdendaRequest;
import com.convecsa.erp.contrato.entity.Adenda;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.contrato.repository.AdendaRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.repository.PlantillaContratoRepository;
import com.convecsa.erp.contrato.service.impl.AdendaServiceImpl;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdendaServiceImplTest {

    @Mock
    private AdendaRepository adendaRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private PlantillaContratoRepository plantillaContratoRepository;

    @Mock
    private ContratoPlantillaService contratoPlantillaService;

    @InjectMocks
    private AdendaServiceImpl servicio;

    private Contrato contrato;
    private PlantillaContrato plantilla;

    @BeforeEach
    void setUp() {
        Persona persona = new Persona();
        persona.setId(3L);
        persona.setNombres("Pedro");
        persona.setApellidos("Quispe");
        persona.setNumeroDocumento("12345678");

        contrato = new Contrato();
        contrato.setId(10L);
        contrato.setPersona(persona);
        contrato.setActivo(true);
        contrato.setFechaInicio(LocalDate.of(2026, 1, 15));
        contrato.setFechaFin(LocalDate.of(2026, 7, 15));
        contrato.setSueldo(new BigDecimal("2500.00"));

        plantilla = new PlantillaContrato();
        plantilla.setId(8L);
        plantilla.setCodigo("ADENDA_PRORROGA");
        plantilla.setNombre("Adenda de prórroga");
        plantilla.setUso(UsoPlantilla.ADENDA);
        plantilla.setPlantilla("<p>{{motivo}}</p>");
    }

    @Test
    void correlativoUnoLuegoDos() {
        when(contratoRepository.findById(10L)).thenReturn(Optional.of(contrato));
        when(plantillaContratoRepository.findById(8L)).thenReturn(Optional.of(plantilla));
        when(adendaRepository.countByContratoId(10L)).thenReturn(0L, 1L);
        when(adendaRepository.save(any(Adenda.class))).thenAnswer(invocation -> {
            Adenda adenda = invocation.getArgument(0);
            if (adenda.getId() == null) {
                adenda.setId((long) adenda.getNumero());
            }
            return adenda;
        });
        when(contratoRepository.save(contrato)).thenReturn(contrato);

        AdendaDTO primera = servicio.crear(request(LocalDate.of(2026, 10, 15)));
        contrato.setFechaFin(LocalDate.of(2026, 10, 15));
        AdendaDTO segunda = servicio.crear(request(LocalDate.of(2027, 1, 15)));

        assertEquals(1, primera.getNumero());
        assertEquals(2, segunda.getNumero());
    }

    @Test
    void rechazaFechaNuevaNoPosterior() {
        when(contratoRepository.findById(10L)).thenReturn(Optional.of(contrato));
        when(plantillaContratoRepository.findById(8L)).thenReturn(Optional.of(plantilla));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crear(request(LocalDate.of(2026, 7, 15))));
        assertEquals("La nueva fecha de término debe ser posterior al vencimiento actual.", error.getMessage());
    }

    @Test
    void actualizaFechaFinDelContrato() {
        when(contratoRepository.findById(10L)).thenReturn(Optional.of(contrato));
        when(plantillaContratoRepository.findById(8L)).thenReturn(Optional.of(plantilla));
        when(adendaRepository.countByContratoId(10L)).thenReturn(0L);
        when(adendaRepository.save(any(Adenda.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contratoRepository.save(contrato)).thenReturn(contrato);

        servicio.crear(request(LocalDate.of(2026, 10, 15)));

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(captor.capture());
        assertEquals(LocalDate.of(2026, 10, 15), captor.getValue().getFechaFin());
        assertEquals(new BigDecimal("2800.00"), captor.getValue().getSueldo());
    }

    private AdendaRequest request(LocalDate fechaFinNueva) {
        AdendaRequest request = new AdendaRequest();
        request.setContratoId(10L);
        request.setPlantillaContratoId(8L);
        request.setFechaFinNueva(fechaFinNueva);
        request.setSueldo(new BigDecimal("2800.00"));
        request.setMotivo("necesidades operativas del proyecto y acuerdo mutuo de las partes");
        request.setCiudadEmision("Lima");
        request.setFechaEmision(LocalDate.of(2026, 7, 1));
        return request;
    }
}
