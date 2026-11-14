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
import { Textarea } from '@/components/ui/textarea'
import {
  actualizarCuadrillaObra,
  actualizarEquipoObra,
  actualizarSubcontratoObra,
  agregarMiembroCuadrilla,
  crearCuadrillaObra,
  crearEquipoObra,
  crearSubcontratoObra,
  eliminarAvanceSubcontrato,
  eliminarCuadrillaObra,
  eliminarImputacionEquipo,
  listarAvancesSubcontrato,
  listarCuadrillasObra,
  listarEquiposObra,
  listarImputacionesEquipo,
  listarPersonalObra,
  listarSubcontratosObra,
  obtenerPresupuesto,
  quitarMiembroCuadrilla,
  registrarAvanceSubcontrato,
  registrarImputacionEquipo,
} from '@/services/construccion/construccionService'
import { listarBloqueosPreUso } from '@/services/soma/somaService'
import type {
  AsignacionEquipo,
  CuadrillaObra,
  ImputacionEquipo,
  ModoTarifaEquipo,
  OrigenEquipo,
  PartidaPresupuesto,
  PersonalObra,
  SubcontratoAvance,
  SubcontratoObra,
} from '@/types/construccion'
import { fieldInputClass, fieldSelectClass, tabsListClass } from './fieldClasses'

type TabRecursos = 'equipo' | 'cuadrillas' | 'equipos' | 'subcontratos'

const inputClass = fieldInputClass
const selectClass = fieldSelectClass

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const qtyFmt = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 0, maximumFractionDigits: 4 })
const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const tab = ref<TabRecursos>('equipo')
const loading = ref(true)
const saving = ref(false)
const partidas = ref<PartidaPresupuesto[]>([])
const equipos = ref<AsignacionEquipo[]>([])
const subcontratos = ref<SubcontratoObra[]>([])
const personal = ref<PersonalObra[]>([])
const cuadrillas = ref<CuadrillaObra[]>([])
const equipoSeleccionadoId = ref<number | null>(null)
const subcontratoSeleccionadoId = ref<number | null>(null)
const cuadrillaSeleccionadaId = ref<number | null>(null)
const imputaciones = ref<ImputacionEquipo[]>([])
const avances = ref<SubcontratoAvance[]>([])
const personaAAgregar = ref('')

const equipoForm = reactive({
  id: null as number | null,
  codigo: '',
  descripcion: '',
  origen: 'PROPIO' as OrigenEquipo,
  proveedorRazon: '',
  proveedorRuc: '',
  fechaIngreso: '',
  fechaSalida: '',
  modoTarifa: 'DIA' as ModoTarifaEquipo,
  tarifa: '',
  partidaId: '',
  observaciones: '',
})

const imputacionForm = reactive({
  fecha: '',
  cantidad: '',
  partidaId: '',
  observaciones: '',
})

const subcontratoForm = reactive({
  id: null as number | null,
  especialidad: '',
  subcontratistaRazon: '',
  subcontratistaRuc: '',
  montoContratado: '',
  fechaInicio: '',
  fechaFin: '',
  partidaId: '',
  observaciones: '',
})

const avanceForm = reactive({
  fecha: '',
  monto: '',
  partidaId: '',
  observaciones: '',
})

const cuadrillaForm = reactive({
  id: null as number | null,
  codigo: '',
  nombre: '',
  centroCosto: '',
  observaciones: '',
})

const equipoSeleccionado = computed(
  () => equipos.value.find((item) => item.id === equipoSeleccionadoId.value) ?? null,
)
const subcontratoSeleccionado = computed(
  () => subcontratos.value.find((item) => item.id === subcontratoSeleccionadoId.value) ?? null,
)
const cuadrillaSeleccionada = computed(
  () => cuadrillas.value.find((item) => item.id === cuadrillaSeleccionadaId.value) ?? null,
)
const cuadrillaPorPersona = computed(() => {
  const map = new Map<number, string>()
  for (const cuadrilla of cuadrillas.value) {
    for (const miembro of cuadrilla.miembros ?? []) {
      map.set(miembro.personaId, `${cuadrilla.codigo} — ${cuadrilla.nombre}`)
    }
  }
  return map
})
const personalSinCuadrilla = computed(() => {
  const asignados = new Set(
    cuadrillas.value.flatMap((item) => (item.miembros ?? []).map((miembro) => miembro.personaId)),
  )
  return personal.value.filter((item) => !asignados.has(item.personaId))
})

function n(valor: number | string | null | undefined): number {
  const parsed = typeof valor === 'number' ? valor : Number(valor)
  return Number.isFinite(parsed) ? parsed : 0
}

function partidaLabel(item: PartidaPresupuesto): string {
  return `${item.codigo} — ${item.descripcion}`
}

function origenLabel(origen: OrigenEquipo): string {
  return origen === 'ALQUILADO' ? 'Alquilado' : 'Propio'
}

function tarifaLabel(modo: ModoTarifaEquipo): string {
  return modo === 'HORA' ? 'Hora' : 'Día'
}

function categoriaLabel(categoria: string | null | undefined): string {
  if (categoria === 'OPERARIO') return 'Operario'
  if (categoria === 'OFICIAL') return 'Oficial'
  if (categoria === 'PEON') return 'Peón'
  return '—'
}

function personaLabel(item: { apellidos: string; nombres: string; numeroDocumento: string }): string {
  return `${item.apellidos} ${item.nombres} · ${item.numeroDocumento}`
}

function resetEquipoForm() {
  equipoForm.id = null
  equipoForm.codigo = ''
  equipoForm.descripcion = ''
  equipoForm.origen = 'PROPIO'
  equipoForm.proveedorRazon = ''
  equipoForm.proveedorRuc = ''
  equipoForm.fechaIngreso = ''
  equipoForm.fechaSalida = ''
  equipoForm.modoTarifa = 'DIA'
  equipoForm.tarifa = ''
  equipoForm.partidaId = ''
  equipoForm.observaciones = ''
}

function resetSubcontratoForm() {
  subcontratoForm.id = null
  subcontratoForm.especialidad = ''
  subcontratoForm.subcontratistaRazon = ''
  subcontratoForm.subcontratistaRuc = ''
  subcontratoForm.montoContratado = ''
  subcontratoForm.fechaInicio = ''
  subcontratoForm.fechaFin = ''
  subcontratoForm.partidaId = ''
  subcontratoForm.observaciones = ''
}

function resetCuadrillaForm() {
  cuadrillaForm.id = null
  cuadrillaForm.codigo = ''
  cuadrillaForm.nombre = ''
  cuadrillaForm.centroCosto = ''
  cuadrillaForm.observaciones = ''
}

function parseMonto(texto: string, etiqueta: string): number {
  const valor = Number(texto.trim().replace(',', '.'))
  if (!Number.isFinite(valor) || valor < 0) {
    throw new Error(`${etiqueta} inválida.`)
  }
  return valor
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    const [listaEquipos, listaSub, listaPersonal, listaCuadrillas, pres] = await Promise.all([
      listarEquiposObra(proyectoId.value),
      listarSubcontratosObra(proyectoId.value),
      listarPersonalObra(proyectoId.value).catch(() => []),
      listarCuadrillasObra(proyectoId.value),
      obtenerPresupuesto(proyectoId.value).catch(() => null),
    ])
    equipos.value = listaEquipos
    subcontratos.value = listaSub
    personal.value = listaPersonal
    cuadrillas.value = listaCuadrillas
    partidas.value = pres?.version?.estado === 'VIGENTE' ? pres.version.partidas : []
    if (cuadrillaSeleccionadaId.value) {
      const actual = listaCuadrillas.find((item) => item.id === cuadrillaSeleccionadaId.value)
      if (!actual) {
        cuadrillaSeleccionadaId.value = null
      }
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los recursos.')
  } finally {
    loading.value = false
  }
}

async function loadImputaciones(equipoId: number) {
  imputaciones.value = await listarImputacionesEquipo(proyectoId.value, equipoId)
}

async function loadAvances(subcontratoId: number) {
  avances.value = await listarAvancesSubcontrato(proyectoId.value, subcontratoId)
}

async function seleccionarEquipo(item: AsignacionEquipo) {
  equipoSeleccionadoId.value = item.id
  try {
    await loadImputaciones(item.id)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las imputaciones.')
  }
}

async function seleccionarSubcontrato(item: SubcontratoObra) {
  subcontratoSeleccionadoId.value = item.id
  try {
    await loadAvances(item.id)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los avances.')
  }
}

function editarEquipo(item: AsignacionEquipo) {
  equipoForm.id = item.id
  equipoForm.codigo = item.codigo
  equipoForm.descripcion = item.descripcion
  equipoForm.origen = item.origen
  equipoForm.proveedorRazon = item.proveedorRazon ?? ''
  equipoForm.proveedorRuc = item.proveedorRuc ?? ''
  equipoForm.fechaIngreso = item.fechaIngreso
  equipoForm.fechaSalida = item.fechaSalida ?? ''
  equipoForm.modoTarifa = item.modoTarifa
  equipoForm.tarifa = String(item.tarifa)
  equipoForm.partidaId = item.partidaId == null ? '' : String(item.partidaId)
  equipoForm.observaciones = item.observaciones ?? ''
  void seleccionarEquipo(item)
}

function editarSubcontrato(item: SubcontratoObra) {
  subcontratoForm.id = item.id
  subcontratoForm.especialidad = item.especialidad
  subcontratoForm.subcontratistaRazon = item.subcontratistaRazon
  subcontratoForm.subcontratistaRuc = item.subcontratistaRuc ?? ''
  subcontratoForm.montoContratado = String(item.montoContratado)
  subcontratoForm.fechaInicio = item.fechaInicio ?? ''
  subcontratoForm.fechaFin = item.fechaFin ?? ''
  subcontratoForm.partidaId = item.partidaId == null ? '' : String(item.partidaId)
  subcontratoForm.observaciones = item.observaciones ?? ''
  void seleccionarSubcontrato(item)
}

async function onGuardarEquipo() {
  saving.value = true
  try {
    const tarifa = parseMonto(equipoForm.tarifa || '0', 'La tarifa')
    const request = {
      codigo: equipoForm.codigo.trim(),
      descripcion: equipoForm.descripcion.trim(),
      origen: equipoForm.origen,
      proveedorRazon: equipoForm.origen === 'ALQUILADO' ? equipoForm.proveedorRazon.trim() : null,
      proveedorRuc: equipoForm.origen === 'ALQUILADO' ? equipoForm.proveedorRuc.trim() || null : null,
      fechaIngreso: equipoForm.fechaIngreso,
      fechaSalida: equipoForm.fechaSalida || null,
      modoTarifa: equipoForm.modoTarifa,
      tarifa,
      partidaId: equipoForm.partidaId ? Number(equipoForm.partidaId) : null,
      observaciones: equipoForm.observaciones.trim() || null,
    }
    const dto = equipoForm.id
      ? await actualizarEquipoObra(proyectoId.value, equipoForm.id, request)
      : await crearEquipoObra(proyectoId.value, request)
    toast.success(equipoForm.id ? 'Asignación actualizada.' : 'Equipo asignado.')
    resetEquipoForm()
    await load()
    await seleccionarEquipo(dto)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el equipo.')
  } finally {
    saving.value = false
  }
}

async function onImputar() {
  if (!equipoSeleccionadoId.value) {
    toast.error('Seleccione un equipo.')
    return
  }
  saving.value = true
  try {
    const cantidad = Number(imputacionForm.cantidad.trim().replace(',', '.'))
    if (!Number.isFinite(cantidad) || cantidad <= 0) {
      throw new Error('La cantidad debe ser mayor que cero.')
    }
    await registrarImputacionEquipo(proyectoId.value, equipoSeleccionadoId.value, {
      fecha: imputacionForm.fecha,
      cantidad,
      partidaId: imputacionForm.partidaId ? Number(imputacionForm.partidaId) : null,
      observaciones: imputacionForm.observaciones.trim() || null,
    })
    imputacionForm.fecha = ''
    imputacionForm.cantidad = ''
    imputacionForm.partidaId = ''
    imputacionForm.observaciones = ''
    toast.success('Imputación registrada.')
    await load()
    await loadImputaciones(equipoSeleccionadoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la imputación.')
  } finally {
    saving.value = false
  }
}

async function onEliminarImputacion(item: ImputacionEquipo) {
  if (!equipoSeleccionadoId.value) {
    return
  }
  saving.value = true
  try {
    await eliminarImputacionEquipo(proyectoId.value, equipoSeleccionadoId.value, item.id)
    toast.success('Imputación eliminada.')
    await load()
    await loadImputaciones(equipoSeleccionadoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo eliminar la imputación.')
  } finally {
    saving.value = false
  }
}

async function onGuardarSubcontrato() {
  saving.value = true
  try {
    const monto = Number(subcontratoForm.montoContratado.trim().replace(',', '.'))
    if (!Number.isFinite(monto) || monto <= 0) {
      throw new Error('El monto contratado debe ser mayor que cero.')
    }
    const request = {
      especialidad: subcontratoForm.especialidad.trim(),
      subcontratistaRazon: subcontratoForm.subcontratistaRazon.trim(),
      subcontratistaRuc: subcontratoForm.subcontratistaRuc.trim() || null,
      montoContratado: monto,
      fechaInicio: subcontratoForm.fechaInicio || null,
      fechaFin: subcontratoForm.fechaFin || null,
      partidaId: subcontratoForm.partidaId ? Number(subcontratoForm.partidaId) : null,
      observaciones: subcontratoForm.observaciones.trim() || null,
    }
    const dto = subcontratoForm.id
      ? await actualizarSubcontratoObra(proyectoId.value, subcontratoForm.id, request)
      : await crearSubcontratoObra(proyectoId.value, request)
    toast.success(subcontratoForm.id ? 'Subcontrato actualizado.' : 'Subcontrato registrado.')
    resetSubcontratoForm()
    await load()
    await seleccionarSubcontrato(dto)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el subcontrato.')
  } finally {
    saving.value = false
  }
}

async function onRegistrarAvance() {
  if (!subcontratoSeleccionadoId.value) {
    toast.error('Seleccione un subcontrato.')
    return
  }
  saving.value = true
  try {
    const monto = Number(avanceForm.monto.trim().replace(',', '.'))
    if (!Number.isFinite(monto) || monto <= 0) {
      throw new Error('El monto del avance debe ser mayor que cero.')
    }
    await registrarAvanceSubcontrato(proyectoId.value, subcontratoSeleccionadoId.value, {
      fecha: avanceForm.fecha,
      monto,
      partidaId: avanceForm.partidaId ? Number(avanceForm.partidaId) : null,
      observaciones: avanceForm.observaciones.trim() || null,
    })
    avanceForm.fecha = ''
    avanceForm.monto = ''
    avanceForm.partidaId = ''
    avanceForm.observaciones = ''
    toast.success('Avance registrado.')
    await load()
    await loadAvances(subcontratoSeleccionadoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el avance.')
  } finally {
    saving.value = false
  }
}

async function onEliminarAvance(item: SubcontratoAvance) {
  if (!subcontratoSeleccionadoId.value) {
    return
  }
  saving.value = true
  try {
    await eliminarAvanceSubcontrato(proyectoId.value, subcontratoSeleccionadoId.value, item.id)
    toast.success('Avance eliminado.')
    await load()
    await loadAvances(subcontratoSeleccionadoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo eliminar el avance.')
  } finally {
    saving.value = false
  }
}

function seleccionarCuadrilla(item: CuadrillaObra) {
  cuadrillaSeleccionadaId.value = item.id
  personaAAgregar.value = ''
}

function editarCuadrilla(item: CuadrillaObra) {
  cuadrillaForm.id = item.id
  cuadrillaForm.codigo = item.codigo
  cuadrillaForm.nombre = item.nombre
  cuadrillaForm.centroCosto = item.centroCosto ?? ''
  cuadrillaForm.observaciones = item.observaciones ?? ''
  seleccionarCuadrilla(item)
}

async function onGuardarCuadrilla() {
  saving.value = true
  try {
    const request = {
      codigo: cuadrillaForm.codigo.trim(),
      nombre: cuadrillaForm.nombre.trim(),
      centroCosto: cuadrillaForm.centroCosto.trim() || null,
      observaciones: cuadrillaForm.observaciones.trim() || null,
    }
    const dto = cuadrillaForm.id
      ? await actualizarCuadrillaObra(proyectoId.value, cuadrillaForm.id, request)
      : await crearCuadrillaObra(proyectoId.value, request)
    toast.success(cuadrillaForm.id ? 'Cuadrilla actualizada.' : 'Cuadrilla creada.')
    resetCuadrillaForm()
    await load()
    seleccionarCuadrilla(dto)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la cuadrilla.')
  } finally {
    saving.value = false
  }
}

async function onEliminarCuadrilla() {
  if (!cuadrillaForm.id) {
    return
  }
  saving.value = true
  try {
    await eliminarCuadrillaObra(proyectoId.value, cuadrillaForm.id)
    toast.success('Cuadrilla eliminada.')
    resetCuadrillaForm()
    cuadrillaSeleccionadaId.value = null
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo eliminar la cuadrilla.')
  } finally {
    saving.value = false
  }
}

async function onAgregarMiembro() {
  if (!cuadrillaSeleccionadaId.value) {
    toast.error('Seleccione una cuadrilla.')
    return
  }
  const personaId = Number(personaAAgregar.value)
  if (!Number.isFinite(personaId) || personaId <= 0) {
    toast.error('Seleccione un obrero.')
    return
  }
  saving.value = true
  try {
    const dto = await agregarMiembroCuadrilla(proyectoId.value, cuadrillaSeleccionadaId.value, { personaId })
    toast.success('Obrero asignado a la cuadrilla.')
    personaAAgregar.value = ''
    await load()
    seleccionarCuadrilla(dto)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo asignar el obrero.')
  } finally {
    saving.value = false
  }
}

async function onQuitarMiembro(personaId: number) {
  if (!cuadrillaSeleccionadaId.value) {
    return
  }
  saving.value = true
  try {
    await quitarMiembroCuadrilla(proyectoId.value, cuadrillaSeleccionadaId.value, personaId)
    toast.success('Obrero retirado de la cuadrilla.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo retirar el obrero.')
  } finally {
    saving.value = false
  }
}

watch([equipoSeleccionadoId, () => imputacionForm.fecha], async ([equipoId, fecha]) => {
  if (!equipoId || !fecha) {
    return
  }
  try {
    const bloqueos = await listarBloqueosPreUso(proyectoId.value, fecha)
    if (bloqueos.some((item) => item.asignacionId === equipoId)) {
      toast.warning('El equipo está inoperativo / bloqueado ese día según el pre-uso SOMA. Puede imputar igual.')
    }
  } catch {
    /* el aviso no debe impedir imputar */
  }
})

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs :model-value="tab" @update:model-value="(value) => (tab = String(value) as TabRecursos)">
      <TabsList :class="tabsListClass">
        <TabsTrigger value="equipo" class="h-9 rounded-lg px-4 text-sm">Equipo de obra</TabsTrigger>
        <TabsTrigger value="cuadrillas" class="h-9 rounded-lg px-4 text-sm">Cuadrillas</TabsTrigger>
        <TabsTrigger value="equipos" class="h-9 rounded-lg px-4 text-sm">Equipos</TabsTrigger>
        <TabsTrigger value="subcontratos" class="h-9 rounded-lg px-4 text-sm">Subcontratos</TabsTrigger>
      </TabsList>
    </Tabs>

    <div v-if="loading" class="space-y-3">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <div v-else-if="tab === 'equipo'" class="space-y-4">
      <p class="text-sm text-slate-500">
        Vista unificada del personal RCC, equipos y subcontratos. El alta de obreros se hace en Gestión Humana → Contratos.
      </p>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">Personal</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Obrero</TableHead>
              <TableHead>Documento</TableHead>
              <TableHead>Categoría</TableHead>
              <TableHead>Cuadrilla</TableHead>
              <TableHead>Ingreso</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in personal" :key="item.personaId">
              <TableCell>{{ item.apellidos }} {{ item.nombres }}</TableCell>
              <TableCell>{{ item.numeroDocumento }}</TableCell>
              <TableCell>{{ categoriaLabel(item.categoria) }}</TableCell>
              <TableCell>{{ cuadrillaPorPersona.get(item.personaId) || 'Sin cuadrilla' }}</TableCell>
              <TableCell>{{ item.fechaIngreso || '—' }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!personal.length" :colspan="5">Sin personal activo. Asigne el contrato RCC a esta obra.</TableEmpty>
          </TableBody>
        </Table>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">Equipos</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead>Origen</TableHead>
              <TableHead>Imputado</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in equipos" :key="item.id">
              <TableCell>{{ item.codigo }}</TableCell>
              <TableCell>{{ item.descripcion }}</TableCell>
              <TableCell>{{ origenLabel(item.origen) }}</TableCell>
              <TableCell>{{ money.format(n(item.costoImputado)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!equipos.length" :colspan="4">Sin equipos asignados.</TableEmpty>
          </TableBody>
        </Table>
      </div>
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">Subcontratos</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Especialidad</TableHead>
              <TableHead>Subcontratista</TableHead>
              <TableHead>Contratado</TableHead>
              <TableHead>Saldo</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in subcontratos" :key="item.id">
              <TableCell>{{ item.especialidad }}</TableCell>
              <TableCell>{{ item.subcontratistaRazon }}</TableCell>
              <TableCell>{{ money.format(n(item.montoContratado)) }}</TableCell>
              <TableCell>{{ money.format(n(item.saldo)) }}</TableCell>
            </TableRow>
            <TableEmpty v-if="!subcontratos.length" :colspan="4">Sin subcontratos.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else-if="tab === 'cuadrillas'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">
          {{ cuadrillaForm.id ? 'Editar cuadrilla' : 'Nueva cuadrilla' }}
        </h3>
        <form class="grid gap-3 md:grid-cols-3" @submit.prevent="onGuardarCuadrilla">
          <div>
            <label class="text-xs text-slate-500" for="cu-codigo">Código</label>
            <Input
              id="cu-codigo"
              v-model="cuadrillaForm.codigo"
              maxlength="40"
              placeholder="EST"
              :class="inputClass"
              required
            />
          </div>
          <div class="md:col-span-2">
            <label class="text-xs text-slate-500" for="cu-nombre">Nombre</label>
            <Input
              id="cu-nombre"
              v-model="cuadrillaForm.nombre"
              maxlength="160"
              placeholder="Estructura, Acabados, Instalaciones…"
              :class="inputClass"
              required
            />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="cu-cc">Centro de costo</label>
            <Input id="cu-cc" v-model="cuadrillaForm.centroCosto" maxlength="40" :class="inputClass" />
          </div>
          <div class="md:col-span-2">
            <label class="text-xs text-slate-500" for="cu-obs">Observaciones</label>
            <Textarea id="cu-obs" v-model="cuadrillaForm.observaciones" class="min-h-16" />
          </div>
          <div class="flex flex-wrap gap-2 md:col-span-3">
            <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="saving">
              {{ cuadrillaForm.id ? 'Guardar cambios' : 'Crear' }}
            </Button>
            <Button v-if="cuadrillaForm.id" variant="outline" type="button" :disabled="saving" @click="resetCuadrillaForm">
              Cancelar
            </Button>
            <Button
              v-if="cuadrillaForm.id"
              variant="outline"
              type="button"
              :disabled="saving"
              @click="onEliminarCuadrilla"
            >
              Eliminar
            </Button>
          </div>
        </form>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">Cuadrillas</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Nombre</TableHead>
              <TableHead>Centro</TableHead>
              <TableHead>Miembros</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="item in cuadrillas"
              :key="item.id"
              class="cursor-pointer"
              :class="item.id === cuadrillaSeleccionadaId ? 'bg-sky-50' : ''"
              @click="seleccionarCuadrilla(item)"
            >
              <TableCell>{{ item.codigo }}</TableCell>
              <TableCell>{{ item.nombre }}</TableCell>
              <TableCell>{{ item.centroCosto || '—' }}</TableCell>
              <TableCell>{{ item.miembros?.length ?? 0 }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" @click.stop="editarCuadrilla(item)">Editar</Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!cuadrillas.length" :colspan="5">Sin cuadrillas.</TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div v-if="cuadrillaSeleccionada" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-1 text-base font-semibold text-palette-navy">
          Miembros · {{ cuadrillaSeleccionada.codigo }} {{ cuadrillaSeleccionada.nombre }}
        </h3>
        <p class="mb-3 text-sm text-slate-500">Un obrero solo puede estar en una cuadrilla a la vez. Si ya está en otra, se mueve.</p>
        <form class="mb-4 flex flex-wrap items-end gap-3" @submit.prevent="onAgregarMiembro">
          <div class="min-w-64 flex-1">
            <label class="text-xs text-slate-500" for="cu-persona">Obrero sin cuadrilla</label>
            <select id="cu-persona" v-model="personaAAgregar" :class="selectClass">
              <option value="">Seleccione</option>
              <option v-for="item in personalSinCuadrilla" :key="item.personaId" :value="String(item.personaId)">
                {{ personaLabel(item) }}
              </option>
            </select>
          </div>
          <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="saving">
            Asignar
          </Button>
        </form>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Obrero</TableHead>
              <TableHead>Documento</TableHead>
              <TableHead>Categoría</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in cuadrillaSeleccionada.miembros" :key="item.personaId">
              <TableCell>{{ item.apellidos }} {{ item.nombres }}</TableCell>
              <TableCell>{{ item.numeroDocumento }}</TableCell>
              <TableCell>{{ categoriaLabel(item.categoria) }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" :disabled="saving" @click="onQuitarMiembro(item.personaId)">
                  Quitar
                </Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!cuadrillaSeleccionada.miembros?.length" :colspan="4">Sin miembros.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else-if="tab === 'equipos'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">
          {{ equipoForm.id ? 'Editar asignación' : 'Asignar equipo' }}
        </h3>
        <form class="grid gap-3 md:grid-cols-3" @submit.prevent="onGuardarEquipo">
          <div>
            <label class="text-xs text-slate-500" for="eq-codigo">Código</label>
            <Input id="eq-codigo" v-model="equipoForm.codigo" maxlength="40" :class="inputClass" required />
          </div>
          <div class="md:col-span-2">
            <label class="text-xs text-slate-500" for="eq-desc">Descripción</label>
            <Input id="eq-desc" v-model="equipoForm.descripcion" maxlength="240" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="eq-origen">Origen</label>
            <select id="eq-origen" v-model="equipoForm.origen" :class="selectClass">
              <option value="PROPIO">Propio</option>
              <option value="ALQUILADO">Alquilado</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500" for="eq-modo">Tarifa</label>
            <select id="eq-modo" v-model="equipoForm.modoTarifa" :class="selectClass">
              <option value="DIA">Por día</option>
              <option value="HORA">Por hora</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500" for="eq-tarifa">Monto tarifa</label>
            <Input id="eq-tarifa" v-model="equipoForm.tarifa" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="eq-ingreso">Ingreso</label>
            <Input id="eq-ingreso" v-model="equipoForm.fechaIngreso" type="date" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="eq-salida">Salida</label>
            <Input id="eq-salida" v-model="equipoForm.fechaSalida" type="date" :class="inputClass" />
          </div>
          <div class="md:col-span-2">
            <label class="text-xs text-slate-500" for="eq-partida">Partida</label>
            <select id="eq-partida" v-model="equipoForm.partidaId" :class="selectClass">
              <option value="">Sin partida</option>
              <option v-for="partida in partidas" :key="partida.id ?? partida.codigo" :value="String(partida.id)">
                {{ partidaLabel(partida) }}
              </option>
            </select>
          </div>
          <template v-if="equipoForm.origen === 'ALQUILADO'">
            <div>
              <label class="text-xs text-slate-500" for="eq-prov">Proveedor</label>
              <Input id="eq-prov" v-model="equipoForm.proveedorRazon" maxlength="160" :class="inputClass" required />
            </div>
            <div>
              <label class="text-xs text-slate-500" for="eq-ruc">RUC</label>
              <Input id="eq-ruc" v-model="equipoForm.proveedorRuc" maxlength="11" :class="inputClass" />
            </div>
          </template>
          <div class="md:col-span-3">
            <label class="text-xs text-slate-500" for="eq-obs">Observaciones</label>
            <Textarea id="eq-obs" v-model="equipoForm.observaciones" class="min-h-16" />
          </div>
          <div class="flex flex-wrap gap-2 md:col-span-3">
            <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="saving">
              {{ equipoForm.id ? 'Guardar cambios' : 'Asignar' }}
            </Button>
            <Button v-if="equipoForm.id" variant="outline" type="button" :disabled="saving" @click="resetEquipoForm">
              Cancelar
            </Button>
          </div>
        </form>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">Equipos asignados</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead>Origen</TableHead>
              <TableHead>Tarifa</TableHead>
              <TableHead>Ingreso</TableHead>
              <TableHead>Imputado</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="item in equipos"
              :key="item.id"
              class="cursor-pointer"
              :class="item.id === equipoSeleccionadoId ? 'bg-sky-50' : ''"
              @click="seleccionarEquipo(item)"
            >
              <TableCell>{{ item.codigo }}</TableCell>
              <TableCell>{{ item.descripcion }}</TableCell>
              <TableCell>
                <Badge variant="secondary">{{ origenLabel(item.origen) }}</Badge>
              </TableCell>
              <TableCell>{{ money.format(n(item.tarifa)) }} / {{ tarifaLabel(item.modoTarifa) }}</TableCell>
              <TableCell>{{ item.fechaIngreso }}</TableCell>
              <TableCell>{{ money.format(n(item.costoImputado)) }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" @click.stop="editarEquipo(item)">Editar</Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!equipos.length" :colspan="7">Sin equipos asignados.</TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div v-if="equipoSeleccionado" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-1 text-base font-semibold text-palette-navy">
          Imputaciones · {{ equipoSeleccionado.codigo }}
        </h3>
        <p class="mb-3 text-sm text-slate-500">
          El costo real solo se registra con imputaciones explícitas (cantidad × tarifa).
        </p>
        <form class="mb-4 grid gap-3 md:grid-cols-2 lg:grid-cols-4" @submit.prevent="onImputar">
          <div>
            <label class="text-xs text-slate-500" for="imp-fecha">Fecha</label>
            <Input id="imp-fecha" v-model="imputacionForm.fecha" type="date" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="imp-cant">Cantidad</label>
            <Input id="imp-cant" v-model="imputacionForm.cantidad" :class="inputClass" required />
          </div>
          <div class="md:col-span-2">
            <label class="text-xs text-slate-500" for="imp-partida">Partida</label>
            <select id="imp-partida" v-model="imputacionForm.partidaId" :class="selectClass">
              <option value="">Cabecera / sin partida</option>
              <option v-for="partida in partidas" :key="partida.id ?? partida.codigo" :value="String(partida.id)">
                {{ partidaLabel(partida) }}
              </option>
            </select>
          </div>
          <div class="flex items-end">
            <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="saving">
              Registrar
            </Button>
          </div>
        </form>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Fecha</TableHead>
              <TableHead>Cantidad</TableHead>
              <TableHead>Tarifa</TableHead>
              <TableHead>Costo</TableHead>
              <TableHead>Centro</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in imputaciones" :key="item.id">
              <TableCell>{{ item.fecha }}</TableCell>
              <TableCell>{{ qtyFmt.format(n(item.cantidad)) }}</TableCell>
              <TableCell>{{ money.format(n(item.tarifa)) }}</TableCell>
              <TableCell>{{ money.format(n(item.costoTotal)) }}</TableCell>
              <TableCell>{{ item.centroCosto || '—' }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" :disabled="saving" @click="onEliminarImputacion(item)">
                  Eliminar
                </Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!imputaciones.length" :colspan="6">Sin imputaciones.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>

    <div v-else-if="tab === 'subcontratos'" class="space-y-4">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">
          {{ subcontratoForm.id ? 'Editar subcontrato' : 'Registrar subcontrato' }}
        </h3>
        <form class="grid gap-3 md:grid-cols-3" @submit.prevent="onGuardarSubcontrato">
          <div>
            <label class="text-xs text-slate-500" for="sub-esp">Especialidad</label>
            <Input id="sub-esp" v-model="subcontratoForm.especialidad" maxlength="160" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="sub-razon">Subcontratista</label>
            <Input id="sub-razon" v-model="subcontratoForm.subcontratistaRazon" maxlength="160" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="sub-ruc">RUC</label>
            <Input id="sub-ruc" v-model="subcontratoForm.subcontratistaRuc" maxlength="11" :class="inputClass" />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="sub-monto">Monto contratado</label>
            <Input id="sub-monto" v-model="subcontratoForm.montoContratado" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="sub-ini">Inicio</label>
            <Input id="sub-ini" v-model="subcontratoForm.fechaInicio" type="date" :class="inputClass" />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="sub-fin">Fin</label>
            <Input id="sub-fin" v-model="subcontratoForm.fechaFin" type="date" :class="inputClass" />
          </div>
          <div class="md:col-span-2">
            <label class="text-xs text-slate-500" for="sub-partida">Partida</label>
            <select id="sub-partida" v-model="subcontratoForm.partidaId" :class="selectClass">
              <option value="">Sin partida</option>
              <option v-for="partida in partidas" :key="partida.id ?? partida.codigo" :value="String(partida.id)">
                {{ partidaLabel(partida) }}
              </option>
            </select>
          </div>
          <div class="md:col-span-3">
            <label class="text-xs text-slate-500" for="sub-obs">Observaciones</label>
            <Textarea id="sub-obs" v-model="subcontratoForm.observaciones" class="min-h-16" />
          </div>
          <div class="flex flex-wrap gap-2 md:col-span-3">
            <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="saving">
              {{ subcontratoForm.id ? 'Guardar cambios' : 'Registrar' }}
            </Button>
            <Button v-if="subcontratoForm.id" variant="outline" type="button" :disabled="saving" @click="resetSubcontratoForm">
              Cancelar
            </Button>
          </div>
        </form>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-3 text-base font-semibold text-palette-navy">Subcontratos</h3>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Especialidad</TableHead>
              <TableHead>Subcontratista</TableHead>
              <TableHead>Contratado</TableHead>
              <TableHead>Avanzado</TableHead>
              <TableHead>Saldo</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="item in subcontratos"
              :key="item.id"
              class="cursor-pointer"
              :class="item.id === subcontratoSeleccionadoId ? 'bg-sky-50' : ''"
              @click="seleccionarSubcontrato(item)"
            >
              <TableCell>{{ item.especialidad }}</TableCell>
              <TableCell>{{ item.subcontratistaRazon }}</TableCell>
              <TableCell>{{ money.format(n(item.montoContratado)) }}</TableCell>
              <TableCell>{{ money.format(n(item.montoAvanzado)) }}</TableCell>
              <TableCell>{{ money.format(n(item.saldo)) }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" @click.stop="editarSubcontrato(item)">Editar</Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!subcontratos.length" :colspan="6">Sin subcontratos.</TableEmpty>
          </TableBody>
        </Table>
      </div>

      <div v-if="subcontratoSeleccionado" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <h3 class="mb-1 text-base font-semibold text-palette-navy">
          Avances · {{ subcontratoSeleccionado.especialidad }}
        </h3>
        <p class="mb-3 text-sm text-slate-500">
          Saldo disponible: {{ money.format(n(subcontratoSeleccionado.saldo)) }}.
        </p>
        <form class="mb-4 grid gap-3 md:grid-cols-2 lg:grid-cols-4" @submit.prevent="onRegistrarAvance">
          <div>
            <label class="text-xs text-slate-500" for="av-fecha">Fecha</label>
            <Input id="av-fecha" v-model="avanceForm.fecha" type="date" :class="inputClass" required />
          </div>
          <div>
            <label class="text-xs text-slate-500" for="av-monto">Monto</label>
            <Input id="av-monto" v-model="avanceForm.monto" :class="inputClass" required />
          </div>
          <div class="md:col-span-2">
            <label class="text-xs text-slate-500" for="av-partida">Partida</label>
            <select id="av-partida" v-model="avanceForm.partidaId" :class="selectClass">
              <option value="">Cabecera / sin partida</option>
              <option v-for="partida in partidas" :key="partida.id ?? partida.codigo" :value="String(partida.id)">
                {{ partidaLabel(partida) }}
              </option>
            </select>
          </div>
          <div class="flex items-end">
            <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="saving">
              Registrar
            </Button>
          </div>
        </form>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Fecha</TableHead>
              <TableHead>Monto</TableHead>
              <TableHead>Centro</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in avances" :key="item.id">
              <TableCell>{{ item.fecha }}</TableCell>
              <TableCell>{{ money.format(n(item.monto)) }}</TableCell>
              <TableCell>{{ item.centroCosto || '—' }}</TableCell>
              <TableCell>
                <Button variant="outline" size="sm" type="button" :disabled="saving" @click="onEliminarAvance(item)">
                  Eliminar
                </Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!avances.length" :colspan="4">Sin avances.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </div>
  </div>
</template>
