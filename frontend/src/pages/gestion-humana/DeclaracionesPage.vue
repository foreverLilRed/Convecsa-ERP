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
import { crearDeclaracion, listarDeclaraciones } from '@/services/nomina/declaracionService'
import { mesesCatalogo, periodoLabel } from '@/types/nomina'
import type { DeclaracionMensual } from '@/types/declaracion'
import type { PagedResponse } from '@/types/persona'

const PAGE_SIZE = 20
const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const router = useRouter()
const loading = ref(true)
const data = ref<PagedResponse<DeclaracionMensual> | null>(null)
const page = ref(0)
const modalOpen = ref(false)
const submitting = ref(false)
const formError = ref('')
const form = reactive({
  anio: String(new Date().getFullYear()),
  mes: String(new Date().getMonth() + 1),
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
    data.value = await listarDeclaraciones(page.value, PAGE_SIZE)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las declaraciones.')
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

async function onCrear() {
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
    const creada = await crearDeclaracion({ anio, mes })
    toast.success('Declaración creada.')
    modalOpen.value = false
    await router.push({ name: 'gh-declaracion-detalle', params: { id: String(creada.id) } })
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear la declaración.'
    formError.value = message
    toast.error(message)
  } finally {
    submitting.value = false
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
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          Declaración mensual
        </h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Hub SUNAT por año-mes: consolidado RCC, PLAME, AFPnet, asiento, dispersión y recibos por honorarios.
        </p>
      </div>
      <Button
        class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:mt-7 sm:w-auto"
        type="button"
        @click="modalOpen = true"
      >
        <PlusIcon data-icon="inline-start" />
        Nueva declaración
      </Button>
    </div>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <h3 class="text-base font-semibold text-palette-navy">Declaraciones</h3>
      <p class="mt-1 text-sm text-slate-500">
        Una declaración por mes. No se mezcla con el periodo interno de nómina administrativa.
      </p>

      <div
        v-if="loading && !data"
        class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60"
        aria-busy="true"
      >
        <span class="sr-only">Cargando declaraciones</span>
        <div class="space-y-3 p-4">
          <Skeleton class="h-9 w-full" />
          <Skeleton v-for="row in 5" :key="row" class="h-11 w-full" />
        </div>
      </div>

      <div v-else class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60">
        <Table>
          <TableCaption class="sr-only">Listado de declaraciones mensuales.</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead class="px-3">Periodo</TableHead>
              <TableHead class="px-3">Estado</TableHead>
              <TableHead class="px-3">Admin</TableHead>
              <TableHead class="px-3">Obreros</TableHead>
              <TableHead class="px-3">RxH</TableHead>
              <TableHead class="px-3">Neto RCC</TableHead>
              <TableHead class="px-3">
                <span class="sr-only">Acciones</span>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!data?.content.length" :colspan="7">
              <span class="text-slate-500">No hay declaraciones. Cree la del mes para exportar a SUNAT.</span>
            </TableEmpty>
            <TableRow v-for="item in data?.content ?? []" :key="item.id">
              <TableCell class="px-3 py-3 font-medium">{{ periodoLabel(item.anio, item.mes) }}</TableCell>
              <TableCell class="px-3 py-3">
                <span
                  class="inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide"
                  :class="item.estado === 'CERRADO'
                    ? 'border border-slate-200 bg-slate-50 text-slate-600'
                    : 'border border-palette-cyan/80 bg-palette-cyan/10 text-palette-royal'"
                >
                  {{ estadoLabel(item.estado) }}
                </span>
              </TableCell>
              <TableCell class="px-3 py-3">{{ item.cantidadAdmin }}</TableCell>
              <TableCell class="px-3 py-3">{{ item.cantidadObreros }}</TableCell>
              <TableCell class="px-3 py-3">{{ item.cantidadRxh }}</TableCell>
              <TableCell class="px-3 py-3 whitespace-nowrap">{{ money.format(item.totalNetoRcc) }}</TableCell>
              <TableCell class="px-3 py-3">
                <div class="flex justify-end">
                  <Button
                    variant="outline"
                    size="sm"
                    class="h-8 px-3"
                    type="button"
                    @click="router.push({ name: 'gh-declaracion-detalle', params: { id: String(item.id) } })"
                  >
                    Ver
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>

      <div class="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p class="text-sm text-slate-500">
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
              <PaginationEllipsis v-else-if="item.type === 'ellipsis'" />
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
              aria-labelledby="declaracion-title"
            >
              <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <div>
                  <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                    Gestión Humana / Declaración
                  </p>
                  <h2 id="declaracion-title" class="mt-1 text-lg font-bold text-palette-navy">
                    Nueva declaración
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
              <form class="flex flex-col px-5 py-5" @submit.prevent="onCrear">
                <fieldset class="space-y-4" :disabled="submitting">
                  <div class="grid gap-4 sm:grid-cols-2">
                    <div>
                      <label class="text-sm font-medium text-slate-700" for="decl-mes">Mes</label>
                      <select id="decl-mes" v-model="form.mes" :class="inputClass">
                        <option v-for="item in meses" :key="item.value" :value="String(item.value)">
                          {{ item.label }}
                        </option>
                      </select>
                    </div>
                    <div>
                      <label class="text-sm font-medium text-slate-700" for="decl-anio">Año</label>
                      <input
                        id="decl-anio"
                        v-model="form.anio"
                        type="number"
                        min="2000"
                        max="2100"
                        :class="inputClass"
                      />
                    </div>
                  </div>
                </fieldset>
                <p v-if="formError" class="mt-4 text-sm font-medium text-red-600" role="alert">
                  {{ formError }}
                </p>
                <footer class="mt-4 flex flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
                  <Button variant="outline" class="h-10 px-4" type="button" :disabled="submitting" @click="modalOpen = false">
                    Cancelar
                  </Button>
                  <Button
                    class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                    type="submit"
                    :disabled="submitting"
                  >
                    {{ submitting ? 'Creando…' : 'Crear declaración' }}
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
