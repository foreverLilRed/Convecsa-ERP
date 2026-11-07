package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.FacturaObraNoEncontradaException;
import com.convecsa.erp.common.exception.OperacionFactoringNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.EstadoFactoring;
import com.convecsa.erp.construccion.domain.EstadoFacturaObra;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.domain.TipoLiberacionRetencion;
import com.convecsa.erp.construccion.dto.CobranzaObraDTO;
import com.convecsa.erp.construccion.dto.CobranzaObraRequest;
import com.convecsa.erp.construccion.dto.FacturaObraDTO;
import com.convecsa.erp.construccion.dto.FacturaVencimientoRequest;
import com.convecsa.erp.construccion.dto.FlujoCajaDTO;
import com.convecsa.erp.construccion.dto.FlujoCajaPuntoDTO;
import com.convecsa.erp.construccion.dto.LiberacionRetencionDTO;
import com.convecsa.erp.construccion.dto.LiberacionRetencionRequest;
import com.convecsa.erp.construccion.dto.OperacionFactoringDTO;
import com.convecsa.erp.construccion.dto.OperacionFactoringRequest;
import com.convecsa.erp.construccion.dto.RetencionesDTO;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.CobranzaObra;
import com.convecsa.erp.construccion.entity.FacturaObra;
import com.convecsa.erp.construccion.entity.LiberacionRetencion;
import com.convecsa.erp.construccion.entity.OperacionFactoring;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.entity.Valorizacion;
import com.convecsa.erp.construccion.repository.AlmacenObraRepository;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.CobranzaObraRepository;
import com.convecsa.erp.construccion.repository.FacturaObraRepository;
import com.convecsa.erp.construccion.repository.LiberacionRetencionRepository;
import com.convecsa.erp.construccion.repository.OperacionFactoringRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaRepository;
import com.convecsa.erp.construccion.repository.ValorizacionRepository;
import com.convecsa.erp.construccion.service.InterfazFinanciamientoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class FinanciamientoServiceImpl implements InterfazFinanciamientoService {

    private static final BigDecimal IGV = new BigDecimal("0.18");
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ProyectoRepository proyectoRepository;
    private final FacturaObraRepository facturaObraRepository;
    private final CobranzaObraRepository cobranzaObraRepository;
    private final LiberacionRetencionRepository liberacionRetencionRepository;
    private final OperacionFactoringRepository operacionFactoringRepository;
    private final ValorizacionRepository valorizacionRepository;
    private final PeriodoCcRepository periodoCcRepository;
    private final BoletaCcRepository boletaCcRepository;
    private final AlmacenObraRepository almacenObraRepository;
    private final ValeSalidaRepository valeSalidaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FacturaObraDTO> listarFacturas(Long proyectoId) {
        buscarProyecto(proyectoId);
        return facturaObraRepository.findByProyectoIdOrderByNumeroDesc(proyectoId).stream()
                .map(this::toFacturaDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaObraDTO obtenerFactura(Long proyectoId, Long facturaId) {
        return toFacturaDto(buscarFactura(proyectoId, facturaId));
    }

    @Override
    @Transactional
    public FacturaObraDTO actualizarVencimiento(
            Long proyectoId, Long facturaId, FacturaVencimientoRequest request) {
        FacturaObra factura = buscarFactura(proyectoId, facturaId);
        factura.setFechaVencimiento(request == null ? null : request.getFechaVencimiento());
        return toFacturaDto(facturaObraRepository.save(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CobranzaObraDTO> listarCobranzas(Long proyectoId) {
        buscarProyecto(proyectoId);
        return cobranzaObraRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId).stream()
                .map(FinanciamientoServiceImpl::toCobranzaDto)
                .toList();
    }

    @Override
    @Transactional
    public FacturaObraDTO registrarCobranza(Long proyectoId, CobranzaObraRequest request) {
        FacturaObra factura = buscarFactura(proyectoId, request.getFacturaId());
        if (factura.getEstado() == EstadoFacturaObra.CEDIDA) {
            throw new IllegalArgumentException("No se puede cobrar una factura cedida");
        }
        if (factura.getEstado() == EstadoFacturaObra.COBRADA) {
            throw new IllegalArgumentException("La factura ya está cobrada");
        }
        BigDecimal importe = money(request.getImporte());
        BigDecimal detraccion = money(request.getDetraccion());
        BigDecimal aplicado = money(importe.add(detraccion));
        if (aplicado.signum() <= 0) {
            throw new IllegalArgumentException("El cobro debe tener importe o detracción mayor a cero");
        }
        BigDecimal saldo = saldoOperativo(factura);
        if (aplicado.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El cobro excede el saldo de la factura");
        }
        CobranzaObra cobranza = new CobranzaObra();
        cobranza.setProyecto(factura.getProyecto());
        cobranza.setFactura(factura);
        cobranza.setFecha(request.getFecha());
        cobranza.setBanco(blankToNull(request.getBanco()));
        cobranza.setNumeroOperacion(blankToNull(request.getNumeroOperacion()));
        cobranza.setImporte(importe);
        cobranza.setDetraccion(detraccion);
        cobranza.setObservaciones(blankToNull(request.getObservaciones()));
        factura.getCobranzas().add(cobranza);
        actualizarEstadoCobro(factura);
        return toFacturaDto(facturaObraRepository.save(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public RetencionesDTO obtenerRetenciones(Long proyectoId) {
        buscarProyecto(proyectoId);
        return armarRetenciones(proyectoId);
    }

    @Override
    @Transactional
    public RetencionesDTO liberarRetencion(Long proyectoId, LiberacionRetencionRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        RetencionesDTO actual = armarRetenciones(proyectoId);
        BigDecimal importe = money(request.getImporte());
        if (importe.compareTo(actual.getSaldo()) > 0) {
            throw new IllegalArgumentException("La liberación no puede superar el saldo retenido");
        }
        LiberacionRetencion movimiento = new LiberacionRetencion();
        movimiento.setProyecto(proyecto);
        movimiento.setTipo(request.getTipo());
        movimiento.setFecha(request.getFecha());
        movimiento.setImporte(importe);
        movimiento.setEntidad(blankToNull(request.getEntidad()));
        movimiento.setNumeroDocumento(blankToNull(request.getNumeroDocumento()));
        movimiento.setObservaciones(blankToNull(request.getObservaciones()));
        liberacionRetencionRepository.save(movimiento);
        return armarRetenciones(proyectoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperacionFactoringDTO> listarFactoring(Long proyectoId) {
        buscarProyecto(proyectoId);
        return operacionFactoringRepository.findByProyectoIdOrderByFechaCesionDescIdDesc(proyectoId).stream()
                .map(FinanciamientoServiceImpl::toFactoringDto)
                .toList();
    }

    @Override
    @Transactional
    public OperacionFactoringDTO cederFactura(Long proyectoId, OperacionFactoringRequest request) {
        FacturaObra factura = buscarFactura(proyectoId, request.getFacturaId());
        if (factura.getEstado() != EstadoFacturaObra.EMITIDA) {
            throw new IllegalArgumentException("Solo se puede ceder una factura emitida sin cobros");
        }
        if (aplicado(factura).signum() > 0) {
            throw new IllegalArgumentException("No se puede ceder una factura con cobros");
        }
        BigDecimal tasa = pct(request.getTasaDescuento());
        if (tasa.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("La tasa de descuento no puede superar 100%");
        }
        BigDecimal comision = money(request.getComision());
        BigDecimal nominal = money(factura.getTotal());
        BigDecimal descuento = money(nominal.multiply(tasa).divide(CIEN, 8, RoundingMode.HALF_UP));
        BigDecimal igvComision = money(comision.multiply(IGV));
        BigDecimal neto = money(nominal.subtract(descuento).subtract(comision).subtract(igvComision));
        OperacionFactoring operacion = operacionFactoringRepository.findByFacturaId(factura.getId())
                .orElseGet(OperacionFactoring::new);
        if (operacion.getId() != null && operacion.getEstado() != EstadoFactoring.REVERTIDA) {
            throw new IllegalArgumentException("La factura ya tiene una operación de factoring");
        }
        operacion.setProyecto(factura.getProyecto());
        operacion.setFactura(factura);
        operacion.setEntidadRazon(request.getEntidadRazon().trim());
        operacion.setEntidadRuc(normalizarRuc(request.getEntidadRuc()));
        operacion.setFechaCesion(request.getFechaCesion());
        operacion.setTasaDescuento(tasa);
        operacion.setComision(comision);
        operacion.setIgvComision(igvComision);
        operacion.setImporteNominal(nominal);
        operacion.setDescuento(descuento);
        operacion.setImporteNeto(neto);
        operacion.setFechaPagoEsperada(request.getFechaPagoEsperada());
        operacion.setFechaAdelanto(null);
        operacion.setEstado(EstadoFactoring.CEDIDA);
        factura.setEstado(EstadoFacturaObra.CEDIDA);
        factura.setFactoring(operacion);
        facturaObraRepository.save(factura);
        return toFactoringDto(operacionFactoringRepository.save(operacion));
    }

    @Override
    @Transactional
    public OperacionFactoringDTO adelantarFactoring(Long proyectoId, Long operacionId) {
        OperacionFactoring operacion = buscarFactoring(proyectoId, operacionId);
        if (operacion.getEstado() != EstadoFactoring.CEDIDA) {
            throw new IllegalArgumentException("Solo se puede adelantar una cesión");
        }
        operacion.setEstado(EstadoFactoring.ADELANTADA);
        operacion.setFechaAdelanto(LocalDate.now());
        return toFactoringDto(operacionFactoringRepository.save(operacion));
    }

    @Override
    @Transactional
    public OperacionFactoringDTO cobrarFactoring(Long proyectoId, Long operacionId) {
        OperacionFactoring operacion = buscarFactoring(proyectoId, operacionId);
        if (operacion.getEstado() != EstadoFactoring.ADELANTADA
                && operacion.getEstado() != EstadoFactoring.IMPAGA) {
            throw new IllegalArgumentException("Solo se puede marcar cobrada una operación adelantada o impaga");
        }
        operacion.setEstado(EstadoFactoring.COBRADA);
        return toFactoringDto(operacionFactoringRepository.save(operacion));
    }

    @Override
    @Transactional
    public OperacionFactoringDTO impagarFactoring(Long proyectoId, Long operacionId) {
        OperacionFactoring operacion = buscarFactoring(proyectoId, operacionId);
        if (operacion.getEstado() != EstadoFactoring.ADELANTADA) {
            throw new IllegalArgumentException("Solo se puede marcar impaga una operación adelantada");
        }
        operacion.setEstado(EstadoFactoring.IMPAGA);
        return toFactoringDto(operacionFactoringRepository.save(operacion));
    }

    @Override
    @Transactional
    public OperacionFactoringDTO revertirFactoring(Long proyectoId, Long operacionId) {
        OperacionFactoring operacion = buscarFactoring(proyectoId, operacionId);
        if (operacion.getEstado() != EstadoFactoring.CEDIDA) {
            throw new IllegalArgumentException("Solo se puede revertir una cesión sin adelanto");
        }
        operacion.setEstado(EstadoFactoring.REVERTIDA);
        FacturaObra factura = operacion.getFactura();
        factura.setEstado(EstadoFacturaObra.EMITIDA);
        facturaObraRepository.save(factura);
        return toFactoringDto(operacionFactoringRepository.save(operacion));
    }

    @Override
    @Transactional(readOnly = true)
    public FlujoCajaDTO flujoCaja(Long proyectoId, LocalDate desdeReq, LocalDate hastaReq) {
        Proyecto proyecto = buscarProyecto(proyectoId);
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
        LocalDate inicio = desde;
        LocalDate fin = hasta;
        Map<YearMonth, BigDecimal> ingresos = vacio(inicio, fin);
        Map<YearMonth, BigDecimal> egresos = vacio(inicio, fin);
        Map<YearMonth, BigDecimal> costoFact = vacio(inicio, fin);
        for (CobranzaObra cobranza : cobranzaObraRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId)) {
            acumular(ingresos, cobranza.getFecha(), inicio, fin, nz(cobranza.getImporte()));
        }
        for (OperacionFactoring operacion : operacionFactoringRepository
                .findByProyectoIdOrderByFechaCesionDescIdDesc(proyectoId)) {
            if (operacion.getFechaAdelanto() == null || operacion.getEstado() == EstadoFactoring.REVERTIDA) {
                continue;
            }
            acumular(ingresos, operacion.getFechaAdelanto(), inicio, fin, nz(operacion.getImporteNeto()));
            BigDecimal costo = nz(operacion.getDescuento())
                    .add(nz(operacion.getComision()))
                    .add(nz(operacion.getIgvComision()));
            acumular(costoFact, operacion.getFechaAdelanto(), inicio, fin, costo);
            acumular(egresos, operacion.getFechaAdelanto(), inicio, fin, costo);
        }
        List<PeriodoCc> periodos = periodoCcRepository.findByProyectoIdAndEstado(proyectoId, EstadoPeriodoCc.CERRADO);
        if (!periodos.isEmpty()) {
            List<Long> ids = periodos.stream().map(PeriodoCc::getId).toList();
            Map<Long, PeriodoCc> porId = new LinkedHashMap<>();
            for (PeriodoCc periodo : periodos) {
                porId.put(periodo.getId(), periodo);
            }
            for (BoletaCc boleta : boletaCcRepository.findByPeriodoIdIn(ids)) {
                PeriodoCc periodo = porId.get(boleta.getPeriodo().getId());
                BigDecimal egreso = nz(boleta.getNeto()).add(nz(boleta.getTotalAportesEmpleador()));
                acumular(egresos, periodo.getFechaHasta(), inicio, fin, egreso);
            }
        }
        almacenObraRepository.findByProyectoId(proyectoId).ifPresent(almacen -> {
            for (ValeSalida vale : valeSalidaRepository.findByAlmacenIdOrderByNumeroDesc(almacen.getId())) {
                BigDecimal costo = vale.getItems().stream()
                        .map(item -> nz(item.getCostoTotal()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                acumular(egresos, vale.getFecha(), inicio, fin, costo);
            }
        });
        List<FlujoCajaPuntoDTO> puntos = new ArrayList<>();
        BigDecimal totIng = BigDecimal.ZERO;
        BigDecimal totEgr = BigDecimal.ZERO;
        BigDecimal totCost = BigDecimal.ZERO;
        for (YearMonth mes : ingresos.keySet()) {
            BigDecimal ing = money(ingresos.get(mes));
            BigDecimal egr = money(egresos.get(mes));
            BigDecimal cost = money(costoFact.get(mes));
            totIng = totIng.add(ing);
            totEgr = totEgr.add(egr);
            totCost = totCost.add(cost);
            puntos.add(FlujoCajaPuntoDTO.builder()
                    .anio(mes.getYear())
                    .mes(mes.getMonthValue())
                    .ingresos(ing)
                    .egresos(egr)
                    .costoFactoring(cost)
                    .neto(money(ing.subtract(egr)))
                    .build());
        }
        RetencionesDTO retenciones = armarRetenciones(proyectoId);
        BigDecimal porCobrar = facturaObraRepository.findByProyectoIdOrderByNumeroDesc(proyectoId).stream()
                .map(this::saldoOperativo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cedido = operacionFactoringRepository.findByProyectoIdOrderByFechaCesionDescIdDesc(proyectoId)
                .stream()
                .filter(item -> item.getEstado() == EstadoFactoring.CEDIDA
                        || item.getEstado() == EstadoFactoring.ADELANTADA
                        || item.getEstado() == EstadoFactoring.IMPAGA)
                .map(item -> nz(item.getImporteNominal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return FlujoCajaDTO.builder()
                .porCobrar(money(porCobrar))
                .retenido(retenciones.getSaldo())
                .cedido(money(cedido))
                .ingresos(money(totIng))
                .egresos(money(totEgr))
                .costoFactoring(money(totCost))
                .neto(money(totIng.subtract(totEgr)))
                .puntos(puntos)
                .build();
    }

    private RetencionesDTO armarRetenciones(Long proyectoId) {
        BigDecimal retenido = valorizacionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoValorizacion.APROBADA)
                .stream()
                .map(Valorizacion::getMontoRetencion)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal liberado = BigDecimal.ZERO;
        BigDecimal carta = BigDecimal.ZERO;
        List<LiberacionRetencion> movimientos =
                liberacionRetencionRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId);
        for (LiberacionRetencion item : movimientos) {
            if (item.getTipo() == TipoLiberacionRetencion.CARTA_FIANZA) {
                carta = carta.add(nz(item.getImporte()));
            } else {
                liberado = liberado.add(nz(item.getImporte()));
            }
        }
        BigDecimal saldo = money(retenido.subtract(liberado).subtract(carta));
        if (saldo.signum() < 0) {
            saldo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return RetencionesDTO.builder()
                .retenidoAcumulado(money(retenido))
                .liberado(money(liberado))
                .cartaFianza(money(carta))
                .saldo(saldo)
                .movimientos(movimientos.stream().map(FinanciamientoServiceImpl::toLiberacionDto).toList())
                .build();
    }

    private void actualizarEstadoCobro(FacturaObra factura) {
        BigDecimal saldo = money(nz(factura.getTotal()).subtract(aplicado(factura)));
        if (saldo.signum() <= 0) {
            factura.setEstado(EstadoFacturaObra.COBRADA);
        } else {
            factura.setEstado(EstadoFacturaObra.PARCIAL);
        }
    }

    private BigDecimal saldoOperativo(FacturaObra factura) {
        if (factura.getEstado() == EstadoFacturaObra.CEDIDA) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal saldo = money(nz(factura.getTotal()).subtract(aplicado(factura)));
        return saldo.signum() < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : saldo;
    }

    private BigDecimal aplicado(FacturaObra factura) {
        List<CobranzaObra> cobranzas = factura.getCobranzas() == null || factura.getCobranzas().isEmpty()
                ? cobranzaObraRepository.findByFacturaIdOrderByFechaAscIdAsc(factura.getId())
                : factura.getCobranzas();
        return cobranzas.stream()
                .map(item -> nz(item.getImporte()).add(nz(item.getDetraccion())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FacturaObraDTO toFacturaDto(FacturaObra factura) {
        List<CobranzaObra> cobranzas = factura.getCobranzas() == null || factura.getCobranzas().isEmpty()
                ? cobranzaObraRepository.findByFacturaIdOrderByFechaAscIdAsc(factura.getId())
                : factura.getCobranzas();
        OperacionFactoring factoring = factura.getFactoring();
        if (factoring == null) {
            factoring = operacionFactoringRepository.findByFacturaId(factura.getId())
                    .filter(item -> item.getEstado() != EstadoFactoring.REVERTIDA)
                    .orElse(null);
        } else if (factoring.getEstado() == EstadoFactoring.REVERTIDA) {
            factoring = null;
        }
        return FacturaObraDTO.builder()
                .id(factura.getId())
                .valorizacionId(factura.getValorizacion() == null ? null : factura.getValorizacion().getId())
                .numero(factura.getNumero())
                .fecha(factura.getFecha())
                .fechaVencimiento(factura.getFechaVencimiento())
                .clienteRazon(factura.getClienteRazon())
                .clienteRuc(factura.getClienteRuc())
                .subtotal(money(factura.getSubtotal()))
                .igv(money(factura.getIgv()))
                .total(money(factura.getTotal()))
                .saldo(saldoOperativo(factura))
                .estado(factura.getEstado())
                .createdAt(factura.getCreatedAt())
                .cobranzas(cobranzas.stream().map(FinanciamientoServiceImpl::toCobranzaDto).toList())
                .factoring(factoring == null ? null : toFactoringDto(factoring))
                .build();
    }

    private static CobranzaObraDTO toCobranzaDto(CobranzaObra cobranza) {
        BigDecimal importe = money(cobranza.getImporte());
        BigDecimal detraccion = money(cobranza.getDetraccion());
        return CobranzaObraDTO.builder()
                .id(cobranza.getId())
                .proyectoId(cobranza.getProyecto() == null ? null : cobranza.getProyecto().getId())
                .facturaId(cobranza.getFactura() == null ? null : cobranza.getFactura().getId())
                .facturaNumero(cobranza.getFactura() == null ? 0 : cobranza.getFactura().getNumero())
                .fecha(cobranza.getFecha())
                .banco(cobranza.getBanco())
                .numeroOperacion(cobranza.getNumeroOperacion())
                .importe(importe)
                .detraccion(detraccion)
                .aplicado(money(importe.add(detraccion)))
                .observaciones(cobranza.getObservaciones())
                .createdAt(cobranza.getCreatedAt())
                .build();
    }

    private static OperacionFactoringDTO toFactoringDto(OperacionFactoring operacion) {
        return OperacionFactoringDTO.builder()
                .id(operacion.getId())
                .proyectoId(operacion.getProyecto() == null ? null : operacion.getProyecto().getId())
                .facturaId(operacion.getFactura() == null ? null : operacion.getFactura().getId())
                .facturaNumero(operacion.getFactura() == null ? 0 : operacion.getFactura().getNumero())
                .entidadRazon(operacion.getEntidadRazon())
                .entidadRuc(operacion.getEntidadRuc())
                .fechaCesion(operacion.getFechaCesion())
                .tasaDescuento(pct(operacion.getTasaDescuento()))
                .comision(money(operacion.getComision()))
                .igvComision(money(operacion.getIgvComision()))
                .importeNominal(money(operacion.getImporteNominal()))
                .descuento(money(operacion.getDescuento()))
                .importeNeto(money(operacion.getImporteNeto()))
                .fechaPagoEsperada(operacion.getFechaPagoEsperada())
                .fechaAdelanto(operacion.getFechaAdelanto())
                .estado(operacion.getEstado())
                .createdAt(operacion.getCreatedAt())
                .build();
    }

    private static LiberacionRetencionDTO toLiberacionDto(LiberacionRetencion movimiento) {
        return LiberacionRetencionDTO.builder()
                .id(movimiento.getId())
                .proyectoId(movimiento.getProyecto() == null ? null : movimiento.getProyecto().getId())
                .tipo(movimiento.getTipo())
                .fecha(movimiento.getFecha())
                .importe(money(movimiento.getImporte()))
                .entidad(movimiento.getEntidad())
                .numeroDocumento(movimiento.getNumeroDocumento())
                .observaciones(movimiento.getObservaciones())
                .createdAt(movimiento.getCreatedAt())
                .build();
    }

    private Proyecto buscarProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private FacturaObra buscarFactura(Long proyectoId, Long facturaId) {
        return facturaObraRepository.findByIdAndProyectoId(facturaId, proyectoId)
                .orElseThrow(() -> new FacturaObraNoEncontradaException("No se encontró la factura"));
    }

    private OperacionFactoring buscarFactoring(Long proyectoId, Long operacionId) {
        return operacionFactoringRepository.findByIdAndProyectoId(operacionId, proyectoId)
                .orElseThrow(() -> new OperacionFactoringNoEncontradaException("No se encontró la operación de factoring"));
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
        if (fecha == null || fecha.isBefore(desde) || fecha.isAfter(hasta)) {
            return;
        }
        mapa.merge(YearMonth.from(fecha), nz(monto), BigDecimal::add);
    }

    private static String normalizarRuc(String ruc) {
        String valor = blankToNull(ruc);
        if (valor == null) {
            return null;
        }
        if (!valor.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC de la entidad debe tener 11 dígitos");
        }
        return valor;
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

    private static BigDecimal pct(BigDecimal valor) {
        return nz(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
