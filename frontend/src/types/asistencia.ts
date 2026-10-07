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
