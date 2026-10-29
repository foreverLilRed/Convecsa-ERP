package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AfpnetArchivoServiceTest {

    private final AfpnetArchivoService service = new AfpnetArchivoService();

    @Test
    void omiteOnpEIncluyeIndicadorRccYTipoComision() {
        Persona obrero = new Persona();
        obrero.setId(1L);
        obrero.setApellidos("QUISPE HUAMAN");
        obrero.setNombres("JUAN");
        obrero.setTipoDocumento(TipoDocumento.DNI);
        obrero.setNumeroDocumento("71118318");
        obrero.setCuspp("123456789012");
        obrero.setPension("INTEGRA");
        obrero.setTipoComisionAfp(TipoComisionAfp.FLUJO);
        ConsolidadoCc rcc = new ConsolidadoCc();
        rcc.setPersona(obrero);
        rcc.setTotalAfecto(new BigDecimal("4563.19"));

        Persona onp = new Persona();
        onp.setId(2L);
        onp.setApellidos("ONP");
        onp.setNombres("TRABAJADOR");
        onp.setTipoDocumento(TipoDocumento.DNI);
        onp.setNumeroDocumento("11111111");
        onp.setPension("ONP");
        ConsolidadoCc rccOnp = new ConsolidadoCc();
        rccOnp.setPersona(onp);
        rccOnp.setTotalAfecto(new BigDecimal("1000.00"));

        Persona admin = new Persona();
        admin.setId(3L);
        admin.setApellidos("RAMOS DIAZ");
        admin.setNombres("ANA");
        admin.setTipoDocumento(TipoDocumento.DNI);
        admin.setNumeroDocumento("44556677");
        admin.setCuspp("999888777666");
        admin.setTipoComisionAfp(TipoComisionAfp.MIXTA);
        Afp afp = new Afp();
        afp.setEsOnp(false);
        BoletaPago boleta = new BoletaPago();
        boleta.setPersona(admin);
        boleta.setAfp(afp);
        boleta.setTotalHaberes(new BigDecimal("2500.00"));

        String csv = service.generar(List.of(rcc, rccOnp), List.of(boleta));
        assertTrue(csv.contains("123456789012,0,71118318,QUISPE,HUAMAN,JUAN,S,N,N,N,4563.19,0.00,0.00,0.00,C,F"));
        assertTrue(csv.contains("999888777666,0,44556677,RAMOS,DIAZ,ANA,S,N,N,N,2500.00,0.00,0.00,0.00,N,M"));
        assertFalse(csv.contains("11111111"));
        assertEquals(2, csv.trim().split("\r\n").length);
    }
}
