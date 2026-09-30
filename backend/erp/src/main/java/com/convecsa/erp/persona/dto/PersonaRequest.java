package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.AptitudMedica;
import com.convecsa.erp.persona.domain.GradoInstruccion;
import com.convecsa.erp.persona.domain.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaRequest {

    @NotBlank
    @Size(max = 120)
    private String apellidos;

    @NotBlank
    @Size(max = 120)
    private String nombres;

    @NotNull
    private TipoDocumento tipoDocumento;

    @NotBlank
    @Size(max = 30)
    private String numeroDocumento;

    @NotNull
    private LocalDate fechaNacimiento;

    private LocalDate fechaEmisionDni;

    private LocalDate fechaCaducidadDni;

    @NotEmpty
    private List<@NotBlank String> celulares = new ArrayList<>();

    @NotBlank
    @Email
    @Size(max = 180)
    private String correo;

    @Email
    @Size(max = 180)
    private String correoSecundario;

    @NotNull
    private GradoInstruccion gradoInstruccion;

    @NotBlank
    @Size(max = 255)
    private String direccion;

    @NotBlank
    @Size(max = 80)
    private String departamento;

    @NotBlank
    @Size(max = 80)
    private String provincia;

    @NotBlank
    @Size(max = 80)
    private String distrito;

    @NotBlank
    @Size(max = 120)
    private String residencia;

    @Size(max = 500)
    private String fotoUrl;

    @Valid
    private List<CuentaBancariaRequest> cuentasBancarias = new ArrayList<>();

    @Valid
    private List<ExperienciaLaboralRequest> experienciasLaborales = new ArrayList<>();

    private LocalDate fechaVigenciaEmo;

    private AptitudMedica aptitudMedica;

    @Size(max = 2000)
    private String restriccionesMedicas;

    @AssertTrue(message = "Las fechas de emisión y caducidad del DNI son obligatorias")
    public boolean isFechasDniValidas() {
        if (tipoDocumento != TipoDocumento.DNI) {
            return true;
        }
        return fechaEmisionDni != null && fechaCaducidadDni != null;
    }

    @AssertTrue(message = "Debe marcar exactamente una cuenta como principal")
    public boolean isCuentaPrincipalValida() {
        if (cuentasBancarias == null || cuentasBancarias.isEmpty()) {
            return true;
        }
        long principales = cuentasBancarias.stream().filter(CuentaBancariaRequest::isPrincipal).count();
        return principales == 1;
    }

    @AssertTrue(message = "La fecha de vigencia de EMO es obligatoria si hay aptitud u observaciones")
    public boolean isFechaVigenciaEmoRequerida() {
        if (aptitudMedica == null && blank(restriccionesMedicas)) {
            return true;
        }
        return fechaVigenciaEmo != null;
    }

    @AssertTrue(message = "La aptitud médica es obligatoria si hay fecha de vigencia de EMO")
    public boolean isAptitudMedicaRequerida() {
        if (fechaVigenciaEmo == null) {
            return true;
        }
        return aptitudMedica != null;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
