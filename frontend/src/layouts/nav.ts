import type { Component } from 'vue'
import {
  BadgeCheckIcon,
  BookOpenIcon,
  BoxesIcon,
  Building2Icon,
  CalendarCheckIcon,
  ChartLineIcon,
  ClipboardListIcon,
  ContactIcon,
  FileSpreadsheetIcon,
  FileTextIcon,
  FilesIcon,
  FolderKanbanIcon,
  GraduationCapIcon,
  HeartPulseIcon,
  LayoutDashboardIcon,
  PalmtreeIcon,
  PenLineIcon,
  ShieldIcon,
  UserPlusIcon,
  UsersIcon,
  WalletIcon,
} from '@lucide/vue'

import type { RolUsuario } from '@/types/auth'

export type NavChild = {
  name: string
  label: string
  path: string
  icon: Component
}

export type NavGroup = {
  kind: 'group'
  id: string
  label: string
  icon: Component
  children: NavChild[]
  roles?: RolUsuario[]
}

export type NavItem = NavGroup

export const navItems: NavItem[] = [
  {
    kind: 'group',
    id: 'gestion-humana',
    label: 'Gestión Humana',
    icon: UsersIcon,
    children: [
      { name: 'gh-personal', label: 'Personal', path: '/gestion-humana/personal', icon: ContactIcon },
      { name: 'gh-contratos', label: 'Contratos', path: '/gestion-humana/contratos', icon: FileTextIcon },
      { name: 'gh-plantillas-contrato', label: 'Plantillas de Contrato', path: '/gestion-humana/plantillas-contrato', icon: FilesIcon },
      { name: 'gh-onboarding', label: 'Onboarding y Offboarding', path: '/gestion-humana/onboarding', icon: UserPlusIcon },
      { name: 'gh-nominas', label: 'Nóminas', path: '/gestion-humana/nominas', icon: WalletIcon },
      { name: 'gh-declaraciones', label: 'Declaración mensual', path: '/gestion-humana/nominas/declaraciones', icon: ClipboardListIcon },
      { name: 'gh-asistencia', label: 'Asistencia', path: '/gestion-humana/asistencia', icon: CalendarCheckIcon },
      { name: 'gh-kpis', label: 'KPIs y desempeño', path: '/gestion-humana/kpis', icon: ChartLineIcon },
      { name: 'gh-manuales-mof', label: 'Manuales MOF', path: '/gestion-humana/manuales-mof', icon: BookOpenIcon },
      { name: 'gh-vacaciones', label: 'Vacaciones', path: '/gestion-humana/vacaciones', icon: PalmtreeIcon },
      { name: 'gh-constancias-certificados', label: 'Constancias y Certificados', path: '/gestion-humana/constancias-certificados', icon: BadgeCheckIcon },
      { name: 'gh-firmas-digitales', label: 'Firmas Digitales', path: '/gestion-humana/firmas-digitales', icon: PenLineIcon },
    ],
  },
  {
    kind: 'group',
    id: 'proyectos',
    label: 'Proyectos',
    icon: FolderKanbanIcon,
    children: [
      { name: 'proyectos', label: 'Obras', path: '/proyectos', icon: Building2Icon },
      { name: 'proyecto-materiales', label: 'Catálogo de materiales', path: '/proyectos/materiales', icon: BoxesIcon },
    ],
  },
  {
    kind: 'group',
    id: 'soma',
    label: 'SOMA',
    icon: ShieldIcon,
    roles: ['SUPERADMIN', 'ADMIN', 'INGENIERO_SOMA'],
    children: [
      { name: 'soma-dashboard', label: 'Tablero SST', path: '/soma/dashboard', icon: LayoutDashboardIcon },
      { name: 'soma-habilitacion', label: 'Vigilancia médica y SCTR', path: '/soma/habilitacion', icon: HeartPulseIcon },
      { name: 'soma-capacitaciones', label: 'Capacitaciones', path: '/soma/capacitaciones', icon: GraduationCapIcon },
      { name: 'soma-formatos', label: 'Formatos SUNAFIL', path: '/soma/formatos', icon: FileSpreadsheetIcon },
    ],
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

  if (name === 'proyecto-soma') {
    return 'SOMA'
  }

  for (const item of navItems) {
    const child = item.children.find((entry) => entry.name === name)
    if (child) {
      return child.label
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
    || name === 'proyecto-soma'
  ) {
    return 'proyectos'
  }

  return navItems.find((item) => item.children.some((child) => child.name === name))?.id ?? null
}
