export type TipoDocumento = 'DNI' | 'CE' | 'PASAPORTE'

export type GradoInstruccion = 'PRIMARIA' | 'SECUNDARIA' | 'TECNICO' | 'UNIVERSITARIO' | 'POSGRADO'

export type Banco = 'BCP' | 'BBVA' | 'INTERBANK' | 'SCOTIABANK' | 'OTROS'

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
  }
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
