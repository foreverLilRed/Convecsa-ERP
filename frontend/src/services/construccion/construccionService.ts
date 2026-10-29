import { apiRequest, openAuthenticatedDocument } from '@/services/http'
import type {
  BoletaCc,
  BoletaCcResumen,
  ConstruccionCatalogos,
  PeriodoCc,
  PeriodoCcRequest,
  PersonalObra,
  Proyecto,
  ProyectoRequest,
  TareoCcDia,
  TareoCcGrilla,
} from '@/types/construccion'

export type TareoCcGuardar = {
  dias: TareoCcDia[]
}

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export function obtenerCatalogosConstruccion(): Promise<ConstruccionCatalogos> {
  return apiRequest<ConstruccionCatalogos>('/api/proyectos/catalogos')
}

export function listarProyectos(activo?: boolean): Promise<Proyecto[]> {
  const params = activo === undefined ? '' : `?activo=${activo}`
  return apiRequest<Proyecto[]>(`/api/proyectos${params}`)
}

export function crearProyecto(request: ProyectoRequest): Promise<Proyecto> {
  return apiRequest<Proyecto>('/api/proyectos', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function obtenerProyecto(id: number): Promise<Proyecto> {
  return apiRequest<Proyecto>(`/api/proyectos/${id}`)
}

export function listarPersonalObra(proyectoId: number): Promise<PersonalObra[]> {
  return apiRequest<PersonalObra[]>(`/api/proyectos/${proyectoId}/personal`)
}

export function listarPeriodosCc(proyectoId: number): Promise<PeriodoCc[]> {
  return apiRequest<PeriodoCc[]>(`/api/proyectos/${proyectoId}/periodos`)
}

export function crearPeriodoCc(proyectoId: number, request: PeriodoCcRequest): Promise<PeriodoCc> {
  return apiRequest<PeriodoCc>(`/api/proyectos/${proyectoId}/periodos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function cerrarPeriodoCc(proyectoId: number, periodoId: number): Promise<PeriodoCc> {
  return apiRequest<PeriodoCc>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/cerrar`, {
    method: 'POST',
  })
}

export function obtenerTareo(proyectoId: number, periodoId: number): Promise<TareoCcGrilla> {
  return apiRequest<TareoCcGrilla>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/tareo`)
}

export function guardarTareo(
  proyectoId: number,
  periodoId: number,
  request: TareoCcGuardar,
): Promise<TareoCcGrilla> {
  return apiRequest<TareoCcGrilla>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/tareo`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function liquidarPeriodoCc(proyectoId: number, periodoId: number): Promise<BoletaCcResumen[]> {
  return apiRequest<BoletaCcResumen[]>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/liquidar`, {
    method: 'POST',
  })
}

export function listarBoletasCc(proyectoId: number, periodoId: number): Promise<BoletaCcResumen[]> {
  return apiRequest<BoletaCcResumen[]>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas`)
}

export function obtenerBoletaCc(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): Promise<BoletaCc> {
  return apiRequest<BoletaCc>(`/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/${boletaId}`)
}

export function boletaCcDocumentoUrl(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): string {
  return `${API_BASE_URL}/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/${boletaId}/documento`
}

export function boletaCcPdfUrl(proyectoId: number, periodoId: number, boletaId: number): string {
  return `${API_BASE_URL}/api/proyectos/${proyectoId}/periodos/${periodoId}/boletas/${boletaId}/pdf`
}

export function abrirDocumentoBoletaCc(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): Promise<void> {
  return openAuthenticatedDocument(boletaCcDocumentoUrl(proyectoId, periodoId, boletaId))
}

export function abrirPdfBoletaCc(
  proyectoId: number,
  periodoId: number,
  boletaId: number,
): Promise<void> {
  return openAuthenticatedDocument(boletaCcPdfUrl(proyectoId, periodoId, boletaId))
}
