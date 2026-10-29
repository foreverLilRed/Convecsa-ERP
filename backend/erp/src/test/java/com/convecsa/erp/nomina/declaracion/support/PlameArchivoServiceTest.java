package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCcConcepto;
import com.convecsa.erp.nomina.declaracion.entity.ReciboHonorario;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;

class PlameArchivoServiceTest {

    private final PlameArchivoService plame = new PlameArchivoService();
    private final PlameZipService zipService = new PlameZipService();

    @Test
    void jorRemSnlUnificanAdminRccYRxhSegunLayout() {
        ConsolidadoCc obrero = consolidadoOb001();
        BoletaPago admin = boletaAdmin();
        ReciboHonorario rxh = recibo();

        String jor = plame.generarJor(List.of(obrero), List.of(admin));
        String rem = plame.generarRem(List.of(obrero), List.of(admin), List.of(rxh));
        String snl = plame.generarSnl(List.of(obrero), List.of());

        assertTrue(jor.contains("01|71118318|187|30|0|0"));
        assertTrue(jor.contains("01|44556677|240|0|0|0"));
        assertTrue(rem.contains("01|71118318|0121|1|2092.97|2092.97"));
        assertTrue(rem.contains("01|71118318|0311|1|669.75|669.75"));
        assertTrue(rem.contains("01|44556677|0121|1|2500.00|2500.00"));
        assertTrue(rem.contains("01|10203040|0121|1|800.00|800.00"));
        assertTrue(rem.contains("01|10203040|0604|1|64.00|64.00"));
        assertEquals("01|71118318|07|1\r\n", snl);

        byte[] zip = zipService.empaquetar("20601414067202604", jor, rem, snl);
        assertTrue(zip.length > 50);
        try (ZipInputStream in = new ZipInputStream(new java.io.ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            assertEquals("20601414067202604.jor", in.getNextEntry().getName());
            assertEquals("20601414067202604.rem", in.getNextEntry().getName());
            assertEquals("20601414067202604.snl", in.getNextEntry().getName());
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    private static ConsolidadoCc consolidadoOb001() {
        ConsolidadoCc fila = new ConsolidadoCc();
        fila.setPersona(ConsolidadoCcCalculatorTest.persona());
        fila.setHorasOrd(new BigDecimal("187.50"));
        fila.setHe60(BigDecimal.ZERO);
        fila.setHe100(BigDecimal.ZERO);
        fila.setDiasFalta(1);
        fila.setDiasLab(24);
        fila.setNeto(new BigDecimal("5724.46"));
        ConsolidadoCcConcepto jornal = concepto(CodigoConceptoCc.JORNAL_BASICO, "2092.97");
        ConsolidadoCcConcepto buc = concepto(CodigoConceptoCc.BUC, "669.75");
        fila.reemplazarConceptos(List.of(jornal, buc));
        return fila;
    }

    private static ConsolidadoCcConcepto concepto(CodigoConceptoCc codigo, String monto) {
        ConsolidadoCcConcepto concepto = new ConsolidadoCcConcepto();
        concepto.setCategoria(CategoriaConcepto.HABER);
        concepto.setCodigo(codigo);
        concepto.setLabel(codigo.name());
        concepto.setMonto(new BigDecimal(monto));
        return concepto;
    }

    private static BoletaPago boletaAdmin() {
        Persona persona = new Persona();
        persona.setId(2L);
        persona.setApellidos("RAMOS");
        persona.setNombres("ANA");
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("44556677");
        BoletaPago boleta = new BoletaPago();
        boleta.setPersona(persona);
        boleta.setDiasTrabajados(30);
        boleta.setTotalHaberes(new BigDecimal("2500.00"));
        BoletaConcepto sueldo = new BoletaConcepto();
        sueldo.setCategoria(CategoriaConcepto.HABER);
        sueldo.setCodigo(CodigoConcepto.SUELDO_BASICO);
        sueldo.setLabel("Sueldo");
        sueldo.setMonto(new BigDecimal("2500.00"));
        boleta.addConcepto(sueldo);
        return boleta;
    }

    private static ReciboHonorario recibo() {
        ReciboHonorario recibo = new ReciboHonorario();
        recibo.setTipoDocumento("01");
        recibo.setNumeroDocumento("10203040");
        recibo.setSerie("E001");
        recibo.setNumero("123");
        recibo.setMonto(new BigDecimal("800.00"));
        recibo.setRetencionCuarta(true);
        return recibo;
    }
}
