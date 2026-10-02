package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.CriticidadDocumento;
import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentoChecklistDTO {

    private final Long id;
    private final TipoDocumentoChecklist tipo;
    private final CriticidadDocumento criticidad;
    private final EstadoDocumentoChecklist estado;
}
