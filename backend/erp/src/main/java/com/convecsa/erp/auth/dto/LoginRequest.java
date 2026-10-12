package com.convecsa.erp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Ingrese su usuario o correo")
    private String username;

    @NotBlank(message = "Ingrese su contraseña")
    private String password;
}
