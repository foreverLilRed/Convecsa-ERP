import { apiRequest } from '@/services/http'
import type { MofPuesto, MofPuestoRequest, MofPuestoResumen } from '@/types/mof'

export function listarMofPuestos(): Promise<MofPuestoResumen[]> {
  return apiRequest<MofPuestoResumen[]>('/api/gestion-humana/mof')
}

export function obtenerMofPuesto(id: number): Promise<MofPuesto> {
  return apiRequest<MofPuesto>(`/api/gestion-humana/mof/${id}`)
}

export function guardarMofPuesto(id: number, request: MofPuestoRequest): Promise<MofPuesto> {
  return apiRequest<MofPuesto>(`/api/gestion-humana/mof/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}
