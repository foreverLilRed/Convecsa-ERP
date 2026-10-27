export type EstadoObra =
  | 'BORRADOR'
  | 'PLANIFICADA'
  | 'EN_EJECUCION'
  | 'SUSPENDIDA'
  | 'CERRADA'
  | 'CANCELADA'

export type ObraRequest = {
  codigo: string
  nombre: string
  clienteNombre: string
  contratistaPrincipal: string | null
  ubicacion: string | null
  fechaInicioPlanificada: string | null
  fechaFinPlanificada: string | null
  fechaInicioReal: string | null
  fechaFinReal: string | null
  montoContratado: number | null
  moneda: string | null
  responsableNombre: string | null
}

export type ObraDTO = {
  id: number
  codigo: string
  nombre: string
  clienteNombre: string
  contratistaPrincipal: string | null
  ubicacion: string | null
  fechaInicioPlanificada: string | null
  fechaFinPlanificada: string | null
  fechaInicioReal: string | null
  fechaFinReal: string | null
  montoContratado: number | null
  moneda: string
  responsableNombre: string | null
  estado: EstadoObra
  createdAt: string
  updatedAt: string
}

export type ObraEstadoRequest = {
  estado: EstadoObra
  justificacion: string | null
}

export type ObraResumen = {
  total: number
  borrador: number
  planificada: number
  enEjecucion: number
  suspendida: number
  cerrada: number
  cancelada: number
}

export const ESTADOS_OBRA: Array<{ value: EstadoObra; label: string }> = [
  { value: 'BORRADOR', label: 'Borrador' },
  { value: 'PLANIFICADA', label: 'Planificada' },
  { value: 'EN_EJECUCION', label: 'En ejecución' },
  { value: 'SUSPENDIDA', label: 'Suspendida' },
  { value: 'CERRADA', label: 'Cerrada' },
  { value: 'CANCELADA', label: 'Cancelada' },
]

export const TRANSICIONES_OBRA: Record<EstadoObra, EstadoObra[]> = {
  BORRADOR: ['PLANIFICADA', 'CANCELADA'],
  PLANIFICADA: ['EN_EJECUCION', 'CANCELADA'],
  EN_EJECUCION: ['SUSPENDIDA', 'CERRADA'],
  SUSPENDIDA: ['EN_EJECUCION', 'CANCELADA'],
  CERRADA: [],
  CANCELADA: [],
}

export function etiquetaEstadoObra(estado: EstadoObra): string {
  return ESTADOS_OBRA.find((item) => item.value === estado)?.label ?? estado
}

export function etiquetaAccionEstado(destino: EstadoObra): string {
  switch (destino) {
    case 'PLANIFICADA':
      return 'Planificar'
    case 'EN_EJECUCION':
      return 'Iniciar / reanudar'
    case 'SUSPENDIDA':
      return 'Suspender'
    case 'CERRADA':
      return 'Cerrar'
    case 'CANCELADA':
      return 'Cancelar'
    default:
      return etiquetaEstadoObra(destino)
  }
}
