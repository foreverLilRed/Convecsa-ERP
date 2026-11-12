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
