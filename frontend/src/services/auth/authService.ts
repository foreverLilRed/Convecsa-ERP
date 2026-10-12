import { apiRequest } from '@/services/http'
import type { AuthSession, RequestLogin } from '@/types/auth'

import { clearSession, getSession, isAuthenticated, setSession } from './session'

export { getSession, isAuthenticated }

export async function login(request: RequestLogin, rememberMe = false): Promise<AuthSession> {
  const username = request.username.trim()
  const password = request.password

  if (!username || !password) {
    throw new Error('Ingrese su usuario o correo y contraseña.')
  }

  const session = await apiRequest<AuthSession>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })

  setSession(session, rememberMe)
  return session
}

export function logout(): void {
  clearSession()
}
