package com.convecsa.erp.persona;

import com.convecsa.erp.common.exception.CampoOrdenamientoInvalidoException;
import java.util.Set;
import org.springframework.data.domain.Sort;

public final class PersonaSort {

    private static final Set<String> CAMPOS = Set.of("apellidos", "nombres", "numeroDocumento", "createdAt");

    private PersonaSort() {}

    public static Sort parse(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "apellidos");
        }

        String[] parts = sort.split(",", 2);
        String campo = parts[0].trim();
        if (!CAMPOS.contains(campo)) {
            throw new CampoOrdenamientoInvalidoException();
        }

        Sort.Direction direccion = Sort.Direction.ASC;
        if (parts.length == 2) {
            String dir = parts[1].trim().toLowerCase();
            if ("desc".equals(dir)) {
                direccion = Sort.Direction.DESC;
            } else if (!"asc".equals(dir)) {
                throw new CampoOrdenamientoInvalidoException();
            }
        }

        return Sort.by(direccion, campo);
    }
}
