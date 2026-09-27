<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { watchDebounced } from '@vueuse/core'
import {
  ArrowDownIcon,
  ArrowUpDownIcon,
  ArrowUpIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  SearchIcon,
} from '@lucide/vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
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
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { listarPersonas } from '@/services/persona/personaService'
import { type PagedResponse, type PersonaResumenDTO } from '@/types/persona'

const PAGE_SIZE = 20
const SORTABLE = ['apellidos', 'nombres', 'numeroDocumento', 'createdAt'] as const
type SortField = (typeof SORTABLE)[number]

const props = defineProps<{
  reloadKey?: number
}>()

const searchInput = ref('')
const q = ref('')
const page = ref(0)
const sortField = ref<SortField>('apellidos')
const sortDir = ref<'asc' | 'desc'>('asc')
const loading = ref(true)
const data = ref<PagedResponse<PersonaResumenDTO> | null>(null)

const sort = computed(() => `${sortField.value},${sortDir.value}`)
const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)

function ariaSort(field: SortField): 'ascending' | 'descending' | 'none' {
  if (sortField.value !== field) {
    return 'none'
  }
  return sortDir.value === 'asc' ? 'ascending' : 'descending'
}

function sortLabel(field: SortField): string {
  const labels: Record<SortField, string> = {
    apellidos: 'apellidos',
    nombres: 'nombres',
    numeroDocumento: 'documento',
    createdAt: 'fecha de registro',
  }
  if (sortField.value !== field) {
    return `Ordenar por ${labels[field]}`
  }
  return sortDir.value === 'asc'
    ? `${labels[field]}, orden ascendente. Activar para descendente`
    : `${labels[field]}, orden descendente. Activar para ascendente`
}

function toggleSort(field: SortField) {
  if (sortField.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDir.value = 'asc'
  }
  page.value = 0
}

function onDisplayPageChange(nextPage: number) {
  page.value = Math.max(0, nextPage - 1)
}

async function load() {
  loading.value = true
  try {
    data.value = await listarPersonas({
      page: page.value,
      size: PAGE_SIZE,
      sort: sort.value,
      q: q.value,
    })
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el listado.')
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

watch([page, q, sort, () => props.reloadKey], () => {
  void load()
}, { immediate: true })
</script>

<template>
  <div class="space-y-5">
    <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
      <div class="min-w-0">
        <h3 class="text-base font-semibold text-palette-navy">Padrón de personas</h3>
        <p class="mt-1 text-sm text-slate-500">
          Busque por nombre, documento o correo. Pulse una cabecera para ordenar.
        </p>
      </div>
      <div class="flex w-full flex-col gap-3 sm:flex-row sm:items-center lg:w-auto">
        <div class="w-full sm:min-w-[18rem] sm:flex-1 lg:w-80 lg:flex-none">
          <label class="sr-only" for="persona-busqueda">Buscar personas</label>
          <InputGroup>
            <InputGroupAddon>
              <SearchIcon class="size-4" aria-hidden="true" />
            </InputGroupAddon>
            <InputGroupInput
              id="persona-busqueda"
              v-model="searchInput"
              type="search"
              placeholder="Nombre, documento o correo"
              autocomplete="off"
            />
          </InputGroup>
        </div>
        <div class="shrink-0">
          <slot name="actions" />
        </div>
      </div>
    </div>

    <div
      v-if="loading"
      class="overflow-hidden rounded-lg border border-palette-cyan/60"
      aria-busy="true"
      aria-live="polite"
    >
      <span class="sr-only">Cargando listado de personas</span>
      <div class="space-y-3 p-4">
        <Skeleton class="h-9 w-full" />
        <Skeleton v-for="row in 8" :key="row" class="h-11 w-full" />
      </div>
    </div>

    <div v-else class="overflow-hidden rounded-lg border border-palette-cyan/60">
      <Table>
        <TableCaption class="sr-only">
          Listado paginado de personas del padrón de personal.
        </TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead :aria-sort="ariaSort('apellidos')" class="px-3">
              <Button
                variant="ghost"
                size="sm"
                class="h-8 gap-1.5 px-2.5 font-medium"
                type="button"
                :aria-label="sortLabel('apellidos')"
                @click="toggleSort('apellidos')"
              >
                Apellidos
                <ArrowUpIcon v-if="sortField === 'apellidos' && sortDir === 'asc'" class="size-3.5" />
                <ArrowDownIcon v-else-if="sortField === 'apellidos'" class="size-3.5" />
                <ArrowUpDownIcon v-else class="size-3.5 opacity-40" />
              </Button>
            </TableHead>
            <TableHead :aria-sort="ariaSort('nombres')" class="px-3">
              <Button
                variant="ghost"
                size="sm"
                class="h-8 gap-1.5 px-2.5 font-medium"
                type="button"
                :aria-label="sortLabel('nombres')"
                @click="toggleSort('nombres')"
              >
                Nombres
                <ArrowUpIcon v-if="sortField === 'nombres' && sortDir === 'asc'" class="size-3.5" />
                <ArrowDownIcon v-else-if="sortField === 'nombres'" class="size-3.5" />
                <ArrowUpDownIcon v-else class="size-3.5 opacity-40" />
              </Button>
            </TableHead>
            <TableHead :aria-sort="ariaSort('numeroDocumento')" class="px-3">
              <Button
                variant="ghost"
                size="sm"
                class="h-8 gap-1.5 px-2.5 font-medium"
                type="button"
                :aria-label="sortLabel('numeroDocumento')"
                @click="toggleSort('numeroDocumento')"
              >
                Doc. de identidad
                <ArrowUpIcon v-if="sortField === 'numeroDocumento' && sortDir === 'asc'" class="size-3.5" />
                <ArrowDownIcon v-else-if="sortField === 'numeroDocumento'" class="size-3.5" />
                <ArrowUpDownIcon v-else class="size-3.5 opacity-40" />
              </Button>
            </TableHead>
            <TableHead class="px-3">Correo</TableHead>
            <TableHead :aria-sort="ariaSort('createdAt')" class="px-3">
              <Button
                variant="ghost"
                size="sm"
                class="h-8 gap-1.5 px-2.5 font-medium"
                type="button"
                :aria-label="sortLabel('createdAt')"
                @click="toggleSort('createdAt')"
              >
                Registro
                <ArrowUpIcon v-if="sortField === 'createdAt' && sortDir === 'asc'" class="size-3.5" />
                <ArrowDownIcon v-else-if="sortField === 'createdAt'" class="size-3.5" />
                <ArrowUpDownIcon v-else class="size-3.5 opacity-40" />
              </Button>
            </TableHead>
            <TableHead class="px-3">
              <span class="sr-only">Indicadores</span>
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableEmpty v-if="!data?.content.length" :colspan="6">
            <span class="text-slate-500">No hay personas registradas.</span>
          </TableEmpty>
          <TableRow v-for="persona in data?.content ?? []" :key="persona.id">
            <TableCell class="px-3 py-3 font-medium">{{ persona.apellidos }}</TableCell>
            <TableCell class="px-3 py-3">{{ persona.nombres }}</TableCell>
            <TableCell class="px-3 py-3">{{ persona.tipoDocumento }} {{ persona.numeroDocumento }}</TableCell>
            <TableCell class="max-w-[14rem] truncate px-3 py-3" :title="persona.correo">
              {{ persona.correo }}
            </TableCell>
            <TableCell class="px-3 py-3 whitespace-nowrap text-slate-500">
              {{ new Date(persona.createdAt).toLocaleDateString('es-PE') }}
            </TableCell>
            <TableCell class="px-3 py-3">
              <div class="flex items-center gap-1.5" aria-label="Indicadores pendientes">
                <span
                  class="size-3.5 rounded-full bg-emerald-500"
                  title="Activo"
                  aria-label="Indicador activo"
                />
                <span
                  class="size-3.5 rounded-full border-2 border-slate-300 bg-transparent"
                  title="Pendiente"
                  aria-hidden="true"
                />
                <span
                  class="size-3.5 rounded-full border-2 border-slate-300 bg-transparent"
                  title="Pendiente"
                  aria-hidden="true"
                />
                <span
                  class="size-3.5 rounded-full border-2 border-slate-300 bg-transparent"
                  title="Pendiente"
                  aria-hidden="true"
                />
              </div>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <p class="text-sm text-slate-500" aria-live="polite">
        Página {{ displayPage }} de {{ Math.max(totalPages, 1) }} · {{ totalElements }} registros
      </p>
      <Pagination
        v-if="totalPages > 1"
        v-slot="{ page: current }"
        class="mx-0 w-auto justify-start sm:justify-end"
        :page="displayPage"
        :items-per-page="PAGE_SIZE"
        :total="totalElements"
        :disabled="loading"
        aria-label="Paginación del padrón"
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
</template>
