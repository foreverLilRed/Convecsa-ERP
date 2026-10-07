package com.convecsa.erp.onboarding.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class OnboardingHtmlPacketTest {

    @Test
    void unDocumentoDevuelveHtmlCompleto() {
        String html = "<html><body><p>Uno</p></body></html>";
        assertTrue(OnboardingHtmlPacket.unir(List.of(html)).contains("<p>Uno</p>"));
        assertFalse(OnboardingHtmlPacket.unir(List.of(html)).contains("onboarding-page"));
    }

    @Test
    void variosDocumentosUnenCuerpoConSaltoDePagina() {
        String uno = "<html><head><style>.a{color:red}</style></head><body><p>Uno</p></body></html>";
        String dos = "<html><head><style>.b{color:blue}</style></head><body><p>Dos</p></body></html>";

        String paquete = OnboardingHtmlPacket.unir(List.of(uno, dos));

        assertTrue(paquete.contains("<p>Uno</p>"));
        assertTrue(paquete.contains("<p>Dos</p>"));
        assertTrue(paquete.contains("page-break-after: always"));
        assertTrue(paquete.contains(".a{color:red}"));
        assertTrue(paquete.contains(".b{color:blue}"));
        assertFalse(paquete.contains("<body><p>Uno</p></body>"));
    }

    @Test
    void conImpresionAutomaticaInsertaScriptAntesDelBody() {
        String html = "<html><body><p>Uno</p></body></html>";
        String conPrint = OnboardingHtmlPacket.conImpresionAutomatica(html);
        assertTrue(conPrint.contains("window.print()"));
        assertTrue(conPrint.contains("</body>"));
        assertTrue(conPrint.indexOf("window.print()") < conPrint.toLowerCase().indexOf("</body>"));
    }
}
