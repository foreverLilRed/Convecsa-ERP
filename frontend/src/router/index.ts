import { createRouter, createWebHistory } from 'vue-router'

import { isAuthenticated } from '@/services/auth/authService'
import AppLayout from '@/layouts/AppLayout.vue'
import DashboardPage from '@/pages/DashboardPage.vue'
import ContratosPage from '@/pages/gestion-humana/ContratosPage.vue'
import PlaceholderPage from '@/pages/gestion-humana/PlaceholderPage.vue'
import PersonalPage from '@/pages/gestion-humana/PersonalPage.vue'
import PlantillasContratoPage from '@/pages/gestion-humana/PlantillasContratoPage.vue'
import LoginPage from '@/pages/LoginPage.vue'
import UsuariosPage from '@/pages/UsuariosPage.vue'

const gestionHumanaPages = [
  { path: 'onboarding', name: 'gh-onboarding', title: 'Onboarding' },
  { path: 'nominas', name: 'gh-nominas', title: 'Nóminas' },
  { path: 'asistencia', name: 'gh-asistencia', title: 'Asistencia' },
  { path: 'kpis', name: 'gh-kpis', title: 'KPIs' },
  { path: 'capacitaciones', name: 'gh-capacitaciones', title: 'Capacitaciones' },
  { path: 'portal-boletas', name: 'gh-portal-boletas', title: 'Portal Boletas' },
  { path: 'cumplimiento-sunafil', name: 'gh-cumplimiento-sunafil', title: 'Cumplimiento SUNAFIL' },
  { path: 'formacion', name: 'gh-formacion', title: 'Formación' },
  { path: 'expedientes-adendas', name: 'gh-expedientes-adendas', title: 'Expedientes y Adendas' },
  { path: 'documentacion-sunafil', name: 'gh-documentacion-sunafil', title: 'Documentación SUNAFIL' },
  { path: 'manuales-mof', name: 'gh-manuales-mof', title: 'Manuales MOF' },
  { path: 'vacaciones', name: 'gh-vacaciones', title: 'Vacaciones' },
  { path: 'constancias-certificados', name: 'gh-constancias-certificados', title: 'Constancias y Certificados' },
  { path: 'firmas-digitales', name: 'gh-firmas-digitales', title: 'Firmas Digitales' },
] as const

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: { guestOnly: true },
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardPage,
        },
        {
          path: 'usuarios',
          name: 'usuarios',
          component: UsuariosPage,
        },
        {
          path: 'gestion-humana/personal',
          name: 'gh-personal',
          component: PersonalPage,
        },
        {
          path: 'gestion-humana/contratos',
          name: 'gh-contratos',
          component: ContratosPage,
        },
        {
          path: 'gestion-humana/plantillas-contrato',
          name: 'gh-plantillas-contrato',
          component: PlantillasContratoPage,
        },
        ...gestionHumanaPages.map((page) => ({
          path: `gestion-humana/${page.path}`,
          name: page.name,
          component: PlaceholderPage,
          meta: { title: page.title },
        })),
        {
          path: 'personal/:legacy(.*)*',
          redirect: { name: 'gh-personal' },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const authenticated = isAuthenticated()

  if (to.meta.requiresAuth && !authenticated) {
    return { name: 'login' }
  }

  if (to.meta.guestOnly && authenticated) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
