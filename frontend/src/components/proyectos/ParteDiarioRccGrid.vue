<script setup lang="ts">
import { EllipsisVerticalIcon } from '@lucide/vue'
import { onClickOutside } from '@vueuse/core'
import { computed, nextTick, ref } from 'vue'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
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
import {
  CODIGOS_TAREO,
  type CodigoTareoCc,
  type PersonalObra,
  type TareoCcDia,
  type TareoCcGrilla,
} from '@/types/construccion'

const INCIDENCIAS: CodigoTareoCc[] = ['F', 'DSC', 'FE', 'DM', 'LIC']

const FLAGS = [
  { key: 'bae', label: 'BAE' },
  { key: 'movilidad', label: 'Movilidad' },
  { key: 'altura', label: 'Altura' },
  { key: 'altitud', label: 'Altitud' },
  { key: 'cotaCero', label: 'Cota 0' },
  { key: 'altasTemp', label: 'Altas temp.' },
  { key: 'agua', label: 'Agua' },
  { key: 'aguasServidas', label: 'Aguas servidas' },
  { key: 'nocturno', label: 'Nocturno' },
] as const

const props = withDefaults(
  defineProps<{
    grilla: TareoCcGrilla
    cerrado?: boolean
    loading?: boolean
    anio: number
    mes: number
  }>(),
  {
    cerrado: false,
    loading: false,
  },
)

const cellInputClass =
  'h-6 min-w-0 rounded border-slate-300 px-0.5 text-center text-[11px] tabular-nums'

const extrasPanel = ref<HTMLElement | null>(null)
const extrasDia = ref<TareoCcDia | null>(null)
const extrasPos = ref<{ top: string; left: string }>({ top: '0px', left: '0px' })

onClickOutside(extrasPanel, () => {
  extrasDia.value = null
})

const hoyIso = isoDe(new Date())

const diasDelMes = computed(() => {
  if (props.grilla.fechas.length) {
    return props.grilla.fechas
  }
  const last = new Date(props.anio, props.mes, 0).getDate()
  return Array.from({ length: last }, (_, i) => isoDe(new Date(props.anio, props.mes - 1, i + 1)))
})

const porClave = computed(() => {
  const mapa = new Map<string, TareoCcDia>()
  for (const dia of props.grilla.dias) {
    mapa.set(clave(dia.personaId, dia.fecha), dia)
  }
  return mapa
})

const colspanVacio = computed(() => 4 + diasDelMes.value.length * 3 + 3)

function clave(personaId: number, fecha: string): string {
  return `${personaId}|${fecha}`
}

function isoDe(fecha: Date): string {
  const y = fecha.getFullYear()
  const m = String(fecha.getMonth() + 1).padStart(2, '0')
  const d = String(fecha.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function diaDe(personaId: number, fecha: string): TareoCcDia | undefined {
  return porClave.value.get(clave(personaId, fecha))
}

function fechaCorta(valor: string | null | undefined): string {
  if (!valor) {
    return '—'
  }
  const [, m, d] = valor.split('-')
  return `${d}/${m}`
}

function fechaLocal(fecha: string): Date {
  return new Date(Number(fecha.slice(0, 4)), Number(fecha.slice(5, 7)) - 1, Number(fecha.slice(8, 10)))
}

function diaCorto(fecha: string): string {
  return fechaLocal(fecha).toLocaleDateString('es-PE', { weekday: 'short' }).replace('.', '')
}

function numeroDia(fecha: string): string {
  return fecha.slice(8, 10)
}

function esDomingo(fecha: string): boolean {
  return fechaLocal(fecha).getDay() === 0
}

function vigente(persona: PersonalObra, fecha: string): boolean {
  if (persona.fechaIngreso && fecha < persona.fechaIngreso) {
    return false
  }
  if (persona.fechaCese && fecha > persona.fechaCese) {
    return false
  }
  return true
}

function cesadoEnMes(persona: PersonalObra): boolean {
  if (!persona.fechaCese) {
    return false
  }
  const desde = isoDe(new Date(props.anio, props.mes - 1, 1))
  const hasta = isoDe(new Date(props.anio, props.mes, 0))
  return persona.fechaCese >= desde && persona.fechaCese <= hasta
}

function esIncidencia(codigo: CodigoTareoCc): boolean {
  return INCIDENCIAS.includes(codigo)
}

function codigoBadgeClass(codigo: CodigoTareoCc): string {
  if (codigo === 'F') return 'border-red-200 bg-red-100 text-red-800'
  if (codigo === 'DSC') return 'border-slate-300 bg-slate-200 text-slate-700'
  if (codigo === 'FE') return 'border-amber-200 bg-amber-100 text-amber-900'
  if (codigo === 'DM') return 'border-sky-200 bg-sky-100 text-sky-800'
  if (codigo === 'LIC') return 'border-violet-200 bg-violet-100 text-violet-800'
  return ''
}

function codigoLabel(codigo: CodigoTareoCc): string {
  return CODIGOS_TAREO.find((item) => item.codigo === codigo)?.label ?? codigo
}

function totalDe(persona: PersonalObra, campo: 'horasOrd' | 'he60' | 'he100'): number {
  let suma = 0
  for (const fecha of diasDelMes.value) {
    if (!vigente(persona, fecha)) {
      continue
    }
    suma += Number(diaDe(persona.personaId, fecha)?.[campo] ?? 0)
  }
  return suma
}

function fmtHoras(valor: number): string {
  return valor.toLocaleString('es-PE', { maximumFractionDigits: 2 })
}

function setHoras(dia: TareoCcDia, campo: 'horasOrd' | 'he60' | 'he100', valor: string | number) {
  dia[campo] = Number(valor) || 0
}

function onCodigoChange(dia: TareoCcDia, codigo: string) {
  dia.codigo = codigo as CodigoTareoCc
  if (esIncidencia(dia.codigo)) {
    dia.horasOrd = 0
    dia.he60 = 0
    dia.he100 = 0
  }
  if (codigo === 'F' || codigo === 'DSC') {
    dia.movilidad = false
  }
}

async function abrirExtras(event: MouseEvent, dia: TareoCcDia) {
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  extrasDia.value = dia
  const panelAncho = 272
  const panelAlto = 360
  const left = Math.min(rect.left, window.innerWidth - panelAncho - 8)
  const top = rect.bottom + 4 + panelAlto > window.innerHeight
    ? Math.max(8, rect.top - panelAlto - 4)
    : rect.bottom + 4
  extrasPos.value = { top: `${top}px`, left: `${Math.max(8, left)}px` }
  await nextTick()
}

function diaHeaderClass(fecha: string): string {
  return esDomingo(fecha) ? 'bg-amber-100' : 'bg-palette-ice'
}

function diaCellClass(fecha: string, hoy: boolean): string {
  return [
    esDomingo(fecha) ? 'bg-amber-50' : '',
    hoy ? 'ring-1 ring-inset ring-sky-400' : '',
  ].join(' ')
}
</script>

<template>
  <div v-if="loading" class="space-y-2" aria-busy="true">
    <span class="sr-only">Cargando parte diario de asistencia</span>
    <Skeleton class="h-8 w-full" />
    <Skeleton v-for="row in 8" :key="row" class="h-10 w-full" />
  </div>

  <div
    v-else
    class="overflow-auto rounded-lg border border-palette-cyan/60 **:data-[slot=table-container]:overflow-visible"
    tabindex="0"
    role="region"
    aria-label="Parte diario de asistencia. Desplace para ver todos los días. Nombre, ingreso y cese permanecen fijos."
  >
    <Table class="min-w-max text-[11px]">
      <TableCaption class="sr-only">
        Parte diario de asistencia del 1 al {{ diasDelMes.length }}.
        Cada día tiene horas ordinarias, extras al 60 por ciento y extras al 100 por ciento.
      </TableCaption>
      <TableHeader>
        <TableRow class="hover:bg-transparent">
          <TableHead
            rowspan="2"
            class="sticky top-0 left-0 z-30 box-border h-auto w-8 min-w-8 max-w-8 overflow-hidden bg-palette-ice px-1 text-center"
          >
            #
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 left-8 z-30 box-border h-auto w-48 min-w-48 max-w-48 overflow-hidden bg-palette-ice px-1.5"
          >
            Trabajador
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 left-56 z-30 box-border h-auto w-24 min-w-24 max-w-24 overflow-hidden bg-palette-ice px-1 text-center"
          >
            F. Ingreso
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 left-80 z-30 box-border h-auto w-24 min-w-24 max-w-24 overflow-hidden bg-palette-ice px-1 text-center shadow-[2px_0_0_0_#8cd2f5]"
          >
            F. Cese
          </TableHead>
          <TableHead
            v-for="fecha in diasDelMes"
            :key="`h-${fecha}`"
            colspan="3"
            class="sticky top-0 z-20 h-10 min-w-28 px-0.5 text-center"
            :class="diaHeaderClass(fecha)"
            :aria-current="fecha === hoyIso ? 'date' : undefined"
          >
            <span class="block text-[9px] font-semibold uppercase tracking-wide text-slate-600">
              {{ diaCorto(fecha) }}
            </span>
            <span class="font-bold text-palette-navy">{{ numeroDia(fecha) }}</span>
          </TableHead>
          <TableHead
            colspan="3"
            class="sticky top-0 z-20 bg-palette-ice px-1 text-center"
          >
            Total
          </TableHead>
        </TableRow>
        <TableRow class="hover:bg-transparent">
          <template v-for="fecha in diasDelMes" :key="`sub-${fecha}`">
            <TableHead
              class="sticky top-10 z-20 min-w-10 px-0.5 py-0.5 text-center text-[9px] font-semibold text-slate-600"
              :class="diaHeaderClass(fecha)"
            >
              Ord
            </TableHead>
            <TableHead
              class="sticky top-10 z-20 min-w-10 px-0.5 py-0.5 text-center text-[9px] font-semibold text-slate-600"
              :class="diaHeaderClass(fecha)"
            >
              +60
            </TableHead>
            <TableHead
              class="sticky top-10 z-20 min-w-10 px-0.5 py-0.5 text-center text-[9px] font-semibold text-slate-600"
              :class="diaHeaderClass(fecha)"
            >
              +100
            </TableHead>
          </template>
          <TableHead class="sticky top-10 z-20 bg-palette-ice px-1 py-0.5 text-center text-[9px] font-semibold text-slate-600">
            Ord
          </TableHead>
          <TableHead class="sticky top-10 z-20 bg-palette-ice px-1 py-0.5 text-center text-[9px] font-semibold text-slate-600">
            +60
          </TableHead>
          <TableHead class="sticky top-10 z-20 bg-palette-ice px-1 py-0.5 text-center text-[9px] font-semibold text-slate-600">
            +100
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <TableEmpty v-if="!grilla.personal.length" :colspan="colspanVacio">
          <span class="text-slate-600">No hay personal RCC asignado a esta obra. Asígnalo en el alta de contrato.</span>
        </TableEmpty>
        <TableRow
          v-for="(persona, index) in grilla.personal"
          v-else
          :key="persona.personaId"
          class="hover:bg-transparent"
          :class="cesadoEnMes(persona) ? 'bg-amber-300/40' : ''"
        >
          <TableCell
            class="sticky left-0 z-10 box-border w-8 min-w-8 max-w-8 overflow-hidden px-1 text-center tabular-nums text-slate-600"
            :class="cesadoEnMes(persona) ? 'bg-amber-300/40' : 'bg-white'"
          >
            {{ index + 1 }}
          </TableCell>
          <TableHead
            scope="row"
            class="sticky left-8 z-10 box-border w-48 min-w-48 max-w-48 overflow-hidden whitespace-normal px-1.5 text-left font-medium"
            :class="cesadoEnMes(persona) ? 'bg-amber-300/40' : 'bg-white'"
          >
            {{ persona.apellidos }}, {{ persona.nombres }}
            <span class="mt-0.5 block font-normal text-[10px] text-slate-600">
              {{ persona.numeroDocumento }} · {{ persona.categoria }}
            </span>
          </TableHead>
          <TableCell
            class="sticky left-56 z-10 box-border w-24 min-w-24 max-w-24 overflow-hidden px-1 text-center tabular-nums"
            :class="cesadoEnMes(persona) ? 'bg-amber-300/40' : 'bg-white'"
          >
            {{ fechaCorta(persona.fechaIngreso) }}
          </TableCell>
          <TableCell
            class="sticky left-80 z-10 box-border w-24 min-w-24 max-w-24 overflow-hidden px-1 text-center tabular-nums shadow-[2px_0_0_0_#8cd2f5]"
            :class="cesadoEnMes(persona) ? 'bg-amber-300/40' : 'bg-white'"
          >
            {{ fechaCorta(persona.fechaCese) }}
          </TableCell>
          <template v-for="fecha in diasDelMes" :key="clave(persona.personaId, fecha)">
            <template v-if="!diaDe(persona.personaId, fecha) || !vigente(persona, fecha)">
              <TableCell
                v-for="sub in ['ord', '60', '100']"
                :key="`${persona.personaId}-${fecha}-${sub}`"
                class="min-w-10 bg-slate-100 px-0.5 py-0.5 text-center text-slate-400"
                :class="diaCellClass(fecha, fecha === hoyIso)"
                :title="`Sin vigencia el ${fecha}`"
              >
                —
              </TableCell>
            </template>
            <TableCell
              v-else-if="esIncidencia(diaDe(persona.personaId, fecha)!.codigo)"
              colspan="3"
              class="px-0.5 py-0.5"
              :class="diaCellClass(fecha, fecha === hoyIso)"
            >
              <div class="flex items-center justify-center gap-0.5">
                <Badge :class="codigoBadgeClass(diaDe(persona.personaId, fecha)!.codigo)">
                  {{ diaDe(persona.personaId, fecha)!.codigo }}
                </Badge>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon-xs"
                  :disabled="cerrado"
                  :aria-label="`Atributos del ${fecha} de ${persona.apellidos}`"
                  @click="abrirExtras($event, diaDe(persona.personaId, fecha)!)"
                >
                  <EllipsisVerticalIcon />
                </Button>
              </div>
            </TableCell>
            <template v-else>
              <TableCell
                class="min-w-10 px-0.5 py-0.5"
                :class="diaCellClass(fecha, fecha === hoyIso)"
              >
                <div class="flex items-center gap-0.5">
                  <label class="sr-only" :for="`ord-${persona.personaId}-${fecha}`">
                    Horas ordinarias de {{ persona.apellidos }} el {{ fecha }}
                  </label>
                  <Input
                    :id="`ord-${persona.personaId}-${fecha}`"
                    :class="cellInputClass"
                    type="number"
                    min="0"
                    max="24"
                    step="0.5"
                    inputmode="decimal"
                    :disabled="cerrado"
                    :model-value="diaDe(persona.personaId, fecha)!.horasOrd"
                    @update:model-value="setHoras(diaDe(persona.personaId, fecha)!, 'horasOrd', $event)"
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-xs"
                    :disabled="cerrado"
                    :aria-label="`Atributos del ${fecha} de ${persona.apellidos}`"
                    @click="abrirExtras($event, diaDe(persona.personaId, fecha)!)"
                  >
                    <EllipsisVerticalIcon />
                  </Button>
                </div>
              </TableCell>
              <TableCell
                class="min-w-10 px-0.5 py-0.5"
                :class="diaCellClass(fecha, fecha === hoyIso)"
              >
                <label class="sr-only" :for="`he60-${persona.personaId}-${fecha}`">
                  Horas extras 60 por ciento de {{ persona.apellidos }} el {{ fecha }}
                </label>
                <Input
                  :id="`he60-${persona.personaId}-${fecha}`"
                  :class="cellInputClass"
                  type="number"
                  min="0"
                  max="24"
                  step="0.5"
                  inputmode="decimal"
                  :disabled="cerrado"
                  :model-value="diaDe(persona.personaId, fecha)!.he60"
                  @update:model-value="setHoras(diaDe(persona.personaId, fecha)!, 'he60', $event)"
                />
              </TableCell>
              <TableCell
                class="min-w-10 px-0.5 py-0.5"
                :class="diaCellClass(fecha, fecha === hoyIso)"
              >
                <label class="sr-only" :for="`he100-${persona.personaId}-${fecha}`">
                  Horas extras 100 por ciento de {{ persona.apellidos }} el {{ fecha }}
                </label>
                <Input
                  :id="`he100-${persona.personaId}-${fecha}`"
                  :class="cellInputClass"
                  type="number"
                  min="0"
                  max="24"
                  step="0.5"
                  inputmode="decimal"
                  :disabled="cerrado"
                  :model-value="diaDe(persona.personaId, fecha)!.he100"
                  @update:model-value="setHoras(diaDe(persona.personaId, fecha)!, 'he100', $event)"
                />
              </TableCell>
            </template>
          </template>
          <TableCell class="px-1 text-right font-semibold tabular-nums text-palette-navy">
            {{ fmtHoras(totalDe(persona, 'horasOrd')) }}
          </TableCell>
          <TableCell class="px-1 text-right font-semibold tabular-nums text-palette-navy">
            {{ fmtHoras(totalDe(persona, 'he60')) }}
          </TableCell>
          <TableCell class="px-1 text-right font-semibold tabular-nums text-palette-navy">
            {{ fmtHoras(totalDe(persona, 'he100')) }}
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>
  </div>

  <Teleport to="body">
    <div
      v-if="extrasDia"
      ref="extrasPanel"
      class="fixed z-[80] w-64 rounded-lg border border-slate-200 bg-white p-3 shadow-xl"
      :style="extrasPos"
      role="dialog"
      aria-label="Atributos del día"
    >
      <label class="text-[11px] font-medium text-slate-700" for="tareo-codigo-dia">Código</label>
      <select
        id="tareo-codigo-dia"
        class="mt-1 h-8 w-full rounded border border-slate-200 bg-white px-1.5 text-xs text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-500/30"
        :value="extrasDia.codigo"
        :disabled="cerrado"
        @change="onCodigoChange(extrasDia, ($event.target as HTMLSelectElement).value)"
      >
        <option v-for="item in CODIGOS_TAREO" :key="item.codigo" :value="item.codigo">
          {{ item.label }}
        </option>
      </select>
      <p class="mt-1 text-[10px] text-slate-500">{{ codigoLabel(extrasDia.codigo) }}</p>
      <div class="mt-2 grid grid-cols-2 gap-1">
        <label
          v-for="flag in FLAGS"
          :key="flag.key"
          class="flex items-center gap-1 text-[11px] text-slate-700"
        >
          <input
            v-model="extrasDia[flag.key]"
            type="checkbox"
            :disabled="cerrado"
          />
          {{ flag.label }}
        </label>
      </div>
      <label class="mt-2 block text-[11px] font-medium text-slate-700" for="tareo-pisos-dia">
        Pisos
      </label>
      <Input
        id="tareo-pisos-dia"
        class="mt-1 h-8"
        type="number"
        min="0"
        step="1"
        inputmode="numeric"
        :disabled="cerrado"
        :model-value="extrasDia.pisos"
        @update:model-value="extrasDia.pisos = Number($event) || 0"
      />
    </div>
  </Teleport>
</template>
