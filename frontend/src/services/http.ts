const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  readonly status: number
  readonly errors?: Record<string, string>

  constructor(message: string, status: number, errors?: Record<string, string>) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.errors = errors
  }
}

type ApiErrorBody = {
  status?: number
  message?: string
  errors?: Record<string, string>
}

function firstErrorMessage(errors: Record<string, string>): string {
  const messages = Object.values(errors).filter(Boolean)
  return messages[0] ?? 'Solicitud inválida'
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        ...init?.headers,
      },
    })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor.', 0)
  }

  if (!response.ok) {
    let body: ApiErrorBody | null = null
    try {
      body = (await response.json()) as ApiErrorBody
    } catch {
      body = null
    }

    const message = body?.errors
      ? firstErrorMessage(body.errors)
      : (body?.message ?? `Error ${response.status}`)
    throw new ApiError(message, response.status, body?.errors)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}
