<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
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
import { Textarea } from '@/components/ui/textarea'
import {
  actualizarVencimientoFactura,
  adelantarFactoring,
  cederFacturaFactoring,
  cobrarFactoring,
  impagarFactoring,
  liberarRetencionObra,
  listarFacturasObra,
  listarFactoring,
  obtenerFlujoCaja,
  obtenerRetencionesObra,
  registrarCobranzaObra,
  revertirFactoring,
} from '@/services/construccion/construccionService'
import type {
  EstadoFacturaObra,
  EstadoFactoring,
  FacturaObra,
  FlujoCaja,
  OperacionFactoring,
  RetencionesObra,
  TipoLiberacionRetencion,
} from '@/types/construccion'

type TabFin = 'facturas' | 'factoring' | 'retenciones' | 'caja'

const inputClass =
  'h-8 rounded-lg border-slate-200 bg-white px-2 text-sm text-slate-900 placeholder-slate-400 focus-visible:border-sky-600 focus-visible:ring-sky-500/20'

const selectClass =
  'h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-500/30'

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
const tab = ref<TabFin>('facturas')
const loading = ref(true)
const saving = ref(false)

const facturas = ref<FacturaObra[]>([])
const seleccionada = ref<FacturaObra | null>(null)
const operaciones = ref<OperacionFactoring[]>([])
const retenciones = ref<RetencionesObra | null>(null)
const flujo = ref<FlujoCaja | null>(null)

const cobro = reactive({
  fecha: new Date().toISOString().slice(0, 10),
  banco: '',
  numeroOperacion: '',
  importe: '',
  detraccion: '0',
  observaciones: '',
})

const vencimiento = ref('')

const cesion = reactive({
  facturaId: '',
  entidadRazon: '',
  entidadRuc: '',
  fechaCesion: new Date().toISOString().slice(0, 10),
  tasaDescuento: '',
  comision: '',
  fechaPagoEsperada: '',
})

const liberacion = reactive({
  tipo: 'EFECTIVO' as TipoLiberacionRetencion,
  fecha: new Date().toISOString().slice(0, 10),
  importe: '',
  entidad: '',
  numeroDocumento: '',
  observaciones: '',
})

const emitidas = computed(() => facturas.value.filter((item) => item.estado === 'EMITIDA'))

function n(valor: number | string | null | undefined): number {
  const parsed = Number(valor)
  return Number.isFinite(parsed) ? parsed : 0
}

function mesLabel(valor: number): string {
  return MESES[valor - 1] ?? String(valor)
}

function estadoFacturaLabel(estado: EstadoFacturaObra): string {
  if (estado === 'PARCIAL') return 'Parcial'
  if (estado === 'COBRADA') return 'Cobrada'
  if (estado === 'CEDIDA') return 'Cedida'
  return 'Emitida'
}

function estadoFactoringLabel(estado: EstadoFactoring): string {
  if (estado === 'ADELANTADA') return 'Adelantada'
  if (estado === 'COBRADA') return 'Cobrada'
  if (estado === 'IMPAGA') return 'Impaga'
  if (estado === 'REVERTIDA') return 'Revertida'
  return 'Cedida'
}

function tipoLiberacionLabel(tipo: TipoLiberacionRetencion): string {
  return tipo === 'CARTA_FIANZA' ? 'Carta fianza' : 'Efectivo'
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    const [facts, ops, ret, caja] = await Promise.all([
      listarFacturasObra(proyectoId.value),
      listarFactoring(proyectoId.value),
      obtenerRetencionesObra(proyectoId.value),
      obtenerFlujoCaja(proyectoId.value),
    ])
    facturas.value = facts
    operaciones.value = ops
    retenciones.value = ret
    flujo.value = caja
    if (seleccionada.value) {
      seleccionada.value = facts.find((item) => item.id === seleccionada.value?.id) ?? null
      vencimiento.value = seleccionada.value?.fechaVencimiento ?? ''
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el financiamiento.')
  } finally {
    loading.value = false
  }
}

function abrirFactura(item: FacturaObra) {
  seleccionada.value = item
  vencimiento.value = item.fechaVencimiento ?? ''
  cobro.fecha = new Date().toISOString().slice(0, 10)
  cobro.importe = ''
  cobro.detraccion = '0'
}

async function onGuardarVencimiento() {
  if (!seleccionada.value) return
  saving.value = true
  try {
    seleccionada.value = await actualizarVencimientoFactura(
      proyectoId.value,
      seleccionada.value.id,
      vencimiento.value || null,
    )
    toast.success('Vencimiento actualizado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el vencimiento.')
  } finally {
    saving.value = false
  }
}

async function onCobrar() {
  if (!seleccionada.value) return
  const importe = n(cobro.importe)
  const detraccion = n(cobro.detraccion)
  if (importe + detraccion <= 0) {
    toast.error('Ingrese un importe o una detracción mayor a cero.')
    return
  }
  saving.value = true
  try {
    seleccionada.value = await registrarCobranzaObra(proyectoId.value, {
      facturaId: seleccionada.value.id,
      fecha: cobro.fecha,
      banco: cobro.banco.trim() || null,
      numeroOperacion: cobro.numeroOperacion.trim() || null,
      importe,
      detraccion,
      observaciones: cobro.observaciones.trim() || null,
    })
    cobro.importe = ''
    cobro.detraccion = '0'
    cobro.observaciones = ''
    toast.success('Cobro registrado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el cobro.')
  } finally {
    saving.value = false
  }
}

async function onCeder() {
  const facturaId = Number(cesion.facturaId)
  if (!facturaId || !cesion.entidadRazon.trim()) {
    toast.error('Elija factura y entidad de factoring.')
    return
  }
  saving.value = true
  try {
    await cederFacturaFactoring(proyectoId.value, {
      facturaId,
      entidadRazon: cesion.entidadRazon.trim(),
      entidadRuc: cesion.entidadRuc.trim() || null,
      fechaCesion: cesion.fechaCesion,
      tasaDescuento: n(cesion.tasaDescuento),
      comision: n(cesion.comision),
      fechaPagoEsperada: cesion.fechaPagoEsperada || null,
    })
    cesion.facturaId = ''
    cesion.entidadRazon = ''
    cesion.entidadRuc = ''
    cesion.tasaDescuento = ''
    cesion.comision = ''
    toast.success('Factura cedida.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo ceder la factura.')
  } finally {
    saving.value = false
  }
}

async function onFactoring(accion: () => Promise<OperacionFactoring>, ok: string) {
  saving.value = true
  try {
    await accion()
    toast.success(ok)
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo actualizar el factoring.')
  } finally {
    saving.value = false
  }
}

async function onLiberar() {
  const importe = n(liberacion.importe)
  if (importe <= 0) {
    toast.error('Ingrese un importe mayor a cero.')
    return
  }
  saving.value = true
  try {
    retenciones.value = await liberarRetencionObra(
      proyectoId.value,
      liberacion.tipo,
      liberacion.fecha,
      importe,
      liberacion.entidad.trim() || null,
      liberacion.numeroDocumento.trim() || null,
      liberacion.observaciones.trim() || null,
    )
    liberacion.importe = ''
    liberacion.entidad = ''
    liberacion.numeroDocumento = ''
    liberacion.observaciones = ''
    toast.success('Movimiento de retención registrado.')
    flujo.value = await obtenerFlujoCaja(proyectoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la liberación.')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs :model-value="tab" @update:model-value="(value) => (tab = String(value) as TabFin)">
      <TabsList class="h-auto rounded-xl border border-slate-200 bg-slate-100/85 p-1.5">
        <TabsTrigger value="facturas" class="h-9 rounded-lg px-4 text-sm">Facturas</TabsTrigger>
        <TabsTrigger value="factoring" class="h-9 rounded-lg px-4 text-sm">Factoring</TabsTrigger>
        <TabsTrigger value="retenciones" class="h-9 rounded-lg px-4 text-sm">Retenciones</TabsTrigger>
        <TabsTrigger value="caja" class="h-9 rounded-lg px-4 text-sm">Caja</TabsTrigger>
      </TabsList>
    </Tabs>

    <div v-if="loading" class="space-y-3">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <div v-else-if="tab === 'facturas'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>N.º</TableHead>
              <TableHead>Fecha</TableHead>
              <TableHead>Cliente</TableHead>
              <TableHead>Total</TableHead>
              <TableHead>Saldo</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in facturas" :key="item.id">
              <TableCell>{{ item.numero }}</TableCell>
              <TableCell>{{ item.fecha }}</TableCell>
              <TableCell>{{ item.clienteRazon || '—' }}</TableCell>
              <TableCell>{{ money.format(n(item.total)) }}</TableCell>
              <TableCell>{{ money.format(n(item.saldo)) }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ estadoFacturaLabel(item.estado) }}</Badge>
              </TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" @click="abrirFactura(item)">Abrir</Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!facturas.length" :colspan="7">Aún no hay facturas. Genérelas desde Avances.</TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div v-if="seleccionada" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">
          Factura N.º {{ seleccionada.numero }} · {{ estadoFacturaLabel(seleccionada.estado) }}
        </h3>
        <p class="mt-1 text-sm text-slate-500">
          Total {{ money.format(n(seleccionada.total)) }} · saldo {{ money.format(n(seleccionada.saldo)) }}
        </p>
        <div class="mt-4 flex flex-wrap items-end gap-3">
          <div>
            <label class="text-sm font-medium text-slate-700" for="fac-venc">Vencimiento</label>
            <Input id="fac-venc" v-model="vencimiento" type="date" :class="inputClass" class="mt-1.5 w-44" />
          </div>
          <Button variant="outline" type="button" :disabled="saving" @click="onGuardarVencimiento">Guardar fecha</Button>
        </div>
        <div
          v-if="seleccionada.estado === 'EMITIDA' || seleccionada.estado === 'PARCIAL'"
          class="mt-4 grid gap-3 sm:grid-cols-6"
        >
          <Input v-model="cobro.fecha" type="date" :class="inputClass" />
          <Input v-model="cobro.banco" :class="inputClass" placeholder="Banco" maxlength="80" />
          <Input v-model="cobro.numeroOperacion" :class="inputClass" placeholder="N.º operación" maxlength="40" />
          <Input v-model="cobro.importe" :class="inputClass" placeholder="Importe" inputmode="decimal" />
          <Input v-model="cobro.detraccion" :class="inputClass" placeholder="Detracción" inputmode="decimal" />
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="saving"
            @click="onCobrar"
          >
            Registrar cobro
          </Button>
        </div>
        <Textarea
          v-if="seleccionada.estado === 'EMITIDA' || seleccionada.estado === 'PARCIAL'"
          v-model="cobro.observaciones"
          class="mt-3"
          placeholder="Observaciones"
        />
        <Table class="mt-4">
          <TableHeader>
            <TableRow>
              <TableHead>Fecha</TableHead>
              <TableHead>Banco</TableHead>
              <TableHead>Importe</TableHead>
              <TableHead>Detracción</TableHead>
              <TableHead>Aplicado</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in seleccionada.cobranzas ?? []" :key="item.id">
              <TableCell>{{ item.fecha }}</TableCell>
              <TableCell>{{ item.banco || '—' }}</TableCell>
              <TableCell>{{ money.format(n(item.importe)) }}</TableCell>
              <TableCell>{{ money.format(n(item.detraccion)) }}</TableCell>
              <TableCell>{{ money.format(n(item.aplicado)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!(seleccionada.cobranzas?.length)" :colspan="5">Sin cobros.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else-if="tab === 'factoring'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Ceder factura</h3>
        <div class="mt-4 grid gap-3 sm:grid-cols-4">
          <select v-model="cesion.facturaId" :class="selectClass">
            <option value="">Factura emitida</option>
            <option v-for="item in emitidas" :key="item.id" :value="String(item.id)">
              N.º {{ item.numero }} · {{ money.format(n(item.total)) }}
            </option>
          </select>
          <Input v-model="cesion.entidadRazon" :class="inputClass" placeholder="Entidad" maxlength="160" />
          <Input v-model="cesion.entidadRuc" :class="inputClass" placeholder="RUC" maxlength="11" />
          <Input v-model="cesion.fechaCesion" type="date" :class="inputClass" />
          <Input v-model="cesion.tasaDescuento" :class="inputClass" placeholder="Tasa %" inputmode="decimal" />
          <Input v-model="cesion.comision" :class="inputClass" placeholder="Comisión" inputmode="decimal" />
          <Input v-model="cesion.fechaPagoEsperada" type="date" :class="inputClass" />
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="saving"
            @click="onCeder"
          >
            Ceder
          </Button>
        </div>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Factura</TableHead>
              <TableHead>Entidad</TableHead>
              <TableHead>Nominal</TableHead>
              <TableHead>Neto</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in operaciones" :key="item.id">
              <TableCell>N.º {{ item.facturaNumero }}</TableCell>
              <TableCell>{{ item.entidadRazon }}</TableCell>
              <TableCell>{{ money.format(n(item.importeNominal)) }}</TableCell>
              <TableCell>{{ money.format(n(item.importeNeto)) }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ estadoFactoringLabel(item.estado) }}</Badge>
              </TableCell>
              <TableCell class="space-x-2">
                <Button
                  v-if="item.estado === 'CEDIDA'"
                  variant="outline"
                  size="sm"
                  type="button"
                  :disabled="saving"
                  @click="onFactoring(() => adelantarFactoring(proyectoId, item.id), 'Adelanto registrado.')"
                >
                  Adelantar
                </Button>
                <Button
                  v-if="item.estado === 'CEDIDA'"
                  variant="outline"
                  size="sm"
                  type="button"
                  :disabled="saving"
                  @click="onFactoring(() => revertirFactoring(proyectoId, item.id), 'Cesión revertida.')"
                >
                  Revertir
                </Button>
                <Button
                  v-if="item.estado === 'ADELANTADA'"
                  variant="outline"
                  size="sm"
                  type="button"
                  :disabled="saving"
                  @click="onFactoring(() => impagarFactoring(proyectoId, item.id), 'Marcada impaga.')"
                >
                  Impaga
                </Button>
                <Button
                  v-if="item.estado === 'ADELANTADA' || item.estado === 'IMPAGA'"
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  size="sm"
                  type="button"
                  :disabled="saving"
                  @click="onFactoring(() => cobrarFactoring(proyectoId, item.id), 'Cliente pagó al factor.')"
                >
                  Cobrar
                </Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!operaciones.length" :colspan="6">Sin operaciones de factoring.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else-if="tab === 'retenciones'" class="space-y-4">
      <div v-if="retenciones" class="grid gap-3 sm:grid-cols-4">
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Retenido</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(retenciones.retenidoAcumulado)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Liberado</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(retenciones.liberado)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Carta fianza</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(retenciones.cartaFianza)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Saldo</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(retenciones.saldo)) }}</p>
        </div>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <div class="grid gap-3 sm:grid-cols-6">
          <select v-model="liberacion.tipo" :class="selectClass">
            <option value="EFECTIVO">Efectivo</option>
            <option value="CARTA_FIANZA">Carta fianza</option>
          </select>
          <Input v-model="liberacion.fecha" type="date" :class="inputClass" />
          <Input v-model="liberacion.importe" :class="inputClass" placeholder="Importe" inputmode="decimal" />
          <Input v-model="liberacion.entidad" :class="inputClass" placeholder="Entidad" maxlength="160" />
          <Input v-model="liberacion.numeroDocumento" :class="inputClass" placeholder="N.º documento" maxlength="40" />
          <Button variant="outline" type="button" :disabled="saving" @click="onLiberar">Registrar</Button>
        </div>
        <Table class="mt-4">
          <TableHeader>
            <TableRow>
              <TableHead>Fecha</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Importe</TableHead>
              <TableHead>Entidad</TableHead>
              <TableHead>Documento</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in retenciones?.movimientos ?? []" :key="item.id">
              <TableCell>{{ item.fecha }}</TableCell>
              <TableCell>{{ tipoLiberacionLabel(item.tipo) }}</TableCell>
              <TableCell>{{ money.format(n(item.importe)) }}</TableCell>
              <TableCell>{{ item.entidad || '—' }}</TableCell>
              <TableCell>{{ item.numeroDocumento || '—' }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!(retenciones?.movimientos.length)" :colspan="5">Sin movimientos.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else class="space-y-4">
      <div v-if="flujo" class="grid gap-3 sm:grid-cols-3">
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Por cobrar</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(flujo.porCobrar)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Retenido</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(flujo.retenido)) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm">
          <p class="text-slate-500">Cedido</p>
          <p class="font-semibold text-palette-navy">{{ money.format(n(flujo.cedido)) }}</p>
        </div>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Periodo</TableHead>
              <TableHead>Ingresos</TableHead>
              <TableHead>Egresos</TableHead>
              <TableHead>Costo factoring</TableHead>
              <TableHead>Neto</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="punto in flujo?.puntos ?? []" :key="`${punto.anio}-${punto.mes}`">
              <TableCell>{{ mesLabel(punto.mes) }} {{ punto.anio }}</TableCell>
              <TableCell>{{ money.format(n(punto.ingresos)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.egresos)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.costoFactoring)) }}</TableCell>
              <TableCell>{{ money.format(n(punto.neto)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!(flujo?.puntos.length)" :colspan="5">Sin movimientos en el periodo.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>
  </div>
</template>
