<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
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
import {
  abrirDocumentoBoletaCc,
  abrirPdfBoletaCc,
  cerrarPeriodoCc,
  liquidarPeriodoCc,
  listarBoletasCc,
  listarPeriodosCc,
  obtenerBoletaCc,
  obtenerProyecto,
} from '@/services/construccion/construccionService'
import type { BoletaCc, BoletaCcResumen, PeriodoCc, Proyecto } from '@/types/construccion'

const selectClass =
  'mt-1.5 h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const route = useRoute()
const router = useRouter()
const proyectoId = computed(() => Number(route.params.id))
const proyecto = ref<Proyecto | null>(null)
const periodos = ref<PeriodoCc[]>([])
const periodoId = ref<number | null>(null)
const boletas = ref<BoletaCcResumen[]>([])
const detalle = ref<BoletaCc | null>(null)
const loading = ref(true)
const liquidando = ref(false)
const cerrando = ref(false)

const periodoActual = computed(() => periodos.value.find((item) => item.id === periodoId.value) ?? null)
const cerrado = computed(() => periodoActual.value?.estado === 'CERRADO')

async function load() {
  loading.value = true
  try {
    proyecto.value = await obtenerProyecto(proyectoId.value)
    periodos.value = await listarPeriodosCc(proyectoId.value)
    if (!periodoId.value && periodos.value.length) {
      periodoId.value = periodos.value[0]?.id ?? null
    }
    await loadBoletas()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la planilla.')
  } finally {
    loading.value = false
  }
}

async function loadBoletas() {
  detalle.value = null
  if (!periodoId.value) {
    boletas.value = []
    return
  }
  boletas.value = await listarBoletasCc(proyectoId.value, periodoId.value)
}

watch(periodoId, async (id, prev) => {
  if (id && id !== prev && !loading.value) {
    try {
      await loadBoletas()
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las boletas.')
    }
  }
})

onMounted(() => {
  void load()
})

async function onLiquidar() {
  if (!periodoId.value) {
    return
  }
  liquidando.value = true
  try {
    boletas.value = await liquidarPeriodoCc(proyectoId.value, periodoId.value)
    periodos.value = await listarPeriodosCc(proyectoId.value)
    toast.success('Planilla liquidada.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo liquidar la planilla.')
  } finally {
    liquidando.value = false
  }
}

async function onCerrar() {
  if (!periodoId.value) {
    return
  }
  cerrando.value = true
  try {
    await cerrarPeriodoCc(proyectoId.value, periodoId.value)
    periodos.value = await listarPeriodosCc(proyectoId.value)
    toast.success('Periodo cerrado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cerrar el periodo.')
  } finally {
    cerrando.value = false
  }
}

async function onVer(boleta: BoletaCcResumen) {
  if (!periodoId.value) {
    return
  }
  try {
    detalle.value = await obtenerBoletaCc(proyectoId.value, periodoId.value, boleta.id)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo abrir la boleta.')
  }
}

async function onHtml(boleta: BoletaCcResumen) {
  if (!periodoId.value) {
    return
  }
  try {
    await abrirDocumentoBoletaCc(proyectoId.value, periodoId.value, boleta.id)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo abrir el documento.')
  }
}

async function onPdf(boleta: BoletaCcResumen) {
  if (!periodoId.value) {
    return
  }
  try {
    await abrirPdfBoletaCc(proyectoId.value, periodoId.value, boleta.id)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo descargar el PDF.')
  }
}

function conceptosDe(categoria: BoletaCc['conceptos'][number]['categoria']) {
  return (detalle.value?.conceptos ?? []).filter((item) => item.categoria === categoria)
}
</script>

<template>
  <section>
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Proyectos</p>
    <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
      Planilla RCC {{ proyecto ? `· ${proyecto.codigo}` : '' }}
    </h2>
    <p class="mt-3 max-w-3xl text-sm leading-relaxed text-slate-500">
      Liquide la semana a partir del tareo y emita la boleta de construcción civil.
    </p>

    <div class="mt-5 flex flex-wrap gap-2">
      <Button variant="outline" type="button" @click="router.push({ name: 'proyectos' })">Obras</Button>
      <Button
        variant="outline"
        type="button"
        @click="router.push({ name: 'proyecto-tareo', params: { id: String(proyectoId) } })"
      >
        Tareo
      </Button>
    </div>

    <div class="mt-6 space-y-5 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div class="w-full max-w-md">
          <label class="text-sm font-medium text-slate-700" for="planilla-periodo">Periodo</label>
          <select id="planilla-periodo" v-model.number="periodoId" :class="selectClass">
            <option :value="null">Sin periodos</option>
            <option v-for="item in periodos" :key="item.id" :value="item.id">
              Sem. {{ item.nroSemana }} · {{ item.fechaDesde }} → {{ item.fechaHasta }}
              ({{ item.estado === 'CERRADO' ? 'Cerrado' : 'Borrador' }})
            </option>
          </select>
        </div>
        <div class="flex flex-wrap gap-2">
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="liquidando || cerrado || !periodoId"
            @click="onLiquidar"
          >
            Liquidar semana
          </Button>
          <Button
            variant="outline"
            type="button"
            :disabled="cerrando || cerrado || !boletas.length"
            @click="onCerrar"
          >
            Cerrar periodo
          </Button>
        </div>
      </div>

      <div v-if="loading">
        <Skeleton class="h-24 w-full" />
      </div>
      <Table v-else>
        <TableHeader>
          <TableRow>
            <TableHead>Trabajador</TableHead>
            <TableHead>Documento</TableHead>
            <TableHead>Categoría</TableHead>
            <TableHead class="text-right">Haberes</TableHead>
            <TableHead class="text-right">Descuentos</TableHead>
            <TableHead class="text-right">Neto</TableHead>
            <TableHead class="text-right">Boleta</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableEmpty v-if="!boletas.length" :colspan="7">
            Aún no hay boletas. Guarde el tareo y liquide la semana.
          </TableEmpty>
          <TableRow v-for="boleta in boletas" :key="boleta.id">
            <TableCell class="font-medium">{{ boleta.personaNombreCompleto }}</TableCell>
            <TableCell>{{ boleta.numeroDocumento }}</TableCell>
            <TableCell>{{ boleta.categoria || '—' }}</TableCell>
            <TableCell class="text-right">{{ money.format(boleta.totalHaberes) }}</TableCell>
            <TableCell class="text-right">{{ money.format(boleta.totalDescuentos) }}</TableCell>
            <TableCell class="text-right font-medium">{{ money.format(boleta.neto) }}</TableCell>
            <TableCell class="text-right">
              <div class="flex justify-end gap-1">
                <Button variant="outline" class="h-8 px-2 text-xs" type="button" @click="onVer(boleta)">
                  Ver
                </Button>
                <Button variant="outline" class="h-8 px-2 text-xs" type="button" @click="onHtml(boleta)">
                  HTML
                </Button>
                <Button variant="outline" class="h-8 px-2 text-xs" type="button" @click="onPdf(boleta)">
                  PDF
                </Button>
              </div>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <div v-if="detalle" class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="flex items-start justify-between gap-3">
        <div>
          <h3 class="text-base font-semibold text-palette-navy">
            {{ detalle.personaApellidos }}, {{ detalle.personaNombres }}
          </h3>
          <p class="text-sm text-slate-500">{{ detalle.numeroDocumento }} · Neto {{ money.format(detalle.neto) }}</p>
        </div>
        <Button variant="outline" type="button" @click="detalle = null">Cerrar</Button>
      </div>
      <div class="mt-4 grid gap-4 md:grid-cols-3">
        <div>
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Haberes</p>
          <ul class="mt-2 space-y-1 text-sm">
            <li v-for="item in conceptosDe('HABER')" :key="item.codigo" class="flex justify-between gap-3">
              <span>{{ item.label }}</span>
              <span>{{ money.format(item.monto) }}</span>
            </li>
          </ul>
        </div>
        <div>
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Descuentos</p>
          <ul class="mt-2 space-y-1 text-sm">
            <li v-for="item in conceptosDe('DESCUENTO')" :key="item.codigo" class="flex justify-between gap-3">
              <span>{{ item.label }}</span>
              <span>{{ money.format(item.monto) }}</span>
            </li>
          </ul>
        </div>
        <div>
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Aportes</p>
          <ul class="mt-2 space-y-1 text-sm">
            <li v-for="item in conceptosDe('APORTE_EMPLEADOR')" :key="item.codigo" class="flex justify-between gap-3">
              <span>{{ item.label }}</span>
              <span>{{ money.format(item.monto) }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>
