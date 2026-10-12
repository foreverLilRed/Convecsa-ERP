package com.convecsa.erp.offboarding.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.ContratoYaCesadoException;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.offboarding.domain.TipoCese;
import com.convecsa.erp.offboarding.dto.CeseDTO;
import com.convecsa.erp.offboarding.dto.CeseRequest;
import com.convecsa.erp.offboarding.entity.Cese;
import com.convecsa.erp.offboarding.repository.CeseRepository;
import com.convecsa.erp.offboarding.service.impl.OffboardingServiceImpl;
import com.convecsa.erp.offboarding.support.CalculoLiquidacionService;
import com.convecsa.erp.onboarding.repository.GeneracionOnboardingRepository;
import com.convecsa.erp.onboarding.repository.PlantillaOnboardingRepository;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffboardingServiceImplTest {

    @Mock
    private CeseRepository ceseRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PlantillaOnboardingRepository plantillaOnboardingRepository;

    @Mock
    private GeneracionOnboardingRepository generacionOnboardingRepository;

    @Spy
    private CalculoLiquidacionService calculoLiquidacionService = new CalculoLiquidacionService();

    @InjectMocks
    private OffboardingServiceImpl servicio;

    private Persona persona;
    private Contrato contrato;
    private CeseRequest request;

    @BeforeEach
    void setUp() {
        persona = new Persona();
        persona.setId(4L);
        persona.setNombres("Ana");
        persona.setApellidos("Lopez");
        persona.setNumeroDocumento("87654321");

        contrato = new Contrato();
        contrato.setId(9L);
        contrato.setPersona(persona);
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setFechaInicio(LocalDate.of(2026, 1, 15));
        contrato.setSueldo(new BigDecimal("1500.00"));

        request = new CeseRequest();
        request.setPersonaId(4L);
        request.setFechaCese(LocalDate.of(2026, 10, 11));
        request.setTipoCese(TipoCese.RENUNCIA);
        request.setMotivo("Término de obra");
    }

    @Test
    void registrarCeseRescindeContratoYGuardaSnapshot() {
        when(personaRepository.existsById(4L)).thenReturn(true);
        when(contratoRepository.findFirstByPersonaIdAndEstadoOrderByCreatedAtDesc(4L, EstadoContrato.ACTIVO))
                .thenReturn(Optional.of(contrato));
        when(ceseRepository.existsByContratoId(9L)).thenReturn(false);
        when(ceseRepository.save(any(Cese.class))).thenAnswer(invocation -> {
            Cese cese = invocation.getArgument(0);
            cese.setId(3L);
            return cese;
        });
        when(contratoRepository.save(contrato)).thenReturn(contrato);

        CeseDTO dto = servicio.registrarCese(request);

        assertEquals(EstadoContrato.RESCINDIDO, contrato.getEstado());
        assertEquals(LocalDate.of(2026, 10, 11), contrato.getFechaCorte());
        assertEquals(new BigDecimal("1266.67"), dto.getLiquidacion().getTotal());
        ArgumentCaptor<Cese> captor = ArgumentCaptor.forClass(Cese.class);
        verify(ceseRepository).save(captor.capture());
        assertEquals(TipoCese.RENUNCIA, captor.getValue().getTipoCese());
    }

    @Test
    void registrarCeseDuplicadoFalla() {
        when(personaRepository.existsById(4L)).thenReturn(true);
        when(contratoRepository.findFirstByPersonaIdAndEstadoOrderByCreatedAtDesc(4L, EstadoContrato.ACTIVO))
                .thenReturn(Optional.of(contrato));
        when(ceseRepository.existsByContratoId(9L)).thenReturn(true);

        assertThrows(ContratoYaCesadoException.class, () -> servicio.registrarCese(request));
        verify(ceseRepository, never()).save(any());
    }

    @Test
    void previewSinContratoActivoFalla() {
        when(personaRepository.existsById(4L)).thenReturn(true);
        when(contratoRepository.findFirstByPersonaIdAndEstadoOrderByCreatedAtDesc(4L, EstadoContrato.ACTIVO))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.previewLiquidacion(4L, LocalDate.of(2026, 10, 11)));
    }
}
