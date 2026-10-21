export type EstadoPeriodoNomina = 'BORRADOR' | 'CERRADO'

export type EstadoBoletaPago = 'BORRADOR' | 'APROBADA'

export type CategoriaConcepto = 'HABER' | 'DESCUENTO' | 'APORTE_EMPLEADOR'

export type CodigoConcepto =
  | 'SUELDO_BASICO'
  | 'ASIG_FAMILIAR'
  | 'APORTE_OBLIG'
  | 'COMISION_AFP'
  | 'PRIMA_SEGURO'
  | 'APORTE_ONP'
  | 'ESSALUD'
  | 'SCTR_SALUD'
  | 'SCTR_PENSION'
  | 'VIDA_LEY'
  | 'ESSALUD_VIDA'
  | 'EXTRA'

export type OrigenConcepto = 'CALCULADO' | 'MANUAL' | 'TAREO'

export type Afp = {
  id: number
  codigo: string
  label: string
  aporteObligatorio: number
  comisionFlujo: number
  primaSeguro: number
  esOnp: boolean
}

export type PeriodoNomina = {
  id: number
  anio: number
  mes: number
  estado: EstadoPeriodoNomina
  observaciones: string | null
  createdAt: string
  closedAt: string | null
  cantidadBoletas: number
  totalNeto: number
}

export type PeriodoNominaRequest = {
  anio: number
  mes: number
  observaciones?: string
}

export type BoletaConcepto = {
  id: number
  categoria: CategoriaConcepto
  codigo: CodigoConcepto
  label: string
  monto: number
  orden: number
  origen: OrigenConcepto
}

export type BoletaPagoResumen = {
  id: number
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  cargo: string | null
  afpNombre: string | null
  diasTrabajados: number
  totalHaberes: number
  totalDescuentos: number
  neto: number
  estado: EstadoBoletaPago
}

export type BoletaPago = {
  id: number
  periodoId: number
  periodoAnio: number
  periodoMes: number
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  cargo: string | null
  banco: string | null
  cci: string | null
  afpId: number | null
  afpCodigo: string | null
  afpNombre: string | null
  diasTrabajados: number
  aplicaAsigFamiliar: boolean
  totalHaberes: number
  totalDescuentos: number
  totalAportesEmpleador: number
  neto: number
  estado: EstadoBoletaPago
  conceptos: BoletaConcepto[]
  createdAt: string
  updatedAt: string
}

export type BoletaConceptoExtraRequest = {
  categoria: CategoriaConcepto
  label: string
  monto: number
}

export type BoletaUpdateRequest = {
  diasTrabajados: number
  aplicaAsigFamiliar: boolean
  afpId: number
  extras: BoletaConceptoExtraRequest[]
}

export type BoletaEnvioResumen = {
  enviados: number
  errores: number
  detalles: string[]
}

export type NominaDocumentoConfiguracion = {
  razonSocial: string
  ruc: string
  domicilio: string
  representante: string | null
  logoDataUrl: string | null
  firmaDataUrl: string | null
}

export type AsientoContableLinea = {
  cuenta: string
  descripcion: string
  destino: string | null
  debe: number
  haber: number
}

export type AsientoContableBloque = {
  codigo: string
  titulo: string
  lineas: AsientoContableLinea[]
  totalDebe: number
  totalHaber: number
}

export type AsientoContable = {
  periodoId: number
  anio: number
  mes: number
  estadoPeriodo: EstadoPeriodoNomina
  preliminar: boolean
  generadoAt: string
  cantidadTrabajadores: number
  totalHaberes: number
  totalDescuentos: number
  totalAportesEmpleador: number
  totalNeto: number
  asientos: AsientoContableBloque[]
  totalDebe: number
  totalHaber: number
  diferencia: number
}

const MESES = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Septiembre',
  'Octubre',
  'Noviembre',
  'Diciembre',
] as const

export function periodoLabel(anio: number, mes: number): string {
  return `${MESES[mes - 1] ?? mes} ${anio}`
}

export function mesesCatalogo(): { value: number; label: string }[] {
  return MESES.map((label, index) => ({ value: index + 1, label }))
}

const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export function boletaDocumentoUrl(
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
  const path = `${API_BASE}/api/gestion-humana/nominas/boletas/${id}/documento`
  return query ? `${path}?${query}` : path
}

export function boletaPdfUrl(id: number, descarga = false): string {
  const params = descarga ? '?descarga=true' : ''
  return `${API_BASE}/api/gestion-humana/nominas/boletas/${id}/pdf${params}`
}
