import { apiRequest, downloadAuthenticatedRequest, openAuthenticatedDocument, openAuthenticatedRequest } from '@/services/http'
import type { BoletaEnvioResumen } from '@/types/nomina'
import type {
  AlmacenObra,
  BancoDispersion,
  BoletaCc,
  BoletaCcResumen,
  ComparativoPresupuesto,
  ConstruccionCatalogos,
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
  TipoVersionPresupuesto,
  ValeSalida,
  ValeSalidaRequest,
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
