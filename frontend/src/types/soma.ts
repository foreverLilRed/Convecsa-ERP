export type SomaResumenObra = {
  proyectoId: number
  codigoObra: string
  nombreObra: string
  supervisorSsoma: string | null
}

export type TipoEmo = 'INGRESO' | 'PERIODICO' | 'RETIRO' | 'CAMBIO_PUESTO' | 'REINCORPORACION'

export type AptitudEmo = 'APTO' | 'APTO_CON_RESTRICCION' | 'OBSERVADO' | 'NO_APTO'

export type SomaEmo = {
  id: number
  personaId: number
  personaNombre: string | null
  proyectoId: number | null
  tipoEmo: TipoEmo
  clinicaNombre: string
  clinicaRuc: string | null
  medicoEvaluador: string | null
  cmpMedico: string | null
  fechaExamen: string
  fechaVencimiento: string
  aptitud: AptitudEmo
  restricciones: string | null
  interconsultas: string | null
  observaciones: string | null
  documentoObraId: number | null
  createdAt: string
}

export type SomaEmoRequest = {
  personaId: number
  tipoEmo: TipoEmo
  clinicaNombre: string
  clinicaRuc?: string | null
  medicoEvaluador?: string | null
  cmpMedico?: string | null
  fechaExamen: string
  fechaVencimiento: string
  aptitud: AptitudEmo
  restricciones?: string | null
  interconsultas?: string | null
  observaciones?: string | null
  documentoObraId?: number | null
}

export type EstadoSctrPoliza = 'VIGENTE' | 'ANULADA'

export type SomaSctrNominaFila = {
  personaId: number
  personaNombre: string | null
  numeroDocumento: string | null
  incluidoSalud: boolean
  incluidoPension: boolean
}

export type SomaSctrPoliza = {
  id: number
  proyectoId: number
  anio: number
  mes: number
  aseguradoraSalud: string
  numeroPolizaSalud: string
  aseguradoraPension: string
  numeroPolizaPension: string
  fechaInicio: string
  fechaFin: string
  montoPrimaSalud: number
  montoPrimaPension: number
  documentoObraId: number | null
  estado: EstadoSctrPoliza
  createdAt: string
  nomina: SomaSctrNominaFila[]
}

export type SomaSctrPolizaRequest = {
  anio: number
  mes: number
  aseguradoraSalud: string
  numeroPolizaSalud: string
  aseguradoraPension: string
  numeroPolizaPension: string
  fechaInicio: string
  fechaFin: string
  montoPrimaSalud: number
  montoPrimaPension: number
  documentoObraId?: number | null
}

export type SomaSctrNominaGuardarRequest = {
  filas: Array<{
    personaId: number
    incluidoSalud: boolean
    incluidoPension: boolean
  }>
}

export type SomaTrabajadorObra = {
  personaId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  categoria: 'OPERARIO' | 'OFICIAL' | 'PEON'
  fechaIngreso: string | null
  fechaCese: string | null
}

export type MotivoEntregaEpp = 'DOTACION_INICIAL' | 'RENOVACION_DESGASTE' | 'REPOSICION_PERDIDA' | 'TRABAJO_ESPECIAL'

export type EstadoItemEpp = 'VIGENTE' | 'DEVUELTO' | 'DETERIORADO' | 'EXTRAVIADO'

export type SomaEntregaEppItem = {
  id: number
  materialId: number
  materialCodigo: string | null
  materialDescripcion: string | null
  cantidad: number
  talla: string | null
  fechaVencimientoEstimada: string | null
  estadoItem: EstadoItemEpp
}

export type SomaEntregaEpp = {
  id: number
  proyectoId: number
  personaId: number
  personaNombre: string | null
  numeroDocumento: string | null
  valeSalidaId: number | null
  fechaEntrega: string
  motivo: MotivoEntregaEpp
  firmadoTrabajador: boolean
  entregadoPor: string
  observaciones: string | null
  createdAt: string
  items: SomaEntregaEppItem[]
}

export type SomaEntregaEppRequest = {
  personaId: number
  fechaEntrega: string
  motivo: MotivoEntregaEpp
  firmadoTrabajador?: boolean
  descontarAlmacen?: boolean
  partidaId?: number | null
  observaciones?: string | null
  items: Array<{
    materialId: number
    cantidad: number
    talla?: string | null
  }>
}

export type SomaEppMaterial = {
  materialId: number
  codigo: string
  descripcion: string
  unidad: string | null
  tipoEpp: string | null
  vidaUtilDias: number | null
  stock: number
}
