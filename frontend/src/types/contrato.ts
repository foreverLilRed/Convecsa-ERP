export type ContratosResume = {
  conContratoActivo: number
  sinContratoActivo: number
}

export type CatalogoItem = {
  id: number
  codigo: string
  label: string
}

export type ContratosCatalogos = {
  jornadas: CatalogoItem[]
  causasObjetivas: CatalogoItem[]
  tiposPlantilla: CatalogoItem[]
  metodosPago: CatalogoItem[]
}

export type ContratoRequest = {
  personaId: number
  tipoPlantillaId: number
  causaObjetivaId: number
  cargo: string
  area: string
  proyecto: string
  ubicacionObra: string
  fechaInicio: string
  fechaFin: string
  fechaCorte: string | null
  jornadaId: number
  sueldo: number
  metodoPagoId: number
  ciudadFirma: string
  numero: string | null
  observaciones: string | null
}

export type ContratoDTO = {
  id: number
  personaId: number
  estado: string
  fechaInicio: string
  fechaFin: string
  cargo: string
  proyecto: string
  createdAt: string
  sueldo: number
}

export function contratoDocumentoUrl(id: number): string {
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  return `${base}/api/gestion-humana/contratos/${id}/documento`
}
