import { apiRequest, openAuthenticatedRequest } from '@/services/http'
import type { PagedResponse } from '@/types/persona'
import type {
  EstadoVacacion,
  TipoVacacion,
  VacacionDTO,
  VacacionRequest,
  VacacionResumen,
  VacacionSaldo,
} from '@/types/vacacion'

export function listarVacaciones(query: {
  page?: number
  size?: number
  q?: string
  tipo?: TipoVacacion | ''
  estado?: EstadoVacacion | ''
} = {}): Promise<PagedResponse<VacacionDTO>> {
  const params = new URLSearchParams({
    page: String(query.page ?? 0),
    size: String(query.size ?? 20),
  })
  if (query.q?.trim()) params.set('q', query.q.trim())
  if (query.tipo) params.set('tipo', query.tipo)
  if (query.estado) params.set('estado', query.estado)
  return apiRequest<PagedResponse<VacacionDTO>>(`/api/gestion-humana/vacaciones?${params}`)
}

export function obtenerResumenVacaciones(): Promise<VacacionResumen> {
  return apiRequest<VacacionResumen>('/api/gestion-humana/vacaciones/resumen')
}

export function obtenerSaldoVacaciones(
  personaId: number,
  periodoInicio: string,
  periodoFin: string,
  diasDerecho: number,
): Promise<VacacionSaldo> {
  const params = new URLSearchParams({ periodoInicio, periodoFin, diasDerecho: String(diasDerecho) })
  return apiRequest<VacacionSaldo>(`/api/gestion-humana/vacaciones/personas/${personaId}/saldo?${params}`)
}

export function crearVacacion(request: VacacionRequest): Promise<VacacionDTO> {
  return apiRequest<VacacionDTO>('/api/gestion-humana/vacaciones', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function aprobarVacacion(id: number): Promise<VacacionDTO> {
  return apiRequest<VacacionDTO>(`/api/gestion-humana/vacaciones/${id}/aprobar`, { method: 'PATCH' })
}

export function rechazarVacacion(id: number): Promise<VacacionDTO> {
  return apiRequest<VacacionDTO>(`/api/gestion-humana/vacaciones/${id}/rechazar`, { method: 'PATCH' })
}

export function eliminarVacacion(id: number): Promise<void> {
  return apiRequest<void>(`/api/gestion-humana/vacaciones/${id}`, { method: 'DELETE' })
}

export function abrirDocumentoVacacion(id: number): Promise<void> {
  return openAuthenticatedRequest(`/api/gestion-humana/vacaciones/${id}/documento`)
}
