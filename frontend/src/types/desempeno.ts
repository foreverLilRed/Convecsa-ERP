export type EstadoEvaluacionDesempeno = 'PENDIENTE' | 'COMPLETADA'

export type ColaboradorDesempeno = {
  personaId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  fotoUrl: string | null
  cargo: string | null
  proyecto: string | null
  evaluacionId: number | null
  promedio: number | null
  estado: EstadoEvaluacionDesempeno | null
  metaCumplida: number | null
}

export type EvaluacionDesempeno = {
  id: number | null
  personaId: number
  periodo: string
  seguridadSst: number
  productividad: number
  calidadTecnica: number
  puntualidad: number
  trabajoEquipo: number
  metaCumplida: number
  comentarios: string
  estado: EstadoEvaluacionDesempeno
  promedio: number
}

export type ColaboradorDesempenoDetalle = {
  personaId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  fotoUrl: string | null
  cargo: string | null
  proyecto: string | null
  evaluacion: EvaluacionDesempeno
}

export type EvaluacionDesempenoRequest = {
  periodo: string
  seguridadSst: number
  productividad: number
  calidadTecnica: number
  puntualidad: number
  trabajoEquipo: number
  metaCumplida: number
  comentarios: string
  estado: EstadoEvaluacionDesempeno
}

export type DesempenoListQuery = {
  periodo: string
  page?: number
  size?: number
  q?: string
}

export type CompetenciaCampo =
  | 'seguridadSst'
  | 'productividad'
  | 'calidadTecnica'
  | 'puntualidad'
  | 'trabajoEquipo'

export const COMPETENCIAS: { campo: CompetenciaCampo; label: string; desc: string }[] = [
  {
    campo: 'seguridadSst',
    label: 'Seguridad y cumplimiento SSOMA',
    desc: 'Uso correcto de EPP, reporte de actos inseguros y respeto a protocolos.',
  },
  {
    campo: 'productividad',
    label: 'Productividad y ritmo de trabajo',
    desc: 'Avance dentro de los tiempos estipulados y efectividad en la jornada.',
  },
  {
    campo: 'calidadTecnica',
    label: 'Calidad técnica y acabados',
    desc: 'Cuidado de detalles, cero retrabajos y cumplimiento de especificaciones.',
  },
  {
    campo: 'puntualidad',
    label: 'Puntualidad y asistencia',
    desc: 'Llegada a tiempo y presencia activa en charlas de cinco minutos.',
  },
  {
    campo: 'trabajoEquipo',
    label: 'Trabajo en equipo y cooperación',
    desc: 'Actitud positiva, compañerismo y respeto en cuadrilla.',
  },
]

export const TRIMESTRES = [
  { value: 1, label: 'T1' },
  { value: 2, label: 'T2' },
  { value: 3, label: 'T3' },
  { value: 4, label: 'T4' },
] as const

export function periodoActual(): string {
  const hoy = new Date()
  const trimestre = Math.floor(hoy.getMonth() / 3) + 1
  return `${hoy.getFullYear()}-Q${trimestre}`
}

export function parsePeriodo(periodo: string): { anio: number; trimestre: number } {
  const match = /^(\d{4})-Q([1-4])$/.exec(periodo)
  if (!match) {
    const actual = parsePeriodo(periodoActual())
    return actual
  }
  return { anio: Number(match[1]), trimestre: Number(match[2]) }
}

export function formatPeriodo(anio: number, trimestre: number): string {
  return `${anio}-Q${trimestre}`
}

export function periodoLabel(periodo: string): string {
  const { anio, trimestre } = parsePeriodo(periodo)
  return `${anio} · T${trimestre}`
}

export function estadoEvaluacionLabel(estado: EstadoEvaluacionDesempeno | null): string {
  if (estado === 'COMPLETADA') {
    return 'Completada'
  }
  if (estado === 'PENDIENTE') {
    return 'Pendiente'
  }
  return 'Sin evaluar'
}

export function iniciales(nombres: string, apellidos: string): string {
  const a = apellidos.trim().charAt(0)
  const n = nombres.trim().charAt(0)
  return `${a}${n}`.toUpperCase() || '?'
}

export function promedioCompetencias(evalucion: Pick<
  EvaluacionDesempeno,
  'seguridadSst' | 'productividad' | 'calidadTecnica' | 'puntualidad' | 'trabajoEquipo'
>): number {
  const suma =
    evalucion.seguridadSst +
    evalucion.productividad +
    evalucion.calidadTecnica +
    evalucion.puntualidad +
    evalucion.trabajoEquipo
  return Math.round((suma / 5) * 10) / 10
}
