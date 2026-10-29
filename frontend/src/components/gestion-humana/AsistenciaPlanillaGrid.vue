<script setup lang="ts">
import { computed } from 'vue'

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
  type AsistenciaPlanilla,
  type AsistenciaRegistro,
  type CodigoTareo,
  type EstadoAsistencia,
} from '@/types/asistencia'

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
    totalFila: (registros: AsistenciaRegistro[]) => number
    estadoClase: (estado: EstadoAsistencia) => string
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

const cellInputClass = computed(() =>
  props.compact
    ? 'h-6 w-full rounded border border-slate-300 bg-white px-0.5 text-center text-[11px] tabular-nums text-slate-900 focus-visible:border-sky-600 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-sky-500/30 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500'
    : 'h-8 w-full rounded-md border border-slate-300 bg-white px-1.5 text-center text-sm tabular-nums text-slate-900 focus-visible:border-sky-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/30 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500',
)

const cellSelectClass = computed(() =>
  props.compact
    ? 'h-6 w-full rounded border px-0.5 text-[10px] font-medium text-slate-900 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-sky-500/30'
    : 'h-8 w-full rounded-md border px-1 text-xs font-medium text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/30',
)
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
    <Skeleton v-for="row in 8" :key="row" :class="compact ? 'h-12 w-full' : 'h-14 w-full'" />
  </div>

  <div
    v-else
    class="overflow-auto rounded-lg border border-palette-cyan/60 **:data-[slot=table-container]:overflow-visible"
    :class="compact ? 'min-h-0 flex-1' : 'max-h-[min(70vh,42rem)]'"
    tabindex="0"
    role="region"
    aria-label="Planilla de asistencia. Desplace para ver todos los días. La columna de persona permanece fija."
  >
    <Table class="min-w-max">
      <TableCaption class="sr-only">
        {{ tituloPeriodo }} · {{ planilla.diasLaborables.length }} días laborables.
        Cada celda tiene horas, código de tareo, minutos extra al 25 y 35 por ciento y minutos de tardanza.
        Falta y Descanso dejan las horas en cero.
      </TableCaption>
      <TableHeader>
        <TableRow class="hover:bg-transparent">
          <TableHead
            class="sticky top-0 left-0 z-30 bg-palette-ice shadow-[2px_0_0_0_#8cd2f5]"
            :class="compact ? 'min-w-36 px-1 py-0.75 text-xs' : 'min-w-52 px-1.5'"
          >
            Persona
          </TableHead>
          <TableHead
            class="sticky top-0 z-20 bg-palette-ice"
            :class="compact ? 'min-w-28 px-1 py-0.75 text-xs' : 'min-w-40 px-1.5'"
          >
            Jornada
          </TableHead>
          <TableHead
            v-for="fecha in planilla.diasLaborables"
            :key="fecha"
            class="sticky top-0 z-20 text-center"
            :class="[
              compact ? 'min-w-28 px-0.5 py-0.5' : 'min-w-40 px-1.5',
              fecha === hoyIso ? 'bg-sky-100' : 'bg-palette-ice',
            ]"
            :aria-current="fecha === hoyIso ? 'date' : undefined"
          >
            <span class="block font-semibold uppercase tracking-wide text-slate-600" :class="compact ? 'text-[9px]' : 'text-[10px]'">
              {{ diaCorto(fecha) }}
            </span>
            <span class="font-bold text-palette-navy" :class="compact ? 'text-xs' : 'text-sm'">{{ diaMes(fecha) }}</span>
            <span class="sr-only">{{ fechaLarga(fecha) }}{{ fecha === hoyIso ? ', hoy' : '' }}</span>
          </TableHead>
          <TableHead
            class="sticky top-0 z-20 bg-palette-ice text-right"
            :class="compact ? 'px-1 py-0.75 text-xs' : 'px-1.5'"
          >
            Total h
          </TableHead>
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
            class="sticky left-0 z-10 whitespace-normal bg-white text-left font-medium shadow-[2px_0_0_0_#8cd2f5]"
            :class="compact ? 'min-w-36 px-1 py-0.5 text-xs' : 'min-w-52 px-1.5 py-1.5'"
          >
            {{ fila.personaApellidos }}, {{ fila.personaNombres }}
            <span class="mt-0.5 block font-normal text-slate-600" :class="compact ? 'text-[10px]' : 'text-xs'">
              {{ fila.numeroDocumento }}
            </span>
          </TableHead>
          <TableCell :class="compact ? 'px-1 py-0.5 text-xs text-slate-700' : 'px-1.5 py-1.5 text-sm text-slate-700'">
            {{ fila.jornadaLabel || '8 horas diarias' }}
            <span class="mt-0.5 block text-slate-600" :class="compact ? 'text-[10px]' : 'text-xs'">
              {{ fila.horasEsperadasDia }} h esperadas
            </span>
          </TableCell>
          <TableCell
            v-for="fecha in planilla.diasLaborables"
            :key="`${fila.personaId}-${fecha}`"
            class="align-middle"
            :class="[
              compact ? 'px-0.5 py-0.5' : 'px-0.5 py-0.75',
              fila.porFecha[fecha] ? estadoClase(estadoDe(fila.porFecha[fecha])) : 'bg-white',
              fecha === hoyIso ? 'ring-1 ring-inset ring-sky-400' : '',
            ]"
          >
            <div
              v-if="fila.porFecha[fecha]"
              class="flex flex-col"
              :class="compact ? 'min-w-26 gap-0.5 p-0.5' : 'min-w-38 gap-0.5 p-0.5'"
            >
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
                :disabled="horasBloqueadas(codigoDe(fila.porFecha[fecha]))"
                :aria-describedby="horasBloqueadas(codigoDe(fila.porFecha[fecha])) ? 'asistencia-horas-bloqueadas' : undefined"
                :title="horasBloqueadas(codigoDe(fila.porFecha[fecha])) ? 'Las horas quedan en 0 para esta incidencia' : 'Horas trabajadas'"
                :value="horasDe(fila.porFecha[fecha])"
                @input="emit('horas', fila.porFecha[fecha], fila.horasEsperadasDia, ($event.target as HTMLInputElement).value)"
              />
              <label class="sr-only" :for="`codigo-${fila.porFecha[fecha].id}`">
                Código de tareo de {{ fila.personaApellidos }}, {{ fila.personaNombres }} el {{ fechaLarga(fecha) }}
              </label>
              <select
                :id="`codigo-${fila.porFecha[fecha].id}`"
                :class="[cellSelectClass, estadoSelectClase(estadoDe(fila.porFecha[fecha]))]"
                :value="codigoDe(fila.porFecha[fecha])"
                @change="emit('codigo', fila.porFecha[fecha], fila.horasEsperadasDia, ($event.target as HTMLSelectElement).value as CodigoTareo)"
              >
                <option v-for="item in CODIGOS_TAREO" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
              <div class="grid grid-cols-3 gap-0.5" aria-label="Minutos adicionales del día">
                <label class="min-w-0 text-center font-semibold leading-3 text-slate-600" :class="compact ? 'text-[8px]' : 'text-[9px]'">
                  <span class="block whitespace-nowrap">HE 25%</span>
                  <input
                    :id="`extras-25-${fila.porFecha[fecha].id}`"
                    :class="cellInputClass"
                    type="number"
                    min="0"
                    max="1440"
                    step="1"
                    inputmode="numeric"
                    title="Minutos de horas extra con sobretasa del 25%"
                    :value="minutosDe(fila.porFecha[fecha], 'minutosExtras25')"
                    @input="emit('minutos', fila.porFecha[fecha], 'minutosExtras25', ($event.target as HTMLInputElement).value)"
                  />
                </label>
                <label class="min-w-0 text-center font-semibold leading-3 text-slate-600" :class="compact ? 'text-[8px]' : 'text-[9px]'">
                  <span class="block whitespace-nowrap">HE 35%</span>
                  <input
                    :id="`extras-35-${fila.porFecha[fecha].id}`"
                    :class="cellInputClass"
                    type="number"
                    min="0"
                    max="1440"
                    step="1"
                    inputmode="numeric"
                    title="Minutos de horas extra con sobretasa del 35%"
                    :value="minutosDe(fila.porFecha[fecha], 'minutosExtras35')"
                    @input="emit('minutos', fila.porFecha[fecha], 'minutosExtras35', ($event.target as HTMLInputElement).value)"
                  />
                </label>
                <label class="min-w-0 text-center font-semibold leading-3 text-slate-600" :class="compact ? 'text-[8px]' : 'text-[9px]'">
                  <span class="block whitespace-nowrap">Tardanza</span>
                  <input
                    :id="`tardanza-${fila.porFecha[fecha].id}`"
                    :class="cellInputClass"
                    type="number"
                    min="0"
                    max="1440"
                    step="1"
                    inputmode="numeric"
                    title="Minutos de tardanza"
                    :value="minutosDe(fila.porFecha[fecha], 'minutosTardanza')"
                    @input="emit('minutos', fila.porFecha[fecha], 'minutosTardanza', ($event.target as HTMLInputElement).value)"
                  />
                </label>
              </div>
            </div>
            <span
              v-else
              class="block text-center font-medium text-slate-600"
              :class="compact ? 'px-0.5 py-1 text-[10px]' : 'px-0.5 py-1.5 text-xs'"
              :title="`Sin registro el ${fechaLarga(fecha)}. El contrato puede no estar vigente ese día.`"
            >
              —
              <span class="sr-only">Sin registro el {{ fechaLarga(fecha) }}</span>
            </span>
          </TableCell>
          <TableCell
            class="text-right font-semibold tabular-nums text-palette-navy"
            :class="compact ? 'px-1 py-0.5 text-xs' : 'px-1.5 py-1.5 text-sm'"
          >
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
</template>
