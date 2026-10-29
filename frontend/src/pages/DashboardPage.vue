<script setup lang="ts">
import {
  ActivityIcon,
  ArrowUpRightIcon,
  CalendarClockIcon,
  CircleDollarSignIcon,
  Clock3Icon,
  FileTextIcon,
  RefreshCwIcon,
  UsersRoundIcon,
} from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { getSession } from '@/services/auth/authService'
import {
  obtenerDashboardAsistencia,
  obtenerDashboardContratos,
  obtenerDashboardNomina,
  obtenerDashboardPersonas,
} from '@/services/dashboard/dashboardService'
import type {
  DashboardAsistencia,
  DashboardContratos,
  DashboardNomina,
  DashboardPersonas,
} from '@/types/dashboard'

type SectionState<T> = { data: T | null; loading: boolean; error: string }

const session = getSession()
const personas = ref<SectionState<DashboardPersonas>>({ data: null, loading: true, error: '' })
const contratos = ref<SectionState<DashboardContratos>>({ data: null, loading: true, error: '' })
const nomina = ref<SectionState<DashboardNomina>>({ data: null, loading: true, error: '' })
const asistencia = ref<SectionState<DashboardAsistencia>>({ data: null, loading: true, error: '' })
const refreshing = ref(false)

const fechaActual = new Intl.DateTimeFormat('es-PE', {
  weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
}).format(new Date())

const periodoActual = computed(() => {
  const data = nomina.value.data
  if (!data) return 'Mes actual'
  const texto = new Intl.DateTimeFormat('es-PE', { month: 'long', year: 'numeric' })
    .format(new Date(data.anio, data.mes - 1, 1))
  return texto.charAt(0).toUpperCase() + texto.slice(1)
})

const currency = new Intl.NumberFormat('es-PE', {
  style: 'currency', currency: 'PEN', minimumFractionDigits: 2,
})
const number = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 2 })

function message(error: unknown): string {
  return error instanceof Error ? error.message : 'No se pudo cargar esta información.'
}

async function loadPersonas() {
  personas.value = { ...personas.value, loading: true, error: '' }
  try {
    personas.value = { data: await obtenerDashboardPersonas(), loading: false, error: '' }
  } catch (error) {
    personas.value = { data: null, loading: false, error: message(error) }
  }
}

async function loadContratos() {
  contratos.value = { ...contratos.value, loading: true, error: '' }
  try {
    contratos.value = { data: await obtenerDashboardContratos(), loading: false, error: '' }
  } catch (error) {
    contratos.value = { data: null, loading: false, error: message(error) }
  }
}

async function loadNomina() {
  nomina.value = { ...nomina.value, loading: true, error: '' }
  try {
    nomina.value = { data: await obtenerDashboardNomina(), loading: false, error: '' }
  } catch (error) {
    nomina.value = { data: null, loading: false, error: message(error) }
  }
}

async function loadAsistencia() {
  asistencia.value = { ...asistencia.value, loading: true, error: '' }
  try {
    asistencia.value = { data: await obtenerDashboardAsistencia(), loading: false, error: '' }
  } catch (error) {
    asistencia.value = { data: null, loading: false, error: message(error) }
  }
}

async function loadAll() {
  refreshing.value = true
  await Promise.allSettled([loadPersonas(), loadContratos(), loadNomina(), loadAsistencia()])
  refreshing.value = false
}

onMounted(() => { void loadAll() })
</script>

<template>
  <section class="min-w-0 space-y-3">
    <header class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Panel principal</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          Bienvenido, {{ session?.username ?? 'usuario' }}
        </h2>
        <p class="mt-2 text-sm capitalize text-slate-500">{{ fechaActual }}</p>
      </div>
      <Button variant="outline" class="h-10 bg-white px-4" type="button" :disabled="refreshing" @click="loadAll">
        <RefreshCwIcon data-icon="inline-start" :class="refreshing ? 'animate-spin' : ''" />
        {{ refreshing ? 'Actualizando…' : 'Actualizar datos' }}
      </Button>
    </header>

    <div class="grid gap-2 sm:grid-cols-2 xl:grid-cols-4">
      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div><p class="text-sm font-medium text-slate-500">Personas registradas</p><Skeleton v-if="personas.loading" class="mt-3 h-9 w-24" /><p v-else-if="personas.data" class="mt-2 text-3xl font-bold tabular-nums text-palette-navy">{{ personas.data.total }}</p><p v-else class="mt-3 text-sm text-red-700">{{ personas.error }}</p></div>
          <span class="flex size-11 shrink-0 items-center justify-center rounded-xl bg-sky-50 text-palette-royal"><UsersRoundIcon class="size-5" /></span>
        </div>
        <p v-if="personas.data" class="mt-4 text-xs text-slate-500"><strong class="text-emerald-700">{{ personas.data.activos }} activos</strong> · {{ personas.data.inactivos }} inactivos</p>
      </article>

      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div><p class="text-sm font-medium text-slate-500">Contratos vigentes</p><Skeleton v-if="contratos.loading" class="mt-3 h-9 w-24" /><p v-else-if="contratos.data" class="mt-2 text-3xl font-bold tabular-nums text-palette-navy">{{ contratos.data.vigentesHoy }}</p><p v-else class="mt-3 text-sm text-red-700">{{ contratos.error }}</p></div>
          <span class="flex size-11 shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600"><FileTextIcon class="size-5" /></span>
        </div>
        <p v-if="contratos.data" class="mt-4 text-xs text-slate-500"><strong :class="contratos.data.porVencer30Dias ? 'text-amber-700' : 'text-emerald-700'">{{ contratos.data.porVencer30Dias }} por vencer</strong> en los próximos 30 días</p>
      </article>

      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0"><p class="text-sm font-medium text-slate-500">Sueldos a pagar este mes</p><Skeleton v-if="nomina.loading" class="mt-3 h-9 w-36" /><p v-else-if="nomina.data" class="mt-2 truncate text-2xl font-bold tabular-nums text-palette-navy">{{ currency.format(nomina.data.montoPagar) }}</p><p v-else class="mt-3 text-sm text-red-700">{{ nomina.error }}</p></div>
          <span class="flex size-11 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-700"><CircleDollarSignIcon class="size-5" /></span>
        </div>
        <p v-if="nomina.data" class="mt-4 text-xs text-slate-500">{{ nomina.data.montoEstimado ? 'Estimado según contratos vigentes' : `${nomina.data.cantidadBoletas} boletas procesadas` }}</p>
      </article>

      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div><p class="text-sm font-medium text-slate-500">Asistencia del mes</p><Skeleton v-if="asistencia.loading" class="mt-3 h-9 w-24" /><p v-else-if="asistencia.data" class="mt-2 text-3xl font-bold tabular-nums text-palette-navy">{{ asistencia.data.personasConRegistro }}</p><p v-else class="mt-3 text-sm text-red-700">{{ asistencia.error }}</p></div>
          <span class="flex size-11 shrink-0 items-center justify-center rounded-xl bg-amber-50 text-amber-700"><Clock3Icon class="size-5" /></span>
        </div>
        <p v-if="asistencia.data" class="mt-4 text-xs text-slate-500">{{ asistencia.data.inasistencias }} inasistencias · {{ asistencia.data.minutosTardanza }} min de tardanza</p>
      </article>
    </div>

    <div class="grid gap-2.5 xl:grid-cols-2">
      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm sm:p-6">
        <div class="flex items-center justify-between gap-3"><div><h3 class="font-semibold text-palette-navy">Personal y contratos</h3><p class="mt-1 text-sm text-slate-500">Estado actual de la fuerza laboral.</p></div><ActivityIcon class="size-5 text-palette-royal" /></div>
        <div v-if="personas.loading || contratos.loading" class="mt-5 grid gap-3 sm:grid-cols-2"><Skeleton v-for="item in 6" :key="item" class="h-20 rounded-xl" /></div>
        <div v-else-if="personas.data && contratos.data" class="mt-5 grid gap-3 sm:grid-cols-2">
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Personas activas</p><p class="mt-1 text-xl font-bold text-slate-800">{{ personas.data.activos }}</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Personas inactivas</p><p class="mt-1 text-xl font-bold text-slate-800">{{ personas.data.inactivos }}</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Nuevas este mes</p><p class="mt-1 text-xl font-bold text-slate-800">{{ personas.data.nuevosEsteMes }}</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">EMO por vencer</p><p class="mt-1 text-xl font-bold text-slate-800">{{ personas.data.examenesMedicosPorVencer }}</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Contratos activos</p><p class="mt-1 text-xl font-bold text-slate-800">{{ contratos.data.activos }}</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Sin fecha de fin</p><p class="mt-1 text-xl font-bold text-slate-800">{{ contratos.data.sinFechaFin }}</p></div>
        </div>
        <div class="mt-5 flex flex-wrap gap-3 border-t border-slate-100 pt-4">
          <RouterLink class="inline-flex items-center gap-1 text-sm font-semibold text-palette-royal hover:text-palette-navy" :to="{ name: 'gh-personal' }">Ver personal <ArrowUpRightIcon class="size-4" /></RouterLink>
          <RouterLink class="inline-flex items-center gap-1 text-sm font-semibold text-palette-royal hover:text-palette-navy" :to="{ name: 'gh-contratos' }">Ver contratos <ArrowUpRightIcon class="size-4" /></RouterLink>
        </div>
      </article>

      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm sm:p-6">
        <div class="flex items-center justify-between gap-3"><div><h3 class="font-semibold text-palette-navy">Planilla · {{ periodoActual }}</h3><p class="mt-1 text-sm text-slate-500">Haberes, descuentos y aportes del periodo.</p></div><CalendarClockIcon class="size-5 text-palette-royal" /></div>
        <div v-if="nomina.loading" class="mt-5 space-y-3"><Skeleton v-for="item in 4" :key="item" class="h-12 rounded-lg" /></div>
        <div v-else-if="nomina.data" class="mt-5 space-y-1">
          <div class="flex items-center justify-between gap-4 rounded-lg px-3 py-2.5"><span class="text-sm text-slate-500">Total de haberes</span><strong class="tabular-nums text-slate-800">{{ currency.format(nomina.data.totalHaberes) }}</strong></div>
          <div class="flex items-center justify-between gap-4 rounded-lg bg-slate-50 px-3 py-2.5"><span class="text-sm text-slate-500">Descuentos</span><strong class="tabular-nums text-slate-800">{{ currency.format(nomina.data.totalDescuentos) }}</strong></div>
          <div class="flex items-center justify-between gap-4 rounded-lg px-3 py-2.5"><span class="text-sm text-slate-500">Aportes del empleador</span><strong class="tabular-nums text-slate-800">{{ currency.format(nomina.data.aportesEmpleador) }}</strong></div>
          <div class="flex items-center justify-between gap-4 rounded-lg bg-palette-ice px-3 py-3"><span class="text-sm font-semibold text-palette-navy">Neto por pagar</span><strong class="text-lg tabular-nums text-palette-navy">{{ currency.format(nomina.data.montoPagar) }}</strong></div>
          <p v-if="nomina.data.montoEstimado" class="pt-2 text-xs leading-5 text-amber-700">Todavía no existe una planilla para el mes; el monto mostrado es la suma de sueldos de contratos vigentes.</p>
        </div>
        <div class="mt-5 border-t border-slate-100 pt-4"><RouterLink class="inline-flex items-center gap-1 text-sm font-semibold text-palette-royal hover:text-palette-navy" :to="nomina.data?.periodoId ? { name: 'gh-nomina-detalle', params: { id: nomina.data.periodoId } } : { name: 'gh-nominas' }">Ir a nóminas <ArrowUpRightIcon class="size-4" /></RouterLink></div>
      </article>

      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm sm:p-6 xl:col-span-2">
        <div class="flex items-center justify-between gap-3"><div><h3 class="font-semibold text-palette-navy">Tareo del mes</h3><p class="mt-1 text-sm text-slate-500">Consolidado de asistencia e incidencias registradas.</p></div><Clock3Icon class="size-5 text-palette-royal" /></div>
        <div v-if="asistencia.loading" class="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4"><Skeleton v-for="item in 4" :key="item" class="h-20 rounded-xl" /></div>
        <div v-else-if="asistencia.data" class="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Horas trabajadas</p><p class="mt-1 text-xl font-bold text-slate-800">{{ number.format(asistencia.data.horasTrabajadas) }} h</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Inasistencias</p><p class="mt-1 text-xl font-bold text-slate-800">{{ asistencia.data.inasistencias }}</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Descansos médicos</p><p class="mt-1 text-xl font-bold text-slate-800">{{ asistencia.data.descansosMedicos }}</p></div>
          <div class="rounded-xl bg-slate-50 p-4"><p class="text-xs text-slate-500">Horas extra</p><p class="mt-1 text-xl font-bold text-slate-800">{{ number.format((asistencia.data.minutosExtras25 + asistencia.data.minutosExtras35) / 60) }} h</p><p class="mt-1 text-[11px] text-slate-500">25%: {{ asistencia.data.minutosExtras25 }} min · 35%: {{ asistencia.data.minutosExtras35 }} min</p></div>
        </div>
        <div class="mt-5 border-t border-slate-100 pt-4"><RouterLink class="inline-flex items-center gap-1 text-sm font-semibold text-palette-royal hover:text-palette-navy" :to="{ name: 'gh-asistencia' }">Ir a asistencia <ArrowUpRightIcon class="size-4" /></RouterLink></div>
      </article>
    </div>
  </section>
</template>
