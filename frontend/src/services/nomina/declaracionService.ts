import { ApiError, apiRequest, downloadAuthenticatedRequest, openAuthenticatedRequest } from '@/services/http'
import { clearSession, getSession } from '@/services/auth/session'
import type {
  DeclaracionDetalle,
  DeclaracionMensual,
  DeclaracionMensualRequest,
  ReciboHonorario,
  RxhImportacion,
} from '@/types/declaracion'
import type { AsientoContable } from '@/types/nomina'
import type { PagedResponse } from '@/types/persona'

const BASE = '/api/gestion-humana/nominas/declaraciones'

export function listarDeclaraciones(page = 0, size = 20): Promise<PagedResponse<DeclaracionMensual>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  return apiRequest<PagedResponse<DeclaracionMensual>>(`${BASE}?${params}`)
}

export function crearDeclaracion(request: DeclaracionMensualRequest): Promise<DeclaracionMensual> {
  return apiRequest<DeclaracionMensual>(BASE, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerDeclaracion(id: number): Promise<DeclaracionDetalle> {
  return apiRequest<DeclaracionDetalle>(`${BASE}/${id}`)
}

export function consolidarRcc(id: number): Promise<DeclaracionDetalle> {
  return apiRequest<DeclaracionDetalle>(`${BASE}/${id}/consolidar`, { method: 'POST' })
}

export function cerrarDeclaracion(id: number): Promise<DeclaracionMensual> {
  return apiRequest<DeclaracionMensual>(`${BASE}/${id}/cerrar`, { method: 'POST' })
}

export function asignarRxh(
  id: number,
  reciboId: number,
  numeroDocumento: string,
): Promise<ReciboHonorario> {
  return apiRequest<ReciboHonorario>(`${BASE}/${id}/rxh/${reciboId}`, {
    method: 'PUT',
    body: JSON.stringify({ numeroDocumento }),
  })
}

export async function importarRxh(id: number, file: File): Promise<RxhImportacion> {
  const session = getSession()
  const headers: Record<string, string> = session?.token ? { Authorization: `Bearer ${session.token}` } : {}
  const body = new FormData()
  body.append('file', file)
  const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${BASE}/${id}/rxh`, {
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
  return (await response.json()) as RxhImportacion
}

export function descargarPlame(id: number, filename: string): Promise<void> {
  return downloadAuthenticatedRequest(`${BASE}/${id}/plame`, filename)
}

export function descargarAfpnet(id: number, filename: string): Promise<void> {
  return downloadAuthenticatedRequest(`${BASE}/${id}/afpnet`, filename)
}

export function descargarDispersion(id: number, filename: string): Promise<void> {
  return downloadAuthenticatedRequest(`${BASE}/${id}/dispersion`, filename)
}

export function descargarAsientoPdf(id: number, filename: string): Promise<void> {
  return downloadAuthenticatedRequest(`${BASE}/${id}/asiento-contable/pdf`, filename)
}

export function abrirAsientoPdf(id: number): Promise<void> {
  return openAuthenticatedRequest(`${BASE}/${id}/asiento-contable/pdf`)
}

export function obtenerAsiento(id: number): Promise<AsientoContable> {
  return apiRequest<AsientoContable>(`${BASE}/${id}/asiento-contable`)
}
