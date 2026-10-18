package com.convecsa.erp.nomina.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.EstadoPeriodoNomina;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.nomina.dto.AsientoContableDTO;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.entity.PeriodoNomina;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

class AsientoContableServiceTest {

    private final AsientoContableService service = new AsientoContableService(new BoletaPdfService());

    @Test
    void generaAsientosBalanceadosPorDestinoYPdfA4() throws Exception {
        PeriodoNomina periodo = new PeriodoNomina();
        periodo.setId(10L);
        periodo.setAnio(2026);
        periodo.setMes(10);
        periodo.setEstado(EstadoPeriodoNomina.CERRADO);

        BoletaPago obra = boleta("Proyecto Hospital Regional", null, "2500.00", "321.00", "225.00", "2179.00");
        concepto(obra, CategoriaConcepto.DESCUENTO, CodigoConcepto.APORTE_OBLIG, "Aporte AFP", "250.00");
        concepto(obra, CategoriaConcepto.DESCUENTO, CodigoConcepto.COMISION_AFP, "Comisión AFP", "37.50");
        concepto(obra, CategoriaConcepto.DESCUENTO, CodigoConcepto.PRIMA_SEGURO, "Prima AFP", "33.50");
        concepto(obra, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConcepto.ESSALUD, "EsSalud", "225.00");

        BoletaPago administracion = boleta(null, "Administración", "1500.00", "195.00", "135.00", "1305.00");
        concepto(administracion, CategoriaConcepto.DESCUENTO, CodigoConcepto.APORTE_ONP, "ONP", "195.00");
        concepto(administracion, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConcepto.ESSALUD, "EsSalud", "135.00");

        AsientoContableDTO asiento = service.construir(periodo, List.of(obra, administracion));

        assertEquals(new BigDecimal("0.00"), asiento.getDiferencia());
        assertEquals(asiento.getTotalDebe(), asiento.getTotalHaber());
        assertEquals(2, asiento.getAsientos().size());
        assertTrue(asiento.getAsientos().get(1).getLineas().stream()
                .anyMatch(linea -> "92".equals(linea.getCuenta())
                        && "Proyecto Hospital Regional".equals(linea.getDestino())));
        assertTrue(asiento.getAsientos().get(1).getLineas().stream()
                .anyMatch(linea -> "94".equals(linea.getCuenta())
                        && "Administración".equals(linea.getDestino())));

        NominaDocumentoConfiguracion empresa = new NominaDocumentoConfiguracion();
        empresa.setRazonSocial("CONVECSA S.A.C.");
        empresa.setRuc("20601414067");
        empresa.setDomicilio("San Borja, Lima");
        byte[] pdf = service.generarPdf(asiento, empresa);
        Path output = Path.of("target", "test-output", "asiento-contable-sample.pdf");
        Files.createDirectories(output.getParent());
        Files.write(output, pdf);
        assertTrue(pdf.length > 1_000);
        try (PDDocument document = PDDocument.load(pdf)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }

    private static BoletaPago boleta(
            String proyecto,
            String area,
            String haberes,
            String descuentos,
            String aportes,
            String neto) {
        Contrato contrato = new Contrato();
        contrato.getMetadata().put("proyecto", proyecto);
        contrato.getMetadata().put("area", area);
        BoletaPago boleta = new BoletaPago();
        boleta.setContrato(contrato);
        boleta.setTotalHaberes(new BigDecimal(haberes));
        boleta.setTotalDescuentos(new BigDecimal(descuentos));
        boleta.setTotalAportesEmpleador(new BigDecimal(aportes));
        boleta.setNeto(new BigDecimal(neto));
        return boleta;
    }

    private static void concepto(
            BoletaPago boleta,
            CategoriaConcepto categoria,
            CodigoConcepto codigo,
            String label,
            String monto) {
        BoletaConcepto concepto = new BoletaConcepto();
        concepto.setCategoria(categoria);
        concepto.setCodigo(codigo);
        concepto.setLabel(label);
        concepto.setMonto(new BigDecimal(monto));
        concepto.setOrigen(OrigenConcepto.CALCULADO);
        boleta.addConcepto(concepto);
    }
}
