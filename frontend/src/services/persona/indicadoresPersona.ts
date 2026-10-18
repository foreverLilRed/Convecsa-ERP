import {
  DOCUMENTOS_CHECKLIST,
  type CuentaBancariaDTO,
  type ExperienciaLaboralDTO,
  type PersonaResumenDTO,
} from '@/types/persona'

export type IndicadorSeccion = 'COMPLETO' | 'INCOMPLETO' | 'VACIO'

export const SECCIONES_INDICADOR: { key: string; label: string }[] = [
  { key: 'identidad', label: 'Identidad' },
  { key: 'experiencia', label: 'Experiencia laboral' },
  { key: 'banco', label: 'Banco y pensiones' },
  { key: 'salud', label: 'Salud y SOMA' },
  { key: 'documentos', label: 'Checklist de documentos' },
]

export function calcularIndicadores(persona: PersonaResumenDTO): IndicadorSeccion[] {
  return [
    indicadorIdentidad(persona),
    indicadorExperiencia(persona.experienciasLaborales ?? []),
    indicadorBanco(persona.cuentasBancarias ?? []),
    indicadorSalud(persona),
    indicadorChecklist(persona.documentosChecklist ?? []),
  ]
}

function indicadorIdentidad(persona: PersonaResumenDTO): IndicadorSeccion {
  const campos: boolean[] = [
    present(persona.apellidos),
    present(persona.nombres),
    present(persona.tipoDocumento),
    present(persona.numeroDocumento),
    present(persona.fechaNacimiento),
    (persona.celulares ?? []).length > 0,
    present(persona.correo),
    present(persona.gradoInstruccion),
    present(persona.direccion),
    present(persona.departamento),
    present(persona.provincia),
    present(persona.distrito),
  ]
  if (persona.tipoDocumento === 'DNI') {
    campos.push(present(persona.fechaEmisionDni), present(persona.fechaCaducidadDni))
  }
  const llenos = campos.filter(Boolean).length
  if (llenos === 0) {
    return 'VACIO'
  }
  return llenos === campos.length ? 'COMPLETO' : 'INCOMPLETO'
}

function indicadorExperiencia(experiencias: ExperienciaLaboralDTO[]): IndicadorSeccion {
  if (experiencias.length === 0) {
    return 'VACIO'
  }
  const todasCompletas = experiencias.every((experiencia) => {
    if (!present(experiencia.organizacion) || !present(experiencia.cargo) || !present(experiencia.fechaInicioLaboral)) {
      return false
    }
    if (!experiencia.fechaFin) {
      return true
    }
    return experiencia.fechaFin >= experiencia.fechaInicioLaboral
  })
  return todasCompletas ? 'COMPLETO' : 'INCOMPLETO'
}

function indicadorBanco(cuentas: CuentaBancariaDTO[]): IndicadorSeccion {
  if (cuentas.length === 0) {
    return 'VACIO'
  }
  const todasCompletas = cuentas.every(
    (cuenta) => present(cuenta.banco) && present(cuenta.numeroCuenta) && cuenta.cci.trim().length === 20,
  )
  const principales = cuentas.filter((cuenta) => cuenta.principal).length
  return todasCompletas && principales === 1 ? 'COMPLETO' : 'INCOMPLETO'
}

function indicadorSalud(persona: PersonaResumenDTO): IndicadorSeccion {
  const fecha = present(persona.fechaVigenciaEmo)
  const aptitud = present(persona.aptitudMedica)
  if (!fecha && !aptitud) {
    return 'VACIO'
  }
  return fecha && aptitud ? 'COMPLETO' : 'INCOMPLETO'
}

function indicadorChecklist(documentos: PersonaResumenDTO['documentosChecklist']): IndicadorSeccion {
  if (documentos.length === 0) {
    return 'VACIO'
  }
  const catalogo = DOCUMENTOS_CHECKLIST.length
  const conformes = documentos.filter((documento) => documento.estado === 'CONFORME').length
  if (documentos.length === catalogo && conformes === catalogo) {
    return 'COMPLETO'
  }
  return 'INCOMPLETO'
}

function present(value: string | null | undefined): boolean {
  return Boolean(value && value.trim())
}
