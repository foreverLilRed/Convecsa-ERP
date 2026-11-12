import { apiRequest } from '@/services/http'
import type { SomaEmo, SomaEmoRequest, SomaResumenObra } from '@/types/soma'

export function obtenerResumenSoma(proyectoId: number): Promise<SomaResumenObra> {
  return apiRequest<SomaResumenObra>(`/api/proyectos/${proyectoId}/soma/resumen`)
}

export function listarEmosObra(proyectoId: number): Promise<SomaEmo[]> {
  return apiRequest<SomaEmo[]>(`/api/proyectos/${proyectoId}/soma/emos`)
}

export function registrarEmoObra(proyectoId: number, request: SomaEmoRequest): Promise<SomaEmo> {
  return apiRequest<SomaEmo>(`/api/proyectos/${proyectoId}/soma/emos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
