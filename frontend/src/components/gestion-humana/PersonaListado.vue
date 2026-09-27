<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { watchDebounced } from '@vueuse/core'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
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
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { listarPersonas } from '@/services/persona/personaService'
import {
  GRADOS_INSTRUCCION,
  type GradoInstruccion,
  type PagedResponse,
  type PersonaResumenDTO,
} from '@/types/persona'

const PAGE_SIZE = 20
const SORTABLE = ['apellidos', 'nombres', 'numeroDocumento', 'createdAt'] as const

const props = defineProps<{
  reloadKey?: number
}>()

const searchInput = ref('')
const q = ref('')
const page = ref(0)
const sortField = ref<(typeof SORTABLE)[number]>('apellidos')
const sortDir = ref<'asc' | 'desc'>('asc')
const loading = ref(true)
const data = ref<PagedResponse<PersonaResumenDTO> | null>(null)

const sort = computed(() => `${sortField.value},${sortDir.value}`)
const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)

function labelGrado(value: GradoInstruccion): string {
  return GRADOS_INSTRUCCION.find((grado) => grado.value === value)?.label ?? value
}

function sortIndicator(field: (typeof SORTABLE)[number]): string {
  if (sortField.value !== field) {
    return ''
  }
  return sortDir.value === 'asc' ? ' ↑' : ' ↓'
}

function toggleSort(field: (typeof SORTABLE)[number]) {
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
  <div class="space-y-4">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <p class="text-sm text-slate-500">Personas registradas en el padrón de personal.</p>
      <div class="flex flex-col gap-2 sm:flex-row sm:items-center">
        <Input
          v-model="searchInput"
          class="sm:max-w-xs"
          type="search"
          placeholder="Buscar por nombre, documento o correo"
        />
        <slot name="actions" />
      </div>
    </div>

    <div v-if="loading" class="overflow-hidden rounded-lg border border-palette-cyan/60">
      <div class="space-y-3 p-4">
        <Skeleton class="h-8 w-full" />
        <Skeleton v-for="row in 8" :key="row" class="h-10 w-full" />
      </div>
    </div>

    <div v-else class="overflow-hidden rounded-lg border border-palette-cyan/60">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>
              <Button variant="ghost" size="sm" class="-ml-2" type="button" @click="toggleSort('apellidos')">
                Apellidos{{ sortIndicator('apellidos') }}
              </Button>
            </TableHead>
            <TableHead>
              <Button variant="ghost" size="sm" class="-ml-2" type="button" @click="toggleSort('nombres')">
                Nombres{{ sortIndicator('nombres') }}
              </Button>
            </TableHead>
            <TableHead>
              <Button variant="ghost" size="sm" class="-ml-2" type="button" @click="toggleSort('numeroDocumento')">
                Documento{{ sortIndicator('numeroDocumento') }}
              </Button>
            </TableHead>
            <TableHead>Correo</TableHead>
            <TableHead>Grado</TableHead>
            <TableHead>Departamento</TableHead>
            <TableHead>Distrito</TableHead>
            <TableHead>
              <Button variant="ghost" size="sm" class="-ml-2" type="button" @click="toggleSort('createdAt')">
                Registro{{ sortIndicator('createdAt') }}
              </Button>
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableEmpty v-if="!data?.content.length" :colspan="8">
            <span class="text-slate-500">No hay personas registradas.</span>
          </TableEmpty>
          <TableRow v-for="persona in data?.content ?? []" :key="persona.id">
            <TableCell class="font-medium">{{ persona.apellidos }}</TableCell>
            <TableCell>{{ persona.nombres }}</TableCell>
            <TableCell>{{ persona.tipoDocumento }} {{ persona.numeroDocumento }}</TableCell>
            <TableCell>{{ persona.correo }}</TableCell>
            <TableCell>{{ labelGrado(persona.gradoInstruccion) }}</TableCell>
            <TableCell>{{ persona.departamento }}</TableCell>
            <TableCell>{{ persona.distrito }}</TableCell>
            <TableCell class="whitespace-nowrap text-slate-500">
              {{ new Date(persona.createdAt).toLocaleDateString('es-PE') }}
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <p class="text-xs text-slate-500">
        Página {{ displayPage }} de {{ Math.max(totalPages, 1) }} · {{ totalElements }} registros
      </p>
      <Pagination
        v-if="totalPages > 1"
        v-slot="{ page: current }"
        :page="displayPage"
        :items-per-page="PAGE_SIZE"
        :total="totalElements"
        :disabled="loading"
        @update:page="onDisplayPageChange"
      >
        <PaginationContent v-slot="{ items }">
          <PaginationPrevious>
            <span class="hidden sm:block">Anterior</span>
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
            <span class="hidden sm:block">Siguiente</span>
          </PaginationNext>
        </PaginationContent>
      </Pagination>
    </div>
  </div>
</template>
