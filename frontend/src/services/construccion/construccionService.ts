import { ApiError, apiRequest, downloadAuthenticatedRequest, openAuthenticatedDocument, openAuthenticatedRequest } from '@/services/http'
import { clearSession, getSession } from '@/services/auth/session'
import type { BoletaEnvioResumen } from '@/types/nomina'
import type {
  AdicionalObra,
  AdicionalObraRequest,
  AlmacenObra,
  AvanceObra,
  BancoDispersion,
  BoletaCc,
  BoletaCcResumen,
  ComparativoPresupuesto,
  ConstruccionCatalogos,
  CuadrillaMiembroRequest,
  CuadrillaObra,
  CuadrillaObraRequest,
  CronogramaPartida,
  CurvaS,
  InventarioCierre,
  MaterialCatalogo,
  MaterialRequest,
  MovimientoAlmacen,
  OrdenCompra,
  OrdenCompraRequest,
  PartidaPresupuestoRequest,
  PeriodoCc,
  PeriodoCcRequest,
  PersonalObra,
  Presupuesto,
  Proyecto,
  ProyectoRequest,
  ProyectoResponsableRequest,
  RecepcionRequest,
  RequerimientoObra,
  RequerimientoRequest,
  ResponsableObra,
  SugeridoApu,
  TareoCcDia,
  TareoCcGrilla,
  TipoValorizacion,
  TipoVersionPresupuesto,
  ValeSalida,
  ValeSalidaRequest,
  Valorizacion,
  CobranzaObra,
  CobranzaObraRequest,
  FacturaObra,
  FlujoCaja,
  AccesoObra,
  AccesoObraRequest,
  AlertaObra,
  AuditoriaObra,
  AsientosObra,
  AsignacionEquipo,
  AsignacionEquipoRequest,
  BitacoraObra,
  BitacoraObraRequest,
  DocumentoObra,
  ImputacionEquipo,
  ImputacionEquipoRequest,
  RentabilidadObra,
  SubcontratoAvance,
  SubcontratoAvanceRequest,
  SubcontratoObra,
  SubcontratoObraRequest,
  TipoDocumentoObra,
  UsuarioAsignable,
  OperacionFactoring,
  OperacionFactoringRequest,
  RetencionesObra,
} from '@/types/construccion'

export type TareoCcGuardar = {
  dias: TareoCcDia[]
}

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export function obtenerCatalogosConstruccion(): Promise<ConstruccionCatalogos> {
  return apiRequest<ConstruccionCatalogos>('/api/proyectos/catalogos')
}

export function listarProyectos(activo?: boolean): Promise<Proyecto[]> {
  const params = activo === undefined ? '' : `?activo=${activo}`
  return apiRequest<Proyecto[]>(`/api/proyectos${params}`)
}

export function crearProyecto(request: ProyectoRequest): Promise<Proyecto> {
  return apiRequest<Proyecto>('/api/proyectos', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerProyecto(id: number): Promise<Proyecto> {
  return apiRequest<Proyecto>(`/api/proyectos/${id}`)
}

export function actualizarProyecto(id: number, request: ProyectoRequest): Promise<Proyecto> {
  return apiRequest<Proyecto>(`/api/proyectos/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function listarResponsablesObra(proyectoId: number): Promise<ResponsableObra[]> {
  return apiRequest<ResponsableObra[]>(`/api/proyectos/${proyectoId}/responsables`)
}

export function guardarResponsablesObra(
  proyectoId: number,
  responsables: ProyectoResponsableRequest[],
): Promise<ResponsableObra[]> {
  return apiRequest<ResponsableObra[]>(`/api/proyectos/${proyectoId}/responsables`, {
    method: 'PUT',
    body: JSON.stringify({ responsables }),
  })
}

export function obtenerPresupuesto(proyectoId: number, versionId?: number): Promise<Presupuesto> {
  const params = versionId == null ? '' : `?versionId=${versionId}`
  return apiRequest<Presupuesto>(`/api/proyectos/${proyectoId}/presupuesto${params}`)
}

export function crearVersionPresupuesto(
  proyectoId: number,
  tipo?: TipoVersionPresupuesto,
): Promise<Presupuesto> {
  return apiRequest<Presupuesto>(`/api/proyectos/${proyectoId}/presupuesto/versiones`, {
    method: 'POST',
    body: JSON.stringify(tipo ? { tipo } : {}),
  })
}

export function guardarPartidasPresupuesto(
  proyectoId: number,
  versionId: number,
  partidas: PartidaPresupuestoRequest[],
): Promise<Presupuesto> {
  return apiRequest<Presupuesto>(
    `/api/proyectos/${proyectoId}/presupuesto/versiones/${versionId}/partidas`,
    { method: 'PUT', body: JSON.stringify({ partidas }) },
  )
}

export function vigentarPresupuesto(proyectoId: number, versionId: number): Promise<Presupuesto> {
  return apiRequest<Presupuesto>(
    `/api/proyectos/${proyectoId}/presupuesto/versiones/${versionId}/vigentar`,
    { method: 'POST' },
  )
}

export function descargarPlantillaPresupuesto(proyectoId: number, versionId?: number): Promise<void> {
  const params = versionId == null ? '' : `?versionId=${versionId}`
  return downloadAuthenticatedRequest(
    `/api/proyectos/${proyectoId}/presupuesto/plantilla${params}`,
    'presupuesto-plantilla.xlsx',
  )
}

export async function importarPresupuestoExcel(
  proyectoId: number,
  versionId: number,
  file: File,
): Promise<Presupuesto> {
  const session = getSession()
  const headers: Record<string, string> = session?.token ? { Authorization: `Bearer ${session.token}` } : {}
  const body = new FormData()
  body.append('file', file)
  const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  let response: Response
  try {
    response = await fetch(
      `${API_BASE_URL}/api/proyectos/${proyectoId}/presupuesto/versiones/${versionId}/importar`,
      { method: 'POST', headers, body },
    )
  } catch {
    throw new ApiError('No se pudo conectar con el servidor.', 0)
  }
  if (response.status === 401) {
    clearSession()
    const pathname = window.location.pathname
    if (pathname !== '/login' && !pathname.startsWith('/invitar/')) {
      window.location.assign('/login')
    }
  }
  if (!response.ok) {
    let message = `Error ${response.status}`
    try {
      const payload = (await response.json()) as { message?: string }
      message = payload.message ?? message
    } catch {
      // respuesta no JSON
    }
    throw new ApiError(message, response.status)
  }
  return (await response.json()) as Presupuesto
}

export function obtenerComparativoPresupuesto(proyectoId: number): Promise<ComparativoPresupuesto> {
  return apiRequest<ComparativoPresupuesto>(`/api/proyectos/${proyectoId}/presupuesto/comparativo`)
}

export function listarMateriales(): Promise<MaterialCatalogo[]> {
  return apiRequest<MaterialCatalogo[]>('/api/proyectos/materiales')
}

export function crearMaterial(request: MaterialRequest): Promise<MaterialCatalogo> {
  return apiRequest<MaterialCatalogo>('/api/proyectos/materiales', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function actualizarMaterial(materialId: number, request: MaterialRequest): Promise<MaterialCatalogo> {
  return apiRequest<MaterialCatalogo>(`/api/proyectos/materiales/${materialId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function obtenerAlmacen(proyectoId: number): Promise<AlmacenObra> {
  return apiRequest<AlmacenObra>(`/api/proyectos/${proyectoId}/almacen`)
}

export function listarKardex(proyectoId: number, materialId?: number): Promise<MovimientoAlmacen[]> {
  const params = materialId == null ? '' : `?materialId=${materialId}`
  return apiRequest<MovimientoAlmacen[]>(`/api/proyectos/${proyectoId}/almacen/kardex${params}`)
}

export function registrarAjusteAlmacen(
  proyectoId: number,
  request: { materialId: number; cantidad: number; costoUnitario?: number | null; fecha?: string | null; observaciones?: string | null },
): Promise<AlmacenObra> {
  return apiRequest<AlmacenObra>(`/api/proyectos/${proyectoId}/almacen/ajustes`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function cerrarInventario(proyectoId: number, anio: number, mes: number): Promise<InventarioCierre> {
  return apiRequest<InventarioCierre>(`/api/proyectos/${proyectoId}/almacen/cierre`, {
    method: 'POST',
    body: JSON.stringify({ anio, mes }),
  })
}

export function listarCierresInventario(proyectoId: number): Promise<InventarioCierre[]> {
  return apiRequest<InventarioCierre[]>(`/api/proyectos/${proyectoId}/almacen/cierres`)
}

export function listarRequerimientosObra(proyectoId: number): Promise<RequerimientoObra[]> {
  return apiRequest<RequerimientoObra[]>(`/api/proyectos/${proyectoId}/requerimientos`)
}

export function crearRequerimientoObra(
  proyectoId: number,
  request: RequerimientoRequest,
): Promise<RequerimientoObra> {
  return apiRequest<RequerimientoObra>(`/api/proyectos/${proyectoId}/requerimientos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function guardarRequerimientoObra(
  proyectoId: number,
  reqId: number,
  request: RequerimientoRequest,
): Promise<RequerimientoObra> {
  return apiRequest<RequerimientoObra>(`/api/proyectos/${proyectoId}/requerimientos/${reqId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function aprobarRequerimientoObra(proyectoId: number, reqId: number): Promise<RequerimientoObra> {
  return apiRequest<RequerimientoObra>(`/api/proyectos/${proyectoId}/requerimientos/${reqId}/aprobar`, {
    method: 'POST',
  })
}

export function anularRequerimientoObra(proyectoId: number, reqId: number): Promise<RequerimientoObra> {
  return apiRequest<RequerimientoObra>(`/api/proyectos/${proyectoId}/requerimientos/${reqId}/anular`, {
    method: 'POST',
  })
}

export function sugeridosApuMateriales(proyectoId: number): Promise<SugeridoApu[]> {
  return apiRequest<SugeridoApu[]>(`/api/proyectos/${proyectoId}/requerimientos/sugeridos-apu`)
}

export function listarOrdenesCompra(proyectoId: number): Promise<OrdenCompra[]> {
  return apiRequest<OrdenCompra[]>(`/api/proyectos/${proyectoId}/compras`)
}

export function obtenerOrdenCompra(proyectoId: number, ocId: number): Promise<OrdenCompra> {
  return apiRequest<OrdenCompra>(`/api/proyectos/${proyectoId}/compras/${ocId}`)
}

export function crearOrdenCompra(proyectoId: number, request: OrdenCompraRequest): Promise<OrdenCompra> {
  return apiRequest<OrdenCompra>(`/api/proyectos/${proyectoId}/compras`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function guardarOrdenCompra(
  proyectoId: number,
  ocId: number,
  request: OrdenCompraRequest,
): Promise<OrdenCompra> {
  return apiRequest<OrdenCompra>(`/api/proyectos/${proyectoId}/compras/${ocId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function emitirOrdenCompra(proyectoId: number, ocId: number): Promise<OrdenCompra> {
  return apiRequest<OrdenCompra>(`/api/proyectos/${proyectoId}/compras/${ocId}/emitir`, { method: 'POST' })
}

export function cerrarOrdenCompra(proyectoId: number, ocId: number): Promise<OrdenCompra> {
  return apiRequest<OrdenCompra>(`/api/proyectos/${proyectoId}/compras/${ocId}/cerrar`, { method: 'POST' })
}

export function recepcionarOrdenCompra(
  proyectoId: number,
  ocId: number,
  request: RecepcionRequest,
): Promise<OrdenCompra> {
  return apiRequest<OrdenCompra>(`/api/proyectos/${proyectoId}/compras/${ocId}/recepciones`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function devolverProveedor(
  proyectoId: number,
  ocId: number,
  items: { ordenCompraItemId: number; cantidad: number }[],
): Promise<OrdenCompra> {
  return apiRequest<OrdenCompra>(`/api/proyectos/${proyectoId}/compras/${ocId}/devolver`, {
    method: 'POST',
    body: JSON.stringify({ items }),
  })
}

export function listarValesSalida(proyectoId: number): Promise<ValeSalida[]> {
  return apiRequest<ValeSalida[]>(`/api/proyectos/${proyectoId}/almacen/vales`)
}

export function crearValeSalida(proyectoId: number, request: ValeSalidaRequest): Promise<ValeSalida> {
  return apiRequest<ValeSalida>(`/api/proyectos/${proyectoId}/almacen/vales`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function devolverValeSalida(proyectoId: number, valeId: number): Promise<ValeSalida> {
  return apiRequest<ValeSalida>(`/api/proyectos/${proyectoId}/almacen/vales/${valeId}/devolver`, {
    method: 'POST',
    body: JSON.stringify({}),
  })
}

export function listarPersonalObra(proyectoId: number): Promise<PersonalObra[]> {
  return apiRequest<PersonalObra[]>(`/api/proyectos/${proyectoId}/personal`)
}

export function listarPeriodosCc(proyectoId: number): Promise<PeriodoCc[]> {
  return apiRequest<PeriodoCc[]>(`/api/proyectos/${proyectoId}/periodos`)
}

export function crearPeriodoCc(proyectoId: number, request: PeriodoCcRequest): Promise<PeriodoCc> {
  return apiRequest<PeriodoCc>(`/api/proyectos/${proyectoId}/periodos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function cerrarPeriodoCc(proyectoId: number, periodoId: number): Promise<PeriodoCc> {
  return apiRequest<PeriodoCc>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/cerrar`, {
    method: 'POST',
  })
}

export function obtenerTareo(proyectoId: number, periodoId: number): Promise<TareoCcGrilla> {
  return apiRequest<TareoCcGrilla>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/tareo`)
}

export function guardarTareo(
  proyectoId: number,
  periodoId: number,
  request: TareoCcGuardar,
): Promise<TareoCcGrilla> {
  return apiRequest<TareoCcGrilla>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/tareo`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function liquidarPeriodoCc(proyectoId: number, periodoId: number): Promise<BoletaCcResumen[]> {
  return apiRequest<BoletaCcResumen[]>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/liquidar`, {
    method: 'POST',
  })
}

export function listarBoletasCc(proyectoId: number, periodoId: number): Promise<BoletaCcResumen[]> {
  return apiRequest<BoletaCcResumen[]>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas`)
}

export function obtenerBoletaCc(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): Promise<BoletaCc> {
  return apiRequest<BoletaCc>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/${boletaId}`)
}

export function boletaCcDocumentoUrl(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): string {
  return `${API_BASE_URL}/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/${boletaId}/documento`
}

export function boletaCcPdfUrl(proyectoId: number, periodoId: number, boletaId: number): string {
  return `${API_BASE_URL}/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/${boletaId}/pdf`
}

export function abrirDocumentoBoletaCc(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): Promise<void> {
  return openAuthenticatedDocument(boletaCcDocumentoUrl(proyectoId, periodoId, boletaId))
}

export function abrirPdfBoletaCc(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): Promise<void> {
  return openAuthenticatedDocument(boletaCcPdfUrl(proyectoId, periodoId, boletaId))
}

function loteBody(boletaIds: number[]): string {
  return JSON.stringify({ boletaIds })
}

export function guardarExtrasBoletaCc(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
  extras: { categoria: 'HABER' | 'DESCUENTO' | 'APORTE_EMPLEADOR'; label: string; monto: number }[],
): Promise<BoletaCc> {
  return apiRequest<BoletaCc>(
    `/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/${boletaId}/extras`,
    { method: 'PUT', body: JSON.stringify({ extras }) },
  )
}

export function descargarBoletasCcPdf(
  proyectoId: number,
  periodoId: number,
  boletaIds: number[],
  filename: string,
): Promise<void> {
  return downloadAuthenticatedRequest(
    `/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/pdf?descarga=true`,
    filename,
    { method: 'POST', body: loteBody(boletaIds) },
  )
}

export function enviarBoletasCcCorreo(
  proyectoId: number,
  periodoId: number,
  boletaIds: number[],
): Promise<BoletaEnvioResumen> {
  return apiRequest<BoletaEnvioResumen>(
    `/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/correo`,
    { method: 'POST', body: loteBody(boletaIds) },
  )
}

export function descargarDispersionCc(
  proyectoId: number,
  periodoId: number,
  banco: BancoDispersion,
  filename: string,
): Promise<void> {
  return downloadAuthenticatedRequest(
    `/api/proyectos/${proyectoId}/periodos/${periodoId}/dispersion?banco=${banco}`,
    filename,
  )
}

export function abrirLoteBoletasCcPdf(
  proyectoId: number,
  periodoId: number,
  boletaIds: number[],
): Promise<void> {
  return openAuthenticatedRequest(
    `/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/pdf?descarga=false`,
    { method: 'POST', body: loteBody(boletaIds) },
  )
}

export function listarCronograma(proyectoId: number): Promise<CronogramaPartida[]> {
  return apiRequest<CronogramaPartida[]>(`/api/proyectos/${proyectoId}/cronograma`)
}

export function guardarCronograma(
  proyectoId: number,
  partidas: { partidaId: number; fechaInicio: string | null; fechaFin: string | null }[],
): Promise<CronogramaPartida[]> {
  return apiRequest<CronogramaPartida[]>(`/api/proyectos/${proyectoId}/cronograma`, {
    method: 'PUT',
    body: JSON.stringify({ partidas }),
  })
}

export function obtenerAvance(proyectoId: number, anio: number, mes: number): Promise<AvanceObra> {
  return apiRequest<AvanceObra>(`/api/proyectos/${proyectoId}/avances?anio=${anio}&mes=${mes}`)
}

export function guardarAvance(
  proyectoId: number,
  anio: number,
  mes: number,
  items: { partidaId: number; metradoPeriodo: number }[],
): Promise<AvanceObra> {
  return apiRequest<AvanceObra>(`/api/proyectos/${proyectoId}/avances`, {
    method: 'PUT',
    body: JSON.stringify({ anio, mes, items }),
  })
}

export function cerrarAvance(proyectoId: number, avanceId: number): Promise<AvanceObra> {
  return apiRequest<AvanceObra>(`/api/proyectos/${proyectoId}/avances/${avanceId}/cerrar`, {
    method: 'POST',
  })
}

export function listarAdicionalesObra(proyectoId: number): Promise<AdicionalObra[]> {
  return apiRequest<AdicionalObra[]>(`/api/proyectos/${proyectoId}/adicionales`)
}

export function crearAdicionalObra(
  proyectoId: number,
  request: AdicionalObraRequest,
): Promise<AdicionalObra> {
  return apiRequest<AdicionalObra>(`/api/proyectos/${proyectoId}/adicionales`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function guardarAdicionalObra(
  proyectoId: number,
  adicionalId: number,
  request: AdicionalObraRequest,
): Promise<AdicionalObra> {
  return apiRequest<AdicionalObra>(`/api/proyectos/${proyectoId}/adicionales/${adicionalId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function vigentarAdicionalObra(proyectoId: number, adicionalId: number): Promise<AdicionalObra> {
  return apiRequest<AdicionalObra>(`/api/proyectos/${proyectoId}/adicionales/${adicionalId}/vigentar`, {
    method: 'POST',
  })
}

export function anularAdicionalObra(proyectoId: number, adicionalId: number): Promise<AdicionalObra> {
  return apiRequest<AdicionalObra>(`/api/proyectos/${proyectoId}/adicionales/${adicionalId}/anular`, {
    method: 'POST',
  })
}

export function listarValorizaciones(proyectoId: number): Promise<Valorizacion[]> {
  return apiRequest<Valorizacion[]>(`/api/proyectos/${proyectoId}/valorizaciones`)
}

export function obtenerValorizacion(proyectoId: number, valorizacionId: number): Promise<Valorizacion> {
  return apiRequest<Valorizacion>(`/api/proyectos/${proyectoId}/valorizaciones/${valorizacionId}`)
}

export function crearValorizacion(
  proyectoId: number,
  tipo: TipoValorizacion,
  anio: number,
  mes: number,
): Promise<Valorizacion> {
  return apiRequest<Valorizacion>(`/api/proyectos/${proyectoId}/valorizaciones`, {
    method: 'POST',
    body: JSON.stringify({ tipo, anio, mes }),
  })
}

export function presentarValorizacion(proyectoId: number, valorizacionId: number): Promise<Valorizacion> {
  return apiRequest<Valorizacion>(`/api/proyectos/${proyectoId}/valorizaciones/${valorizacionId}/presentar`, {
    method: 'POST',
  })
}

export function observarValorizacion(proyectoId: number, valorizacionId: number): Promise<Valorizacion> {
  return apiRequest<Valorizacion>(`/api/proyectos/${proyectoId}/valorizaciones/${valorizacionId}/observar`, {
    method: 'POST',
  })
}

export function aprobarValorizacion(proyectoId: number, valorizacionId: number): Promise<Valorizacion> {
  return apiRequest<Valorizacion>(`/api/proyectos/${proyectoId}/valorizaciones/${valorizacionId}/aprobar`, {
    method: 'POST',
  })
}

export function facturarValorizacion(proyectoId: number, valorizacionId: number): Promise<Valorizacion> {
  return apiRequest<Valorizacion>(`/api/proyectos/${proyectoId}/valorizaciones/${valorizacionId}/facturar`, {
    method: 'POST',
  })
}

export function obtenerCurvaS(proyectoId: number): Promise<CurvaS> {
  return apiRequest<CurvaS>(`/api/proyectos/${proyectoId}/curva-s`)
}

export function listarFacturasObra(proyectoId: number): Promise<FacturaObra[]> {
  return apiRequest<FacturaObra[]>(`/api/proyectos/${proyectoId}/facturas`)
}

export function obtenerFacturaObra(proyectoId: number, facturaId: number): Promise<FacturaObra> {
  return apiRequest<FacturaObra>(`/api/proyectos/${proyectoId}/facturas/${facturaId}`)
}

export function actualizarVencimientoFactura(
  proyectoId: number,
  facturaId: number,
  fechaVencimiento: string | null,
): Promise<FacturaObra> {
  return apiRequest<FacturaObra>(`/api/proyectos/${proyectoId}/facturas/${facturaId}`, {
    method: 'PUT',
    body: JSON.stringify({ fechaVencimiento }),
  })
}

export function listarCobranzasObra(proyectoId: number): Promise<CobranzaObra[]> {
  return apiRequest<CobranzaObra[]>(`/api/proyectos/${proyectoId}/cobranzas`)
}

export function registrarCobranzaObra(
  proyectoId: number,
  request: CobranzaObraRequest,
): Promise<FacturaObra> {
  return apiRequest<FacturaObra>(`/api/proyectos/${proyectoId}/cobranzas`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerRetencionesObra(proyectoId: number): Promise<RetencionesObra> {
  return apiRequest<RetencionesObra>(`/api/proyectos/${proyectoId}/retenciones`)
}

export function liberarRetencionObra(
  proyectoId: number,
  tipo: 'EFECTIVO' | 'CARTA_FIANZA',
  fecha: string,
  importe: number,
  entidad?: string | null,
  numeroDocumento?: string | null,
  observaciones?: string | null,
): Promise<RetencionesObra> {
  return apiRequest<RetencionesObra>(`/api/proyectos/${proyectoId}/retenciones/liberar`, {
    method: 'POST',
    body: JSON.stringify({ tipo, fecha, importe, entidad, numeroDocumento, observaciones }),
  })
}

export function listarFactoring(proyectoId: number): Promise<OperacionFactoring[]> {
  return apiRequest<OperacionFactoring[]>(`/api/proyectos/${proyectoId}/factoring`)
}

export function cederFacturaFactoring(
  proyectoId: number,
  request: OperacionFactoringRequest,
): Promise<OperacionFactoring> {
  return apiRequest<OperacionFactoring>(`/api/proyectos/${proyectoId}/factoring`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function adelantarFactoring(proyectoId: number, operacionId: number): Promise<OperacionFactoring> {
  return apiRequest<OperacionFactoring>(`/api/proyectos/${proyectoId}/factoring/${operacionId}/adelantar`, {
    method: 'POST',
  })
}

export function cobrarFactoring(proyectoId: number, operacionId: number): Promise<OperacionFactoring> {
  return apiRequest<OperacionFactoring>(`/api/proyectos/${proyectoId}/factoring/${operacionId}/cobrar`, {
    method: 'POST',
  })
}

export function impagarFactoring(proyectoId: number, operacionId: number): Promise<OperacionFactoring> {
  return apiRequest<OperacionFactoring>(`/api/proyectos/${proyectoId}/factoring/${operacionId}/impagar`, {
    method: 'POST',
  })
}

export function revertirFactoring(proyectoId: number, operacionId: number): Promise<OperacionFactoring> {
  return apiRequest<OperacionFactoring>(`/api/proyectos/${proyectoId}/factoring/${operacionId}/revertir`, {
    method: 'POST',
  })
}

export function obtenerFlujoCaja(
  proyectoId: number,
  desde?: string,
  hasta?: string,
): Promise<FlujoCaja> {
  const params = new URLSearchParams()
  if (desde) params.set('desde', desde)
  if (hasta) params.set('hasta', hasta)
  const query = params.toString()
  return apiRequest<FlujoCaja>(`/api/proyectos/${proyectoId}/flujo-caja${query ? `?${query}` : ''}`)
}

export function obtenerRentabilidadObra(
  proyectoId: number,
  desde?: string,
  hasta?: string,
): Promise<RentabilidadObra> {
  const params = new URLSearchParams()
  if (desde) params.set('desde', desde)
  if (hasta) params.set('hasta', hasta)
  const query = params.toString()
  return apiRequest<RentabilidadObra>(`/api/proyectos/${proyectoId}/rentabilidad${query ? `?${query}` : ''}`)
}

export function obtenerAsientosObra(
  proyectoId: number,
  desde?: string,
  hasta?: string,
): Promise<AsientosObra> {
  const params = new URLSearchParams()
  if (desde) params.set('desde', desde)
  if (hasta) params.set('hasta', hasta)
  const query = params.toString()
  return apiRequest<AsientosObra>(`/api/proyectos/${proyectoId}/asientos${query ? `?${query}` : ''}`)
}

export function listarDocumentosObra(proyectoId: number): Promise<DocumentoObra[]> {
  return apiRequest<DocumentoObra[]>(`/api/proyectos/${proyectoId}/documentos`)
}

export async function subirDocumentoObra(
  proyectoId: number,
  tipo: TipoDocumentoObra,
  file: File,
  observaciones?: string | null,
): Promise<DocumentoObra> {
  const session = getSession()
  const headers: Record<string, string> = session?.token ? { Authorization: `Bearer ${session.token}` } : {}
  const body = new FormData()
  body.append('tipo', tipo)
  body.append('file', file)
  if (observaciones?.trim()) {
    body.append('observaciones', observaciones.trim())
  }
  const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}/api/proyectos/${proyectoId}/documentos`, {
      method: 'POST',
      headers,
      body,
    })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor.', 0)
  }
  if (response.status === 401) {
    clearSession()
    const pathname = window.location.pathname
    if (pathname !== '/login' && !pathname.startsWith('/invitar/')) {
      window.location.assign('/login')
    }
  }
  if (!response.ok) {
    let message = `Error ${response.status}`
    try {
      const payload = (await response.json()) as { message?: string }
      message = payload.message ?? message
    } catch {
      // respuesta no JSON
    }
    throw new ApiError(message, response.status)
  }
  return (await response.json()) as DocumentoObra
}

export function descargarDocumentoObra(proyectoId: number, documentoId: number, filename: string): Promise<void> {
  return downloadAuthenticatedRequest(
    `/api/proyectos/${proyectoId}/documentos/${documentoId}`,
    filename,
  )
}

export function eliminarDocumentoObra(proyectoId: number, documentoId: number): Promise<void> {
  return apiRequest<void>(`/api/proyectos/${proyectoId}/documentos/${documentoId}`, {
    method: 'DELETE',
  })
}

export function listarAlertasObra(proyectoId: number): Promise<AlertaObra[]> {
  return apiRequest<AlertaObra[]>(`/api/proyectos/${proyectoId}/alertas`)
}

export function listarAccesosObra(proyectoId: number): Promise<AccesoObra[]> {
  return apiRequest<AccesoObra[]>(`/api/proyectos/${proyectoId}/accesos`)
}

export function guardarAccesosObra(proyectoId: number, accesos: AccesoObraRequest[]): Promise<AccesoObra[]> {
  return apiRequest<AccesoObra[]>(`/api/proyectos/${proyectoId}/accesos`, {
    method: 'PUT',
    body: JSON.stringify({ accesos }),
  })
}

export function listarUsuariosAsignables(q = ''): Promise<UsuarioAsignable[]> {
  const params = q.trim() ? `?q=${encodeURIComponent(q.trim())}` : ''
  return apiRequest<UsuarioAsignable[]>(`/api/proyectos/usuarios-asignables${params}`)
}

export function listarBitacoraObra(proyectoId: number): Promise<BitacoraObra[]> {
  return apiRequest<BitacoraObra[]>(`/api/proyectos/${proyectoId}/bitacora`)
}

export function crearNotaBitacora(proyectoId: number, request: BitacoraObraRequest): Promise<BitacoraObra> {
  return apiRequest<BitacoraObra>(`/api/proyectos/${proyectoId}/bitacora`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function eliminarNotaBitacora(proyectoId: number, notaId: number): Promise<void> {
  return apiRequest<void>(`/api/proyectos/${proyectoId}/bitacora/${notaId}`, {
    method: 'DELETE',
  })
}

export function listarAuditoriaObra(proyectoId: number): Promise<AuditoriaObra[]> {
  return apiRequest<AuditoriaObra[]>(`/api/proyectos/${proyectoId}/auditoria`)
}

export function listarEquiposObra(proyectoId: number): Promise<AsignacionEquipo[]> {
  return apiRequest<AsignacionEquipo[]>(`/api/proyectos/${proyectoId}/equipos`)
}

export function crearEquipoObra(proyectoId: number, request: AsignacionEquipoRequest): Promise<AsignacionEquipo> {
  return apiRequest<AsignacionEquipo>(`/api/proyectos/${proyectoId}/equipos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function actualizarEquipoObra(
  proyectoId: number,
  equipoId: number,
  request: AsignacionEquipoRequest,
): Promise<AsignacionEquipo> {
  return apiRequest<AsignacionEquipo>(`/api/proyectos/${proyectoId}/equipos/${equipoId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function listarImputacionesEquipo(proyectoId: number, equipoId: number): Promise<ImputacionEquipo[]> {
  return apiRequest<ImputacionEquipo[]>(`/api/proyectos/${proyectoId}/equipos/${equipoId}/imputaciones`)
}

export function registrarImputacionEquipo(
  proyectoId: number,
  equipoId: number,
  request: ImputacionEquipoRequest,
): Promise<ImputacionEquipo> {
  return apiRequest<ImputacionEquipo>(`/api/proyectos/${proyectoId}/equipos/${equipoId}/imputaciones`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function eliminarImputacionEquipo(
  proyectoId: number,
  equipoId: number,
  imputacionId: number,
): Promise<void> {
  return apiRequest<void>(`/api/proyectos/${proyectoId}/equipos/${equipoId}/imputaciones/${imputacionId}`, {
    method: 'DELETE',
  })
}

export function listarSubcontratosObra(proyectoId: number): Promise<SubcontratoObra[]> {
  return apiRequest<SubcontratoObra[]>(`/api/proyectos/${proyectoId}/subcontratos`)
}

export function crearSubcontratoObra(
  proyectoId: number,
  request: SubcontratoObraRequest,
): Promise<SubcontratoObra> {
  return apiRequest<SubcontratoObra>(`/api/proyectos/${proyectoId}/subcontratos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function actualizarSubcontratoObra(
  proyectoId: number,
  subcontratoId: number,
  request: SubcontratoObraRequest,
): Promise<SubcontratoObra> {
  return apiRequest<SubcontratoObra>(`/api/proyectos/${proyectoId}/subcontratos/${subcontratoId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function listarAvancesSubcontrato(proyectoId: number, subcontratoId: number): Promise<SubcontratoAvance[]> {
  return apiRequest<SubcontratoAvance[]>(`/api/proyectos/${proyectoId}/subcontratos/${subcontratoId}/avances`)
}

export function registrarAvanceSubcontrato(
  proyectoId: number,
  subcontratoId: number,
  request: SubcontratoAvanceRequest,
): Promise<SubcontratoAvance> {
  return apiRequest<SubcontratoAvance>(`/api/proyectos/${proyectoId}/subcontratos/${subcontratoId}/avances`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function eliminarAvanceSubcontrato(
  proyectoId: number,
  subcontratoId: number,
  avanceId: number,
): Promise<void> {
  return apiRequest<void>(`/api/proyectos/${proyectoId}/subcontratos/${subcontratoId}/avances/${avanceId}`, {
    method: 'DELETE',
  })
}

export function listarCuadrillasObra(proyectoId: number): Promise<CuadrillaObra[]> {
  return apiRequest<CuadrillaObra[]>(`/api/proyectos/${proyectoId}/cuadrillas`)
}

export function crearCuadrillaObra(proyectoId: number, request: CuadrillaObraRequest): Promise<CuadrillaObra> {
  return apiRequest<CuadrillaObra>(`/api/proyectos/${proyectoId}/cuadrillas`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function actualizarCuadrillaObra(
  proyectoId: number,
  cuadrillaId: number,
  request: CuadrillaObraRequest,
): Promise<CuadrillaObra> {
  return apiRequest<CuadrillaObra>(`/api/proyectos/${proyectoId}/cuadrillas/${cuadrillaId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function eliminarCuadrillaObra(proyectoId: number, cuadrillaId: number): Promise<void> {
  return apiRequest<void>(`/api/proyectos/${proyectoId}/cuadrillas/${cuadrillaId}`, {
    method: 'DELETE',
  })
}

export function agregarMiembroCuadrilla(
  proyectoId: number,
  cuadrillaId: number,
  request: CuadrillaMiembroRequest,
): Promise<CuadrillaObra> {
  return apiRequest<CuadrillaObra>(`/api/proyectos/${proyectoId}/cuadrillas/${cuadrillaId}/miembros`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function quitarMiembroCuadrilla(
  proyectoId: number,
  cuadrillaId: number,
  personaId: number,
): Promise<void> {
  return apiRequest<void>(`/api/proyectos/${proyectoId}/cuadrillas/${cuadrillaId}/miembros/${personaId}`, {
    method: 'DELETE',
  })
}
