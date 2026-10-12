import type { PagedResponse } from '@/types/persona'
import type { PlantillaOnboarding } from '@/types/onboarding'

export type TipoCese = 'RENUNCIA' | 'VENCIMIENTO_PLAZO' | 'MUTUO_ACUERDO' | 'DESPIDO'

export type LiquidacionPreview = {
  sueldo: number
  cts: number
  gratificacion: number
  vacaciones: number
  total: number
  tiempoServicio: string
  fechaInicio: string
  fechaCese: string
}

export type Cese = {
  id: number
  personaId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  contratoId: number
  fechaCese: string
  tipoCese: TipoCese
  motivo: string | null
  liquidacion: LiquidacionPreview
  createdAt: string
}

export type CeseRequest = {
  personaId: number
  fechaCese: string
  tipoCese: TipoCese
  motivo?: string
  cts: number
  gratificacion: number
  vacaciones: number
}

export type PlantillaOffboarding = PlantillaOnboarding

export type PagedCeses = PagedResponse<Cese>

export const TIPOS_CESE: { value: TipoCese; label: string }[] = [
  { value: 'RENUNCIA', label: 'Renuncia voluntaria' },
  { value: 'VENCIMIENTO_PLAZO', label: 'Vencimiento de plazo' },
  { value: 'MUTUO_ACUERDO', label: 'Mutuo acuerdo' },
  { value: 'DESPIDO', label: 'Despido' },
]
