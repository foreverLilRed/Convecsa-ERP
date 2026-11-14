package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.entity.AuditoriaObra;
import com.convecsa.erp.construccion.repository.AuditoriaObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditoriaObraRegistrar {

    public static final String USUARIO_SISTEMA = "sistema";
    public static final String ENTIDAD_TAREO = "TAREO";
    public static final String ENTIDAD_PRESUPUESTO = "PRESUPUESTO";
    public static final String ENTIDAD_VALORIZACION = "VALORIZACION";
    public static final String ENTIDAD_FACTORING = "FACTORING";
    public static final String ENTIDAD_EMO_SOMA = "EMO_SOMA";
    public static final String ENTIDAD_SCTR_SOMA = "SCTR_SOMA";
    public static final String ENTIDAD_EPP_SOMA = "EPP_SOMA";
    public static final String ENTIDAD_CAPACITACION_SOMA = "CAPACITACION_SOMA";
    public static final String ENTIDAD_HALLAZGO_SOMA = "HALLAZGO_SOMA";
    public static final String ENTIDAD_PETAR_SOMA = "PETAR_SOMA";

    private final AuditoriaObraRepository auditoriaObraRepository;
    private final ProyectoRepository proyectoRepository;

    public void registrar(
            Long proyectoId, AccionAuditoriaObra accion, String entidad, Long entidadId, String detalle) {
        AuditoriaObra fila = new AuditoriaObra();
        fila.setProyecto(proyectoRepository.getReferenceById(proyectoId));
        fila.setAccion(accion.name());
        fila.setEntidad(entidad);
        fila.setEntidadId(entidadId);
        fila.setDetalle(recortar(detalle, 1000));
        fila.setUsuario(usuarioActual());
        auditoriaObraRepository.save(fila);
    }

    public static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return USUARIO_SISTEMA;
        }
        String nombre = auth.getName();
        if (nombre == null || nombre.isBlank() || "anonymousUser".equals(nombre)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof String texto && !texto.isBlank()) {
                nombre = texto;
            } else {
                return USUARIO_SISTEMA;
            }
        }
        return recortar(nombre, 80);
    }

    private static String recortar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= maximo ? trimmed : trimmed.substring(0, maximo);
    }
}
