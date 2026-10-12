export type RolUsuario = 'SUPERADMIN' | 'ADMIN' | 'SUPERVISOR'

export interface RequestLogin {
  username: string
  password: string
}

export interface AuthSession {
  token: string
  username: string
  correo: string
  rol: RolUsuario
}
