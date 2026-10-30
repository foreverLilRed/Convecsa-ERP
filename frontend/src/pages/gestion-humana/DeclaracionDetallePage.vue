<script setup lang="ts">
import { DownloadIcon } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
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
  asignarRxh,
  cerrarDeclaracion,
  consolidarRcc,
  descargarAfpnet,
  descargarAsientoPdf,
  descargarDispersion,
  descargarPlame,
  importarRxh,
  obtenerDeclaracion,
} from '@/services/nomina/declaracionService'
import type { ConsolidadoCc, DeclaracionDetalle, ReciboHonorario } from '@/types/declaracion'
import { periodoLabel } from '@/types/nomina'

const props = defineProps<{
  id: string
}>()

const inputClass =
  'block w-28 rounded-lg border border-slate-200 px-2 py-1.5 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const router = useRouter()
const loading = ref(true)
const detalle = ref<DeclaracionDetalle | null>(null)
const selected = ref<ConsolidadoCc | null>(null)
const working = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const dniAsignar = ref<Record<number, string>>({})

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const numericId = computed(() => Number(props.id))
const declaracion = computed(() => detalle.value?.declaracion ?? null)
const borrador = computed(() => declaracion.value?.estado === 'BORRADOR')

function estadoLabel(estado: string): string {
  return estado === 'CERRADO' ? 'Cerrado' : 'Borrador'
}

function periodoArchivo(): string {
  const item = declaracion.value
  if (!item) {
    return ''
  }
  return `${item.anio}${String(item.mes).padStart(2, '0')}`
}

async function load() {
  loading.value = true
  try {
    detalle.value = await obtenerDeclaracion(numericId.value)
    const actual = selected.value
    selected.value = actual
      ? (detalle.value.consolidado.find((row) => row.id === actual.id) ?? null)
      : null
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la declaración.')
  } finally {
    loading.value = false
  }
}

async function run(key: string, action: () => Promise<void>, ok: string) {
  working.value = key
  try {
    await action()
    toast.success(ok)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo completar la acción.')
  } finally {
    working.value = ''
  }
}

async function onConsolidar() {
  await run('consolidar', async () => {
    detalle.value = await consolidarRcc(numericId.value)
    selected.value = null
  }, 'Consolidado RCC actualizado.')
}

async function onCerrar() {
  await run('cerrar', async () => {
    const resumen = await cerrarDeclaracion(numericId.value)
    if (detalle.value) {
      detalle.value = { ...detalle.value, declaracion: resumen }
    }
  }, 'Declaración cerrada.')
}

async function onPlame() {
  await run('plame', () => descargarPlame(numericId.value, `${periodoArchivo()}.zip`), 'ZIP PLAME descargado.')
}

async function onAfpnet() {
  await run(
    'afpnet',
    () => descargarAfpnet(numericId.value, `afpnet-${periodoArchivo()}.csv`),
    'AFPnet descargado.',
  )
}

async function onAsiento() {
  await run(
    'asiento',
    () => descargarAsientoPdf(numericId.value, `asiento-rcc-${periodoArchivo()}.pdf`),
    'Asiento descargado.',
  )
}

async function onDispersion(banco: 'CCI' | 'BCP' | 'INTERBANK') {
  await run(
    `dispersion-${banco}`,
    () => descargarDispersion(numericId.value, `dispersion-${periodoArchivo()}-${banco.toLowerCase()}.txt`, banco),
    `Dispersión ${banco} descargada.`,
  )
}

function onElegirRxh() {
  fileInput.value?.click()
}

async function onArchivoRxh(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  await run('rxh', async () => {
    const resumen = await importarRxh(numericId.value, file)
    if (resumen.huerfanos > 0) {
      toast.warning(`${resumen.importados} recibos importados, ${resumen.huerfanos} sin persona.`)
    }
    await load()
  }, 'Recibos por honorarios importados.')
}

async function onAsignar(recibo: ReciboHonorario) {
  const dni = (dniAsignar.value[recibo.id] ?? '').trim()
  if (!dni) {
    toast.error('Ingrese el DNI de la persona.')
    return
  }
  await run(`rxh-${recibo.id}`, async () => {
    await asignarRxh(numericId.value, recibo.id, dni)
    await load()
  }, 'Recibo asignado.')
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <button
          class="text-sm font-medium text-palette-royal hover:underline"
          type="button"
          @click="router.push({ name: 'gh-declaraciones' })"
        >
          ← Declaraciones
        </button>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          {{ declaracion ? periodoLabel(declaracion.anio, declaracion.mes) : 'Declaración mensual' }}
        </h2>
        <p v-if="declaracion" class="mt-2 text-sm text-slate-500">
          {{ estadoLabel(declaracion.estado) }}
          · {{ declaracion.cantidadAdmin }} boletas admin
          · {{ declaracion.cantidadObreros }} obreros
          · {{ declaracion.cantidadRxh }} RxH
          <span v-if="declaracion.cantidadRxhSinMatch > 0">
            ({{ declaracion.cantidadRxhSinMatch }} sin persona)
          </span>
        </p>
      </div>
    </div>

    <div v-if="loading && !detalle" class="mt-6 space-y-3 rounded-lg border border-palette-cyan/70 bg-white p-5">
      <Skeleton class="h-9 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <template v-else-if="detalle && declaracion">
      <div class="mt-6 flex flex-wrap gap-2 rounded-lg border border-palette-cyan/70 bg-white px-4 py-4">
        <Button
          v-if="borrador"
          class="h-9 bg-palette-navy px-3 text-white hover:bg-palette-royal"
          type="button"
          :disabled="working === 'consolidar'"
          @click="onConsolidar"
        >
          {{ working === 'consolidar' ? 'Consolidando…' : 'Consolidar RCC' }}
        </Button>
        <Button variant="outline" class="h-9 px-3" type="button" :disabled="!!working" @click="onPlame">
          <DownloadIcon data-icon="inline-start" />
          PLAME ZIP
        </Button>
        <Button variant="outline" class="h-9 px-3" type="button" :disabled="!!working" @click="onAfpnet">
          AFPnet
        </Button>
        <Button variant="outline" class="h-9 px-3" type="button" :disabled="!!working" @click="onAsiento">
          Asiento PDF
        </Button>
        <Button variant="outline" class="h-9 px-3" type="button" :disabled="!!working" @click="onDispersion('CCI')">
          Dispersión CCI
        </Button>
        <Button variant="outline" class="h-9 px-3" type="button" :disabled="!!working" @click="onDispersion('BCP')">
          BCP
        </Button>
        <Button variant="outline" class="h-9 px-3" type="button" :disabled="!!working" @click="onDispersion('INTERBANK')">
          Interbank
        </Button>
        <Button
          v-if="borrador"
          variant="outline"
          class="h-9 px-3"
          type="button"
          :disabled="!!working"
          @click="onElegirRxh"
        >
          Importar RxH
        </Button>
        <Button
          v-if="borrador"
          class="h-9 bg-palette-navy px-3 text-white hover:bg-palette-royal"
          type="button"
          :disabled="working === 'cerrar'"
          @click="onCerrar"
        >
          {{ working === 'cerrar' ? 'Cerrando…' : 'Cerrar declaración' }}
        </Button>
        <input
          ref="fileInput"
          class="sr-only"
          type="file"
          accept=".txt,text/plain"
          @change="onArchivoRxh"
        />
      </div>

      <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Consolidado RCC</h3>
        <p class="mt-1 text-sm text-slate-500">
          Un trabajador = un DNI. Neto RCC: {{ money.format(declaracion.totalNetoRcc) }}
        </p>
        <div class="mt-4 overflow-hidden rounded-lg border border-palette-cyan/60">
          <Table>
            <TableCaption class="sr-only">Obreros consolidados del mes.</TableCaption>
            <TableHeader>
              <TableRow>
                <TableHead class="px-3">DNI</TableHead>
                <TableHead class="px-3">Trabajador</TableHead>
                <TableHead class="px-3">Obra</TableHead>
                <TableHead class="px-3">Horas</TableHead>
                <TableHead class="px-3">Neto</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableEmpty v-if="!detalle.consolidado.length" :colspan="5">
                <span class="text-slate-500">Aún no hay consolidado. Cierre las semanas RCC del mes y consolide.</span>
              </TableEmpty>
              <TableRow
                v-for="fila in detalle.consolidado"
                :key="fila.id"
                class="cursor-pointer"
                :class="selected?.id === fila.id ? 'bg-palette-cyan/10' : ''"
                @click="selected = fila"
              >
                <TableCell class="px-3 py-3 font-medium">{{ fila.numeroDocumento }}</TableCell>
                <TableCell class="px-3 py-3">{{ fila.personaApellidos }} {{ fila.personaNombres }}</TableCell>
                <TableCell class="px-3 py-3">{{ fila.proyectoCodigo ?? '—' }}</TableCell>
                <TableCell class="px-3 py-3">{{ fila.horasOrd }}</TableCell>
                <TableCell class="px-3 py-3 whitespace-nowrap">{{ money.format(fila.neto) }}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>

        <div v-if="selected" class="mt-4 overflow-hidden rounded-lg border border-palette-cyan/60">
          <p class="border-b border-palette-cyan/60 px-3 py-2 text-sm font-medium text-palette-navy">
            Conceptos de {{ selected.personaApellidos }} {{ selected.personaNombres }}
          </p>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead class="px-3">Concepto</TableHead>
                <TableHead class="px-3">Categoría</TableHead>
                <TableHead class="px-3">Monto</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="concepto in selected.conceptos" :key="concepto.id">
                <TableCell class="px-3 py-2">{{ concepto.label }}</TableCell>
                <TableCell class="px-3 py-2">{{ concepto.categoria }}</TableCell>
                <TableCell class="px-3 py-2 whitespace-nowrap">{{ money.format(concepto.monto) }}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>

      <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Recibos por honorarios</h3>
        <p class="mt-1 text-sm text-slate-500">
          Solo se declara lo importado de SUNAT. Las filas sin persona se asignan por DNI.
        </p>
        <div class="mt-4 overflow-hidden rounded-lg border border-palette-cyan/60">
          <Table>
            <TableCaption class="sr-only">Recibos por honorarios del mes.</TableCaption>
            <TableHeader>
              <TableRow>
                <TableHead class="px-3">Documento</TableHead>
                <TableHead class="px-3">Emisor</TableHead>
                <TableHead class="px-3">Comprobante</TableHead>
                <TableHead class="px-3">Monto</TableHead>
                <TableHead class="px-3">Persona</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableEmpty v-if="!detalle.recibos.length" :colspan="5">
                <span class="text-slate-500">No hay RxH importados en este mes.</span>
              </TableEmpty>
              <TableRow v-for="recibo in detalle.recibos" :key="recibo.id">
                <TableCell class="px-3 py-3">{{ recibo.numeroDocumento }}</TableCell>
                <TableCell class="px-3 py-3">
                  {{ [recibo.apellidoPaterno, recibo.apellidoMaterno, recibo.nombres].filter(Boolean).join(' ') || '—' }}
                </TableCell>
                <TableCell class="px-3 py-3">{{ recibo.serie }}-{{ recibo.numero }}</TableCell>
                <TableCell class="px-3 py-3 whitespace-nowrap">{{ money.format(recibo.monto) }}</TableCell>
                <TableCell class="px-3 py-3">
                  <div v-if="recibo.huerfano && borrador" class="flex flex-wrap items-center gap-2">
                    <input
                      v-model="dniAsignar[recibo.id]"
                      :class="inputClass"
                      placeholder="DNI"
                      :aria-label="`DNI para ${recibo.numeroDocumento}`"
                    />
                    <Button
                      size="sm"
                      class="h-8 bg-palette-navy px-3 text-white hover:bg-palette-royal"
                      type="button"
                      :disabled="working === `rxh-${recibo.id}`"
                      @click="onAsignar(recibo)"
                    >
                      Asignar
                    </Button>
                  </div>
                  <span v-else>{{ recibo.personaNombres ?? 'Sin match' }}</span>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>
    </template>
  </section>
</template>
