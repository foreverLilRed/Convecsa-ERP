export type NivelRiesgoObra = 'BAJO' | 'MEDIO' | 'ALTO'

export type EstadoObra = 'EN_EJECUCION' | 'PARALIZADA' | 'LIQUIDADA'

export type RolResponsableObra = 'RESIDENTE' | 'MAESTRO_OBRA' | 'SUPERVISOR_SSOMA'

export type CadenciaPeriodoCc = 'SEMANAL' | 'QUINCENAL' | 'MENSUAL'

export type BancoDispersion = 'CCI' | 'BCP' | 'INTERBANK'

export type Proyecto = {
  id: number
  codigo: string
  nombre: string
  ubicacion: string | null
  centroCosto: string | null
  valorObra: number | null
  nivelRiesgo: NivelRiesgoObra | null
  tasaSctrSalud: number | null
  tasaSctrPension: number | null
  clienteRazon: string | null
  clienteRuc: string | null
  fechaInicio: string | null
  fechaFin: string | null
  estadoObra: EstadoObra
  activo: boolean
  alertaUit: boolean
  uitAnio: number
}

export type ProyectoRequest = {
  codigo: string
  nombre: string
  ubicacion?: string | null
  centroCosto?: string | null
  valorObra?: number | null
  nivelRiesgo?: NivelRiesgoObra | null
  tasaSctrSalud?: number | null
  tasaSctrPension?: number | null
  clienteRazon?: string | null
  clienteRuc?: string | null
  fechaInicio?: string | null
  fechaFin?: string | null
  estadoObra?: EstadoObra
  activo?: boolean
}

export type ResponsableObra = {
  id: number
  personaId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  rol: RolResponsableObra
}

export type ProyectoResponsableRequest = {
  personaId: number
  rol: RolResponsableObra
}

export type ConstruccionCatalogos = {
  categorias: { id: number; codigo: string; label: string }[]
  especialidadesBae: { id: number; codigo: string; label: string }[]
  proyectos: Proyecto[]
  anioParametros: number
  uitAnio: number
  uit: number
  umbralUit: number
}

export type PersonalObra = {
  personaId: number
  contratoId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  categoria: 'OPERARIO' | 'OFICIAL' | 'PEON'
  especialidadBae: string | null
  hijosEscolaridad: number
  afiliadoSindical: boolean
  centroCosto: string | null
  fechaIngreso: string | null
  fechaCese: string | null
}

export type PeriodoCc = {
  id: number
  proyectoId: number
  proyectoCodigo: string
  proyectoNombre: string
  fechaDesde: string
  fechaHasta: string
  nroSemana: number
  cadencia: CadenciaPeriodoCc
  estado: 'BORRADOR' | 'CERRADO'
  createdAt: string
  closedAt: string | null
  cantidadBoletas: number
}

export type PeriodoCcRequest = {
  fechaDesde: string
  fechaHasta: string
  nroSemana?: number
  cadencia?: CadenciaPeriodoCc
}

export type CodigoTareoCc = 'LAB' | 'F' | 'DSC' | 'FE' | 'DM' | 'LIC'

export type TareoCcDia = {
  personaId: number
  fecha: string
  horasOrd: number
  he60: number
  he100: number
  codigo: CodigoTareoCc
  altura: boolean
  altitud: boolean
  agua: boolean
  altasTemp: boolean
  cotaCero: boolean
  nocturno: boolean
  aguasServidas: boolean
  bae: boolean
  movilidad: boolean
  pisos: number
}

export type TareoCcGrilla = {
  periodo: PeriodoCc
  fechas: string[]
  personal: PersonalObra[]
  dias: TareoCcDia[]
  jornadaLv: number
  jornadaSabado: number
}

export type BoletaCcResumen = {
  id: number
  personaId: number
  personaNombreCompleto: string
  numeroDocumento: string
  categoria: string | null
  totalHaberes: number
  totalDescuentos: number
  neto: number
}

export type BoletaCcConcepto = {
  categoria: 'HABER' | 'DESCUENTO' | 'APORTE_EMPLEADOR'
  codigo: string
  label: string
  monto: number
  origen?: 'CALCULADO' | 'MANUAL' | 'TAREO'
  orden: number
}

export type BoletaCc = {
  id: number
  periodoId: number
  personaId: number
  contratoId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  totalHaberes: number
  totalDescuentos: number
  totalAportesEmpleador: number
  totalAfecto: number
  neto: number
  tarifaDia: number
  tarifaHora: number
  createdAt: string
  conceptos: BoletaCcConcepto[]
}

export const CODIGOS_TAREO: { codigo: CodigoTareoCc; label: string }[] = [
  { codigo: 'LAB', label: 'Labor' },
  { codigo: 'F', label: 'Falta' },
  { codigo: 'DSC', label: 'Descanso' },
  { codigo: 'FE', label: 'Feriado' },
  { codigo: 'DM', label: 'D. médico' },
  { codigo: 'LIC', label: 'Licencia' },
]
