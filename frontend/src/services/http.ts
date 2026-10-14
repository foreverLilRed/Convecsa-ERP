import { clearSession, getSession } from '@/services/auth/session'

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

function authHeaders(path: string): Record<string, string> {
  if (path.startsWith('/api/auth/login')) {
    return {}
  }
  const session = getSession()
  return session?.token ? { Authorization: `Bearer ${session.token}` } : {}
}

function handleUnauthorized(path: string): void {
  if (path.startsWith('/api/auth/login')) {
    return
  }
  clearSession()
  const pathname = window.location.pathname
  if (pathname !== '/login' && !pathname.startsWith('/invitar/')) {
    window.location.assign('/login')
  }
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        ...authHeaders(path),
        ...init?.headers,
      },
    })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor.', 0)
  }

  if (response.status === 401) {
    handleUnauthorized(path)
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

export async function openAuthenticatedDocument(url: string): Promise<void> {
  const session = getSession()
  const headers: Record<string, string> = {}
  if (session?.token) {
    headers.Authorization = `Bearer ${session.token}`
  }

  let response: Response
  try {
    response = await fetch(url, { headers })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor.', 0)
  }

  if (response.status === 401) {
    handleUnauthorized(url)
    throw new ApiError('No autenticado', 401)
  }

  if (!response.ok) {
    throw new ApiError(`Error ${response.status}`, response.status)
  }

  const blob = await response.blob()
  const objectUrl = URL.createObjectURL(blob)
  window.open(objectUrl, '_blank', 'noopener,noreferrer')
}

export async function downloadAuthenticatedFile(path: string, filename: string): Promise<void> {
  const session = getSession()
  const headers: Record<string, string> = {}
  if (session?.token) {
    headers.Authorization = `Bearer ${session.token}`
  }

  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { headers })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor.', 0)
  }

  if (response.status === 401) {
    handleUnauthorized(path)
  }
  if (!response.ok) {
    throw new ApiError(`Error ${response.status}`, response.status)
  }

  const blob = await response.blob()
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = filename
  link.click()
  URL.revokeObjectURL(objectUrl)
}
