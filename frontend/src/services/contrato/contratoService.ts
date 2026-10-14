import { apiRequest } from '@/services/http'
import type { PagedResponse } from '@/types/persona'
import type {
  AdendaDTO,
  AdendaRequest,
  ContratoDTO,
  ContratoRequest,
  ContratosCatalogos,
  ContratosResume,
  PlantillaContrato,
  PlantillaContratoDetalle,
  PlantillaContratoRequest,
  PlantillaContratoUpdateRequest,
} from '@/types/contrato'

export function obtenerContratosResume(): Promise<ContratosResume> {
  return apiRequest<ContratosResume>('/api/gestion-humana/contratos/resume')
}

export function obtenerContratosCatalogos(): Promise<ContratosCatalogos> {
  return apiRequest<ContratosCatalogos>('/api/gestion-humana/contratos/catalogos')
}

export function listarContratos(
  page = 0,
  size = 20,
  estado?: string,
): Promise<PagedResponse<ContratoDTO>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  if (estado) {
    params.set('estado', estado)
  }
  return apiRequest<PagedResponse<ContratoDTO>>(`/api/gestion-humana/contratos?${params}`)
}

export function obtenerUltimoContrato(personaId: number): Promise<ContratoDTO> {
  return apiRequest<ContratoDTO>(`/api/gestion-humana/contratos/personas/${personaId}/ultimo`)
}

export function listarAdendas(page = 0, size = 20): Promise<PagedResponse<AdendaDTO>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  return apiRequest<PagedResponse<AdendaDTO>>(`/api/gestion-humana/contratos/adendas?${params}`)
}

export function crearAdenda(request: AdendaRequest): Promise<AdendaDTO> {
  return apiRequest<AdendaDTO>('/api/gestion-humana/contratos/adendas', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function crearContrato(request: ContratoRequest): Promise<ContratoDTO> {
  return apiRequest<ContratoDTO>('/api/gestion-humana/contratos', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function listarPlantillasContrato(): Promise<PlantillaContrato[]> {
  return apiRequest<PlantillaContrato[]>('/api/gestion-humana/contratos/plantillas')
}

export function crearPlantillaContrato(request: PlantillaContratoRequest): Promise<PlantillaContrato> {
  return apiRequest<PlantillaContrato>('/api/gestion-humana/contratos/plantillas', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerPlantillaContrato(id: number): Promise<PlantillaContratoDetalle> {
  return apiRequest<PlantillaContratoDetalle>(`/api/gestion-humana/contratos/plantillas/${id}`)
}

export function actualizarPlantillaContrato(
  id: number,
  request: PlantillaContratoUpdateRequest,
): Promise<PlantillaContratoDetalle> {
  return apiRequest<PlantillaContratoDetalle>(`/api/gestion-humana/contratos/plantillas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}
