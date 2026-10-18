<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { watchDebounced } from '@vueuse/core'
import {
  ArrowDownIcon,
  ArrowUpDownIcon,
  ArrowUpIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  DownloadIcon,
  SearchIcon,
} from '@lucide/vue'
import {
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogOverlay,
  AlertDialogPortal,
  AlertDialogRoot,
  AlertDialogTitle,
} from 'reka-ui'
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
import {
  calcularIndicadores,
  SECCIONES_INDICADOR,
  type IndicadorSeccion,
} from '@/services/persona/indicadoresPersona'
import {
  crearLinkInvitado,
  deshabilitarPersona,
  exportarPersonas,
  habilitarPersona,
  listarPersonas,
} from '@/services/persona/personaService'
import { type PagedResponse, type PersonaResumenDTO } from '@/types/persona'

const PAGE_SIZE = 10
const SORTABLE = ['apellidos', 'nombres', 'numeroDocumento', 'createdAt'] as const
const DEFAULT_SORT = 'apellidos,asc'
type SortField = (typeof SORTABLE)[number]

const props = defineProps<{
  reloadKey?: number
}>()

const emit = defineEmits<{
  edit: [id: number]
}>()

const route = useRoute()
const router = useRouter()

function parseSort(value: unknown): { field: SortField; dir: 'asc' | 'desc' } {
  const raw = typeof value === 'string' ? value : DEFAULT_SORT
  const [fieldRaw, dirRaw] = raw.split(',')
  const field = SORTABLE.includes(fieldRaw as SortField) ? (fieldRaw as SortField) : 'apellidos'
  return { field, dir: dirRaw === 'desc' ? 'desc' : 'asc' }
}

function parsePage(value: unknown): number {
  const parsed = Number(value)
  if (!Number.isInteger(parsed) || parsed < 1) {
    return 0
  }
  return parsed - 1
}

const initialSort = parseSort(route.query.sort)
const searchInput = ref(typeof route.query.q === 'string' ? route.query.q : '')
const q = ref(searchInput.value.trim())
const page = ref(parsePage(route.query.page))
const sortField = ref<SortField>(initialSort.field)
const sortDir = ref<'asc' | 'desc'>(initialSort.dir)
const loading = ref(true)
const creatingLinkId = ref<number | null>(null)
const changingStatusIds = ref<number[]>([])
const statusDialogOpen = ref(false)
const pendingStatusPersona = ref<PersonaResumenDTO | null>(null)
const exporting = ref(false)
const data = ref<PagedResponse<PersonaResumenDTO> | null>(null)

const sort = computed(() => `${sortField.value},${sortDir.value}`)
const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)

function listQuery(): Record<string, string> {
  const next: Record<string, string> = {}
  if (page.value > 0) {
    next.page = String(page.value + 1)
  }
  if (q.value) {
    next.q = q.value
  }
  if (sort.value !== DEFAULT_SORT) {
    next.sort = sort.value
  }
  return next
}

function queryUnchanged(next: Record<string, string>): boolean {
  const current = route.query
  return (current.page ?? undefined) === next.page
    && (current.q ?? undefined) === next.q
    && (current.sort ?? undefined) === next.sort
}

function syncQuery() {
  const next = listQuery()
  if (queryUnchanged(next)) {
    return
  }
  void router.replace({ path: route.path, query: next })
}

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

async function onLinkTemporal(persona: PersonaResumenDTO) {
  if (creatingLinkId.value !== null) {
    return
  }
  creatingLinkId.value = persona.id
  try {
    const link = await crearLinkInvitado(persona.id)
    const url = `${window.location.origin}/invitar/persona/${link.token}`
    try {
      await navigator.clipboard.writeText(url)
      toast.success('Link temporal copiado. Válido 3 días.')
    } catch {
      toast.success(`Link temporal (válido 3 días): ${url}`)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo crear el link temporal.')
  } finally {
    creatingLinkId.value = null
  }
}

async function onExportar() {
  if (exporting.value) {
    return
  }
  exporting.value = true
  try {
    await exportarPersonas({ q: q.value, sort: sort.value })
    toast.success('Se descargó la plantilla AFPnet.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo exportar el padrón.')
  } finally {
    exporting.value = false
  }
}

function isChangingStatus(id: number): boolean {
  return changingStatusIds.value.includes(id)
}

async function onActivoChange(persona: PersonaResumenDTO, activo: boolean) {
  if (isChangingStatus(persona.id) || persona.activo === activo) {
    return
  }
  changingStatusIds.value = [...changingStatusIds.value, persona.id]
  try {
    const updated = activo
      ? await habilitarPersona(persona.id)
      : await deshabilitarPersona(persona.id)
    persona.activo = updated.activo
    toast.success(activo ? 'Persona activada.' : 'Persona desactivada.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cambiar el estado de la persona.')
  } finally {
    changingStatusIds.value = changingStatusIds.value.filter((id) => id !== persona.id)
  }
}

function requestActivoChange(persona: PersonaResumenDTO) {
  if (isChangingStatus(persona.id)) {
    return
  }
  pendingStatusPersona.value = persona
  statusDialogOpen.value = true
}

async function confirmActivoChange() {
  const persona = pendingStatusPersona.value
  if (!persona) {
    return
  }
  const activo = !persona.activo
  statusDialogOpen.value = false
  pendingStatusPersona.value = null
  await onActivoChange(persona, activo)
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
    const pages = data.value.totalPages
    if (pages > 0 && page.value >= pages) {
      page.value = pages - 1
    }
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
  syncQuery()
  void load()
}, { immediate: true })

watch(statusDialogOpen, (open) => {
  if (!open) {
    pendingStatusPersona.value = null
  }
})

watch(
  () => route.query,
  () => {
    const nextPage = parsePage(route.query.page)
    const nextQ = typeof route.query.q === 'string' ? route.query.q : ''
    const nextSort = parseSort(route.query.sort)
    if (
      nextPage === page.value
      && nextQ === q.value
      && nextSort.field === sortField.value
      && nextSort.dir === sortDir.value
    ) {
      return
    }
    page.value = nextPage
    q.value = nextQ
    searchInput.value = nextQ
    sortField.value = nextSort.field
    sortDir.value = nextSort.dir
  },
)

function indicadorClass(indicador: IndicadorSeccion): string {
  if (indicador === 'COMPLETO') {
    return 'bg-emerald-500'
  }
  if (indicador === 'INCOMPLETO') {
    return 'bg-amber-500'
  }
  return 'bg-red-500'
}

function indicadorLabel(seccion: string, indicador: IndicadorSeccion): string {
  const estado =
    indicador === 'COMPLETO' ? 'completo' : indicador === 'INCOMPLETO' ? 'incompleto' : 'vacío'
  return `${seccion}: ${estado}`
}
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
          <div class="flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
            <Button
              variant="outline"
              class="h-10 w-full px-4 sm:w-auto"
              type="button"
              :disabled="exporting"
              @click="onExportar"
            >
              <DownloadIcon data-icon="inline-start" />
              {{ exporting ? 'Exportando…' : 'Exportar AFPnet' }}
            </Button>
            <slot name="actions" />
          </div>
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
              <span class="sr-only">Acciones</span>
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
              <div class="flex items-center justify-end gap-2">
                <div class="flex items-center gap-1.5" aria-label="Indicadores por sección">
                  <span
                    v-for="(indicador, index) in calcularIndicadores(persona)"
                    :key="SECCIONES_INDICADOR[index]?.key"
                    class="size-3.5 rounded-full"
                    :class="indicadorClass(indicador)"
                    :title="indicadorLabel(SECCIONES_INDICADOR[index]?.label ?? '', indicador)"
                    :aria-label="indicadorLabel(SECCIONES_INDICADOR[index]?.label ?? '', indicador)"
                  />
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  class="h-8 px-3"
                  type="button"
                  @click="emit('edit', persona.id)"
                >
                  Editar
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  class="h-8 px-3"
                  type="button"
                  :disabled="creatingLinkId === persona.id"
                  @click="onLinkTemporal(persona)"
                >
                  {{ creatingLinkId === persona.id ? 'Creando…' : 'Link temporal' }}
                </Button>
                <div class="ml-1 flex items-center gap-1.5">
                  <span
                    class="text-xs font-medium"
                    :class="persona.activo ? 'text-emerald-700' : 'text-slate-500'"
                  >
                    {{ persona.activo ? 'Activo' : 'Inactivo' }}
                  </span>
                  <button
                    class="relative inline-flex h-6 w-11 shrink-0 items-center rounded-full border transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/40 disabled:cursor-not-allowed disabled:opacity-60"
                    :class="persona.activo ? 'border-emerald-600 bg-emerald-600' : 'border-slate-300 bg-slate-200'"
                    type="button"
                    role="switch"
                    :aria-checked="persona.activo"
                    :aria-label="`${persona.activo ? 'Desactivar' : 'Activar'} a ${persona.nombres} ${persona.apellidos}`"
                    :disabled="isChangingStatus(persona.id)"
                    @click="requestActivoChange(persona)"
                  >
                    <span
                      class="pointer-events-none block size-4 rounded-full bg-white shadow-sm transition-transform"
                      :class="persona.activo ? 'translate-x-6' : 'translate-x-1'"
                    />
                  </button>
                </div>
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

    <AlertDialogRoot v-model:open="statusDialogOpen">
      <AlertDialogPortal>
        <AlertDialogOverlay
          class="fixed inset-0 z-[90] bg-palette-navy/45 backdrop-blur-[1px] data-[state=closed]:animate-out data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0"
        />
        <AlertDialogContent
          class="fixed top-1/2 left-1/2 z-[100] w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 rounded-xl border border-slate-200 bg-white p-6 shadow-xl data-[state=closed]:animate-out data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95"
        >
          <AlertDialogTitle class="text-lg font-semibold text-palette-navy">
            ¿{{ pendingStatusPersona?.activo ? 'Desactivar' : 'Activar' }} persona?
          </AlertDialogTitle>
          <AlertDialogDescription class="mt-2 text-sm leading-6 text-slate-500">
            Se cambiará el estado de
            <strong class="font-semibold text-slate-700">
              {{ pendingStatusPersona?.nombres }} {{ pendingStatusPersona?.apellidos }}
            </strong>
            a {{ pendingStatusPersona?.activo ? 'inactivo' : 'activo' }}.
          </AlertDialogDescription>
          <div class="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <AlertDialogCancel as-child>
              <Button
                variant="outline"
                class="h-10 px-4"
                type="button"
                @click="pendingStatusPersona = null"
              >
                Cancelar
              </Button>
            </AlertDialogCancel>
            <AlertDialogAction as-child>
              <Button
                :variant="pendingStatusPersona?.activo ? 'destructive' : 'default'"
                class="h-10 px-4"
                :class="pendingStatusPersona?.activo ? '' : 'bg-palette-navy text-white hover:bg-palette-royal'"
                type="button"
                @click="confirmActivoChange"
              >
                Sí, {{ pendingStatusPersona?.activo ? 'desactivar' : 'activar' }}
              </Button>
            </AlertDialogAction>
          </div>
        </AlertDialogContent>
      </AlertDialogPortal>
    </AlertDialogRoot>
  </div>
</template>
