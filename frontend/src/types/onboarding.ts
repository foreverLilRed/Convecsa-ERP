export type AmbitoPlantilla = 'ONBOARDING' | 'OFFBOARDING' | 'AMBOS'

export type PlantillaOnboarding = {
  id: number
  codigo: string
  categoria: string
  titulo: string
  descripcion: string
  baseLegal: string
  ambito?: AmbitoPlantilla
}

export type GeneracionOnboarding = {
  id: number
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  codigos: string[]
  createdAt: string
}

export type GeneracionOnboardingRequest = {
  personaId: number
  codigos: string[]
}

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export function onboardingDocumentoUrl(personaId: number, codigos: string[]): string {
  const params = new URLSearchParams({
    personaId: String(personaId),
    codigos: codigos.join(','),
  })
  return `${API_BASE}/api/gestion-humana/onboarding/documento?${params}`
}

export function onboardingGeneracionDocumentoUrl(
  id: number,
  opciones: { descarga?: boolean; imprimir?: boolean } = {},
): string {
  const params = new URLSearchParams()
  if (opciones.descarga) {
    params.set('descarga', 'true')
  }
  if (opciones.imprimir) {
    params.set('imprimir', 'true')
  }
  const query = params.toString()
  const path = `${API_BASE}/api/gestion-humana/onboarding/generaciones/${id}/documento`
  return query ? `${path}?${query}` : path
}
