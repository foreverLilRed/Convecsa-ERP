import { apiRequest } from '@/services/http'
import type {
  DashboardAsistencia,
  DashboardContratos,
  DashboardNomina,
  DashboardPersonas,
} from '@/types/dashboard'

export function obtenerDashboardPersonas(): Promise<DashboardPersonas> {
  return apiRequest<DashboardPersonas>('/api/dashboard/personas')
}

export function obtenerDashboardContratos(): Promise<DashboardContratos> {
  return apiRequest<DashboardContratos>('/api/dashboard/contratos')
}

export function obtenerDashboardNomina(): Promise<DashboardNomina> {
  return apiRequest<DashboardNomina>('/api/dashboard/nomina')
}

export function obtenerDashboardAsistencia(): Promise<DashboardAsistencia> {
  return apiRequest<DashboardAsistencia>('/api/dashboard/asistencia')
}
