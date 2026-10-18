import { apiRequest, downloadAuthenticatedFile } from '@/services/http'
import type { PagedResponse, PersonaDTO, PersonaListQuery, PersonaRequest, PersonaResumenDTO } from '@/types/persona'

export type PersonaLinkInvitadoDTO = {
  id: number
  tipo: 'EDICION' | 'ALTA'
  personaId: number | null
  apellidos: string | null
  nombres: string | null
  tipoDocumento: string | null
  numeroDocumento: string | null
  token: string
  creadoEn: string
  expiraEn: string
}

export type PersonaLinkAltaRequest = {
  apellidos: string
  nombres: string
  tipoDocumento: string
  numeroDocumento: string
}

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

export function habilitarPersona(id: number): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>(`/api/gestion-humana/personas/${id}/enable`, {
    method: 'PATCH',
  })
}

export function deshabilitarPersona(id: number): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>(`/api/gestion-humana/personas/${id}/disable`, {
    method: 'PATCH',
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

export function exportarPersonas(query: PersonaListQuery = {}): Promise<void> {
  const params = new URLSearchParams()
  if (query.sort) {
    params.set('sort', query.sort)
  }
  if (query.q?.trim()) {
    params.set('q', query.q.trim())
  }
  const search = params.toString()
  return downloadAuthenticatedFile(
    `/api/gestion-humana/personas/exportar${search ? `?${search}` : ''}`,
    'afpnet-personas.xlsx',
  )
}

export function crearLinkInvitado(personaId: number): Promise<PersonaLinkInvitadoDTO> {
  return apiRequest<PersonaLinkInvitadoDTO>(`/api/gestion-humana/personas/${personaId}/links-invitado`, {
    method: 'POST',
  })
}

export function crearLinkAlta(request: PersonaLinkAltaRequest): Promise<PersonaLinkInvitadoDTO> {
  return apiRequest<PersonaLinkInvitadoDTO>('/api/gestion-humana/personas/links-invitado/alta', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerPersonaInvitado(token: string): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>(`/api/invitado/personas/${encodeURIComponent(token)}`)
}

export function actualizarPersonaInvitado(token: string, request: PersonaRequest): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>(`/api/invitado/personas/${encodeURIComponent(token)}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}
