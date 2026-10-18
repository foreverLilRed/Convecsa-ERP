export type DashboardPersonas = {
  total: number
  activos: number
  inactivos: number
  nuevosEsteMes: number
  examenesMedicosPorVencer: number
}

export type DashboardContratos = {
  total: number
  activos: number
  inactivos: number
  vigentesHoy: number
  porVencer30Dias: number
  sinFechaFin: number
  sueldosContratadosMes: number
}

export type DashboardNomina = {
  anio: number
  mes: number
  periodoId: number | null
  estado: 'BORRADOR' | 'CERRADO' | null
  periodoGenerado: boolean
  montoEstimado: boolean
  cantidadBoletas: number
  totalHaberes: number
  totalDescuentos: number
  aportesEmpleador: number
  montoPagar: number
}

export type DashboardAsistencia = {
  anio: number
  mes: number
  registros: number
  personasConRegistro: number
  inasistencias: number
  descansosMedicos: number
  minutosTardanza: number
  minutosExtras25: number
  minutosExtras35: number
  horasTrabajadas: number
}
