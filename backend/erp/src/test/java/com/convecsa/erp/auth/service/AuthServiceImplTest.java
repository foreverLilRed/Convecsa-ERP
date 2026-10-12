package com.convecsa.erp.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.auth.dto.AuthResponse;
import com.convecsa.erp.auth.dto.LoginRequest;
import com.convecsa.erp.auth.service.impl.AuthServiceImpl;
import com.convecsa.erp.auth.support.JwtTokenService;
import com.convecsa.erp.common.exception.CredencialesInvalidasException;
import com.convecsa.erp.usuario.domain.RolUsuario;
import com.convecsa.erp.usuario.entity.Usuario;
import com.convecsa.erp.usuario.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthServiceImpl servicio;

    private Usuario usuario;
    private LoginRequest request;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setUsername("superadmin");
        usuario.setCorreo("ethan.b@example.com");
        usuario.setPasswordHash("hash");
        usuario.setRol(RolUsuario.SUPERADMIN);
        usuario.setActivo(true);

        request = new LoginRequest();
        request.setUsername("superadmin");
        request.setPassword("Convecsa2026!");
    }

    @Test
    void loginConUsernameDevuelveTokenYPerfil() {
        when(usuarioRepository.findByUsernameOrCorreoIgnoreCase("superadmin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Convecsa2026!", "hash")).thenReturn(true);
        when(jwtTokenService.generar(usuario)).thenReturn("jwt-token");

        AuthResponse response = servicio.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("superadmin", response.getUsername());
        assertEquals("ethan.b@example.com", response.getCorreo());
        assertEquals(RolUsuario.SUPERADMIN, response.getRol());
        verify(jwtTokenService).generar(usuario);
    }

    @Test
    void loginConCorreoEncuentraUsuario() {
        request.setUsername("ethan.b@example.com");
        when(usuarioRepository.findByUsernameOrCorreoIgnoreCase("ethan.b@example.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Convecsa2026!", "hash")).thenReturn(true);
        when(jwtTokenService.generar(usuario)).thenReturn("jwt-token");

        AuthResponse response = servicio.login(request);

        assertEquals("superadmin", response.getUsername());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void loginConPasswordIncorrectoFalla() {
        when(usuarioRepository.findByUsernameOrCorreoIgnoreCase("superadmin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Convecsa2026!", "hash")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> servicio.login(request));
    }

    @Test
    void loginConUsuarioInactivoFalla() {
        usuario.setActivo(false);
        when(usuarioRepository.findByUsernameOrCorreoIgnoreCase("superadmin")).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class, () -> servicio.login(request));
    }

    @Test
    void loginConIdentificadorInexistenteFalla() {
        when(usuarioRepository.findByUsernameOrCorreoIgnoreCase("superadmin")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> servicio.login(request));
    }

    @Test
    void hashSemillaCoincideConPasswordTemporal() {
        assertTrue(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .matches("Convecsa2026!", "$2b$10$Ummi/R/Giwc8a30El2jROerG8zn1qQQNEO3QHJ5VD2IZSXRkRGL5S"));
    }
}
