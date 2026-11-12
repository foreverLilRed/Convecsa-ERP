package com.convecsa.erp.auth.service.impl;

import com.convecsa.erp.auth.dto.AuthResponse;
import com.convecsa.erp.auth.dto.LoginRequest;
import com.convecsa.erp.auth.service.InterfazAuthService;
import com.convecsa.erp.auth.support.JwtTokenService;
import com.convecsa.erp.common.exception.CredencialesInvalidasException;
import com.convecsa.erp.usuario.entity.Usuario;
import com.convecsa.erp.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements InterfazAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identificador = request.getUsername().trim();
        Usuario usuario = usuarioRepository
                .findByUsernameOrCorreoIgnoreCase(identificador)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!Boolean.TRUE.equals(usuario.getActivo())
                || !passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        return AuthResponse.builder()
                .token(jwtTokenService.generar(usuario))
                .username(usuario.getUsername())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .usuarioId(usuario.getId())
                .build();
    }
}
