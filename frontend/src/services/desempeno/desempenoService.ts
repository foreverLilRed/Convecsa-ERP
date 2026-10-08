import { apiRequest } from '@/services/http'
import type {
  ColaboradorDesempeno,
  ColaboradorDesempenoDetalle,
  DesempenoListQuery,
  EvaluacionDesempenoRequest,
} from '@/types/desempeno'
import type { PagedResponse } from '@/types/persona'

export function listarColaboradoresDesempeno(
  query: DesempenoListQuery,
): Promise<PagedResponse<ColaboradorDesempeno>> {
  const params = new URLSearchParams()
  params.set('periodo', query.periodo)
  if (query.page !== undefined) {
    params.set('page', String(query.page))
  }
  if (query.size !== undefined) {
    params.set('size', String(query.size))
  }
  if (query.q?.trim()) {
    params.set('q', query.q.trim())
  }
  return apiRequest<PagedResponse<ColaboradorDesempeno>>(
    `/api/gestion-humana/desempeno/colaboradores?${params.toString()}`,
  )
}

export function obtenerColaboradorDesempeno(
  personaId: number,
  periodo: string,
): Promise<ColaboradorDesempenoDetalle> {
  const params = new URLSearchParams()
  params.set('periodo', periodo)
  return apiRequest<ColaboradorDesempenoDetalle>(
    `/api/gestion-humana/desempeno/colaboradores/${personaId}?${params.toString()}`,
  )
}

export function guardarEvaluacionDesempeno(
  personaId: number,
  request: EvaluacionDesempenoRequest,
): Promise<ColaboradorDesempenoDetalle> {
  return apiRequest<ColaboradorDesempenoDetalle>(
    `/api/gestion-humana/desempeno/colaboradores/${personaId}`,
    {
      method: 'PUT',
      body: JSON.stringify(request),
    },
  )
}
