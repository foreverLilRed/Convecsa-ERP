import { apiRequest } from '@/services/http'
import type { SomaResumenObra } from '@/types/soma'

export function obtenerResumenSoma(proyectoId: number): Promise<SomaResumenObra> {
  return apiRequest<SomaResumenObra>(`/api/proyectos/${proyectoId}/soma/resumen`)
}
