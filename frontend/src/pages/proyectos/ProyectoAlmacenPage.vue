<script setup lang="ts">
import { PlusIcon } from '@lucide/vue'
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
  anularRequerimientoObra,
  aprobarRequerimientoObra,
  cerrarInventario,
  cerrarOrdenCompra,
  crearOrdenCompra,
  crearRequerimientoObra,
  crearValeSalida,
  devolverProveedor,
  devolverValeSalida,
  emitirOrdenCompra,
  guardarOrdenCompra,
  guardarRequerimientoObra,
  listarCierresInventario,
  listarKardex,
  listarMateriales,
  listarOrdenesCompra,
  listarRequerimientosObra,
  listarValesSalida,
  obtenerAlmacen,
  obtenerOrdenCompra,
  obtenerPresupuesto,
  recepcionarOrdenCompra,
  registrarAjusteAlmacen,
  sugeridosApuMateriales,
} from '@/services/construccion/construccionService'
import { getSession } from '@/services/auth/authService'
import { esAlmacen } from '@/types/auth'
import type {
  AlmacenObra,
  InventarioCierre,
  MaterialCatalogo,
  MovimientoAlmacen,
  OrdenCompra,
  PartidaPresupuesto,
  RequerimientoObra,
  ValeSalida,
} from '@/types/construccion'

type TabAlmacen = 'requerimientos' | 'compras' | 'kardex'

type ItemDraft = {
  materialId: string
  partidaId: string
  cantidad: string
  precioUnitario: string
  requerimientoItemId?: number | null
}

const inputClass =
  'h-8 rounded-lg border-slate-200 bg-white px-2 text-sm text-slate-900 placeholder-slate-400 focus-visible:border-sky-600 focus-visible:ring-sky-500/20'

const selectClass =
  'h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-500/30'

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const qtyFmt = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 0, maximumFractionDigits: 4 })
const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const almacenRol = computed(() => esAlmacen(getSession()?.rol))
const tab = ref<TabAlmacen>(almacenRol.value ? 'kardex' : 'requerimientos')
const loading = ref(true)
const saving = ref(false)

const materiales = ref<MaterialCatalogo[]>([])
const almacen = ref<AlmacenObra | null>(null)
const partidas = ref<PartidaPresupuesto[]>([])
const requerimientos = ref<RequerimientoObra[]>([])
const compras = ref<OrdenCompra[]>([])
const kardex = ref<MovimientoAlmacen[]>([])
const vales = ref<ValeSalida[]>([])
const cierres = ref<InventarioCierre[]>([])

const reqId = ref<number | null>(null)
const reqObs = ref('')
const reqItems = ref<ItemDraft[]>([])

const ocId = ref<number | null>(null)
const ocForm = reactive({
  proveedorRazon: '',
  proveedorRuc: '',
  plazoEntrega: '',
  observaciones: '',
  requerimientoId: '',
})
const ocItems = ref<ItemDraft[]>([])
const recepcionGuia = ref('')
const recepcionCantidades = ref<Record<number, string>>({})
const devolucionCantidades = ref<Record<number, string>>({})

const valePartidaId = ref('')
const valeItems = ref<ItemDraft[]>([{ materialId: '', partidaId: '', cantidad: '', precioUnitario: '' }])
const ajuste = reactive({ materialId: '', cantidad: '', observaciones: '' })
const cierreAnio = ref(String(new Date().getFullYear()))
const cierreMes = ref(String(new Date().getMonth() + 1))

const ocSeleccionada = computed(() => compras.value.find((item) => item.id === ocId.value) ?? null)
const reqSeleccionado = computed(() => requerimientos.value.find((item) => item.id === reqId.value) ?? null)
const esReqBorrador = computed(() => !reqSeleccionado.value || reqSeleccionado.value.estado === 'BORRADOR')
const esOcBorrador = computed(() => !ocSeleccionada.value || ocSeleccionada.value.estado === 'BORRADOR')
const ocRecepcionable = computed(() => {
  const estado = ocSeleccionada.value?.estado
  return estado === 'EMITIDA' || estado === 'RECEPCIONADA'
})

onMounted(() => {
  void load()
})

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    if (esAlmacen(getSession()?.rol)) {
      const [alm, movs] = await Promise.all([
        obtenerAlmacen(proyectoId.value),
        listarKardex(proyectoId.value),
      ])
      almacen.value = alm
      kardex.value = movs
      return
    }
    const [mats, alm, pres, reqs, ocs, movs, valesData, cierresData] = await Promise.all([
      listarMateriales(),
      obtenerAlmacen(proyectoId.value),
      obtenerPresupuesto(proyectoId.value).catch(() => null),
      listarRequerimientosObra(proyectoId.value),
      listarOrdenesCompra(proyectoId.value),
      listarKardex(proyectoId.value),
      listarValesSalida(proyectoId.value),
      listarCierresInventario(proyectoId.value),
    ])
    materiales.value = mats
    almacen.value = alm
    partidas.value = pres?.version?.estado === 'VIGENTE' ? pres.version.partidas : []
    requerimientos.value = reqs
    compras.value = ocs
    kardex.value = movs
    vales.value = valesData
    cierres.value = cierresData
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el almacén.')
  } finally {
    loading.value = false
  }
}

function nuevoReq() {
  reqId.value = null
  reqObs.value = ''
  reqItems.value = [{ materialId: '', partidaId: '', cantidad: '', precioUnitario: '' }]
}

function cargarReq(item: RequerimientoObra) {
  reqId.value = item.id
  reqObs.value = item.observaciones ?? ''
  reqItems.value = item.items.map((fila) => ({
    materialId: String(fila.materialId),
    partidaId: fila.partidaId == null ? '' : String(fila.partidaId),
    cantidad: String(fila.cantidad),
    precioUnitario: '',
  }))
}

async function onGuardarReq() {
  saving.value = true
  try {
    const request = {
      observaciones: reqObs.value || null,
      items: reqItems.value
        .filter((item) => item.materialId && item.cantidad)
        .map((item) => ({
          materialId: Number(item.materialId),
          partidaId: item.partidaId ? Number(item.partidaId) : null,
          cantidad: Number(item.cantidad),
        })),
    }
    const saved = reqId.value
      ? await guardarRequerimientoObra(proyectoId.value, reqId.value, request)
      : await crearRequerimientoObra(proyectoId.value, request)
    toast.success(reqId.value ? 'Requerimiento guardado.' : 'Requerimiento creado.')
    await load()
    const actual = requerimientos.value.find((item) => item.id === saved.id)
    if (actual) {
      cargarReq(actual)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el requerimiento.')
  } finally {
    saving.value = false
  }
}

async function onAprobarReq() {
  if (!reqId.value) return
  saving.value = true
  try {
    await aprobarRequerimientoObra(proyectoId.value, reqId.value)
    toast.success('Requerimiento aprobado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo aprobar.')
  } finally {
    saving.value = false
  }
}

async function onAnularReq() {
  if (!reqId.value) return
  saving.value = true
  try {
    await anularRequerimientoObra(proyectoId.value, reqId.value)
    toast.success('Requerimiento anulado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo anular.')
  } finally {
    saving.value = false
  }
}

async function onSugerirApu() {
  saving.value = true
  try {
    const sugeridos = await sugeridosApuMateriales(proyectoId.value)
    const conCatalogo = sugeridos.filter((item) => item.materialId)
    if (!conCatalogo.length) {
      toast.error('No hay líneas APU de material con código de catálogo. Cree los materiales primero.')
      return
    }
    reqItems.value = conCatalogo.map((item) => ({
      materialId: String(item.materialId),
      partidaId: String(item.partidaId),
      cantidad: String(item.cantidad),
      precioUnitario: '',
    }))
    toast.success('Se cargaron las sugerencias del APU vigente.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las sugerencias.')
  } finally {
    saving.value = false
  }
}

function nuevaOc() {
  ocId.value = null
  ocForm.proveedorRazon = ''
  ocForm.proveedorRuc = ''
  ocForm.plazoEntrega = ''
  ocForm.observaciones = ''
  ocForm.requerimientoId = ''
  ocItems.value = [{ materialId: '', partidaId: '', cantidad: '', precioUnitario: '' }]
  recepcionCantidades.value = {}
  devolucionCantidades.value = {}
}

async function cargarOc(item: OrdenCompra) {
  ocId.value = item.id
  ocForm.proveedorRazon = item.proveedorRazon ?? ''
  ocForm.proveedorRuc = item.proveedorRuc ?? ''
  ocForm.plazoEntrega = item.plazoEntrega ?? ''
  ocForm.observaciones = item.observaciones ?? ''
  ocForm.requerimientoId = ''
  ocItems.value = item.items.map((fila) => ({
    materialId: String(fila.materialId),
    partidaId: '',
    cantidad: String(fila.cantidad),
    precioUnitario: String(fila.precioUnitario),
    requerimientoItemId: fila.requerimientoItemId,
  }))
  try {
    const detalle = await obtenerOrdenCompra(proyectoId.value, item.id)
    const idx = compras.value.findIndex((fila) => fila.id === item.id)
    if (idx >= 0) {
      compras.value[idx] = detalle
    }
    recepcionCantidades.value = Object.fromEntries(
      detalle.items.map((fila) => [fila.id, fila.cantidadPendiente > 0 ? String(fila.cantidadPendiente) : '']),
    )
    devolucionCantidades.value = Object.fromEntries(detalle.items.map((fila) => [fila.id, '']))
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la orden.')
  }
}

async function onGuardarOc() {
  saving.value = true
  try {
    const request = {
      requerimientoId: ocForm.requerimientoId ? Number(ocForm.requerimientoId) : null,
      proveedorRazon: ocForm.proveedorRazon || null,
      proveedorRuc: ocForm.proveedorRuc || null,
      plazoEntrega: ocForm.plazoEntrega || null,
      observaciones: ocForm.observaciones || null,
      items: ocItems.value
        .filter((item) => item.materialId && item.cantidad)
        .map((item) => ({
          materialId: Number(item.materialId),
          requerimientoItemId: item.requerimientoItemId ?? null,
          cantidad: Number(item.cantidad),
          precioUnitario: Number(item.precioUnitario || '0'),
        })),
    }
    const saved = ocId.value
      ? await guardarOrdenCompra(proyectoId.value, ocId.value, request)
      : await crearOrdenCompra(proyectoId.value, request)
    toast.success(ocId.value ? 'Orden guardada.' : 'Orden creada.')
    await load()
    const actual = compras.value.find((item) => item.id === saved.id)
    if (actual) {
      await cargarOc(actual)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la orden.')
  } finally {
    saving.value = false
  }
}

async function onEmitirOc() {
  if (!ocId.value) return
  saving.value = true
  try {
    await emitirOrdenCompra(proyectoId.value, ocId.value)
    toast.success('Orden emitida.')
    await load()
    const actual = compras.value.find((item) => item.id === ocId.value)
    if (actual) await cargarOc(actual)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo emitir.')
  } finally {
    saving.value = false
  }
}

async function onCerrarOc() {
  if (!ocId.value) return
  saving.value = true
  try {
    await cerrarOrdenCompra(proyectoId.value, ocId.value)
    toast.success('Orden cerrada.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cerrar.')
  } finally {
    saving.value = false
  }
}

async function onRecepcionar() {
  if (!ocId.value || !ocSeleccionada.value) return
  const items = ocSeleccionada.value.items
    .map((item) => ({
      ordenCompraItemId: item.id,
      cantidad: Number(recepcionCantidades.value[item.id] || '0'),
    }))
    .filter((item) => item.cantidad > 0)
  if (!items.length) {
    toast.error('Indique cantidades a recepcionar.')
    return
  }
  saving.value = true
  try {
    await recepcionarOrdenCompra(proyectoId.value, ocId.value, {
      numeroGuia: recepcionGuia.value || null,
      items,
    })
    toast.success('Recepción registrada.')
    recepcionGuia.value = ''
    await load()
    const actual = compras.value.find((item) => item.id === ocId.value)
    if (actual) await cargarOc(actual)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo recepcionar.')
  } finally {
    saving.value = false
  }
}

async function onDevolverProveedor() {
  if (!ocId.value || !ocSeleccionada.value) return
  const items = ocSeleccionada.value.items
    .map((item) => ({
      ordenCompraItemId: item.id,
      cantidad: Number(devolucionCantidades.value[item.id] || '0'),
    }))
    .filter((item) => item.cantidad > 0)
  if (!items.length) {
    toast.error('Indique cantidades a devolver.')
    return
  }
  saving.value = true
  try {
    await devolverProveedor(proyectoId.value, ocId.value, items)
    toast.success('Devolución a proveedor registrada.')
    await load()
    const actual = compras.value.find((item) => item.id === ocId.value)
    if (actual) await cargarOc(actual)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo devolver.')
  } finally {
    saving.value = false
  }
}

async function onCrearVale() {
  const items = valeItems.value
    .filter((item) => item.materialId && item.cantidad)
    .map((item) => ({ materialId: Number(item.materialId), cantidad: Number(item.cantidad) }))
  if (!valePartidaId.value || !items.length) {
    toast.error('Indique partida e ítems del vale.')
    return
  }
  saving.value = true
  try {
    await crearValeSalida(proyectoId.value, {
      partidaId: Number(valePartidaId.value),
      items,
    })
    toast.success('Vale de salida registrado.')
    valeItems.value = [{ materialId: '', partidaId: '', cantidad: '', precioUnitario: '' }]
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo crear el vale.')
  } finally {
    saving.value = false
  }
}

async function onDevolverVale(valeId: number) {
  saving.value = true
  try {
    await devolverValeSalida(proyectoId.value, valeId)
    toast.success('Devolución a almacén registrada.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo devolver el vale.')
  } finally {
    saving.value = false
  }
}

async function onAjuste() {
  if (!ajuste.materialId || !ajuste.cantidad) {
    toast.error('Indique material y cantidad.')
    return
  }
  saving.value = true
  try {
    await registrarAjusteAlmacen(proyectoId.value, {
      materialId: Number(ajuste.materialId),
      cantidad: Number(ajuste.cantidad),
      observaciones: ajuste.observaciones || null,
    })
    toast.success('Ajuste registrado.')
    ajuste.materialId = ''
    ajuste.cantidad = ''
    ajuste.observaciones = ''
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el ajuste.')
  } finally {
    saving.value = false
  }
}

async function onCerrarMes() {
  saving.value = true
  try {
    await cerrarInventario(proyectoId.value, Number(cierreAnio.value), Number(cierreMes.value))
    toast.success('Cierre de inventario registrado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cerrar el mes.')
  } finally {
    saving.value = false
  }
}

function onSelectReq(event: Event) {
  const id = Number((event.target as HTMLSelectElement).value)
  if (!id) {
    nuevoReq()
    return
  }
  const encontrado = requerimientos.value.find((item) => item.id === id)
  if (encontrado) {
    cargarReq(encontrado)
  }
}

function onSelectOc(event: Event) {
  const id = Number((event.target as HTMLSelectElement).value)
  if (!id) {
    nuevaOc()
    return
  }
  const encontrado = compras.value.find((item) => item.id === id)
  if (encontrado) {
    void cargarOc(encontrado)
  }
}
</script>

<template>
  <div class="mt-4 space-y-4">
    <div
      v-if="almacen && (almacen.quiebres || almacen.criticosSinStock)"
      class="rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-950"
    >
      Alertas de stock: {{ almacen.quiebres }} quiebre(s)
      <span v-if="almacen.criticosSinStock"> y {{ almacen.criticosSinStock }} crítico(s) sin stock</span>.
    </div>

    <Tabs :model-value="tab" @update:model-value="(value) => (tab = String(value) as TabAlmacen)">
      <TabsList class="h-auto rounded-xl border border-slate-200 bg-slate-100/85 p-1.5">
        <TabsTrigger v-if="!almacenRol" value="requerimientos" class="h-9 rounded-lg px-4 text-sm">Requerimientos</TabsTrigger>
        <TabsTrigger v-if="!almacenRol" value="compras" class="h-9 rounded-lg px-4 text-sm">Compras</TabsTrigger>
        <TabsTrigger value="kardex" class="h-9 rounded-lg px-4 text-sm">Kardex</TabsTrigger>
      </TabsList>
    </Tabs>

    <div v-if="loading" class="space-y-3">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <div v-else-if="tab === 'requerimientos'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <div class="mb-4 flex flex-wrap gap-2">
          <Button variant="outline" type="button" @click="nuevoReq">
            <PlusIcon data-icon="inline-start" />
            Nuevo
          </Button>
          <Button variant="outline" type="button" :disabled="saving" @click="onSugerirApu">Sugerir desde APU</Button>
          <Button v-if="esReqBorrador" class="bg-palette-navy text-white hover:bg-palette-royal" type="button" :disabled="saving" @click="onGuardarReq">
            Guardar
          </Button>
          <Button v-if="reqSeleccionado?.estado === 'BORRADOR'" type="button" :disabled="saving" @click="onAprobarReq">Aprobar</Button>
          <Button v-if="reqSeleccionado && reqSeleccionado.estado !== 'ATENDIDO' && reqSeleccionado.estado !== 'ANULADO'" variant="outline" type="button" :disabled="saving" @click="onAnularReq">
            Anular
          </Button>
        </div>
        <div class="mb-4 grid gap-3 sm:grid-cols-2">
          <select :class="selectClass" :value="reqId ?? ''" @change="onSelectReq">
            <option value="">Requerimiento nuevo</option>
            <option v-for="item in requerimientos" :key="item.id" :value="item.id">
              REQ-{{ item.numero }} · {{ item.estado }}
            </option>
          </select>
          <Textarea v-model="reqObs" :disabled="!esReqBorrador" placeholder="Observaciones" />
        </div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Material</TableHead>
              <TableHead>Partida</TableHead>
              <TableHead>Cantidad</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="(item, index) in reqItems" :key="index">
              <TableCell>
                <select v-model="item.materialId" :class="selectClass" :disabled="!esReqBorrador">
                  <option value="">Material</option>
                  <option v-for="mat in materiales" :key="mat.id" :value="String(mat.id)">{{ mat.codigo }} — {{ mat.descripcion }}</option>
                </select>
              </TableCell>
              <TableCell>
                <select v-model="item.partidaId" :class="selectClass" :disabled="!esReqBorrador">
                  <option value="">Sin partida</option>
                  <option v-for="partida in partidas" :key="partida.id" :value="String(partida.id)">
                    {{ partida.codigo }} — {{ partida.descripcion }}
                  </option>
                </select>
              </TableCell>
              <TableCell>
                <Input v-model="item.cantidad" :class="inputClass" :disabled="!esReqBorrador" />
              </TableCell>
              <TableCell>
                <Button v-if="esReqBorrador" variant="outline" size="sm" type="button" @click="reqItems.splice(index, 1)">Quitar</Button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
        <Button v-if="esReqBorrador" class="mt-3" variant="outline" type="button" @click="reqItems.push({ materialId: '', partidaId: '', cantidad: '', precioUnitario: '' })">
          Agregar ítem
        </Button>
      </div>
    </div>

    <div v-else-if="tab === 'compras'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <div class="mb-4 flex flex-wrap gap-2">
          <Button variant="outline" type="button" @click="nuevaOc">
            <PlusIcon data-icon="inline-start" />
            Nueva OC
          </Button>
          <Button v-if="esOcBorrador" class="bg-palette-navy text-white hover:bg-palette-royal" type="button" :disabled="saving" @click="onGuardarOc">
            Guardar
          </Button>
          <Button v-if="ocSeleccionada?.estado === 'BORRADOR'" type="button" :disabled="saving" @click="onEmitirOc">Emitir</Button>
          <Button v-if="ocRecepcionable" variant="outline" type="button" :disabled="saving" @click="onCerrarOc">Cerrar</Button>
        </div>
        <div class="mb-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <select :class="selectClass" :value="ocId ?? ''" @change="onSelectOc">
            <option value="">Orden nueva</option>
            <option v-for="item in compras" :key="item.id" :value="item.id">OC-{{ item.numero }} · {{ item.estado }}</option>
          </select>
          <select v-if="esOcBorrador" v-model="ocForm.requerimientoId" :class="selectClass">
            <option value="">Copiar de requerimiento</option>
            <option v-for="item in requerimientos.filter((fila) => fila.estado === 'APROBADO')" :key="item.id" :value="String(item.id)">
              REQ-{{ item.numero }}
            </option>
          </select>
          <Input v-model="ocForm.proveedorRazon" :class="inputClass" :disabled="!esOcBorrador" placeholder="Proveedor" />
          <Input v-model="ocForm.proveedorRuc" :class="inputClass" :disabled="!esOcBorrador" placeholder="RUC" />
        </div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Material</TableHead>
              <TableHead>Cantidad</TableHead>
              <TableHead>Precio</TableHead>
              <TableHead>Recibido</TableHead>
              <TableHead>Pendiente</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="(item, index) in ocItems" :key="index">
              <TableCell>
                <select v-model="item.materialId" :class="selectClass" :disabled="!esOcBorrador">
                  <option value="">Material</option>
                  <option v-for="mat in materiales" :key="mat.id" :value="String(mat.id)">{{ mat.codigo }} — {{ mat.descripcion }}</option>
                </select>
              </TableCell>
              <TableCell><Input v-model="item.cantidad" :class="inputClass" :disabled="!esOcBorrador" /></TableCell>
              <TableCell><Input v-model="item.precioUnitario" :class="inputClass" :disabled="!esOcBorrador" /></TableCell>
              <TableCell>{{ ocSeleccionada?.items[index] ? qtyFmt.format(ocSeleccionada.items[index].cantidadRecibida) : '—' }}</TableCell>
              <TableCell>{{ ocSeleccionada?.items[index] ? qtyFmt.format(ocSeleccionada.items[index].cantidadPendiente) : '—' }}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
        <Button v-if="esOcBorrador" class="mt-3" variant="outline" type="button" @click="ocItems.push({ materialId: '', partidaId: '', cantidad: '', precioUnitario: '' })">
          Agregar ítem
        </Button>

        <div v-if="ocRecepcionable && ocSeleccionada" class="mt-6 border-t border-slate-200 pt-4">
          <h4 class="text-sm font-semibold text-palette-navy">Recepción</h4>
          <div class="mt-3 grid gap-3 sm:grid-cols-3">
            <Input v-model="recepcionGuia" :class="inputClass" placeholder="N° guía (opcional)" />
            <div v-for="item in ocSeleccionada.items" :key="item.id" class="flex items-center gap-2 text-sm">
              <span class="w-32 truncate">{{ item.codigoMaterial }}</span>
              <Input v-model="recepcionCantidades[item.id]" :class="inputClass" :placeholder="'Pend. ' + item.cantidadPendiente" />
            </div>
          </div>
          <Button class="mt-3 bg-palette-navy text-white hover:bg-palette-royal" type="button" :disabled="saving" @click="onRecepcionar">
            Recepcionar
          </Button>
          <div v-if="ocSeleccionada.estado === 'RECEPCIONADA' || ocSeleccionada.estado === 'CERRADA'" class="mt-4">
            <h4 class="text-sm font-semibold text-palette-navy">Devolución a proveedor</h4>
            <div class="mt-3 flex flex-wrap gap-2">
              <div v-for="item in ocSeleccionada.items" :key="'dev-' + item.id" class="flex items-center gap-2 text-sm">
                <span class="w-32 truncate">{{ item.codigoMaterial }}</span>
                <Input v-model="devolucionCantidades[item.id]" :class="inputClass" placeholder="Cantidad" />
              </div>
            </div>
            <Button class="mt-3" variant="outline" type="button" :disabled="saving" @click="onDevolverProveedor">Devolver</Button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Stock</h3>
        <Table class="mt-3">
          <TableHeader>
            <TableRow>
              <TableHead>Material</TableHead>
              <TableHead>Cantidad</TableHead>
              <TableHead>CMP</TableHead>
              <TableHead>Valor</TableHead>
              <TableHead>Alerta</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in almacen?.stock ?? []" :key="item.materialId">
              <TableCell>{{ item.codigo }} — {{ item.descripcion }}</TableCell>
              <TableCell>{{ qtyFmt.format(item.cantidad) }} {{ item.unidad }}</TableCell>
              <TableCell>{{ money.format(item.costoPromedio) }}</TableCell>
              <TableCell>{{ money.format(item.valor) }}</TableCell>
              <TableCell>
                <Badge v-if="item.criticoSinStock" variant="secondary">Crítico sin stock</Badge>
                <Badge v-else-if="item.quiebre" variant="secondary">Quiebre</Badge>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!(almacen?.stock.length)" :colspan="5">Sin materiales en catálogo.</TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div v-if="!almacenRol" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Vale de salida</h3>
        <div class="mt-3 grid gap-3 sm:grid-cols-3">
          <select v-model="valePartidaId" :class="selectClass">
            <option value="">Partida vigente</option>
            <option v-for="partida in partidas" :key="partida.id" :value="String(partida.id)">
              {{ partida.codigo }} — {{ partida.descripcion }}
            </option>
          </select>
          <select v-model="valeItems[0].materialId" :class="selectClass">
            <option value="">Material</option>
            <option v-for="mat in materiales" :key="mat.id" :value="String(mat.id)">{{ mat.codigo }} — {{ mat.descripcion }}</option>
          </select>
          <Input v-model="valeItems[0].cantidad" :class="inputClass" placeholder="Cantidad" />
        </div>
        <Button class="mt-3 bg-palette-navy text-white hover:bg-palette-royal" type="button" :disabled="saving" @click="onCrearVale">
          Registrar vale
        </Button>
        <Table class="mt-4">
          <TableHeader>
            <TableRow>
              <TableHead>N°</TableHead>
              <TableHead>Partida</TableHead>
              <TableHead>Costo</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="vale in vales" :key="vale.id">
              <TableCell>{{ vale.numero }}</TableCell>
              <TableCell>{{ vale.codigoPartida }} — {{ vale.descripcionPartida }}</TableCell>
              <TableCell>{{ money.format(vale.costoTotal) }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" :disabled="saving" @click="onDevolverVale(vale.id)">Devolver</Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!vales.length" :colspan="4">Aún no hay vales.</TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div v-if="!almacenRol" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Ajuste y cierre</h3>
        <div class="mt-3 grid gap-3 sm:grid-cols-4">
          <select v-model="ajuste.materialId" :class="selectClass">
            <option value="">Material</option>
            <option v-for="mat in materiales" :key="mat.id" :value="String(mat.id)">{{ mat.codigo }}</option>
          </select>
          <Input v-model="ajuste.cantidad" :class="inputClass" placeholder="Cantidad (+/-)" />
          <Input v-model="ajuste.observaciones" :class="inputClass" placeholder="Observación" />
          <Button variant="outline" type="button" :disabled="saving" @click="onAjuste">Ajustar</Button>
        </div>
        <div class="mt-4 grid gap-3 sm:grid-cols-4">
          <Input v-model="cierreAnio" :class="inputClass" placeholder="Año" />
          <Input v-model="cierreMes" :class="inputClass" placeholder="Mes" />
          <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="button" :disabled="saving" @click="onCerrarMes">
            Cerrar mes
          </Button>
        </div>
        <p v-if="cierres[0]" class="mt-3 text-sm text-slate-600">
          Último cierre: {{ cierres[0].mes }}/{{ cierres[0].anio }} · {{ money.format(cierres[0].valorTotal) }}
        </p>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="text-base font-semibold text-palette-navy">Movimientos</h3>
        <Table class="mt-3">
          <TableHeader>
            <TableRow>
              <TableHead>Fecha</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Material</TableHead>
              <TableHead>Cantidad</TableHead>
              <TableHead>Saldo</TableHead>
              <TableHead>CMP</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in kardex" :key="item.id">
              <TableCell>{{ item.fecha }}</TableCell>
              <TableCell>{{ item.tipo }}</TableCell>
              <TableCell>{{ item.codigoMaterial }}</TableCell>
              <TableCell>{{ qtyFmt.format(item.cantidad) }}</TableCell>
              <TableCell>{{ qtyFmt.format(item.saldoCantidad) }}</TableCell>
              <TableCell>{{ money.format(item.costoPromedio) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!kardex.length" :colspan="6">Sin movimientos.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>
  </div>
</template>
