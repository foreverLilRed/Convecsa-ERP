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

export type UsoPlantilla = 'CONTRATO' | 'ADENDA'

export type PlantillaContrato = {
  id: number
  codigo: string
  nombre: string
  uso: UsoPlantilla
  metadata: PlantillaCampo[]
}

export type PlantillaContratoRequest = {
  codigo: string
  nombre: string
  plantilla: string
  uso: UsoPlantilla
}

export type PlantillaContratoDetalle = PlantillaContrato & {
  plantilla: string
}

export type PlantillaContratoUpdateRequest = {
  nombre: string
  plantilla: string
  uso: UsoPlantilla
  pin: string
}

export type ModalidadContrato = CatalogoItem & {
  regimenLaboralId: number
  requiereFechaFin: boolean
}

export type ContratosCatalogos = {
  regimenesLaborales: CatalogoItem[]
  modalidadesContrato: ModalidadContrato[]
  jornadas: CatalogoItem[]
  causasObjetivas: CatalogoItem[]
  metodosPago: CatalogoItem[]
  plantillas: PlantillaContrato[]
  afps: CatalogoItem[]
}

export type ContratoRequest = {
  titulo: string
  categoriaContratoId: number
  personaId: number
  fechaInicio?: string | null
  fechaFin?: string | null
  sueldo: number
  activo?: boolean
  fechaCorte?: string | null
  tipoPlantillaId: number
  observaciones?: string | null
  metadata: Record<string, string>
  categoriaObrera?: string | null
  especialidadBae?: string | null
  afiliadoSindical?: boolean
  cuotaSindical?: number | null
  hijosEscolaridad?: number | null
  proyectoId?: number | null
  centroCosto?: string | null
}

export type ContratoDTO = {
  id: number
  titulo: string
  categoriaContratoId: number
  categoriaContratoCodigo: string
  categoriaContratoNombre: string
  regimenLaboralId: number | null
  regimenLaboralCodigo: string | null
  regimenLaboralNombre: string | null
  personaId: number
  personaNombreCompleto: string
  personaNumeroDocumento: string
  fechaInicio: string | null
  fechaFin: string | null
  sueldo: number
  activo: boolean
  fechaCorte: string | null
  tipoPlantillaId: number
  tipoPlantillaCodigo: string
  tipoPlantillaNombre: string
  observaciones: string | null
  metadata: Record<string, string>
  categoriaObrera?: string | null
  especialidadBae?: string | null
  afiliadoSindical?: boolean | null
  cuotaSindical?: number | null
  hijosEscolaridad?: number | null
  proyectoId?: number | null
  proyectoCodigo?: string | null
  proyectoNombre?: string | null
  centroCosto?: string | null
}

export type AdendaDTO = {
  id: number
  numero: number
  contratoId: number
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  contratoFechaInicio: string | null
  fechaFinAnterior: string
  fechaFinNueva: string
  sueldo: number
  motivo: string
  ciudadEmision: string | null
  fechaEmision: string
  plantillaCodigo: string | null
  plantillaNombre: string | null
  createdAt: string
}

export type AdendaRequest = {
  contratoId: number
  plantillaContratoId: number
  fechaFinNueva: string
  sueldo: number
  motivo: string
  ciudadEmision?: string | null
  fechaEmision: string
  metadata: Record<string, string>
}

export const MOTIVOS_ADENDA = [
  'necesidades operativas del proyecto y acuerdo mutuo de las partes',
  'ampliación del plazo de ejecución de la obra principal',
  'requerimiento de continuidad laboral por avance físico de la partida',
  'reestructuración del cronograma contractual de obra',
] as const

export function contratoDocumentoUrl(id: number): string {
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  return `${base}/api/gestion-humana/contratos/${id}/documento`
}

export function adendaDocumentoUrl(id: number): string {
  const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
  return `${base}/api/gestion-humana/contratos/adendas/${id}/documento`
}

export function duracionAdendaLabel(inicio: string, fin: string): string {
  if (!inicio || !fin || fin <= inicio) {
    return ''
  }
  const d1 = new Date(`${inicio}T12:00:00`)
  const d2 = new Date(`${fin}T12:00:00`)
  let months = (d2.getFullYear() - d1.getFullYear()) * 12 + (d2.getMonth() - d1.getMonth())
  const years = Math.floor(months / 12)
  const rest = months % 12
  let label = ''
  if (years) {
    label += `${years} año${years > 1 ? 's' : ''}`
  }
  if (years && rest) {
    label += ' y '
  }
  if (rest) {
    label += `${rest} mes${rest > 1 ? 'es' : ''}`
  }
  if (!label) {
    const days = Math.round((d2.getTime() - d1.getTime()) / 86_400_000)
    label = `${days} día${days !== 1 ? 's' : ''}`
  }
  return label
}
