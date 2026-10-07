export type EstadoAsistencia = 'ASISTIO' | 'FALTA' | 'PERMISO' | 'DESCANSO'

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
  observacion?: string | null
}

export const DIAS_SEMANA = [
  { value: 1, label: 'Lunes' },
  { value: 2, label: 'Martes' },
  { value: 3, label: 'Miércoles' },
  { value: 4, label: 'Jueves' },
  { value: 5, label: 'Viernes' },
  { value: 6, label: 'Sábado' },
  { value: 7, label: 'Domingo' },
] as const

export const ESTADOS_ASISTENCIA: { value: EstadoAsistencia; label: string }[] = [
  { value: 'ASISTIO', label: 'Asistió' },
  { value: 'FALTA', label: 'Falta' },
  { value: 'PERMISO', label: 'Permiso' },
  { value: 'DESCANSO', label: 'Descanso' },
]

export function diaMes(fecha: string): string {
  return fecha.slice(8, 10)
}
