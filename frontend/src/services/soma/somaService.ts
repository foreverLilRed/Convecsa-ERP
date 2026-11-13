import { apiRequest, downloadAuthenticatedFile } from '@/services/http'
import type {
  EstadoItemEpp,
  SomaCapacitacion,
  SomaCapacitacionAsistente,
  SomaCapacitacionRequest,
  SomaEmo,
  SomaEmoRequest,
  SomaEntregaEpp,
  SomaEntregaEppRequest,
  SomaEppMaterial,
  SomaResumenObra,
  SomaSctrNominaGuardarRequest,
  SomaSctrPoliza,
  SomaSctrPolizaRequest,
  SomaTrabajadorObra,
} from '@/types/soma'

export function obtenerResumenSoma(proyectoId: number): Promise<SomaResumenObra> {
  return apiRequest<SomaResumenObra>(`/api/proyectos/${proyectoId}/soma/resumen`)
}

export function listarEmosObra(proyectoId: number): Promise<SomaEmo[]> {
  return apiRequest<SomaEmo[]>(`/api/proyectos/${proyectoId}/soma/emos`)
}

export function registrarEmoObra(proyectoId: number, request: SomaEmoRequest): Promise<SomaEmo> {
  return apiRequest<SomaEmo>(`/api/proyectos/${proyectoId}/soma/emos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function listarPolizasSctr(proyectoId: number): Promise<SomaSctrPoliza[]> {
  return apiRequest<SomaSctrPoliza[]>(`/api/proyectos/${proyectoId}/soma/sctr`)
}

export function crearPolizaSctr(proyectoId: number, request: SomaSctrPolizaRequest): Promise<SomaSctrPoliza> {
  return apiRequest<SomaSctrPoliza>(`/api/proyectos/${proyectoId}/soma/sctr`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function guardarNominaSctr(
  proyectoId: number,
  polizaId: number,
  request: SomaSctrNominaGuardarRequest,
): Promise<SomaSctrPoliza> {
  return apiRequest<SomaSctrPoliza>(`/api/proyectos/${proyectoId}/soma/sctr/${polizaId}/nomina`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function anularPolizaSctr(proyectoId: number, polizaId: number): Promise<SomaSctrPoliza> {
  return apiRequest<SomaSctrPoliza>(`/api/proyectos/${proyectoId}/soma/sctr/${polizaId}/anular`, {
    method: 'POST',
  })
}

export function sugerirNominaSctr(proyectoId: number, anio: number, mes: number): Promise<SomaTrabajadorObra[]> {
  const params = new URLSearchParams({ anio: String(anio), mes: String(mes) })
  return apiRequest<SomaTrabajadorObra[]>(`/api/proyectos/${proyectoId}/soma/sctr/sugerir-nomina?${params}`)
}

export function listarEntregasEpp(proyectoId: number): Promise<SomaEntregaEpp[]> {
  return apiRequest<SomaEntregaEpp[]>(`/api/proyectos/${proyectoId}/soma/epps`)
}

export function crearEntregaEpp(proyectoId: number, request: SomaEntregaEppRequest): Promise<SomaEntregaEpp> {
  return apiRequest<SomaEntregaEpp>(`/api/proyectos/${proyectoId}/soma/epps`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function cambiarEstadoItemEpp(
  proyectoId: number,
  itemId: number,
  estado: EstadoItemEpp,
): Promise<SomaEntregaEpp> {
  return apiRequest<SomaEntregaEpp>(`/api/proyectos/${proyectoId}/soma/epps/items/${itemId}/estado`, {
    method: 'PUT',
    body: JSON.stringify({ estado }),
  })
}

export function listarMaterialesEpp(proyectoId: number): Promise<SomaEppMaterial[]> {
  return apiRequest<SomaEppMaterial[]>(`/api/proyectos/${proyectoId}/soma/epps/materiales`)
}

export function descargarKardexEpp(proyectoId: number, personaId: number, filename: string): Promise<void> {
  return downloadAuthenticatedFile(`/api/proyectos/${proyectoId}/soma/epps/kardex/${personaId}/pdf`, filename)
}

export function listarCapacitacionesObra(proyectoId: number): Promise<SomaCapacitacion[]> {
  return apiRequest<SomaCapacitacion[]>(`/api/proyectos/${proyectoId}/soma/capacitaciones`)
}

export function crearCapacitacionObra(
  proyectoId: number,
  request: SomaCapacitacionRequest,
): Promise<SomaCapacitacion> {
  return apiRequest<SomaCapacitacion>(`/api/proyectos/${proyectoId}/soma/capacitaciones`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function precargarCuadrillaCapacitacion(
  proyectoId: number,
  cuadrillaId: number,
): Promise<SomaCapacitacionAsistente[]> {
  const params = new URLSearchParams({ cuadrillaId: String(cuadrillaId) })
  return apiRequest<SomaCapacitacionAsistente[]>(
    `/api/proyectos/${proyectoId}/soma/capacitaciones/precargar-cuadrilla?${params}`,
  )
}

export function listarCapacitacionesCorporativo(anio: number): Promise<SomaCapacitacion[]> {
  const params = new URLSearchParams({ anio: String(anio) })
  return apiRequest<SomaCapacitacion[]>(`/api/soma/capacitaciones?${params}`)
}
