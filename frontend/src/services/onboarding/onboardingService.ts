import { apiRequest } from '@/services/http'
import type { GeneracionOnboarding, GeneracionOnboardingRequest, PlantillaOnboarding } from '@/types/onboarding'
import type { PagedResponse } from '@/types/persona'

export function listarPlantillasOnboarding(): Promise<PlantillaOnboarding[]> {
  return apiRequest<PlantillaOnboarding[]>('/api/gestion-humana/onboarding/plantillas')
}

export function crearGeneracionOnboarding(
  request: GeneracionOnboardingRequest,
): Promise<GeneracionOnboarding> {
  return apiRequest<GeneracionOnboarding>('/api/gestion-humana/onboarding/generaciones', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function listarGeneracionesOnboarding(page = 0, size = 20): Promise<PagedResponse<GeneracionOnboarding>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  return apiRequest<PagedResponse<GeneracionOnboarding>>(
    `/api/gestion-humana/onboarding/generaciones?${params}`,
  )
}
