package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.nomina.declaracion.domain.EstadoDeclaracion;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCcConcepto;
import com.convecsa.erp.nomina.declaracion.entity.DeclaracionMensual;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.dto.AsientoContableDTO;
import com.convecsa.erp.nomina.support.AsientoContableService;
import com.convecsa.erp.nomina.support.BoletaPdfService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AsientoRccServiceTest {

    private final AsientoRccService service = new AsientoRccService(new AsientoContableService(new BoletaPdfService()));

    @Test
    void asientoUsaCuenta92PorObraYPasivosRcc() {
        DeclaracionMensual declaracion = new DeclaracionMensual();
        declaracion.setId(5L);
        declaracion.setAnio(2026);
        declaracion.setMes(4);
        declaracion.setEstado(EstadoDeclaracion.BORRADOR);

        Proyecto proyecto = new Proyecto();
        proyecto.setCodigo("OB001");
        proyecto.setCentroCosto("MANO DE OBRA");

        ConsolidadoCc fila = new ConsolidadoCc();
        fila.setProyecto(proyecto);
        fila.setTotalHaberes(new BigDecimal("6421.51"));
        fila.setTotalDescuentos(new BigDecimal("697.05"));
        fila.setTotalAportesEmpleador(new BigDecimal("420.11"));
        fila.setNeto(new BigDecimal("5724.46"));
        fila.reemplazarConceptos(List.of(
                concepto(CategoriaConcepto.DESCUENTO, CodigoConceptoCc.ONP, "593.21"),
                concepto(CategoriaConcepto.DESCUENTO, CodigoConceptoCc.CONAFOVICER, "48.84"),
                concepto(CategoriaConcepto.DESCUENTO, CodigoConceptoCc.CUOTA_SINDICAL, "40.00"),
                concepto(CategoriaConcepto.DESCUENTO, CodigoConceptoCc.RENTA_5TA, "15.00"),
                concepto(CategoriaConcepto.APORTE_EMPLEADOR, CodigoConceptoCc.ESSALUD, "410.69"),
                concepto(CategoriaConcepto.APORTE_EMPLEADOR, CodigoConceptoCc.FONDO_CAPACITACION, "9.42")));

        AsientoContableDTO asiento = service.construir(declaracion, List.of(fila));
        assertEquals(new BigDecimal("0.00"), asiento.getDiferencia());
        assertEquals(asiento.getTotalDebe(), asiento.getTotalHaber());
        assertTrue(asiento.getAsientos().get(0).getLineas().stream()
                .anyMatch(l -> "4038".equals(l.getCuenta())));
        assertTrue(asiento.getAsientos().get(0).getLineas().stream()
                .anyMatch(l -> "4151".equals(l.getCuenta())));
        assertTrue(asiento.getAsientos().get(0).getLineas().stream()
                .anyMatch(l -> "4171".equals(l.getCuenta())));
        assertTrue(asiento.getAsientos().get(1).getLineas().stream()
                .anyMatch(l -> "92".equals(l.getCuenta()) && "OB001 / MANO DE OBRA".equals(l.getDestino())));
    }

    private static ConsolidadoCcConcepto concepto(
            CategoriaConcepto categoria, CodigoConceptoCc codigo, String monto) {
        ConsolidadoCcConcepto concepto = new ConsolidadoCcConcepto();
        concepto.setCategoria(categoria);
        concepto.setCodigo(codigo);
        concepto.setLabel(codigo.name());
        concepto.setMonto(new BigDecimal(monto));
        return concepto;
    }
}
