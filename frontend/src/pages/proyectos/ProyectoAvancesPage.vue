<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import { Badge } from '@/components/ui/badge'
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
import {
  anularAdicionalObra,
  aprobarValorizacion,
  cerrarAvance,
  crearAdicionalObra,
  crearValorizacion,
  facturarValorizacion,
  guardarAvance,
  guardarCronograma,
  listarAdicionalesObra,
  listarCronograma,
  listarValorizaciones,
  observarValorizacion,
  obtenerAvance,
  obtenerCurvaS,
  obtenerValorizacion,
  presentarValorizacion,
  vigentarAdicionalObra,
} from '@/services/construccion/construccionService'
import type {
  AdicionalObra,
  AvanceObra,
  CronogramaPartida,
  CurvaS,
  EstadoAdicionalObra,
  EstadoAvanceObra,
  EstadoValorizacion,
  TipoAdicionalObra,
  TipoValorizacion,
  Valorizacion,
} from '@/types/construccion'

type TabAvances = 'avance' | 'valorizaciones' | 'curva'

const inputClass =
  'h-8 rounded-lg border-slate-200 bg-white px-2 text-sm text-slate-900 placeholder-slate-400 focus-visible:border-sky-600 focus-visible:ring-sky-500/20'

const selectClass =
  'h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-500/30'

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const qtyFmt = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 0, maximumFractionDigits: 4 })
const pctFmt = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
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
const tab = ref<TabAvances>('avance')
const loading = ref(true)
const saving = ref(false)
const errorCarga = ref('')

const hoy = new Date()
const anio = ref(String(hoy.getFullYear()))
const mes = ref(String(hoy.getMonth() + 1))
const tipoNueva = ref<TipoValorizacion>('REGULAR')

const avance = ref<AvanceObra | null>(null)
const cronograma = ref<CronogramaPartida[]>([])
const periodos = ref<Record<number, string>>({})
const fechasInicio = ref<Record<number, string>>({})
const fechasFin = ref<Record<number, string>>({})
const valorizaciones = ref<Valorizacion[]>([])
const seleccionada = ref<Valorizacion | null>(null)
const adicionales = ref<AdicionalObra[]>([])
const curva = ref<CurvaS | null>(null)

const adicionalForm = reactive({
  tipo: 'ADICIONAL' as TipoAdicionalObra,
  codigo: '',
  descripcion: '',
  unidad: '',
  metrado: '',
  precioUnitario: '',
})

const avanceCerrado = computed(() => avance.value?.estado === 'CERRADO')
const valEditable = computed(
  () => seleccionada.value?.estado === 'BORRADOR' || seleccionada.value?.estado === 'OBSERVADA',
)

function n(valor: number | string | null | undefined): number {
  const parsed = Number(valor)
  return Number.isFinite(parsed) ? parsed : 0
}

function mesLabel(valor: number): string {
  return MESES[valor - 1] ?? String(valor)
}

function estadoAvanceLabel(estado: EstadoAvanceObra): string {
  return estado === 'CERRADO' ? 'Cerrado' : 'Borrador'
}

function estadoValLabel(estado: EstadoValorizacion): string {
  if (estado === 'PRESENTADA') return 'Presentada'
  if (estado === 'OBSERVADA') return 'Observada'
  if (estado === 'APROBADA') return 'Aprobada'
  return 'Borrador'
}

function tipoValLabel(tipo: TipoValorizacion): string {
  if (tipo === 'ADICIONAL') return 'Adicional'
  if (tipo === 'DEDUCTIVO') return 'Deductivo'
  return 'Regular'
}

function estadoAdicionalLabel(estado: EstadoAdicionalObra): string {
  if (estado === 'VIGENTE') return 'Vigente'
  if (estado === 'ANULADO') return 'Anulado'
  return 'Borrador'
}

function aplicarAvance(dto: AvanceObra) {
  avance.value = dto
  const mapa: Record<number, string> = {}
  for (const item of dto.items) {
    mapa[item.partidaId] = String(item.metradoPeriodo ?? 0)
  }
  periodos.value = mapa
}

function aplicarCronograma(filas: CronogramaPartida[]) {
  cronograma.value = filas
  const inicio: Record<number, string> = {}
  const fin: Record<number, string> = {}
  for (const fila of filas) {
    inicio[fila.partidaId] = fila.fechaInicio ?? ''
    fin[fila.partidaId] = fila.fechaFin ?? ''
  }
  fechasInicio.value = inicio
  fechasFin.value = fin
}

async function loadAvance() {
  const dto = await obtenerAvance(proyectoId.value, Number(anio.value), Number(mes.value))
  aplicarAvance(dto)
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  errorCarga.value = ''
  try {
    const [av, crono, vals, ads, curvaDto] = await Promise.all([
      obtenerAvance(proyectoId.value, Number(anio.value), Number(mes.value)),
      listarCronograma(proyectoId.value),
      listarValorizaciones(proyectoId.value),
      listarAdicionalesObra(proyectoId.value),
      obtenerCurvaS(proyectoId.value),
    ])
    aplicarAvance(av)
    aplicarCronograma(crono)
    valorizaciones.value = vals
    adicionales.value = ads
    curva.value = curvaDto
    if (seleccionada.value) {
      const actual = vals.find((item) => item.id === seleccionada.value?.id) ?? null
      seleccionada.value = actual
    }
  } catch (err) {
    errorCarga.value = err instanceof Error ? err.message : 'No se pudo cargar avances.'
    toast.error(errorCarga.value)
  } finally {
    loading.value = false
  }
}

async function onGuardarAvance() {
  saving.value = true
  try {
    const items = (avance.value?.items ?? []).map((item) => ({
      partidaId: item.partidaId,
      metradoPeriodo: n(periodos.value[item.partidaId]),
    }))
    const dto = await guardarAvance(proyectoId.value, Number(anio.value), Number(mes.value), items)
    aplicarAvance(dto)
    toast.success('Avance guardado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el avance.')
  } finally {
    saving.value = false
  }
}

async function onCerrarAvance() {
  if (!avance.value?.id) {
    await onGuardarAvance()
  }
  const id = avance.value?.id
  if (!id) {
    toast.error('Guarde el avance antes de cerrar el mes.')
    return
  }
  saving.value = true
  try {
    const dto = await cerrarAvance(proyectoId.value, id)
    aplicarAvance(dto)
    toast.success('Mes de avance cerrado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cerrar el avance.')
  } finally {
    saving.value = false
  }
}

async function onGuardarCronograma() {
  saving.value = true
  try {
    const filas = cronograma.value.map((item) => ({
      partidaId: item.partidaId,
      fechaInicio: fechasInicio.value[item.partidaId] || null,
      fechaFin: fechasFin.value[item.partidaId] || null,
    }))
    aplicarCronograma(await guardarCronograma(proyectoId.value, filas))
    curva.value = await obtenerCurvaS(proyectoId.value)
    toast.success('Cronograma guardado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el cronograma.')
  } finally {
    saving.value = false
  }
}

async function onGenerarValorizacion() {
  saving.value = true
  try {
    const dto = await crearValorizacion(
      proyectoId.value,
      tipoNueva.value,
      Number(anio.value),
      Number(mes.value),
    )
    seleccionada.value = dto
    valorizaciones.value = await listarValorizaciones(proyectoId.value)
    toast.success(`Valorización ${tipoValLabel(dto.tipo)} N.º ${dto.numero} generada.`)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo generar la valorización.')
  } finally {
    saving.value = false
  }
}

async function onSeleccionarVal(id: number) {
  saving.value = true
  try {
    seleccionada.value = await obtenerValorizacion(proyectoId.value, id)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo abrir la valorización.')
  } finally {
    saving.value = false
  }
}

async function refrescarValorizacion(accion: () => Promise<Valorizacion>, ok: string) {
  if (!seleccionada.value) {
    return
  }
  saving.value = true
  try {
    seleccionada.value = await accion()
    valorizaciones.value = await listarValorizaciones(proyectoId.value)
    curva.value = await obtenerCurvaS(proyectoId.value)
    toast.success(ok)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo actualizar la valorización.')
  } finally {
    saving.value = false
  }
}

function onPresentar() {
  if (!seleccionada.value) return
  void refrescarValorizacion(
    () => presentarValorizacion(proyectoId.value, seleccionada.value!.id),
    'Valorización presentada.',
  )
}

function onObservar() {
  if (!seleccionada.value) return
  void refrescarValorizacion(
    () => observarValorizacion(proyectoId.value, seleccionada.value!.id),
    'Valorización observada.',
  )
}

function onAprobar() {
  if (!seleccionada.value) return
  void refrescarValorizacion(
    () => aprobarValorizacion(proyectoId.value, seleccionada.value!.id),
    'Valorización aprobada.',
  )
}

function onFacturar() {
  if (!seleccionada.value) return
  void refrescarValorizacion(
    () => facturarValorizacion(proyectoId.value, seleccionada.value!.id),
    'Factura interna emitida.',
  )
}

async function onCrearAdicional() {
  if (!adicionalForm.codigo.trim() || !adicionalForm.descripcion.trim()) {
    toast.error('Ingrese código y descripción del adicional.')
    return
  }
  const metrado = n(adicionalForm.metrado)
  const precio = n(adicionalForm.precioUnitario)
  if (metrado < 0 || precio < 0) {
    toast.error('Metrado y precio deben ser mayores o iguales a cero.')
    return
  }
  saving.value = true
  try {
    await crearAdicionalObra(proyectoId.value, {
      tipo: adicionalForm.tipo,
      codigo: adicionalForm.codigo.trim(),
      descripcion: adicionalForm.descripcion.trim(),
      unidad: adicionalForm.unidad.trim() || null,
      metrado,
      precioUnitario: precio,
    })
    adicionalForm.codigo = ''
    adicionalForm.descripcion = ''
    adicionalForm.unidad = ''
    adicionalForm.metrado = ''
    adicionalForm.precioUnitario = ''
    adicionales.value = await listarAdicionalesObra(proyectoId.value)
    toast.success('Adicional registrado en borrador.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo crear el adicional.')
  } finally {
    saving.value = false
  }
}

async function onVigentarAdicional(id: number) {
  saving.value = true
  try {
    await vigentarAdicionalObra(proyectoId.value, id)
    adicionales.value = await listarAdicionalesObra(proyectoId.value)
    toast.success('Adicional vigente.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo vigentar el adicional.')
  } finally {
    saving.value = false
  }
}

async function onAnularAdicional(id: number) {
  saving.value = true
  try {
    await anularAdicionalObra(proyectoId.value, id)
    adicionales.value = await listarAdicionalesObra(proyectoId.value)
    toast.success('Adicional anulado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo anular el adicional.')
  } finally {
    saving.value = false
  }
}

watch([anio, mes], () => {
  if (!loading.value && Number.isFinite(proyectoId.value)) {
    void loadAvance().catch((err) => {
      toast.error(err instanceof Error ? err.message : 'No se pudo cargar el avance del mes.')
    })
  }
})

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs :model-value="tab" @update:model-value="(value) => (tab = String(value) as TabAvances)">
      <TabsList class="h-auto rounded-xl border border-slate-200 bg-slate-100/85 p-1.5">
        <TabsTrigger value="avance" class="h-9 rounded-lg px-4 text-sm">Avance</TabsTrigger>
        <TabsTrigger value="valorizaciones" class="h-9 rounded-lg px-4 text-sm">Valorizaciones</TabsTrigger>
        <TabsTrigger value="curva" class="h-9 rounded-lg px-4 text-sm">Curva S</TabsTrigger>
      </TabsList>
    </Tabs>

    <div v-if="loading" class="space-y-3">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <p v-else-if="errorCarga" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
      {{ errorCarga }}
    </p>

    <div v-else-if="tab === 'avance'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <div class="mb-4 flex flex-wrap items-end gap-3">
          <div>
            <label class="text-sm font-medium text-slate-700" for="avance-anio">Año</label>
            <Input id="avance-anio" v-model="anio" inputmode="numeric" :class="inputClass" class="mt-1.5 w-28" />
          </div>
          <div>
            <label class="text-sm font-medium text-slate-700" for="avance-mes">Mes</label>
            <select id="avance-mes" v-model="mes" :class="selectClass" class="mt-1.5 w-40">
              <option v-for="(nombre, index) in MESES" :key="nombre" :value="String(index + 1)">{{ nombre }}</option>
            </select>
          </div>
          <Badge v-if="avance" variant="secondary">{{ estadoAvanceLabel(avance.estado) }}</Badge>
          <div class="ml-auto flex flex-wrap gap-2">
            <Button
              v-if="!avanceCerrado"
              class="bg-palette-navy text-white hover:bg-palette-royal"
              type="button"
              :disabled="saving"
              @click="onGuardarAvance"
            >
              Guardar avance
            </Button>
            <Button v-if="!avanceCerrado" type="button" :disabled="saving" @click="onCerrarAvance">
              Cerrar mes
            </Button>
            <Button variant="outline" type="button" :disabled="saving" @click="onGuardarCronograma">
              Guardar cronograma
            </Button>
          </div>
        </div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Partida</TableHead>
              <TableHead>Unidad</TableHead>
              <TableHead>Meta</TableHead>
              <TableHead>Anterior</TableHead>
              <TableHead>Periodo</TableHead>
              <TableHead>Acumulado</TableHead>
              <TableHead>Desvío</TableHead>
              <TableHead>Inicio</TableHead>
              <TableHead>Fin</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in avance?.items ?? []" :key="item.partidaId">
              <TableCell>{{ item.codigo }}</TableCell>
              <TableCell>{{ item.descripcion }}</TableCell>
              <TableCell>{{ item.unidad }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metradoMeta)) }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metradoAnterior)) }}</TableCell>
              <TableCell>
                <Input
                  v-model="periodos[item.partidaId]"
                  :class="inputClass"
                  inputmode="decimal"
                  :disabled="avanceCerrado"
                />
              </TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metradoAnterior) + n(periodos[item.partidaId])) }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metradoAnterior) + n(periodos[item.partidaId]) - n(item.metradoMeta)) }}</TableCell>
              <TableCell>
                <Input v-model="fechasInicio[item.partidaId]" type="date" :class="inputClass" />
              </TableCell>
              <TableCell>
                <Input v-model="fechasFin[item.partidaId]" type="date" :class="inputClass" />
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!(avance?.items.length)" :colspan="10">
              No hay partidas en el presupuesto vigente.
            </TableEmpty>
          </TableBody>
        </Table>
        <p v-if="cronograma[0]" class="mt-3 text-xs text-slate-500">
          Desvío de plazo (partida {{ cronograma[0].codigo }}): {{ pctFmt.format(n(cronograma[0].desvioPlazoPct)) }} %.
        </p>
      </div>
    </div>

    <div v-else-if="tab === 'valorizaciones'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <div class="mb-4 flex flex-wrap items-end gap-3">
          <div>
            <label class="text-sm font-medium text-slate-700" for="val-tipo">Tipo</label>
            <select id="val-tipo" v-model="tipoNueva" :class="selectClass" class="mt-1.5 w-40">
              <option value="REGULAR">Regular</option>
              <option value="ADICIONAL">Adicional</option>
              <option value="DEDUCTIVO">Deductivo</option>
            </select>
          </div>
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="saving"
            @click="onGenerarValorizacion"
          >
            Generar
          </Button>
        </div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>N.º</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Periodo</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead>Bruto</TableHead>
              <TableHead>Retención</TableHead>
              <TableHead>Neto</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in valorizaciones" :key="item.id">
              <TableCell>{{ item.numero }}</TableCell>
              <TableCell>{{ tipoValLabel(item.tipo) }}</TableCell>
              <TableCell>{{ mesLabel(item.mes) }} {{ item.anio }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ estadoValLabel(item.estado) }}</Badge>
              </TableCell>
              <TableCell>{{ money.format(n(item.montoBruto)) }}</TableCell>
              <TableCell>{{ money.format(n(item.montoRetencion)) }}</TableCell>
              <TableCell>{{ money.format(n(item.montoNeto)) }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" :disabled="saving" @click="onSeleccionarVal(item.id)">
                  Abrir
                </Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!valorizaciones.length" :colspan="8">Aún no hay valorizaciones.</TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div v-if="seleccionada" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h3 class="text-base font-semibold text-palette-navy">
              Valorización {{ tipoValLabel(seleccionada.tipo) }} N.º {{ seleccionada.numero }}
            </h3>
            <p class="mt-1 text-sm text-slate-500">
              {{ mesLabel(seleccionada.mes) }} {{ seleccionada.anio }} · retención {{ pctFmt.format(n(seleccionada.porcentajeRetencion)) }} %
            </p>
          </div>
          <div class="flex flex-wrap gap-2">
            <Button
              v-if="valEditable"
              type="button"
              :disabled="saving"
              @click="onPresentar"
            >
              Presentar
            </Button>
            <Button
              v-if="seleccionada.estado === 'PRESENTADA'"
              variant="outline"
              type="button"
              :disabled="saving"
              @click="onObservar"
            >
              Observar
            </Button>
            <Button
              v-if="seleccionada.estado === 'PRESENTADA'"
              class="bg-palette-navy text-white hover:bg-palette-royal"
              type="button"
              :disabled="saving"
              @click="onAprobar"
            >
              Aprobar
            </Button>
            <Button
              v-if="seleccionada.estado === 'APROBADA' && !seleccionada.factura"
              type="button"
              :disabled="saving"
              @click="onFacturar"
            >
              Facturar
            </Button>
          </div>
        </div>
        <div class="mb-4 grid gap-3 sm:grid-cols-3">
          <div class="rounded-lg border border-slate-200 px-3 py-2 text-sm">
            <p class="text-slate-500">Bruto</p>
            <p class="font-semibold text-palette-navy">{{ money.format(n(seleccionada.montoBruto)) }}</p>
          </div>
          <div class="rounded-lg border border-slate-200 px-3 py-2 text-sm">
            <p class="text-slate-500">Retención</p>
            <p class="font-semibold text-palette-navy">{{ money.format(n(seleccionada.montoRetencion)) }}</p>
          </div>
          <div class="rounded-lg border border-slate-200 px-3 py-2 text-sm">
            <p class="text-slate-500">Neto</p>
            <p class="font-semibold text-palette-navy">{{ money.format(n(seleccionada.montoNeto)) }}</p>
          </div>
        </div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead>Anterior</TableHead>
              <TableHead>Periodo</TableHead>
              <TableHead>Acumulado</TableHead>
              <TableHead>P.U.</TableHead>
              <TableHead>Total periodo</TableHead>
              <TableHead>Total acum.</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="(item, index) in seleccionada.items" :key="item.id ?? index">
              <TableCell>{{ item.codigo }}</TableCell>
              <TableCell>{{ item.descripcion }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metradoAnterior)) }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metradoPeriodo)) }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metradoAcumulado)) }}</TableCell>
              <TableCell>{{ money.format(n(item.precioUnitario)) }}</TableCell>
              <TableCell>{{ money.format(n(item.totalPeriodo)) }}</TableCell>
              <TableCell>{{ money.format(n(item.totalAcumulado)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!seleccionada.items.length" :colspan="8">Sin ítems.</TableEmpty>
          </TableBody>
        </Table>
        <div
          v-if="seleccionada.factura"
          class="mt-4 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm"
        >
          <p class="font-semibold text-palette-navy">Factura interna N.º {{ seleccionada.factura.numero }}</p>
          <p class="mt-1 text-slate-600">
            {{ seleccionada.factura.fecha }} · {{ seleccionada.factura.clienteRazon || 'Sin cliente' }}
            <span v-if="seleccionada.factura.clienteRuc"> · RUC {{ seleccionada.factura.clienteRuc }}</span>
          </p>
          <p class="mt-1">
            Subtotal {{ money.format(n(seleccionada.factura.subtotal)) }} · IGV
            {{ money.format(n(seleccionada.factura.igv)) }} · Total
            {{ money.format(n(seleccionada.factura.total)) }}
          </p>
        </div>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Adicionales y deductivos</h3>
        <div class="mt-4 grid gap-3 sm:grid-cols-6">
          <select v-model="adicionalForm.tipo" :class="selectClass">
            <option value="ADICIONAL">Adicional</option>
            <option value="DEDUCTIVO">Deductivo</option>
          </select>
          <Input v-model="adicionalForm.codigo" :class="inputClass" placeholder="Código" maxlength="40" />
          <Input
            v-model="adicionalForm.descripcion"
            class="sm:col-span-2"
            :class="inputClass"
            placeholder="Descripción"
            maxlength="240"
          />
          <Input v-model="adicionalForm.unidad" :class="inputClass" placeholder="Unidad" maxlength="20" />
          <Input v-model="adicionalForm.metrado" :class="inputClass" placeholder="Metrado" inputmode="decimal" />
          <Input
            v-model="adicionalForm.precioUnitario"
            :class="inputClass"
            placeholder="P.U."
            inputmode="decimal"
          />
        </div>
        <Button class="mt-3" variant="outline" type="button" :disabled="saving" @click="onCrearAdicional">
          Registrar borrador
        </Button>
        <Table class="mt-4">
          <TableHeader>
            <TableRow>
              <TableHead>Tipo</TableHead>
              <TableHead>Código</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead>Metrado</TableHead>
              <TableHead>P.U.</TableHead>
              <TableHead>Total</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in adicionales" :key="item.id">
              <TableCell>{{ item.tipo === 'DEDUCTIVO' ? 'Deductivo' : 'Adicional' }}</TableCell>
              <TableCell>{{ item.codigo }}</TableCell>
              <TableCell>{{ item.descripcion }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.metrado)) }}</TableCell>
              <TableCell>{{ money.format(n(item.precioUnitario)) }}</TableCell>
              <TableCell>{{ money.format(n(item.total)) }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ estadoAdicionalLabel(item.estado) }}</Badge>
              </TableCell>
              <TableCell class="space-x-2">
                <Button
                  v-if="item.estado === 'BORRADOR'"
                  variant="outline"
                  size="sm"
                  type="button"
                  :disabled="saving"
                  @click="onVigentarAdicional(item.id)"
                >
                  Vigentar
                </Button>
                <Button
                  v-if="item.estado !== 'ANULADO'"
                  variant="outline"
                  size="sm"
                  type="button"
                  :disabled="saving"
                  @click="onAnularAdicional(item.id)"
                >
                  Anular
                </Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!adicionales.length" :colspan="8">Sin adicionales ni deductivos.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div v-if="curva" class="mb-4 grid gap-3 sm:grid-cols-3">
        <div class="rounded-lg border border-slate-200 px-3 py-2 text-sm">
          <p class="text-slate-500">Metrado meta</p>
          <p class="font-semibold text-palette-navy">{{ qtyFmt.format(n(curva.metradoMeta)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 px-3 py-2 text-sm">
          <p class="text-slate-500">Monto meta</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(curva.montoMeta)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 px-3 py-2 text-sm">
          <p class="text-slate-500">Desvío de plazo</p>
          <p class="font-semibold text-palette-navy">{{ pctFmt.format(n(curva.desvioPlazoPct)) }} %</p>
        </div>
      </div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Periodo</TableHead>
            <TableHead>Prog. físico</TableHead>
            <TableHead>Ejec. físico</TableHead>
            <TableHead>Desvío físico</TableHead>
            <TableHead>Prog. económico</TableHead>
            <TableHead>Ejec. económico</TableHead>
            <TableHead>Desvío económico</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="punto in curva?.puntos ?? []" :key="`${punto.anio}-${punto.mes}`">
            <TableCell>{{ mesLabel(punto.mes) }} {{ punto.anio }}</TableCell>
            <TableCell>{{ qtyFmt.format(n(punto.programadoFisico)) }}</TableCell>
            <TableCell>{{ qtyFmt.format(n(punto.ejecutadoFisico)) }}</TableCell>
            <TableCell>{{ qtyFmt.format(n(punto.desvioFisico)) }}</TableCell>
            <TableCell>{{ money.format(n(punto.programadoEconomico)) }}</TableCell>
            <TableCell>{{ money.format(n(punto.ejecutadoEconomico)) }}</TableCell>
            <TableCell>{{ money.format(n(punto.desvioEconomico)) }}</TableCell>
          </TableRow>
          <TableEmpty v-if="!(curva?.puntos.length)" :colspan="7">Sin puntos de curva S.</TableEmpty>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
