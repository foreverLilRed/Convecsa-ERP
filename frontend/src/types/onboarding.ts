export type PlantillaOnboarding = {
  id: number
  codigo: string
  categoria: string
  titulo: string
  descripcion: string
  baseLegal: string
}

export function onboardingDocumentoUrl(personaId: number, codigos: string[]): string {
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  const params = new URLSearchParams({
    personaId: String(personaId),
    codigos: codigos.join(','),
  })
  return `${base}/api/gestion-humana/onboarding/documento?${params}`
}
