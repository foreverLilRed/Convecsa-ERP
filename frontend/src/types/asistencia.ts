export type EstadoAsistencia = 'ASISTIO' | 'FALTA' | 'PERMISO' | 'DESCANSO'

export type CodigoTareo =
  | 'A'
  | 'D'
  | 'F'
  | 'TF'
  | 'TD'
  | 'DM'
  | 'SIT'
  | 'MAT_PRE'
  | 'MAT_POST'
  | 'VAC'
  | 'VAC_COMP'
  | 'LCG'
  | 'LSG'
  | 'PAT'
  | 'FI'

export type TipoConceptoVariableTareo =
  | 'COMISION'
  | 'TRABAJO_NOCTURNO'
  | 'ASIGNACION_ALIMENTACION'
  | 'ADELANTO_QUINCENA'
  | 'CUOTA_SINDICAL'
  | 'DESCUENTO_JUDICIAL'
  | 'OTRO_INGRESO'
  | 'OTRO_DESCUENTO'

export type AsistenciaConfig = {
  diasSemana: number[]
}

export type AsistenciaFeriado = {
  id: number
  fecha: string
  nombre: string
  laborable: boolean
}

export type AsistenciaFeriadoRequest = {
  fecha: string
  nombre: string
  laborable?: boolean
}

export type AsistenciaRegistro = {
  id: number
  fecha: string
  horasEsperadas: number
  horasTrabajadas: number
  estado: EstadoAsistencia
  codigo: CodigoTareo
  minutosExtras25: number
  minutosExtras35: number
  minutosTardanza: number
  observacion: string | null
}

export type AsistenciaFila = {
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  contratoId: number
  jornadaCodigo: string | null
  jornadaLabel: string | null
  horasEsperadasDia: number
  cargo: string | null
  fechaIngreso: string | null
  fechaCese: string | null
  registros: AsistenciaRegistro[]
}

export type AsistenciaPlanilla = {
  anio: number
  mes: number
  config: AsistenciaConfig
  feriados: AsistenciaFeriado[]
  diasLaborables: string[]
  generado: boolean
  filas: AsistenciaFila[]
}

export type AsistenciaRegistroUpdate = {
  id: number
  horasTrabajadas: number
  estado: EstadoAsistencia
  codigo: CodigoTareo
  minutosExtras25: number
  minutosExtras35: number
  minutosTardanza: number
  observacion?: string | null
}

export type AsistenciaConceptoVariable = {
  id: number
  personaId: number
  contratoId: number
  tipo: TipoConceptoVariableTareo
  monto: number
  detalle: string | null
}

export type AsistenciaConceptoVariableRequest = Omit<AsistenciaConceptoVariable, 'id' | 'detalle'> & {
  detalle?: string | null
}

export const DIAS_SEMANA = [
  { value: 1, label: 'Lunes', corto: 'Lu' },
  { value: 2, label: 'Martes', corto: 'Ma' },
  { value: 3, label: 'Miércoles', corto: 'Mi' },
  { value: 4, label: 'Jueves', corto: 'Ju' },
  { value: 5, label: 'Viernes', corto: 'Vi' },
  { value: 6, label: 'Sábado', corto: 'Sá' },
  { value: 7, label: 'Domingo', corto: 'Do' },
] as const

export const ESTADOS_ASISTENCIA: { value: EstadoAsistencia; label: string }[] = [
  { value: 'ASISTIO', label: 'Asistió' },
  { value: 'FALTA', label: 'Falta' },
  { value: 'PERMISO', label: 'Permiso' },
  { value: 'DESCANSO', label: 'Descanso' },
]

export const CODIGOS_TAREO: { value: CodigoTareo; label: string }[] = [
  { value: 'A', label: 'A · Asistencia' },
  { value: 'D', label: 'D · Descanso semanal' },
  { value: 'F', label: 'F · Feriado no laborable' },
  { value: 'TF', label: 'TF · Trabajo en feriado' },
  { value: 'TD', label: 'TD · Trabajo en descanso' },
  { value: 'DM', label: 'DM · Descanso médico' },
  { value: 'SIT', label: 'SIT · Subsidio incapacidad temporal' },
  { value: 'MAT_PRE', label: 'Pre natal' },
  { value: 'MAT_POST', label: 'Post natal' },
  { value: 'VAC', label: 'Vacaciones gozadas' },
  { value: 'VAC_COMP', label: 'Vacaciones compensadas' },
  { value: 'LCG', label: 'Licencia con goce' },
  { value: 'LSG', label: 'Licencia sin goce' },
  { value: 'PAT', label: 'Licencia por paternidad' },
  { value: 'FI', label: 'Inasistencia injustificada' },
]

export function diaMes(fecha: string): string {
  return fecha.slice(8, 10)
}

export function isoDiaSemana(fecha: string): number {
  const js = new Date(`${fecha}T12:00:00`).getDay()
  return js === 0 ? 7 : js
}

export function diaCorto(fecha: string): string {
  return DIAS_SEMANA[isoDiaSemana(fecha) - 1]?.corto ?? ''
}

export function fechaLarga(fecha: string): string {
  const parsed = new Date(`${fecha}T12:00:00`)
  if (Number.isNaN(parsed.getTime())) {
    return fecha
  }
  return parsed.toLocaleDateString('es-PE', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  })
}
