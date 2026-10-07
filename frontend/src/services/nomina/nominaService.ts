import { apiRequest } from '@/services/http'
import type {
  Afp,
  BoletaPago,
  BoletaPagoResumen,
  BoletaUpdateRequest,
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
