export type NavChild = {
  name: string
  label: string
  path: string
}

export type NavLink = {
  kind: 'link'
  name: string
  label: string
  path: string
  icon: string
}

export type NavGroup = {
  kind: 'group'
  id: string
  label: string
  icon: string
  children: NavChild[]
}

export type NavItem = NavLink | NavGroup

export const navItems: NavItem[] = [
  {
    kind: 'link',
    name: 'dashboard',
    label: 'Dashboard',
    path: '/',
    icon: 'home',
  },
  {
    kind: 'group',
    id: 'gestion-humana',
    label: 'Gestión Humana',
    icon: 'users',
    children: [
      { name: 'gh-personal', label: 'Personal', path: '/gestion-humana/personal' },
      { name: 'gh-contratos', label: 'Contratos', path: '/gestion-humana/contratos' },
      { name: 'gh-plantillas-contrato', label: 'Plantillas de Contrato', path: '/gestion-humana/plantillas-contrato' },
      { name: 'gh-onboarding', label: 'Onboarding', path: '/gestion-humana/onboarding' },
      { name: 'gh-nominas', label: 'Nóminas', path: '/gestion-humana/nominas' },
      { name: 'gh-asistencia', label: 'Asistencia', path: '/gestion-humana/asistencia' },
      { name: 'gh-kpis', label: 'KPIs', path: '/gestion-humana/kpis' },
      { name: 'gh-capacitaciones', label: 'Capacitaciones', path: '/gestion-humana/capacitaciones' },
      { name: 'gh-portal-boletas', label: 'Portal Boletas', path: '/gestion-humana/portal-boletas' },
      { name: 'gh-cumplimiento-sunafil', label: 'Cumplimiento SUNAFIL', path: '/gestion-humana/cumplimiento-sunafil' },
      { name: 'gh-formacion', label: 'Formación', path: '/gestion-humana/formacion' },
      { name: 'gh-expedientes-adendas', label: 'Expedientes y Adendas', path: '/gestion-humana/expedientes-adendas' },
      { name: 'gh-documentacion-sunafil', label: 'Documentación SUNAFIL', path: '/gestion-humana/documentacion-sunafil' },
      { name: 'gh-manuales-mof', label: 'Manuales MOF', path: '/gestion-humana/manuales-mof' },
      { name: 'gh-vacaciones', label: 'Vacaciones', path: '/gestion-humana/vacaciones' },
      { name: 'gh-constancias-certificados', label: 'Constancias y Certificados', path: '/gestion-humana/constancias-certificados' },
      { name: 'gh-firmas-digitales', label: 'Firmas Digitales', path: '/gestion-humana/firmas-digitales' },
    ],
  },
  {
    kind: 'group',
    id: 'proyectos',
    label: 'Proyectos',
    icon: 'folder',
    children: [],
  },
  {
    kind: 'group',
    id: 'soma',
    label: 'SOMA',
    icon: 'shield',
    children: [],
  },
]

export function getNavLabelByRouteName(name: string | symbol | null | undefined): string {
  if (!name || typeof name !== 'string') {
    return 'Dashboard'
  }

  for (const item of navItems) {
    if (item.kind === 'link' && item.name === name) {
      return item.label
    }

    if (item.kind === 'group') {
      const child = item.children.find((entry) => entry.name === name)
      if (child) {
        return child.label
      }
    }
  }

  return 'Dashboard'
}

export function getGroupIdForRouteName(name: string | symbol | null | undefined): string | null {
  if (!name || typeof name !== 'string') {
    return null
  }

  const group = navItems.find(
    (item): item is NavGroup =>
      item.kind === 'group' && item.children.some((child) => child.name === name),
  )

  return group?.id ?? null
}
