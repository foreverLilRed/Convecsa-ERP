import type { AuthSession, RequestLogin } from '@/types/auth'

const SESSION_KEY = 'convecsa.auth.session'
const MOCK_LATENCY_MS = 2000

// TODO: reemplazar el mock por POST /auth/login cuando exista backend.
// Firma prevista: login(request: RequestLogin) => Promise<AuthSession>

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

function setSession(session: AuthSession): void {
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

function clearSession(): void {
  sessionStorage.removeItem(SESSION_KEY)
}

export function getSession(): AuthSession | null {
  const raw = sessionStorage.getItem(SESSION_KEY)
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw) as AuthSession
    if (!parsed?.username) {
      clearSession()
      return null
    }
    return parsed
  } catch {
    clearSession()
    return null
  }
}

export function isAuthenticated(): boolean {
  return getSession() !== null
}

export async function login(request: RequestLogin): Promise<AuthSession> {
  await delay(MOCK_LATENCY_MS)

  const username = request.username.trim()
  const password = request.password.trim()

  if (!username || !password) {
    throw new Error('Ingrese su usuario y contraseña.')
  }

  const session: AuthSession = { username }
  setSession(session)
  return session
}

export function logout(): void {
  clearSession()
}
