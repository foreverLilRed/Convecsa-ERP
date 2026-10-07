<script setup lang="ts">
import { CalendarDaysIcon, ChevronLeftIcon, ChevronRightIcon, PlusIcon, XIcon } from '@lucide/vue'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
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
  actualizarPlanilla,
  crearFeriado,
  eliminarFeriado,
  generarPlanilla,
  guardarConfigAsistencia,
  listarFeriados,
  obtenerPlanilla,
} from '@/services/asistencia/asistenciaService'
import { mesesCatalogo, periodoLabel } from '@/types/nomina'
import {
  DIAS_SEMANA,
  ESTADOS_ASISTENCIA,
  diaCorto,
  diaMes,
  fechaLarga,
  type AsistenciaFeriado,
  type AsistenciaFila,
  type AsistenciaPlanilla,
  type AsistenciaRegistro,
  type EstadoAsistencia,
} from '@/types/asistencia'

type FilaVista = AsistenciaFila & {
  porFecha: Record<string, AsistenciaRegistro>
}

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const cellInputClass =
  'h-8 w-full rounded-md border border-slate-300 bg-white px-1.5 text-center text-sm tabular-nums text-slate-900 focus-visible:border-sky-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/30 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500'

const cellSelectClass =
  'h-8 w-full rounded-md border px-1 text-xs font-medium text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/30'

const FOCUSABLE =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

const hoy = new Date()
const anio = ref(hoy.getFullYear())
const mes = ref(hoy.getMonth() + 1)
const loading = ref(true)
const acting = ref(false)
const saving = ref(false)
const planilla = ref<AsistenciaPlanilla | null>(null)
const edits = reactive<Record<number, { horasTrabajadas: string; estado: EstadoAsistencia }>>({})
const configOpen = ref(false)
const configSaving = ref(false)
const configError = ref('')
const configDias = ref<number[]>([1, 2, 3, 4, 5])
const feriados = ref<AsistenciaFeriado[]>([])
const feriadoForm = reactive({ fecha: '', nombre: '' })
const feriadoError = ref('')
const meses = mesesCatalogo()
const configTriggerWrap = ref<HTMLElement | null>(null)
const configPanel = ref<HTMLElement | null>(null)
const estadoVivo = ref('')
let skipPeriodoWatch = false

const hoyIso = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`

const dirtyCount = computed(() => Object.keys(edits).length)
const tituloPeriodo = computed(() => periodoLabel(anio.value, mes.value))

const filasVista = computed<FilaVista[]>(() => {
  if (!planilla.value) {
    return []
  }
  return planilla.value.filas.map((fila) => ({
    ...fila,
    porFecha: Object.fromEntries(fila.registros.map((registro) => [registro.fecha, registro])),
  }))
})

const totalHoras = computed(() => {
  if (!planilla.value) {
    return 0
  }
  let suma = 0
  for (const fila of planilla.value.filas) {
    for (const registro of fila.registros) {
      suma += horasDe(registro)
    }
  }
  return Math.round(suma * 100) / 100
})

const totalFaltas = computed(() => {
  if (!planilla.value) {
    return 0
  }
  let n = 0
  for (const fila of planilla.value.filas) {
    for (const registro of fila.registros) {
      if (estadoDe(registro) === 'FALTA') {
        n += 1
      }
    }
  }
  return n
})

function horasDe(registro: AsistenciaRegistro): number {
  const edit = edits[registro.id]
  if (edit) {
    const valor = Number(edit.horasTrabajadas)
    return Number.isFinite(valor) ? valor : 0
  }
  return Number(registro.horasTrabajadas)
}

function estadoDe(registro: AsistenciaRegistro): EstadoAsistencia {
  return edits[registro.id]?.estado ?? registro.estado
}

function horasBloqueadas(estado: EstadoAsistencia): boolean {
  return estado === 'FALTA' || estado === 'DESCANSO'
}

function totalFila(registros: AsistenciaRegistro[]): number {
  return Math.round(registros.reduce((acc, registro) => acc + horasDe(registro), 0) * 100) / 100
}

function estadoClase(estado: EstadoAsistencia): string {
  switch (estado) {
    case 'ASISTIO':
      return 'border-l-3 border-l-emerald-700 bg-emerald-100'
    case 'FALTA':
      return 'border-l-3 border-l-red-700 bg-red-100'
    case 'PERMISO':
      return 'border-l-3 border-l-amber-700 bg-amber-100'
    default:
      return 'border-l-3 border-l-slate-500 bg-slate-200'
  }
}

function estadoSelectClase(estado: EstadoAsistencia): string {
  switch (estado) {
    case 'ASISTIO':
      return 'border-emerald-400 bg-emerald-50'
    case 'FALTA':
      return 'border-red-400 bg-red-50'
    case 'PERMISO':
      return 'border-amber-400 bg-amber-50'
    default:
      return 'border-slate-400 bg-slate-100'
  }
}

function onHoras(registro: AsistenciaRegistro, esperado: number, value: string) {
  markEdit(registro, value, estadoDe(registro), esperado)
}

function onEstado(registro: AsistenciaRegistro, esperado: number, estado: EstadoAsistencia) {
  let horas = String(horasDe(registro))
  if (estado === 'FALTA' || estado === 'DESCANSO' || estado === 'PERMISO') {
    horas = '0'
  } else if (estado === 'ASISTIO' && Number(horas) === 0) {
    horas = String(esperado)
  }
  markEdit(registro, horas, estado, esperado)
}

function markEdit(
  registro: AsistenciaRegistro,
  horasTrabajadas: string,
  estado: EstadoAsistencia,
  _esperado: number,
) {
  const originalHoras = String(Number(registro.horasTrabajadas))
  const sameHoras = Number(horasTrabajadas) === Number(originalHoras)
  const sameEstado = estado === registro.estado
  if (sameHoras && sameEstado) {
    delete edits[registro.id]
    return
  }
  edits[registro.id] = { horasTrabajadas, estado }
}

function clearEdits() {
  for (const key of Object.keys(edits)) {
    delete edits[Number(key)]
  }
}

function onDescartar() {
  if (dirtyCount.value === 0) {
    return
  }
  if (!window.confirm('¿Descartar los cambios sin guardar?')) {
    return
  }
  clearEdits()
  estadoVivo.value = 'Cambios descartados'
}

function cambiarPeriodo(delta: number) {
  const siguiente = new Date(anio.value, mes.value - 1 + delta, 1)
  anio.value = siguiente.getFullYear()
  mes.value = siguiente.getMonth() + 1
}

function confirmarCambioPeriodo(): boolean {
  if (dirtyCount.value === 0) {
    return true
  }
  return window.confirm('Hay cambios sin guardar. Si continúa se perderán. ¿Cambiar de mes?')
}

async function load() {
  loading.value = true
  estadoVivo.value = 'Cargando planilla de asistencia'
  try {
    planilla.value = await obtenerPlanilla(anio.value, mes.value)
    clearEdits()
    estadoVivo.value = planilla.value.generado
      ? `Planilla de ${tituloPeriodo.value} con ${planilla.value.filas.length} personas`
      : `Sin planilla generada para ${tituloPeriodo.value}`
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo cargar la asistencia.'
    estadoVivo.value = message
    toast.error(message)
  } finally {
    loading.value = false
  }
}

async function onGenerar() {
  if (planilla.value?.generado) {
    const ok = window.confirm(
      'Ya hay registros este mes. Solo se crearán los días o personas que falten; no se pisarán las horas ya marcadas. ¿Continuar?',
    )
    if (!ok) {
      return
    }
  }
  acting.value = true
  estadoVivo.value = 'Generando planilla'
  try {
    planilla.value = await generarPlanilla(anio.value, mes.value)
    clearEdits()
    estadoVivo.value = 'Planilla generada'
    toast.success('Planilla generada.')
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo generar la planilla.'
    estadoVivo.value = message
    toast.error(message)
  } finally {
    acting.value = false
  }
}

async function onGuardar() {
  const items = Object.entries(edits).map(([id, value]) => ({
    id: Number(id),
    horasTrabajadas: Number(value.horasTrabajadas),
    estado: value.estado,
  }))
  if (!items.length) {
    return
  }
  for (const item of items) {
    if (!Number.isFinite(item.horasTrabajadas) || item.horasTrabajadas < 0 || item.horasTrabajadas > 24) {
      toast.error('Las horas deben estar entre 0 y 24.')
      return
    }
  }
  saving.value = true
  estadoVivo.value = 'Guardando asistencia'
  try {
    planilla.value = await actualizarPlanilla(items)
    clearEdits()
    estadoVivo.value = 'Asistencia guardada'
    toast.success('Asistencia guardada.')
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo guardar la asistencia.'
    estadoVivo.value = message
    toast.error(message)
  } finally {
    saving.value = false
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

function focusablesDelDialogo(): HTMLElement[] {
  if (!configPanel.value) {
    return []
  }
  return [...configPanel.value.querySelectorAll<HTMLElement>(FOCUSABLE)].filter(
    (el) => el.getAttribute('aria-hidden') !== 'true',
  )
}

function onDocumentKeydown(event: KeyboardEvent) {
  if (!configOpen.value) {
    return
  }
  if (event.key === 'Escape' && !configSaving.value) {
    event.preventDefault()
    configOpen.value = false
    return
  }
  if (event.key !== 'Tab') {
    return
  }
  const items = focusablesDelDialogo()
  if (!items.length) {
    event.preventDefault()
    configPanel.value?.focus()
    return
  }
  const first = items[0]
  const last = items[items.length - 1]
  const active = document.activeElement
  if (event.shiftKey) {
    if (active === first || active === configPanel.value || !configPanel.value?.contains(active)) {
      event.preventDefault()
      last.focus()
    }
  } else if (active === last) {
    event.preventDefault()
    first.focus()
  }
}

function onWindowKeydown(event: KeyboardEvent) {
  if (!(event.ctrlKey || event.metaKey) || event.key.toLowerCase() !== 's' || event.repeat) {
    return
  }
  if (configOpen.value) {
    event.preventDefault()
    if (!configSaving.value) {
      void onGuardarConfig()
    }
    return
  }
  if (dirtyCount.value > 0 && !saving.value) {
    event.preventDefault()
    void onGuardar()
  }
}

function onBeforeUnload(event: BeforeUnloadEvent) {
  if (dirtyCount.value > 0) {
    event.preventDefault()
    event.returnValue = ''
  }
}

watch(configOpen, async (isOpen) => {
  if (isOpen) {
    lockBodyScroll()
    document.addEventListener('keydown', onDocumentKeydown)
    configError.value = ''
    feriadoError.value = ''
    configDias.value = [...(planilla.value?.config.diasSemana ?? [1, 2, 3, 4, 5])]
    feriadoForm.fecha = ''
    feriadoForm.nombre = ''
    try {
      feriados.value = await listarFeriados(anio.value)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los feriados.')
    }
    await nextTick()
    configPanel.value?.focus()
  } else {
    document.removeEventListener('keydown', onDocumentKeydown)
    await nextTick()
    const trigger = configTriggerWrap.value?.querySelector('button')
    trigger?.focus()
  }
})

onMounted(() => {
  window.addEventListener('keydown', onWindowKeydown)
  window.addEventListener('beforeunload', onBeforeUnload)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onWindowKeydown)
  window.removeEventListener('beforeunload', onBeforeUnload)
  document.removeEventListener('keydown', onDocumentKeydown)
  unlockBodyScroll()
})

function toggleDia(dia: number) {
  if (configDias.value.includes(dia)) {
    configDias.value = configDias.value.filter((item) => item !== dia)
  } else {
    configDias.value = [...configDias.value, dia].sort((a, b) => a - b)
  }
}

async function onGuardarConfig() {
  if (!configDias.value.length) {
    configError.value = 'Seleccione al menos un día laborable.'
    toast.error(configError.value)
    await nextTick()
    document.getElementById('asistencia-dia-1')?.focus()
    return
  }
  configSaving.value = true
  try {
    await guardarConfigAsistencia(configDias.value)
    toast.success('Calendario actualizado.')
    configOpen.value = false
    await load()
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo guardar la configuración.'
    configError.value = message
    toast.error(message)
  } finally {
    configSaving.value = false
  }
}

async function onCrearFeriado() {
  feriadoError.value = ''
  if (!feriadoForm.fecha || !feriadoForm.nombre.trim()) {
    feriadoError.value = 'Indique fecha y nombre del feriado.'
    toast.error(feriadoError.value)
    document.getElementById(feriadoForm.fecha ? 'feriado-nombre' : 'feriado-fecha')?.focus()
    return
  }
  configSaving.value = true
  try {
    await crearFeriado({ fecha: feriadoForm.fecha, nombre: feriadoForm.nombre.trim() })
    feriadoForm.fecha = ''
    feriadoForm.nombre = ''
    feriados.value = await listarFeriados(anio.value)
    toast.success('Feriado agregado.')
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear el feriado.'
    feriadoError.value = message
    toast.error(message)
  } finally {
    configSaving.value = false
  }
}

async function onEliminarFeriado(item: AsistenciaFeriado) {
  const ok = window.confirm(`¿Quitar el feriado ${item.nombre} del ${item.fecha}?`)
  if (!ok) {
    return
  }
  configSaving.value = true
  try {
    await eliminarFeriado(item.id)
    feriados.value = feriados.value.filter((feriado) => feriado.id !== item.id)
    toast.success('Feriado eliminado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo eliminar el feriado.')
  } finally {
    configSaving.value = false
  }
}

watch([anio, mes], (siguiente, anterior) => {
  if (skipPeriodoWatch) {
    skipPeriodoWatch = false
    return
  }
  if (anterior && dirtyCount.value > 0 && !confirmarCambioPeriodo()) {
    skipPeriodoWatch = true
    anio.value = anterior[0]
    mes.value = anterior[1]
    return
  }
  void load()
})

void load()
</script>

<template>
  <section>
    <p class="sr-only" role="status" aria-live="polite">{{ estadoVivo }}</p>

    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Asistencia</h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-600">
          Planilla mensual del personal con contrato activo. Las horas se prellenan según la jornada; Falta y Descanso quedan en 0 h.
        </p>
      </div>
      <div ref="configTriggerWrap" class="sm:mt-7">
        <Button
          variant="outline"
          class="h-10 w-full px-4 sm:w-auto"
          type="button"
          :aria-expanded="configOpen"
          aria-controls="asistencia-config-dialog"
          aria-haspopup="dialog"
          @click="configOpen = true"
        >
          <CalendarDaysIcon data-icon="inline-start" />
          Configurar calendario
        </Button>
      </div>
    </div>

    <dl class="mt-6 grid gap-3 sm:grid-cols-3">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-4">
        <dt class="text-xs font-semibold uppercase tracking-wide text-slate-600">Periodo</dt>
        <dd class="mt-2 text-lg font-bold text-palette-navy">{{ tituloPeriodo }}</dd>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-4">
        <dt class="text-xs font-semibold uppercase tracking-wide text-slate-600">Días laborables</dt>
        <dd class="mt-2 text-lg font-bold text-palette-navy">{{ planilla?.diasLaborables.length ?? '—' }}</dd>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-4">
        <dt class="text-xs font-semibold uppercase tracking-wide text-slate-600">Horas del mes</dt>
        <dd class="mt-2 text-lg font-bold text-palette-navy">{{ totalHoras }} h</dd>
        <dd v-if="totalFaltas" class="mt-1 text-xs font-medium text-red-800">
          {{ totalFaltas }} falta{{ totalFaltas === 1 ? '' : 's' }}
        </dd>
      </div>
    </dl>

    <div class="mt-4 flex flex-col gap-3 rounded-lg border border-palette-cyan/70 bg-white px-4 py-4 sm:flex-row sm:items-end sm:justify-between sm:px-6">
      <div class="flex flex-wrap items-end gap-2">
        <Button
          variant="outline"
          size="icon"
          class="h-10 w-10"
          type="button"
          :disabled="loading || acting || saving"
          aria-label="Mes anterior"
          @click="cambiarPeriodo(-1)"
        >
          <ChevronLeftIcon class="cn-rtl-flip" />
        </Button>
        <div>
          <label class="text-sm font-medium text-slate-700" for="asistencia-mes">Mes</label>
          <select
            id="asistencia-mes"
            v-model.number="mes"
            :class="inputClass"
            :disabled="loading || acting || saving"
            :aria-describedby="dirtyCount ? 'asistencia-dirty' : undefined"
          >
            <option v-for="item in meses" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="asistencia-anio">Año</label>
          <input
            id="asistencia-anio"
            v-model.number="anio"
            type="number"
            min="2000"
            max="2100"
            :class="inputClass"
            :disabled="loading || acting || saving"
          />
        </div>
        <Button
          variant="outline"
          size="icon"
          class="h-10 w-10"
          type="button"
          :disabled="loading || acting || saving"
          aria-label="Mes siguiente"
          @click="cambiarPeriodo(1)"
        >
          <ChevronRightIcon class="cn-rtl-flip" />
        </Button>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button
          class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="acting || loading"
          :aria-busy="acting"
          :aria-describedby="planilla && !planilla.generado ? 'asistencia-vacio' : undefined"
          @click="onGenerar"
        >
          {{ acting ? 'Generando…' : planilla?.generado ? 'Completar días faltantes' : 'Generar mes' }}
        </Button>
        <Button
          class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="saving || dirtyCount === 0"
          :aria-busy="saving"
          :aria-describedby="dirtyCount ? 'asistencia-dirty' : undefined"
          :title="dirtyCount ? 'Atajo: Ctrl+S' : undefined"
          @click="onGuardar"
        >
          {{ saving ? 'Guardando…' : dirtyCount ? `Guardar ${dirtyCount} cambio${dirtyCount === 1 ? '' : 's'}` : 'Guardar cambios' }}
        </Button>
      </div>
    </div>

    <div
      v-if="dirtyCount"
      id="asistencia-dirty"
      class="mt-3 flex flex-col gap-2 rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-950 sm:flex-row sm:items-center sm:justify-between"
      role="status"
    >
      <p>
        Hay {{ dirtyCount }} celda{{ dirtyCount === 1 ? '' : 's' }} sin guardar.
        <span class="text-amber-900">Atajo: Ctrl+S.</span>
      </p>
      <Button variant="outline" size="sm" class="h-8 px-3" type="button" @click="onDescartar">
        Descartar
      </Button>
    </div>

    <ul class="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-xs text-slate-800" aria-label="Leyenda de estados">
      <li class="inline-flex items-center gap-1.5">
        <span class="size-3 rounded-sm border-l-3 border-l-emerald-700 bg-emerald-100" aria-hidden="true" />
        Asistió
      </li>
      <li class="inline-flex items-center gap-1.5">
        <span class="size-3 rounded-sm border-l-3 border-l-red-700 bg-red-100" aria-hidden="true" />
        Falta (0 h)
      </li>
      <li class="inline-flex items-center gap-1.5">
        <span class="size-3 rounded-sm border-l-3 border-l-amber-700 bg-amber-100" aria-hidden="true" />
        Permiso
      </li>
      <li class="inline-flex items-center gap-1.5">
        <span class="size-3 rounded-sm border-l-3 border-l-slate-500 bg-slate-200" aria-hidden="true" />
        Descanso (0 h)
      </li>
    </ul>

    <div class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <div class="mb-4 flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h3 class="text-base font-semibold text-palette-navy">Planilla</h3>
          <p class="mt-1 text-sm text-slate-600">
            Desplace horizontalmente para ver todos los días. La columna de persona permanece fija.
          </p>
        </div>
        <p v-if="planilla" class="text-sm text-slate-600">
          {{ planilla.filas.length }} persona{{ planilla.filas.length === 1 ? '' : 's' }}
        </p>
      </div>

      <div
        v-if="loading && !planilla"
        class="space-y-3"
        aria-busy="true"
      >
        <span class="sr-only">Cargando planilla de asistencia</span>
        <Skeleton class="h-9 w-full" />
        <Skeleton v-for="row in 6" :key="row" class="h-14 w-full" />
      </div>

      <div v-else-if="planilla && !planilla.generado" id="asistencia-vacio" class="rounded-lg border border-dashed border-palette-cyan/80 bg-palette-ice/40 px-4 py-10 text-center">
        <p class="text-sm text-slate-600">
          Aún no hay registros para {{ tituloPeriodo }}. Genere la planilla con el personal de contrato activo y los días laborables configurados.
        </p>
        <Button
          class="mt-4 h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="acting"
          @click="onGenerar"
        >
          Generar {{ tituloPeriodo }}
        </Button>
      </div>

      <div
        v-else-if="planilla"
        class="max-h-[min(70vh,42rem)] overflow-auto rounded-lg border border-palette-cyan/60 **:data-[slot=table-container]:overflow-visible"
        tabindex="0"
        role="region"
        aria-label="Planilla de asistencia. Desplace para ver todos los días. La columna de persona permanece fija."
      >
        <Table class="min-w-max">
          <TableCaption class="sr-only">
            {{ tituloPeriodo }} · {{ planilla.diasLaborables.length }} días laborables.
            Cada celda tiene horas y estado. Falta y Descanso dejan las horas en cero.
          </TableCaption>
          <TableHeader>
            <TableRow class="hover:bg-transparent">
              <TableHead class="sticky top-0 left-0 z-30 min-w-52 bg-palette-ice px-3 shadow-[2px_0_0_0_#8cd2f5]">
                Persona
              </TableHead>
              <TableHead class="sticky top-0 z-20 min-w-40 bg-palette-ice px-3">Jornada</TableHead>
              <TableHead
                v-for="fecha in planilla.diasLaborables"
                :key="fecha"
                class="sticky top-0 z-20 min-w-26 px-1.5 text-center"
                :class="fecha === hoyIso ? 'bg-sky-100' : 'bg-palette-ice'"
                :aria-current="fecha === hoyIso ? 'date' : undefined"
              >
                <span class="block text-[10px] font-semibold uppercase tracking-wide text-slate-600">
                  {{ diaCorto(fecha) }}
                </span>
                <span class="text-sm font-bold text-palette-navy">{{ diaMes(fecha) }}</span>
                <span class="sr-only">{{ fechaLarga(fecha) }}{{ fecha === hoyIso ? ', hoy' : '' }}</span>
              </TableHead>
              <TableHead class="sticky top-0 z-20 bg-palette-ice px-3 text-right">Total h</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!filasVista.length" :colspan="3 + planilla.diasLaborables.length">
              <span class="text-slate-600">No hay personas con contrato activo vigente en el mes.</span>
            </TableEmpty>
            <TableRow
              v-for="fila in filasVista"
              :key="fila.personaId"
              class="hover:bg-transparent"
            >
              <TableHead
                scope="row"
                class="sticky left-0 z-10 min-w-52 whitespace-normal bg-white px-3 py-3 text-left font-medium shadow-[2px_0_0_0_#8cd2f5]"
              >
                {{ fila.personaApellidos }}, {{ fila.personaNombres }}
                <span class="mt-0.5 block text-xs font-normal text-slate-600">{{ fila.numeroDocumento }}</span>
              </TableHead>
              <TableCell class="px-3 py-3 text-sm text-slate-700">
                {{ fila.jornadaLabel || '8 horas diarias' }}
                <span class="mt-0.5 block text-xs text-slate-600">{{ fila.horasEsperadasDia }} h esperadas</span>
              </TableCell>
              <TableCell
                v-for="fecha in planilla.diasLaborables"
                :key="`${fila.personaId}-${fecha}`"
                class="px-1 py-1.5 align-middle"
                :class="[
                  fila.porFecha[fecha] ? estadoClase(estadoDe(fila.porFecha[fecha])) : 'bg-white',
                  fecha === hoyIso ? 'ring-1 ring-inset ring-sky-400' : '',
                ]"
              >
                <div v-if="fila.porFecha[fecha]" class="flex min-w-24 flex-col gap-1 p-1">
                  <label class="sr-only" :for="`horas-${fila.porFecha[fecha].id}`">
                    Horas de {{ fila.personaApellidos }}, {{ fila.personaNombres }} el {{ fechaLarga(fecha) }}
                  </label>
                  <input
                    :id="`horas-${fila.porFecha[fecha].id}`"
                    :class="cellInputClass"
                    type="number"
                    min="0"
                    max="24"
                    step="0.25"
                    inputmode="decimal"
                    autocomplete="off"
                    :disabled="horasBloqueadas(estadoDe(fila.porFecha[fecha]))"
                    :aria-describedby="horasBloqueadas(estadoDe(fila.porFecha[fecha])) ? 'asistencia-horas-bloqueadas' : undefined"
                    :title="horasBloqueadas(estadoDe(fila.porFecha[fecha])) ? 'Las horas quedan en 0 con Falta o Descanso' : 'Horas trabajadas'"
                    :value="horasDe(fila.porFecha[fecha])"
                    @input="onHoras(fila.porFecha[fecha], fila.horasEsperadasDia, ($event.target as HTMLInputElement).value)"
                  />
                  <label class="sr-only" :for="`estado-${fila.porFecha[fecha].id}`">
                    Estado de {{ fila.personaApellidos }}, {{ fila.personaNombres }} el {{ fechaLarga(fecha) }}
                  </label>
                  <select
                    :id="`estado-${fila.porFecha[fecha].id}`"
                    :class="[cellSelectClass, estadoSelectClase(estadoDe(fila.porFecha[fecha]))]"
                    :value="estadoDe(fila.porFecha[fecha])"
                    @change="onEstado(fila.porFecha[fecha], fila.horasEsperadasDia, ($event.target as HTMLSelectElement).value as EstadoAsistencia)"
                  >
                    <option v-for="item in ESTADOS_ASISTENCIA" :key="item.value" :value="item.value">
                      {{ item.label }}
                    </option>
                  </select>
                </div>
                <span
                  v-else
                  class="block px-1 py-3 text-center text-xs font-medium text-slate-600"
                  :title="`Sin registro el ${fechaLarga(fecha)}. El contrato puede no estar vigente ese día.`"
                >
                  —
                  <span class="sr-only">Sin registro el {{ fechaLarga(fecha) }}</span>
                </span>
              </TableCell>
              <TableCell class="px-3 py-3 text-right text-sm font-semibold tabular-nums text-palette-navy">
                {{ totalFila(fila.registros) }}
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
      <p id="asistencia-horas-bloqueadas" class="sr-only">
        Las horas quedan en 0 y no se editan cuando el estado es Falta o Descanso.
      </p>
    </div>

    <Teleport to="body">
      <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
        <div v-if="configOpen" class="modal-root fixed inset-0 z-50 overflow-y-auto">
          <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="!configSaving && (configOpen = false)" />
          <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
            <section
              id="asistencia-config-dialog"
              ref="configPanel"
              class="modal-panel relative z-10 flex h-[min(92vh,46rem)] w-full max-w-2xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
              aria-labelledby="asistencia-config-title"
              aria-describedby="asistencia-config-desc"
              :aria-busy="configSaving"
              tabindex="-1"
            >
              <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <div>
                  <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                    Gestión Humana / Asistencia
                  </p>
                  <h2 id="asistencia-config-title" class="mt-1 text-lg font-bold text-palette-navy">
                    Calendario laborable
                  </h2>
                  <p id="asistencia-config-desc" class="mt-1 text-sm text-slate-600">
                    Aplica a todo el personal. Los feriados no laborables se omiten al generar.
                  </p>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  type="button"
                  aria-label="Cerrar configuración"
                  :disabled="configSaving"
                  @click="configOpen = false"
                >
                  <XIcon />
                </Button>
              </header>

              <div class="min-h-0 flex-1 space-y-6 overflow-y-auto px-5 py-5">
                <fieldset
                  :disabled="configSaving"
                  :aria-describedby="configError ? 'asistencia-config-error' : 'asistencia-dias-ayuda'"
                >
                  <legend class="text-sm font-semibold text-palette-navy">Días de la semana</legend>
                  <p id="asistencia-dias-ayuda" class="mt-1 text-sm text-slate-600">
                    Seleccione al menos un día. Aplica a todo el personal.
                  </p>
                  <div class="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-4">
                    <label
                      v-for="dia in DIAS_SEMANA"
                      :key="dia.value"
                      class="flex cursor-pointer items-center gap-2 rounded-lg border px-3 py-2 text-sm text-slate-800"
                      :class="configDias.includes(dia.value)
                        ? 'border-palette-royal bg-palette-ice text-palette-navy'
                        : 'border-slate-200 bg-white'"
                      :for="`asistencia-dia-${dia.value}`"
                    >
                      <input
                        :id="`asistencia-dia-${dia.value}`"
                        type="checkbox"
                        class="h-4 w-4 rounded border-slate-300 text-palette-navy focus:ring-palette-royal"
                        :checked="configDias.includes(dia.value)"
                        @change="toggleDia(dia.value)"
                      />
                      {{ dia.label }}
                    </label>
                  </div>
                </fieldset>

                <div>
                  <h3 class="text-sm font-semibold text-palette-navy">Feriados de {{ anio }}</h3>
                  <p class="mt-1 text-sm text-slate-600">
                    Agregue feriados nacionales o institucionales. No se generan celdas ese día.
                  </p>
                  <form class="mt-3 space-y-3" @submit.prevent="onCrearFeriado">
                    <div class="grid gap-3 sm:grid-cols-2">
                      <div>
                        <label class="text-sm font-medium text-slate-700" for="feriado-fecha">Fecha</label>
                        <input
                          id="feriado-fecha"
                          v-model="feriadoForm.fecha"
                          type="date"
                          :class="inputClass"
                          required
                          :aria-invalid="!!feriadoError"
                          :aria-describedby="feriadoError ? 'feriado-error' : undefined"
                        />
                      </div>
                      <div>
                        <label class="text-sm font-medium text-slate-700" for="feriado-nombre">Nombre</label>
                        <input
                          id="feriado-nombre"
                          v-model="feriadoForm.nombre"
                          type="text"
                          maxlength="160"
                          :class="inputClass"
                          placeholder="Ej. Día de la Independencia"
                          required
                          :aria-invalid="!!feriadoError"
                          :aria-describedby="feriadoError ? 'feriado-error' : undefined"
                        />
                      </div>
                    </div>
                    <p v-if="feriadoError" id="feriado-error" class="text-sm font-medium text-red-800" role="alert">
                      {{ feriadoError }}
                    </p>
                    <Button class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal" type="submit" :disabled="configSaving">
                      <PlusIcon data-icon="inline-start" />
                      Agregar feriado
                    </Button>
                  </form>
                  <ul v-if="feriados.length" class="mt-4 divide-y divide-palette-cyan/40 rounded-lg border border-palette-cyan/60">
                    <li
                      v-for="item in feriados"
                      :key="item.id"
                      class="flex items-center justify-between gap-3 px-3 py-2.5 text-sm"
                    >
                      <span>
                        <time class="font-medium text-palette-navy" :datetime="item.fecha">{{ fechaLarga(item.fecha) }}</time>
                        <span class="text-slate-700"> · {{ item.nombre }}</span>
                      </span>
                      <Button
                        variant="ghost"
                        size="sm"
                        type="button"
                        :disabled="configSaving"
                        :aria-label="`Quitar feriado ${item.nombre} del ${item.fecha}`"
                        @click="onEliminarFeriado(item)"
                      >
                        Quitar
                      </Button>
                    </li>
                  </ul>
                  <p v-else class="mt-4 text-sm text-slate-600">No hay feriados cargados este año.</p>
                </div>
              </div>

              <p v-if="configError" id="asistencia-config-error" class="px-5 text-sm font-medium text-red-800" role="alert">
                {{ configError }}
              </p>
              <footer class="flex shrink-0 flex-col-reverse gap-3 border-t border-palette-cyan/70 px-5 py-4 sm:flex-row sm:justify-end">
                <Button variant="outline" class="h-10 px-4" type="button" :disabled="configSaving" @click="configOpen = false">
                  Cancelar
                </Button>
                <Button
                  class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                  type="button"
                  :disabled="configSaving"
                  :aria-busy="configSaving"
                  :aria-describedby="configError ? 'asistencia-config-error' : undefined"
                  title="Atajo: Ctrl+S"
                  @click="onGuardarConfig"
                >
                  Guardar calendario
                </Button>
              </footer>
            </section>
          </div>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>
