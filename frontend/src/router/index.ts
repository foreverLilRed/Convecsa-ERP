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
import DeclaracionDetallePage from '@/pages/gestion-humana/DeclaracionDetallePage.vue'
import DeclaracionesPage from '@/pages/gestion-humana/DeclaracionesPage.vue'
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
import ProyectosPage from '@/pages/proyectos/ProyectosPage.vue'
import ProyectoFichaPage from '@/pages/proyectos/ProyectoFichaPage.vue'
import ProyectoTareoPage from '@/pages/proyectos/ProyectoTareoPage.vue'
import ProyectoPlanillaPage from '@/pages/proyectos/ProyectoPlanillaPage.vue'
import ProyectoPresupuestoPage from '@/pages/proyectos/ProyectoPresupuestoPage.vue'
import ProyectoAlmacenPage from '@/pages/proyectos/ProyectoAlmacenPage.vue'
import ProyectoAvancesPage from '@/pages/proyectos/ProyectoAvancesPage.vue'
import ProyectoFinanciamientoPage from '@/pages/proyectos/ProyectoFinanciamientoPage.vue'
import ProyectoRentabilidadPage from '@/pages/proyectos/ProyectoRentabilidadPage.vue'
import ProyectoDocumentosPage from '@/pages/proyectos/ProyectoDocumentosPage.vue'

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
          path: 'gestion-humana/nominas/declaraciones',
          name: 'gh-declaraciones',
          component: DeclaracionesPage,
        },
        {
          path: 'gestion-humana/nominas/declaraciones/:id(\\d+)',
          name: 'gh-declaracion-detalle',
          component: DeclaracionDetallePage,
          props: true,
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
          path: 'personal/:legacy(.*)*',
          redirect: { name: 'gh-personal' },
        },
        {
          path: 'proyectos',
          name: 'proyectos',
          component: ProyectosPage,
        },
        {
          path: 'proyectos/:id(\\d+)',
          name: 'proyecto-ficha',
          component: ProyectoFichaPage,
          children: [
            {
              path: 'tareo',
              name: 'proyecto-tareo',
              component: ProyectoTareoPage,
            },
            {
              path: 'planilla',
              name: 'proyecto-planilla',
              component: ProyectoPlanillaPage,
            },
            {
              path: 'presupuesto',
              name: 'proyecto-presupuesto',
              component: ProyectoPresupuestoPage,
            },
            {
              path: 'almacen',
              name: 'proyecto-almacen',
              component: ProyectoAlmacenPage,
            },
            {
              path: 'avances',
              name: 'proyecto-avances',
              component: ProyectoAvancesPage,
            },
            {
              path: 'financiamiento',
              name: 'proyecto-financiamiento',
              component: ProyectoFinanciamientoPage,
            },
            {
              path: 'rentabilidad',
              name: 'proyecto-rentabilidad',
              component: ProyectoRentabilidadPage,
            },
            {
              path: 'documentos',
              name: 'proyecto-documentos',
              component: ProyectoDocumentosPage,
            },
          ],
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
