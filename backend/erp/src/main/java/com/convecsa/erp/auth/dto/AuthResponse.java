package com.convecsa.erp.auth.dto;

import com.convecsa.erp.usuario.domain.RolUsuario;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private final String token;
    private final String username;
    private final String correo;
    private final RolUsuario rol;
    private final Long usuarioId;
}
