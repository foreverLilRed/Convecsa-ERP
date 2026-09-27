export type TipoDocumento = 'DNI' | 'CE' | 'PASAPORTE'

export type CuentaBancariaForm = {
  banco: string
  numeroCuenta: string
  cci: string
  principal: boolean
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
  gradoInstruccion: string
  direccion: string
  departamento: string
  provincia: string
  distrito: string
  residencia: string
  cuentasBancarias: CuentaBancariaForm[]
  fotoUrl: string | null
}

export const TIPOS_DOCUMENTO: TipoDocumento[] = ['DNI', 'CE', 'PASAPORTE']

export const GRADOS_INSTRUCCION = [
  'Primaria',
  'Secundaria',
  'Técnico',
  'Universitario',
  'Posgrado',
] as const

export const BANCOS = ['BCP', 'BBVA', 'Interbank', 'Scotiabank', 'Otros'] as const

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
    fotoUrl: null,
  }
}

export const TIPOS_IMAGEN = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'] as const
