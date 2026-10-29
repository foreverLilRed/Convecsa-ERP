package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.nomina.entity.RentaQuintaCategoria;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RentaQuintaProyeccionServiceTest {

    private final RentaQuintaProyeccionService service = new RentaQuintaProyeccionService(null, null, null);

    @Test
    void bajoSieteUitNoRetiene() {
        BigDecimal retencion = service.calcular(
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDate.of(2026, 1, 4),
                new BigDecimal("5500"),
                new BigDecimal("7"),
                tramos2026());
        assertEquals(new BigDecimal("0.00"), retencion);
    }

    @Test
    void afectoAltoRetienePositivo() {
        BigDecimal retencion = service.calcular(
                new BigDecimal("10000.00"),
                new BigDecimal("200000.00"),
                BigDecimal.ZERO,
                LocalDate.of(2026, 6, 28),
                new BigDecimal("5500"),
                new BigDecimal("7"),
                tramos2026());
        assertTrue(retencion.compareTo(BigDecimal.ZERO) > 0, retencion.toPlainString());
        assertEquals(new BigDecimal("39487.50"), retencion);
    }

    @Test
    void retencionNoEsNegativaSiYaSeRetuvoDeMas() {
        BigDecimal retencion = service.calcular(
                new BigDecimal("10000.00"),
                new BigDecimal("200000.00"),
                new BigDecimal("50000.00"),
                LocalDate.of(2026, 6, 28),
                new BigDecimal("5500"),
                new BigDecimal("7"),
                tramos2026());
        assertEquals(new BigDecimal("0.00"), retencion);
    }

    private static List<RentaQuintaCategoria> tramos2026() {
        return List.of(
                tramo(1, "0", "5", "0.0800"),
                tramo(2, "5", "20", "0.1400"),
                tramo(3, "20", "35", "0.1700"),
                tramo(4, "35", "45", "0.2000"),
                tramo(5, "45", null, "0.3000"));
    }

    private static RentaQuintaCategoria tramo(int orden, String desde, String hasta, String tasa) {
        RentaQuintaCategoria item = new RentaQuintaCategoria();
        item.setOrden(orden);
        item.setDesdeUit(new BigDecimal(desde));
        item.setHastaUit(hasta == null ? null : new BigDecimal(hasta));
        item.setTasa(new BigDecimal(tasa));
        return item;
    }
}
