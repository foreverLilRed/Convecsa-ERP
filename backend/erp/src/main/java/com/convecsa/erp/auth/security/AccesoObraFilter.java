package com.convecsa.erp.auth.security;

import com.convecsa.erp.common.exception.ProyectoSinAccesoException;
import com.convecsa.erp.construccion.support.AccesoObraService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class AccesoObraFilter extends OncePerRequestFilter {

    private static final Pattern OBRA = Pattern.compile("^/api/proyectos/(\\d+)(/.*)?$");

    private final AccesoObraService accesoObraService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = AccesoObraService.normalizarPath(request.getRequestURI(), request.getContextPath());
        String method = request.getMethod();
        try {
            if ("POST".equalsIgnoreCase(method) && "/api/proyectos".equals(path)) {
                accesoObraService.exigirGerencia();
            } else if (path.startsWith("/api/proyectos/materiales")
                    && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))) {
                accesoObraService.exigirNoAlmacen();
            } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/proyectos/usuarios-asignables")) {
                accesoObraService.exigirGerencia();
            } else if (path.startsWith("/api/soma")) {
                accesoObraService.exigirSoma();
            } else {
                Matcher matcher = OBRA.matcher(path);
                if (matcher.matches()) {
                    Long proyectoId = Long.valueOf(matcher.group(1));
                    String resto = matcher.group(2) == null ? "" : matcher.group(2);
                    if ("/accesos".equals(resto)) {
                        accesoObraService.exigirGerencia();
                    }
                    accesoObraService.exigirRutaObra(method, path, proyectoId);
                }
            }
        } catch (ProyectoSinAccesoException exception) {
            escribir403(response, exception.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void escribir403(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String texto = mensaje == null ? "" : mensaje.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"status\":403,\"message\":\"" + texto + "\"}");
    }
}
