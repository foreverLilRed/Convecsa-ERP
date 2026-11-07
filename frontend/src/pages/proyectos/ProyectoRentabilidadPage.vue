<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { obtenerAsientosObra, obtenerRentabilidadObra } from '@/services/construccion/construccionService'
import type { AsientoObra, AsientosObra, RentabilidadObra } from '@/types/construccion'

type TabRent = 'margen' | 'asientos'

const inputClass =
  'h-8 rounded-lg border-slate-200 bg-white px-2 text-sm text-slate-900 placeholder-slate-400 focus-visible:border-sky-600 focus-visible:ring-sky-500/20'

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const MESES = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Setiembre',
  'Octubre',
  'Noviembre',
  'Diciembre',
]

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const tab = ref<TabRent>('margen')
const loading = ref(true)

const filtro = reactive({
  desde: '',
  hasta: '',
})

const rentabilidad = ref<RentabilidadObra | null>(null)
const asientos = ref<AsientosObra | null>(null)

function n(valor: number | string | null | undefined): number {
  const parsed = typeof valor === 'number' ? valor : Number(valor)
  return Number.isFinite(parsed) ? parsed : 0
}

function mesLabel(mes: number): string {
  return MESES[mes - 1] ?? String(mes)
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    const desde = filtro.desde || undefined
    const hasta = filtro.hasta || undefined
    const [margen, asiento] = await Promise.all([
      obtenerRentabilidadObra(proyectoId.value, desde, hasta),
      obtenerAsientosObra(proyectoId.value, desde, hasta),
    ])
    rentabilidad.value = margen
    asientos.value = asiento
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la rentabilidad.')
  } finally {
    loading.value = false
  }
}

function lineasAsiento(asiento: AsientoObra | null | undefined) {
  return asiento?.bloques.flatMap((bloque) =>
    bloque.lineas.map((linea) => ({
      ...linea,
      bloque: bloque.titulo,
    })),
  ) ?? []
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs :model-value="tab" @update:model-value="(value) => (tab = String(value) as TabRent)">
      <TabsList class="h-auto rounded-xl border border-slate-200 bg-slate-100/85 p-1.5">
        <TabsTrigger value="margen" class="h-9 rounded-lg px-4 text-sm">Margen</TabsTrigger>
        <TabsTrigger value="asientos" class="h-9 rounded-lg px-4 text-sm">Asientos</TabsTrigger>
      </TabsList>
    </Tabs>

    <div class="flex flex-wrap items-end gap-3">
      <div>
        <label class="text-xs text-slate-500" for="rent-desde">Desde</label>
        <Input id="rent-desde" v-model="filtro.desde" type="date" :class="inputClass" />
      </div>
      <div>
        <label class="text-xs text-slate-500" for="rent-hasta">Hasta</label>
        <Input id="rent-hasta" v-model="filtro.hasta" type="date" :class="inputClass" />
      </div>
      <Button variant="outline" type="button" :disabled="loading" @click="load">Consultar</Button>
    </div>

    <div v-if="loading" class="space-y-3">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <div v-else-if="tab === 'margen'" class="space-y-4">
      <div v-if="rentabilidad" class="grid gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Ingreso</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(rentabilidad.ingreso)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Mano de obra</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(rentabilidad.costoMo)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Materiales</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(rentabilidad.costoMateriales)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Factoring</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(rentabilidad.costoFactoring)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Margen bruto</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(rentabilidad.margenBruto)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Margen neto</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(rentabilidad.margenNeto)) }}</p>
        </div>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Periodo</TableHead>
              <TableHead>Ingreso</TableHead>
              <TableHead>MO</TableHead>
              <TableHead>Materiales</TableHead>
              <TableHead>Factoring</TableHead>
              <TableHead>Bruto</TableHead>
              <TableHead>Neto</TableHead>
              <TableHead>Bruto acum.</TableHead>
              <TableHead>Neto acum.</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="punto in rentabilidad?.puntos ?? []" :key="`${punto.anio}-${punto.mes}`">
              <TableCell>{{ mesLabel(punto.mes) }} {{ punto.anio }}</TableCell>
              <TableCell>{{ money.format(n(punto.ingreso)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.costoMo)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.costoMateriales)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.costoFactoring)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.margenBruto)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.margenNeto)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.margenBrutoAcumulado)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.margenNetoAcumulado)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!(rentabilidad?.puntos.length)" :colspan="9">Sin movimientos en el periodo.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-sm font-semibold text-palette-navy">Consumo de materiales</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Cuenta</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead>Auxiliar</TableHead>
              <TableHead>Debe</TableHead>
              <TableHead>Haber</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="(linea, idx) in lineasAsiento(asientos?.materiales)"
              :key="`mat-${linea.cuenta}-${idx}`"
            >
              <TableCell>{{ linea.cuenta }}</TableCell>
              <TableCell>{{ linea.descripcion }}</TableCell>
              <TableCell>{{ linea.auxiliar || '—' }}</TableCell>
              <TableCell>{{ money.format(n(linea.debe)) }}</TableCell>
              <TableCell>{{ money.format(n(linea.haber)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!lineasAsiento(asientos?.materiales).length" :colspan="5">
              Sin consumo de materiales en el periodo.
            </TableEmpty>
            <TableRow v-if="asientos?.materiales && lineasAsiento(asientos.materiales).length">
              <TableCell colspan="3" class="font-semibold text-palette-navy">Totales</TableCell>
              <TableCell class="font-semibold">{{ money.format(n(asientos.materiales.totalDebe)) }}</TableCell>
              <TableCell class="font-semibold">{{ money.format(n(asientos.materiales.totalHaber)) }}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-sm font-semibold text-palette-navy">Adelanto de factoring</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Cuenta</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead>Auxiliar</TableHead>
              <TableHead>Debe</TableHead>
              <TableHead>Haber</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="(linea, idx) in lineasAsiento(asientos?.factoring)"
              :key="`fact-${linea.cuenta}-${idx}`"
            >
              <TableCell>{{ linea.cuenta }}</TableCell>
              <TableCell>{{ linea.descripcion }}</TableCell>
              <TableCell>{{ linea.auxiliar || '—' }}</TableCell>
              <TableCell>{{ money.format(n(linea.debe)) }}</TableCell>
              <TableCell>{{ money.format(n(linea.haber)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!lineasAsiento(asientos?.factoring).length" :colspan="5">
              Sin adelantos de factoring en el periodo.
            </TableEmpty>
            <TableRow v-if="asientos?.factoring && lineasAsiento(asientos.factoring).length">
              <TableCell colspan="3" class="font-semibold text-palette-navy">Totales</TableCell>
              <TableCell class="font-semibold">{{ money.format(n(asientos.factoring.totalDebe)) }}</TableCell>
              <TableCell class="font-semibold">{{ money.format(n(asientos.factoring.totalHaber)) }}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </div>
  </div>
</template>
