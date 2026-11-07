package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.EstadoFactoring;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.dto.AsientoObraBloqueDTO;
import com.convecsa.erp.construccion.dto.AsientoObraDTO;
import com.convecsa.erp.construccion.dto.AsientoObraLineaDTO;
import com.convecsa.erp.construccion.dto.AsientosObraDTO;
import com.convecsa.erp.construccion.dto.RentabilidadObraDTO;
import com.convecsa.erp.construccion.dto.RentabilidadPuntoDTO;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.OperacionFactoring;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.entity.Valorizacion;
import com.convecsa.erp.construccion.repository.AlmacenObraRepository;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.OperacionFactoringRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaRepository;
import com.convecsa.erp.construccion.repository.ValorizacionRepository;
import com.convecsa.erp.construccion.service.InterfazRentabilidadObraService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentabilidadObraServiceImpl implements InterfazRentabilidadObraService {

    private final ProyectoRepository proyectoRepository;
    private final ValorizacionRepository valorizacionRepository;
    private final PeriodoCcRepository periodoCcRepository;
    private final BoletaCcRepository boletaCcRepository;
    private final AlmacenObraRepository almacenObraRepository;
    private final ValeSalidaRepository valeSalidaRepository;
    private final OperacionFactoringRepository operacionFactoringRepository;

    @Override
    @Transactional(readOnly = true)
    public RentabilidadObraDTO rentabilidad(Long proyectoId, LocalDate desdeReq, LocalDate hastaReq) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        Rango rango = resolverRango(proyecto, desdeReq, hastaReq);
        Map<YearMonth, BigDecimal> ingresos = vacio(rango.inicio(), rango.fin());
        Map<YearMonth, BigDecimal> costoMo = vacio(rango.inicio(), rango.fin());
        Map<YearMonth, BigDecimal> costoMat = vacio(rango.inicio(), rango.fin());
        Map<YearMonth, BigDecimal> costoFact = vacio(rango.inicio(), rango.fin());

        for (Valorizacion valorizacion : valorizacionRepository.findByProyectoIdOrderByNumeroDesc(proyectoId)) {
            if (valorizacion.getEstado() != EstadoValorizacion.APROBADA) {
                continue;
            }
            YearMonth mes = YearMonth.of(valorizacion.getAnio(), valorizacion.getMes());
            if (ingresos.containsKey(mes)) {
                ingresos.merge(mes, nz(valorizacion.getMontoBruto()), BigDecimal::add);
            }
        }

        List<PeriodoCc> periodos = periodoCcRepository.findByProyectoIdOrderByFechaDesdeDesc(proyectoId).stream()
                .filter(periodo -> periodo.getEstado() == EstadoPeriodoCc.CERRADO)
                .toList();
        if (!periodos.isEmpty()) {
            List<Long> ids = periodos.stream().map(PeriodoCc::getId).toList();
            Map<Long, PeriodoCc> porId = new LinkedHashMap<>();
            for (PeriodoCc periodo : periodos) {
                porId.put(periodo.getId(), periodo);
            }
            for (BoletaCc boleta : boletaCcRepository.findByPeriodoIdIn(ids)) {
                PeriodoCc periodo = porId.get(boleta.getPeriodo().getId());
                BigDecimal costo = nz(boleta.getTotalHaberes()).add(nz(boleta.getTotalAportesEmpleador()));
                acumular(costoMo, periodo.getFechaHasta(), rango.inicio(), rango.fin(), costo);
            }
        }

        for (ValeSalida vale : vales(proyectoId)) {
            acumular(costoMat, vale.getFecha(), rango.inicio(), rango.fin(), costoVale(vale));
        }

        for (OperacionFactoring operacion : adelantos(proyectoId)) {
            BigDecimal costo = nz(operacion.getDescuento())
                    .add(nz(operacion.getComision()))
                    .add(nz(operacion.getIgvComision()));
            acumular(costoFact, operacion.getFechaAdelanto(), rango.inicio(), rango.fin(), costo);
        }

        List<RentabilidadPuntoDTO> puntos = new ArrayList<>();
        BigDecimal totIng = BigDecimal.ZERO;
        BigDecimal totMo = BigDecimal.ZERO;
        BigDecimal totMat = BigDecimal.ZERO;
        BigDecimal totFact = BigDecimal.ZERO;
        BigDecimal acumBruto = BigDecimal.ZERO;
        BigDecimal acumNeto = BigDecimal.ZERO;
        for (YearMonth mes : ingresos.keySet()) {
            BigDecimal ingreso = money(ingresos.get(mes));
            BigDecimal mo = money(costoMo.get(mes));
            BigDecimal mat = money(costoMat.get(mes));
            BigDecimal fact = money(costoFact.get(mes));
            BigDecimal bruto = money(ingreso.subtract(mo).subtract(mat));
            BigDecimal neto = money(bruto.subtract(fact));
            totIng = totIng.add(ingreso);
            totMo = totMo.add(mo);
            totMat = totMat.add(mat);
            totFact = totFact.add(fact);
            acumBruto = money(acumBruto.add(bruto));
            acumNeto = money(acumNeto.add(neto));
            puntos.add(RentabilidadPuntoDTO.builder()
                    .anio(mes.getYear())
                    .mes(mes.getMonthValue())
                    .ingreso(ingreso)
                    .costoMo(mo)
                    .costoMateriales(mat)
                    .costoFactoring(fact)
                    .margenBruto(bruto)
                    .margenNeto(neto)
                    .margenBrutoAcumulado(acumBruto)
                    .margenNetoAcumulado(acumNeto)
                    .build());
        }
        return RentabilidadObraDTO.builder()
                .ingreso(money(totIng))
                .costoMo(money(totMo))
                .costoMateriales(money(totMat))
                .costoFactoring(money(totFact))
                .margenBruto(money(totIng.subtract(totMo).subtract(totMat)))
                .margenNeto(money(totIng.subtract(totMo).subtract(totMat).subtract(totFact)))
                .puntos(puntos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AsientosObraDTO asientos(Long proyectoId, LocalDate desdeReq, LocalDate hastaReq) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        Rango rango = resolverRango(proyecto, desdeReq, hastaReq);
        Instant ahora = Instant.now();
        return AsientosObraDTO.builder()
                .materiales(asientoMateriales(proyecto, rango, ahora))
                .factoring(asientoFactoring(proyectoId, rango, ahora))
                .build();
    }

    private AsientoObraDTO asientoMateriales(Proyecto proyecto, Rango rango, Instant ahora) {
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> destinos = new LinkedHashMap<>();
        for (ValeSalida vale : vales(proyecto.getId())) {
            if (!enRango(vale.getFecha(), rango)) {
                continue;
            }
            BigDecimal costo = costoVale(vale);
            if (costo.signum() == 0) {
                continue;
            }
            total = total.add(costo);
            destinos.merge(auxiliar(proyecto, vale.getCentroCosto()), costo, BigDecimal::add);
        }
        List<AsientoObraBloqueDTO> bloques = new ArrayList<>();
        if (total.signum() > 0) {
            List<AsientoObraLineaDTO> consumo = new ArrayList<>();
            agregarDebe(consumo, "61", "Variación de existencias", null, total);
            agregarHaber(consumo, "24", "Materias primas", null, total);
            bloques.add(bloque("CONSUMO", "Consumo de materiales de obra", consumo));

            List<AsientoObraLineaDTO> destino = new ArrayList<>();
            destinos.forEach((auxiliar, monto) ->
                    agregarDebe(destino, "92", "Costos de producción / obra", auxiliar, monto));
            agregarHaber(destino, "791", "Cargas imputables a cuentas de costos y gastos", null, total);
            bloques.add(bloque("DESTINO", "Destino del consumo por obra / centro de costo", destino));
        }
        return asiento(rango, ahora, bloques);
    }

    private AsientoObraDTO asientoFactoring(Long proyectoId, Rango rango, Instant ahora) {
        BigDecimal neto = BigDecimal.ZERO;
        BigDecimal gasto = BigDecimal.ZERO;
        BigDecimal igv = BigDecimal.ZERO;
        BigDecimal nominal = BigDecimal.ZERO;
        for (OperacionFactoring operacion : adelantos(proyectoId)) {
            if (!enRango(operacion.getFechaAdelanto(), rango)) {
                continue;
            }
            neto = neto.add(nz(operacion.getImporteNeto()));
            gasto = gasto.add(nz(operacion.getDescuento()).add(nz(operacion.getComision())));
            igv = igv.add(nz(operacion.getIgvComision()));
            nominal = nominal.add(nz(operacion.getImporteNominal()));
        }
        List<AsientoObraBloqueDTO> bloques = new ArrayList<>();
        if (nominal.signum() > 0) {
            List<AsientoObraLineaDTO> lineas = new ArrayList<>();
            agregarDebe(lineas, "1041", "Cuentas corrientes", null, neto);
            agregarDebe(lineas, "67", "Gastos financieros", null, gasto);
            agregarDebe(lineas, "4011", "IGV", null, igv);
            agregarHaber(lineas, "12", "Cuentas por cobrar", null, nominal);
            bloques.add(bloque("ADELANTO", "Adelanto de factoring", lineas));
        }
        return asiento(rango, ahora, bloques);
    }

    private List<ValeSalida> vales(Long proyectoId) {
        return almacenObraRepository.findByProyectoId(proyectoId)
                .map(almacen -> valeSalidaRepository.findByAlmacenIdOrderByNumeroDesc(almacen.getId()))
                .orElse(List.of());
    }

    private List<OperacionFactoring> adelantos(Long proyectoId) {
        return operacionFactoringRepository.findByProyectoIdOrderByFechaCesionDescIdDesc(proyectoId).stream()
                .filter(operacion -> operacion.getFechaAdelanto() != null
                        && operacion.getEstado() != EstadoFactoring.REVERTIDA)
                .toList();
    }

    private Proyecto buscarProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static Rango resolverRango(Proyecto proyecto, LocalDate desdeReq, LocalDate hastaReq) {
        LocalDate desde = desdeReq != null ? desdeReq : proyecto.getFechaInicio();
        LocalDate hasta = hastaReq != null ? hastaReq : proyecto.getFechaFin();
        if (desde == null) {
            desde = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        }
        if (hasta == null) {
            hasta = LocalDate.of(desde.getYear(), 12, 31);
            if (hasta.isBefore(desde)) {
                hasta = desde.plusMonths(11);
            }
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser menor que la de inicio");
        }
        return new Rango(desde, hasta);
    }

    private static Map<YearMonth, BigDecimal> vacio(LocalDate desde, LocalDate hasta) {
        Map<YearMonth, BigDecimal> mapa = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(desde);
        YearMonth fin = YearMonth.from(hasta);
        while (!cursor.isAfter(fin)) {
            mapa.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }
        return mapa;
    }

    private static void acumular(
            Map<YearMonth, BigDecimal> mapa, LocalDate fecha, LocalDate desde, LocalDate hasta, BigDecimal monto) {
        if (!enRango(fecha, desde, hasta)) {
            return;
        }
        mapa.merge(YearMonth.from(fecha), nz(monto), BigDecimal::add);
    }

    private static boolean enRango(LocalDate fecha, Rango rango) {
        return enRango(fecha, rango.inicio(), rango.fin());
    }

    private static boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private static BigDecimal costoVale(ValeSalida vale) {
        return vale.getItems().stream()
                .map(item -> nz(item.getCostoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String auxiliar(Proyecto proyecto, String centroCostoVale) {
        String cc = blankToNull(centroCostoVale);
        if (cc == null) {
            cc = blankToNull(proyecto.getCentroCosto());
        }
        String codigo = blankToNull(proyecto.getCodigo());
        if (codigo == null) {
            return cc;
        }
        return cc == null ? codigo : codigo + " / " + cc;
    }

    private static void agregarDebe(
            List<AsientoObraLineaDTO> lineas, String cuenta, String descripcion, String auxiliar, BigDecimal monto) {
        BigDecimal valor = money(monto);
        if (valor.signum() == 0) {
            return;
        }
        lineas.add(AsientoObraLineaDTO.builder()
                .cuenta(cuenta)
                .descripcion(descripcion)
                .auxiliar(auxiliar)
                .debe(valor)
                .haber(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .build());
    }

    private static void agregarHaber(
            List<AsientoObraLineaDTO> lineas, String cuenta, String descripcion, String auxiliar, BigDecimal monto) {
        BigDecimal valor = money(monto);
        if (valor.signum() == 0) {
            return;
        }
        lineas.add(AsientoObraLineaDTO.builder()
                .cuenta(cuenta)
                .descripcion(descripcion)
                .auxiliar(auxiliar)
                .debe(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .haber(valor)
                .build());
    }

    private static AsientoObraBloqueDTO bloque(String codigo, String titulo, List<AsientoObraLineaDTO> lineas) {
        BigDecimal debe = lineas.stream().map(AsientoObraLineaDTO::getDebe).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal haber = lineas.stream().map(AsientoObraLineaDTO::getHaber).reduce(BigDecimal.ZERO, BigDecimal::add);
        return AsientoObraBloqueDTO.builder()
                .codigo(codigo)
                .titulo(titulo)
                .lineas(lineas)
                .totalDebe(money(debe))
                .totalHaber(money(haber))
                .build();
    }

    private static AsientoObraDTO asiento(Rango rango, Instant ahora, List<AsientoObraBloqueDTO> bloques) {
        BigDecimal debe = bloques.stream()
                .map(AsientoObraBloqueDTO::getTotalDebe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal haber = bloques.stream()
                .map(AsientoObraBloqueDTO::getTotalHaber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return AsientoObraDTO.builder()
                .desde(rango.inicio())
                .hasta(rango.fin())
                .generadoAt(ahora)
                .bloques(bloques)
                .totalDebe(money(debe))
                .totalHaber(money(haber))
                .diferencia(money(debe.subtract(haber).abs()))
                .build();
    }

    private static String blankToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static BigDecimal money(BigDecimal valor) {
        return nz(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private record Rango(LocalDate inicio, LocalDate fin) {
    }
}
