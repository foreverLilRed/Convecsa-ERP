<script setup lang="ts">
import { PlusIcon, SettingsIcon, XIcon } from '@lucide/vue'
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
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
import { mesesCatalogo } from '@/types/nomina'
import {
  DIAS_SEMANA,
  ESTADOS_ASISTENCIA,
  diaMes,
  type AsistenciaFeriado,
  type AsistenciaPlanilla,
  type AsistenciaRegistro,
  type EstadoAsistencia,
} from '@/types/asistencia'

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const cellInputClass =
  'h-8 w-16 rounded border border-slate-200 px-1.5 text-right text-xs text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const cellSelectClass =
  'h-8 w-[5.6rem] rounded border border-slate-200 px-1 text-[11px] text-slate-800 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

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
const meses = mesesCatalogo()

const dirtyCount = computed(() => Object.keys(edits).length)
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

function totalFila(registros: AsistenciaRegistro[]): number {
  return Math.round(registros.reduce((acc, registro) => acc + horasDe(registro), 0) * 100) / 100
}

function registroDe(filaRegistros: AsistenciaRegistro[], fecha: string): AsistenciaRegistro | undefined {
  return filaRegistros.find((registro) => registro.fecha === fecha)
}

function onHoras(registro: AsistenciaRegistro, esperado: number, value: string) {
  const estado = estadoDe(registro)
  markEdit(registro, value, estado, esperado)
}

function onEstado(registro: AsistenciaRegistro, esperado: number, estado: EstadoAsistencia) {
  let horas = String(horasDe(registro))
  if (estado === 'FALTA' || estado === 'DESCANSO') {
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

async function load() {
  loading.value = true
  try {
    planilla.value = await obtenerPlanilla(anio.value, mes.value)
    clearEdits()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la asistencia.')
  } finally {
    loading.value = false
  }
}

async function onGenerar() {
  acting.value = true
  try {
    planilla.value = await generarPlanilla(anio.value, mes.value)
    clearEdits()
    toast.success('Planilla generada.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo generar la planilla.')
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
  try {
    planilla.value = await actualizarPlanilla(items)
    clearEdits()
    toast.success('Asistencia guardada.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la asistencia.')
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

function onDocumentKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && configOpen.value && !configSaving.value) {
    configOpen.value = false
  }
}

watch(configOpen, async (isOpen) => {
  if (isOpen) {
    lockBodyScroll()
    document.addEventListener('keydown', onDocumentKeydown)
    configError.value = ''
    configDias.value = [...(planilla.value?.config.diasSemana ?? [1, 2, 3, 4, 5])]
    feriadoForm.fecha = ''
    feriadoForm.nombre = ''
    try {
      feriados.value = await listarFeriados(anio.value)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los feriados.')
    }
  } else {
    document.removeEventListener('keydown', onDocumentKeydown)
  }
})

onUnmounted(() => {
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
    return
  }
  configSaving.value = true
  try {
    const config = await guardarConfigAsistencia(configDias.value)
    if (planilla.value) {
      planilla.value = { ...planilla.value, config }
    }
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
  if (!feriadoForm.fecha || !feriadoForm.nombre.trim()) {
    toast.error('Indique fecha y nombre del feriado.')
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
    toast.error(err instanceof Error ? err.message : 'No se pudo crear el feriado.')
  } finally {
    configSaving.value = false
  }
}

async function onEliminarFeriado(id: number) {
  configSaving.value = true
  try {
    await eliminarFeriado(id)
    feriados.value = feriados.value.filter((item) => item.id !== id)
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

onMounted(() => {
  void load()
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Asistencia</h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Marque las horas de cada día laborable para el personal con contrato activo. Las horas esperadas salen de la jornada.
        </p>
      </div>
      <Button
        variant="outline"
        class="h-10 px-4 sm:mt-7"
        type="button"
        aria-label="Configurar días laborables y feriados"
        @click="configOpen = true"
      >
        <SettingsIcon data-icon="inline-start" />
        Configurar
      </Button>
    </div>

    <div class="mt-6 flex flex-col gap-3 rounded-lg border border-palette-cyan/70 bg-white px-4 py-4 sm:flex-row sm:items-end sm:justify-between sm:px-6">
      <div class="flex flex-wrap items-end gap-3">
        <label class="text-sm font-medium text-slate-700" for="asistencia-mes">
          Mes
          <select id="asistencia-mes" v-model.number="mes" :class="inputClass">
            <option v-for="item in meses" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </label>
        <label class="text-sm font-medium text-slate-700" for="asistencia-anio">
          Año
          <input
            id="asistencia-anio"
            v-model.number="anio"
            type="number"
            min="2000"
            max="2100"
            :class="inputClass"
          />
        </label>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button
          class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="acting || loading"
          @click="onGenerar"
        >
          Generar mes
        </Button>
        <Button
          variant="outline"
          class="h-10 px-4"
          type="button"
          :disabled="saving || dirtyCount === 0"
          @click="onGuardar"
        >
          {{ saving ? 'Guardando…' : `Guardar cambios${dirtyCount ? ` (${dirtyCount})` : ''}` }}
        </Button>
      </div>
    </div>

    <p class="mt-3 text-sm text-slate-500" aria-live="polite">
      Total del mes: {{ totalHoras }} h
      <span v-if="planilla"> · {{ planilla.diasLaborables.length }} días laborables · {{ planilla.filas.length }} personas</span>
    </p>

    <div class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <div
        v-if="loading && !planilla"
        class="space-y-3"
        aria-busy="true"
      >
        <span class="sr-only">Cargando planilla de asistencia</span>
        <Skeleton class="h-9 w-full" />
        <Skeleton v-for="row in 6" :key="row" class="h-12 w-full" />
      </div>

      <div v-else-if="planilla && !planilla.generado" class="py-8 text-center">
        <p class="text-sm text-slate-500">
          Aún no hay registros para este mes. Genere la planilla con el personal de contrato activo y los días laborables configurados.
        </p>
      </div>

      <div v-else-if="planilla" class="overflow-x-auto">
        <Table class="min-w-max">
          <TableCaption class="sr-only">Planilla mensual de asistencia por persona y día laborable.</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead class="sticky left-0 z-10 min-w-52 bg-white px-3">Persona</TableHead>
              <TableHead class="min-w-36 px-3">Jornada</TableHead>
              <TableHead
                v-for="fecha in planilla.diasLaborables"
                :key="fecha"
                class="px-2 text-center"
              >
                {{ diaMes(fecha) }}
              </TableHead>
              <TableHead class="px-3 text-right">Total</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!planilla.filas.length" :colspan="3 + planilla.diasLaborables.length">
              <span class="text-slate-500">No hay personas con contrato activo vigente en el mes.</span>
            </TableEmpty>
            <TableRow v-for="fila in planilla.filas" :key="fila.personaId">
              <TableCell class="sticky left-0 z-10 bg-white px-3 py-2 font-medium">
                {{ fila.personaApellidos }}, {{ fila.personaNombres }}
                <span class="mt-0.5 block text-xs font-normal text-slate-500">{{ fila.numeroDocumento }}</span>
              </TableCell>
              <TableCell class="px-3 py-2 text-xs text-slate-600">
                {{ fila.jornadaLabel || '8 h / día' }}
                <span class="block text-[11px] text-slate-400">{{ fila.horasEsperadasDia }} h esperadas</span>
              </TableCell>
              <TableCell
                v-for="fecha in planilla.diasLaborables"
                :key="`${fila.personaId}-${fecha}`"
                class="px-1 py-2 align-top"
              >
                <template v-if="registroDe(fila.registros, fecha)">
                  <div class="flex flex-col items-center gap-1">
                    <label class="sr-only" :for="`horas-${registroDe(fila.registros, fecha)!.id}`">
                      Horas {{ diaMes(fecha) }} {{ fila.personaApellidos }}
                    </label>
                    <input
                      :id="`horas-${registroDe(fila.registros, fecha)!.id}`"
                      :class="cellInputClass"
                      type="number"
                      min="0"
                      max="24"
                      step="0.25"
                      :value="horasDe(registroDe(fila.registros, fecha)!)"
                      @input="onHoras(registroDe(fila.registros, fecha)!, fila.horasEsperadasDia, ($event.target as HTMLInputElement).value)"
                    />
                    <label class="sr-only" :for="`estado-${registroDe(fila.registros, fecha)!.id}`">
                      Estado {{ diaMes(fecha) }} {{ fila.personaApellidos }}
                    </label>
                    <select
                      :id="`estado-${registroDe(fila.registros, fecha)!.id}`"
                      :class="cellSelectClass"
                      :value="estadoDe(registroDe(fila.registros, fecha)!)"
                      @change="onEstado(registroDe(fila.registros, fecha)!, fila.horasEsperadasDia, ($event.target as HTMLSelectElement).value as EstadoAsistencia)"
                    >
                      <option v-for="item in ESTADOS_ASISTENCIA" :key="item.value" :value="item.value">
                        {{ item.label }}
                      </option>
                    </select>
                  </div>
                </template>
                <span v-else class="block text-center text-xs text-slate-300">—</span>
              </TableCell>
              <TableCell class="px-3 py-2 text-right font-semibold">
                {{ totalFila(fila.registros) }}
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </div>

    <Teleport to="body">
      <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
        <div v-if="configOpen" class="modal-root fixed inset-0 z-50 overflow-y-auto">
          <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="!configSaving && (configOpen = false)" />
          <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
            <section
              class="modal-panel relative z-10 flex h-[min(92vh,44rem)] w-full max-w-2xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
              aria-labelledby="asistencia-config-title"
            >
              <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <div>
                  <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                    Gestión Humana / Asistencia
                  </p>
                  <h2 id="asistencia-config-title" class="mt-1 text-lg font-bold text-palette-navy">
                    Días laborables y feriados
                  </h2>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  type="button"
                  aria-label="Cerrar"
                  :disabled="configSaving"
                  @click="configOpen = false"
                >
                  <XIcon />
                </Button>
              </header>

              <div class="min-h-0 flex-1 space-y-5 overflow-y-auto px-5 py-5">
                <fieldset :disabled="configSaving">
                  <legend class="text-sm font-semibold text-palette-navy">Días de la semana</legend>
                  <div class="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-4">
                    <label
                      v-for="dia in DIAS_SEMANA"
                      :key="dia.value"
                      class="flex items-center gap-2 text-sm text-slate-700"
                      :for="`asistencia-dia-${dia.value}`"
                    >
                      <input
                        :id="`asistencia-dia-${dia.value}`"
                        type="checkbox"
                        class="h-4 w-4 rounded border-slate-300"
                        :checked="configDias.includes(dia.value)"
                        @change="toggleDia(dia.value)"
                      />
                      {{ dia.label }}
                    </label>
                  </div>
                </fieldset>

                <div>
                  <h3 class="text-sm font-semibold text-palette-navy">Feriados {{ anio }}</h3>
                  <p class="mt-1 text-xs text-slate-500">
                    Un feriado no laborable se omite al generar la planilla.
                  </p>
                  <form class="mt-3 grid gap-2 sm:grid-cols-[9rem_1fr_auto]" @submit.prevent="onCrearFeriado">
                    <label class="sr-only" for="feriado-fecha">Fecha</label>
                    <input id="feriado-fecha" v-model="feriadoForm.fecha" type="date" :class="inputClass" />
                    <label class="sr-only" for="feriado-nombre">Nombre</label>
                    <input
                      id="feriado-nombre"
                      v-model="feriadoForm.nombre"
                      type="text"
                      :class="inputClass"
                      placeholder="Nombre del feriado"
                    />
                    <Button class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal" type="submit" :disabled="configSaving">
                      <PlusIcon data-icon="inline-start" />
                      Agregar
                    </Button>
                  </form>
                  <ul v-if="feriados.length" class="mt-3 divide-y divide-palette-cyan/40 rounded-lg border border-palette-cyan/60">
                    <li
                      v-for="item in feriados"
                      :key="item.id"
                      class="flex items-center justify-between gap-3 px-3 py-2 text-sm"
                    >
                      <span>
                        <span class="font-medium text-palette-navy">{{ item.fecha }}</span>
                        <span class="text-slate-600"> · {{ item.nombre }}</span>
                      </span>
                      <Button variant="ghost" size="sm" type="button" :disabled="configSaving" @click="onEliminarFeriado(item.id)">
                        Quitar
                      </Button>
                    </li>
                  </ul>
                  <p v-else class="mt-3 text-sm text-slate-500">No hay feriados cargados este año.</p>
                </div>
              </div>

              <p v-if="configError" class="px-5 text-sm font-medium text-red-600" role="alert">{{ configError }}</p>
              <footer class="flex shrink-0 flex-col-reverse gap-3 border-t border-palette-cyan/70 px-5 py-4 sm:flex-row sm:justify-end">
                <Button variant="outline" class="h-10 px-4" type="button" :disabled="configSaving" @click="configOpen = false">
                  Cancelar
                </Button>
                <Button
                  class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                  type="button"
                  :disabled="configSaving"
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
