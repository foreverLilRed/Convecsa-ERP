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
      { name: 'gh-onboarding', label: 'Onboarding y Offboarding', path: '/gestion-humana/onboarding' },
      { name: 'gh-nominas', label: 'Nóminas', path: '/gestion-humana/nominas' },
      { name: 'gh-declaraciones', label: 'Declaración mensual', path: '/gestion-humana/nominas/declaraciones' },
      { name: 'gh-asistencia', label: 'Asistencia', path: '/gestion-humana/asistencia' },
      { name: 'gh-kpis', label: 'KPIs y desempeño', path: '/gestion-humana/kpis' },
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
    children: [
      { name: 'proyectos', label: 'Obras', path: '/proyectos' },
      { name: 'proyecto-materiales', label: 'Catálogo de materiales', path: '/proyectos/materiales' },
    ],
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

  if (name === 'gh-nomina-detalle') {
    return 'Nóminas'
  }

  if (name === 'gh-declaracion-detalle') {
    return 'Declaración mensual'
  }

  if (name === 'gh-kpis-detalle') {
    return 'KPIs y desempeño'
  }

  if (name === 'proyecto-ficha') {
    return 'Ficha de obra'
  }

  if (name === 'proyecto-tareo') {
    return 'Tareo'
  }

  if (name === 'proyecto-planilla') {
    return 'Planilla RCC'
  }

  if (name === 'proyecto-presupuesto') {
    return 'Presupuesto'
  }

  if (name === 'proyecto-almacen') {
    return 'Almacén'
  }

  if (name === 'proyecto-recursos') {
    return 'Recursos'
  }

  if (name === 'proyecto-avances') {
    return 'Avances'
  }

  if (name === 'proyecto-financiamiento') {
    return 'Financiamiento'
  }

  if (name === 'proyecto-rentabilidad') {
    return 'Rentabilidad'
  }

  if (name === 'proyecto-documentos') {
    return 'Documentos'
  }

  if (name === 'proyecto-bitacora') {
    return 'Bitácora'
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

  if (name === 'gh-nomina-detalle' || name === 'gh-declaracion-detalle' || name === 'gh-kpis-detalle') {
    return 'gestion-humana'
  }

  if (
    name === 'proyecto-ficha'
    || name === 'proyecto-tareo'
    || name === 'proyecto-planilla'
    || name === 'proyecto-presupuesto'
    || name === 'proyecto-almacen'
    || name === 'proyecto-recursos'
    || name === 'proyecto-avances'
    || name === 'proyecto-financiamiento'
    || name === 'proyecto-rentabilidad'
    || name === 'proyecto-documentos'
    || name === 'proyecto-bitacora'
  ) {
    return 'proyectos'
  }

  const group = navItems.find(
    (item): item is NavGroup =>
      item.kind === 'group' && item.children.some((child) => child.name === name),
  )

  return group?.id ?? null
}
