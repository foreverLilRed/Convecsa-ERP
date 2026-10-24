<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon, SearchIcon } from '@lucide/vue'
import { computed, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { watchDebounced } from '@vueuse/core'
import { toast } from 'vue-sonner'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import { Skeleton } from '@/components/ui/skeleton'
import { listarColaboradoresDesempeno } from '@/services/desempeno/desempenoService'
import {
  estadoEvaluacionLabel,
  formatPeriodo,
  iniciales,
  parsePeriodo,
  periodoActual,
  periodoLabel,
  TRIMESTRES,
  type ColaboradorDesempeno,
} from '@/types/desempeno'
import type { PagedResponse } from '@/types/persona'

const PAGE_SIZE = 20
const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const route = useRoute()
const inicial = parsePeriodo(
  typeof route.query.periodo === 'string' ? route.query.periodo : periodoActual(),
)
const anio = ref(inicial.anio)
const trimestre = ref(inicial.trimestre)
const searchInput = ref('')
const q = ref('')
const page = ref(0)
const loading = ref(true)
const data = ref<PagedResponse<ColaboradorDesempeno> | null>(null)
const fotoFallida = reactive<Record<number, boolean>>({})

const periodo = computed(() => formatPeriodo(anio.value, trimestre.value))
const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)

function onDisplayPageChange(nextPage: number) {
  page.value = Math.max(0, nextPage - 1)
}

async function load() {
  loading.value = true
  try {
    data.value = await listarColaboradoresDesempeno({
      periodo: periodo.value,
      page: page.value,
      size: PAGE_SIZE,
      q: q.value,
    })
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el desempeño.')
  } finally {
    loading.value = false
  }
}

watchDebounced(
  searchInput,
  (value) => {
    q.value = value.trim()
    page.value = 0
  },
  { debounce: 300 },
)

watch(
  () => route.query.periodo,
  (value) => {
    if (typeof value !== 'string' || !value) {
      return
    }
    const parsed = parsePeriodo(value)
    anio.value = parsed.anio
    trimestre.value = parsed.trimestre
  },
)

watch([anio, trimestre], () => {
  page.value = 0
})

watch([periodo, page, q], () => {
  void load()
}, { immediate: true })
</script>

<template>
  <section>
    <div>
      <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">KPIs y desempeño</h2>
      <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-600">
        Evaluación de competencias y cumplimiento de objetivos por trimestre. El puntaje de cada persona es el promedio de las cinco competencias.
      </p>
    </div>

    <div class="mt-3 flex flex-col gap-3 rounded-lg border border-palette-cyan/70 bg-white px-4 py-4 sm:flex-row sm:items-end sm:justify-between sm:px-6">
      <div class="flex flex-wrap items-end gap-3">
        <div>
          <label class="text-sm font-medium text-slate-700" for="desempeno-anio">Año</label>
          <input
            id="desempeno-anio"
            v-model.number="anio"
            type="number"
            min="2000"
            max="2100"
            :class="inputClass"
            :disabled="loading"
          />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="desempeno-trimestre">Trimestre</label>
          <select
            id="desempeno-trimestre"
            v-model.number="trimestre"
            :class="inputClass"
            :disabled="loading"
          >
            <option v-for="item in TRIMESTRES" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
      </div>
      <p class="text-sm text-slate-600">Periodo {{ periodoLabel(periodo) }}</p>
    </div>

    <div class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h3 class="text-base font-semibold text-palette-navy">Colaboradores</h3>
          <p class="mt-1 text-sm text-slate-600">
            Pulse una tarjeta para registrar o revisar la evaluación del trimestre.
          </p>
        </div>
        <div class="w-full sm:max-w-sm">
          <label class="sr-only" for="desempeno-busqueda">Buscar colaboradores</label>
          <InputGroup>
            <InputGroupAddon>
              <SearchIcon class="size-4" aria-hidden="true" />
            </InputGroupAddon>
            <InputGroupInput
              id="desempeno-busqueda"
              v-model="searchInput"
              type="search"
              placeholder="Nombre o documento"
              autocomplete="off"
            />
          </InputGroup>
        </div>
      </div>

      <div
        v-if="loading && !data"
        class="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-3"
        aria-busy="true"
      >
        <span class="sr-only">Cargando colaboradores</span>
        <Skeleton v-for="item in 6" :key="item" class="h-36 w-full rounded-lg" />
      </div>

      <p
        v-else-if="!data?.content.length"
        class="mt-5 rounded-lg border border-dashed border-palette-cyan/80 bg-palette-ice/40 px-4 py-10 text-center text-sm text-slate-600"
      >
        No hay personas para mostrar en este periodo.
      </p>

      <ul
        v-else
        class="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-3"
        aria-label="Colaboradores del periodo"
      >
        <li v-for="item in data.content" :key="item.personaId">
          <RouterLink
            :to="{
              name: 'gh-kpis-detalle',
              params: { personaId: String(item.personaId) },
              query: { periodo },
            }"
            class="flex h-full gap-3 rounded-lg border border-palette-cyan/70 bg-white p-4 outline-none transition hover:border-palette-royal hover:bg-palette-ice/50 focus-visible:ring-2 focus-visible:ring-sky-500/40"
            :aria-label="`Evaluar a ${item.apellidos}, ${item.nombres}. ${item.promedio == null ? 'Sin evaluar' : `Promedio ${item.promedio} de 5`}`"
          >
            <span
              class="flex size-12 shrink-0 items-center justify-center overflow-hidden rounded-full bg-palette-ice text-sm font-bold text-palette-navy"
              aria-hidden="true"
            >
              <img
                v-if="item.fotoUrl && !fotoFallida[item.personaId]"
                :src="item.fotoUrl"
                :alt="''"
                class="size-12 object-cover"
                @error="fotoFallida[item.personaId] = true"
              />
              <span v-else>{{ iniciales(item.nombres, item.apellidos) }}</span>
            </span>
            <span class="min-w-0 flex-1">
              <span class="block truncate font-semibold text-palette-navy">
                {{ item.apellidos }}, {{ item.nombres }}
              </span>
              <span class="mt-0.5 block truncate text-xs text-slate-600">
                {{ item.numeroDocumento }}
                <template v-if="item.cargo"> · {{ item.cargo }}</template>
              </span>
              <span v-if="item.proyecto" class="mt-0.5 block truncate text-xs text-slate-500">
                {{ item.proyecto }}
              </span>
              <span class="mt-2 flex items-baseline justify-between gap-2">
                <span
                  class="text-[11px] font-semibold uppercase tracking-wide"
                  :class="item.estado === 'COMPLETADA'
                    ? 'text-emerald-800'
                    : item.estado === 'PENDIENTE'
                      ? 'text-amber-800'
                      : 'text-slate-600'"
                >
                  {{ estadoEvaluacionLabel(item.estado) }}
                </span>
                <span v-if="item.promedio != null" class="text-lg font-bold tabular-nums text-palette-navy">
                  {{ item.promedio }}
                  <span class="text-xs font-medium text-slate-600">/ 5</span>
                </span>
                <span v-else class="text-sm font-medium text-slate-500">Sin puntaje</span>
              </span>
            </span>
          </RouterLink>
        </li>
      </ul>

      <div class="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p class="text-sm text-slate-600" aria-live="polite">
          Página {{ displayPage }} de {{ Math.max(totalPages, 1) }} · {{ totalElements }} personas
        </p>
        <Pagination
          v-if="totalPages > 1"
          v-slot="{ page: current }"
          class="mx-0 w-auto justify-start sm:justify-end"
          :page="displayPage"
          :items-per-page="PAGE_SIZE"
          :total="totalElements"
          :disabled="loading"
          aria-label="Paginación de colaboradores"
          @update:page="onDisplayPageChange"
        >
          <PaginationContent v-slot="{ items }">
            <PaginationPrevious>
              <ChevronLeftIcon data-icon="inline-start" class="cn-rtl-flip" />
              <span>Anterior</span>
            </PaginationPrevious>
            <template v-for="(item, index) in items" :key="index">
              <PaginationItem
                v-if="item.type === 'page'"
                :value="item.value"
                :is-active="item.value === current"
              >
                {{ item.value }}
              </PaginationItem>
              <PaginationEllipsis v-else-if="item.type === 'ellipsis'" :index="index" />
            </template>
            <PaginationNext>
              <span>Siguiente</span>
              <ChevronRightIcon data-icon="inline-end" class="cn-rtl-flip" />
            </PaginationNext>
          </PaginationContent>
        </Pagination>
      </div>
    </div>
  </section>
</template>
