import { apiRequest } from '@/services/http'
import { onboardingGeneracionDocumentoUrl } from '@/types/onboarding'
import type { GeneracionOnboarding } from '@/types/onboarding'
import type { Cese, CeseRequest, LiquidacionPreview, PlantillaOffboarding } from '@/types/offboarding'
import type { PagedResponse } from '@/types/persona'

export function previewLiquidacion(personaId: number, fechaCese: string): Promise<LiquidacionPreview> {
  const params = new URLSearchParams({
    personaId: String(personaId),
    fechaCese,
  })
  return apiRequest<LiquidacionPreview>(`/api/gestion-humana/offboarding/liquidacion?${params}`)
}

export function registrarCese(request: CeseRequest): Promise<Cese> {
  return apiRequest<Cese>('/api/gestion-humana/offboarding/ceses', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function listarCeses(page = 0, size = 20): Promise<PagedResponse<Cese>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  return apiRequest<PagedResponse<Cese>>(`/api/gestion-humana/offboarding/ceses?${params}`)
}

export function listarPlantillasOffboarding(): Promise<PlantillaOffboarding[]> {
  return apiRequest<PlantillaOffboarding[]>('/api/gestion-humana/offboarding/plantillas')
}

export function crearGeneracionOffboarding(
  ceseId: number,
  codigos: string[],
): Promise<GeneracionOnboarding> {
  return apiRequest<GeneracionOnboarding>(`/api/gestion-humana/offboarding/ceses/${ceseId}/generaciones`, {
    method: 'POST',
    body: JSON.stringify({ codigos }),
  })
}

export function documentoOffboardingUrl(
  generacionId: number,
  opciones: { descarga?: boolean; imprimir?: boolean } = {},
): string {
  return onboardingGeneracionDocumentoUrl(generacionId, opciones)
}
