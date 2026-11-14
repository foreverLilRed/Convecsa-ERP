export type RolUsuario = 'SUPERADMIN' | 'ADMIN' | 'SUPERVISOR' | 'RESIDENTE' | 'ALMACEN' | 'INGENIERO_SOMA'

export interface RequestLogin {
  username: string
  password: string
}

export interface AuthSession {
  token: string
  username: string
  correo: string
  rol: RolUsuario
  usuarioId?: number
}

export function esGerencia(rol?: RolUsuario | null): boolean {
  return rol === 'SUPERADMIN' || rol === 'ADMIN'
}

export function esAlmacen(rol?: RolUsuario | null): boolean {
  return rol === 'ALMACEN'
}

export function puedeSomaCorporativo(rol?: RolUsuario | null): boolean {
  return esGerencia(rol) || rol === 'INGENIERO_SOMA'
}
