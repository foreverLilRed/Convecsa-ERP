package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.nomina.entity.RentaQuintaCategoria;
import com.convecsa.erp.nomina.entity.RentaQuintaConfiguracion;
import com.convecsa.erp.nomina.repository.RentaQuintaCategoriaRepository;
import com.convecsa.erp.nomina.repository.RentaQuintaConfiguracionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentaQuintaProyeccionService {

    private static final int SEMANAS_ANIO = 52;
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    private final RentaQuintaConfiguracionRepository configuracionRepository;
    private final RentaQuintaCategoriaRepository categoriaRepository;
    private final BoletaCcRepository boletaCcRepository;

    public BigDecimal proyectar(String dni, LocalDate fechaHasta, BigDecimal afectoSemana) {
        if (fechaHasta == null) {
            return BigDecimal.ZERO.setScale(2, ROUND);
        }
        int anio = fechaHasta.getYear();
        RentaQuintaConfiguracion config = configuracionRepository.findById(anio)
                .or(() -> configuracionRepository.findFirstByOrderByAnioDesc())
                .orElse(null);
        if (config == null) {
            return BigDecimal.ZERO.setScale(2, ROUND);
        }
        LocalDate desde = LocalDate.of(anio, 1, 1);
        LocalDate hasta = LocalDate.of(anio, 12, 31);
        BigDecimal ytdCerrado = BigDecimal.ZERO;
        BigDecimal retencionYtd = BigDecimal.ZERO;
        if (dni != null && !dni.isBlank()) {
            ytdCerrado = nz(boletaCcRepository.sumTotalAfectoCerradoPorDni(
                    dni.trim(), EstadoPeriodoCc.CERRADO, desde, hasta));
            retencionYtd = nz(boletaCcRepository.sumConceptoCerradoPorDni(
                    dni.trim(),
                    CodigoConceptoCc.RENTA_5TA,
                    EstadoPeriodoCc.CERRADO,
                    desde,
                    hasta));
        }
        return calcular(
                afectoSemana,
                ytdCerrado,
                retencionYtd,
                fechaHasta,
                config.getValorUit(),
                config.getDeduccionUit(),
                categoriaRepository.findAllByOrderByOrdenAsc());
    }

    public BigDecimal calcular(
            BigDecimal afectoSemana,
            BigDecimal afectoCerradoYtd,
            BigDecimal retencionCerradaYtd,
            LocalDate fechaHasta,
            BigDecimal valorUit,
            BigDecimal deduccionUit,
            List<RentaQuintaCategoria> tramos) {
        int n = semanaIso(fechaHasta);
        BigDecimal ytd = nz(afectoCerradoYtd).add(nz(afectoSemana));
        if (valorUit == null || valorUit.signum() <= 0 || tramos == null || tramos.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, ROUND);
        }
        BigDecimal anualProyectado = ytd.multiply(BigDecimal.valueOf(SEMANAS_ANIO))
                .divide(BigDecimal.valueOf(n), 2, ROUND);
        BigDecimal rentaNeta = anualProyectado.subtract(nz(deduccionUit).multiply(valorUit));
        if (rentaNeta.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, ROUND);
        }
        BigDecimal impuestoAnual = impuestoPorTramos(rentaNeta, valorUit, tramos);
        BigDecimal acumuladoTeorico = impuestoAnual.multiply(BigDecimal.valueOf(n))
                .divide(BigDecimal.valueOf(SEMANAS_ANIO), 2, ROUND);
        BigDecimal retencion = acumuladoTeorico.subtract(nz(retencionCerradaYtd));
        if (retencion.signum() < 0) {
            return BigDecimal.ZERO.setScale(2, ROUND);
        }
        return retencion.setScale(2, ROUND);
    }

    static int semanaIso(LocalDate fechaHasta) {
        if (fechaHasta == null) {
            return 1;
        }
        return Math.max(1, fechaHasta.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }

    private static BigDecimal impuestoPorTramos(
            BigDecimal rentaNeta,
            BigDecimal valorUit,
            List<RentaQuintaCategoria> tramos) {
        BigDecimal impuesto = BigDecimal.ZERO;
        for (RentaQuintaCategoria tramo : tramos) {
            BigDecimal desde = nz(tramo.getDesdeUit()).multiply(valorUit);
            if (rentaNeta.compareTo(desde) <= 0) {
                continue;
            }
            BigDecimal hastaMonto = tramo.getHastaUit() == null
                    ? rentaNeta
                    : tramo.getHastaUit().multiply(valorUit);
            BigDecimal tope = rentaNeta.min(hastaMonto);
            BigDecimal base = tope.subtract(desde);
            if (base.signum() > 0) {
                impuesto = impuesto.add(base.multiply(nz(tramo.getTasa())));
            }
        }
        return impuesto.setScale(2, ROUND);
    }

    private static BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
