import { apiRequest } from '@/services/http'
import type { PagedResponse, PersonaDTO, PersonaListQuery, PersonaRequest, PersonaResumenDTO } from '@/types/persona'

export function crearPersona(request: PersonaRequest): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>('/api/gestion-humana/personas', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerPersona(id: number): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>(`/api/gestion-humana/personas/${id}`)
}

export function actualizarPersona(id: number, request: PersonaRequest): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>(`/api/gestion-humana/personas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function listarPersonas(query: PersonaListQuery = {}): Promise<PagedResponse<PersonaResumenDTO>> {
  const params = new URLSearchParams()
  if (query.page !== undefined) {
    params.set('page', String(query.page))
  }
  if (query.size !== undefined) {
    params.set('size', String(query.size))
  }
  if (query.sort) {
    params.set('sort', query.sort)
  }
  if (query.q?.trim()) {
    params.set('q', query.q.trim())
  }
  if (query.conContratoActivo) {
    params.set('conContratoActivo', 'true')
  }

  const search = params.toString()
  const path = search
    ? `/api/gestion-humana/personas?${search}`
    : '/api/gestion-humana/personas'
  return apiRequest<PagedResponse<PersonaResumenDTO>>(path)
}
