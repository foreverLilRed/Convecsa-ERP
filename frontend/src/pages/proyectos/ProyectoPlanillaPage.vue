<script setup lang="ts">
import { PlusIcon } from '@lucide/vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
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
  descargarBoletasCcPdf,
  descargarDispersionCc,
  enviarBoletasCcCorreo,
  guardarExtrasBoletaCc,
  liquidarPeriodoCc,
  listarBoletasCc,
  listarPeriodosCc,
  obtenerBoletaCc,
  obtenerProyecto,
} from '@/services/construccion/construccionService'
import type {
  BancoDispersion,
  BoletaCc,
  BoletaCcResumen,
  CadenciaPeriodoCc,
  PeriodoCc,
  Proyecto,
} from '@/types/construccion'
import { fieldSelectClass } from './fieldClasses'

const selectClass = `mt-1.5 ${fieldSelectClass}`

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const proyecto = ref<Proyecto | null>(null)
const periodos = ref<PeriodoCc[]>([])
const periodoId = ref<number | null>(null)
const boletas = ref<BoletaCcResumen[]>([])
const detalle = ref<BoletaCc | null>(null)
const loading = ref(true)
const liquidando = ref(false)
const cerrando = ref(false)
const acting = ref(false)
const savingExtras = ref(false)
const selectedIds = ref<number[]>([])
let extraSeq = 0
const extras = ref<{ key: number; categoria: 'HABER' | 'DESCUENTO'; label: string; monto: string }[]>([])

const periodoActual = computed(() => periodos.value.find((item) => item.id === periodoId.value) ?? null)
const cerrado = computed(() => periodoActual.value?.estado === 'CERRADO')
const allSelected = computed(
  () => boletas.value.length > 0 && boletas.value.every((item) => selectedIds.value.includes(item.id)),
)

function cadenciaLabel(cadencia: CadenciaPeriodoCc | undefined): string {
  if (cadencia === 'QUINCENAL') return 'Quincena'
  if (cadencia === 'MENSUAL') return 'Mes'
  return 'Semana'
}

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
  extras.value = []
  selectedIds.value = []
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
    extras.value = (detalle.value.conceptos ?? [])
      .filter((item) => item.origen === 'MANUAL')
      .map((item) => ({
        key: ++extraSeq,
        categoria: item.categoria === 'DESCUENTO' ? 'DESCUENTO' : 'HABER',
        label: item.label,
        monto: String(item.monto),
      }))
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo abrir la boleta.')
  }
}

function toggleAll(checked: boolean) {
  selectedIds.value = checked ? boletas.value.map((item) => item.id) : []
}

function toggleBoleta(id: number, checked: boolean) {
  selectedIds.value = checked
    ? [...new Set([...selectedIds.value, id])]
    : selectedIds.value.filter((item) => item !== id)
}

function idsLote(): number[] {
  return selectedIds.value.length ? selectedIds.value : boletas.value.map((item) => item.id)
}

function agregarExtra() {
  extras.value = [...extras.value, { key: ++extraSeq, categoria: 'HABER', label: '', monto: '' }]
}

function quitarExtra(key: number) {
  extras.value = extras.value.filter((row) => row.key !== key)
}

async function onGuardarExtras() {
  if (!periodoId.value || !detalle.value) {
    return
  }
  const payload = []
  for (const row of extras.value) {
    const label = row.label.trim()
    const monto = Number(row.monto)
    if (!label && row.monto === '') {
      continue
    }
    if (!label || !Number.isFinite(monto) || monto < 0) {
      toast.error('Cada reintegro o descuento necesita nombre y monto válido.')
      return
    }
    payload.push({ categoria: row.categoria, label, monto })
  }
  savingExtras.value = true
  try {
    detalle.value = await guardarExtrasBoletaCc(proyectoId.value, periodoId.value, detalle.value.id, payload)
    boletas.value = await listarBoletasCc(proyectoId.value, periodoId.value)
    toast.success('Reintegros guardados. Sobreviven una reliquidación.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron guardar los reintegros.')
  } finally {
    savingExtras.value = false
  }
}

async function onLotePdf() {
  if (!periodoId.value || !boletas.value.length) {
    return
  }
  acting.value = true
  try {
    await descargarBoletasCcPdf(
      proyectoId.value,
      periodoId.value,
      idsLote(),
      `boletas-rcc-${periodoId.value}.pdf`,
    )
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo descargar el lote.')
  } finally {
    acting.value = false
  }
}

async function onLoteCorreo() {
  if (!periodoId.value || !boletas.value.length) {
    return
  }
  acting.value = true
  try {
    const result = await enviarBoletasCcCorreo(proyectoId.value, periodoId.value, idsLote())
    if (result.errores) {
      toast.warning(`${result.enviados} enviadas y ${result.errores} con error. ${result.detalles[0] ?? ''}`)
    } else {
      toast.success(`${result.enviados} boleta(s) enviadas por correo.`)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron enviar las boletas.')
  } finally {
    acting.value = false
  }
}

async function onDispersion(banco: BancoDispersion) {
  if (!periodoId.value) {
    return
  }
  acting.value = true
  try {
    await descargarDispersionCc(
      proyectoId.value,
      periodoId.value,
      banco,
      `dispersion-${proyecto.value?.codigo ?? 'rcc'}-${banco.toLowerCase()}.txt`,
    )
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo descargar la dispersión.')
  } finally {
    acting.value = false
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
    <div class="space-y-5 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div class="w-full max-w-md">
          <label class="text-sm font-medium text-slate-700" for="planilla-periodo">Periodo</label>
          <select id="planilla-periodo" v-model.number="periodoId" :class="selectClass">
            <option :value="null">Sin periodos</option>
            <option v-for="item in periodos" :key="item.id" :value="item.id">
              {{ cadenciaLabel(item.cadencia) }} · {{ item.fechaDesde }} → {{ item.fechaHasta }}
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
            Liquidar
          </Button>
          <Button
            variant="outline"
            type="button"
            :disabled="cerrando || cerrado || !boletas.length"
            @click="onCerrar"
          >
            Cerrar periodo
          </Button>
          <Button variant="outline" type="button" :disabled="acting || !boletas.length" @click="onLotePdf">
            PDF lote
          </Button>
          <Button variant="outline" type="button" :disabled="acting || !boletas.length" @click="onLoteCorreo">
            Enviar correo
          </Button>
          <Button variant="outline" type="button" :disabled="acting || !boletas.length" @click="onDispersion('CCI')">
            CCI
          </Button>
          <Button variant="outline" type="button" :disabled="acting || !boletas.length" @click="onDispersion('BCP')">
            BCP
          </Button>
          <Button variant="outline" type="button" :disabled="acting || !boletas.length" @click="onDispersion('INTERBANK')">
            Interbank
          </Button>
        </div>
      </div>

      <div v-if="loading">
        <Skeleton class="h-24 w-full" />
      </div>
      <Table v-else>
        <TableHeader>
          <TableRow>
            <TableHead class="w-10">
              <input
                type="checkbox"
                class="h-4 w-4 rounded border-slate-300"
                :checked="allSelected"
                :disabled="!boletas.length"
                @change="toggleAll(($event.target as HTMLInputElement).checked)"
              />
            </TableHead>
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
          <TableEmpty v-if="!boletas.length" :colspan="8">
            Aún no hay boletas. Guarde el tareo y liquide el periodo.
          </TableEmpty>
          <TableRow v-for="boleta in boletas" :key="boleta.id">
            <TableCell>
              <input
                type="checkbox"
                class="h-4 w-4 rounded border-slate-300"
                :checked="selectedIds.includes(boleta.id)"
                @change="toggleBoleta(boleta.id, ($event.target as HTMLInputElement).checked)"
              />
            </TableCell>
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
          <p class="text-sm text-slate-500">
            {{ detalle.numeroDocumento }} · Neto {{ money.format(detalle.neto) }}
            <span v-if="detalle.tarifaDia">
              · Tarifa día {{ money.format(detalle.tarifaDia) }} · Tarifa hora
              {{ money.format(detalle.tarifaHora) }}
            </span>
          </p>
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

      <div v-if="!cerrado" class="mt-6 border-t border-palette-cyan/60 pt-4">
        <div class="flex items-center justify-between">
          <h4 class="text-sm font-semibold text-palette-navy">Reintegros y descuentos</h4>
          <Button variant="outline" class="h-8 px-3 text-xs" type="button" @click="agregarExtra">
            <PlusIcon data-icon="inline-start" />
            Agregar
          </Button>
        </div>
        <p class="mt-1 text-xs text-slate-500">
          Los conceptos manuales se conservan al reliquidar. No entran a la base de AFP ni EsSalud.
        </p>
        <ul v-if="extras.length" class="mt-3 space-y-2">
          <li v-for="row in extras" :key="row.key" class="grid gap-2 sm:grid-cols-[8rem_1fr_7rem_auto]">
            <select v-model="row.categoria" :class="selectClass">
              <option value="HABER">Reintegro</option>
              <option value="DESCUENTO">Descuento</option>
            </select>
            <input v-model="row.label" :class="selectClass" placeholder="Nombre" />
            <input v-model="row.monto" type="number" min="0" step="0.01" :class="selectClass" placeholder="0.00" />
            <Button variant="ghost" class="h-10" type="button" @click="quitarExtra(row.key)">Quitar</Button>
          </li>
        </ul>
        <div class="mt-3 flex justify-end">
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="savingExtras"
            @click="onGuardarExtras"
          >
            Guardar extras
          </Button>
        </div>
      </div>
    </div>
  </section>
</template>
