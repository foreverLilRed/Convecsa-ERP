package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentoChecklistRequest {

    @NotNull
    private TipoDocumentoChecklist tipo;

    @NotNull
    private EstadoDocumentoChecklist estado;

    @AssertTrue(message = "NO_APLICA solo aplica a CARNET_RETCC")
    public boolean isNoAplicaSoloRetcc() {
        if (estado != EstadoDocumentoChecklist.NO_APLICA) {
            return true;
        }
        return tipo == TipoDocumentoChecklist.CARNET_RETCC;
    }
}
