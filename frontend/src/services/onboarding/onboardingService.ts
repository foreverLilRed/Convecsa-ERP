import { apiRequest } from '@/services/http'
import type { PlantillaOnboarding } from '@/types/onboarding'

export function listarPlantillasOnboarding(): Promise<PlantillaOnboarding[]> {
  return apiRequest<PlantillaOnboarding[]>('/api/gestion-humana/onboarding/plantillas')
}
