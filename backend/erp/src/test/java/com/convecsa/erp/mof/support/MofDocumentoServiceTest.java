package com.convecsa.erp.mof.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.mof.dto.MofPuestoDTO;
import java.util.List;
import org.junit.jupiter.api.Test;

class MofDocumentoServiceTest {

    @Test
    void renderIncluyeCodigoYFuncionSinBarraDeImpresion() {
        MofPuestoDTO puesto = MofPuestoDTO.builder()
                .id(1L)
                .codigo("CVSA-MOF-P005")
                .cargo("RESIDENTE DE OBRA")
                .grupo("STAFF DE OBRA")
                .proposito("Dirigir la obra")
                .superior("Gerencia")
                .subordinados("Equipo")
                .funciones(List.of("Representar a CONVECSA ante el cliente"))
                .educacion(List.of("Ingeniería Civil"))
                .experiencia(List.of("5 años"))
                .competencias(List.of("Liderazgo"))
                .build();

        String html = new MofDocumentoService().render(List.of(puesto));

        assertTrue(html.contains("CVSA-MOF-P005"));
        assertTrue(html.contains("Representar a CONVECSA ante el cliente"));
        assertTrue(html.contains("RESIDENTE DE OBRA"));
        assertFalse(html.contains("Ctrl+P"));
        assertFalse(html.contains("print-bar"));
    }
}
