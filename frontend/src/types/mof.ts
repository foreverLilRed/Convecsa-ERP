export type MofPuestoResumen = {
  id: number
  codigo: string
  cargo: string
  grupo: string
}

export type MofPuesto = MofPuestoResumen & {
  proposito: string
  superior: string
  subordinados: string
  funciones: string[]
  educacion: string[]
  experiencia: string[]
  competencias: string[]
}

export type MofPuestoRequest = {
  proposito: string
  funciones: string[]
}

export const MOF_GRUPO_LABELS: Record<string, string> = {
  'STAFF DE OBRA': 'Staff de Obra y Control',
  ADMINISTRACIÓN: 'Gestión Administrativa',
  HÍBRIDOS: 'Cargos Híbridos / Formación',
  TECNICOS: 'Técnicos Especialistas',
  AUXILIARES: 'Auxiliares de Campo',
}

export const MOF_GRUPO_ORDEN = [
  'STAFF DE OBRA',
  'ADMINISTRACIÓN',
  'HÍBRIDOS',
  'TECNICOS',
  'AUXILIARES',
] as const

export function mofDocumentoUrl(id: number): string {
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  return `${base}/api/gestion-humana/mof/${id}/documento`
}

export function mofDocumentoCompletoUrl(): string {
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  return `${base}/api/gestion-humana/mof/documento`
}
