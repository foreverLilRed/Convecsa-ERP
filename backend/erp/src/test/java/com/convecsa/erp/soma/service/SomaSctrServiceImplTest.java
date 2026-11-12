package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaDocumentoChecklist;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import com.convecsa.erp.soma.dto.SomaSctrNominaFilaRequest;
import com.convecsa.erp.soma.dto.SomaSctrNominaGuardarRequest;
import com.convecsa.erp.soma.dto.SomaSctrPolizaRequest;
import com.convecsa.erp.soma.dto.SomaTrabajadorObraDTO;
import com.convecsa.erp.soma.entity.SomaSctrPoliza;
import com.convecsa.erp.soma.repository.SomaSctrPolizaRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.impl.SomaSctrServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaSctrServiceImplTest {

    @Mock
    private SomaSctrPolizaRepository somaSctrPolizaRepository;

    @Mock
    private SomaTrabajadorObraRepository somaTrabajadorObraRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private SomaSctrServiceImpl servicio;

    @Test
    void rechazaPolizaDuplicadaDelMismoMes() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(somaSctrPolizaRepository.existsByProyectoIdAndAnioAndMes(10L, 2026, 3)).thenReturn(true);

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, requestPoliza(2026, 3)));

        assertEquals("Ya existe una póliza SCTR para ese mes", error.getMessage());
        verify(somaSctrPolizaRepository, never()).save(any());
    }

    @Test
    void guardarNominaDelMesEnCursoActualizaChecklist() {
        YearMonth actual = YearMonth.now();
        Persona cubierto = persona(1L, "Ana", "García");
        Persona excluido = persona(2L, "Luis", "Pérez");
        SomaSctrPoliza poliza = poliza(10L, actual.getYear(), actual.getMonthValue());
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(somaSctrPolizaRepository.findByIdAndProyectoId(5L, 10L)).thenReturn(Optional.of(poliza));
        when(personaRepository.findAllById(any())).thenReturn(List.of(cubierto));
        when(somaTrabajadorObraRepository.buscarActivosEnMes(10L, actual.getYear(), actual.getMonthValue()))
                .thenReturn(List.of(contratoCc(cubierto), contratoCc(excluido)));

        servicio.guardarNomina(10L, 5L, nomina(fila(1L, true, true)));

        assertEquals(EstadoDocumentoChecklist.CONFORME, estadoSctr(cubierto));
        assertEquals(EstadoDocumentoChecklist.PENDIENTE, estadoSctr(excluido));
    }

    @Test
    void guardarNominaDeMesPasadoNoTocaChecklist() {
        YearMonth pasado = YearMonth.now().minusMonths(1);
        Persona cubierto = persona(1L, "Ana", "García");
        Persona excluido = persona(2L, "Luis", "Pérez");
        SomaSctrPoliza poliza = poliza(10L, pasado.getYear(), pasado.getMonthValue());
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(somaSctrPolizaRepository.findByIdAndProyectoId(5L, 10L)).thenReturn(Optional.of(poliza));
        when(personaRepository.findAllById(any())).thenReturn(List.of(cubierto));

        servicio.guardarNomina(10L, 5L, nomina(fila(1L, true, true)));

        assertTrue(cubierto.getDocumentosChecklist().isEmpty());
        assertTrue(excluido.getDocumentosChecklist().isEmpty());
        verify(somaTrabajadorObraRepository, never()).buscarActivosEnMes(anyLong(), anyInt(), anyInt());
    }

    @Test
    void sugerenciaRespetaVigenciaDelContrato() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        Persona incluida = persona(1L, "Ana", "García");
        ContratoCc cesoEnMarzo = contratoCc(incluida, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 15));
        when(somaTrabajadorObraRepository.buscarActivosEnMes(10L, 2026, 3)).thenReturn(List.of(cesoEnMarzo));

        List<SomaTrabajadorObraDTO> sugeridos = servicio.sugerirNomina(10L, 2026, 3);

        assertEquals(1, sugeridos.size());
        assertEquals(1L, sugeridos.get(0).getPersonaId());
        assertEquals(LocalDate.of(2026, 3, 15), sugeridos.get(0).getFechaCese());
        verify(somaTrabajadorObraRepository).buscarActivosEnMes(eq(10L), eq(2026), eq(3));
    }

    @Test
    void buscarActivosEnMesUsaPrimerYUltimoDia() {
        when(somaTrabajadorObraRepository.buscarActivosEnMes(anyLong(), anyInt(), anyInt())).thenCallRealMethod();
        when(somaTrabajadorObraRepository.buscarActivos(
                        eq(10L), eq(LocalDate.of(2026, 3, 1)), eq(LocalDate.of(2026, 3, 31))))
                .thenReturn(List.of());

        List<ContratoCc> resultado = somaTrabajadorObraRepository.buscarActivosEnMes(10L, 2026, 3);

        assertTrue(resultado.isEmpty());
        verify(somaTrabajadorObraRepository)
                .buscarActivos(eq(10L), eq(LocalDate.of(2026, 3, 1)), eq(LocalDate.of(2026, 3, 31)));
    }

    private static Proyecto obra(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(id);
        proyecto.setCodigo("OB" + id);
        proyecto.setNombre("Obra " + id);
        return proyecto;
    }

    private static SomaSctrPoliza poliza(Long proyectoId, int anio, int mes) {
        SomaSctrPoliza poliza = new SomaSctrPoliza();
        poliza.setId(5L);
        poliza.setProyecto(obra(proyectoId));
        poliza.setAnio(anio);
        poliza.setMes(mes);
        poliza.setEstado(EstadoSctrPoliza.VIGENTE);
        return poliza;
    }

    private static SomaSctrPolizaRequest requestPoliza(int anio, int mes) {
        SomaSctrPolizaRequest request = new SomaSctrPolizaRequest();
        request.setAnio(anio);
        request.setMes(mes);
        request.setAseguradoraSalud("Rimac");
        request.setNumeroPolizaSalud("SAL-1");
        request.setAseguradoraPension("Mapfre");
        request.setNumeroPolizaPension("PEN-1");
        request.setFechaInicio(LocalDate.of(anio, mes, 1));
        request.setFechaFin(YearMonth.of(anio, mes).atEndOfMonth());
        request.setMontoPrimaSalud(BigDecimal.ONE);
        request.setMontoPrimaPension(BigDecimal.ONE);
        return request;
    }

    private static SomaSctrNominaGuardarRequest nomina(SomaSctrNominaFilaRequest... filas) {
        SomaSctrNominaGuardarRequest request = new SomaSctrNominaGuardarRequest();
        request.setFilas(List.of(filas));
        return request;
    }

    private static SomaSctrNominaFilaRequest fila(Long personaId, boolean salud, boolean pension) {
        SomaSctrNominaFilaRequest fila = new SomaSctrNominaFilaRequest();
        fila.setPersonaId(personaId);
        fila.setIncluidoSalud(salud);
        fila.setIncluidoPension(pension);
        return fila;
    }

    private static Persona persona(Long id, String nombres, String apellidos) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        persona.setNumeroDocumento("1000000" + id);
        return persona;
    }

    private static ContratoCc contratoCc(Persona persona) {
        return contratoCc(persona, LocalDate.of(2026, 1, 1), null);
    }

    private static ContratoCc contratoCc(Persona persona, LocalDate inicio, LocalDate fin) {
        Contrato contrato = new Contrato();
        contrato.setId(persona.getId());
        contrato.setPersona(persona);
        contrato.setActivo(true);
        contrato.setFechaInicio(inicio);
        contrato.setFechaFin(fin);
        ContratoCc cc = new ContratoCc();
        cc.setContratoId(contrato.getId());
        cc.setContrato(contrato);
        cc.setCategoria(CategoriaObrera.OPERARIO);
        return cc;
    }

    private static EstadoDocumentoChecklist estadoSctr(Persona persona) {
        return persona.getDocumentosChecklist().stream()
                .filter(item -> item.getTipoDocumento() == TipoDocumentoChecklist.SCTR)
                .map(PersonaDocumentoChecklist::getEstado)
                .findFirst()
                .orElseThrow();
    }
}
