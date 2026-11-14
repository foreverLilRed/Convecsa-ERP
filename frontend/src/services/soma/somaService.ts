import { apiRequest, downloadAuthenticatedFile } from '@/services/http'
import type {
  EstadoItemEpp,
  SomaCapacitacion,
  SomaCapacitacionAsistente,
  SomaCapacitacionRequest,
  SomaCharlaAsistente,
  SomaCharlaAts,
  SomaCharlaAtsRequest,
  EstadoHallazgoSoma,
  SomaHallazgo,
  SomaHallazgoLevantarRequest,
  SomaHallazgoRequest,
  SomaInspeccion,
  SomaInspeccionRequest,
  SomaPetar,
  SomaPetarChecklistItem,
  SomaPetarFaltanteTareo,
  SomaPetarRequest,
  SomaPreUso,
  SomaPreUsoBloqueo,
  SomaPreUsoRequest,
  TipoTrabajoPetar,
  SomaEmo,
  SomaEmoRequest,
  SomaEntregaEpp,
  SomaEntregaEppRequest,
  SomaEppMaterial,
  SomaHabilitacion,
  SomaIpercFila,
  SomaIpercFilaRequest,
  SomaResumenObra,
  SomaSctrNominaGuardarRequest,
  SomaSctrPoliza,
  SomaSctrPolizaRequest,
  SomaTrabajadorObra,
} from '@/types/soma'

export function obtenerResumenSoma(proyectoId: number): Promise<SomaResumenObra> {
  return apiRequest<SomaResumenObra>(`/api/proyectos/${proyectoId}/soma/resumen`)
}

export function listarHabilitacionObra(proyectoId: number, fecha?: string): Promise<SomaHabilitacion> {
  const params = new URLSearchParams()
  if (fecha) {
    params.set('fecha', fecha)
  }
  const qs = params.toString()
  return apiRequest<SomaHabilitacion>(`/api/proyectos/${proyectoId}/soma/habilitacion${qs ? `?${qs}` : ''}`)
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

export function listarIpercObra(
  proyectoId: number,
  partidaId?: number | null,
  incluirInactivos?: boolean,
): Promise<SomaIpercFila[]> {
  const params = new URLSearchParams()
  if (partidaId) {
    params.set('partidaId', String(partidaId))
  }
  if (incluirInactivos) {
    params.set('incluirInactivos', 'true')
  }
  const qs = params.toString()
  return apiRequest<SomaIpercFila[]>(`/api/proyectos/${proyectoId}/soma/iperc${qs ? `?${qs}` : ''}`)
}

export function crearIpercObra(proyectoId: number, request: SomaIpercFilaRequest): Promise<SomaIpercFila> {
  return apiRequest<SomaIpercFila>(`/api/proyectos/${proyectoId}/soma/iperc`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function actualizarIpercObra(
  proyectoId: number,
  filaId: number,
  request: SomaIpercFilaRequest,
): Promise<SomaIpercFila> {
  return apiRequest<SomaIpercFila>(`/api/proyectos/${proyectoId}/soma/iperc/${filaId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function desactivarIpercObra(proyectoId: number, filaId: number): Promise<SomaIpercFila> {
  return apiRequest<SomaIpercFila>(`/api/proyectos/${proyectoId}/soma/iperc/${filaId}/desactivar`, {
    method: 'POST',
  })
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

export function listarCharlasAtsObra(proyectoId: number, fecha?: string | null): Promise<SomaCharlaAts[]> {
  const params = new URLSearchParams()
  if (fecha) {
    params.set('fecha', fecha)
  }
  const qs = params.toString()
  return apiRequest<SomaCharlaAts[]>(`/api/proyectos/${proyectoId}/soma/charlas-ats${qs ? `?${qs}` : ''}`)
}

export function crearCharlaAtsObra(proyectoId: number, request: SomaCharlaAtsRequest): Promise<SomaCharlaAts> {
  return apiRequest<SomaCharlaAts>(`/api/proyectos/${proyectoId}/soma/charlas-ats`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function listarPetarObra(proyectoId: number, fecha?: string | null): Promise<SomaPetar[]> {
  const params = new URLSearchParams()
  if (fecha) {
    params.set('fecha', fecha)
  }
  const qs = params.toString()
  return apiRequest<SomaPetar[]>(`/api/proyectos/${proyectoId}/soma/petar${qs ? `?${qs}` : ''}`)
}

export function crearPetarObra(proyectoId: number, request: SomaPetarRequest): Promise<SomaPetar> {
  return apiRequest<SomaPetar>(`/api/proyectos/${proyectoId}/soma/petar`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function actualizarPetarObra(
  proyectoId: number,
  petarId: number,
  request: SomaPetarRequest,
): Promise<SomaPetar> {
  return apiRequest<SomaPetar>(`/api/proyectos/${proyectoId}/soma/petar/${petarId}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function autorizarPetarObra(proyectoId: number, petarId: number): Promise<SomaPetar> {
  return apiRequest<SomaPetar>(`/api/proyectos/${proyectoId}/soma/petar/${petarId}/autorizar`, {
    method: 'POST',
  })
}

export function cerrarPetarObra(proyectoId: number, petarId: number): Promise<SomaPetar> {
  return apiRequest<SomaPetar>(`/api/proyectos/${proyectoId}/soma/petar/${petarId}/cerrar`, {
    method: 'POST',
  })
}

export function suspenderPetarObra(proyectoId: number, petarId: number): Promise<SomaPetar> {
  return apiRequest<SomaPetar>(`/api/proyectos/${proyectoId}/soma/petar/${petarId}/suspender`, {
    method: 'POST',
  })
}

export function reanudarPetarObra(proyectoId: number, petarId: number): Promise<SomaPetar> {
  return apiRequest<SomaPetar>(`/api/proyectos/${proyectoId}/soma/petar/${petarId}/reanudar`, {
    method: 'POST',
  })
}

export function checklistPetarObra(
  proyectoId: number,
  tipo: TipoTrabajoPetar,
): Promise<SomaPetarChecklistItem[]> {
  return apiRequest<SomaPetarChecklistItem[]>(`/api/proyectos/${proyectoId}/soma/petar/checklist/${tipo}`)
}

export function faltantesTareoPetar(proyectoId: number): Promise<SomaPetarFaltanteTareo[]> {
  return apiRequest<SomaPetarFaltanteTareo[]>(`/api/proyectos/${proyectoId}/soma/petar/faltantes-tareo`)
}

export function precargarCuadrillaCharlaAts(
  proyectoId: number,
  cuadrillaId: number,
  fecha?: string | null,
): Promise<SomaCharlaAsistente[]> {
  const params = new URLSearchParams({ cuadrillaId: String(cuadrillaId) })
  if (fecha) {
    params.set('fecha', fecha)
  }
  return apiRequest<SomaCharlaAsistente[]>(
    `/api/proyectos/${proyectoId}/soma/charlas-ats/precargar-cuadrilla?${params}`,
  )
}

export function listarInspeccionesObra(proyectoId: number): Promise<SomaInspeccion[]> {
  return apiRequest<SomaInspeccion[]>(`/api/proyectos/${proyectoId}/soma/inspecciones`)
}

export function crearInspeccionObra(proyectoId: number, request: SomaInspeccionRequest): Promise<SomaInspeccion> {
  return apiRequest<SomaInspeccion>(`/api/proyectos/${proyectoId}/soma/inspecciones`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function listarHallazgosObra(
  proyectoId: number,
  estado?: EstadoHallazgoSoma | null,
  vencidos?: boolean | null,
): Promise<SomaHallazgo[]> {
  const params = new URLSearchParams()
  if (estado) {
    params.set('estado', estado)
  }
  if (vencidos != null) {
    params.set('vencidos', String(vencidos))
  }
  const qs = params.toString()
  return apiRequest<SomaHallazgo[]>(`/api/proyectos/${proyectoId}/soma/hallazgos${qs ? `?${qs}` : ''}`)
}

export function crearHallazgoObra(proyectoId: number, request: SomaHallazgoRequest): Promise<SomaHallazgo> {
  return apiRequest<SomaHallazgo>(`/api/proyectos/${proyectoId}/soma/hallazgos`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function marcarHallazgoEnProcesoObra(proyectoId: number, hallazgoId: number): Promise<SomaHallazgo> {
  return apiRequest<SomaHallazgo>(`/api/proyectos/${proyectoId}/soma/hallazgos/${hallazgoId}/en-proceso`, {
    method: 'PUT',
  })
}

export function levantarHallazgoObra(
  proyectoId: number,
  hallazgoId: number,
  request: SomaHallazgoLevantarRequest,
): Promise<SomaHallazgo> {
  return apiRequest<SomaHallazgo>(`/api/proyectos/${proyectoId}/soma/hallazgos/${hallazgoId}/levantar`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export function listarPreUsoObra(proyectoId: number): Promise<SomaPreUso[]> {
  return apiRequest<SomaPreUso[]>(`/api/proyectos/${proyectoId}/soma/equipos-preuso`)
}

export function crearPreUsoObra(proyectoId: number, request: SomaPreUsoRequest): Promise<SomaPreUso> {
  return apiRequest<SomaPreUso>(`/api/proyectos/${proyectoId}/soma/equipos-preuso`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export function listarBloqueosPreUso(proyectoId: number, fecha: string): Promise<SomaPreUsoBloqueo[]> {
  const params = new URLSearchParams({ fecha })
  return apiRequest<SomaPreUsoBloqueo[]>(
    `/api/proyectos/${proyectoId}/soma/equipos-preuso/bloqueos?${params}`,
  )
}
