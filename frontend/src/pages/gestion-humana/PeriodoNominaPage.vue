<script setup lang="ts">
import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DownloadIcon,
  PencilIcon,
  PlusIcon,
  PrinterIcon,
  XIcon,
} from '@lucide/vue'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
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
import {
  actualizarBoleta,
  cerrarPeriodo,
  generarBoletas,
  listarAfp,
  listarBoletas,
  obtenerBoleta,
  obtenerPeriodo,
  recalcularPeriodo,
} from '@/services/nomina/nominaService'
import {
  boletaDocumentoUrl,
  periodoLabel,
  type Afp,
  type BoletaPago,
  type BoletaPagoResumen,
  type CategoriaConcepto,
  type PeriodoNomina,
} from '@/types/nomina'
import type { PagedResponse } from '@/types/persona'

const props = defineProps<{
  id: string
}>()

const PAGE_SIZE = 20
const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

type ExtraRow = {
  key: number
  categoria: CategoriaConcepto
  label: string
  monto: string
}

const router = useRouter()
const periodoId = computed(() => Number(props.id))
const loading = ref(true)
const acting = ref(false)
const saving = ref(false)
const page = ref(0)
const periodo = ref<PeriodoNomina | null>(null)
const boletas = ref<PagedResponse<BoletaPagoResumen> | null>(null)
const afps = ref<Afp[]>([])
const editOpen = ref(false)
const editError = ref('')
const editBoleta = ref<BoletaPago | null>(null)
const editDias = ref('30')
const editAsig = ref(false)
const editAfpId = ref('')
const extras = ref<ExtraRow[]>([])
let extraSeq = 0

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const displayPage = computed(() => page.value + 1)
const totalPages = computed(() => boletas.value?.totalPages ?? 0)
const totalElements = computed(() => boletas.value?.totalElements ?? 0)
const esBorrador = computed(() => periodo.value?.estado === 'BORRADOR')
const puedeGenerar = computed(() => esBorrador.value && (periodo.value?.cantidadBoletas ?? 0) === 0)
const tituloPeriodo = computed(() => {
  if (!periodo.value) {
    const mes = Number(props.id)
    return Number.isFinite(mes) ? `Periodo ${props.id}` : 'Periodo'
  }
  return periodoLabel(periodo.value.anio, periodo.value.mes)
})

function estadoLabel(estado: string): string {
  return estado === 'CERRADO' || estado === 'APROBADA' ? (estado === 'CERRADO' ? 'Cerrado' : 'Aprobada') : 'Borrador'
}

function onDisplayPageChange(nextPage: number) {
  page.value = Math.max(0, nextPage - 1)
  void loadBoletas()
}

async function load() {
  loading.value = true
  try {
    const [nextPeriodo, nextAfps] = await Promise.all([
      obtenerPeriodo(periodoId.value),
      afps.value.length ? Promise.resolve(afps.value) : listarAfp(),
    ])
    periodo.value = nextPeriodo
    afps.value = nextAfps
    await loadBoletas()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el periodo.')
  } finally {
    loading.value = false
  }
}

async function loadBoletas() {
  boletas.value = await listarBoletas(periodoId.value, page.value, PAGE_SIZE)
}

async function onGenerar() {
  acting.value = true
  try {
    periodo.value = await generarBoletas(periodoId.value)
    page.value = 0
    await loadBoletas()
    toast.success('Boletas generadas.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron generar las boletas.')
  } finally {
    acting.value = false
  }
}

async function onRecalcularTodas() {
  acting.value = true
  try {
    periodo.value = await recalcularPeriodo(periodoId.value)
    await loadBoletas()
    toast.success('Boletas recalculadas.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron recalcular las boletas.')
  } finally {
    acting.value = false
  }
}

async function onCerrar() {
  acting.value = true
  try {
    periodo.value = await cerrarPeriodo(periodoId.value)
    await loadBoletas()
    toast.success('Periodo cerrado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cerrar el periodo.')
  } finally {
    acting.value = false
  }
}

function abrirDocumento(id: number, modo: 'ver' | 'descarga' | 'imprimir') {
  const url = boletaDocumentoUrl(id, {
    descarga: modo === 'descarga',
    imprimir: modo === 'imprimir',
  })
  window.open(url, '_blank', 'noopener,noreferrer')
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
  if (event.key === 'Escape' && editOpen.value && !saving.value) {
    editOpen.value = false
  }
}

watch(editOpen, (isOpen) => {
  if (isOpen) {
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

async function abrirEdicion(id: number) {
  try {
    const detalle = await obtenerBoleta(id)
    editBoleta.value = detalle
    editDias.value = String(detalle.diasTrabajados)
    editAsig.value = detalle.aplicaAsigFamiliar
    editAfpId.value = detalle.afpId ? String(detalle.afpId) : ''
    extras.value = detalle.conceptos
      .filter((concepto) => concepto.origen === 'MANUAL')
      .map((concepto) => ({
        key: ++extraSeq,
        categoria: concepto.categoria,
        label: concepto.label,
        monto: String(concepto.monto),
      }))
    editError.value = ''
    editOpen.value = true
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo abrir la boleta.')
  }
}

function agregarExtra() {
  extras.value = [
    ...extras.value,
    { key: ++extraSeq, categoria: 'HABER', label: '', monto: '' },
  ]
}

function quitarExtra(key: number) {
  extras.value = extras.value.filter((row) => row.key !== key)
}

async function guardarEdicion() {
  if (!editBoleta.value) {
    return
  }
  editError.value = ''
  const dias = Number(editDias.value)
  if (!Number.isInteger(dias) || dias < 0 || dias > 30) {
    editError.value = 'Los días trabajados deben estar entre 0 y 30.'
    toast.error(editError.value)
    return
  }
  const afpId = Number(editAfpId.value)
  if (!Number.isFinite(afpId) || afpId < 1) {
    editError.value = 'Seleccione AFP u ONP.'
    toast.error(editError.value)
    return
  }
  const extrasPayload = []
  for (const row of extras.value) {
    const label = row.label.trim()
    const monto = Number(row.monto)
    if (!label && row.monto === '') {
      continue
    }
    if (!label) {
      editError.value = 'Cada concepto extra necesita un nombre.'
      toast.error(editError.value)
      return
    }
    if (!Number.isFinite(monto) || monto < 0) {
      editError.value = 'Cada concepto extra necesita un monto válido.'
      toast.error(editError.value)
      return
    }
    extrasPayload.push({ categoria: row.categoria, label, monto })
  }
  saving.value = true
  try {
    await actualizarBoleta(editBoleta.value.id, {
      diasTrabajados: dias,
      aplicaAsigFamiliar: editAsig.value,
      afpId,
      extras: extrasPayload,
    })
    toast.success('Boleta actualizada.')
    editOpen.value = false
    periodo.value = await obtenerPeriodo(periodoId.value)
    await loadBoletas()
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo guardar la boleta.'
    editError.value = message
    toast.error(message)
  } finally {
    saving.value = false
  }
}

watch(
  () => props.id,
  () => {
    page.value = 0
    void load()
  },
)

onMounted(() => {
  void load()
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          {{ tituloPeriodo }}
        </h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Régimen Pequeña Empresa – REMYPE. El neto descuenta AFP/ONP; EsSalud es aporte del empleador.
        </p>
      </div>
      <Button variant="outline" class="h-10 px-4 sm:mt-7" type="button" @click="router.push({ name: 'gh-nominas' })">
        <ChevronLeftIcon data-icon="inline-start" class="cn-rtl-flip" />
        Periodos
      </Button>
    </div>

    <div class="mt-6 grid gap-4 sm:grid-cols-3">
      <template v-if="loading && !periodo">
        <div v-for="card in 3" :key="card" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <Skeleton class="h-4 w-28" />
          <Skeleton class="mt-4 h-8 w-20" />
        </div>
      </template>
      <template v-else-if="periodo">
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Estado</p>
          <p class="mt-3 text-2xl font-bold text-palette-navy">{{ estadoLabel(periodo.estado) }}</p>
        </article>
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Boletas</p>
          <p class="mt-3 text-2xl font-bold text-palette-navy">{{ periodo.cantidadBoletas }}</p>
        </article>
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5" aria-live="polite">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Total neto</p>
          <p class="mt-3 text-2xl font-bold text-palette-navy">{{ money.format(periodo.totalNeto) }}</p>
        </article>
      </template>
    </div>

    <div class="mt-4 flex flex-wrap gap-2">
      <Button
        v-if="puedeGenerar"
        class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
        type="button"
        :disabled="acting"
        @click="onGenerar"
      >
        Generar boletas
      </Button>
      <Button
        v-if="esBorrador && (periodo?.cantidadBoletas ?? 0) > 0"
        variant="outline"
        class="h-10 px-4"
        type="button"
        :disabled="acting"
        @click="onRecalcularTodas"
      >
        Recalcular todas
      </Button>
      <Button
        v-if="esBorrador && (periodo?.cantidadBoletas ?? 0) > 0"
        class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
        type="button"
        :disabled="acting"
        @click="onCerrar"
      >
        Cerrar periodo
      </Button>
    </div>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <h3 class="text-base font-semibold text-palette-navy">Boletas</h3>
      <p class="mt-1 text-sm text-slate-500">
        Descargue o imprima el HTML. En borrador puede editar días, AFP y conceptos extra.
      </p>

      <div
        v-if="loading && !boletas"
        class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60"
        aria-busy="true"
      >
        <span class="sr-only">Cargando boletas</span>
        <div class="space-y-3 p-4">
          <Skeleton class="h-9 w-full" />
          <Skeleton v-for="row in 5" :key="row" class="h-11 w-full" />
        </div>
      </div>

      <div v-else class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60">
        <Table>
          <TableCaption class="sr-only">Listado paginado de boletas del periodo.</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead class="px-3">Persona</TableHead>
              <TableHead class="px-3">Cargo</TableHead>
              <TableHead class="px-3">Días</TableHead>
              <TableHead class="px-3">Haberes</TableHead>
              <TableHead class="px-3">Descuentos</TableHead>
              <TableHead class="px-3">Neto</TableHead>
              <TableHead class="px-3">
                <span class="sr-only">Acciones</span>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!boletas?.content.length" :colspan="7">
              <span class="text-slate-500">
                {{ puedeGenerar ? 'Aún no hay boletas. Genere la planilla con los contratos vigentes.' : 'No hay boletas.' }}
              </span>
            </TableEmpty>
            <TableRow v-for="boleta in boletas?.content ?? []" :key="boleta.id">
              <TableCell class="px-3 py-3 font-medium">
                {{ boleta.personaApellidos }}, {{ boleta.personaNombres }}
                <span class="mt-0.5 block text-xs font-normal text-slate-500">{{ boleta.numeroDocumento }}</span>
              </TableCell>
              <TableCell class="px-3 py-3">{{ boleta.cargo || '—' }}</TableCell>
              <TableCell class="px-3 py-3">{{ boleta.diasTrabajados }}</TableCell>
              <TableCell class="px-3 py-3 whitespace-nowrap">{{ money.format(boleta.totalHaberes) }}</TableCell>
              <TableCell class="px-3 py-3 whitespace-nowrap">{{ money.format(boleta.totalDescuentos) }}</TableCell>
              <TableCell class="px-3 py-3 whitespace-nowrap font-semibold">{{ money.format(boleta.neto) }}</TableCell>
              <TableCell class="px-3 py-3">
                <div class="flex flex-wrap justify-end gap-2">
                  <Button
                    v-if="esBorrador"
                    variant="outline"
                    size="sm"
                    class="h-8 px-3"
                    type="button"
                    :aria-label="`Editar boleta de ${boleta.personaApellidos}`"
                    @click="abrirEdicion(boleta.id)"
                  >
                    <PencilIcon data-icon="inline-start" />
                    Editar
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    class="h-8 px-3"
                    type="button"
                    :aria-label="`Descargar boleta de ${boleta.personaApellidos}`"
                    @click="abrirDocumento(boleta.id, 'descarga')"
                  >
                    <DownloadIcon data-icon="inline-start" />
                    Descargar
                  </Button>
                  <Button
                    class="h-8 bg-palette-navy px-3 text-white hover:bg-palette-royal"
                    size="sm"
                    type="button"
                    :aria-label="`Imprimir boleta de ${boleta.personaApellidos}`"
                    @click="abrirDocumento(boleta.id, 'imprimir')"
                  >
                    <PrinterIcon data-icon="inline-start" />
                    Imprimir
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
          aria-label="Paginación de boletas"
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
        <div v-if="editOpen && editBoleta" class="modal-root fixed inset-0 z-50 overflow-y-auto">
          <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="!saving && (editOpen = false)" />
          <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
            <section
              class="modal-panel relative z-10 flex h-[min(92vh,46rem)] w-full max-w-3xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
              aria-labelledby="boleta-edit-title"
            >
              <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <div>
                  <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                    Boleta de pago
                  </p>
                  <h2 id="boleta-edit-title" class="mt-1 text-lg font-bold text-palette-navy">
                    {{ editBoleta.personaApellidos }}, {{ editBoleta.personaNombres }}
                  </h2>
                  <p class="mt-1 text-xs text-slate-500">
                    DNI {{ editBoleta.numeroDocumento }}
                    <span v-if="editBoleta.cargo"> · {{ editBoleta.cargo }}</span>
                    <span v-if="editBoleta.banco"> · {{ editBoleta.banco }} {{ editBoleta.cci }}</span>
                  </p>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  type="button"
                  aria-label="Cerrar"
                  :disabled="saving"
                  @click="editOpen = false"
                >
                  <XIcon />
                </Button>
              </header>

              <form class="flex min-h-0 flex-1 flex-col px-5 py-5" @submit.prevent="guardarEdicion">
                <fieldset class="min-h-0 flex-1 space-y-4 overflow-y-auto pr-1" :disabled="saving">
                  <div class="grid gap-4 sm:grid-cols-3">
                    <div>
                      <label class="text-sm font-medium text-slate-700" for="boleta-dias">Días trabajados</label>
                      <input
                        id="boleta-dias"
                        v-model="editDias"
                        type="number"
                        min="0"
                        max="30"
                        :class="inputClass"
                      />
                    </div>
                    <div>
                      <label class="text-sm font-medium text-slate-700" for="boleta-afp">AFP / ONP</label>
                      <select id="boleta-afp" v-model="editAfpId" :class="inputClass">
                        <option value="">Seleccione</option>
                        <option v-for="item in afps" :key="item.id" :value="String(item.id)">
                          {{ item.label }}
                        </option>
                      </select>
                    </div>
                    <div class="flex items-end pb-2">
                      <label class="flex items-center gap-2 text-sm text-slate-700" for="boleta-asig">
                        <input id="boleta-asig" v-model="editAsig" type="checkbox" class="h-4 w-4 rounded border-slate-300" />
                        Asignación familiar (S/ 113.00)
                      </label>
                    </div>
                  </div>

                  <div>
                    <div class="flex items-center justify-between">
                      <h3 class="text-sm font-semibold text-palette-navy">Conceptos extra</h3>
                      <Button variant="outline" size="sm" type="button" class="h-8" @click="agregarExtra">
                        <PlusIcon data-icon="inline-start" />
                        Agregar
                      </Button>
                    </div>
                    <p class="mt-1 text-xs text-slate-500">
                      Los haberes extra entran a la base de AFP/ONP y EsSalud. Los descuentos extra solo restan del neto.
                    </p>
                    <ul v-if="extras.length" class="mt-3 space-y-2">
                      <li v-for="row in extras" :key="row.key" class="grid gap-2 sm:grid-cols-[8rem_1fr_7rem_auto]">
                        <label class="sr-only" :for="`extra-cat-${row.key}`">Categoría</label>
                        <select :id="`extra-cat-${row.key}`" v-model="row.categoria" :class="inputClass">
                          <option value="HABER">Haber</option>
                          <option value="DESCUENTO">Descuento</option>
                          <option value="APORTE_EMPLEADOR">Aporte empleador</option>
                        </select>
                        <label class="sr-only" :for="`extra-label-${row.key}`">Nombre</label>
                        <input
                          :id="`extra-label-${row.key}`"
                          v-model="row.label"
                          type="text"
                          :class="inputClass"
                          placeholder="Nombre"
                        />
                        <label class="sr-only" :for="`extra-monto-${row.key}`">Monto</label>
                        <input
                          :id="`extra-monto-${row.key}`"
                          v-model="row.monto"
                          type="number"
                          min="0"
                          step="0.01"
                          :class="inputClass"
                          placeholder="0.00"
                        />
                        <Button variant="ghost" size="sm" type="button" class="h-9" :aria-label="'Quitar concepto'" @click="quitarExtra(row.key)">
                          Quitar
                        </Button>
                      </li>
                    </ul>
                  </div>
                </fieldset>

                <p v-if="editError" id="boleta-edit-error" class="mt-4 text-sm font-medium text-red-600" role="alert">
                  {{ editError }}
                </p>
                <footer class="mt-4 flex shrink-0 flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
                  <Button variant="outline" class="h-10 px-4" type="button" :disabled="saving" @click="editOpen = false">
                    Cancelar
                  </Button>
                  <Button
                    class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                    type="submit"
                    :disabled="saving"
                    :aria-describedby="editError ? 'boleta-edit-error' : undefined"
                  >
                    {{ saving ? 'Guardando…' : 'Guardar y recalcular' }}
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
