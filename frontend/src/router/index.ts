import { createRouter, createWebHistory } from 'vue-router'

import { isAuthenticated } from '@/services/auth/authService'
import AppLayout from '@/layouts/AppLayout.vue'
import DashboardPage from '@/pages/DashboardPage.vue'
import AsistenciaPage from '@/pages/gestion-humana/AsistenciaPage.vue'
import AsistenciaPlanillaPage from '@/pages/gestion-humana/AsistenciaPlanillaPage.vue'
import ContratosPage from '@/pages/gestion-humana/ContratosPage.vue'
import FirmasDigitalesPage from '@/pages/gestion-humana/FirmasDigitalesPage.vue'
import KpisDesempenoDetallePage from '@/pages/gestion-humana/KpisDesempenoDetallePage.vue'
import KpisDesempenoPage from '@/pages/gestion-humana/KpisDesempenoPage.vue'
import NominasPage from '@/pages/gestion-humana/NominasPage.vue'
import OnboardingPage from '@/pages/gestion-humana/OnboardingPage.vue'
import PeriodoNominaPage from '@/pages/gestion-humana/PeriodoNominaPage.vue'
import PersonalPage from '@/pages/gestion-humana/PersonalPage.vue'
import PlantillasContratoPage from '@/pages/gestion-humana/PlantillasContratoPage.vue'
import PlantillaContratoEditorPage from '@/pages/gestion-humana/PlantillaContratoEditorPage.vue'
import ManualesMofPage from '@/pages/gestion-humana/ManualesMofPage.vue'
import LoginPage from '@/pages/LoginPage.vue'
import PersonaInvitadoPage from '@/pages/PersonaInvitadoPage.vue'
import UsuariosPage from '@/pages/UsuariosPage.vue'
import VacacionesPage from '@/pages/gestion-humana/VacacionesPage.vue'
import ConstanciasCertificadosPage from '@/pages/gestion-humana/ConstanciasCertificadosPage.vue'
import ObrasPage from '@/pages/proyectos/ObrasPage.vue'
import ProyectosPlaceholderPage from '@/pages/proyectos/PlaceholderPage.vue'

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
      path: '/invitar/persona/:token',
      name: 'persona-invitado',
      component: PersonaInvitadoPage,
    },
    {
      path: '/gestion-humana/asistencia/planilla',
      name: 'gh-asistencia-planilla',
      component: AsistenciaPlanillaPage,
      meta: { requiresAuth: true },
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
        {
          path: 'gestion-humana/plantillas-contrato/nueva',
          name: 'gh-plantilla-contrato-nueva',
          component: PlantillaContratoEditorPage,
        },
        {
          path: 'gestion-humana/plantillas-contrato/:id(\\d+)/editar',
          name: 'gh-plantilla-contrato-editar',
          component: PlantillaContratoEditorPage,
        },
        {
          path: 'gestion-humana/onboarding',
          name: 'gh-onboarding',
          component: OnboardingPage,
        },
        {
          path: 'gestion-humana/firmas-digitales',
          name: 'gh-firmas-digitales',
          component: FirmasDigitalesPage,
        },
        {
          path: 'gestion-humana/nominas',
          name: 'gh-nominas',
          component: NominasPage,
        },
        {
          path: 'gestion-humana/nominas/:id(\\d+)',
          name: 'gh-nomina-detalle',
          component: PeriodoNominaPage,
          props: true,
        },
        {
          path: 'gestion-humana/asistencia',
          name: 'gh-asistencia',
          component: AsistenciaPage,
        },
        {
          path: 'gestion-humana/vacaciones',
          name: 'gh-vacaciones',
          component: VacacionesPage,
        },
        {
          path: 'gestion-humana/constancias-certificados',
          name: 'gh-constancias-certificados',
          component: ConstanciasCertificadosPage,
        },
        {
          path: 'gestion-humana/kpis',
          name: 'gh-kpis',
          component: KpisDesempenoPage,
        },
        {
          path: 'gestion-humana/kpis/:personaId(\\d+)',
          name: 'gh-kpis-detalle',
          component: KpisDesempenoDetallePage,
        },
        {
          path: 'gestion-humana/manuales-mof',
          name: 'gh-manuales-mof',
          component: ManualesMofPage,
        },
        {
          path: 'proyectos/obras',
          name: 'proy-obras',
          component: ObrasPage,
        },
        {
          path: 'proyectos/asignaciones',
          name: 'proy-asignaciones',
          component: ProyectosPlaceholderPage,
          meta: {
            title: 'Asignaciones',
            description:
              'Asigne personal a obras con rol, fechas de ingreso y salida. Se vincula con contratos y tareo.',
          },
        },
        {
          path: 'proyectos/avances',
          name: 'proy-avances',
          component: ProyectosPlaceholderPage,
          meta: {
            title: 'Avances',
            description:
              'Registre avance físico por partida o frente: metrados, porcentajes y cortes de control.',
          },
        },
        {
          path: 'proyectos/presupuesto',
          name: 'proy-presupuesto',
          component: ProyectosPlaceholderPage,
          meta: {
            title: 'Presupuesto',
            description:
              'Presupuesto de obra por partidas: metrado, precios unitarios y totales contractuales o internos.',
          },
        },
        {
          path: 'proyectos/valorizaciones',
          name: 'proy-valorizaciones',
          component: ProyectosPlaceholderPage,
          meta: {
            title: 'Valorizaciones',
            description:
              'Cortes de valorización del avance físico frente al presupuesto y al cliente.',
          },
        },
        {
          path: 'proyectos/bitacora',
          name: 'proy-bitacora',
          component: ProyectosPlaceholderPage,
          meta: {
            title: 'Bitácora',
            description:
              'Diario de obra: novedades, clima, visitas, incidencias y evidencias del día.',
          },
        },
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
