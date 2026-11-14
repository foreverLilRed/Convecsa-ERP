package com.convecsa.erp.soma.controller;

import com.convecsa.erp.soma.dto.SomaCapacitacionAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionRequest;
import com.convecsa.erp.soma.dto.SomaCharlaAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCharlaAtsDTO;
import com.convecsa.erp.soma.dto.SomaCharlaAtsRequest;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.dto.SomaHallazgoDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoLevantarRequest;
import com.convecsa.erp.soma.dto.SomaHallazgoRequest;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaInspeccionDTO;
import com.convecsa.erp.soma.dto.SomaInspeccionRequest;
import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import com.convecsa.erp.soma.dto.SomaIpercFilaDTO;
import com.convecsa.erp.soma.dto.SomaIpercFilaRequest;
import com.convecsa.erp.soma.dto.SomaPetarChecklistItemDTO;
import com.convecsa.erp.soma.dto.SomaPetarDTO;
import com.convecsa.erp.soma.dto.SomaPetarFaltanteTareoDTO;
import com.convecsa.erp.soma.dto.SomaPetarRequest;
import com.convecsa.erp.soma.dto.SomaPreUsoBloqueoDTO;
import com.convecsa.erp.soma.dto.SomaPreUsoDTO;
import com.convecsa.erp.soma.dto.SomaPreUsoRequest;
import com.convecsa.erp.soma.dto.SomaComiteActaDTO;
import com.convecsa.erp.soma.dto.SomaComiteActaRequest;
import com.convecsa.erp.soma.dto.SomaComiteMiembroDTO;
import com.convecsa.erp.soma.dto.SomaComiteMiembroRequest;
import com.convecsa.erp.soma.dto.SomaComiteRequerimientoDTO;
import com.convecsa.erp.soma.dto.SomaEventoDTO;
import com.convecsa.erp.soma.dto.SomaEventoRequest;
import com.convecsa.erp.soma.dto.SomaEventoSyncDmDTO;
import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.dto.SomaEntregaEppDTO;
import com.convecsa.erp.soma.dto.SomaEntregaEppRequest;
import com.convecsa.erp.soma.dto.SomaEppItemEstadoRequest;
import com.convecsa.erp.soma.dto.SomaEppMaterialDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.dto.SomaSctrNominaGuardarRequest;
import com.convecsa.erp.soma.dto.SomaSctrPolizaDTO;
import com.convecsa.erp.soma.dto.SomaSctrPolizaRequest;
import com.convecsa.erp.soma.dto.SomaTrabajadorObraDTO;
import com.convecsa.erp.soma.service.InterfazSomaCapacitacionService;
import com.convecsa.erp.soma.service.InterfazSomaCharlaAtsService;
import com.convecsa.erp.soma.service.InterfazSomaEmoService;
import com.convecsa.erp.soma.service.InterfazSomaEppService;
import com.convecsa.erp.soma.service.InterfazSomaHabilitacionService;
import com.convecsa.erp.soma.service.InterfazSomaInspeccionService;
import com.convecsa.erp.soma.service.InterfazSomaIpercService;
import com.convecsa.erp.soma.service.InterfazSomaObraService;
import com.convecsa.erp.soma.service.InterfazSomaPetarService;
import com.convecsa.erp.soma.service.InterfazSomaPreUsoService;
import com.convecsa.erp.soma.service.InterfazSomaComiteService;
import com.convecsa.erp.soma.service.InterfazSomaEventoService;
import com.convecsa.erp.soma.service.InterfazSomaEstadisticaService;
import com.convecsa.erp.soma.service.InterfazSomaSctrService;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos/{proyectoId:\\d+}/soma")
@RequiredArgsConstructor
public class SomaObraController {

    private final InterfazSomaObraService somaObraService;
    private final InterfazSomaEmoService somaEmoService;
    private final InterfazSomaSctrService somaSctrService;
    private final InterfazSomaEppService somaEppService;
    private final InterfazSomaCapacitacionService somaCapacitacionService;
    private final InterfazSomaHabilitacionService somaHabilitacionService;
    private final InterfazSomaIpercService somaIpercService;
    private final InterfazSomaCharlaAtsService somaCharlaAtsService;
    private final InterfazSomaPetarService somaPetarService;
    private final InterfazSomaInspeccionService somaInspeccionService;
    private final InterfazSomaPreUsoService somaPreUsoService;
    private final InterfazSomaEventoService somaEventoService;
    private final InterfazSomaEstadisticaService somaEstadisticaService;
    private final InterfazSomaComiteService somaComiteService;

    @GetMapping("/resumen")
    public SomaResumenObraDTO resumen(@PathVariable Long proyectoId) {
        return somaObraService.obtenerResumen(proyectoId);
    }

    @GetMapping("/estadisticas")
    public SomaEstadisticaAnioDTO estadisticas(
            @PathVariable Long proyectoId, @RequestParam(required = false) Integer anio) {
        return somaEstadisticaService.porObra(proyectoId, anio);
    }

    @GetMapping("/habilitacion")
    public SomaHabilitacionDTO habilitacion(
            @PathVariable Long proyectoId, @RequestParam(required = false) LocalDate fecha) {
        return somaHabilitacionService.evaluarObra(proyectoId, fecha);
    }

    @GetMapping("/emos")
    public List<SomaEmoDTO> listarEmos(@PathVariable Long proyectoId) {
        return somaEmoService.listarPorObra(proyectoId);
    }

    @PostMapping("/emos")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaEmoDTO registrarEmo(@PathVariable Long proyectoId, @RequestBody SomaEmoRequest request) {
        return somaEmoService.registrar(proyectoId, request);
    }

    @GetMapping("/sctr")
    public List<SomaSctrPolizaDTO> listarSctr(@PathVariable Long proyectoId) {
        return somaSctrService.listar(proyectoId);
    }

    @PostMapping("/sctr")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaSctrPolizaDTO crearSctr(@PathVariable Long proyectoId, @RequestBody SomaSctrPolizaRequest request) {
        return somaSctrService.crear(proyectoId, request);
    }

    @GetMapping("/sctr/sugerir-nomina")
    public List<SomaTrabajadorObraDTO> sugerirNominaSctr(
            @PathVariable Long proyectoId, @RequestParam Integer anio, @RequestParam Integer mes) {
        return somaSctrService.sugerirNomina(proyectoId, anio, mes);
    }

    @PutMapping("/sctr/{polizaId}/nomina")
    public SomaSctrPolizaDTO guardarNominaSctr(
            @PathVariable Long proyectoId,
            @PathVariable Long polizaId,
            @RequestBody SomaSctrNominaGuardarRequest request) {
        return somaSctrService.guardarNomina(proyectoId, polizaId, request);
    }

    @PostMapping("/sctr/{polizaId}/anular")
    public SomaSctrPolizaDTO anularSctr(@PathVariable Long proyectoId, @PathVariable Long polizaId) {
        return somaSctrService.anular(proyectoId, polizaId);
    }

    @GetMapping("/epps")
    public List<SomaEntregaEppDTO> listarEpps(@PathVariable Long proyectoId) {
        return somaEppService.listar(proyectoId);
    }

    @PostMapping("/epps")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaEntregaEppDTO crearEpp(@PathVariable Long proyectoId, @RequestBody SomaEntregaEppRequest request) {
        return somaEppService.crear(proyectoId, request);
    }

    @PutMapping("/epps/items/{itemId}/estado")
    public SomaEntregaEppDTO cambiarEstadoItemEpp(
            @PathVariable Long proyectoId,
            @PathVariable Long itemId,
            @RequestBody SomaEppItemEstadoRequest request) {
        return somaEppService.cambiarEstadoItem(proyectoId, itemId, request);
    }

    @GetMapping("/epps/materiales")
    public List<SomaEppMaterialDTO> listarMaterialesEpp(@PathVariable Long proyectoId) {
        return somaEppService.listarMateriales(proyectoId);
    }

    @GetMapping(value = "/epps/kardex/{personaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> kardexEppPdf(@PathVariable Long proyectoId, @PathVariable Long personaId) {
        byte[] pdf = somaEppService.kardexPdf(proyectoId, personaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("kardex-epp-" + personaId + ".pdf", StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/iperc")
    public List<SomaIpercFilaDTO> listarIperc(
            @PathVariable Long proyectoId,
            @RequestParam(required = false) Long partidaId,
            @RequestParam(required = false) Boolean incluirInactivos) {
        return somaIpercService.listar(proyectoId, partidaId, incluirInactivos);
    }

    @PostMapping("/iperc")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaIpercFilaDTO crearIperc(@PathVariable Long proyectoId, @RequestBody SomaIpercFilaRequest request) {
        return somaIpercService.crear(proyectoId, request);
    }

    @PutMapping("/iperc/{filaId}")
    public SomaIpercFilaDTO actualizarIperc(
            @PathVariable Long proyectoId,
            @PathVariable Long filaId,
            @RequestBody SomaIpercFilaRequest request) {
        return somaIpercService.actualizar(proyectoId, filaId, request);
    }

    @PostMapping("/iperc/{filaId}/desactivar")
    public SomaIpercFilaDTO desactivarIperc(@PathVariable Long proyectoId, @PathVariable Long filaId) {
        return somaIpercService.desactivar(proyectoId, filaId);
    }

    @GetMapping("/capacitaciones")
    public List<SomaCapacitacionDTO> listarCapacitaciones(@PathVariable Long proyectoId) {
        return somaCapacitacionService.listar(proyectoId);
    }

    @PostMapping("/capacitaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaCapacitacionDTO crearCapacitacion(
            @PathVariable Long proyectoId, @RequestBody SomaCapacitacionRequest request) {
        return somaCapacitacionService.crear(proyectoId, request);
    }

    @GetMapping("/capacitaciones/precargar-cuadrilla")
    public List<SomaCapacitacionAsistenteDTO> precargarCuadrilla(
            @PathVariable Long proyectoId, @RequestParam Long cuadrillaId) {
        return somaCapacitacionService.precargarCuadrilla(proyectoId, cuadrillaId);
    }

    @GetMapping("/charlas-ats")
    public List<SomaCharlaAtsDTO> listarCharlasAts(
            @PathVariable Long proyectoId, @RequestParam(required = false) LocalDate fecha) {
        return somaCharlaAtsService.listar(proyectoId, fecha);
    }

    @PostMapping("/charlas-ats")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaCharlaAtsDTO crearCharlaAts(
            @PathVariable Long proyectoId, @RequestBody SomaCharlaAtsRequest request) {
        return somaCharlaAtsService.crear(proyectoId, request);
    }

    @GetMapping("/petar")
    public List<SomaPetarDTO> listarPetar(
            @PathVariable Long proyectoId, @RequestParam(required = false) LocalDate fecha) {
        return somaPetarService.listar(proyectoId, fecha);
    }

    @PostMapping("/petar")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaPetarDTO crearPetar(@PathVariable Long proyectoId, @RequestBody SomaPetarRequest request) {
        return somaPetarService.crear(proyectoId, request);
    }

    @GetMapping("/petar/faltantes-tareo")
    public List<SomaPetarFaltanteTareoDTO> faltantesTareoPetar(@PathVariable Long proyectoId) {
        return somaPetarService.faltantesTareo(proyectoId);
    }

    @GetMapping("/petar/checklist/{tipo}")
    public List<SomaPetarChecklistItemDTO> checklistPetar(
            @PathVariable Long proyectoId, @PathVariable TipoTrabajoPetar tipo) {
        return somaPetarService.checklist(tipo);
    }

    @PutMapping("/petar/{petarId}")
    public SomaPetarDTO actualizarPetar(
            @PathVariable Long proyectoId,
            @PathVariable Long petarId,
            @RequestBody SomaPetarRequest request) {
        return somaPetarService.actualizar(proyectoId, petarId, request);
    }

    @PostMapping("/petar/{petarId}/autorizar")
    public SomaPetarDTO autorizarPetar(@PathVariable Long proyectoId, @PathVariable Long petarId) {
        return somaPetarService.autorizar(proyectoId, petarId);
    }

    @PostMapping("/petar/{petarId}/cerrar")
    public SomaPetarDTO cerrarPetar(@PathVariable Long proyectoId, @PathVariable Long petarId) {
        return somaPetarService.cerrar(proyectoId, petarId);
    }

    @PostMapping("/petar/{petarId}/suspender")
    public SomaPetarDTO suspenderPetar(@PathVariable Long proyectoId, @PathVariable Long petarId) {
        return somaPetarService.suspender(proyectoId, petarId);
    }

    @PostMapping("/petar/{petarId}/reanudar")
    public SomaPetarDTO reanudarPetar(@PathVariable Long proyectoId, @PathVariable Long petarId) {
        return somaPetarService.reanudar(proyectoId, petarId);
    }

    @GetMapping("/charlas-ats/precargar-cuadrilla")
    public List<SomaCharlaAsistenteDTO> precargarCuadrillaCharlaAts(
            @PathVariable Long proyectoId,
            @RequestParam Long cuadrillaId,
            @RequestParam(required = false) LocalDate fecha) {
        return somaCharlaAtsService.precargarCuadrilla(proyectoId, cuadrillaId, fecha);
    }

    @GetMapping("/inspecciones")
    public List<SomaInspeccionDTO> listarInspecciones(@PathVariable Long proyectoId) {
        return somaInspeccionService.listarInspecciones(proyectoId);
    }

    @PostMapping("/inspecciones")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaInspeccionDTO crearInspeccion(
            @PathVariable Long proyectoId, @RequestBody SomaInspeccionRequest request) {
        return somaInspeccionService.crearInspeccion(proyectoId, request);
    }

    @GetMapping("/hallazgos")
    public List<SomaHallazgoDTO> listarHallazgos(
            @PathVariable Long proyectoId,
            @RequestParam(required = false) EstadoHallazgoSoma estado,
            @RequestParam(required = false) Boolean vencidos) {
        return somaInspeccionService.listarHallazgos(proyectoId, estado, vencidos);
    }

    @PostMapping("/hallazgos")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaHallazgoDTO crearHallazgo(
            @PathVariable Long proyectoId, @RequestBody SomaHallazgoRequest request) {
        return somaInspeccionService.crearHallazgo(proyectoId, request);
    }

    @PutMapping("/hallazgos/{hallazgoId}/en-proceso")
    public SomaHallazgoDTO marcarHallazgoEnProceso(
            @PathVariable Long proyectoId, @PathVariable Long hallazgoId) {
        return somaInspeccionService.marcarEnProceso(proyectoId, hallazgoId);
    }

    @PutMapping("/hallazgos/{hallazgoId}/levantar")
    public SomaHallazgoDTO levantarHallazgo(
            @PathVariable Long proyectoId,
            @PathVariable Long hallazgoId,
            @RequestBody SomaHallazgoLevantarRequest request) {
        return somaInspeccionService.levantar(proyectoId, hallazgoId, request);
    }

    @GetMapping("/equipos-preuso")
    public List<SomaPreUsoDTO> listarPreUso(@PathVariable Long proyectoId) {
        return somaPreUsoService.listar(proyectoId);
    }

    @PostMapping("/equipos-preuso")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaPreUsoDTO crearPreUso(@PathVariable Long proyectoId, @RequestBody SomaPreUsoRequest request) {
        return somaPreUsoService.crear(proyectoId, request);
    }

    @GetMapping("/equipos-preuso/bloqueos")
    public List<SomaPreUsoBloqueoDTO> bloqueosPreUso(
            @PathVariable Long proyectoId, @RequestParam LocalDate fecha) {
        return somaPreUsoService.listarBloqueos(proyectoId, fecha);
    }

    @GetMapping("/eventos")
    public List<SomaEventoDTO> listarEventos(@PathVariable Long proyectoId) {
        return somaEventoService.listar(proyectoId);
    }

    @PostMapping("/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaEventoDTO crearEvento(@PathVariable Long proyectoId, @RequestBody SomaEventoRequest request) {
        return somaEventoService.crear(proyectoId, request);
    }

    @PutMapping("/eventos/{eventoId}")
    public SomaEventoDTO actualizarEvento(
            @PathVariable Long proyectoId,
            @PathVariable Long eventoId,
            @RequestBody SomaEventoRequest request) {
        return somaEventoService.actualizar(proyectoId, eventoId, request);
    }

    @PostMapping("/eventos/{eventoId}/hallazgos")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaHallazgoDTO agregarHallazgoEvento(
            @PathVariable Long proyectoId,
            @PathVariable Long eventoId,
            @RequestBody SomaHallazgoRequest request) {
        return somaEventoService.agregarHallazgo(proyectoId, eventoId, request);
    }

    @PostMapping("/eventos/{eventoId}/cerrar")
    public SomaEventoDTO cerrarEvento(@PathVariable Long proyectoId, @PathVariable Long eventoId) {
        return somaEventoService.cerrar(proyectoId, eventoId);
    }

    @PostMapping("/eventos/{eventoId}/sincronizar-dm")
    public SomaEventoSyncDmDTO sincronizarDmEvento(@PathVariable Long proyectoId, @PathVariable Long eventoId) {
        return somaEventoService.sincronizarDm(proyectoId, eventoId);
    }

    @GetMapping("/comite/requerimiento")
    public SomaComiteRequerimientoDTO requerimientoComite(@PathVariable Long proyectoId) {
        return somaComiteService.requerimiento(proyectoId);
    }

    @GetMapping("/comite/miembros")
    public List<SomaComiteMiembroDTO> listarMiembrosComite(@PathVariable Long proyectoId) {
        return somaComiteService.listarMiembros(proyectoId);
    }

    @PostMapping("/comite/miembros")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaComiteMiembroDTO crearMiembroComite(
            @PathVariable Long proyectoId, @RequestBody SomaComiteMiembroRequest request) {
        return somaComiteService.crearMiembro(proyectoId, request);
    }

    @PostMapping("/comite/miembros/{miembroId}/baja")
    public SomaComiteMiembroDTO darBajaMiembroComite(
            @PathVariable Long proyectoId, @PathVariable Long miembroId) {
        return somaComiteService.darBaja(proyectoId, miembroId);
    }

    @GetMapping("/comite/actas")
    public List<SomaComiteActaDTO> listarActasComite(@PathVariable Long proyectoId) {
        return somaComiteService.listarActas(proyectoId);
    }

    @PostMapping("/comite/actas")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaComiteActaDTO crearActaComite(
            @PathVariable Long proyectoId, @RequestBody SomaComiteActaRequest request) {
        return somaComiteService.crearActa(proyectoId, request);
    }
}
