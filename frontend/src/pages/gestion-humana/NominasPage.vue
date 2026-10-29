<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon, PlusIcon, XIcon } from '@lucide/vue'
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

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
import { cerrarPeriodo, crearPeriodo, listarPeriodos } from '@/services/nomina/nominaService'
import { mesesCatalogo, periodoLabel, type PeriodoNomina } from '@/types/nomina'
import type { PagedResponse } from '@/types/persona'

const PAGE_SIZE = 20
const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const router = useRouter()
const loading = ref(true)
const closingId = ref<number | null>(null)
const data = ref<PagedResponse<PeriodoNomina> | null>(null)
const page = ref(0)
const modalOpen = ref(false)
const submitting = ref(false)
const formError = ref('')
const form = reactive({
  anio: String(new Date().getFullYear()),
  mes: String(new Date().getMonth() + 1),
  observaciones: '',
})

const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)
const meses = mesesCatalogo()

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })

function estadoLabel(estado: string): string {
  return estado === 'CERRADO' ? 'Cerrado' : 'Borrador'
}

function onDisplayPageChange(nextPage: number) {
  page.value = Math.max(0, nextPage - 1)
  void load()
}

async function load() {
  loading.value = true
  try {
    data.value = await listarPeriodos(page.value, PAGE_SIZE)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los periodos.')
  } finally {
    loading.value = false
  }
}

function lockBodyScroll() {
  const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth
  document.body.style.overflow = 'hidden'
  document.body.style.paddingRight = scrollbarWidth > 0 ? `${scrollbarWidth}px` : ''
}

function unlockBodyScroll() {
  document.body.style.overflow = ''
  document.body.style.paddingRight = ''
}

function onDocumentKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && modalOpen.value && !submitting.value) {
    modalOpen.value = false
  }
}

function resetForm() {
  form.anio = String(new Date().getFullYear())
  form.mes = String(new Date().getMonth() + 1)
  form.observaciones = ''
  formError.value = ''
  submitting.value = false
}

watch(modalOpen, (isOpen) => {
  if (isOpen) {
    resetForm()
    lockBodyScroll()
    document.addEventListener('keydown', onDocumentKeydown)
  } else {
    document.removeEventListener('keydown', onDocumentKeydown)
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onDocumentKeydown)
  unlockBodyScroll()
})

async function onCrearPeriodo() {
  formError.value = ''
  const anio = Number(form.anio)
  const mes = Number(form.mes)
  if (!Number.isInteger(anio) || anio < 2000 || anio > 2100) {
    formError.value = 'Ingrese un año válido.'
    toast.error(formError.value)
    return
  }
  if (!Number.isInteger(mes) || mes < 1 || mes > 12) {
    formError.value = 'Seleccione el mes.'
    toast.error(formError.value)
    return
  }
  submitting.value = true
  try {
    const creado = await crearPeriodo({
      anio,
      mes,
      observaciones: form.observaciones.trim() || undefined,
    })
    toast.success('Periodo creado.')
    modalOpen.value = false
    await router.push({ name: 'gh-nomina-detalle', params: { id: String(creado.id) } })
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear el periodo.'
    formError.value = message
    toast.error(message)
  } finally {
    submitting.value = false
  }
}

async function onCerrar(periodo: PeriodoNomina) {
  if (periodo.estado !== 'BORRADOR' || periodo.cantidadBoletas < 1) {
    return
  }
  closingId.value = periodo.id
  try {
    await cerrarPeriodo(periodo.id)
    toast.success('Periodo cerrado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cerrar el periodo.')
  } finally {
    closingId.value = null
  }
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
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Nóminas</h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Periodos mensuales REMYPE: genere boletas, ajuste conceptos y descargue o imprima el documento.
        </p>
      </div>
      <div class="flex w-full flex-col gap-2 sm:mt-7 sm:w-auto sm:flex-row">
        <Button
          variant="outline"
          class="h-10 w-full px-4 sm:w-auto"
          type="button"
          @click="router.push({ name: 'gh-declaraciones' })"
        >
          Declaración mensual
        </Button>
        <Button
          class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:w-auto"
          type="button"
          @click="modalOpen = true"
        >
          <PlusIcon data-icon="inline-start" />
          Nuevo periodo
        </Button>
      </div>
    </div>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <h3 class="text-base font-semibold text-palette-navy">Periodos</h3>
      <p class="mt-1 text-sm text-slate-500">
        Cada periodo agrupa las boletas del mes. Cierre cuando la planilla esté lista.
      </p>

      <div
        v-if="loading && !data"
        class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60"
        aria-busy="true"
        aria-live="polite"
      >
        <span class="sr-only">Cargando periodos</span>
        <div class="space-y-3 p-4">
          <Skeleton class="h-9 w-full" />
          <Skeleton v-for="row in 5" :key="row" class="h-11 w-full" />
        </div>
      </div>

      <div v-else class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60">
        <Table>
          <TableCaption class="sr-only">Listado paginado de periodos de nómina.</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead class="px-3">Periodo</TableHead>
              <TableHead class="px-3">Estado</TableHead>
              <TableHead class="px-3">Boletas</TableHead>
              <TableHead class="px-3">Total neto</TableHead>
              <TableHead class="px-3">
                <span class="sr-only">Acciones</span>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!data?.content.length" :colspan="5">
              <span class="text-slate-500">No hay periodos. Cree el primero para generar boletas.</span>
            </TableEmpty>
            <TableRow v-for="periodo in data?.content ?? []" :key="periodo.id">
              <TableCell class="px-3 py-3 font-medium">
                {{ periodoLabel(periodo.anio, periodo.mes) }}
              </TableCell>
              <TableCell class="px-3 py-3">
                <span
                  class="inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide"
                  :class="periodo.estado === 'CERRADO'
                    ? 'border border-slate-200 bg-slate-50 text-slate-600'
                    : 'border border-palette-cyan/80 bg-palette-cyan/10 text-palette-royal'"
                >
                  {{ estadoLabel(periodo.estado) }}
                </span>
              </TableCell>
              <TableCell class="px-3 py-3">{{ periodo.cantidadBoletas }}</TableCell>
              <TableCell class="px-3 py-3 whitespace-nowrap">{{ money.format(periodo.totalNeto) }}</TableCell>
              <TableCell class="px-3 py-3">
                <div class="flex justify-end gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    class="h-8 px-3"
                    type="button"
                    :aria-label="`Ver periodo ${periodoLabel(periodo.anio, periodo.mes)}`"
                    @click="router.push({ name: 'gh-nomina-detalle', params: { id: String(periodo.id) } })"
                  >
                    Ver
                  </Button>
                  <Button
                    v-if="periodo.estado === 'BORRADOR' && periodo.cantidadBoletas > 0"
                    class="h-8 bg-palette-navy px-3 text-white hover:bg-palette-royal"
                    size="sm"
                    type="button"
                    :disabled="closingId === periodo.id"
                    :aria-label="`Cerrar periodo ${periodoLabel(periodo.anio, periodo.mes)}`"
                    @click="onCerrar(periodo)"
                  >
                    {{ closingId === periodo.id ? 'Cerrando…' : 'Cerrar' }}
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
          aria-label="Paginación de periodos"
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

    <Teleport to="body">
      <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
        <div v-if="modalOpen" class="modal-root fixed inset-0 z-50 overflow-y-auto">
          <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="!submitting && (modalOpen = false)" />
          <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
            <section
              class="modal-panel relative z-10 flex w-full max-w-lg flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
              aria-labelledby="nomina-periodo-title"
            >
              <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <div>
                  <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                    Gestión Humana / Nóminas
                  </p>
                  <h2 id="nomina-periodo-title" class="mt-1 text-lg font-bold text-palette-navy">
                    Nuevo periodo
                  </h2>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  class="shrink-0"
                  type="button"
                  aria-label="Cerrar"
                  :disabled="submitting"
                  @click="modalOpen = false"
                >
                  <XIcon />
                </Button>
              </header>
              <form class="flex flex-col px-5 py-5" @submit.prevent="onCrearPeriodo">
                <fieldset class="space-y-4" :disabled="submitting">
                  <div class="grid gap-4 sm:grid-cols-2">
                    <div>
                      <label class="text-sm font-medium text-slate-700" for="nomina-mes">Mes</label>
                      <select id="nomina-mes" v-model="form.mes" :class="inputClass">
                        <option v-for="item in meses" :key="item.value" :value="String(item.value)">
                          {{ item.label }}
                        </option>
                      </select>
                    </div>
                    <div>
                      <label class="text-sm font-medium text-slate-700" for="nomina-anio">Año</label>
                      <input
                        id="nomina-anio"
                        v-model="form.anio"
                        type="number"
                        min="2000"
                        max="2100"
                        :class="inputClass"
                      />
                    </div>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="nomina-obs">Observaciones</label>
                    <input
                      id="nomina-obs"
                      v-model="form.observaciones"
                      type="text"
                      maxlength="2000"
                      :class="inputClass"
                      placeholder="Opcional"
                    />
                  </div>
                </fieldset>
                <p
                  v-if="formError"
                  id="nomina-periodo-error"
                  class="mt-4 text-sm font-medium text-red-600"
                  role="alert"
                >
                  {{ formError }}
                </p>
                <footer class="mt-4 flex flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
                  <Button
                    variant="outline"
                    class="h-10 px-4"
                    type="button"
                    :disabled="submitting"
                    @click="modalOpen = false"
                  >
                    Cancelar
                  </Button>
                  <Button
                    class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                    type="submit"
                    :disabled="submitting"
                    :aria-describedby="formError ? 'nomina-periodo-error' : undefined"
                  >
                    {{ submitting ? 'Creando…' : 'Crear periodo' }}
                  </Button>
                </footer>
              </form>
            </section>
          </div>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>
