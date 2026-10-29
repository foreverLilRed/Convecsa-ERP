<script setup lang="ts">
import {
  ArrowUpRightIcon,
  CalendarDaysIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  PlusIcon,
  XIcon,
} from '@lucide/vue'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import {
  crearFeriado,
  eliminarFeriado,
  generarPlanilla,
  guardarConfigAsistencia,
  eliminarConceptoVariable,
  listarFeriados,
  listarConceptosVariables,
  obtenerPlanilla,
  registrarConceptoVariable,
} from '@/services/asistencia/asistenciaService'
import { mesesCatalogo, periodoLabel } from '@/types/nomina'
import {
  DIAS_SEMANA,
  fechaLarga,
  type AsistenciaFeriado,
  type AsistenciaPlanilla,
  type AsistenciaConceptoVariable,
  type TipoConceptoVariableTareo,
} from '@/types/asistencia'

const router = useRouter()

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const FOCUSABLE =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

const hoy = new Date()
const anio = ref(hoy.getFullYear())
const mes = ref(hoy.getMonth() + 1)
const loading = ref(true)
const acting = ref(false)
const planilla = ref<AsistenciaPlanilla | null>(null)
const configOpen = ref(false)
const configSaving = ref(false)
const configError = ref('')
const configDias = ref<number[]>([1, 2, 3, 4, 5])
const feriados = ref<AsistenciaFeriado[]>([])
const feriadoForm = reactive({ fecha: '', nombre: '' })
const feriadoError = ref('')
const conceptosVariables = ref<AsistenciaConceptoVariable[]>([])
const conceptoSaving = ref(false)
const conceptoForm = reactive<{
  personaId: string
  tipo: TipoConceptoVariableTareo
  monto: string
  detalle: string
}>({ personaId: '', tipo: 'COMISION', monto: '', detalle: '' })
const meses = mesesCatalogo()
const configTriggerWrap = ref<HTMLElement | null>(null)
const configPanel = ref<HTMLElement | null>(null)
const estadoVivo = ref('')

const tituloPeriodo = computed(() => periodoLabel(anio.value, mes.value))

const totalHoras = computed(() => {
  if (!planilla.value) {
    return 0
  }
  let suma = 0
  for (const fila of planilla.value.filas) {
    for (const registro of fila.registros) {
      suma += Number(registro.horasTrabajadas)
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
      if (registro.estado === 'FALTA') {
        n += 1
      }
    }
  }
  return n
})

function cambiarPeriodo(delta: number) {
  const siguiente = new Date(anio.value, mes.value - 1 + delta, 1)
  anio.value = siguiente.getFullYear()
  mes.value = siguiente.getMonth() + 1
}

async function load() {
  loading.value = true
  estadoVivo.value = 'Cargando asistencia'
  try {
    planilla.value = await obtenerPlanilla(anio.value, mes.value)
    conceptosVariables.value = await listarConceptosVariables(anio.value, mes.value)
    if (!conceptoForm.personaId && planilla.value.filas[0]) {
      conceptoForm.personaId = String(planilla.value.filas[0].personaId)
    }
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

function abrirPlanilla() {
  const route = router.resolve({
    name: 'gh-asistencia-planilla',
    query: { anio: String(anio.value), mes: String(mes.value) },
  })
  const opened = window.open(route.href, '_blank')
  if (!opened) {
    toast.error('El navegador bloqueó la nueva pestaña. Permita ventanas emergentes para este sitio.')
    return
  }
  opened.opener = null
}

async function onRegistrarConceptoVariable() {
  const personaId = Number(conceptoForm.personaId)
  const fila = planilla.value?.filas.find((item) => item.personaId === personaId)
  const monto = Number(conceptoForm.monto)
  if (!fila || !Number.isFinite(monto) || monto < 0) {
    toast.error('Seleccione una persona e ingrese un monto válido.')
    return
  }
  conceptoSaving.value = true
  try {
    const creado = await registrarConceptoVariable(anio.value, mes.value, {
      personaId,
      contratoId: fila.contratoId,
      tipo: conceptoForm.tipo,
      monto,
      detalle: conceptoForm.detalle || null,
    })
    conceptosVariables.value = [...conceptosVariables.value, creado]
    conceptoForm.monto = ''
    conceptoForm.detalle = ''
    toast.success('Concepto variable registrado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el concepto.')
  } finally {
    conceptoSaving.value = false
  }
}

async function onEliminarConceptoVariable(id: number) {
  try {
    await eliminarConceptoVariable(id)
    conceptosVariables.value = conceptosVariables.value.filter((item) => item.id !== id)
    toast.success('Concepto variable eliminado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo eliminar el concepto.')
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
  const first = items[0]!
  const last = items[items.length - 1]!
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
})

onUnmounted(() => {
  window.removeEventListener('keydown', onWindowKeydown)
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

watch([anio, mes], () => {
  void load()
})

void load()
</script>

<template>
  <section>
    <p class="sr-only" role="status" aria-live="polite">{{ estadoVivo }}</p>

    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Asistencia</h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-600">
          Configure el calendario, conceptos variables y abra la planilla de tareo en una pestaña dedicada.
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

    <dl class="mt-3 grid gap-2 sm:grid-cols-3">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-4">
        <dt class="text-xs font-semibold uppercase tracking-wide text-slate-600">Periodo</dt>
        <dd class="mt-2 text-lg font-bold text-palette-navy">{{ tituloPeriodo }}</dd>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-4">
        <dt class="text-xs font-semibold uppercase tracking-wide text-slate-600">Días del período</dt>
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

    <div class="mt-3 flex flex-col gap-3 rounded-lg border border-palette-cyan/70 bg-white px-4 py-4 sm:flex-row sm:items-end sm:justify-between sm:px-6">
      <div class="flex flex-wrap items-end gap-2">
        <Button
          variant="outline"
          size="icon"
          class="h-10 w-10"
          type="button"
          :disabled="loading || acting"
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
            :disabled="loading || acting"
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
            :disabled="loading || acting"
          />
        </div>
        <Button
          variant="outline"
          size="icon"
          class="h-10 w-10"
          type="button"
          :disabled="loading || acting"
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
          @click="onGenerar"
        >
          {{ acting ? 'Generando…' : planilla?.generado ? 'Completar días faltantes' : 'Generar mes' }}
        </Button>
        <Button
          class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="loading"
          @click="abrirPlanilla"
        >
          <ArrowUpRightIcon data-icon="inline-start" />
          Abrir planilla
        </Button>
      </div>
    </div>

    <section v-if="planilla?.generado" class="mt-3 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div>
        <h3 class="text-base font-semibold text-palette-navy">Conceptos variables del mes</h3>
        <p class="mt-1 text-sm text-slate-600">
          Registre comisiones, alimentación, trabajo nocturno, adelantos y descuentos. Se incorporan al generar la nómina del período.
        </p>
      </div>
      <div class="mt-4 grid gap-3 md:grid-cols-2 lg:grid-cols-5">
        <label class="text-xs font-semibold uppercase tracking-wide text-slate-700">
          Persona
          <select v-model="conceptoForm.personaId" :class="inputClass">
            <option v-for="fila in planilla.filas" :key="fila.personaId" :value="String(fila.personaId)">
              {{ fila.personaApellidos }}, {{ fila.personaNombres }}
            </option>
          </select>
        </label>
        <label class="text-xs font-semibold uppercase tracking-wide text-slate-700">
          Concepto
          <select v-model="conceptoForm.tipo" :class="inputClass">
            <option value="COMISION">Comisión</option>
            <option value="TRABAJO_NOCTURNO">Trabajo nocturno (35%)</option>
            <option value="ASIGNACION_ALIMENTACION">Asignación de alimentación</option>
            <option value="ADELANTO_QUINCENA">Adelanto de quincena</option>
            <option value="CUOTA_SINDICAL">Cuota sindical</option>
            <option value="DESCUENTO_JUDICIAL">Descuento judicial</option>
            <option value="OTRO_INGRESO">Otro ingreso</option>
            <option value="OTRO_DESCUENTO">Otro descuento</option>
          </select>
        </label>
        <label class="text-xs font-semibold uppercase tracking-wide text-slate-700">
          Monto
          <input v-model="conceptoForm.monto" :class="inputClass" type="number" min="0" step="0.01" />
        </label>
        <label class="text-xs font-semibold uppercase tracking-wide text-slate-700">
          Detalle
          <input v-model="conceptoForm.detalle" :class="inputClass" maxlength="300" />
        </label>
        <div class="flex items-end">
          <Button class="h-10 w-full bg-palette-navy text-white hover:bg-palette-royal" type="button" :disabled="conceptoSaving" @click="onRegistrarConceptoVariable">
            {{ conceptoSaving ? 'Registrando…' : 'Agregar concepto' }}
          </Button>
        </div>
      </div>
      <div v-if="conceptosVariables.length" class="mt-4 overflow-x-auto rounded-md border border-slate-200">
        <table class="min-w-full text-sm">
          <tbody>
            <tr v-for="item in conceptosVariables" :key="item.id" class="border-b border-slate-100 last:border-0">
              <td class="px-3 py-2">{{ planilla.filas.find((fila) => fila.personaId === item.personaId)?.personaApellidos }}</td>
              <td class="px-3 py-2">{{ item.tipo.replaceAll('_', ' ') }}</td>
              <td class="px-3 py-2 text-right tabular-nums">S/ {{ item.monto.toFixed(2) }}</td>
              <td class="px-3 py-2 text-slate-600">{{ item.detalle }}</td>
              <td class="px-3 py-2 text-right"><Button variant="ghost" size="sm" type="button" @click="onEliminarConceptoVariable(item.id)">Quitar</Button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <div class="mt-3 rounded-lg border border-palette-cyan/70 bg-white px-4 py-6 sm:px-6">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 class="text-base font-semibold text-palette-navy">Planilla de tareo</h3>
          <p class="mt-1 text-sm text-slate-600">
            Se abre en una pestaña a pantalla completa, sin menú lateral, para ver y llenar todos los días con más espacio.
          </p>
          <p v-if="planilla?.generado" class="mt-2 text-sm text-slate-600">
            {{ planilla.filas.length }} persona{{ planilla.filas.length === 1 ? '' : 's' }} ·
            {{ planilla.diasLaborables.length }} días laborables
          </p>
        </div>
        <Button
          class="h-10 shrink-0 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="loading"
          @click="abrirPlanilla"
        >
          <ArrowUpRightIcon data-icon="inline-start" />
          Abrir planilla
        </Button>
      </div>
      <div
        v-if="planilla && !planilla.generado"
        class="mt-4 rounded-lg border border-dashed border-palette-cyan/80 bg-palette-ice/40 px-4 py-8 text-center"
      >
        <p class="text-sm text-slate-600">
          Aún no hay registros para {{ tituloPeriodo }}. Genere la planilla y luego ábrala para llenar el tareo.
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
                  <div class="mt-3 grid gap-2 grid-cols-2 sm:grid-cols-4">
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
