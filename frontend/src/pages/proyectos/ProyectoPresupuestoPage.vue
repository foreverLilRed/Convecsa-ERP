<script setup lang="ts">
import { PlusIcon } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
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
import {
  crearVersionPresupuesto,
  descargarPlantillaPresupuesto,
  guardarPartidasPresupuesto,
  importarPresupuestoExcel,
  obtenerComparativoPresupuesto,
  obtenerPresupuesto,
  vigentarPresupuesto,
} from '@/services/construccion/construccionService'
import { fieldInputClass, fieldSelectClass } from './fieldClasses'
import type {
  ComparativoPresupuesto,
  PartidaPresupuesto,
  Presupuesto,
  TipoRecursoApu,
  TipoVersionPresupuesto,
} from '@/types/construccion'

type ApuDraft = {
  tipo: TipoRecursoApu
  codigo: string
  descripcion: string
  unidad: string
  cantidad: string
  precioUnitario: string
}

type PartidaDraft = {
  codigo: string
  capitulo: string
  descripcion: string
  unidad: string
  metrado: string
  precioUnitario: string
  centroCosto: string
  apu: ApuDraft[]
}

const TIPOS_APU: { valor: TipoRecursoApu; label: string }[] = [
  { valor: 'MANO_OBRA', label: 'Mano de obra' },
  { valor: 'MATERIAL', label: 'Material' },
  { valor: 'EQUIPO', label: 'Equipo' },
  { valor: 'SUBCONTRATO', label: 'Subcontrato' },
  { valor: 'GASTOS_GENERALES', label: 'Gastos generales' },
]

const inputClass = fieldInputClass
const selectClass = fieldSelectClass

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const loading = ref(true)
const saving = ref(false)
const acting = ref(false)
const presupuesto = ref<Presupuesto | null>(null)
const comparativo = ref<ComparativoPresupuesto | null>(null)
const versionId = ref<number | null>(null)
const tipoNueva = ref<TipoVersionPresupuesto>('ACTUALIZADO')
const partidas = ref<PartidaDraft[]>([])
const partidaActiva = ref(0)
const excelInput = ref<HTMLInputElement | null>(null)

const version = computed(() => presupuesto.value?.version ?? null)
const esBorrador = computed(() => version.value?.estado === 'BORRADOR')
const hayVigente = computed(() =>
  (presupuesto.value?.versiones ?? []).some((item) => item.estado === 'VIGENTE'),
)

const partidaSeleccionada = computed(() => partidas.value[partidaActiva.value] ?? null)

function tipoLabel(tipo: string): string {
  if (tipo === 'ACTUALIZADO') return 'Actualizado'
  if (tipo === 'ADICIONAL') return 'Adicional'
  return 'Inicial'
}

function estadoLabel(estado: string): string {
  if (estado === 'VIGENTE') return 'Vigente'
  if (estado === 'HISTORICO') return 'Histórico'
  return 'Borrador'
}

function emptyApu(): ApuDraft {
  return { tipo: 'MANO_OBRA', codigo: '', descripcion: '', unidad: '', cantidad: '', precioUnitario: '' }
}

function emptyPartida(): PartidaDraft {
  return {
    codigo: '',
    capitulo: '',
    descripcion: '',
    unidad: '',
    metrado: '',
    precioUnitario: '',
    centroCosto: '',
    apu: [],
  }
}

function toDraft(partida: PartidaPresupuesto): PartidaDraft {
  return {
    codigo: partida.codigo,
    capitulo: partida.capitulo ?? '',
    descripcion: partida.descripcion,
    unidad: partida.unidad ?? '',
    metrado: partida.metrado == null ? '' : String(partida.metrado),
    precioUnitario: partida.precioUnitario == null ? '' : String(partida.precioUnitario),
    centroCosto: partida.centroCosto ?? '',
    apu: (partida.apu ?? []).map((item) => ({
      tipo: item.tipo,
      codigo: item.codigo ?? '',
      descripcion: item.descripcion,
      unidad: item.unidad ?? '',
      cantidad: item.cantidad == null ? '' : String(item.cantidad),
      precioUnitario: item.precioUnitario == null ? '' : String(item.precioUnitario),
    })),
  }
}

function parseNumero(texto: string): number | null {
  const limpio = texto.trim().replace(',', '.')
  if (!limpio) {
    return null
  }
  const valor = Number(limpio)
  return Number.isFinite(valor) ? valor : Number.NaN
}

function parcialApu(recurso: ApuDraft): number {
  return (parseNumero(recurso.cantidad) || 0) * (parseNumero(recurso.precioUnitario) || 0)
}

function puPartida(partida: PartidaDraft): number {
  if (partida.apu.length > 0) {
    return partida.apu.reduce((suma, item) => suma + parcialApu(item), 0)
  }
  return parseNumero(partida.precioUnitario) || 0
}

function totalPartida(partida: PartidaDraft): number {
  return (parseNumero(partida.metrado) || 0) * puPartida(partida)
}

function aplicarPresupuesto(data: Presupuesto) {
  presupuesto.value = data
  versionId.value = data.version?.id ?? null
  partidas.value = (data.version?.partidas ?? []).map(toDraft)
  if (partidaActiva.value >= partidas.value.length) {
    partidaActiva.value = 0
  }
}

async function load(preferId?: number) {
  loading.value = true
  try {
    const [data, comp] = await Promise.all([
      obtenerPresupuesto(proyectoId.value, preferId),
      obtenerComparativoPresupuesto(proyectoId.value),
    ])
    aplicarPresupuesto(data)
    comparativo.value = comp
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el presupuesto.')
  } finally {
    loading.value = false
  }
}

function onVersionChange() {
  if (versionId.value != null) {
    void load(versionId.value)
  }
}

onMounted(() => {
  void load()
})

async function onCrearVersion() {
  acting.value = true
  try {
    const tipo = hayVigente.value ? tipoNueva.value : undefined
    const data = await crearVersionPresupuesto(proyectoId.value, tipo)
    aplicarPresupuesto(data)
    comparativo.value = await obtenerComparativoPresupuesto(proyectoId.value)
    toast.success('Versión de presupuesto creada.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo crear la versión.')
  } finally {
    acting.value = false
  }
}

async function onVigentar() {
  if (!version.value) {
    return
  }
  acting.value = true
  try {
    const data = await vigentarPresupuesto(proyectoId.value, version.value.id)
    aplicarPresupuesto(data)
    comparativo.value = await obtenerComparativoPresupuesto(proyectoId.value)
    toast.success('Presupuesto marcado como vigente.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo vigentar el presupuesto.')
  } finally {
    acting.value = false
  }
}

async function onPlantilla() {
  try {
    await descargarPlantillaPresupuesto(proyectoId.value, version.value?.id)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo descargar la plantilla.')
  }
}

async function onImportar(event: Event) {
  const input = event.target as HTMLInputElement
  const archivo = input.files?.[0]
  input.value = ''
  if (!archivo || !version.value) {
    return
  }
  if (!archivo.name.toLowerCase().endsWith('.xlsx')) {
    toast.error('El archivo debe ser .xlsx')
    return
  }
  acting.value = true
  try {
    const data = await importarPresupuestoExcel(proyectoId.value, version.value.id, archivo)
    aplicarPresupuesto(data)
    comparativo.value = await obtenerComparativoPresupuesto(proyectoId.value)
    toast.success('Presupuesto importado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo importar el Excel.')
  } finally {
    acting.value = false
  }
}

async function onGuardar() {
  if (!version.value) {
    return
  }
  const payload = partidas.value.map((partida) => {
    const metrado = parseNumero(partida.metrado)
    const precio = parseNumero(partida.precioUnitario)
    return {
      codigo: partida.codigo.trim(),
      capitulo: partida.capitulo.trim() || null,
      descripcion: partida.descripcion.trim(),
      unidad: partida.unidad.trim() || null,
      metrado: metrado,
      precioUnitario: precio,
      centroCosto: partida.centroCosto.trim() || null,
      apu: partida.apu.map((item) => ({
        tipo: item.tipo,
        codigo: item.codigo.trim() || null,
        descripcion: item.descripcion.trim(),
        unidad: item.unidad.trim() || null,
        cantidad: parseNumero(item.cantidad),
        precioUnitario: parseNumero(item.precioUnitario),
      })),
    }
  })
  if (payload.some((item) => !item.codigo || !item.descripcion)) {
    toast.error('Cada partida requiere código y descripción.')
    return
  }
  if (payload.some((item) => Number.isNaN(item.metrado) || Number.isNaN(item.precioUnitario))) {
    toast.error('Metrado y precio unitario deben ser números válidos.')
    return
  }
  if (payload.some((item) => item.apu.some((apu) => !apu.descripcion || Number.isNaN(apu.cantidad) || Number.isNaN(apu.precioUnitario)))) {
    toast.error('Cada recurso APU requiere descripción y montos válidos.')
    return
  }
  saving.value = true
  try {
    const data = await guardarPartidasPresupuesto(proyectoId.value, version.value.id, payload)
    aplicarPresupuesto(data)
    toast.success('Partidas guardadas.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron guardar las partidas.')
  } finally {
    saving.value = false
  }
}

function agregarPartida() {
  partidas.value.push(emptyPartida())
  partidaActiva.value = partidas.value.length - 1
}

function quitarPartida(index: number) {
  partidas.value.splice(index, 1)
  if (partidaActiva.value >= partidas.value.length) {
    partidaActiva.value = Math.max(0, partidas.value.length - 1)
  }
}

function agregarApu() {
  partidaSeleccionada.value?.apu.push(emptyApu())
}

function quitarApu(index: number) {
  partidaSeleccionada.value?.apu.splice(index, 1)
}
</script>

<template>
  <section class="space-y-5">
    <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div class="flex flex-wrap items-end gap-3">
          <div v-if="presupuesto?.versiones.length">
            <label class="text-[10px] font-medium text-slate-600" for="presupuesto-version">Versión</label>
            <select
              id="presupuesto-version"
              v-model.number="versionId"
              :class="selectClass"
              class="mt-0.5 min-w-48"
              @change="onVersionChange"
            >
              <option v-for="item in presupuesto?.versiones" :key="item.id" :value="item.id">
                {{ item.numero }} · {{ tipoLabel(item.tipo) }} · {{ estadoLabel(item.estado) }}
              </option>
            </select>
          </div>
          <p v-if="version" class="pb-1 text-sm text-slate-500">
            {{ tipoLabel(version.tipo) }} · {{ estadoLabel(version.estado) }}
          </p>
        </div>
        <div class="flex flex-wrap items-end gap-2">
          <select
            v-if="hayVigente"
            v-model="tipoNueva"
            :class="selectClass"
            class="min-w-36"
            :disabled="acting || loading"
          >
            <option value="ACTUALIZADO">Actualizado</option>
            <option value="ADICIONAL">Adicional</option>
          </select>
          <Button
            variant="outline"
            type="button"
            :disabled="acting || loading"
            @click="onCrearVersion"
          >
            {{ presupuesto?.versiones.length ? 'Nueva versión' : 'Crear presupuesto' }}
          </Button>
          <Button
            v-if="esBorrador"
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="acting || !partidas.length"
            @click="onVigentar"
          >
            Marcar vigente
          </Button>
        </div>
      </div>
    </div>

    <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <h3 class="text-base font-semibold text-palette-navy">Comparativo meta vs real</h3>
      <p class="mt-1 text-sm text-slate-500">
        Mano de obra: meta desde APU vigente, real desde boletas RCC. Materiales: meta desde APU vigente, real desde vales de salida.
        Equipos: meta desde APU vigente, real desde imputaciones. Subcontratos: meta desde APU vigente, real desde avances.
        La meta se agrupa por centro de costo. El real de mano de obra también se desglosa por la cuadrilla actual del obrero.
      </p>
      <div v-if="comparativo" class="mt-4 grid gap-3 sm:grid-cols-3">
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Meta MO</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.metaMo) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Real MO</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.realMo) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Desvío MO</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">
            {{ money.format(comparativo.desvio) }}
            <span class="ml-1 text-sm font-normal text-slate-500">({{ comparativo.desvioPct }}%)</span>
          </p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Meta materiales</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.metaMaterial) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Real materiales</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.realMaterial) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Desvío materiales</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">
            {{ money.format(comparativo.desvioMaterial) }}
            <span class="ml-1 text-sm font-normal text-slate-500">({{ comparativo.desvioPctMaterial }}%)</span>
          </p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Meta equipos</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.metaEquipo) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Real equipos</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.realEquipo) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Desvío equipos</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">
            {{ money.format(comparativo.desvioEquipo) }}
            <span class="ml-1 text-sm font-normal text-slate-500">({{ comparativo.desvioPctEquipo }}%)</span>
          </p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Meta subcontratos</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.metaSubcontrato) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Real subcontratos</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">{{ money.format(comparativo.realSubcontrato) }}</p>
        </article>
        <article class="rounded-lg border border-slate-200 px-4 py-3">
          <p class="text-xs font-medium uppercase tracking-wide text-slate-500">Desvío subcontratos</p>
          <p class="mt-1 text-lg font-semibold text-palette-navy">
            {{ money.format(comparativo.desvioSubcontrato) }}
            <span class="ml-1 text-sm font-normal text-slate-500">({{ comparativo.desvioPctSubcontrato }}%)</span>
          </p>
        </article>
      </div>
      <Table v-if="comparativo?.centros.length" class="mt-4">
        <TableHeader>
          <TableRow>
            <TableHead>Centro de costo</TableHead>
            <TableHead>Meta MO</TableHead>
            <TableHead>Real MO</TableHead>
            <TableHead>Desvío MO</TableHead>
            <TableHead>Meta mat.</TableHead>
            <TableHead>Real mat.</TableHead>
            <TableHead>Desvío mat.</TableHead>
            <TableHead>Meta eq.</TableHead>
            <TableHead>Real eq.</TableHead>
            <TableHead>Desvío eq.</TableHead>
            <TableHead>Meta sub.</TableHead>
            <TableHead>Real sub.</TableHead>
            <TableHead>Desvío sub.</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="fila in comparativo.centros" :key="fila.centroCosto">
            <TableCell>{{ fila.centroCosto }}</TableCell>
            <TableCell>{{ money.format(fila.metaMo) }}</TableCell>
            <TableCell>{{ money.format(fila.realMo) }}</TableCell>
            <TableCell>{{ money.format(fila.desvio) }}</TableCell>
            <TableCell>{{ money.format(fila.metaMaterial) }}</TableCell>
            <TableCell>{{ money.format(fila.realMaterial) }}</TableCell>
            <TableCell>{{ money.format(fila.desvioMaterial) }}</TableCell>
            <TableCell>{{ money.format(fila.metaEquipo) }}</TableCell>
            <TableCell>{{ money.format(fila.realEquipo) }}</TableCell>
            <TableCell>{{ money.format(fila.desvioEquipo) }}</TableCell>
            <TableCell>{{ money.format(fila.metaSubcontrato) }}</TableCell>
            <TableCell>{{ money.format(fila.realSubcontrato) }}</TableCell>
            <TableCell>{{ money.format(fila.desvioSubcontrato) }}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
      <h4 v-if="comparativo?.cuadrillas?.length" class="mt-6 text-sm font-semibold text-palette-navy">Por cuadrilla</h4>
      <Table v-if="comparativo?.cuadrillas?.length" class="mt-2">
        <TableHeader>
          <TableRow>
            <TableHead>Cuadrilla</TableHead>
            <TableHead>Centro</TableHead>
            <TableHead>Miembros</TableHead>
            <TableHead>Real MO</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="fila in comparativo.cuadrillas" :key="fila.cuadrillaId ?? 'sin'">
            <TableCell>{{ fila.codigo ? `${fila.codigo} — ${fila.nombre}` : fila.nombre }}</TableCell>
            <TableCell>{{ fila.centroCosto || '—' }}</TableCell>
            <TableCell>{{ fila.miembros }}</TableCell>
            <TableCell>{{ money.format(fila.realMo) }}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h3 class="text-base font-semibold text-palette-navy">Partidas</h3>
        <div class="flex flex-wrap gap-2">
          <Button v-if="esBorrador" variant="outline" type="button" :disabled="acting || saving" @click="onPlantilla">
            Descargar plantilla
          </Button>
          <Button
            v-if="esBorrador"
            variant="outline"
            type="button"
            :disabled="acting || saving"
            @click="excelInput?.click()"
          >
            Importar Excel
          </Button>
          <input
            ref="excelInput"
            class="hidden"
            type="file"
            accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            @change="onImportar"
          >
          <Button v-if="esBorrador" variant="outline" type="button" :disabled="saving" @click="agregarPartida">
            <PlusIcon data-icon="inline-start" />
            Agregar partida
          </Button>
          <Button
            v-if="esBorrador"
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="saving || !partidas.length"
            @click="onGuardar"
          >
            Guardar
          </Button>
        </div>
      </div>

      <div v-if="loading" class="space-y-3">
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-24 w-full" />
      </div>
      <Table v-else>
        <TableHeader>
          <TableRow>
            <TableHead>Código</TableHead>
            <TableHead>Capítulo</TableHead>
            <TableHead>Descripción</TableHead>
            <TableHead>Unidad</TableHead>
            <TableHead>Metrado</TableHead>
            <TableHead>P.U.</TableHead>
            <TableHead>Total</TableHead>
            <TableHead>Centro de costo</TableHead>
            <TableHead v-if="esBorrador" class="text-right">Acciones</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableEmpty v-if="partidas.length === 0" :colspan="esBorrador ? 9 : 8">
            {{ version ? 'No hay partidas en esta versión.' : 'Cree un presupuesto para cargar partidas.' }}
          </TableEmpty>
          <TableRow
            v-for="(partida, index) in partidas"
            v-else
            :key="index"
            class="cursor-pointer"
            :class="index === partidaActiva ? 'bg-slate-50' : ''"
            @click="partidaActiva = index"
          >
            <TableCell>
              <Input v-if="esBorrador" v-model="partida.codigo" :class="inputClass" @click.stop />
              <span v-else>{{ partida.codigo }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="partida.capitulo" :class="inputClass" @click.stop />
              <span v-else>{{ partida.capitulo || '—' }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="partida.descripcion" :class="inputClass" @click.stop />
              <span v-else>{{ partida.descripcion }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="partida.unidad" :class="inputClass" class="w-20" @click.stop />
              <span v-else>{{ partida.unidad || '—' }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="partida.metrado" inputmode="decimal" :class="inputClass" class="w-24" @click.stop />
              <span v-else>{{ partida.metrado || '0' }}</span>
            </TableCell>
            <TableCell>
              <Input
                v-if="esBorrador && partida.apu.length === 0"
                v-model="partida.precioUnitario"
                inputmode="decimal"
                :class="inputClass"
                class="w-24"
                @click.stop
              />
              <span v-else>{{ money.format(puPartida(partida)) }}</span>
            </TableCell>
            <TableCell>{{ money.format(totalPartida(partida)) }}</TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="partida.centroCosto" :class="inputClass" class="w-28" @click.stop />
              <span v-else>{{ partida.centroCosto || '—' }}</span>
            </TableCell>
            <TableCell v-if="esBorrador" class="text-right">
              <Button variant="outline" class="h-8 px-3 text-xs" type="button" @click.stop="quitarPartida(index)">
                Quitar
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <div v-if="partidaSeleccionada" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="mb-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 class="text-base font-semibold text-palette-navy">APU</h3>
          <p class="mt-1 text-sm text-slate-500">
            Recursos de la partida {{ partidaSeleccionada.codigo || 'seleccionada' }}. Si hay APU, el P.U. se calcula
            como suma de parciales.
          </p>
        </div>
        <Button v-if="esBorrador" variant="outline" type="button" @click="agregarApu">
          <PlusIcon data-icon="inline-start" />
          Agregar recurso
        </Button>
      </div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Tipo</TableHead>
            <TableHead>Código</TableHead>
            <TableHead>Descripción</TableHead>
            <TableHead>Unidad</TableHead>
            <TableHead>Cantidad</TableHead>
            <TableHead>P.U.</TableHead>
            <TableHead>Parcial</TableHead>
            <TableHead v-if="esBorrador" class="text-right">Acciones</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableEmpty v-if="partidaSeleccionada.apu.length === 0" :colspan="esBorrador ? 8 : 7">
            Sin recursos APU. El precio unitario se teclea en la partida.
          </TableEmpty>
          <TableRow v-for="(recurso, index) in partidaSeleccionada.apu" v-else :key="index">
            <TableCell>
              <select v-if="esBorrador" v-model="recurso.tipo" :class="selectClass" class="min-w-[10rem]">
                <option v-for="item in TIPOS_APU" :key="item.valor" :value="item.valor">{{ item.label }}</option>
              </select>
              <span v-else>{{ TIPOS_APU.find((item) => item.valor === recurso.tipo)?.label }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="recurso.codigo" :class="inputClass" />
              <span v-else>{{ recurso.codigo || '—' }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="recurso.descripcion" :class="inputClass" />
              <span v-else>{{ recurso.descripcion }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="recurso.unidad" :class="inputClass" class="w-20" />
              <span v-else>{{ recurso.unidad || '—' }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="recurso.cantidad" inputmode="decimal" :class="inputClass" class="w-24" />
              <span v-else>{{ recurso.cantidad || '0' }}</span>
            </TableCell>
            <TableCell>
              <Input v-if="esBorrador" v-model="recurso.precioUnitario" inputmode="decimal" :class="inputClass" class="w-24" />
              <span v-else>{{ money.format(Number(recurso.precioUnitario) || 0) }}</span>
            </TableCell>
            <TableCell>{{ money.format(parcialApu(recurso)) }}</TableCell>
            <TableCell v-if="esBorrador" class="text-right">
              <Button variant="outline" class="h-8 px-3 text-xs" type="button" @click="quitarApu(index)">
                Quitar
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </section>
</template>
