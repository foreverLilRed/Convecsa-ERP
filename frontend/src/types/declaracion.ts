import type { CategoriaConcepto } from '@/types/nomina'

export type EstadoDeclaracion = 'BORRADOR' | 'CERRADO'

export type DeclaracionMensual = {
  id: number
  anio: number
  mes: number
  estado: EstadoDeclaracion
  createdAt: string
  closedAt: string | null
  cantidadAdmin: number
  cantidadObreros: number
  cantidadRxh: number
  cantidadRxhSinMatch: number
  totalNetoRcc: number
}

export type DeclaracionMensualRequest = {
  anio: number
  mes: number
}

export type ConsolidadoCcConcepto = {
  id: number
  categoria: CategoriaConcepto
  codigo: string
  label: string
  monto: number
  orden: number
}

export type ConsolidadoCc = {
  id: number
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  proyectoId: number | null
  proyectoCodigo: string | null
  proyectoNombre: string | null
  totalHaberes: number
  totalDescuentos: number
  totalAportesEmpleador: number
  totalAfecto: number
  neto: number
  horasOrd: number
  he60: number
  he100: number
  diasLab: number
  diasFalta: number
  diasDm: number
  diasFe: number
  diasLic: number
  conceptos: ConsolidadoCcConcepto[]
}

export type ReciboHonorario = {
  id: number
  personaId: number | null
  personaNombres: string | null
  tipoDocumento: string
  numeroDocumento: string
  apellidoPaterno: string | null
  apellidoMaterno: string | null
  nombres: string | null
  domiciliado: boolean
  tipoComprobante: string
  serie: string
  numero: string
  monto: number
  fechaEmision: string | null
  fechaPago: string | null
  retencionCuarta: boolean
  retencionPension: boolean
  importeAportePension: number
  huerfano: boolean
}

export type DeclaracionDetalle = {
  declaracion: DeclaracionMensual
  consolidado: ConsolidadoCc[]
  recibos: ReciboHonorario[]
}

export type RxhImportacion = {
  importados: number
  conMatch: number
  huerfanos: number
}
