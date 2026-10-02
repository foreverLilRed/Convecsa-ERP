package com.convecsa.erp.persona.domain;

public enum TipoDocumentoChecklist {
    DNI_2_CARAS(CriticidadDocumento.CRITICO),
    CV_CERTIFICADO_TRABAJO(CriticidadDocumento.NORMAL),
    CARNET_RETCC(CriticidadDocumento.NORMAL),
    CUL(CriticidadDocumento.NORMAL),
    ANTECEDENTES_POLICIALES(CriticidadDocumento.NORMAL),
    ANTECEDENTES_JUDICIALES(CriticidadDocumento.NORMAL),
    ANTECEDENTES_PENALES(CriticidadDocumento.NORMAL),
    CARNET_VACUNACION(CriticidadDocumento.NORMAL),
    EMO_APTO_VIGENTE(CriticidadDocumento.CRITICO),
    SCTR(CriticidadDocumento.CRITICO),
    POLIZA_VIDA_LEY(CriticidadDocumento.NORMAL),
    CHECKLIST_INGRESO(CriticidadDocumento.NORMAL),
    FICHA_TRABAJADOR(CriticidadDocumento.NORMAL),
    ALTA_T_REGISTRO(CriticidadDocumento.CRITICO);

    private final CriticidadDocumento criticidad;

    TipoDocumentoChecklist(CriticidadDocumento criticidad) {
        this.criticidad = criticidad;
    }

    public CriticidadDocumento getCriticidad() {
        return criticidad;
    }
}
