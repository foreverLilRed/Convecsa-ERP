import { apiRequest } from '@/services/http'
import type { PersonaDTO, PersonaRequest } from '@/types/persona'

export function crearPersona(request: PersonaRequest): Promise<PersonaDTO> {
  return apiRequest<PersonaDTO>('/api/gestion-humana/personas', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}
