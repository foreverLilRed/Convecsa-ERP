import {
  apiRequest,
  downloadAuthenticatedRequest,
  openAuthenticatedRequest,
} from '@/services/http'
import type {
  Afp,
  AsientoContable,
  BoletaPago,
  BoletaPagoResumen,
  BoletaUpdateRequest,
  BoletaEnvioResumen,
  NominaDocumentoConfiguracion,
  PeriodoNomina,
  PeriodoNominaRequest,
} from '@/types/nomina'
import type { PagedResponse } from '@/types/persona'

export function listarAfp(): Promise<Afp[]> {
  return apiRequest<Afp[]>('/api/gestion-humana/nominas/afp')
}

export function listarPeriodos(page = 0, size = 20): Promise<PagedResponse<PeriodoNomina>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  return apiRequest<PagedResponse<PeriodoNomina>>(`/api/gestion-humana/nominas/periodos?${params}`)
}

export function crearPeriodo(request: PeriodoNominaRequest): Promise<PeriodoNomina> {
  return apiRequest<PeriodoNomina>('/api/gestion-humana/nominas/periodos', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerPeriodo(id: number): Promise<PeriodoNomina> {
  return apiRequest<PeriodoNomina>(`/api/gestion-humana/nominas/periodos/${id}`)
}

export function generarBoletas(periodoId: number): Promise<PeriodoNomina> {
  return apiRequest<PeriodoNomina>(`/api/gestion-humana/nominas/periodos/${periodoId}/generar`, {
    method: 'POST',
  })
}

export function recalcularPeriodo(periodoId: number): Promise<PeriodoNomina> {
  return apiRequest<PeriodoNomina>(`/api/gestion-humana/nominas/periodos/${periodoId}/recalcular`, {
    method: 'POST',
  })
}

export function cerrarPeriodo(periodoId: number): Promise<PeriodoNomina> {
  return apiRequest<PeriodoNomina>(`/api/gestion-humana/nominas/periodos/${periodoId}/cerrar`, {
    method: 'POST',
  })
}

export function listarBoletas(
  periodoId: number,
  page = 0,
  size = 20,
): Promise<PagedResponse<BoletaPagoResumen>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  return apiRequest<PagedResponse<BoletaPagoResumen>>(
    `/api/gestion-humana/nominas/periodos/${periodoId}/boletas?${params}`,
  )
}

export function obtenerBoleta(id: number): Promise<BoletaPago> {
  return apiRequest<BoletaPago>(`/api/gestion-humana/nominas/boletas/${id}`)
}

export function actualizarBoleta(id: number, request: BoletaUpdateRequest): Promise<BoletaPago> {
  return apiRequest<BoletaPago>(`/api/gestion-humana/nominas/boletas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function recalcularBoleta(id: number): Promise<BoletaPago> {
  return apiRequest<BoletaPago>(`/api/gestion-humana/nominas/boletas/${id}/recalcular`, {
    method: 'POST',
  })
}

export function descargarBoletaPdf(id: number, filename: string): Promise<void> {
  return downloadAuthenticatedRequest(
    `/api/gestion-humana/nominas/boletas/${id}/pdf?descarga=true`,
    filename,
  )
}

export function imprimirBoletaPdf(id: number): Promise<void> {
  return openAuthenticatedRequest(`/api/gestion-humana/nominas/boletas/${id}/pdf`)
}

function loteBody(boletaIds: number[]): string {
  return JSON.stringify({ boletaIds })
}

export function descargarBoletasPdf(
  periodoId: number,
  boletaIds: number[],
  filename: string,
): Promise<void> {
  return downloadAuthenticatedRequest(
    `/api/gestion-humana/nominas/periodos/${periodoId}/boletas/pdf?descarga=true`,
    filename,
    { method: 'POST', body: loteBody(boletaIds) },
  )
}

export function imprimirBoletasPdf(periodoId: number, boletaIds: number[]): Promise<void> {
  return openAuthenticatedRequest(
    `/api/gestion-humana/nominas/periodos/${periodoId}/boletas/pdf?descarga=false`,
    { method: 'POST', body: loteBody(boletaIds) },
  )
}

export function enviarBoletasCorreo(
  periodoId: number,
  boletaIds: number[],
): Promise<BoletaEnvioResumen> {
  return apiRequest<BoletaEnvioResumen>(
    `/api/gestion-humana/nominas/periodos/${periodoId}/boletas/correo`,
    { method: 'POST', body: loteBody(boletaIds) },
  )
}

export function obtenerAsientoContable(periodoId: number): Promise<AsientoContable> {
  return apiRequest<AsientoContable>(
    `/api/gestion-humana/nominas/periodos/${periodoId}/asiento-contable`,
  )
}

export function descargarAsientoContablePdf(
  periodoId: number,
  filename: string,
): Promise<void> {
  return downloadAuthenticatedRequest(
    `/api/gestion-humana/nominas/periodos/${periodoId}/asiento-contable/pdf?descarga=true`,
    filename,
  )
}

export function obtenerConfiguracionDocumento(): Promise<NominaDocumentoConfiguracion> {
  return apiRequest<NominaDocumentoConfiguracion>(
    '/api/gestion-humana/nominas/documentos/configuracion',
  )
}

export function guardarConfiguracionDocumento(
  request: NominaDocumentoConfiguracion,
): Promise<NominaDocumentoConfiguracion> {
  return apiRequest<NominaDocumentoConfiguracion>(
    '/api/gestion-humana/nominas/documentos/configuracion',
    { method: 'PUT', body: JSON.stringify(request) },
  )
}
