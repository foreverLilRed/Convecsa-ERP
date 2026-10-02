import { apiRequest } from '@/services/http'
import type { ContratosResume } from '@/types/contrato'

export function obtenerContratosResume(): Promise<ContratosResume> {
  return apiRequest<ContratosResume>('/api/gestion-humana/contratos/resume')
}
