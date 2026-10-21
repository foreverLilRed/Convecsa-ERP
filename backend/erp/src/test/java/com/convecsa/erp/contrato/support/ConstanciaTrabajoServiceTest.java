package com.convecsa.erp.contrato.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConstanciaTrabajoServiceTest {

    @Mock private ContratoRepository contratos;

    @Test
    void generaConstanciaVigenteConCargoYProyectoDelContrato() {
        Contrato contrato = contrato();
        contrato.setMetadata(Map.of("cargo", "Supervisor <obra>", "proyecto", "Hospital & sede"));
        when(contratos.findById(10L)).thenReturn(Optional.of(contrato));

        String html = new ConstanciaTrabajoService(contratos).generar(10L);

        assertThat(html).contains("hasta la fecha", "Supervisor &lt;obra&gt;", "Hospital &amp; sede",
                "ANA ROJAS", "<img src=\"data:image/png;base64,");
        assertThat(html).doesNotContain("Supervisor <obra>", "Hospital & sede", "Imprimir / guardar PDF");
    }

    @Test
    void contratoFinalizadoUsaFechaDeCorte() {
        Contrato contrato = contrato();
        contrato.setActivo(false);
        contrato.setFechaFin(LocalDate.of(2025, 12, 31));
        contrato.setFechaCorte(LocalDate.of(2025, 9, 30));
        when(contratos.findById(10L)).thenReturn(Optional.of(contrato));

        String html = new ConstanciaTrabajoService(contratos).generar(10L);

        assertThat(html).contains("hasta el <strong>30 de setiembre de 2025</strong>");
        assertThat(html).doesNotContain("hasta la fecha");
    }

    @Test
    void rechazaContratoSinInicio() {
        Contrato contrato = contrato();
        contrato.setFechaInicio(null);
        when(contratos.findById(10L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> new ConstanciaTrabajoService(contratos).generar(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de inicio");
    }

    private static Contrato contrato() {
        Persona persona = new Persona();
        persona.setNombres("ANA");
        persona.setApellidos("ROJAS");
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("12345678");
        Contrato contrato = new Contrato();
        contrato.setId(10L);
        contrato.setPersona(persona);
        contrato.setTitulo("Ingeniera de sistemas");
        contrato.setFechaInicio(LocalDate.of(2025, 1, 1));
        contrato.setActivo(true);
        return contrato;
    }
}
