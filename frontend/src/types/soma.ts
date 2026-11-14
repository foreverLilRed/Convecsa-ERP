export type SomaResumenObra = {
  proyectoId: number
  codigoObra: string
  nombreObra: string
  supervisorSsoma: string | null
  habilitados: number
  bloqueados: number
  emosPorVencer30Dias: number
}

export type EstadoHabilitacionSoma = 'HABILITADO' | 'BLOQUEADO'

export type SomaHabilitacionFila = {
  personaId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  estado: EstadoHabilitacionSoma
  motivos: string[]
  restricciones: string | null
}

export type SomaHabilitacion = {
  fecha: string
  habilitados: number
  bloqueados: number
  emosPorVencer30Dias: number
  filas: SomaHabilitacionFila[]
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

export type TipoCapacitacionSoma =
  | 'INDUCCION_HOMBRE_NUEVO'
  | 'CAPACITACION_PROGRAMADA'
  | 'ENTRENAMIENTO_ESPECIFICO_ALTO_RIESGO'
  | 'SIMULACRO_EMERGENCIA'

export type SomaCapacitacionAsistente = {
  id: number | null
  personaId: number
  personaNombre: string | null
  numeroDocumento: string | null
  nota: number | null
  aprobado: boolean
}

export type SomaCapacitacion = {
  id: number
  proyectoId: number | null
  tipo: TipoCapacitacionSoma
  tema: string
  fecha: string
  horasDuracion: number
  expositorNombre: string
  expositorEntidad: string | null
  puntajeSimulacro: number | null
  observaciones: string | null
  evidenciaDocId: number | null
  createdAt: string
  asistentes: SomaCapacitacionAsistente[]
}

export type TipoPeligroIperc =
  | 'LOCATIVO'
  | 'MECANICO'
  | 'ELECTRICO'
  | 'FISICO'
  | 'QUIMICO'
  | 'ERGONOMICO'
  | 'PSICOSOCIAL'
  | 'BIOLOGICO'

export type NivelRiesgoIperc = 'TRIVIAL' | 'TOLERABLE' | 'MODERADO' | 'IMPORTANTE' | 'INTOLERABLE'

export function nivelRiesgoIpercDesdePuntaje(puntaje: number): NivelRiesgoIperc {
  if (puntaje <= 4) {
    return 'TRIVIAL'
  }
  if (puntaje <= 8) {
    return 'TOLERABLE'
  }
  if (puntaje <= 12) {
    return 'MODERADO'
  }
  if (puntaje <= 19) {
    return 'IMPORTANTE'
  }
  return 'INTOLERABLE'
}

export type SomaIpercFila = {
  id: number
  proyectoId: number
  partidaId: number | null
  partidaCodigo: string | null
  partidaDescripcion: string | null
  actividadTarea: string
  puestoTrabajo: string
  rutinario: boolean
  tipoPeligro: TipoPeligroIperc
  peligro: string
  riesgo: string
  consecuencia: string
  requisitoLegal: string | null
  probabilidadInicial: number
  severidadInicial: number
  nivelRiesgoInicial: NivelRiesgoIperc
  controlEliminacion: string | null
  controlSustitucion: string | null
  controlIngenieria: string | null
  controlAdministrativo: string | null
  controlEpp: string | null
  probabilidadResidual: number
  severidadResidual: number
  nivelRiesgoResidual: NivelRiesgoIperc
  activo: boolean
  createdAt: string
}

export type SomaIpercFilaRequest = {
  partidaId?: number | null
  actividadTarea: string
  puestoTrabajo: string
  rutinario?: boolean
  tipoPeligro: TipoPeligroIperc
  peligro: string
  riesgo: string
  consecuencia: string
  requisitoLegal?: string | null
  probabilidadInicial: number
  severidadInicial: number
  controlEliminacion?: string | null
  controlSustitucion?: string | null
  controlIngenieria?: string | null
  controlAdministrativo?: string | null
  controlEpp?: string | null
  probabilidadResidual: number
  severidadResidual: number
}

export type SomaCapacitacionRequest = {
  tipo: TipoCapacitacionSoma
  tema: string
  fecha: string
  horasDuracion: number
  expositorNombre: string
  expositorEntidad?: string | null
  puntajeSimulacro?: number | null
  observaciones?: string | null
  evidenciaDocId?: number | null
  asistentes: Array<{
    personaId: number
    nota?: number | null
    aprobado?: boolean
  }>
}

export type PasoAts = {
  paso: string
  peligros: string | null
  controles: string | null
}

export type SomaCharlaAsistente = {
  id: number | null
  personaId: number
  personaNombre: string | null
  numeroDocumento: string | null
  habilitadoSoma: boolean
  observacion: string | null
}

export type SomaCharlaAts = {
  id: number
  proyectoId: number
  cuadrillaId: number | null
  cuadrillaNombre: string | null
  partidaId: number | null
  partidaCodigo: string | null
  partidaDescripcion: string | null
  fecha: string
  horaInicio: string
  duracionMinutos: number
  temaCharla: string
  expositorPersonaId: number | null
  expositorNombre: string | null
  supervisorSsomaId: number | null
  supervisorSsomaNombre: string | null
  pasos: PasoAts[]
  eppVerificado: boolean
  herramientasVerif: boolean
  observaciones: string | null
  createdAt: string
  asistentes: SomaCharlaAsistente[]
  advertencia: string | null
}

export type SomaCharlaAtsRequest = {
  cuadrillaId?: number | null
  partidaId?: number | null
  fecha: string
  horaInicio: string
  duracionMinutos?: number | null
  temaCharla: string
  expositorPersonaId?: number | null
  supervisorSsomaId?: number | null
  pasos: PasoAts[]
  eppVerificado?: boolean
  herramientasVerif?: boolean
  observaciones?: string | null
  asistentes: Array<{
    personaId: number
    observacion?: string | null
  }>
}

export type TipoInspeccionSoma =
  | 'PLANEADA_MENSUAL'
  | 'INOPINADA_DIARIA'
  | 'EPP'
  | 'EXTINTORES_EMERGENCIA'
  | 'INSTALACIONES_ELECTRICAS_G050'
  | 'ANDAMIOS_ESCALERAS'
  | 'ORDEN_LIMPIEZA'
  | 'ALMACEN_MATPEL'
  | 'SUBCONTRATISTA'

export type ClasificacionHallazgoSoma = 'ACTO_SUBESTANDAR' | 'CONDICION_SUBESTANDAR'

export type PotencialPerdidaSoma = 'ALTO_GRAVE' | 'MEDIO_MODERADO' | 'BAJO_LEVE'

export type EstadoHallazgoSoma = 'ABIERTO' | 'EN_PROCESO' | 'LEVANTADO'

export type SomaHallazgo = {
  id: number
  proyectoId: number
  inspeccionId: number | null
  inspeccionAreaFrente: string | null
  clasificacion: ClasificacionHallazgoSoma
  potencialPerdida: PotencialPerdidaSoma
  descripcion: string
  ubicacion: string | null
  accionCorrectiva: string
  responsablePersonaId: number | null
  responsableNombre: string | null
  fechaDeteccion: string
  fechaLimite: string
  fechaCierre: string | null
  paralizacionFrente: boolean
  evidenciaAntesDocId: number | null
  evidenciaDespuesDocId: number | null
  estado: EstadoHallazgoSoma
  vencido: boolean
  createdAt: string
}

export type SomaInspeccion = {
  id: number
  proyectoId: number
  tipo: TipoInspeccionSoma
  fecha: string
  areaFrente: string
  subcontratoId: number | null
  subcontratoNombre: string | null
  inspectorPersonaId: number | null
  inspectorNombre: string | null
  puntajeCumplimiento: number | null
  observaciones: string | null
  createdAt: string
  hallazgos: SomaHallazgo[]
}

export type SomaInspeccionRequest = {
  tipo: TipoInspeccionSoma
  fecha: string
  areaFrente: string
  subcontratoId?: number | null
  inspectorPersonaId?: number | null
  puntajeCumplimiento?: number | null
  observaciones?: string | null
}

export type SomaHallazgoRequest = {
  inspeccionId?: number | null
  clasificacion: ClasificacionHallazgoSoma
  potencialPerdida: PotencialPerdidaSoma
  descripcion: string
  ubicacion?: string | null
  accionCorrectiva: string
  responsablePersonaId?: number | null
  fechaDeteccion: string
  fechaLimite: string
  paralizacionFrente?: boolean
  evidenciaAntesDocId?: number | null
}

export type SomaHallazgoLevantarRequest = {
  evidenciaDespuesDocId: number
  fechaCierre?: string | null
}
