<script setup lang="ts">
import { XIcon } from '@lucide/vue'
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Textarea } from '@/components/ui/textarea'
import {
  actualizarObra,
  cambiarEstadoObra,
  crearObra,
  eliminarObra,
  listarObras,
  obtenerResumenObras,
} from '@/services/obra/obraService'
import type { PagedResponse } from '@/types/persona'
import {
  ESTADOS_OBRA,
  TRANSICIONES_OBRA,
  etiquetaAccionEstado,
  etiquetaEstadoObra,
  type EstadoObra,
  type ObraDTO,
  type ObraRequest,
  type ObraResumen,
} from '@/types/obra'

const selectClass =
  'mt-1.5 block h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-900 outline-none focus:border-sky-600 focus:ring-2 focus:ring-sky-500/20 disabled:bg-slate-100'
const PAGE_SIZE = 15

const loading = ref(true)
const saving = ref(false)
const page = ref(0)
const data = ref<PagedResponse<ObraDTO> | null>(null)
const resumen = ref<ObraResumen | null>(null)
const q = ref('')
const filtroEstado = ref<EstadoObra | ''>('')
const formOpen = ref(false)
const editingId = ref<number | null>(null)
let searchTimer: ReturnType<typeof setTimeout> | undefined

const form = reactive({
  codigo: '',
  nombre: '',
  clienteNombre: '',
  contratistaPrincipal: '',
  ubicacion: '',
  fechaInicioPlanificada: '',
  fechaFinPlanificada: '',
  fechaInicioReal: '',
  fechaFinReal: '',
  montoContratado: '' as string | number,
  moneda: 'PEN',
  responsableNombre: '',
})

const estadoModal = ref<{
  obra: ObraDTO
  destino: EstadoObra
  justificacion: string
} | null>(null)

const confirmacion = ref<{ titulo: string; detalle: string; ejecutar: () => Promise<void> } | null>(null)

const resumenCards = computed(() => [
  { label: 'Total', value: resumen.value?.total ?? 0 },
  { label: 'Borrador', value: resumen.value?.borrador ?? 0 },
  { label: 'En ejecución', value: resumen.value?.enEjecucion ?? 0 },
  { label: 'Suspendidas', value: resumen.value?.suspendida ?? 0 },
])

const formTitle = computed(() => (editingId.value ? 'Editar obra' : 'Nueva obra'))

function emptyToNull(value: string): string | null {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

function formatFecha(value: string | null): string {
  if (!value) return '—'
  return new Date(`${value}T12:00:00`).toLocaleDateString('es-PE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function formatMonto(item: ObraDTO): string {
  if (item.montoContratado == null) return '—'
  return `${item.moneda} ${Number(item.montoContratado).toLocaleString('es-PE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
}

function estadoClass(estado: EstadoObra): string {
  switch (estado) {
    case 'BORRADOR':
      return 'border-slate-200 bg-slate-50 text-slate-700'
    case 'PLANIFICADA':
      return 'border-sky-200 bg-sky-50 text-sky-800'
    case 'EN_EJECUCION':
      return 'border-emerald-200 bg-emerald-50 text-emerald-800'
    case 'SUSPENDIDA':
      return 'border-amber-200 bg-amber-50 text-amber-900'
    case 'CERRADA':
      return 'border-indigo-200 bg-indigo-50 text-indigo-800'
    case 'CANCELADA':
      return 'border-red-200 bg-red-50 text-red-800'
    default:
      return 'border-slate-200 bg-slate-50 text-slate-700'
  }
}

function requiereJustificacion(destino: EstadoObra): boolean {
  return destino === 'SUSPENDIDA' || destino === 'CANCELADA'
}

function puedeEditar(item: ObraDTO): boolean {
  return item.estado !== 'CERRADA' && item.estado !== 'CANCELADA'
}

function accionesDisponibles(item: ObraDTO): EstadoObra[] {
  return TRANSICIONES_OBRA[item.estado] ?? []
}

function etiquetaAccion(item: ObraDTO, destino: EstadoObra): string {
  if (destino === 'EN_EJECUCION' && item.estado === 'SUSPENDIDA') return 'Reanudar'
  if (destino === 'EN_EJECUCION') return 'Iniciar'
  return etiquetaAccionEstado(destino)
}

function resetForm() {
  form.codigo = ''
  form.nombre = ''
  form.clienteNombre = ''
  form.contratistaPrincipal = ''
  form.ubicacion = ''
  form.fechaInicioPlanificada = ''
  form.fechaFinPlanificada = ''
  form.fechaInicioReal = ''
  form.fechaFinReal = ''
  form.montoContratado = ''
  form.moneda = 'PEN'
  form.responsableNombre = ''
  editingId.value = null
}

function abrirNueva() {
  resetForm()
  formOpen.value = true
}

function abrirEdicion(item: ObraDTO) {
  editingId.value = item.id
  form.codigo = item.codigo
  form.nombre = item.nombre
  form.clienteNombre = item.clienteNombre
  form.contratistaPrincipal = item.contratistaPrincipal ?? ''
  form.ubicacion = item.ubicacion ?? ''
  form.fechaInicioPlanificada = item.fechaInicioPlanificada ?? ''
  form.fechaFinPlanificada = item.fechaFinPlanificada ?? ''
  form.fechaInicioReal = item.fechaInicioReal ?? ''
  form.fechaFinReal = item.fechaFinReal ?? ''
  form.montoContratado = item.montoContratado ?? ''
  form.moneda = item.moneda || 'PEN'
  form.responsableNombre = item.responsableNombre ?? ''
  formOpen.value = true
}

function buildRequest(): ObraRequest {
  const montoRaw = form.montoContratado
  const monto =
    montoRaw === '' || montoRaw === null || montoRaw === undefined
      ? null
      : Number(montoRaw)
  return {
    codigo: form.codigo.trim(),
    nombre: form.nombre.trim(),
    clienteNombre: form.clienteNombre.trim(),
    contratistaPrincipal: emptyToNull(form.contratistaPrincipal),
    ubicacion: emptyToNull(form.ubicacion),
    fechaInicioPlanificada: emptyToNull(form.fechaInicioPlanificada),
    fechaFinPlanificada: emptyToNull(form.fechaFinPlanificada),
    fechaInicioReal: emptyToNull(form.fechaInicioReal),
    fechaFinReal: emptyToNull(form.fechaFinReal),
    montoContratado: monto != null && Number.isFinite(monto) ? monto : null,
    moneda: emptyToNull(form.moneda) ?? 'PEN',
    responsableNombre: emptyToNull(form.responsableNombre),
  }
}

async function cargarListado() {
  loading.value = true
  try {
    const [nextData, nextResumen] = await Promise.all([
      listarObras({
        page: page.value,
        size: PAGE_SIZE,
        q: q.value,
        estado: filtroEstado.value,
      }),
      obtenerResumenObras(),
    ])
    data.value = nextData
    resumen.value = nextResumen
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo cargar obras.')
  } finally {
    loading.value = false
  }
}

async function guardar() {
  if (!form.codigo.trim() || !form.nombre.trim() || !form.clienteNombre.trim()) {
    toast.error('Complete código, nombre y cliente.')
    return
  }
  saving.value = true
  try {
    const request = buildRequest()
    if (editingId.value) {
      await actualizarObra(editingId.value, request)
      toast.success('Obra actualizada.')
    } else {
      await crearObra(request)
      toast.success('Obra creada en borrador.')
    }
    formOpen.value = false
    resetForm()
    page.value = 0
    await cargarListado()
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo guardar la obra.')
  } finally {
    saving.value = false
  }
}

function abrirCambioEstado(obra: ObraDTO, destino: EstadoObra) {
  estadoModal.value = { obra, destino, justificacion: '' }
}

async function confirmarCambioEstado() {
  const modal = estadoModal.value
  if (!modal) return
  if (requiereJustificacion(modal.destino) && !modal.justificacion.trim()) {
    toast.error('Indique la justificación.')
    return
  }
  saving.value = true
  try {
    await cambiarEstadoObra(modal.obra.id, {
      estado: modal.destino,
      justificacion: modal.justificacion.trim() || null,
    })
    toast.success(`Obra pasó a ${etiquetaEstadoObra(modal.destino).toLowerCase()}.`)
    estadoModal.value = null
    await cargarListado()
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo cambiar el estado.')
  } finally {
    saving.value = false
  }
}

function pedirConfirmacion(titulo: string, detalle: string, ejecutar: () => Promise<void>) {
  confirmacion.value = { titulo, detalle, ejecutar }
}

async function ejecutarConfirmacion() {
  const accion = confirmacion.value
  if (!accion) return
  saving.value = true
  try {
    await accion.ejecutar()
    confirmacion.value = null
    await cargarListado()
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo completar la acción.')
  } finally {
    saving.value = false
  }
}

const hayModalAbierto = computed(
  () => formOpen.value || estadoModal.value != null || confirmacion.value != null,
)

function lockBodyScroll() {
  const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth
  document.body.style.overflow = 'hidden'
  document.body.style.paddingRight = scrollbarWidth > 0 ? `${scrollbarWidth}px` : ''
}

function unlockBodyScroll() {
  if (hayModalAbierto.value) return
  document.body.style.overflow = ''
  document.body.style.paddingRight = ''
}

function cerrarModales() {
  if (saving.value) return
  formOpen.value = false
  estadoModal.value = null
  confirmacion.value = null
}

function onDocumentKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && hayModalAbierto.value && !saving.value) {
    cerrarModales()
  }
}

watch(hayModalAbierto, (isOpen) => {
  if (isOpen) {
    lockBodyScroll()
    document.addEventListener('keydown', onDocumentKeydown)
  } else {
    document.removeEventListener('keydown', onDocumentKeydown)
  }
})

watch([q, filtroEstado], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 0
    void cargarListado()
  }, 300)
})

onMounted(() => {
  void cargarListado()
})

onUnmounted(() => {
  document.removeEventListener('keydown', onDocumentKeydown)
  document.body.style.overflow = ''
  document.body.style.paddingRight = ''
})
</script>

<template>
  <section class="min-w-0 space-y-3 overflow-x-hidden pb-8">
    <header class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Obras</h2>
        <p class="mt-2 max-w-2xl text-sm leading-relaxed text-slate-500">
          Catálogo de obras: identidad, cliente, plazos y ciclo de vida. Las obras nacen en borrador.
        </p>
      </div>
      <Button
        class="h-10 shrink-0 bg-palette-navy text-white hover:bg-palette-royal"
        type="button"
        @click="abrirNueva"
      >
        Nueva obra
      </Button>
    </header>

    <dl class="grid gap-2 sm:grid-cols-2 xl:grid-cols-4">
      <div
        v-for="card in resumenCards"
        :key="card.label"
        class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-3"
      >
        <dt class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ card.label }}</dt>
        <dd class="mt-1 text-2xl font-bold tabular-nums text-palette-navy">{{ card.value }}</dd>
      </div>
    </dl>

    <div class="min-w-0 rounded-lg border border-palette-cyan/70 bg-white">
      <div class="border-b border-slate-100 px-4 py-4 sm:px-5">
        <h3 class="text-base font-semibold text-palette-navy">Listado</h3>
        <p class="mt-1 text-sm text-slate-500">Filtre por código, nombre, cliente o estado.</p>
        <div class="mt-3 grid gap-2 md:grid-cols-[minmax(0,1fr)_12rem]">
          <Input v-model="q" type="search" class="h-10" placeholder="Código, nombre o cliente" />
          <select v-model="filtroEstado" :class="`${selectClass} mt-0`">
            <option value="">Todos los estados</option>
            <option v-for="estado in ESTADOS_OBRA" :key="estado.value" :value="estado.value">
              {{ estado.label }}
            </option>
          </select>
        </div>
      </div>

      <div v-if="loading && !data" class="space-y-3 p-5">
        <Skeleton v-for="item in 5" :key="item" class="h-14 w-full" />
      </div>
      <div v-else class="overflow-x-auto">
        <Table class="min-w-[56rem]">
          <TableHeader>
            <TableRow>
              <TableHead>Código / nombre</TableHead>
              <TableHead>Cliente</TableHead>
              <TableHead>Fechas planificadas</TableHead>
              <TableHead>Monto</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead class="text-right">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!data?.content.length" :colspan="6">
              No hay obras con los filtros elegidos.
            </TableEmpty>
            <TableRow v-for="item in data?.content" :key="item.id">
              <TableCell>
                <p class="font-semibold text-slate-900">{{ item.codigo }}</p>
                <p class="mt-1 text-sm text-slate-700">{{ item.nombre }}</p>
                <p v-if="item.ubicacion" class="mt-1 text-xs text-slate-500">{{ item.ubicacion }}</p>
              </TableCell>
              <TableCell>
                <p class="font-medium text-slate-800">{{ item.clienteNombre }}</p>
                <p v-if="item.responsableNombre" class="mt-1 text-xs text-slate-500">
                  Resp.: {{ item.responsableNombre }}
                </p>
              </TableCell>
              <TableCell>
                <p class="text-sm text-slate-800">
                  {{ formatFecha(item.fechaInicioPlanificada) }}
                  –
                  {{ formatFecha(item.fechaFinPlanificada) }}
                </p>
                <p v-if="item.fechaInicioReal" class="mt-1 text-xs text-slate-500">
                  Real: {{ formatFecha(item.fechaInicioReal) }}
                  <template v-if="item.fechaFinReal"> – {{ formatFecha(item.fechaFinReal) }}</template>
                </p>
              </TableCell>
              <TableCell class="tabular-nums text-slate-800">{{ formatMonto(item) }}</TableCell>
              <TableCell>
                <span
                  class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold"
                  :class="estadoClass(item.estado)"
                >
                  {{ etiquetaEstadoObra(item.estado) }}
                </span>
              </TableCell>
              <TableCell class="text-right">
                <div class="flex flex-wrap justify-end gap-1">
                  <Button
                    v-if="puedeEditar(item)"
                    variant="outline"
                    size="sm"
                    type="button"
                    @click="abrirEdicion(item)"
                  >
                    Editar
                  </Button>
                  <Button
                    v-for="destino in accionesDisponibles(item)"
                    :key="destino"
                    size="sm"
                    type="button"
                    :class="
                      destino === 'CANCELADA'
                        ? 'border-red-200 text-red-700 hover:bg-red-50'
                        : destino === 'CERRADA'
                          ? 'bg-indigo-700 text-white hover:bg-indigo-800'
                          : 'bg-palette-navy text-white hover:bg-palette-royal'
                    "
                    :variant="destino === 'CANCELADA' ? 'outline' : 'default'"
                    @click="abrirCambioEstado(item, destino)"
                  >
                    {{ etiquetaAccion(item, destino) }}
                  </Button>
                  <Button
                    v-if="item.estado === 'BORRADOR'"
                    variant="ghost"
                    size="sm"
                    class="text-slate-500"
                    type="button"
                    @click="
                      pedirConfirmacion(
                        'Eliminar obra',
                        `Se eliminará definitivamente ${item.codigo} · ${item.nombre}.`,
                        async () => {
                          await eliminarObra(item.id)
                          toast.success('Obra eliminada.')
                        },
                      )
                    "
                  >
                    Eliminar
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>

      <footer
        v-if="data && data.totalPages > 1"
        class="flex flex-col gap-2 border-t border-slate-100 px-4 py-3 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between"
      >
        <span>Página {{ data.page + 1 }} de {{ data.totalPages }} · {{ data.totalElements }} registros</span>
        <div class="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            type="button"
            :disabled="data.first"
            @click="page--; cargarListado()"
          >
            Anterior
          </Button>
          <Button
            variant="outline"
            size="sm"
            type="button"
            :disabled="data.last"
            @click="page++; cargarListado()"
          >
            Siguiente
          </Button>
        </div>
      </footer>
    </div>

    <Teleport to="body">
      <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
        <div v-if="formOpen" class="modal-root fixed inset-0 z-50 overflow-y-auto">
          <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="!saving && (formOpen = false)" />
          <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
            <section
              class="modal-panel relative z-10 flex w-full max-w-2xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
              aria-labelledby="obra-form-title"
            >
              <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <div>
                  <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                    Proyectos / Obras
                  </p>
                  <h2 id="obra-form-title" class="mt-1 text-lg font-bold text-palette-navy">
                    {{ formTitle }}
                  </h2>
                  <p class="mt-1 text-sm text-slate-500">
                    {{ editingId ? 'Actualice los datos de la obra.' : 'La obra se creará en estado borrador.' }}
                  </p>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  class="shrink-0"
                  type="button"
                  aria-label="Cerrar"
                  :disabled="saving"
                  @click="formOpen = false"
                >
                  <XIcon />
                </Button>
              </header>

              <form class="flex flex-col px-5 py-5" @submit.prevent="guardar">
                <fieldset class="grid gap-3 sm:grid-cols-2" :disabled="saving">
                  <label class="block text-sm font-medium text-slate-700">
                    Código
                    <Input v-model="form.codigo" class="mt-1.5 h-10" maxlength="40" required />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Nombre
                    <Input v-model="form.nombre" class="mt-1.5 h-10" maxlength="200" required />
                  </label>
                  <label class="block text-sm font-medium text-slate-700 sm:col-span-2">
                    Cliente
                    <Input v-model="form.clienteNombre" class="mt-1.5 h-10" maxlength="200" required />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Contratista principal
                    <Input v-model="form.contratistaPrincipal" class="mt-1.5 h-10" maxlength="200" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Responsable
                    <Input v-model="form.responsableNombre" class="mt-1.5 h-10" maxlength="200" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700 sm:col-span-2">
                    Ubicación
                    <Input v-model="form.ubicacion" class="mt-1.5 h-10" maxlength="300" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Inicio planificado
                    <Input v-model="form.fechaInicioPlanificada" type="date" class="mt-1.5 h-10" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Fin planificado
                    <Input v-model="form.fechaFinPlanificada" type="date" class="mt-1.5 h-10" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Inicio real
                    <Input v-model="form.fechaInicioReal" type="date" class="mt-1.5 h-10" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Fin real
                    <Input v-model="form.fechaFinReal" type="date" class="mt-1.5 h-10" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Monto contratado
                    <Input v-model="form.montoContratado" type="number" min="0" step="0.01" class="mt-1.5 h-10" />
                  </label>
                  <label class="block text-sm font-medium text-slate-700">
                    Moneda
                    <Input v-model="form.moneda" class="mt-1.5 h-10" maxlength="3" />
                  </label>
                </fieldset>

                <footer class="mt-4 flex flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
                  <Button variant="outline" class="h-10 px-4" type="button" :disabled="saving" @click="formOpen = false">
                    Cancelar
                  </Button>
                  <Button
                    class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                    type="submit"
                    :disabled="saving"
                  >
                    {{ saving ? 'Guardando…' : editingId ? 'Guardar cambios' : 'Crear obra' }}
                  </Button>
                </footer>
              </form>
            </section>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
        <div v-if="estadoModal" class="modal-root fixed inset-0 z-50 overflow-y-auto">
          <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="!saving && (estadoModal = null)" />
          <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
            <section
              class="modal-panel relative z-10 w-full max-w-md overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
              aria-labelledby="obra-estado-title"
            >
              <header class="flex items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <h2 id="obra-estado-title" class="text-lg font-bold text-palette-navy">
                  {{ etiquetaAccion(estadoModal.obra, estadoModal.destino) }}
                </h2>
                <Button
                  variant="ghost"
                  size="icon"
                  type="button"
                  aria-label="Cerrar"
                  :disabled="saving"
                  @click="estadoModal = null"
                >
                  <XIcon />
                </Button>
              </header>
              <div class="px-5 py-5">
                <p class="text-sm leading-relaxed text-slate-600">
                  {{ estadoModal.obra.codigo }} · {{ estadoModal.obra.nombre }} pasará de
                  <strong>{{ etiquetaEstadoObra(estadoModal.obra.estado) }}</strong>
                  a
                  <strong>{{ etiquetaEstadoObra(estadoModal.destino) }}</strong>.
                </p>
                <label
                  v-if="requiereJustificacion(estadoModal.destino)"
                  class="mt-4 block text-sm font-medium text-slate-700"
                >
                  Justificación
                  <Textarea
                    v-model="estadoModal.justificacion"
                    class="mt-1.5 min-h-24"
                    maxlength="2000"
                    placeholder="Motivo de la suspensión o cancelación"
                    required
                  />
                </label>
              </div>
              <footer class="flex flex-col-reverse gap-3 border-t border-palette-cyan/70 px-5 py-4 sm:flex-row sm:justify-end">
                <Button variant="outline" class="h-10 px-4" type="button" :disabled="saving" @click="estadoModal = null">
                  Volver
                </Button>
                <Button
                  class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                  type="button"
                  :disabled="saving"
                  @click="confirmarCambioEstado"
                >
                  {{ saving ? 'Aplicando…' : 'Confirmar' }}
                </Button>
              </footer>
            </section>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
        <div v-if="confirmacion" class="modal-root fixed inset-0 z-50 overflow-y-auto">
          <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="!saving && (confirmacion = null)" />
          <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
            <section
              class="modal-panel relative z-10 w-full max-w-md overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
              aria-labelledby="obra-confirm-title"
            >
              <header class="border-b border-palette-cyan/70 px-5 py-4">
                <h2 id="obra-confirm-title" class="text-lg font-bold text-palette-navy">
                  {{ confirmacion.titulo }}
                </h2>
              </header>
              <p class="px-5 py-5 text-sm leading-relaxed text-slate-600">{{ confirmacion.detalle }}</p>
              <footer class="flex flex-col-reverse gap-3 border-t border-palette-cyan/70 px-5 py-4 sm:flex-row sm:justify-end">
                <Button variant="outline" class="h-10 px-4" type="button" :disabled="saving" @click="confirmacion = null">
                  Cancelar
                </Button>
                <Button
                  class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                  type="button"
                  :disabled="saving"
                  @click="ejecutarConfirmacion"
                >
                  Confirmar
                </Button>
              </footer>
            </section>
          </div>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>
