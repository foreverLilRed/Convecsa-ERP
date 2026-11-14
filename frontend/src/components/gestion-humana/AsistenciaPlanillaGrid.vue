<script setup lang="ts">
import { onClickOutside } from '@vueuse/core'
import { computed, nextTick, ref } from 'vue'

import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
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
import type { FilaVista } from '@/composables/useAsistenciaPlanilla'
import {
  CODIGOS_TAREO,
  diaCorto,
  diaMes,
  fechaLarga,
  isoDiaSemana,
  type AsistenciaPlanilla,
  type AsistenciaRegistro,
  type CodigoTareo,
  type EstadoAsistencia,
} from '@/types/asistencia'

const CODIGOS_COMPUTABLES: CodigoTareo[] = ['A', 'TF', 'TD']

const props = withDefaults(
  defineProps<{
    planilla: AsistenciaPlanilla
    filasVista: FilaVista[]
    loading?: boolean
    compact?: boolean
    hoyIso: string
    tituloPeriodo: string
    horasDe: (registro: AsistenciaRegistro) => number
    estadoDe: (registro: AsistenciaRegistro) => EstadoAsistencia
    codigoDe: (registro: AsistenciaRegistro) => CodigoTareo
    minutosDe: (
      registro: AsistenciaRegistro,
      campo: 'minutosExtras25' | 'minutosExtras35' | 'minutosTardanza',
    ) => number
    horasBloqueadas: (codigo: CodigoTareo) => boolean
    estadoSelectClase: (estado: EstadoAsistencia) => string
  }>(),
  {
    loading: false,
    compact: false,
  },
)

const emit = defineEmits<{
  horas: [registro: AsistenciaRegistro, esperado: number, value: string]
  codigo: [registro: AsistenciaRegistro, esperado: number, codigo: CodigoTareo]
  minutos: [
    registro: AsistenciaRegistro,
    campo: 'minutosExtras25' | 'minutosExtras35' | 'minutosTardanza',
    valor: string,
  ]
}>()

const editorPanel = ref<HTMLElement | null>(null)
const editorRegistro = ref<AsistenciaRegistro | null>(null)
const editorFila = ref<FilaVista | null>(null)
const editorPos = ref({ top: '0px', left: '0px' })

onClickOutside(editorPanel, () => {
  editorRegistro.value = null
  editorFila.value = null
})

const colspanVacio = computed(() => 4 + props.planilla.diasLaborables.length + 2)

const cellInputClass =
  'h-8 w-full rounded-md border border-slate-300 bg-white px-1.5 text-center text-sm tabular-nums text-slate-900 focus-visible:border-sky-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/30 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500'

function esDomingo(fecha: string): boolean {
  return isoDiaSemana(fecha) === 7
}

function fechaCorta(valor: string | null | undefined): string {
  if (!valor) {
    return '—'
  }
  const [y, m, d] = valor.split('-')
  if (!y || !m || !d) {
    return valor
  }
  return `${d}/${m}/${y}`
}

function cesadoEnMes(fila: FilaVista): boolean {
  if (!fila.fechaCese) {
    return false
  }
  const prefijo = `${props.planilla.anio}-${String(props.planilla.mes).padStart(2, '0')}`
  return fila.fechaCese.startsWith(prefijo)
}

function stickyFila(fila: FilaVista): string {
  return cesadoEnMes(fila) ? 'bg-[#fcd34d]' : 'bg-white'
}

function codigoChipClass(codigo: CodigoTareo): string {
  switch (codigo) {
    case 'A':
    case 'TF':
    case 'TD':
      return 'bg-[#dcfce7] text-[#15803d] border-[#bbf7d0]'
    case 'FI':
      return 'bg-[#ffe4e6] text-[#be123c] border-[#fecdd3]'
    case 'DM':
    case 'SIT':
      return 'bg-[#dbeafe] text-[#1d4ed8] border-[#bfdbfe]'
    case 'D':
    case 'F':
      return 'bg-[#f1f5f9] text-[#64748b] border-[#e2e8f0]'
    case 'VAC':
    case 'VAC_COMP':
      return 'bg-[#f3e8ff] text-[#6b21a8] border-[#e9d5ff]'
    case 'LSG':
    case 'LCG':
    case 'PAT':
    case 'MAT_PRE':
    case 'MAT_POST':
      return 'bg-[#ffedd5] text-[#9a3412] border-[#fed7aa]'
    default:
      return 'bg-slate-100 text-slate-700 border-slate-300'
  }
}

function letraDe(codigo: CodigoTareo): string {
  if (codigo === 'MAT_PRE') return 'PRE'
  if (codigo === 'MAT_POST') return 'POST'
  if (codigo === 'VAC_COMP') return 'VC'
  return codigo
}

function labelCodigo(codigo: CodigoTareo): string {
  return CODIGOS_TAREO.find((item) => item.value === codigo)?.label ?? codigo
}

function diasComputables(fila: FilaVista): number {
  return fila.registros.filter((registro) => CODIGOS_COMPUTABLES.includes(props.codigoDe(registro))).length
}

function faltasFila(fila: FilaVista): number {
  return fila.registros.filter((registro) => props.codigoDe(registro) === 'FI').length
}

function tieneTardanza(registro: AsistenciaRegistro): boolean {
  return props.minutosDe(registro, 'minutosTardanza') > 0
}

async function abrirEditor(event: MouseEvent, fila: FilaVista, registro: AsistenciaRegistro) {
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  editorFila.value = fila
  editorRegistro.value = registro
  const panelAncho = 272
  const panelAlto = 280
  const left = Math.min(rect.left, window.innerWidth - panelAncho - 8)
  const top = rect.bottom + 4 + panelAlto > window.innerHeight
    ? Math.max(8, rect.top - panelAlto - 4)
    : rect.bottom + 4
  editorPos.value = { top: `${top}px`, left: `${Math.max(8, left)}px` }
  await nextTick()
}
</script>

<template>
  <div :class="compact ? 'flex h-full min-h-0 flex-col' : ''">
    <div
      v-if="loading && !planilla"
      class="space-y-2"
      aria-busy="true"
    >
      <span class="sr-only">Cargando planilla de asistencia</span>
      <Skeleton class="h-8 w-full" />
      <Skeleton v-for="row in 8" :key="row" class="h-12 w-full" />
    </div>

    <div
      v-else
      class="overflow-auto rounded-lg border border-slate-300 bg-white **:data-[slot=table-container]:overflow-visible"
      :class="compact ? 'min-h-0 flex-1' : 'max-h-[min(70vh,42rem)]'"
      tabindex="0"
      role="region"
      aria-label="Parte diario de asistencia del 1 al 31. La columna de empleado permanece fija. Clic en la letra para editar horas y extras."
    >
      <Table class="min-w-max text-[11px]">
        <TableCaption class="sr-only">
          {{ tituloPeriodo }} · {{ planilla.diasLaborables.length }} días.
          Cada celda muestra el código de tareo. Clic para editar horas, extras y tardanza.
        </TableCaption>
        <TableHeader>
          <TableRow class="hover:bg-transparent">
            <TableHead
              class="sticky top-0 left-0 z-30 h-auto w-8 min-w-8 max-w-8 bg-slate-900 px-1 py-2 text-center text-[9.5px] font-bold uppercase text-white"
            >
              ITEM
            </TableHead>
            <TableHead
              class="sticky top-0 left-8 z-30 h-auto w-[210px] min-w-[210px] max-w-[210px] bg-slate-900 px-3 py-2 text-[9.5px] font-bold uppercase text-white"
            >
              EMPLEADO (RGC)
            </TableHead>
            <TableHead
              class="sticky top-0 left-[242px] z-30 h-auto w-[90px] min-w-[90px] max-w-[90px] bg-slate-900 px-2 py-2 text-center text-[9.5px] font-bold uppercase text-white"
            >
              F.INGRESO
            </TableHead>
            <TableHead
              class="sticky top-0 left-[332px] z-30 h-auto w-[110px] min-w-[110px] max-w-[110px] bg-slate-900 px-2 py-2 text-center text-[9.5px] font-bold uppercase text-white shadow-[2px_0_0_0_#334155]"
            >
              F.CESE
            </TableHead>
            <TableHead
              v-for="fecha in planilla.diasLaborables"
              :key="fecha"
              class="sticky top-0 z-20 h-auto min-w-7 px-1 py-2 text-center"
              :class="esDomingo(fecha) ? 'bg-amber-100 text-amber-950' : 'bg-slate-200 text-slate-800'"
              :aria-current="fecha === hoyIso ? 'date' : undefined"
            >
              <span class="block text-[8px] font-bold uppercase">{{ diaCorto(fecha) }}</span>
              <span class="font-mono text-[10px] font-bold">{{ diaMes(fecha) }}</span>
              <span class="sr-only">{{ fechaLarga(fecha) }}{{ fecha === hoyIso ? ', hoy' : '' }}</span>
            </TableHead>
            <TableHead class="sticky top-0 z-20 h-auto min-w-16 bg-emerald-800 px-2 py-2 text-center text-[9.5px] font-bold uppercase text-white">
              Días Comp.
            </TableHead>
            <TableHead class="sticky top-0 z-20 h-auto min-w-14 bg-rose-800 px-2 py-2 text-center text-[9.5px] font-bold uppercase text-white">
              Faltas
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableEmpty v-if="!filasVista.length" :colspan="colspanVacio">
            <span class="text-slate-600">No hay personas con contrato activo vigente en el mes.</span>
          </TableEmpty>
          <TableRow
            v-for="(fila, index) in filasVista"
            :key="fila.personaId"
            class="border-b border-slate-300 hover:bg-transparent"
            :class="cesadoEnMes(fila) ? 'bg-amber-300/90 font-bold text-slate-950' : 'bg-white'"
          >
            <TableCell
              class="sticky left-0 z-10 w-8 min-w-8 max-w-8 px-1 text-center font-mono text-[10px] font-black text-slate-800"
              :class="stickyFila(fila)"
            >
              {{ index + 1 }}
            </TableCell>
            <TableHead
              scope="row"
              class="sticky left-8 z-10 w-[210px] min-w-[210px] max-w-[210px] overflow-hidden px-3 py-1.5 text-left font-medium"
              :class="stickyFila(fila)"
            >
              <div class="flex items-center gap-1.5">
                <span class="truncate text-[11px] font-black text-slate-950">
                  {{ fila.personaApellidos }}, {{ fila.personaNombres }}
                </span>
                <Badge
                  v-if="cesadoEnMes(fila)"
                  class="shrink-0 border-amber-800 bg-amber-800 px-1.5 py-0.5 text-[8px] font-black uppercase text-white"
                >
                  CESADO
                </Badge>
              </div>
              <span v-if="fila.cargo" class="mt-0.5 block truncate font-mono text-[9.5px] font-semibold text-slate-700">
                {{ fila.cargo }}
              </span>
              <span class="mt-0.5 block font-mono text-[9.5px] font-normal text-slate-600">
                {{ fila.numeroDocumento }}
              </span>
            </TableHead>
            <TableCell
              class="sticky left-[242px] z-10 w-[90px] min-w-[90px] max-w-[90px] px-2 text-center font-mono text-[10px] font-bold text-slate-800"
              :class="stickyFila(fila)"
            >
              {{ fechaCorta(fila.fechaIngreso) }}
            </TableCell>
            <TableCell
              class="sticky left-[332px] z-10 w-[110px] min-w-[110px] max-w-[110px] px-1 text-center font-mono text-[10px] font-bold shadow-[2px_0_0_0_#cbd5e1]"
              :class="stickyFila(fila)"
            >
              {{ fechaCorta(fila.fechaCese) }}
            </TableCell>
            <TableCell
              v-for="fecha in planilla.diasLaborables"
              :key="`${fila.personaId}-${fecha}`"
              class="p-0.5 text-center"
              :class="esDomingo(fecha) && !cesadoEnMes(fila) ? 'bg-amber-50' : ''"
            >
              <button
                v-if="fila.porFecha[fecha]"
                type="button"
                class="relative mx-auto flex h-6 w-[26px] items-center justify-center rounded border font-mono text-[10px] font-bold shadow-sm"
                :class="codigoChipClass(codigoDe(fila.porFecha[fecha]))"
                :title="`${labelCodigo(codigoDe(fila.porFecha[fecha]))} · ${fechaLarga(fecha)}. Clic para editar.`"
                :aria-label="`Código ${codigoDe(fila.porFecha[fecha])} de ${fila.personaApellidos} el ${fechaLarga(fecha)}. Clic para editar horas y extras.`"
                @click="abrirEditor($event, fila, fila.porFecha[fecha])"
              >
                {{ letraDe(codigoDe(fila.porFecha[fecha])) }}
                <span
                  v-if="tieneTardanza(fila.porFecha[fecha])"
                  class="absolute -top-0.5 -right-0.5 rounded bg-amber-200 px-0.5 text-[7px] font-black text-amber-900"
                  title="Tardanza"
                >
                  T
                </span>
              </button>
              <span
                v-else
                class="block py-1 text-center font-medium text-slate-400"
                :title="`Sin registro el ${fechaLarga(fecha)}. El contrato puede no estar vigente ese día.`"
              >
                —
                <span class="sr-only">Sin registro el {{ fechaLarga(fecha) }}</span>
              </span>
            </TableCell>
            <TableCell class="bg-emerald-100 px-2 text-center font-mono font-black text-emerald-900">
              {{ diasComputables(fila) }}
            </TableCell>
            <TableCell class="bg-rose-100 px-2 text-center font-mono font-black text-rose-900">
              {{ faltasFila(fila) }}
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>

  <Teleport to="body">
    <div
      v-if="editorRegistro && editorFila"
      ref="editorPanel"
      class="fixed z-[80] w-72 rounded-lg border border-slate-200 bg-white p-3 shadow-xl"
      :style="editorPos"
      role="dialog"
      :aria-label="`Editar ${fechaLarga(editorRegistro.fecha)} de ${editorFila.personaApellidos}`"
    >
      <p class="text-[11px] font-semibold text-slate-800">
        {{ editorFila.personaApellidos }}, {{ editorFila.personaNombres }}
      </p>
      <p class="text-[10px] text-slate-500">{{ fechaLarga(editorRegistro.fecha) }}</p>

      <label class="mt-2 block text-[11px] font-medium text-slate-700" :for="`horas-${editorRegistro.id}`">
        Horas trabajadas
      </label>
      <Input
        :id="`horas-${editorRegistro.id}`"
        :class="cellInputClass"
        class="mt-1"
        type="number"
        min="0"
        max="24"
        step="0.25"
        inputmode="decimal"
        autocomplete="off"
        :disabled="horasBloqueadas(codigoDe(editorRegistro))"
        :model-value="horasDe(editorRegistro)"
        @update:model-value="emit('horas', editorRegistro, editorFila.horasEsperadasDia, String($event ?? ''))"
      />

      <label class="mt-2 block text-[11px] font-medium text-slate-700" :for="`codigo-${editorRegistro.id}`">
        Código
      </label>
      <select
        :id="`codigo-${editorRegistro.id}`"
        class="mt-1 h-8 w-full rounded-md border px-1.5 text-xs font-medium text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/30"
        :class="estadoSelectClase(estadoDe(editorRegistro))"
        :value="codigoDe(editorRegistro)"
        @change="emit('codigo', editorRegistro, editorFila.horasEsperadasDia, ($event.target as HTMLSelectElement).value as CodigoTareo)"
      >
        <option v-for="item in CODIGOS_TAREO" :key="item.value" :value="item.value">
          {{ item.label }}
        </option>
      </select>

      <div class="mt-2 grid grid-cols-3 gap-1.5">
        <label class="text-center text-[10px] font-semibold text-slate-600">
          HE 25%
          <Input
            :id="`extras-25-${editorRegistro.id}`"
            :class="cellInputClass"
            class="mt-0.5"
            type="number"
            min="0"
            max="1440"
            step="1"
            inputmode="numeric"
            :model-value="minutosDe(editorRegistro, 'minutosExtras25')"
            @update:model-value="emit('minutos', editorRegistro, 'minutosExtras25', String($event ?? ''))"
          />
        </label>
        <label class="text-center text-[10px] font-semibold text-slate-600">
          HE 35%
          <Input
            :id="`extras-35-${editorRegistro.id}`"
            :class="cellInputClass"
            class="mt-0.5"
            type="number"
            min="0"
            max="1440"
            step="1"
            inputmode="numeric"
            :model-value="minutosDe(editorRegistro, 'minutosExtras35')"
            @update:model-value="emit('minutos', editorRegistro, 'minutosExtras35', String($event ?? ''))"
          />
        </label>
        <label class="text-center text-[10px] font-semibold text-slate-600">
          Tardanza
          <Input
            :id="`tardanza-${editorRegistro.id}`"
            :class="cellInputClass"
            class="mt-0.5"
            type="number"
            min="0"
            max="1440"
            step="1"
            inputmode="numeric"
            :model-value="minutosDe(editorRegistro, 'minutosTardanza')"
            @update:model-value="emit('minutos', editorRegistro, 'minutosTardanza', String($event ?? ''))"
          />
        </label>
      </div>
    </div>
  </Teleport>
</template>
