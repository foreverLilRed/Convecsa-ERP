package com.convecsa.erp.soma.support;

import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaDocumentoChecklist;

public final class ChecklistPersonaActualizador {

    private ChecklistPersonaActualizador() {}

    public static void aplicar(Persona persona, TipoDocumentoChecklist tipo, EstadoDocumentoChecklist estado) {
        if (persona == null || tipo == null || estado == null) {
            throw new IllegalArgumentException("La persona, el tipo y el estado son obligatorios");
        }
        PersonaDocumentoChecklist fila = persona.getDocumentosChecklist().stream()
                .filter(item -> item.getTipoDocumento() == tipo)
                .findFirst()
                .orElse(null);
        if (fila == null) {
            fila = new PersonaDocumentoChecklist();
            fila.setTipoDocumento(tipo);
            fila.setCriticidad(tipo.getCriticidad());
            persona.addDocumentoChecklist(fila);
        }
        fila.setEstado(estado);
    }
}
