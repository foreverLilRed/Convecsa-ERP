export type TipoDocumento = 'DNI' | 'CE' | 'PASAPORTE'

export type GradoInstruccion = 'PRIMARIA' | 'SECUNDARIA' | 'TECNICO' | 'UNIVERSITARIO' | 'POSGRADO'

export type Genero = 'MASCULINO' | 'FEMENINO' | 'NO_ESPECIFICA'

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
  genero: Genero | ''
  contactoEmergenciaNombre: string
  contactoEmergenciaTelefono: string
  contactoEmergenciaParentesco: string
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
  genero: Genero
  contactoEmergenciaNombre: string
  contactoEmergenciaTelefono: string
  contactoEmergenciaParentesco: string | null
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
  fechaNacimiento: string | null
  fechaEmisionDni: string | null
  fechaCaducidadDni: string | null
  celulares: CelularDTO[]
  correo: string
  gradoInstruccion: GradoInstruccion
  direccion: string
  departamento: string
  provincia: string
  distrito: string
  residencia: string
  cuentasBancarias: CuentaBancariaDTO[]
  experienciasLaborales: ExperienciaLaboralDTO[]
  fechaVigenciaEmo: string | null
  aptitudMedica: AptitudMedica | null
  documentosChecklist: DocumentoChecklistDTO[]
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
  conContratoActivo?: boolean
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
  genero: Genero
  contactoEmergenciaNombre: string | null
  contactoEmergenciaTelefono: string | null
  contactoEmergenciaParentesco: string | null
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

export const GENEROS: CatalogOption<Genero>[] = [
  { value: 'MASCULINO', label: 'Masculino' },
  { value: 'FEMENINO', label: 'Femenino' },
  { value: 'NO_ESPECIFICA', label: 'No especifica' },
]

export const PARENTESCOS_EMERGENCIA = [
  'Madre',
  'Padre',
  'Cónyuge / Esposo(a)',
  'Hijo(a)',
  'Hermano(a)',
  'Familiar',
] as const

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

export function documentosDisponibles(
  agregados: { tipo: TipoDocumentoChecklist }[],
): typeof DOCUMENTOS_CHECKLIST {
  const usados = new Set(agregados.map((item) => item.tipo))
  return DOCUMENTOS_CHECKLIST.filter((documento) => !usados.has(documento.tipo))
}

export function toChecklistForm(
  items: Array<DocumentoChecklistForm | DocumentoChecklistDTO> | undefined,
): DocumentoChecklistForm[] {
  return (items ?? [])
    .filter((item) => ESTADOS_CHECKLIST_VALIDOS.includes(item.estado))
    .map((item) => {
      const catalogo = DOCUMENTOS_CHECKLIST.find((documento) => documento.tipo === item.tipo)
      return {
        tipo: item.tipo,
        criticidad: catalogo?.criticidad ?? item.criticidad,
        estado: item.estado,
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
    genero: '',
    contactoEmergenciaNombre: '',
    contactoEmergenciaTelefono: '',
    contactoEmergenciaParentesco: '',
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
    documentosChecklist: [],
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
    genero: dto.genero ?? '',
    contactoEmergenciaNombre: dto.contactoEmergenciaNombre ?? '',
    contactoEmergenciaTelefono: dto.contactoEmergenciaTelefono ?? '',
    contactoEmergenciaParentesco: dto.contactoEmergenciaParentesco ?? '',
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
    documentosChecklist: toChecklistForm(dto.documentosChecklist),
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
    genero: form.genero as Genero,
    contactoEmergenciaNombre: form.contactoEmergenciaNombre.trim(),
    contactoEmergenciaTelefono: form.contactoEmergenciaTelefono.trim(),
    contactoEmergenciaParentesco: emptyToNull(form.contactoEmergenciaParentesco),
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
    documentosChecklist: toChecklistForm(form.documentosChecklist).map((item) => ({
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
