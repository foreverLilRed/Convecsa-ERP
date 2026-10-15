import { apiRequest } from '@/services/http'
import type {
  AsistenciaConfig,
  AsistenciaConceptoVariable,
  AsistenciaConceptoVariableRequest,
  AsistenciaFeriado,
  AsistenciaFeriadoRequest,
  AsistenciaPlanilla,
  AsistenciaRegistroUpdate,
} from '@/types/asistencia'

export function obtenerConfigAsistencia(): Promise<AsistenciaConfig> {
  return apiRequest<AsistenciaConfig>('/api/gestion-humana/asistencia/config')
}

export function guardarConfigAsistencia(diasSemana: number[]): Promise<AsistenciaConfig> {
  return apiRequest<AsistenciaConfig>('/api/gestion-humana/asistencia/config', {
    method: 'PUT',
    body: JSON.stringify({ diasSemana }),
  })
}

export function listarFeriados(anio: number): Promise<AsistenciaFeriado[]> {
  return apiRequest<AsistenciaFeriado[]>(`/api/gestion-humana/asistencia/feriados?anio=${anio}`)
}

export function crearFeriado(request: AsistenciaFeriadoRequest): Promise<AsistenciaFeriado> {
  return apiRequest<AsistenciaFeriado>('/api/gestion-humana/asistencia/feriados', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function eliminarFeriado(id: number): Promise<void> {
  return apiRequest<void>(`/api/gestion-humana/asistencia/feriados/${id}`, {
    method: 'DELETE',
  })
}

export function obtenerPlanilla(anio: number, mes: number): Promise<AsistenciaPlanilla> {
  const params = new URLSearchParams({
    anio: String(anio),
    mes: String(mes),
  })
  return apiRequest<AsistenciaPlanilla>(`/api/gestion-humana/asistencia/planilla?${params}`)
}

export function generarPlanilla(anio: number, mes: number): Promise<AsistenciaPlanilla> {
  return apiRequest<AsistenciaPlanilla>('/api/gestion-humana/asistencia/planilla/generar', {
    method: 'POST',
    body: JSON.stringify({ anio, mes }),
  })
}

export function actualizarPlanilla(items: AsistenciaRegistroUpdate[]): Promise<AsistenciaPlanilla> {
  return apiRequest<AsistenciaPlanilla>('/api/gestion-humana/asistencia/planilla', {
    method: 'PUT',
    body: JSON.stringify({ items }),
  })
}

export function listarConceptosVariables(anio: number, mes: number): Promise<AsistenciaConceptoVariable[]> {
  return apiRequest<AsistenciaConceptoVariable[]>(
    `/api/gestion-humana/asistencia/conceptos-variables?anio=${anio}&mes=${mes}`,
  )
}

export function registrarConceptoVariable(
  anio: number,
  mes: number,
  request: AsistenciaConceptoVariableRequest,
): Promise<AsistenciaConceptoVariable> {
  return apiRequest<AsistenciaConceptoVariable>(
    `/api/gestion-humana/asistencia/conceptos-variables?anio=${anio}&mes=${mes}`,
    { method: 'POST', body: JSON.stringify(request) },
  )
}

export function eliminarConceptoVariable(id: number): Promise<void> {
  return apiRequest<void>(`/api/gestion-humana/asistencia/conceptos-variables/${id}`, { method: 'DELETE' })
}
