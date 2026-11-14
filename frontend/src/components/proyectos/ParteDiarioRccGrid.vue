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
  'h-6 w-8 min-w-0 rounded border-transparent bg-transparent px-0.5 text-center font-mono text-[10px] font-bold tabular-nums text-slate-900'

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

function diaNombreCompleto(fecha: string): string {
  return fechaLocal(fecha)
    .toLocaleDateString('es-PE', { weekday: 'long' })
    .replace('.', '')
    .toUpperCase()
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

function fmtHoras(valor: number, vacioGuion = false): string {
  if (vacioGuion && valor <= 0) {
    return '—'
  }
  return `${valor.toLocaleString('es-PE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}h`
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
  return esDomingo(fecha) ? 'bg-amber-300 text-amber-950' : 'bg-slate-200 text-slate-900'
}

function diaSubHeaderClass(fecha: string, tono: 'ord' | '60' | '100'): string {
  if (esDomingo(fecha)) {
    return 'bg-amber-200 text-amber-950'
  }
  if (tono === '60') {
    return 'bg-indigo-50 text-indigo-900'
  }
  if (tono === '100') {
    return 'bg-violet-50 text-violet-900'
  }
  return 'bg-slate-50 text-slate-800'
}

function diaCellClass(fecha: string, hoy: boolean, cesado: boolean): string {
  return [
    !cesado && esDomingo(fecha) ? 'bg-amber-100/80' : '',
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
        <TableRow class="border-b border-slate-800 hover:bg-transparent">
          <TableHead
            rowspan="2"
            class="sticky top-0 left-0 z-30 box-border h-auto w-8 min-w-8 max-w-8 overflow-hidden bg-slate-900 px-1 text-center text-[9.5px] font-bold uppercase text-white"
          >
            #
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 left-8 z-30 box-border h-auto w-52 min-w-52 max-w-52 overflow-hidden bg-slate-900 px-3 text-[9.5px] font-bold uppercase text-white"
          >
            Obrero Construcción Civil
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 left-60 z-30 box-border h-auto w-[85px] min-w-[85px] max-w-[85px] overflow-hidden bg-slate-900 px-1 text-center text-[9.5px] font-bold uppercase text-white"
          >
            F. Ingreso
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 left-[325px] z-30 box-border h-auto w-[110px] min-w-[110px] max-w-[110px] overflow-hidden bg-slate-900 px-1 text-center text-[9.5px] font-bold uppercase text-white shadow-[2px_0_0_0_#334155]"
          >
            F. Cese
          </TableHead>
          <TableHead
            v-for="fecha in diasDelMes"
            :key="`h-${fecha}`"
            colspan="3"
            class="sticky top-0 z-20 h-auto min-w-24 px-1 py-1.5 text-center text-[9px] font-black uppercase"
            :class="diaHeaderClass(fecha)"
            :aria-current="fecha === hoyIso ? 'date' : undefined"
          >
            {{ diaNombreCompleto(fecha) }} {{ numeroDia(fecha) }}
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 z-20 h-auto min-w-[55px] bg-slate-800 px-2 py-2.5 text-center text-[9.5px] font-bold uppercase text-white"
          >
            Hrs Ord
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 z-20 h-auto min-w-[48px] bg-indigo-800 px-2 py-2.5 text-center text-[9.5px] font-bold uppercase text-white"
          >
            +60%
          </TableHead>
          <TableHead
            rowspan="2"
            class="sticky top-0 z-20 h-auto min-w-[48px] bg-violet-800 px-2 py-2.5 text-center text-[9.5px] font-bold uppercase text-white"
          >
            +100%
          </TableHead>
        </TableRow>
        <TableRow class="border-b-2 border-slate-300 hover:bg-transparent">
          <template v-for="fecha in diasDelMes" :key="`sub-${fecha}`">
            <TableHead
              class="sticky top-9 z-20 min-w-8 px-0.5 py-1 text-center text-[8.5px] font-bold"
              :class="diaSubHeaderClass(fecha, 'ord')"
            >
              Ord
            </TableHead>
            <TableHead
              class="sticky top-9 z-20 min-w-[26px] px-0.5 py-1 text-center text-[8.5px] font-extrabold"
              :class="diaSubHeaderClass(fecha, '60')"
            >
              60%
            </TableHead>
            <TableHead
              class="sticky top-9 z-20 min-w-[26px] px-0.5 py-1 text-center text-[8.5px] font-extrabold"
              :class="diaSubHeaderClass(fecha, '100')"
            >
              100%
            </TableHead>
          </template>
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
          class="border-b border-slate-300 hover:bg-transparent"
          :class="cesadoEnMes(persona) ? 'bg-amber-300/90 font-bold text-slate-950' : 'bg-white'"
        >
          <TableCell
            class="sticky left-0 z-10 box-border w-8 min-w-8 max-w-8 overflow-hidden px-1 text-center font-mono text-[10px] font-black text-slate-800"
            :class="cesadoEnMes(persona) ? 'bg-[#fcd34d]' : 'bg-white'"
          >
            {{ index + 1 }}
          </TableCell>
          <TableHead
            scope="row"
            class="sticky left-8 z-10 box-border w-52 min-w-52 max-w-52 overflow-hidden whitespace-normal px-3 py-2 text-left font-medium"
            :class="cesadoEnMes(persona) ? 'bg-[#fcd34d]' : 'bg-white'"
          >
            <div class="flex items-center gap-1.5">
              <span class="truncate text-[11px] font-black leading-tight text-slate-950">
                {{ persona.apellidos }}, {{ persona.nombres }}
              </span>
              <Badge
                v-if="cesadoEnMes(persona)"
                class="shrink-0 border-amber-800 bg-amber-800 px-1.5 py-0.5 text-[8px] font-black uppercase text-white"
              >
                CESADO
              </Badge>
            </div>
            <span class="mt-0.5 block font-mono text-[10px] font-bold text-slate-700">
              {{ persona.categoria }}
            </span>
          </TableHead>
          <TableCell
            class="sticky left-60 z-10 box-border w-[85px] min-w-[85px] max-w-[85px] overflow-hidden px-1 text-center font-mono text-[10px] font-bold text-slate-800"
            :class="cesadoEnMes(persona) ? 'bg-[#fcd34d]' : 'bg-white'"
          >
            {{ fechaCorta(persona.fechaIngreso) }}
          </TableCell>
          <TableCell
            class="sticky left-[325px] z-10 box-border w-[110px] min-w-[110px] max-w-[110px] overflow-hidden px-1 text-center font-mono text-[10px] font-bold shadow-[2px_0_0_0_#cbd5e1]"
            :class="cesadoEnMes(persona) ? 'bg-[#fcd34d]' : 'bg-white'"
          >
            {{ fechaCorta(persona.fechaCese) }}
          </TableCell>
          <template v-for="fecha in diasDelMes" :key="clave(persona.personaId, fecha)">
            <template v-if="!diaDe(persona.personaId, fecha) || !vigente(persona, fecha)">
              <TableCell
                v-for="sub in ['ord', '60', '100']"
                :key="`${persona.personaId}-${fecha}-${sub}`"
                class="min-w-8 bg-slate-100 p-0 text-center text-slate-400"
                :class="diaCellClass(fecha, fecha === hoyIso, cesadoEnMes(persona))"
                :title="`Sin vigencia el ${fecha}`"
              >
                —
              </TableCell>
            </template>
            <template v-else-if="esIncidencia(diaDe(persona.personaId, fecha)!.codigo)">
              <TableCell
                class="p-0 text-center"
                :class="diaCellClass(fecha, fecha === hoyIso, cesadoEnMes(persona))"
              >
                <div class="flex items-center justify-center gap-0.5">
                  <button
                    type="button"
                    class="inline-flex h-6 min-w-8 items-center justify-center rounded border px-1 font-mono text-[10px] font-black"
                    :class="codigoBadgeClass(diaDe(persona.personaId, fecha)!.codigo)"
                    :disabled="cerrado"
                    :aria-label="`Atributos del ${fecha} de ${persona.apellidos}`"
                    @click="abrirExtras($event, diaDe(persona.personaId, fecha)!)"
                  >
                    {{ diaDe(persona.personaId, fecha)!.codigo }}
                  </button>
                </div>
              </TableCell>
              <TableCell
                class="p-0 text-center"
                :class="diaCellClass(fecha, fecha === hoyIso, cesadoEnMes(persona))"
              />
              <TableCell
                class="p-0 text-center"
                :class="diaCellClass(fecha, fecha === hoyIso, cesadoEnMes(persona))"
              />
            </template>
            <template v-else>
              <TableCell
                class="p-0"
                :class="diaCellClass(fecha, fecha === hoyIso, cesadoEnMes(persona))"
              >
                <div class="flex items-center justify-center">
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
                    :model-value="diaDe(persona.personaId, fecha)!.horasOrd || ''"
                    :placeholder="'·'"
                    @update:model-value="setHoras(diaDe(persona.personaId, fecha)!, 'horasOrd', $event)"
                    @dblclick="abrirExtras($event, diaDe(persona.personaId, fecha)!)"
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-xs"
                    class="size-5"
                    :disabled="cerrado"
                    :aria-label="`Atributos del ${fecha} de ${persona.apellidos}`"
                    @click="abrirExtras($event, diaDe(persona.personaId, fecha)!)"
                  >
                    <EllipsisVerticalIcon class="size-3" />
                  </Button>
                </div>
              </TableCell>
              <TableCell
                class="p-0 text-center"
                :class="diaCellClass(fecha, fecha === hoyIso, cesadoEnMes(persona))"
              >
                <label class="sr-only" :for="`he60-${persona.personaId}-${fecha}`">
                  Horas extras 60 por ciento de {{ persona.apellidos }} el {{ fecha }}
                </label>
                <Input
                  :id="`he60-${persona.personaId}-${fecha}`"
                  :class="[cellInputClass, 'text-indigo-800']"
                  type="number"
                  min="0"
                  max="24"
                  step="0.5"
                  inputmode="decimal"
                  :disabled="cerrado"
                  :model-value="diaDe(persona.personaId, fecha)!.he60 || ''"
                  @update:model-value="setHoras(diaDe(persona.personaId, fecha)!, 'he60', $event)"
                />
              </TableCell>
              <TableCell
                class="p-0 text-center"
                :class="diaCellClass(fecha, fecha === hoyIso, cesadoEnMes(persona))"
              >
                <label class="sr-only" :for="`he100-${persona.personaId}-${fecha}`">
                  Horas extras 100 por ciento de {{ persona.apellidos }} el {{ fecha }}
                </label>
                <Input
                  :id="`he100-${persona.personaId}-${fecha}`"
                  :class="[cellInputClass, 'text-violet-800']"
                  type="number"
                  min="0"
                  max="24"
                  step="0.5"
                  inputmode="decimal"
                  :disabled="cerrado"
                  :model-value="diaDe(persona.personaId, fecha)!.he100 || ''"
                  @update:model-value="setHoras(diaDe(persona.personaId, fecha)!, 'he100', $event)"
                />
              </TableCell>
            </template>
          </template>
          <TableCell class="bg-slate-200 px-2 text-center font-mono font-black text-slate-900">
            {{ fmtHoras(totalDe(persona, 'horasOrd')) }}
          </TableCell>
          <TableCell class="bg-indigo-100 px-2 text-center font-mono font-black text-indigo-900">
            {{ fmtHoras(totalDe(persona, 'he60'), true) }}
          </TableCell>
          <TableCell class="bg-violet-100 px-2 text-center font-mono font-black text-violet-900">
            {{ fmtHoras(totalDe(persona, 'he100'), true) }}
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
