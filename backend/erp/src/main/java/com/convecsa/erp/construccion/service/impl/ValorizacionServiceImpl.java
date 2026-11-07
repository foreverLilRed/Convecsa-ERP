package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.AdicionalObraNoEncontradoException;
import com.convecsa.erp.common.exception.AvanceNoEncontradoException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.ValorizacionNoEncontradaException;
import com.convecsa.erp.construccion.domain.EstadoAdicionalObra;
import com.convecsa.erp.construccion.domain.EstadoAvanceObra;
import com.convecsa.erp.construccion.domain.EstadoFacturaObra;
import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.TipoAdicionalObra;
import com.convecsa.erp.construccion.domain.TipoValorizacion;
import com.convecsa.erp.construccion.dto.AdicionalObraDTO;
import com.convecsa.erp.construccion.dto.AdicionalObraRequest;
import com.convecsa.erp.construccion.dto.AvanceGuardarRequest;
import com.convecsa.erp.construccion.dto.AvanceObraDTO;
import com.convecsa.erp.construccion.dto.AvancePartidaDTO;
import com.convecsa.erp.construccion.dto.AvancePartidaRequest;
import com.convecsa.erp.construccion.dto.CronogramaGuardarRequest;
import com.convecsa.erp.construccion.dto.CronogramaPartidaDTO;
import com.convecsa.erp.construccion.dto.CronogramaPartidaRequest;
import com.convecsa.erp.construccion.dto.CurvaSDTO;
import com.convecsa.erp.construccion.dto.CurvaSPuntoDTO;
import com.convecsa.erp.construccion.dto.FacturaObraDTO;
import com.convecsa.erp.construccion.dto.ValorizacionCrearRequest;
import com.convecsa.erp.construccion.dto.ValorizacionDTO;
import com.convecsa.erp.construccion.dto.ValorizacionItemDTO;
import com.convecsa.erp.construccion.entity.AdicionalObra;
import com.convecsa.erp.construccion.entity.AvanceObra;
import com.convecsa.erp.construccion.entity.AvancePartida;
import com.convecsa.erp.construccion.entity.CronogramaPartida;
import com.convecsa.erp.construccion.entity.FacturaObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.Valorizacion;
import com.convecsa.erp.construccion.entity.ValorizacionItem;
import com.convecsa.erp.construccion.repository.AdicionalObraRepository;
import com.convecsa.erp.construccion.repository.AvanceObraRepository;
import com.convecsa.erp.construccion.repository.CronogramaPartidaRepository;
import com.convecsa.erp.construccion.repository.FacturaObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ValorizacionItemRepository;
import com.convecsa.erp.construccion.repository.ValorizacionRepository;
import com.convecsa.erp.construccion.service.InterfazValorizacionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ValorizacionServiceImpl implements InterfazValorizacionService {

    private static final BigDecimal IGV = new BigDecimal("0.18");
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ProyectoRepository proyectoRepository;
    private final PresupuestoVersionRepository presupuestoVersionRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;
    private final CronogramaPartidaRepository cronogramaPartidaRepository;
    private final AvanceObraRepository avanceObraRepository;
    private final AdicionalObraRepository adicionalObraRepository;
    private final ValorizacionRepository valorizacionRepository;
    private final ValorizacionItemRepository valorizacionItemRepository;
    private final FacturaObraRepository facturaObraRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CronogramaPartidaDTO> listarCronograma(Long proyectoId) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        List<PartidaPresupuesto> partidas = partidasVigentes(proyectoId);
        Map<Long, CronogramaPartida> porPartida = new LinkedHashMap<>();
        for (CronogramaPartida fila : cronogramaPartidaRepository.findByProyectoIdOrderByCodigoAsc(proyectoId)) {
            if (fila.getPartida() != null) {
                porPartida.put(fila.getPartida().getId(), fila);
            }
        }
        Map<Long, BigDecimal> ejecutado = acumuladoPorPartida(proyectoId, Integer.MAX_VALUE, 12);
        List<CronogramaPartidaDTO> resultado = new ArrayList<>();
        for (PartidaPresupuesto partida : partidas) {
            CronogramaPartida cronograma = porPartida.get(partida.getId());
            LocalDate inicio = cronograma == null ? proyecto.getFechaInicio() : cronograma.getFechaInicio();
            LocalDate fin = cronograma == null ? proyecto.getFechaFin() : cronograma.getFechaFin();
            resultado.add(toCronogramaDto(
                    partida,
                    inicio,
                    fin,
                    ejecutado.getOrDefault(partida.getId(), BigDecimal.ZERO)));
        }
        return resultado;
    }

    @Override
    @Transactional
    public List<CronogramaPartidaDTO> guardarCronograma(Long proyectoId, CronogramaGuardarRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        Map<Long, PartidaPresupuesto> vigentes = indexarVigentes(proyectoId);
        cronogramaPartidaRepository.deleteByProyectoId(proyectoId);
        cronogramaPartidaRepository.flush();
        List<CronogramaPartidaRequest> filas = request.getPartidas() == null ? List.of() : request.getPartidas();
        for (CronogramaPartidaRequest fila : filas) {
            PartidaPresupuesto partida = vigentes.get(fila.getPartidaId());
            if (partida == null) {
                throw new IllegalArgumentException("La partida no pertenece al presupuesto vigente");
            }
            if (fila.getFechaInicio() != null && fila.getFechaFin() != null && fila.getFechaFin().isBefore(fila.getFechaInicio())) {
                throw new IllegalArgumentException("La fecha de fin no puede ser menor que la de inicio");
            }
            CronogramaPartida cronograma = new CronogramaPartida();
            cronograma.setProyecto(proyecto);
            cronograma.setPartida(partida);
            cronograma.setCodigo(partida.getCodigo());
            cronograma.setDescripcion(partida.getDescripcion());
            cronograma.setFechaInicio(fila.getFechaInicio());
            cronograma.setFechaFin(fila.getFechaFin());
            cronogramaPartidaRepository.save(cronograma);
        }
        return listarCronograma(proyectoId);
    }

    @Override
    @Transactional(readOnly = true)
    public AvanceObraDTO obtenerAvance(Long proyectoId, int anio, int mes) {
        validarMes(anio, mes);
        buscarProyecto(proyectoId);
        AvanceObra avance = avanceObraRepository.findByProyectoIdAndAnioAndMes(proyectoId, anio, mes).orElse(null);
        return armarAvanceDto(proyectoId, anio, mes, avance);
    }

    @Override
    @Transactional
    public AvanceObraDTO guardarAvance(Long proyectoId, AvanceGuardarRequest request) {
        int anio = request.getAnio();
        int mes = request.getMes();
        validarMes(anio, mes);
        Proyecto proyecto = buscarProyecto(proyectoId);
        AvanceObra avance = avanceObraRepository.findByProyectoIdAndAnioAndMes(proyectoId, anio, mes)
                .orElseGet(() -> {
                    AvanceObra nuevo = new AvanceObra();
                    nuevo.setProyecto(proyecto);
                    nuevo.setAnio(anio);
                    nuevo.setMes(mes);
                    nuevo.setEstado(EstadoAvanceObra.BORRADOR);
                    return nuevo;
                });
        if (avance.getEstado() == EstadoAvanceObra.CERRADO) {
            throw new IllegalArgumentException("No se puede editar un avance cerrado");
        }
        Map<Long, PartidaPresupuesto> vigentes = indexarVigentes(proyectoId);
        Map<Long, BigDecimal> anterior = acumuladoPorPartida(proyectoId, anio, mes);
        avance.getItems().clear();
        int orden = 0;
        List<AvancePartidaRequest> filas = request.getItems() == null ? List.of() : request.getItems();
        for (AvancePartidaRequest fila : filas) {
            PartidaPresupuesto partida = vigentes.get(fila.getPartidaId());
            if (partida == null) {
                throw new IllegalArgumentException("La partida no pertenece al presupuesto vigente");
            }
            BigDecimal periodo = qty(fila.getMetradoPeriodo());
            AvancePartida item = new AvancePartida();
            item.setAvance(avance);
            item.setPartida(partida);
            item.setCodigo(partida.getCodigo());
            item.setDescripcion(partida.getDescripcion());
            item.setUnidad(partida.getUnidad());
            item.setCentroCosto(partida.getCentroCosto());
            item.setMetradoMeta(qty(partida.getMetrado()));
            item.setMetradoPeriodo(periodo);
            item.setMetradoAcumulado(qty(anterior.getOrDefault(partida.getId(), BigDecimal.ZERO).add(periodo)));
            item.setOrden(orden++);
            avance.getItems().add(item);
        }
        return armarAvanceDto(proyectoId, anio, mes, avanceObraRepository.save(avance));
    }

    @Override
    @Transactional
    public AvanceObraDTO cerrarAvance(Long proyectoId, Long avanceId) {
        AvanceObra avance = avanceObraRepository.findByIdAndProyectoId(avanceId, proyectoId)
                .orElseThrow(() -> new AvanceNoEncontradoException("No se encontró el avance"));
        if (avance.getEstado() == EstadoAvanceObra.CERRADO) {
            return armarAvanceDto(proyectoId, avance.getAnio(), avance.getMes(), avance);
        }
        if (partidasVigentes(proyectoId).isEmpty()) {
            throw new IllegalArgumentException("La obra no tiene presupuesto vigente");
        }
        avance.setEstado(EstadoAvanceObra.CERRADO);
        avance.setClosedAt(Instant.now());
        AvanceObra saved = avanceObraRepository.save(avance);
        return armarAvanceDto(proyectoId, saved.getAnio(), saved.getMes(), saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdicionalObraDTO> listarAdicionales(Long proyectoId) {
        buscarProyecto(proyectoId);
        return adicionalObraRepository.findByProyectoIdOrderByIdDesc(proyectoId).stream()
                .map(ValorizacionServiceImpl::toAdicionalDto)
                .toList();
    }

    @Override
    @Transactional
    public AdicionalObraDTO crearAdicional(Long proyectoId, AdicionalObraRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        AdicionalObra adicional = new AdicionalObra();
        adicional.setProyecto(proyecto);
        aplicarAdicional(adicional, request, proyectoId);
        adicional.setEstado(EstadoAdicionalObra.BORRADOR);
        return toAdicionalDto(adicionalObraRepository.save(adicional));
    }

    @Override
    @Transactional
    public AdicionalObraDTO guardarAdicional(Long proyectoId, Long adicionalId, AdicionalObraRequest request) {
        AdicionalObra adicional = buscarAdicional(proyectoId, adicionalId);
        if (adicional.getEstado() != EstadoAdicionalObra.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede editar un adicional en borrador");
        }
        aplicarAdicional(adicional, request, proyectoId);
        return toAdicionalDto(adicionalObraRepository.save(adicional));
    }

    @Override
    @Transactional
    public AdicionalObraDTO vigentarAdicional(Long proyectoId, Long adicionalId) {
        AdicionalObra adicional = buscarAdicional(proyectoId, adicionalId);
        if (adicional.getEstado() != EstadoAdicionalObra.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede vigentar un adicional en borrador");
        }
        adicional.setEstado(EstadoAdicionalObra.VIGENTE);
        return toAdicionalDto(adicionalObraRepository.save(adicional));
    }

    @Override
    @Transactional
    public AdicionalObraDTO anularAdicional(Long proyectoId, Long adicionalId) {
        AdicionalObra adicional = buscarAdicional(proyectoId, adicionalId);
        if (adicional.getEstado() == EstadoAdicionalObra.ANULADO) {
            return toAdicionalDto(adicional);
        }
        if (!valorizacionItemRepository.findByAdicionalId(adicionalId).isEmpty()) {
            throw new IllegalArgumentException("No se puede anular un adicional ya valorizado");
        }
        adicional.setEstado(EstadoAdicionalObra.ANULADO);
        return toAdicionalDto(adicionalObraRepository.save(adicional));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValorizacionDTO> listarValorizaciones(Long proyectoId) {
        buscarProyecto(proyectoId);
        return valorizacionRepository.findByProyectoIdOrderByNumeroDesc(proyectoId).stream()
                .map(ValorizacionServiceImpl::toValorizacionDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ValorizacionDTO obtenerValorizacion(Long proyectoId, Long valorizacionId) {
        return toValorizacionDto(buscarValorizacion(proyectoId, valorizacionId));
    }

    @Override
    @Transactional
    public ValorizacionDTO crearValorizacion(Long proyectoId, ValorizacionCrearRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        TipoValorizacion tipo = request.getTipo();
        int anio = request.getAnio();
        int mes = request.getMes();
        validarMes(anio, mes);
        Valorizacion existente = tipo == TipoValorizacion.REGULAR
                ? valorizacionRepository.findByProyectoIdAndTipoAndAnioAndMes(proyectoId, tipo, anio, mes).orElse(null)
                : null;
        if (existente != null && (existente.getEstado() == EstadoValorizacion.PRESENTADA
                || existente.getEstado() == EstadoValorizacion.APROBADA)) {
            throw new IllegalArgumentException("Ya existe una valorización regular de ese mes");
        }
        Valorizacion valorizacion = existente != null ? existente : new Valorizacion();
        if (existente == null) {
            valorizacion.setProyecto(proyecto);
            valorizacion.setNumero(siguienteNumeroVal(proyectoId));
            valorizacion.setTipo(tipo);
            valorizacion.setAnio(anio);
            valorizacion.setMes(mes);
        }
        if (existente != null && existente.getEstado() != EstadoValorizacion.BORRADOR
                && existente.getEstado() != EstadoValorizacion.OBSERVADA) {
            throw new IllegalArgumentException("Solo se puede regenerar una valorización en borrador u observada");
        }
        valorizacion.setEstado(EstadoValorizacion.BORRADOR);
        valorizacion.setPorcentajeRetencion(pct(proyecto.getPorcentajeRetencion()));
        armarItems(valorizacion, proyectoId, tipo, anio, mes);
        recalcularMontos(valorizacion);
        return toValorizacionDto(valorizacionRepository.save(valorizacion));
    }

    @Override
    @Transactional
    public ValorizacionDTO presentarValorizacion(Long proyectoId, Long valorizacionId) {
        Valorizacion valorizacion = buscarValorizacion(proyectoId, valorizacionId);
        if (valorizacion.getEstado() != EstadoValorizacion.BORRADOR
                && valorizacion.getEstado() != EstadoValorizacion.OBSERVADA) {
            throw new IllegalArgumentException("Solo se puede presentar un borrador o una observada");
        }
        if (valorizacion.getItems().isEmpty()) {
            throw new IllegalArgumentException("No se puede presentar una valorización sin ítems");
        }
        valorizacion.setEstado(EstadoValorizacion.PRESENTADA);
        return toValorizacionDto(valorizacionRepository.save(valorizacion));
    }

    @Override
    @Transactional
    public ValorizacionDTO observarValorizacion(Long proyectoId, Long valorizacionId) {
        Valorizacion valorizacion = buscarValorizacion(proyectoId, valorizacionId);
        if (valorizacion.getEstado() != EstadoValorizacion.PRESENTADA) {
            throw new IllegalArgumentException("Solo se puede observar una valorización presentada");
        }
        valorizacion.setEstado(EstadoValorizacion.OBSERVADA);
        return toValorizacionDto(valorizacionRepository.save(valorizacion));
    }

    @Override
    @Transactional
    public ValorizacionDTO aprobarValorizacion(Long proyectoId, Long valorizacionId) {
        Valorizacion valorizacion = buscarValorizacion(proyectoId, valorizacionId);
        if (valorizacion.getEstado() != EstadoValorizacion.PRESENTADA) {
            throw new IllegalArgumentException("Solo se puede aprobar una valorización presentada");
        }
        valorizacion.setEstado(EstadoValorizacion.APROBADA);
        return toValorizacionDto(valorizacionRepository.save(valorizacion));
    }

    @Override
    @Transactional
    public ValorizacionDTO facturarValorizacion(Long proyectoId, Long valorizacionId) {
        Valorizacion valorizacion = buscarValorizacion(proyectoId, valorizacionId);
        if (valorizacion.getEstado() != EstadoValorizacion.APROBADA) {
            throw new IllegalArgumentException("Solo se puede facturar una valorización aprobada");
        }
        if (facturaObraRepository.existsByValorizacionId(valorizacionId)) {
            return toValorizacionDto(valorizacion);
        }
        Proyecto proyecto = valorizacion.getProyecto();
        FacturaObra factura = new FacturaObra();
        factura.setProyecto(proyecto);
        factura.setValorizacion(valorizacion);
        factura.setNumero(siguienteNumeroFactura(proyectoId));
        factura.setFecha(LocalDate.now());
        factura.setClienteRazon(proyecto.getClienteRazon());
        factura.setClienteRuc(proyecto.getClienteRuc());
        BigDecimal subtotal = money(valorizacion.getMontoNeto());
        BigDecimal igv = money(subtotal.multiply(IGV));
        factura.setSubtotal(subtotal);
        factura.setIgv(igv);
        factura.setTotal(money(subtotal.add(igv)));
        factura.setEstado(EstadoFacturaObra.EMITIDA);
        valorizacion.setFactura(facturaObraRepository.save(factura));
        return toValorizacionDto(valorizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public CurvaSDTO curvaS(Long proyectoId) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        List<PartidaPresupuesto> partidas = partidasVigentes(proyectoId);
        Map<Long, CronogramaPartida> cronograma = new LinkedHashMap<>();
        for (CronogramaPartida fila : cronogramaPartidaRepository.findByProyectoIdOrderByCodigoAsc(proyectoId)) {
            if (fila.getPartida() != null) {
                cronograma.put(fila.getPartida().getId(), fila);
            }
        }
        LocalDate desde = proyecto.getFechaInicio();
        LocalDate hasta = proyecto.getFechaFin();
        for (CronogramaPartida fila : cronograma.values()) {
            if (fila.getFechaInicio() != null && (desde == null || fila.getFechaInicio().isBefore(desde))) {
                desde = fila.getFechaInicio();
            }
            if (fila.getFechaFin() != null && (hasta == null || fila.getFechaFin().isAfter(hasta))) {
                hasta = fila.getFechaFin();
            }
        }
        if (desde == null) {
            desde = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        }
        if (hasta == null) {
            hasta = LocalDate.of(desde.getYear(), 12, 31);
            if (hasta.isBefore(desde)) {
                hasta = desde.plusMonths(11);
            }
        }
        BigDecimal metradoMeta = BigDecimal.ZERO;
        BigDecimal montoMeta = BigDecimal.ZERO;
        Map<YearMonth, BigDecimal> progFis = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> progEco = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(desde);
        YearMonth fin = YearMonth.from(hasta);
        while (!cursor.isAfter(fin)) {
            progFis.put(cursor, BigDecimal.ZERO);
            progEco.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }
        for (PartidaPresupuesto partida : partidas) {
            metradoMeta = metradoMeta.add(nz(partida.getMetrado()));
            montoMeta = montoMeta.add(nz(partida.getTotal()));
            CronogramaPartida crono = cronograma.get(partida.getId());
            LocalDate ini = crono != null && crono.getFechaInicio() != null ? crono.getFechaInicio() : desde;
            LocalDate end = crono != null && crono.getFechaFin() != null ? crono.getFechaFin() : hasta;
            List<YearMonth> meses = mesesEntre(ini, end);
            if (meses.isEmpty()) {
                continue;
            }
            BigDecimal n = new BigDecimal(meses.size());
            BigDecimal fisMes = qty(nz(partida.getMetrado()).divide(n, 8, RoundingMode.HALF_UP));
            BigDecimal ecoMes = money(nz(partida.getTotal()).divide(n, 8, RoundingMode.HALF_UP));
            for (YearMonth mes : meses) {
                progFis.merge(mes, fisMes, BigDecimal::add);
                progEco.merge(mes, ecoMes, BigDecimal::add);
            }
        }
        Map<YearMonth, BigDecimal> ejeFisAcum = new LinkedHashMap<>();
        BigDecimal corridoFis = BigDecimal.ZERO;
        Map<YearMonth, BigDecimal> ejeEcoAcum = new LinkedHashMap<>();
        BigDecimal corridoEco = BigDecimal.ZERO;
        for (AvanceObra avance : avanceObraRepository.findByProyectoIdOrderByAnioAscMesAsc(proyectoId)) {
            YearMonth clave = YearMonth.of(avance.getAnio(), avance.getMes());
            BigDecimal periodo = avance.getItems().stream()
                    .map(item -> nz(item.getMetradoPeriodo()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            corridoFis = corridoFis.add(periodo);
            ejeFisAcum.put(clave, corridoFis);
        }
        List<Valorizacion> aprobadas = new ArrayList<>(valorizacionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoValorizacion.APROBADA));
        aprobadas.sort((a, b) -> {
            int cmp = Integer.compare(a.getAnio(), b.getAnio());
            return cmp != 0 ? cmp : Integer.compare(a.getMes(), b.getMes());
        });
        for (Valorizacion valorizacion : aprobadas) {
            YearMonth clave = YearMonth.of(valorizacion.getAnio(), valorizacion.getMes());
            corridoEco = corridoEco.add(nz(valorizacion.getMontoBruto()));
            ejeEcoAcum.put(clave, corridoEco);
        }
        BigDecimal lastFis = BigDecimal.ZERO;
        BigDecimal lastEco = BigDecimal.ZERO;
        BigDecimal progFisCorrido = BigDecimal.ZERO;
        BigDecimal progEcoCorrido = BigDecimal.ZERO;
        List<CurvaSPuntoDTO> puntos = new ArrayList<>();
        for (YearMonth mes : progFis.keySet()) {
            progFisCorrido = progFisCorrido.add(progFis.get(mes));
            progEcoCorrido = progEcoCorrido.add(progEco.get(mes));
            if (ejeFisAcum.containsKey(mes)) {
                lastFis = ejeFisAcum.get(mes);
            }
            if (ejeEcoAcum.containsKey(mes)) {
                lastEco = ejeEcoAcum.get(mes);
            }
            puntos.add(CurvaSPuntoDTO.builder()
                    .anio(mes.getYear())
                    .mes(mes.getMonthValue())
                    .programadoFisico(qty(progFisCorrido))
                    .ejecutadoFisico(qty(lastFis))
                    .programadoEconomico(money(progEcoCorrido))
                    .ejecutadoEconomico(money(lastEco))
                    .desvioFisico(qty(lastFis.subtract(progFisCorrido)))
                    .desvioEconomico(money(lastEco.subtract(progEcoCorrido)))
                    .build());
        }
        Map<Long, BigDecimal> ejecutado = acumuladoPorPartida(proyectoId, Integer.MAX_VALUE, 12);
        BigDecimal ejecutadoTotal = ejecutado.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return CurvaSDTO.builder()
                .metradoMeta(qty(metradoMeta))
                .montoMeta(money(montoMeta))
                .desvioPlazoPct(desvioPlazo(desde, hasta, ejecutadoTotal, metradoMeta))
                .puntos(puntos)
                .build();
    }

    private void armarItems(Valorizacion valorizacion, Long proyectoId, TipoValorizacion tipo, int anio, int mes) {
        valorizacion.getItems().clear();
        if (tipo == TipoValorizacion.REGULAR) {
            AvanceObra avance = avanceObraRepository.findByProyectoIdAndAnioAndMes(proyectoId, anio, mes)
                    .orElseThrow(() -> new AvanceNoEncontradoException("No hay avance para ese mes"));
            if (avance.getEstado() != EstadoAvanceObra.CERRADO) {
                throw new IllegalArgumentException("El avance del mes debe estar cerrado");
            }
            Map<Long, BigDecimal> anteriorAprobado = anteriorAprobado(proyectoId);
            int orden = 0;
            for (AvancePartida origen : avance.getItems()) {
                PartidaPresupuesto partida = origen.getPartida();
                BigDecimal pu = partida == null ? BigDecimal.ZERO : nz(partida.getPrecioUnitario());
                BigDecimal periodo = qty(origen.getMetradoPeriodo());
                BigDecimal anterior = qty(anteriorAprobado.getOrDefault(
                        origen.getPartida() == null ? -1L : origen.getPartida().getId(), BigDecimal.ZERO));
                ValorizacionItem item = new ValorizacionItem();
                item.setValorizacion(valorizacion);
                item.setPartida(partida);
                item.setCodigo(origen.getCodigo());
                item.setDescripcion(origen.getDescripcion());
                item.setUnidad(origen.getUnidad());
                item.setCentroCosto(origen.getCentroCosto());
                item.setMetradoAnterior(anterior);
                item.setMetradoPeriodo(periodo);
                item.setMetradoAcumulado(qty(anterior.add(periodo)));
                item.setPrecioUnitario(money(pu));
                item.setTotalPeriodo(money(periodo.multiply(pu)));
                item.setTotalAcumulado(money(item.getMetradoAcumulado().multiply(pu)));
                item.setOrden(orden++);
                valorizacion.getItems().add(item);
            }
            return;
        }
        TipoAdicionalObra tipoAd = tipo == TipoValorizacion.DEDUCTIVO
                ? TipoAdicionalObra.DEDUCTIVO
                : TipoAdicionalObra.ADICIONAL;
        int orden = 0;
        BigDecimal signo = tipo == TipoValorizacion.DEDUCTIVO ? new BigDecimal("-1") : BigDecimal.ONE;
        for (AdicionalObra adicional : adicionalObraRepository.findByProyectoIdAndTipoAndEstado(
                proyectoId, tipoAd, EstadoAdicionalObra.VIGENTE)) {
            boolean usado = valorizacionItemRepository.findByAdicionalId(adicional.getId()).stream()
                    .anyMatch(item -> item.getValorizacion() != null
                            && !Objects.equals(item.getValorizacion().getId(), valorizacion.getId()));
            if (usado) {
                continue;
            }
            ValorizacionItem item = new ValorizacionItem();
            item.setValorizacion(valorizacion);
            item.setAdicional(adicional);
            item.setPartida(adicional.getPartida());
            item.setCodigo(adicional.getCodigo());
            item.setDescripcion(adicional.getDescripcion());
            item.setUnidad(adicional.getUnidad());
            item.setMetradoAnterior(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            item.setMetradoPeriodo(qty(adicional.getMetrado()));
            item.setMetradoAcumulado(qty(adicional.getMetrado()));
            item.setPrecioUnitario(money(adicional.getPrecioUnitario()));
            item.setTotalPeriodo(money(adicional.getTotal().multiply(signo)));
            item.setTotalAcumulado(item.getTotalPeriodo());
            item.setOrden(orden++);
            valorizacion.getItems().add(item);
        }
        if (valorizacion.getItems().isEmpty()) {
            throw new IllegalArgumentException("No hay adicionales vigentes pendientes de valorizar");
        }
    }

    private void recalcularMontos(Valorizacion valorizacion) {
        BigDecimal bruto = valorizacion.getItems().stream()
                .map(item -> money(item.getTotalPeriodo()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retencion = money(bruto.abs().multiply(pct(valorizacion.getPorcentajeRetencion()))
                .divide(CIEN, 8, RoundingMode.HALF_UP));
        if (bruto.signum() < 0) {
            retencion = retencion.negate();
        }
        valorizacion.setMontoBruto(money(bruto));
        valorizacion.setMontoRetencion(retencion);
        valorizacion.setMontoNeto(money(bruto.subtract(retencion)));
    }

    private Map<Long, BigDecimal> anteriorAprobado(Long proyectoId) {
        Map<Long, BigDecimal> anterior = new LinkedHashMap<>();
        List<Valorizacion> aprobadas = valorizacionRepository.findByProyectoIdAndTipoAndEstadoOrderByAnioDescMesDesc(
                proyectoId, TipoValorizacion.REGULAR, EstadoValorizacion.APROBADA);
        if (aprobadas.isEmpty()) {
            return anterior;
        }
        Valorizacion ultima = valorizacionRepository.findByIdAndProyectoId(aprobadas.get(0).getId(), proyectoId)
                .orElse(aprobadas.get(0));
        for (ValorizacionItem item : ultima.getItems()) {
            if (item.getPartida() != null) {
                anterior.put(item.getPartida().getId(), qty(item.getMetradoAcumulado()));
            }
        }
        return anterior;
    }

    private AvanceObraDTO armarAvanceDto(Long proyectoId, int anio, int mes, AvanceObra avance) {
        Map<Long, BigDecimal> anterior = acumuladoPorPartida(proyectoId, anio, mes);
        Map<Long, AvancePartida> actuales = new LinkedHashMap<>();
        if (avance != null) {
            for (AvancePartida item : avance.getItems()) {
                if (item.getPartida() != null) {
                    actuales.put(item.getPartida().getId(), item);
                }
            }
        }
        List<AvancePartidaDTO> items = new ArrayList<>();
        for (PartidaPresupuesto partida : partidasVigentes(proyectoId)) {
            AvancePartida actual = actuales.get(partida.getId());
            BigDecimal periodo = actual == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP) : qty(actual.getMetradoPeriodo());
            BigDecimal ant = qty(anterior.getOrDefault(partida.getId(), BigDecimal.ZERO));
            BigDecimal acum = qty(ant.add(periodo));
            BigDecimal meta = qty(partida.getMetrado());
            items.add(AvancePartidaDTO.builder()
                    .partidaId(partida.getId())
                    .codigo(partida.getCodigo())
                    .descripcion(partida.getDescripcion())
                    .unidad(partida.getUnidad())
                    .centroCosto(partida.getCentroCosto())
                    .metradoMeta(meta)
                    .metradoAnterior(ant)
                    .metradoPeriodo(periodo)
                    .metradoAcumulado(acum)
                    .desvio(qty(acum.subtract(meta)))
                    .build());
        }
        return AvanceObraDTO.builder()
                .id(avance == null ? null : avance.getId())
                .proyectoId(proyectoId)
                .anio(anio)
                .mes(mes)
                .estado(avance == null ? EstadoAvanceObra.BORRADOR : avance.getEstado())
                .createdAt(avance == null ? null : avance.getCreatedAt())
                .closedAt(avance == null ? null : avance.getClosedAt())
                .items(items)
                .build();
    }

    private Map<Long, BigDecimal> acumuladoPorPartida(Long proyectoId, int anioHasta, int mesHasta) {
        Map<Long, BigDecimal> acumulado = new LinkedHashMap<>();
        for (AvanceObra avance : avanceObraRepository.findByProyectoIdOrderByAnioAscMesAsc(proyectoId)) {
            if (avance.getAnio() > anioHasta || (avance.getAnio() == anioHasta && avance.getMes() >= mesHasta)) {
                continue;
            }
            for (AvancePartida item : avance.getItems()) {
                if (item.getPartida() == null) {
                    continue;
                }
                acumulado.merge(item.getPartida().getId(), nz(item.getMetradoPeriodo()), BigDecimal::add);
            }
        }
        acumulado.replaceAll((clave, valor) -> qty(valor));
        return acumulado;
    }

    private CronogramaPartidaDTO toCronogramaDto(
            PartidaPresupuesto partida, LocalDate inicio, LocalDate fin, BigDecimal ejecutado) {
        return CronogramaPartidaDTO.builder()
                .partidaId(partida.getId())
                .codigo(partida.getCodigo())
                .descripcion(partida.getDescripcion())
                .metradoMeta(qty(partida.getMetrado()))
                .fechaInicio(inicio)
                .fechaFin(fin)
                .desvioPlazoPct(desvioPlazo(inicio, fin, ejecutado, partida.getMetrado()))
                .build();
    }

    private void aplicarAdicional(AdicionalObra adicional, AdicionalObraRequest request, Long proyectoId) {
        adicional.setTipo(request.getTipo());
        adicional.setCodigo(request.getCodigo().trim());
        adicional.setDescripcion(request.getDescripcion().trim());
        adicional.setUnidad(blankToNull(request.getUnidad()));
        adicional.setMetrado(qty(request.getMetrado()));
        adicional.setPrecioUnitario(money(request.getPrecioUnitario()));
        adicional.setTotal(money(qty(request.getMetrado()).multiply(nz(request.getPrecioUnitario()))));
        if (request.getPartidaId() == null) {
            adicional.setPartida(null);
            return;
        }
        PartidaPresupuesto partida = indexarVigentes(proyectoId).get(request.getPartidaId());
        if (partida == null) {
            throw new IllegalArgumentException("La partida no pertenece al presupuesto vigente");
        }
        adicional.setPartida(partida);
    }

    private Map<Long, PartidaPresupuesto> indexarVigentes(Long proyectoId) {
        Map<Long, PartidaPresupuesto> mapa = new LinkedHashMap<>();
        for (PartidaPresupuesto partida : partidasVigentes(proyectoId)) {
            mapa.put(partida.getId(), partida);
        }
        return mapa;
    }

    private List<PartidaPresupuesto> partidasVigentes(Long proyectoId) {
        PresupuestoVersion vigente = presupuestoVersionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)
                .orElseThrow(() -> new IllegalArgumentException("La obra no tiene presupuesto vigente"));
        return partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(vigente.getId());
    }

    private Proyecto buscarProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private AdicionalObra buscarAdicional(Long proyectoId, Long adicionalId) {
        return adicionalObraRepository.findByIdAndProyectoId(adicionalId, proyectoId)
                .orElseThrow(() -> new AdicionalObraNoEncontradoException("No se encontró el adicional"));
    }

    private Valorizacion buscarValorizacion(Long proyectoId, Long valorizacionId) {
        return valorizacionRepository.findByIdAndProyectoId(valorizacionId, proyectoId)
                .orElseThrow(() -> new ValorizacionNoEncontradaException("No se encontró la valorización"));
    }

    private int siguienteNumeroVal(Long proyectoId) {
        return valorizacionRepository.findFirstByProyectoIdOrderByNumeroDesc(proyectoId)
                .map(item -> item.getNumero() + 1)
                .orElse(1);
    }

    private int siguienteNumeroFactura(Long proyectoId) {
        return facturaObraRepository.findFirstByProyectoIdOrderByNumeroDesc(proyectoId)
                .map(item -> item.getNumero() + 1)
                .orElse(1);
    }

    private static List<YearMonth> mesesEntre(LocalDate inicio, LocalDate fin) {
        List<YearMonth> meses = new ArrayList<>();
        YearMonth cursor = YearMonth.from(inicio);
        YearMonth ultimo = YearMonth.from(fin);
        while (!cursor.isAfter(ultimo)) {
            meses.add(cursor);
            cursor = cursor.plusMonths(1);
        }
        return meses;
    }

    private static BigDecimal desvioPlazo(LocalDate inicio, LocalDate fin, BigDecimal ejecutado, BigDecimal meta) {
        if (inicio == null || fin == null || fin.isBefore(inicio) || nz(meta).signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long totalDias = Math.max(1, ChronoUnit.DAYS.between(inicio, fin));
        long transcurridos = ChronoUnit.DAYS.between(inicio, LocalDate.now());
        BigDecimal tiempoPct = CIEN.multiply(BigDecimal.valueOf(Math.max(0, Math.min(transcurridos, totalDias))))
                .divide(BigDecimal.valueOf(totalDias), 8, RoundingMode.HALF_UP);
        BigDecimal fisicoPct = CIEN.multiply(nz(ejecutado)).divide(nz(meta), 8, RoundingMode.HALF_UP);
        return money(fisicoPct.subtract(tiempoPct));
    }

    private static ValorizacionDTO toValorizacionDto(Valorizacion valorizacion) {
        return ValorizacionDTO.builder()
                .id(valorizacion.getId())
                .proyectoId(valorizacion.getProyecto().getId())
                .numero(valorizacion.getNumero())
                .tipo(valorizacion.getTipo())
                .estado(valorizacion.getEstado())
                .anio(valorizacion.getAnio())
                .mes(valorizacion.getMes())
                .porcentajeRetencion(pct(valorizacion.getPorcentajeRetencion()))
                .montoBruto(money(valorizacion.getMontoBruto()))
                .montoRetencion(money(valorizacion.getMontoRetencion()))
                .montoNeto(money(valorizacion.getMontoNeto()))
                .observaciones(valorizacion.getObservaciones())
                .createdAt(valorizacion.getCreatedAt())
                .items(valorizacion.getItems().stream().map(ValorizacionServiceImpl::toItemDto).toList())
                .factura(valorizacion.getFactura() == null ? null : toFacturaDto(valorizacion.getFactura()))
                .build();
    }

    private static ValorizacionItemDTO toItemDto(ValorizacionItem item) {
        return ValorizacionItemDTO.builder()
                .id(item.getId())
                .partidaId(item.getPartida() == null ? null : item.getPartida().getId())
                .adicionalId(item.getAdicional() == null ? null : item.getAdicional().getId())
                .codigo(item.getCodigo())
                .descripcion(item.getDescripcion())
                .unidad(item.getUnidad())
                .centroCosto(item.getCentroCosto())
                .metradoAnterior(qty(item.getMetradoAnterior()))
                .metradoPeriodo(qty(item.getMetradoPeriodo()))
                .metradoAcumulado(qty(item.getMetradoAcumulado()))
                .precioUnitario(money(item.getPrecioUnitario()))
                .totalPeriodo(money(item.getTotalPeriodo()))
                .totalAcumulado(money(item.getTotalAcumulado()))
                .build();
    }

    private static FacturaObraDTO toFacturaDto(FacturaObra factura) {
        return FacturaObraDTO.builder()
                .id(factura.getId())
                .valorizacionId(factura.getValorizacion().getId())
                .numero(factura.getNumero())
                .fecha(factura.getFecha())
                .fechaVencimiento(factura.getFechaVencimiento())
                .clienteRazon(factura.getClienteRazon())
                .clienteRuc(factura.getClienteRuc())
                .subtotal(money(factura.getSubtotal()))
                .igv(money(factura.getIgv()))
                .total(money(factura.getTotal()))
                .estado(factura.getEstado())
                .createdAt(factura.getCreatedAt())
                .build();
    }

    private static AdicionalObraDTO toAdicionalDto(AdicionalObra adicional) {
        return AdicionalObraDTO.builder()
                .id(adicional.getId())
                .proyectoId(adicional.getProyecto().getId())
                .tipo(adicional.getTipo())
                .estado(adicional.getEstado())
                .partidaId(adicional.getPartida() == null ? null : adicional.getPartida().getId())
                .codigo(adicional.getCodigo())
                .descripcion(adicional.getDescripcion())
                .unidad(adicional.getUnidad())
                .metrado(qty(adicional.getMetrado()))
                .precioUnitario(money(adicional.getPrecioUnitario()))
                .total(money(adicional.getTotal()))
                .createdAt(adicional.getCreatedAt())
                .build();
    }

    private static void validarMes(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }
        if (anio < 2000) {
            throw new IllegalArgumentException("El año no es válido");
        }
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

    private static BigDecimal qty(BigDecimal valor) {
        return nz(valor).setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal pct(BigDecimal valor) {
        return nz(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
