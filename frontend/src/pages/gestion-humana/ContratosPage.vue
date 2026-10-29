<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon, DownloadIcon, PlusIcon } from '@lucide/vue'
import { computed, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import AdendaRegistroModal from '@/components/gestion-humana/AdendaRegistroModal.vue'
import ContratoRegistroModal from '@/components/gestion-humana/ContratoRegistroModal.vue'
import { Badge } from '@/components/ui/badge'
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { listarAdendas, listarContratos, obtenerContratosResume } from '@/services/contrato/contratoService'
import { openAuthenticatedDocument } from '@/services/http'
import {
  adendaDocumentoUrl,
  contratoDocumentoUrl,
  type AdendaDTO,
  type ContratoDTO,
  type ContratosResume,
} from '@/types/contrato'
import type { PagedResponse } from '@/types/persona'

const PAGE_SIZE = 20
const tab = ref('contratos')
const loading = ref(true)
const loadingAdendas = ref(true)
const resume = ref<ContratosResume | null>(null)
const data = ref<PagedResponse<ContratoDTO> | null>(null)
const adendas = ref<PagedResponse<AdendaDTO> | null>(null)
const page = ref(0)
const adendaPage = ref(0)
const modalOpen = ref(false)
const adendaModalOpen = ref(false)
const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })

const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)
const adendaDisplayPage = computed(() => adendaPage.value + 1)
const adendaTotalPages = computed(() => adendas.value?.totalPages ?? 0)
const adendaTotalElements = computed(() => adendas.value?.totalElements ?? 0)

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

function etiquetaPlantilla(contrato: ContratoDTO): string {
  const nombre = contrato.tipoPlantillaNombre?.trim()
  return nombre || contrato.tipoPlantillaCodigo?.trim() || 'Sin plantilla'
}

function onDisplayPageChange(nextPage: number) {
  page.value = Math.max(0, nextPage - 1)
  void loadContratos()
}

function onAdendaPageChange(nextPage: number) {
  adendaPage.value = Math.max(0, nextPage - 1)
  void loadAdendas()
}

async function descargar(id: number) {
  await openAuthenticatedDocument(contratoDocumentoUrl(id))
}

async function descargarAdenda(id: number) {
  await openAuthenticatedDocument(adendaDocumentoUrl(id))
}

async function loadContratos() {
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

async function loadAdendas() {
  loadingAdendas.value = true
  try {
    adendas.value = await listarAdendas(adendaPage.value, PAGE_SIZE)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las adendas.')
  } finally {
    loadingAdendas.value = false
  }
}

function onSaved() {
  modalOpen.value = false
  page.value = 0
  void loadContratos()
}

function onAdendaSaved() {
  adendaModalOpen.value = false
  adendaPage.value = 0
  void loadAdendas()
  void loadContratos()
}

function onNuevo() {
  if (tab.value === 'adendas') {
    adendaModalOpen.value = true
  } else {
    modalOpen.value = true
  }
}

watch(tab, (value) => {
  if (value === 'adendas' && !adendas.value) {
    void loadAdendas()
  }
})

onMounted(() => {
  void loadContratos()
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Contratos</h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Contratos de trabajo y adendas de prórroga asociadas a cada registro.
        </p>
      </div>
      <Button
        class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:mt-7 sm:w-auto"
        type="button"
        @click="onNuevo"
      >
        <PlusIcon data-icon="inline-start" />
        {{ tab === 'adendas' ? 'Nueva adenda' : 'Nuevo contrato' }}
      </Button>
    </div>

    <div class="mt-3 grid gap-2 sm:grid-cols-2">
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

    <Tabs v-model="tab" class="mt-3">
      <TabsList class="h-auto rounded-xl border border-slate-200 bg-slate-100/85 p-1.5">
        <TabsTrigger
          value="contratos"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Contratos
        </TabsTrigger>
        <TabsTrigger
          value="adendas"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Adendas
        </TabsTrigger>
      </TabsList>

      <TabsContent value="contratos">
        <div class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
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
            <Table class="min-w-[1260px]">
              <TableCaption class="sr-only">Listado paginado de contratos.</TableCaption>
              <TableHeader>
                <TableRow>
                  <TableHead class="px-3">Contrato</TableHead>
                  <TableHead class="px-3">Persona</TableHead>
                  <TableHead class="px-3">Vigencia</TableHead>
                  <TableHead class="px-3 text-right">Sueldo</TableHead>
                  <TableHead class="px-3">Fecha de corte</TableHead>
                  <TableHead class="px-3">Plantilla</TableHead>
                  <TableHead class="px-3">Observaciones</TableHead>
                  <TableHead class="px-3">Estado</TableHead>
                  <TableHead class="px-3">
                    <span class="sr-only">Descargar</span>
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableEmpty v-if="!data?.content.length" :colspan="9">
                  <span class="text-slate-500">No hay contratos generados.</span>
                </TableEmpty>
                <TableRow v-for="contrato in data?.content ?? []" :key="contrato.id">
                  <TableCell class="max-w-64 px-3 py-3">
                    <span class="block truncate font-medium" :title="contrato.titulo">{{ contrato.titulo }}</span>
                    <span class="block truncate text-xs text-slate-500">
                      {{
                        contrato.regimenLaboralNombre
                          ? `${contrato.regimenLaboralNombre} · ${contrato.categoriaContratoNombre}`
                          : contrato.categoriaContratoNombre
                      }}
                    </span>
                  </TableCell>
                  <TableCell class="max-w-56 px-3 py-3">
                    <span class="block truncate font-medium" :title="contrato.personaNombreCompleto">
                      {{ contrato.personaNombreCompleto }}
                    </span>
                    <span class="block text-xs text-slate-500">{{ contrato.personaNumeroDocumento }}</span>
                  </TableCell>
                  <TableCell class="px-3 py-3 whitespace-nowrap text-slate-500">
                    {{ formatFecha(contrato.fechaInicio) }} – {{ formatFecha(contrato.fechaFin) }}
                  </TableCell>
                  <TableCell class="px-3 py-3 text-right font-medium tabular-nums">
                    {{ money.format(contrato.sueldo) }}
                  </TableCell>
                  <TableCell class="px-3 py-3 text-slate-500">
                    {{ formatFecha(contrato.fechaCorte) }}
                  </TableCell>
                  <TableCell class="max-w-56 px-3 py-3">
                    <Badge
                      class="max-w-full"
                      :title="contrato.tipoPlantillaCodigo"
                    >
                      <span class="truncate">{{ etiquetaPlantilla(contrato) }}</span>
                    </Badge>
                  </TableCell>
                  <TableCell class="max-w-64 whitespace-normal px-3 py-3 text-slate-500">
                    <span class="line-clamp-2" :title="contrato.observaciones ?? undefined">
                      {{ contrato.observaciones || '—' }}
                    </span>
                  </TableCell>
                  <TableCell class="px-3 py-3">
                    <Badge :variant="contrato.activo ? 'success' : 'secondary'">
                      {{ contrato.activo ? 'Activo' : 'Inactivo' }}
                    </Badge>
                  </TableCell>
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
      </TabsContent>

      <TabsContent value="adendas">
        <div class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
          <h3 class="text-base font-semibold text-palette-navy">Adendas de prórroga</h3>
          <p class="mt-1 text-sm text-slate-500">
            Cada adenda se asocia a un contrato activo y actualiza su fecha de término.
          </p>

          <div
            v-if="loadingAdendas && !adendas"
            class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60"
            aria-busy="true"
          >
            <span class="sr-only">Cargando listado de adendas</span>
            <div class="space-y-3 p-4">
              <Skeleton class="h-9 w-full" />
              <Skeleton v-for="row in 5" :key="row" class="h-11 w-full" />
            </div>
          </div>

          <div v-else class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60">
            <Table>
              <TableCaption class="sr-only">Listado paginado de adendas.</TableCaption>
              <TableHeader>
                <TableRow>
                  <TableHead class="px-3">Persona</TableHead>
                  <TableHead class="px-3">N.º</TableHead>
                  <TableHead class="px-3">Contrato</TableHead>
                  <TableHead class="px-3">Prórroga</TableHead>
                  <TableHead class="px-3">Motivo</TableHead>
                  <TableHead class="px-3">
                    <span class="sr-only">Descargar</span>
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableEmpty v-if="!adendas?.content.length" :colspan="6">
                  <span class="text-slate-500">No hay adendas. Cree la primera desde un contrato activo.</span>
                </TableEmpty>
                <TableRow v-for="item in adendas?.content ?? []" :key="item.id">
                  <TableCell class="px-3 py-3 font-medium">
                    {{ item.personaApellidos }}, {{ item.personaNombres }}
                    <span class="mt-0.5 block text-xs font-normal text-slate-600">{{ item.numeroDocumento }}</span>
                  </TableCell>
                  <TableCell class="px-3 py-3">{{ item.numero }}</TableCell>
                  <TableCell class="px-3 py-3 whitespace-nowrap text-slate-600">
                    {{ formatFecha(item.contratoFechaInicio) }} – {{ formatFecha(item.fechaFinAnterior) }}
                  </TableCell>
                  <TableCell class="px-3 py-3 whitespace-nowrap text-slate-600">
                    {{ formatFecha(item.fechaFinAnterior) }} → {{ formatFecha(item.fechaFinNueva) }}
                  </TableCell>
                  <TableCell class="max-w-56 truncate px-3 py-3 text-slate-600" :title="item.motivo">
                    {{ item.motivo }}
                  </TableCell>
                  <TableCell class="px-3 py-3">
                    <div class="flex justify-end">
                      <Button
                        variant="outline"
                        size="sm"
                        class="h-8 px-3"
                        type="button"
                        :aria-label="`Descargar adenda ${item.numero} de ${item.personaApellidos}`"
                        @click="descargarAdenda(item.id)"
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
              Página {{ adendaDisplayPage }} de {{ Math.max(adendaTotalPages, 1) }} · {{ adendaTotalElements }} registros
            </p>
            <Pagination
              v-if="adendaTotalPages > 1"
              v-slot="{ page: current }"
              class="mx-0 w-auto justify-start sm:justify-end"
              :page="adendaDisplayPage"
              :items-per-page="PAGE_SIZE"
              :total="adendaTotalElements"
              :disabled="loadingAdendas"
              aria-label="Paginación de adendas"
              @update:page="onAdendaPageChange"
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
      </TabsContent>
    </Tabs>

    <ContratoRegistroModal :open="modalOpen" @close="modalOpen = false" @saved="onSaved" />
    <AdendaRegistroModal :open="adendaModalOpen" @close="adendaModalOpen = false" @saved="onAdendaSaved" />
  </section>
</template>
