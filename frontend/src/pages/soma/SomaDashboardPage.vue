<script setup lang="ts">
import { ActivityIcon, AlertTriangleIcon, RefreshCwIcon, ShieldAlertIcon } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { obtenerDashboardSoma } from '@/services/soma/somaService'
import type { SomaDashboard } from '@/types/soma'

const loading = ref(true)
const error = ref('')
const data = ref<SomaDashboard | null>(null)

const number = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 2 })
const acumulado = computed(() => data.value?.estadistica.acumulado ?? null)

function message(err: unknown): string {
  return err instanceof Error ? err.message : 'No se pudo cargar el tablero SST.'
}

function fmt(valor: number | null | undefined): string {
  if (valor == null) return '—'
  return number.format(valor)
}

async function cargar() {
  loading.value = true
  error.value = ''
  try {
    data.value = await obtenerDashboardSoma()
  } catch (err) {
    data.value = null
    error.value = message(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void cargar()
})
</script>

<template>
  <section class="min-w-0 space-y-3">
    <header class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">SOMA corporativo</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Tablero SST</h2>
        <p class="mt-2 text-sm text-slate-500">
          Indicadores HHT, IF, IS e IA del año {{ data?.estadistica.anio ?? '—' }}, hallazgos y obras con alerta crítica.
        </p>
      </div>
      <Button variant="outline" class="h-10 bg-white px-4" type="button" :disabled="loading" @click="cargar">
        <RefreshCwIcon data-icon="inline-start" :class="loading ? 'animate-spin' : ''" />
        {{ loading ? 'Actualizando…' : 'Actualizar' }}
      </Button>
    </header>

    <p v-if="error" class="text-sm text-red-700">{{ error }}</p>

    <div class="grid gap-2 sm:grid-cols-2 xl:grid-cols-4">
      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <p class="text-sm font-medium text-slate-500">HHT acumuladas</p>
        <Skeleton v-if="loading" class="mt-3 h-9 w-24" />
        <p v-else class="mt-2 text-3xl font-bold tabular-nums text-palette-navy">{{ fmt(acumulado?.hht) }}</p>
      </article>
      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <p class="text-sm font-medium text-slate-500">Accidentes</p>
        <Skeleton v-if="loading" class="mt-3 h-9 w-16" />
        <p v-else class="mt-2 text-3xl font-bold tabular-nums text-palette-navy">{{ acumulado?.accidentes ?? '—' }}</p>
      </article>
      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <p class="text-sm font-medium text-slate-500">IF / IS / IA</p>
        <Skeleton v-if="loading" class="mt-3 h-9 w-40" />
        <p v-else class="mt-2 text-lg font-bold tabular-nums text-palette-navy">
          {{ fmt(acumulado?.indiceFrecuencia) }}
          <span class="text-slate-400">/</span>
          {{ fmt(acumulado?.indiceSeveridad) }}
          <span class="text-slate-400">/</span>
          {{ fmt(acumulado?.indiceAccidentabilidad) }}
        </p>
      </article>
      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
        <p class="text-sm font-medium text-slate-500">Días sin accidente</p>
        <Skeleton v-if="loading" class="mt-3 h-9 w-16" />
        <p v-else class="mt-2 text-3xl font-bold tabular-nums text-palette-navy">
          {{ data?.estadistica.diasSinAccidente ?? '—' }}
        </p>
      </article>
    </div>

    <div class="grid gap-2.5 xl:grid-cols-2">
      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm sm:p-6">
        <div class="flex items-center justify-between gap-3">
          <div>
            <h3 class="font-semibold text-palette-navy">Hallazgos</h3>
            <p class="mt-1 text-sm text-slate-500">Abiertos o en proceso frente a levantados.</p>
          </div>
          <ActivityIcon class="size-5 text-palette-royal" />
        </div>
        <div v-if="loading" class="mt-5 grid gap-3 sm:grid-cols-2">
          <Skeleton class="h-20 rounded-xl" />
          <Skeleton class="h-20 rounded-xl" />
        </div>
        <div v-else class="mt-5 grid gap-3 sm:grid-cols-2">
          <div class="rounded-xl bg-amber-50 p-4">
            <p class="text-xs text-amber-800">Abiertos</p>
            <p class="mt-1 text-2xl font-bold text-amber-900">{{ data?.hallazgosAbiertos ?? 0 }}</p>
          </div>
          <div class="rounded-xl bg-emerald-50 p-4">
            <p class="text-xs text-emerald-800">Levantados</p>
            <p class="mt-1 text-2xl font-bold text-emerald-900">{{ data?.hallazgosLevantados ?? 0 }}</p>
          </div>
        </div>
      </article>

      <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm sm:p-6">
        <div class="flex items-center justify-between gap-3">
          <div>
            <h3 class="font-semibold text-palette-navy">Obras con alerta crítica</h3>
            <p class="mt-1 text-sm text-slate-500">Alertas SOMA de severidad crítica.</p>
          </div>
          <ShieldAlertIcon class="size-5 text-palette-royal" />
        </div>
        <div class="mt-4 overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Código</TableHead>
                <TableHead>Obra</TableHead>
                <TableHead />
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="obra in data?.obrasConAlertasCriticas ?? []" :key="obra.proyectoId">
                <TableCell class="font-medium">{{ obra.codigo }}</TableCell>
                <TableCell>{{ obra.nombre }}</TableCell>
                <TableCell class="text-right">
                  <RouterLink
                    class="inline-flex items-center gap-1 text-sm font-semibold text-palette-royal hover:text-palette-navy"
                    :to="{ name: 'proyecto-soma', params: { id: obra.proyectoId } }"
                  >
                    Ver SOMA
                    <AlertTriangleIcon class="size-3.5" />
                  </RouterLink>
                </TableCell>
              </TableRow>
              <TableEmpty v-if="!loading && !(data?.obrasConAlertasCriticas.length)" :colspan="3">
                Ninguna obra con alerta crítica.
              </TableEmpty>
            </TableBody>
          </Table>
        </div>
      </article>
    </div>
  </section>
</template>
