<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon, CircleHelpIcon, XIcon } from '@lucide/vue'
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogOverlay,
  DialogPortal,
  DialogRoot,
  DialogTitle,
  DialogTrigger,
} from 'reka-ui'

import AsistenciaPlanillaGrid from '@/components/gestion-humana/AsistenciaPlanillaGrid.vue'
import { Button } from '@/components/ui/button'
import { useAsistenciaPlanilla } from '@/composables/useAsistenciaPlanilla'
import { mesesCatalogo } from '@/types/nomina'

const route = useRoute()
const router = useRouter()
const meses = mesesCatalogo()
const tutorialOpen = ref(false)

function parseQueryNumber(value: unknown, fallback: number): number {
  const raw = Array.isArray(value) ? value[0] : value
  const n = Number(raw)
  return Number.isFinite(n) ? n : fallback
}

const hoy = new Date()
const initialAnio = parseQueryNumber(route.query.anio, hoy.getFullYear())
const initialMes = Math.min(12, Math.max(1, parseQueryNumber(route.query.mes, hoy.getMonth() + 1)))

const {
  anio,
  mes,
  loading,
  acting,
  saving,
  planilla,
  estadoVivo,
  hoyIso,
  dirtyCount,
  tituloPeriodo,
  filasVista,
  horasDe,
  estadoDe,
  codigoDe,
  minutosDe,
  horasBloqueadas,
  estadoSelectClase,
  onHoras,
  onCodigo,
  onMinutos,
  onDescartar,
  cambiarPeriodo,
  onGenerar,
  onGuardar,
} = useAsistenciaPlanilla({
  initialAnio,
  initialMes,
  updateDocumentTitle: true,
})

watch([anio, mes], ([nuevoAnio, nuevoMes]) => {
  const nextQuery = {
    ...route.query,
    anio: String(nuevoAnio),
    mes: String(nuevoMes),
  }
  if (route.query.anio === nextQuery.anio && route.query.mes === nextQuery.mes) {
    return
  }
  void router.replace({ query: nextQuery })
})

const inputClass =
  'mt-0.5 block w-full rounded-md border border-slate-200 px-2 py-1 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-500/20'
</script>

<template>
  <div class="flex h-dvh flex-col bg-palette-soft text-slate-700">
    <p class="sr-only" role="status" aria-live="polite">{{ estadoVivo }}</p>

    <header class="shrink-0 border-b border-palette-cyan/60 bg-white px-3 py-2">
      <div class="flex flex-wrap items-end justify-between gap-2">
        <div class="flex flex-wrap items-end gap-1.5">
          <div>
            <p class="text-[10px] font-semibold uppercase tracking-widest text-palette-royal">Parte diario de asistencia</p>
            <h1 class="text-base font-bold text-palette-navy">Del 1 al 31 · {{ tituloPeriodo }}</h1>
          </div>
          <Button
            variant="outline"
            size="icon"
            class="h-8 w-8"
            type="button"
            :disabled="loading || acting || saving"
            aria-label="Mes anterior"
            @click="cambiarPeriodo(-1)"
          >
            <ChevronLeftIcon class="cn-rtl-flip size-4" />
          </Button>
          <div>
            <label class="text-[10px] font-medium text-slate-600" for="planilla-mes">Mes</label>
            <select
              id="planilla-mes"
              v-model.number="mes"
              :class="inputClass"
              class="min-w-28"
              :disabled="loading || acting || saving"
            >
              <option v-for="item in meses" :key="item.value" :value="item.value">{{ item.label }}</option>
            </select>
          </div>
          <div>
            <label class="text-[10px] font-medium text-slate-600" for="planilla-anio">Año</label>
            <input
              id="planilla-anio"
              v-model.number="anio"
              type="number"
              min="2000"
              max="2100"
              :class="inputClass"
              class="w-20"
              :disabled="loading || acting || saving"
            />
          </div>
          <Button
            variant="outline"
            size="icon"
            class="h-8 w-8"
            type="button"
            :disabled="loading || acting || saving"
            aria-label="Mes siguiente"
            @click="cambiarPeriodo(1)"
          >
            <ChevronRightIcon class="cn-rtl-flip size-4" />
          </Button>
        </div>

        <div class="flex flex-wrap items-center gap-1.5">
          <ul class="mr-1 hidden flex-wrap gap-x-3 gap-y-1 text-[10px] text-slate-700 lg:flex" aria-label="Leyenda de códigos">
            <li class="inline-flex items-center gap-1">
              <span class="inline-flex h-4 w-4 items-center justify-center rounded border border-[#bbf7d0] bg-[#dcfce7] font-mono text-[9px] font-bold text-[#15803d]">A</span>
              Asistió
            </li>
            <li class="inline-flex items-center gap-1">
              <span class="inline-flex h-4 w-4 items-center justify-center rounded border border-[#fecdd3] bg-[#ffe4e6] font-mono text-[9px] font-bold text-[#be123c]">FI</span>
              Falta
            </li>
            <li class="inline-flex items-center gap-1">
              <span class="inline-flex h-4 w-4 items-center justify-center rounded border border-[#bfdbfe] bg-[#dbeafe] font-mono text-[9px] font-bold text-[#1d4ed8]">DM</span>
              D. médico
            </li>
            <li class="inline-flex items-center gap-1">
              <span class="inline-flex h-4 w-4 items-center justify-center rounded border border-[#e2e8f0] bg-[#f1f5f9] font-mono text-[9px] font-bold text-[#64748b]">D</span>
              Descanso
            </li>
            <li class="inline-flex items-center gap-1">
              <span class="inline-flex h-4 w-4 items-center justify-center rounded border border-[#e9d5ff] bg-[#f3e8ff] font-mono text-[9px] font-bold text-[#6b21a8]">VAC</span>
              Vacaciones
            </li>
            <li class="inline-flex items-center gap-1">
              <span class="inline-flex h-4 w-4 items-center justify-center rounded border border-[#fed7aa] bg-[#ffedd5] font-mono text-[9px] font-bold text-[#9a3412]">LSG</span>
              Lic. s/ goce
            </li>
          </ul>

          <DialogRoot v-model:open="tutorialOpen">
            <DialogTrigger as-child>
              <Button variant="outline" class="h-8 bg-white px-2.5 text-xs" type="button">
                <CircleHelpIcon data-icon="inline-start" class="size-3.5" />
                Ayuda
              </Button>
            </DialogTrigger>
            <DialogPortal>
              <DialogOverlay
                class="fixed inset-0 z-[90] bg-palette-navy/45 backdrop-blur-[1px] data-[state=closed]:animate-out data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0"
              />
              <DialogContent
                class="fixed top-1/2 left-1/2 z-[100] flex max-h-[calc(100vh-2rem)] w-[calc(100%-2rem)] max-w-2xl -translate-x-1/2 -translate-y-1/2 flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl data-[state=closed]:animate-out data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95"
              >
                <header class="flex items-start justify-between gap-4 border-b border-slate-200 px-5 py-4 sm:px-6">
                  <div>
                    <DialogTitle class="text-lg font-semibold text-palette-navy sm:text-xl">
                      Cómo registrar el tareo diario
                    </DialogTitle>
                    <DialogDescription class="mt-1 text-sm leading-6 text-slate-600">
                      Cada día muestra una letra. Haga clic en la letra para editar horas, extras y el código.
                    </DialogDescription>
                  </div>
                  <DialogClose as-child>
                    <Button variant="ghost" size="icon" class="shrink-0" type="button" aria-label="Cerrar tutorial">
                      <XIcon />
                    </Button>
                  </DialogClose>
                </header>

                <div class="overflow-y-auto px-5 py-5 sm:px-6">
                  <div class="rounded-xl border border-sky-200 bg-sky-50 p-4 text-sm leading-6 text-sky-950">
                    <strong>Clic en la letra del día</strong> para abrir horas, código, HE 25%, HE 35% y tardanza.
                    Jornada completa: código <strong>A</strong>, <strong>8</strong> horas y extras en <strong>0</strong>.
                  </div>

                  <ol class="mt-5 space-y-4">
                    <li class="grid gap-3 rounded-xl border border-slate-200 p-4 sm:grid-cols-[2rem_1fr]">
                      <span class="flex size-8 items-center justify-center rounded-full bg-palette-navy text-sm font-bold text-white">1</span>
                      <div>
                        <h4 class="font-semibold text-palette-navy">Leer el mes de un vistazo</h4>
                        <p class="mt-1 text-sm leading-6 text-slate-600">
                          Cada columna es un día del 1 al 31. La letra es el código real (A, FI, DM, VAC…).
                          Una <strong>T</strong> pequeña indica tardanza, no un código nuevo.
                        </p>
                      </div>
                    </li>
                    <li class="grid gap-3 rounded-xl border border-slate-200 p-4 sm:grid-cols-[2rem_1fr]">
                      <span class="flex size-8 items-center justify-center rounded-full bg-palette-navy text-sm font-bold text-white">2</span>
                      <div>
                        <h4 class="font-semibold text-palette-navy">Editar el día</h4>
                        <p class="mt-1 text-sm leading-6 text-slate-600">
                          Clic en la letra. Cambie horas, el código y los minutos de extras o tardanza.
                        </p>
                      </div>
                    </li>
                    <li class="grid gap-3 rounded-xl border border-slate-200 p-4 sm:grid-cols-[2rem_1fr]">
                      <span class="flex size-8 items-center justify-center rounded-full bg-palette-navy text-sm font-bold text-white">3</span>
                      <div>
                        <h4 class="font-semibold text-palette-navy">Guardar</h4>
                        <p class="mt-1 text-sm leading-6 text-slate-600">
                          Use <strong>Guardar</strong> o <strong>Ctrl+S</strong>. La fecha de cese no se edita aquí.
                        </p>
                      </div>
                    </li>
                  </ol>
                </div>

                <footer class="flex justify-end border-t border-slate-200 px-5 py-4 sm:px-6">
                  <DialogClose as-child>
                    <Button class="h-10 bg-palette-navy px-5 text-white hover:bg-palette-royal" type="button">
                      Entendido
                    </Button>
                  </DialogClose>
                </footer>
              </DialogContent>
            </DialogPortal>
          </DialogRoot>

          <Button
            class="h-8 bg-palette-navy px-3 text-xs text-white hover:bg-palette-royal"
            type="button"
            :disabled="acting || loading"
            :aria-busy="acting"
            @click="onGenerar"
          >
            {{ acting ? 'Generando…' : planilla?.generado ? 'Completar días' : 'Generar mes' }}
          </Button>
          <Button
            class="h-8 bg-palette-navy px-3 text-xs text-white hover:bg-palette-royal"
            type="button"
            :disabled="saving || dirtyCount === 0"
            :aria-busy="saving"
            :title="dirtyCount ? 'Atajo: Ctrl+S' : undefined"
            @click="onGuardar"
          >
            {{ saving ? 'Guardando…' : dirtyCount ? `Guardar ${dirtyCount}` : 'Guardar' }}
          </Button>
        </div>
      </div>

      <div
        v-if="dirtyCount"
        class="mt-2 flex flex-wrap items-center justify-between gap-2 rounded-md border border-amber-300 bg-amber-50 px-2.5 py-1.5 text-xs text-amber-950"
        role="status"
      >
        <p>
          {{ dirtyCount }} celda{{ dirtyCount === 1 ? '' : 's' }} sin guardar.
          <span class="text-amber-900">Ctrl+S</span>
        </p>
        <Button variant="outline" size="sm" class="h-7 px-2 text-xs" type="button" @click="onDescartar">
          Descartar
        </Button>
      </div>
    </header>

    <main class="flex min-h-0 flex-1 flex-col p-2">
      <div
        v-if="loading && !planilla"
        class="flex flex-1 items-center justify-center rounded-lg border border-dashed border-palette-cyan/70 bg-white"
      >
        <p class="text-sm text-slate-600">Cargando planilla…</p>
      </div>

      <div
        v-else-if="planilla && !planilla.generado"
        class="flex flex-1 flex-col items-center justify-center gap-3 rounded-lg border border-dashed border-palette-cyan/80 bg-white px-4 text-center"
      >
        <p class="text-sm text-slate-600">
          Aún no hay registros para {{ tituloPeriodo }}. Genere la planilla con el personal de contrato activo.
        </p>
        <Button
          class="h-9 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="acting"
          @click="onGenerar"
        >
          Generar {{ tituloPeriodo }}
        </Button>
      </div>

      <AsistenciaPlanillaGrid
        v-else-if="planilla"
        class="min-h-0 flex-1"
        compact
        :planilla="planilla"
        :filas-vista="filasVista"
        :loading="loading"
        :hoy-iso="hoyIso"
        :titulo-periodo="tituloPeriodo"
        :horas-de="horasDe"
        :estado-de="estadoDe"
        :codigo-de="codigoDe"
        :minutos-de="minutosDe"
        :horas-bloqueadas="horasBloqueadas"
        :estado-select-clase="estadoSelectClase"
        @horas="onHoras"
        @codigo="onCodigo"
        @minutos="onMinutos"
      />
    </main>
  </div>
</template>
