package com.convecsa.erp.auth.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.usuario.domain.RolUsuario;
import com.convecsa.erp.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService("convecsa-erp-dev-jwt-secret-key-32bytes-min", 86_400_000);
        usuario = new Usuario();
        usuario.setId(42L);
        usuario.setUsername("superadmin");
        usuario.setRol(RolUsuario.SUPERADMIN);
    }

    @Test
    void generarYLeerToken() {
        String token = jwtTokenService.generar(usuario);

        assertTrue(jwtTokenService.esValido(token));
        assertEquals("superadmin", jwtTokenService.extraerUsername(token));
        assertEquals(RolUsuario.SUPERADMIN, jwtTokenService.extraerRol(token));
        assertEquals(42L, jwtTokenService.extraerUsuarioId(token));
    }

    @Test
    void tokenInvalidoNoSeAcepta() {
        assertFalse(jwtTokenService.esValido("token-invalido"));
    }
}
