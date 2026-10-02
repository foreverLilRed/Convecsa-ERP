import { apiRequest } from '@/services/http'
import type { ContratoDTO, ContratoRequest, ContratosCatalogos, ContratosResume } from '@/types/contrato'

export function obtenerContratosResume(): Promise<ContratosResume> {
  return apiRequest<ContratosResume>('/api/gestion-humana/contratos/resume')
}

export function obtenerContratosCatalogos(): Promise<ContratosCatalogos> {
  return apiRequest<ContratosCatalogos>('/api/gestion-humana/contratos/catalogos')
}

export function crearContrato(request: ContratoRequest): Promise<ContratoDTO> {
  return apiRequest<ContratoDTO>('/api/gestion-humana/contratos', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
