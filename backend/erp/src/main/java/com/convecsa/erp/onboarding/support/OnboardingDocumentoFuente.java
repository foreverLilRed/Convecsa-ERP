package com.convecsa.erp.onboarding.support;

import com.convecsa.erp.contrato.entity.Adenda;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.persona.entity.Persona;
import java.util.Map;

public final class OnboardingDocumentoFuente {

    public static final String CODIGO_CONTRATO = "DOC-01";
    public static final String CODIGO_ADENDA = "DOC-02";

    private OnboardingDocumentoFuente() {
    }

    public static String htmlDeCodigo(
            String codigo,
            String plantillaOnboarding,
            Persona persona,
            Contrato contrato,
            Adenda adenda,
            Map<String, String> extras,
            ContratoPlantillaService renderer) {
        if (CODIGO_CONTRATO.equals(codigo)) {
            return renderer.render(contrato);
        }
        if (CODIGO_ADENDA.equals(codigo) && adenda != null) {
            return renderer.render(adenda);
        }
        return renderer.render(plantillaOnboarding, persona, contrato, extras);
    }
}
