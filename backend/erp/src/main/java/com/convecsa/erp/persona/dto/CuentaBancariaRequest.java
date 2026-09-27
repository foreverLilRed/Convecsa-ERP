package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.Banco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CuentaBancariaRequest {

    @NotNull
    private Banco banco;

    @NotBlank
    private String numeroCuenta;

    @NotBlank
    @Pattern(regexp = "\\d{20}", message = "El CCI debe tener exactamente 20 dígitos")
    private String cci;

    private boolean principal;
}
