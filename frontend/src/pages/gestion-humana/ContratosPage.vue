<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon, DownloadIcon, PlusIcon } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'

import ContratoRegistroModal from '@/components/gestion-humana/ContratoRegistroModal.vue'
import { Button } from '@/components/ui/button'
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
import { listarContratos, obtenerContratosResume } from '@/services/contrato/contratoService'
import { contratoDocumentoUrl, type ContratoDTO, type ContratosResume } from '@/types/contrato'
import type { PagedResponse } from '@/types/persona'

const PAGE_SIZE = 20
const loading = ref(true)
const resume = ref<ContratosResume | null>(null)
const data = ref<PagedResponse<ContratoDTO> | null>(null)
const page = ref(0)
const modalOpen = ref(false)

const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)

function formatFecha(value: string | null): string {
  if (!value) {
    return '—'
  }
  const parsed = new Date(`${value}T00:00:00`)
  if (Number.isNaN(parsed.getTime())) {
    return value
  }
  return parsed.toLocaleDateString('es-PE')
}

function estadoLabel(estado: string): string {
  const labels: Record<string, string> = {
    ACTIVO: 'Activo',
    RESCINDIDO: 'Rescindido',
    VENCIDO: 'Vencido',
  }
  return labels[estado] ?? estado
}

function onDisplayPageChange(nextPage: number) {
  page.value = Math.max(0, nextPage - 1)
  void load()
}

function descargar(id: number) {
  window.open(contratoDocumentoUrl(id), '_blank', 'noopener,noreferrer')
}

async function load() {
  loading.value = true
  try {
    const [nextResume, nextData] = await Promise.all([
      obtenerContratosResume(),
      listarContratos(page.value, PAGE_SIZE),
    ])
    resume.value = nextResume
    data.value = nextData
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar contratos.')
  } finally {
    loading.value = false
  }
}

function onSaved() {
  modalOpen.value = false
  page.value = 0
  void load()
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Contratos</h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Personas con al menos un contrato activo y personas sin contrato activo.
        </p>
      </div>
      <Button
        class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:mt-7 sm:w-auto"
        type="button"
        @click="modalOpen = true"
      >
        <PlusIcon data-icon="inline-start" />
        Nuevo contrato
      </Button>
    </div>

    <div class="mt-6 grid gap-4 sm:grid-cols-2">
      <template v-if="loading && !resume">
        <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <Skeleton class="h-4 w-32" />
          <Skeleton class="mt-4 h-10 w-16" />
        </div>
        <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <Skeleton class="h-4 w-32" />
          <Skeleton class="mt-4 h-10 w-16" />
        </div>
      </template>
      <template v-else-if="resume">
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Con contrato activo
          </p>
          <p class="mt-3 text-3xl font-bold text-palette-navy">
            {{ resume.conContratoActivo }}
          </p>
        </article>
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Sin contrato activo
          </p>
          <p class="mt-3 text-3xl font-bold text-palette-navy">
            {{ resume.sinContratoActivo }}
          </p>
        </article>
      </template>
    </div>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <h3 class="text-base font-semibold text-palette-navy">Contratos generados</h3>
      <p class="mt-1 text-sm text-slate-500">
        El documento se arma al descargar con los datos del formulario y las palabras clave de la plantilla.
      </p>

      <div
        v-if="loading && !data"
        class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60"
        aria-busy="true"
        aria-live="polite"
      >
        <span class="sr-only">Cargando listado de contratos</span>
        <div class="space-y-3 p-4">
          <Skeleton class="h-9 w-full" />
          <Skeleton v-for="row in 5" :key="row" class="h-11 w-full" />
        </div>
      </div>

      <div v-else class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60">
        <Table>
          <TableCaption class="sr-only">Listado paginado de contratos.</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead class="px-3">Persona</TableHead>
              <TableHead class="px-3">Documento</TableHead>
              <TableHead class="px-3">Cargo</TableHead>
              <TableHead class="px-3">Proyecto</TableHead>
              <TableHead class="px-3">Fechas</TableHead>
              <TableHead class="px-3">Estado</TableHead>
              <TableHead class="px-3">
                <span class="sr-only">Descargar</span>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!data?.content.length" :colspan="7">
              <span class="text-slate-500">No hay contratos generados.</span>
            </TableEmpty>
            <TableRow v-for="contrato in data?.content ?? []" :key="contrato.id">
              <TableCell class="px-3 py-3 font-medium">
                {{ contrato.personaApellidos }}, {{ contrato.personaNombres }}
              </TableCell>
              <TableCell class="px-3 py-3">{{ contrato.numeroDocumento }}</TableCell>
              <TableCell class="px-3 py-3">{{ contrato.cargo }}</TableCell>
              <TableCell class="max-w-56 truncate px-3 py-3" :title="contrato.proyecto">
                {{ contrato.proyecto }}
              </TableCell>
              <TableCell class="px-3 py-3 whitespace-nowrap text-slate-500">
                {{ formatFecha(contrato.fechaInicio) }} – {{ formatFecha(contrato.fechaFin) }}
              </TableCell>
              <TableCell class="px-3 py-3">{{ estadoLabel(contrato.estado) }}</TableCell>
              <TableCell class="px-3 py-3">
                <div class="flex justify-end">
                  <Button
                    variant="outline"
                    size="sm"
                    class="h-8 px-3"
                    type="button"
                    @click="descargar(contrato.id)"
                  >
                    <DownloadIcon data-icon="inline-start" />
                    Descargar
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>

      <div class="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
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
          aria-label="Paginación de contratos"
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

    <ContratoRegistroModal :open="modalOpen" @close="modalOpen = false" @saved="onSaved" />
  </section>
</template>
