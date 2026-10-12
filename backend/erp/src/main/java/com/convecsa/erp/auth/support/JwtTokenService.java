package com.convecsa.erp.auth.support;

import com.convecsa.erp.usuario.domain.RolUsuario;
import com.convecsa.erp.usuario.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generar(Usuario usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim(CLAIM_ROL, usuario.getRol().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public String extraerUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public RolUsuario extraerRol(String token) {
        String rol = parseClaims(token).get(CLAIM_ROL, String.class);
        return RolUsuario.valueOf(rol);
    }

    public boolean esValido(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
