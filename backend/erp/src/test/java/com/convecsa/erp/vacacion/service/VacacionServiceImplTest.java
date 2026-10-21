package com.convecsa.erp.vacacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.asistencia.domain.CodigoTareo;
import com.convecsa.erp.asistencia.domain.EstadoAsistencia;
import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.asistencia.repository.AsistenciaRegistroRepository;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
import com.convecsa.erp.vacacion.entity.VacacionSolicitud;
import com.convecsa.erp.vacacion.dto.VacacionRequest;
import com.convecsa.erp.vacacion.repository.VacacionSolicitudRepository;
import com.convecsa.erp.vacacion.service.impl.VacacionServiceImpl;
import com.convecsa.erp.vacacion.support.CalendarioVacacionService;
import com.convecsa.erp.vacacion.support.VacacionDocumentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class VacacionServiceImplTest {

    @Mock private VacacionSolicitudRepository vacacionRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private AsistenciaRegistroRepository asistenciaRepository;
    @Spy private CalendarioVacacionService calendarioVacacionService;
    @Mock private VacacionDocumentoService documentoService;

    @InjectMocks private VacacionServiceImpl service;

    private VacacionSolicitud solicitud;

    @BeforeEach
    void setUp() {
        Persona persona = new Persona();
        persona.setId(7L);
        persona.setNombres("Mayra");
        persona.setApellidos("Guillen Jimenez");
        persona.setNumeroDocumento("46636946");

        Contrato contrato = new Contrato();
        contrato.setId(10L);
        contrato.setPersona(persona);
        contrato.setSueldo(new BigDecimal("2500.00"));
        contrato.setFechaInicio(LocalDate.of(2025, 9, 23));

        solicitud = new VacacionSolicitud();
        solicitud.setId(15L);
        solicitud.setPersona(persona);
        solicitud.setContrato(contrato);
        solicitud.setTipo(TipoVacacion.ADELANTADA);
        solicitud.setEstado(EstadoVacacion.PENDIENTE);
        solicitud.setPeriodoInicio(LocalDate.of(2025, 9, 23));
        solicitud.setPeriodoFin(LocalDate.of(2026, 9, 22));
        solicitud.setDiasDerechoPeriodo(30);
        solicitud.setDiasSolicitados(10);
        solicitud.setFechaInicio(LocalDate.of(2026, 7, 28));
        solicitud.setFechaFin(LocalDate.of(2026, 8, 6));
        solicitud.setFechaRetorno(LocalDate.of(2026, 8, 7));
    }

    @Test
    void crearBloqueaPeriodoOcupadoAntesDeGuardar() {
        VacacionRequest request = request();
        when(personaRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(solicitud.getPersona()));
        when(contratoRepository.findUltimoContratoActivoPorPersonaId(7L))
                .thenReturn(Optional.of(solicitud.getContrato()));
        when(vacacionRepository.existeCrucePeriodo(7L,
                List.of(EstadoVacacion.PENDIENTE, EstadoVacacion.APROBADA),
                request.getPeriodoInicio(), request.getPeriodoFin())).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("período de derecho");
        verify(vacacionRepository, never()).save(any());
    }

    @Test
    void crearPermitePeriodoLibreTrasVerificarCruceDeDescanso() {
        VacacionRequest request = request();
        when(personaRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(solicitud.getPersona()));
        when(contratoRepository.findUltimoContratoActivoPorPersonaId(7L))
                .thenReturn(Optional.of(solicitud.getContrato()));
        when(vacacionRepository.save(any())).thenAnswer(invocation -> {
            VacacionSolicitud nueva = invocation.getArgument(0);
            nueva.setId(16L);
            return nueva;
        });

        var resultado = service.crear(request);

        assertThat(resultado.id()).isEqualTo(16L);
        verify(vacacionRepository).existeCrucePeriodo(7L,
                List.of(EstadoVacacion.PENDIENTE, EstadoVacacion.APROBADA),
                request.getPeriodoInicio(), request.getPeriodoFin());
        verify(vacacionRepository).existeCruce(7L,
                List.of(EstadoVacacion.PENDIENTE, EstadoVacacion.APROBADA),
                request.getFechaInicio(), request.getFechaInicio());
    }

    private static VacacionRequest request() {
        VacacionRequest request = new VacacionRequest();
        request.setPersonaId(7L);
        request.setTipo(TipoVacacion.FRACCIONADA);
        request.setPeriodoInicio(LocalDate.of(2026, 9, 23));
        request.setPeriodoFin(LocalDate.of(2027, 9, 22));
        request.setDiasDerechoPeriodo(30);
        request.setDiasSolicitados(1);
        request.setFechaInicio(LocalDate.of(2026, 10, 1));
        return request;
    }

    @Test
    void listarSinFiltrosNoEnviaParametrosNulosAlRepositorio() {
        PageRequest pageable = PageRequest.of(0, 15);
        when(vacacionRepository.buscar(
                false, "",
                false, TipoVacacion.ORDINARIA,
                false, EstadoVacacion.PENDIENTE,
                pageable)).thenReturn(new PageImpl<>(List.of(solicitud), pageable, 1));
        when(vacacionRepository.sumDiasPeriodo(any(), any(), any(), any())).thenReturn(0L);

        var resultado = service.listar(0, 15, null, null, null);

        assertThat(resultado.getContent()).hasSize(1);
        verify(vacacionRepository).buscar(
                false, "",
                false, TipoVacacion.ORDINARIA,
                false, EstadoVacacion.PENDIENTE,
                pageable);
    }

    @Test
    void aprobarReservaSaldoYSincronizaElTareoExistente() {
        AsistenciaRegistro registro = new AsistenciaRegistro();
        registro.setPersona(solicitud.getPersona());
        registro.setFecha(LocalDate.of(2026, 7, 28));
        registro.setCodigo(CodigoTareo.A);
        registro.setEstado(EstadoAsistencia.ASISTIO);
        registro.setHorasTrabajadas(new BigDecimal("8.00"));
        registro.setMinutosExtras25(30);

        when(vacacionRepository.findById(15L)).thenReturn(Optional.of(solicitud));
        when(vacacionRepository.sumDiasPeriodo(any(), any(), any(), any())).thenReturn(0L, 10L);
        when(asistenciaRepository.findByPersonaIdAndFechaBetweenOrderByFechaAsc(
                7L, solicitud.getFechaInicio(), solicitud.getFechaFin())).thenReturn(List.of(registro));
        when(vacacionRepository.save(solicitud)).thenReturn(solicitud);

        service.aprobar(15L);

        assertThat(solicitud.getEstado()).isEqualTo(EstadoVacacion.APROBADA);
        assertThat(registro.getCodigo()).isEqualTo(CodigoTareo.VAC);
        assertThat(registro.getEstado()).isEqualTo(EstadoAsistencia.PERMISO);
        assertThat(registro.getHorasTrabajadas()).isEqualByComparingTo("0.00");
        assertThat(registro.getMinutosExtras25()).isZero();
        verify(asistenciaRepository).saveAll(List.of(registro));
    }
}
