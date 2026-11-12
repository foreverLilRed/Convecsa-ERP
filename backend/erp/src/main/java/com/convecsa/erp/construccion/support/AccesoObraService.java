package com.convecsa.erp.construccion.support;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.ProyectoSinAccesoException;
import com.convecsa.erp.construccion.dto.AccesoObraDTO;
import com.convecsa.erp.construccion.dto.AccesoObraRequest;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.UsuarioAsignableDTO;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.UsuarioProyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.UsuarioProyectoRepository;
import com.convecsa.erp.usuario.domain.RolUsuario;
import com.convecsa.erp.usuario.entity.Usuario;
import com.convecsa.erp.usuario.repository.UsuarioRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccesoObraService {

    static final String MSG_SIN_ACCESO = "No tiene acceso a esta obra";
    static final String MSG_GERENCIA = "Solo gerencia puede realizar esta acción";
    static final String MSG_ALMACEN = "El rol almacén solo puede consultar el kardex";

    private static final List<RolUsuario> GERENCIA = List.of(RolUsuario.SUPERADMIN, RolUsuario.ADMIN);
    private static final int LIMITE_ASIGNABLES = 30;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioProyectoRepository usuarioProyectoRepository;
    private final ProyectoRepository proyectoRepository;

    public static boolean esGerencia(RolUsuario rol) {
        return rol == RolUsuario.SUPERADMIN || rol == RolUsuario.ADMIN;
    }

    public static boolean esAlmacen(RolUsuario rol) {
        return rol == RolUsuario.ALMACEN;
    }

    public boolean kardexPermitido(String method, String path, Long proyectoId) {
        if (!"GET".equalsIgnoreCase(method) || proyectoId == null || path == null) {
            return false;
        }
        String base = "/api/proyectos/" + proyectoId;
        return path.equals(base)
                || path.equals(base + "/alertas")
                || path.equals(base + "/almacen")
                || path.equals(base + "/almacen/kardex");
    }

    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ProyectoSinAccesoException(MSG_SIN_ACCESO);
        }
        String username = auth.getName();
        return usuarioRepository.findByUsernameOrCorreoIgnoreCase(username)
                .orElseThrow(() -> new ProyectoSinAccesoException(MSG_SIN_ACCESO));
    }

    public void exigirGerencia() {
        if (!esGerencia(usuarioActual().getRol())) {
            throw new ProyectoSinAccesoException(MSG_GERENCIA);
        }
    }

    public void exigirNoAlmacen() {
        if (esAlmacen(usuarioActual().getRol())) {
            throw new ProyectoSinAccesoException(MSG_ALMACEN);
        }
    }

    public void exigirObra(Long proyectoId) {
        Usuario usuario = usuarioActual();
        if (esGerencia(usuario.getRol())) {
            return;
        }
        if (!usuarioProyectoRepository.existsByUsuarioIdAndProyectoId(usuario.getId(), proyectoId)) {
            throw new ProyectoSinAccesoException(MSG_SIN_ACCESO);
        }
    }

    public void exigirRutaObra(String method, String path, Long proyectoId) {
        exigirObra(proyectoId);
        if (esAlmacen(usuarioActual().getRol()) && !kardexPermitido(method, path, proyectoId)) {
            throw new ProyectoSinAccesoException(MSG_ALMACEN);
        }
    }

    public List<ProyectoDTO> filtrarListado(List<ProyectoDTO> todos) {
        Usuario usuario = usuarioActual();
        if (esGerencia(usuario.getRol())) {
            return todos;
        }
        Set<Long> ids = new HashSet<>(usuarioProyectoRepository.findProyectoIdsByUsuarioId(usuario.getId()));
        return todos.stream().filter(item -> ids.contains(item.getId())).toList();
    }

    @Transactional(readOnly = true)
    public List<AccesoObraDTO> listarAccesos(Long proyectoId) {
        exigirGerencia();
        buscarProyecto(proyectoId);
        return usuarioProyectoRepository.findByProyectoIdOrderByIdAsc(proyectoId).stream()
                .map(AccesoObraService::toAccesoDto)
                .toList();
    }

    @Transactional
    public List<AccesoObraDTO> guardarAccesos(Long proyectoId, List<AccesoObraRequest> request) {
        exigirGerencia();
        Proyecto proyecto = buscarProyecto(proyectoId);
        List<AccesoObraRequest> filas = request == null ? List.of() : request;
        Set<Long> vistos = new HashSet<>();
        for (AccesoObraRequest fila : filas) {
            if (fila.getUsuarioId() == null) {
                throw new IllegalArgumentException("Cada acceso requiere usuario");
            }
            if (!vistos.add(fila.getUsuarioId())) {
                throw new IllegalArgumentException("Hay usuarios duplicados en los accesos");
            }
        }
        usuarioProyectoRepository.deleteByProyectoId(proyectoId);
        usuarioProyectoRepository.flush();
        for (AccesoObraRequest fila : filas) {
            Usuario usuario = usuarioRepository.findById(fila.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario"));
            if (!Boolean.TRUE.equals(usuario.getActivo()) || esGerencia(usuario.getRol())) {
                throw new IllegalArgumentException("Solo se pueden asignar usuarios activos que no son gerencia");
            }
            UsuarioProyecto acceso = new UsuarioProyecto();
            acceso.setProyecto(proyecto);
            acceso.setUsuario(usuario);
            usuarioProyectoRepository.save(acceso);
        }
        return listarAccesos(proyectoId);
    }

    @Transactional(readOnly = true)
    public List<UsuarioAsignableDTO> listarAsignables(String q) {
        exigirGerencia();
        String texto = q == null ? "" : q.trim();
        return usuarioRepository.findAsignables(texto, GERENCIA).stream()
                .limit(LIMITE_ASIGNABLES)
                .map(usuario -> UsuarioAsignableDTO.builder()
                        .id(usuario.getId())
                        .username(usuario.getUsername())
                        .rol(usuario.getRol())
                        .build())
                .toList();
    }

    private Proyecto buscarProyecto(Long proyectoId) {
        return proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static AccesoObraDTO toAccesoDto(UsuarioProyecto acceso) {
        Usuario usuario = acceso.getUsuario();
        return AccesoObraDTO.builder()
                .usuarioId(usuario.getId())
                .username(usuario.getUsername())
                .rol(usuario.getRol())
                .build();
    }

    public static String normalizarPath(String uri, String contextPath) {
        if (uri == null) {
            return "";
        }
        int query = uri.indexOf('?');
        String path = query >= 0 ? uri.substring(0, query) : uri;
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path.toLowerCase(Locale.ROOT);
    }
}
