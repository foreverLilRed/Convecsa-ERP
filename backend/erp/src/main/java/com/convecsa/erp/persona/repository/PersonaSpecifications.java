package com.convecsa.erp.persona.repository;

import com.convecsa.erp.persona.domain.GradoInstruccion;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.dto.PersonaListQuery;
import com.convecsa.erp.persona.entity.Persona;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;

public final class PersonaSpecifications {

    private PersonaSpecifications() {}

    public static Specification<Persona> from(PersonaListQuery query) {
        return Stream.of(
                        hasTexto(blankToNull(query.q())),
                        hasTipoDocumento(query.tipoDocumento()),
                        hasGradoInstruccion(query.gradoInstruccion()),
                        equalIgnoreCase("departamento", blankToNull(query.departamento())),
                        equalIgnoreCase("provincia", blankToNull(query.provincia())),
                        equalIgnoreCase("distrito", blankToNull(query.distrito())))
                .filter(Objects::nonNull)
                .reduce(Specification.unrestricted(), Specification::and);
    }

    static Specification<Persona> hasTexto(String q) {
        if (q == null) {
            return null;
        }
        String like = "%" + escapeLike(q.toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("apellidos")), like, '\\'),
                cb.like(cb.lower(root.get("nombres")), like, '\\'),
                cb.like(cb.lower(root.get("numeroDocumento")), like, '\\'),
                cb.like(cb.lower(root.get("correo")), like, '\\'));
    }

    static Specification<Persona> hasTipoDocumento(TipoDocumento tipoDocumento) {
        if (tipoDocumento == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("tipoDocumento"), tipoDocumento);
    }

    static Specification<Persona> hasGradoInstruccion(GradoInstruccion gradoInstruccion) {
        if (gradoInstruccion == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("gradoInstruccion"), gradoInstruccion);
    }

    static Specification<Persona> equalIgnoreCase(String attribute, String value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get(attribute)), value.toLowerCase());
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
