import type { AuthSession } from '@/types/auth'

export const SESSION_KEY = 'convecsa.auth.session'

function parseSession(raw: string | null): AuthSession | null {
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw) as AuthSession
    if (!parsed?.token || !parsed.username) {
      return null
    }
    return parsed
  } catch {
    return null
  }
}

export function getSession(): AuthSession | null {
  return parseSession(sessionStorage.getItem(SESSION_KEY)) ?? parseSession(localStorage.getItem(SESSION_KEY))
}

export function setSession(session: AuthSession, rememberMe = false): void {
  clearSession()
  const storage = rememberMe ? localStorage : sessionStorage
  storage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession(): void {
  sessionStorage.removeItem(SESSION_KEY)
  localStorage.removeItem(SESSION_KEY)
}

export function isAuthenticated(): boolean {
  return getSession() !== null
}
