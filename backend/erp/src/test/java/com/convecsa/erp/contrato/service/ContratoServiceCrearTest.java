package com.convecsa.erp.contrato.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import com.convecsa.erp.contrato.catalogo.RegimenLaboral;
import com.convecsa.erp.contrato.domain.UsoPlantilla;
import com.convecsa.erp.contrato.dto.ContratoDTO;
import com.convecsa.erp.contrato.dto.ContratoRequest;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.contrato.repository.CausaObjetivaRepository;
import com.convecsa.erp.contrato.repository.CategoriaContratoRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.repository.JornadaRepository;
import com.convecsa.erp.contrato.repository.MetodoPagoRepository;
import com.convecsa.erp.contrato.repository.PlantillaContratoRepository;
import com.convecsa.erp.contrato.repository.RegimenLaboralRepository;
import com.convecsa.erp.contrato.service.impl.ContratoServiceImpl;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.nomina.repository.AfpRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
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
class ContratoServiceCrearTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private CategoriaContratoRepository categoriaContratoRepository;

    @Mock
    private RegimenLaboralRepository regimenLaboralRepository;

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

    @Mock
    private com.convecsa.erp.construccion.repository.ContratoCcRepository contratoCcRepository;

    @Mock
    private com.convecsa.erp.construccion.repository.ProyectoRepository proyectoRepository;

    @InjectMocks
    private ContratoServiceImpl servicio;

    private Persona persona;
    private PlantillaContrato plantilla;

    @BeforeEach
    void setUp() {
        persona = new Persona();
        persona.setId(2L);
        persona.setNombres("Pedro");
        persona.setApellidos("Quispe");
        persona.setNumeroDocumento("12345678");

        plantilla = new PlantillaContrato();
        plantilla.setId(5L);
        plantilla.setCodigo("CONTRATO_BASE");
        plantilla.setNombre("Contrato base");
        plantilla.setUso(UsoPlantilla.CONTRATO);
        plantilla.setPlantilla("<p>{{trabajador_nombres}}</p>");
        plantilla.setActivo(true);
    }

    @Test
    void plazoIndeterminadoSinFechaFinSeGuarda() {
        CategoriaContrato modalidad = modalidad("RG_PLAZO_INDETERMINADO", "A Plazo Indeterminado", false);
        stubLookups(modalidad);
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> {
            Contrato contrato = invocation.getArgument(0);
            contrato.setId(11L);
            return contrato;
        });

        ContratoRequest request = requestBase();
        request.setFechaFin(null);

        ContratoDTO dto = servicio.crear(request);

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(captor.capture());
        assertNull(captor.getValue().getFechaFin());
        assertNull(dto.getFechaFin());
        assertEquals("REGIMEN_GENERAL", dto.getRegimenLaboralCodigo());
        assertEquals("RG_PLAZO_INDETERMINADO", dto.getCategoriaContratoCodigo());
    }

    @Test
    void plazoIndeterminadoIgnoraFechaFinEnviada() {
        CategoriaContrato modalidad = modalidad("RG_PLAZO_INDETERMINADO", "A Plazo Indeterminado", false);
        stubLookups(modalidad);
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> {
            Contrato contrato = invocation.getArgument(0);
            contrato.setId(12L);
            return contrato;
        });

        ContratoRequest request = requestBase();
        request.setFechaFin(LocalDate.of(2026, 12, 31));

        servicio.crear(request);

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(captor.capture());
        assertNull(captor.getValue().getFechaFin());
    }

    @Test
    void plazoFijoSinFechaFinFalla() {
        stubLookups(modalidad("RG_PLAZO_FIJO_OBRA", "Plazo Fijo - Por Obra Determinada o Servicio Específico", true));

        ContratoRequest request = requestBase();
        request.setFechaFin(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> servicio.crear(request));
        assertEquals("La fecha de fin es obligatoria para esta modalidad", ex.getMessage());
        verify(contratoRepository, never()).save(any());
    }

    @Test
    void rccSinFechaFinFalla() {
        stubLookups(modalidad("RCC_OBRA_PARTIDA", "Contrato por Obra o Partida Específica (RCC)", true));

        ContratoRequest request = requestBase();
        request.setFechaFin(null);

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(request));
        verify(contratoRepository, never()).save(any());
    }

    @Test
    void locacionSinFechaFinFalla() {
        stubLookups(modalidad("LOC_HONORARIOS", "Locación de Servicios Profesionales / Técnicos (Recibo por Honorarios)", true));

        ContratoRequest request = requestBase();
        request.setFechaFin(null);

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(request));
        verify(contratoRepository, never()).save(any());
    }

    private void stubLookups(CategoriaContrato modalidad) {
        when(personaRepository.findById(2L)).thenReturn(Optional.of(persona));
        when(categoriaContratoRepository.findById(7L)).thenReturn(Optional.of(modalidad));
        when(plantillaContratoRepository.findById(5L)).thenReturn(Optional.of(plantilla));
    }

    private static CategoriaContrato modalidad(String codigo, String label, boolean requiereFechaFin) {
        RegimenLaboral regimen = new RegimenLaboral();
        regimen.setId(1L);
        regimen.setCodigo(codigo.startsWith("RCC")
                ? "CONSTRUCCION_CIVIL"
                : codigo.startsWith("LOC") ? "LOCACION_SERVICIOS" : "REGIMEN_GENERAL");
        regimen.setLabel(regimen.getCodigo());
        regimen.setActivo(true);

        CategoriaContrato categoria = new CategoriaContrato();
        categoria.setId(7L);
        categoria.setCodigo(codigo);
        categoria.setLabel(label);
        categoria.setActivo(true);
        categoria.setRequiereFechaFin(requiereFechaFin);
        categoria.setRegimenLaboral(regimen);
        return categoria;
    }

    private static ContratoRequest requestBase() {
        ContratoRequest request = new ContratoRequest();
        request.setTitulo("Contrato de prueba");
        request.setCategoriaContratoId(7L);
        request.setPersonaId(2L);
        request.setTipoPlantillaId(5L);
        request.setFechaInicio(LocalDate.of(2026, 1, 15));
        request.setSueldo(new BigDecimal("2500.00"));
        return request;
    }
}
