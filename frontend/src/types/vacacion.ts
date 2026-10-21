export type TipoVacacion = 'ORDINARIA' | 'ADELANTADA' | 'FRACCIONADA'
export type EstadoVacacion = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA'

export type VacacionRequest = {
  personaId: number
  tipo: TipoVacacion
  periodoInicio: string
  periodoFin: string
  diasDerechoPeriodo: number
  diasSolicitados: number
  fechaInicio: string
  observacion: string | null
}

export type VacacionDTO = {
  id: number
  personaId: number
  personaNombreCompleto: string
  personaNumeroDocumento: string
  cargo: string | null
  contratoId: number
  fechaIngreso: string | null
  tipo: TipoVacacion
  estado: EstadoVacacion
  gozada: boolean
  periodoInicio: string
  periodoFin: string
  diasDerechoPeriodo: number
  diasSolicitados: number
  diasUsadosPeriodo: number
  diasDisponiblesPeriodo: number
  fechaInicio: string
  fechaFin: string
  fechaRetorno: string
  observacion: string | null
  aprobadaEn: string | null
  createdAt: string
}

export type VacacionResumen = {
  totalSolicitudes: number
  pendientes: number
  aprobadas: number
  gozadas: number
  conveniosAdelanto: number
  diasAprobados: number
}

export type VacacionSaldo = {
  personaId: number
  periodoInicio: string
  periodoFin: string
  diasDerecho: number
  diasAprobados: number
  diasPendientes: number
  diasDisponibles: number
}

export const TIPOS_VACACION: Array<{ value: TipoVacacion; label: string }> = [
  { value: 'ORDINARIA', label: 'Ordinarias (récord cumplido)' },
  { value: 'ADELANTADA', label: 'Adelantadas (D. L. 1405)' },
  { value: 'FRACCIONADA', label: 'Fraccionadas' },
]
