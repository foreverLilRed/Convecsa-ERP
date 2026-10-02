export type TipoDocumento = 'DNI' | 'CE' | 'PASAPORTE'

export type GradoInstruccion = 'PRIMARIA' | 'SECUNDARIA' | 'TECNICO' | 'UNIVERSITARIO' | 'POSGRADO'

export type Banco = 'BCP' | 'BBVA' | 'INTERBANK' | 'SCOTIABANK' | 'OTROS'

export type AptitudMedica = 'APTO' | 'OBSERVADO' | 'PENDIENTE' | 'VENCIDO'

export type TipoDocumentoChecklist =
  | 'DNI_2_CARAS'
  | 'CV_CERTIFICADO_TRABAJO'
  | 'CARNET_RETCC'
  | 'CUL'
  | 'ANTECEDENTES_POLICIALES'
  | 'ANTECEDENTES_JUDICIALES'
  | 'ANTECEDENTES_PENALES'
  | 'CARNET_VACUNACION'
  | 'EMO_APTO_VIGENTE'
  | 'SCTR'
  | 'POLIZA_VIDA_LEY'
  | 'CHECKLIST_INGRESO'
  | 'FICHA_TRABAJADOR'
  | 'ALTA_T_REGISTRO'

export type CriticidadDocumento = 'CRITICO' | 'NORMAL'

export type EstadoDocumentoChecklist = 'CONFORME' | 'PENDIENTE' | 'OBSERVADO' | 'NO_APLICA'

export type CatalogOption<T extends string> = {
  value: T
  label: string
}

export type CuentaBancariaForm = {
  banco: Banco | ''
  numeroCuenta: string
  cci: string
  principal: boolean
}

export type ExperienciaLaboralForm = {
  organizacion: string
  cargo: string
  fechaInicioLaboral: string
  fechaFin: string
}

export type DocumentoChecklistForm = {
  tipo: TipoDocumentoChecklist
  criticidad: CriticidadDocumento
  estado: EstadoDocumentoChecklist
}

export type PersonaForm = {
  apellidos: string
  nombres: string
  tipoDocumento: TipoDocumento
  numeroDocumento: string
  fechaNacimiento: string
  fechaEmisionDni: string
  fechaCaducidadDni: string
  celulares: string[]
  correo: string
  correoSecundario: string
  gradoInstruccion: GradoInstruccion | ''
  direccion: string
  departamento: string
  provincia: string
  distrito: string
  residencia: string
  cuentasBancarias: CuentaBancariaForm[]
  experienciasLaborales: ExperienciaLaboralForm[]
  fotoUrl: string | null
  fechaVigenciaEmo: string
  aptitudMedica: AptitudMedica | ''
  restriccionesMedicas: string
  documentosChecklist: DocumentoChecklistForm[]
}

export type CuentaBancariaRequest = {
  banco: Banco
  numeroCuenta: string
  cci: string
  principal: boolean
}

export type ExperienciaLaboralRequest = {
  organizacion: string
  cargo: string
  fechaInicioLaboral: string
  fechaFin: string | null
}

export type DocumentoChecklistRequest = {
  tipo: TipoDocumentoChecklist
  estado: EstadoDocumentoChecklist
}

export type PersonaRequest = {
  apellidos: string
  nombres: string
  tipoDocumento: TipoDocumento
  numeroDocumento: string
  fechaNacimiento: string
  fechaEmisionDni: string | null
  fechaCaducidadDni: string | null
  celulares: string[]
  correo: string
  correoSecundario: string | null
  gradoInstruccion: GradoInstruccion
  direccion: string
  departamento: string
  provincia: string
  distrito: string
  residencia: string
  fotoUrl: string | null
  cuentasBancarias: CuentaBancariaRequest[]
  experienciasLaborales: ExperienciaLaboralRequest[]
  fechaVigenciaEmo: string | null
  aptitudMedica: AptitudMedica | null
  restriccionesMedicas: string | null
  documentosChecklist: DocumentoChecklistRequest[]
}

export type CelularDTO = {
  id: number
  numero: string
}

export type CuentaBancariaDTO = {
  id: number
  banco: Banco
  numeroCuenta: string
  cci: string
  principal: boolean
}

export type ExperienciaLaboralDTO = {
  id: number
  organizacion: string
  cargo: string
  fechaInicioLaboral: string
  fechaFin: string | null
}

export type DocumentoChecklistDTO = {
  id: number
  tipo: TipoDocumentoChecklist
  criticidad: CriticidadDocumento
  estado: EstadoDocumentoChecklist
}

export type PersonaResumenDTO = {
  id: number
  apellidos: string
  nombres: string
  tipoDocumento: TipoDocumento
  numeroDocumento: string
  correo: string
  gradoInstruccion: GradoInstruccion
  departamento: string
  distrito: string
  createdAt: string
}

export type PagedResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export type PersonaListQuery = {
  page?: number
  size?: number
  sort?: string
  q?: string
}

export type PersonaDTO = {
  id: number
  apellidos: string
  nombres: string
  tipoDocumento: TipoDocumento
  numeroDocumento: string
  fechaNacimiento: string
  fechaEmisionDni: string | null
  fechaCaducidadDni: string | null
  celulares: CelularDTO[]
  correo: string
  correoSecundario: string | null
  gradoInstruccion: GradoInstruccion
  direccion: string
  departamento: string
  provincia: string
  distrito: string
  residencia: string
  fotoUrl: string | null
  cuentasBancarias: CuentaBancariaDTO[]
  experienciasLaborales: ExperienciaLaboralDTO[]
  fechaVigenciaEmo: string | null
  aptitudMedica: AptitudMedica | null
  restriccionesMedicas: string | null
  documentosChecklist: DocumentoChecklistDTO[]
  createdAt: string
}

export const TIPOS_DOCUMENTO: TipoDocumento[] = ['DNI', 'CE', 'PASAPORTE']

export const GRADOS_INSTRUCCION: CatalogOption<GradoInstruccion>[] = [
  { value: 'PRIMARIA', label: 'Primaria' },
  { value: 'SECUNDARIA', label: 'Secundaria' },
  { value: 'TECNICO', label: 'Técnico' },
  { value: 'UNIVERSITARIO', label: 'Universitario' },
  { value: 'POSGRADO', label: 'Posgrado' },
]

export const BANCOS: CatalogOption<Banco>[] = [
  { value: 'BCP', label: 'BCP' },
  { value: 'BBVA', label: 'BBVA' },
  { value: 'INTERBANK', label: 'Interbank' },
  { value: 'SCOTIABANK', label: 'Scotiabank' },
  { value: 'OTROS', label: 'Otros' },
]

export const APTITUDES_MEDICAS: CatalogOption<AptitudMedica>[] = [
  { value: 'APTO', label: 'APTO (Sin Restricciones)' },
  { value: 'OBSERVADO', label: 'APTO CON RESTRICCIONES' },
  { value: 'PENDIENTE', label: 'PENDIENTE DE EVALUACIÓN' },
  { value: 'VENCIDO', label: 'NO APTO / VENCIDO' },
]

export const DOCUMENTOS_CHECKLIST: {
  tipo: TipoDocumentoChecklist
  label: string
  criticidad: CriticidadDocumento
}[] = [
  { tipo: 'DNI_2_CARAS', label: 'DNI 2 Caras Vigente', criticidad: 'CRITICO' },
  { tipo: 'CV_CERTIFICADO_TRABAJO', label: 'CV / Certificado Trabajo', criticidad: 'NORMAL' },
  { tipo: 'CARNET_RETCC', label: 'Carnet RETCC (Solo RCC / No Aplica Régimen Común)', criticidad: 'NORMAL' },
  { tipo: 'CUL', label: 'Certificado Único Laboral (CUL)', criticidad: 'NORMAL' },
  { tipo: 'ANTECEDENTES_POLICIALES', label: 'Antecedentes Policiales', criticidad: 'NORMAL' },
  { tipo: 'ANTECEDENTES_JUDICIALES', label: 'Antecedentes Judiciales', criticidad: 'NORMAL' },
  { tipo: 'ANTECEDENTES_PENALES', label: 'Antecedentes Penales', criticidad: 'NORMAL' },
  { tipo: 'CARNET_VACUNACION', label: 'Carnet Vacunación / MINSA', criticidad: 'NORMAL' },
  { tipo: 'EMO_APTO_VIGENTE', label: 'EMO Apto y Vigente', criticidad: 'CRITICO' },
  { tipo: 'SCTR', label: 'SCTR Salud y Pensión', criticidad: 'CRITICO' },
  { tipo: 'POLIZA_VIDA_LEY', label: 'Póliza Seguro Vida Ley', criticidad: 'NORMAL' },
  { tipo: 'CHECKLIST_INGRESO', label: 'Check list de ingreso', criticidad: 'NORMAL' },
  { tipo: 'FICHA_TRABAJADOR', label: 'Ficha del trabajador', criticidad: 'NORMAL' },
  { tipo: 'ALTA_T_REGISTRO', label: 'Alta en T-Registro SUNAT', criticidad: 'CRITICO' },
]

export const ESTADOS_CHECKLIST: CatalogOption<EstadoDocumentoChecklist>[] = [
  { value: 'CONFORME', label: 'OK' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'OBSERVADO', label: 'Observado' },
  { value: 'NO_APLICA', label: 'No aplica' },
]

export const ESTADOS_CHECKLIST_VALIDOS: EstadoDocumentoChecklist[] = [
  'CONFORME',
  'PENDIENTE',
  'OBSERVADO',
  'NO_APLICA',
]

export function createDefaultChecklist(): DocumentoChecklistForm[] {
  return DOCUMENTOS_CHECKLIST.map((documento) => ({
    tipo: documento.tipo,
    criticidad: documento.criticidad,
    estado: 'PENDIENTE',
  }))
}

export function completeChecklist(
  items: DocumentoChecklistForm[] | undefined,
): DocumentoChecklistForm[] {
  const byTipo = new Map((items ?? []).map((item) => [item.tipo, item]))
  return DOCUMENTOS_CHECKLIST.map((documento) => {
    const saved = byTipo.get(documento.tipo)
    const estado = saved && ESTADOS_CHECKLIST_VALIDOS.includes(saved.estado)
      ? saved.estado
      : 'PENDIENTE'
    return {
      tipo: documento.tipo,
      criticidad: documento.criticidad,
      estado,
    }
  })
}

export function cicloEstadoChecklist(
  tipo: TipoDocumentoChecklist,
  estado: EstadoDocumentoChecklist,
): EstadoDocumentoChecklist {
  const ciclo: EstadoDocumentoChecklist[] =
    tipo === 'CARNET_RETCC'
      ? ['CONFORME', 'PENDIENTE', 'OBSERVADO', 'NO_APLICA']
      : ['CONFORME', 'PENDIENTE', 'OBSERVADO']
  const actual = ciclo.indexOf(estado)
  const siguiente = actual === -1 ? 0 : (actual + 1) % ciclo.length
  return ciclo[siguiente] ?? 'PENDIENTE'
}

export function estadoChecklistLabel(estado: EstadoDocumentoChecklist): string {
  return ESTADOS_CHECKLIST.find((item) => item.value === estado)?.label ?? estado
}

export function createEmptyPersonaForm(): PersonaForm {
  return {
    apellidos: '',
    nombres: '',
    tipoDocumento: 'DNI',
    numeroDocumento: '',
    fechaNacimiento: '',
    fechaEmisionDni: '',
    fechaCaducidadDni: '',
    celulares: [''],
    correo: '',
    correoSecundario: '',
    gradoInstruccion: '',
    direccion: '',
    departamento: '',
    provincia: '',
    distrito: '',
    residencia: '',
    cuentasBancarias: [],
    experienciasLaborales: [],
    fotoUrl: null,
    fechaVigenciaEmo: '',
    aptitudMedica: '',
    restriccionesMedicas: '',
    documentosChecklist: createDefaultChecklist(),
  }
}

export function toPersonaForm(dto: PersonaDTO): PersonaForm {
  return {
    apellidos: dto.apellidos,
    nombres: dto.nombres,
    tipoDocumento: dto.tipoDocumento,
    numeroDocumento: dto.numeroDocumento,
    fechaNacimiento: dto.fechaNacimiento,
    fechaEmisionDni: dto.fechaEmisionDni ?? '',
    fechaCaducidadDni: dto.fechaCaducidadDni ?? '',
    celulares: dto.celulares.length > 0 ? dto.celulares.map((celular) => celular.numero) : [''],
    correo: dto.correo,
    correoSecundario: dto.correoSecundario ?? '',
    gradoInstruccion: dto.gradoInstruccion,
    direccion: dto.direccion,
    departamento: dto.departamento,
    provincia: dto.provincia,
    distrito: dto.distrito,
    residencia: dto.residencia,
    cuentasBancarias: dto.cuentasBancarias.map((cuenta) => ({
      banco: cuenta.banco,
      numeroCuenta: cuenta.numeroCuenta,
      cci: cuenta.cci,
      principal: cuenta.principal,
    })),
    experienciasLaborales: dto.experienciasLaborales.map((experiencia) => ({
      organizacion: experiencia.organizacion,
      cargo: experiencia.cargo,
      fechaInicioLaboral: experiencia.fechaInicioLaboral,
      fechaFin: experiencia.fechaFin ?? '',
    })),
    fotoUrl: dto.fotoUrl,
    fechaVigenciaEmo: dto.fechaVigenciaEmo ?? '',
    aptitudMedica: dto.aptitudMedica ?? '',
    restriccionesMedicas: dto.restriccionesMedicas ?? '',
    documentosChecklist: completeChecklist(dto.documentosChecklist),
  }
}

export function toPersonaRequest(form: PersonaForm): PersonaRequest {
  return {
    apellidos: form.apellidos.trim(),
    nombres: form.nombres.trim(),
    tipoDocumento: form.tipoDocumento,
    numeroDocumento: form.numeroDocumento.trim(),
    fechaNacimiento: form.fechaNacimiento,
    fechaEmisionDni: emptyToNull(form.fechaEmisionDni),
    fechaCaducidadDni: emptyToNull(form.fechaCaducidadDni),
    celulares: form.celulares.map((celular) => celular.trim()).filter(Boolean),
    correo: form.correo.trim(),
    correoSecundario: emptyToNull(form.correoSecundario),
    gradoInstruccion: form.gradoInstruccion as GradoInstruccion,
    direccion: form.direccion.trim(),
    departamento: form.departamento.trim(),
    provincia: form.provincia.trim(),
    distrito: form.distrito.trim(),
    residencia: form.residencia.trim(),
    fotoUrl: toPersistedFotoUrl(form.fotoUrl),
    cuentasBancarias: form.cuentasBancarias.map((cuenta) => ({
      banco: cuenta.banco as Banco,
      numeroCuenta: cuenta.numeroCuenta.trim(),
      cci: cuenta.cci.trim(),
      principal: cuenta.principal,
    })),
    experienciasLaborales: form.experienciasLaborales.map((experiencia) => ({
      organizacion: experiencia.organizacion.trim(),
      cargo: experiencia.cargo.trim(),
      fechaInicioLaboral: experiencia.fechaInicioLaboral,
      fechaFin: emptyToNull(experiencia.fechaFin),
    })),
    fechaVigenciaEmo: emptyToNull(form.fechaVigenciaEmo),
    aptitudMedica: form.aptitudMedica ? form.aptitudMedica : null,
    restriccionesMedicas: emptyToNull(form.restriccionesMedicas),
    documentosChecklist: completeChecklist(form.documentosChecklist).map((item) => ({
      tipo: item.tipo,
      estado: item.estado,
    })),
  }
}

export function vigenciaEmoLabel(fecha: string): string {
  if (!fecha) {
    return 'Sin fecha de vigencia'
  }
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const vigencia = new Date(`${fecha}T00:00:00`)
  if (Number.isNaN(vigencia.getTime())) {
    return 'Fecha inválida'
  }
  const diffMs = vigencia.getTime() - today.getTime()
  const days = Math.round(diffMs / 86_400_000)
  if (days < 0) {
    return 'Vencido'
  }
  if (days === 0) {
    return 'Vence hoy'
  }
  if (days === 1) {
    return '1 día restante'
  }
  return `${days} días restantes`
}

export const TIPOS_IMAGEN = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'] as const

function emptyToNull(value: string): string | null {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function toPersistedFotoUrl(value: string | null): string | null {
  if (!value || value.startsWith('blob:')) {
    return null
  }
  return emptyToNull(value)
}
