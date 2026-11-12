package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.usuario.domain.RolUsuario;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsuarioAsignableDTO {

    private final Long id;
    private final String username;
    private final RolUsuario rol;
}
