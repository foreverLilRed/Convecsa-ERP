import { apiRequest } from '@/services/http'
import type { PagedResponse } from '@/types/persona'
import type { ContratoDTO, ContratoRequest, ContratosCatalogos, ContratosResume } from '@/types/contrato'

export function obtenerContratosResume(): Promise<ContratosResume> {
  return apiRequest<ContratosResume>('/api/gestion-humana/contratos/resume')
}

export function obtenerContratosCatalogos(): Promise<ContratosCatalogos> {
  return apiRequest<ContratosCatalogos>('/api/gestion-humana/contratos/catalogos')
}

export function listarContratos(page = 0, size = 20): Promise<PagedResponse<ContratoDTO>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  return apiRequest<PagedResponse<ContratoDTO>>(`/api/gestion-humana/contratos?${params}`)
}

export function crearContrato(request: ContratoRequest): Promise<ContratoDTO> {
  return apiRequest<ContratoDTO>('/api/gestion-humana/contratos', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
