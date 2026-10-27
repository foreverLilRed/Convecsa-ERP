import { apiRequest } from '@/services/http'
import type { PagedResponse } from '@/types/persona'
import type {
  EstadoObra,
  ObraDTO,
  ObraEstadoRequest,
  ObraRequest,
  ObraResumen,
} from '@/types/obra'

export function listarObras(query: {
  page?: number
  size?: number
  q?: string
  estado?: EstadoObra | ''
} = {}): Promise<PagedResponse<ObraDTO>> {
  const params = new URLSearchParams({
    page: String(query.page ?? 0),
    size: String(query.size ?? 20),
  })
  if (query.q?.trim()) params.set('q', query.q.trim())
  if (query.estado) params.set('estado', query.estado)
  return apiRequest<PagedResponse<ObraDTO>>(`/api/proyectos/obras?${params}`)
}

export function obtenerResumenObras(): Promise<ObraResumen> {
  return apiRequest<ObraResumen>('/api/proyectos/obras/resumen')
}

export function obtenerObra(id: number): Promise<ObraDTO> {
  return apiRequest<ObraDTO>(`/api/proyectos/obras/${id}`)
}

export function crearObra(request: ObraRequest): Promise<ObraDTO> {
  return apiRequest<ObraDTO>('/api/proyectos/obras', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function actualizarObra(id: number, request: ObraRequest): Promise<ObraDTO> {
  return apiRequest<ObraDTO>(`/api/proyectos/obras/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function cambiarEstadoObra(id: number, request: ObraEstadoRequest): Promise<ObraDTO> {
  return apiRequest<ObraDTO>(`/api/proyectos/obras/${id}/estado`, {
    method: 'PATCH',
    body: JSON.stringify(request),
  })
}

export function eliminarObra(id: number): Promise<void> {
  return apiRequest<void>(`/api/proyectos/obras/${id}`, { method: 'DELETE' })
}
