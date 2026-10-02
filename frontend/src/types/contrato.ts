export type ContratosResume = {
  conContratoActivo: number
  sinContratoActivo: number
}

export type CatalogoItem = {
  id: number
  codigo: string
  label: string
}

export type PlantillaCampo = {
  clave: string
  label: string
  tipo: 'text' | 'date' | 'number' | 'catalogo' | string
  catalogo?: string | null
  requerido: boolean
}

export type PlantillaContrato = {
  id: number
  codigo: string
  nombre: string
  metadata: PlantillaCampo[]
}

export type ContratosCatalogos = {
  jornadas: CatalogoItem[]
  causasObjetivas: CatalogoItem[]
  metodosPago: CatalogoItem[]
  plantillas: PlantillaContrato[]
}

export type ContratoRequest = {
  personaId: number
  plantillaContratoId: number
  fechaInicio: string
  fechaFin: string
  sueldo: number
  metadata: Record<string, string>
}

export type ContratoDTO = {
  id: number
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  estado: string
  fechaInicio: string | null
  fechaFin: string | null
  cargo: string
  proyecto: string
  createdAt: string
  sueldo: number
}

export function contratoDocumentoUrl(id: number): string {
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  return `${base}/api/gestion-humana/contratos/${id}/documento`
}
