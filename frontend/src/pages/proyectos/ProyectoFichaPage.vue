<script setup lang="ts">
import { CheckIcon, ChevronsUpDownIcon } from '@lucide/vue'
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import {
  Combobox,
  ComboboxAnchor,
  ComboboxEmpty,
  ComboboxGroup,
  ComboboxInput,
  ComboboxItem,
  ComboboxItemIndicator,
  ComboboxList,
  ComboboxTrigger,
} from '@/components/ui/combobox'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  actualizarProyecto,
  guardarAccesosObra,
  guardarResponsablesObra,
  listarAccesosObra,
  listarAlertasObra,
  listarResponsablesObra,
  listarUsuariosAsignables,
  obtenerCatalogosConstruccion,
  obtenerProyecto,
} from '@/services/construccion/construccionService'
import { getSession } from '@/services/auth/authService'
import { listarPersonas } from '@/services/persona/personaService'
import { esAlmacen, esGerencia } from '@/types/auth'
import type { AccesoObra, AlertaObra, EstadoObra, NivelRiesgoObra, RolResponsableObra, UsuarioAsignable } from '@/types/construccion'
import type { PersonaResumenDTO } from '@/types/persona'

type TabFicha = 'resumen' | 'tareo' | 'planilla' | 'presupuesto' | 'almacen' | 'recursos' | 'avances' | 'financiamiento' | 'rentabilidad' | 'documentos' | 'bitacora'

type PersonaOpcion = Pick<PersonaResumenDTO, 'id' | 'nombres' | 'apellidos' | 'numeroDocumento'>

type ResponsableSlot = {
  personaId: number | null
  seleccionada: PersonaOpcion | null
  search: string
  opciones: PersonaOpcion[]
}

const ROLES: { rol: RolResponsableObra; label: string }[] = [
  { rol: 'RESIDENTE', label: 'Residente' },
  { rol: 'MAESTRO_OBRA', label: 'Maestro de obra' },
  { rol: 'SUPERVISOR_SSOMA', label: 'Supervisor SSOMA' },
]

const inputClass =
  'mt-1.5 h-10 rounded-lg border-slate-200 bg-white px-3 text-sm text-slate-900 placeholder-slate-400 focus-visible:border-sky-600 focus-visible:ring-sky-500/20'

const selectClass =
  'mt-1.5 block h-10 min-w-0 w-full rounded-lg border border-slate-200 bg-white px-3 py-0 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const route = useRoute()
const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const formError = ref('')
const alertaUit = ref(false)
const uitAnio = ref(new Date().getFullYear())
const umbralUit = ref(275000)
const alertas = ref<AlertaObra[]>([])
const accesos = ref<AccesoObra[]>([])
const accesoOpciones = ref<UsuarioAsignable[]>([])
const accesoPick = ref('')
const accesoSearch = ref('')
const savingAccesos = ref(false)
let accesoTimer: ReturnType<typeof setTimeout> | undefined
const searchTimers: Partial<Record<RolResponsableObra, ReturnType<typeof setTimeout>>> = {}

const rolActual = computed(() => getSession()?.rol)
const gerencia = computed(() => esGerencia(rolActual.value))
const almacenRol = computed(() => esAlmacen(rolActual.value))

function tabVisible(nombre: TabFicha): boolean {
  return !almacenRol.value || nombre === 'almacen'
}

const proyectoId = computed(() => Number(route.params.id))

const tab = computed<TabFicha>(() => {
  if (route.name === 'proyecto-tareo') return 'tareo'
  if (route.name === 'proyecto-planilla') return 'planilla'
  if (route.name === 'proyecto-presupuesto') return 'presupuesto'
  if (route.name === 'proyecto-almacen') return 'almacen'
  if (route.name === 'proyecto-recursos') return 'recursos'
  if (route.name === 'proyecto-avances') return 'avances'
  if (route.name === 'proyecto-financiamiento') return 'financiamiento'
  if (route.name === 'proyecto-rentabilidad') return 'rentabilidad'
  if (route.name === 'proyecto-documentos') return 'documentos'
  if (route.name === 'proyecto-bitacora') return 'bitacora'
  return 'resumen'
})

const form = reactive({
  codigo: '',
  nombre: '',
  ubicacion: '',
  centroCosto: '',
  valorObra: '',
  nivelRiesgo: '' as '' | NivelRiesgoObra,
  tasaSctrSalud: '',
  tasaSctrPension: '',
  clienteRazon: '',
  clienteRuc: '',
  fechaInicio: '',
  fechaFin: '',
  estadoObra: 'EN_EJECUCION' as EstadoObra,
  porcentajeRetencion: '0',
  activo: true,
})

function emptySlot(): ResponsableSlot {
  return { personaId: null, seleccionada: null, search: '', opciones: [] }
}

const responsables = reactive<Record<RolResponsableObra, ResponsableSlot>>({
  RESIDENTE: emptySlot(),
  MAESTRO_OBRA: emptySlot(),
  SUPERVISOR_SSOMA: emptySlot(),
})

const formAlertaUit = computed(() => {
  const valor = Number(form.valorObra.replace(',', '.'))
  return form.valorObra.trim() !== '' && Number.isFinite(valor) && valor > umbralUit.value
})

function estadoObraLabel(estado: EstadoObra): string {
  if (estado === 'PARALIZADA') return 'Paralizada'
  if (estado === 'LIQUIDADA') return 'Liquidada'
  return 'En ejecución'
}

function personaLabel(persona: PersonaOpcion | null): string {
  if (!persona) {
    return ''
  }
  return `${persona.apellidos}, ${persona.nombres} · ${persona.numeroDocumento}`
}

function opcionesDe(rol: RolResponsableObra): PersonaOpcion[] {
  const slot = responsables[rol]
  const seleccion = slot.seleccionada
  if (!seleccion) {
    return slot.opciones
  }
  if (slot.opciones.some((item) => item.id === seleccion.id)) {
    return slot.opciones
  }
  return [seleccion, ...slot.opciones]
}

function tasaToInput(valor: number | null | undefined): string {
  if (valor == null || valor === 0) {
    return ''
  }
  return String(valor)
}

function parseTasa(texto: string): number | false {
  const limpio = texto.trim().replace(',', '.')
  if (!limpio) {
    return 0
  }
  const valor = Number(limpio)
  if (!Number.isFinite(valor) || valor < 0 || valor > 1) {
    return false
  }
  return valor
}

async function buscarPersonas(rol: RolResponsableObra, q: string) {
  const data = await listarPersonas({ page: 0, size: 15, q })
  responsables[rol].opciones = data.content
}

function scheduleSearch(rol: RolResponsableObra, q: string) {
  const actual = searchTimers[rol]
  if (actual) {
    clearTimeout(actual)
  }
  searchTimers[rol] = setTimeout(() => {
    void buscarPersonas(rol, q)
  }, 300)
}

function onPersonaChange(rol: RolResponsableObra, value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(id)) {
    responsables[rol].personaId = null
    responsables[rol].seleccionada = null
    return
  }
  responsables[rol].personaId = id
  responsables[rol].seleccionada =
    opcionesDe(rol).find((item) => item.id === id) ?? responsables[rol].seleccionada
}

function quitarResponsable(rol: RolResponsableObra) {
  responsables[rol].personaId = null
  responsables[rol].seleccionada = null
  responsables[rol].search = ''
}

function onTab(value: string | number) {
  const siguiente = String(value) as TabFicha
  if (siguiente === tab.value || !tabVisible(siguiente)) {
    return
  }
  const id = String(proyectoId.value)
  if (siguiente === 'tareo') {
    void router.push({ name: 'proyecto-tareo', params: { id } })
    return
  }
  if (siguiente === 'planilla') {
    void router.push({ name: 'proyecto-planilla', params: { id } })
    return
  }
  if (siguiente === 'presupuesto') {
    void router.push({ name: 'proyecto-presupuesto', params: { id } })
    return
  }
  if (siguiente === 'almacen') {
    void router.push({ name: 'proyecto-almacen', params: { id } })
    return
  }
  if (siguiente === 'recursos') {
    void router.push({ name: 'proyecto-recursos', params: { id } })
    return
  }
  if (siguiente === 'avances') {
    void router.push({ name: 'proyecto-avances', params: { id } })
    return
  }
  if (siguiente === 'financiamiento') {
    void router.push({ name: 'proyecto-financiamiento', params: { id } })
    return
  }
  if (siguiente === 'rentabilidad') {
    void router.push({ name: 'proyecto-rentabilidad', params: { id } })
    return
  }
  if (siguiente === 'documentos') {
    void router.push({ name: 'proyecto-documentos', params: { id } })
    return
  }
  if (siguiente === 'bitacora') {
    void router.push({ name: 'proyecto-bitacora', params: { id } })
    return
  }
  void router.push({ name: 'proyecto-ficha', params: { id } })
}

const accesoOpcionesMostrar = computed(() =>
  accesoOpciones.value.filter((item) => !accesos.value.some((acceso) => acceso.usuarioId === item.id)),
)

function rolAccesoLabel(rol: AccesoObra['rol']): string {
  if (rol === 'RESIDENTE') return 'Residente'
  if (rol === 'ALMACEN') return 'Almacén'
  if (rol === 'SUPERVISOR') return 'Supervisor'
  return rol
}

async function buscarAsignables(q: string) {
  accesoOpciones.value = await listarUsuariosAsignables(q)
}

function scheduleAccesoSearch(q: string) {
  if (accesoTimer) {
    clearTimeout(accesoTimer)
  }
  accesoTimer = setTimeout(() => {
    void buscarAsignables(q)
  }, 300)
}

function onAccesoChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(id)) {
    return
  }
  const elegido = accesoOpciones.value.find((item) => item.id === id)
  if (!elegido || accesos.value.some((acceso) => acceso.usuarioId === elegido.id)) {
    return
  }
  accesos.value = [
    ...accesos.value,
    { usuarioId: elegido.id, username: elegido.username, rol: elegido.rol },
  ]
  accesoPick.value = ''
}

function quitarAcceso(usuarioId: number) {
  accesos.value = accesos.value.filter((acceso) => acceso.usuarioId !== usuarioId)
}

async function onGuardarAccesos() {
  savingAccesos.value = true
  try {
    accesos.value = await guardarAccesosObra(
      proyectoId.value,
      accesos.value.map((acceso) => ({ usuarioId: acceso.usuarioId })),
    )
    toast.success('Accesos guardados.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron guardar los accesos.')
  } finally {
    savingAccesos.value = false
  }
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  formError.value = ''
  try {
    const soloAlmacen = esAlmacen(getSession()?.rol)
    const esAdmin = esGerencia(getSession()?.rol)
    const [proyecto, lista, iniciales, catalogos, listaAlertas, listaAccesos] = await Promise.all([
      obtenerProyecto(proyectoId.value),
      soloAlmacen ? Promise.resolve([]) : listarResponsablesObra(proyectoId.value),
      soloAlmacen ? Promise.resolve({ content: [] as PersonaOpcion[] }) : listarPersonas({ page: 0, size: 15, q: '' }),
      obtenerCatalogosConstruccion(),
      listarAlertasObra(proyectoId.value),
      esAdmin ? listarAccesosObra(proyectoId.value) : Promise.resolve([]),
    ])
    accesos.value = listaAccesos
    if (esAdmin) {
      void buscarAsignables('')
    }
    form.codigo = proyecto.codigo
    form.nombre = proyecto.nombre
    form.ubicacion = proyecto.ubicacion ?? ''
    form.centroCosto = proyecto.centroCosto ?? ''
    form.valorObra = proyecto.valorObra == null ? '' : String(proyecto.valorObra)
    form.nivelRiesgo = proyecto.nivelRiesgo ?? ''
    form.tasaSctrSalud = tasaToInput(proyecto.tasaSctrSalud)
    form.tasaSctrPension = tasaToInput(proyecto.tasaSctrPension)
    form.clienteRazon = proyecto.clienteRazon ?? ''
    form.clienteRuc = proyecto.clienteRuc ?? ''
    form.fechaInicio = proyecto.fechaInicio ?? ''
    form.fechaFin = proyecto.fechaFin ?? ''
    form.estadoObra = proyecto.estadoObra ?? 'EN_EJECUCION'
    form.porcentajeRetencion = proyecto.porcentajeRetencion == null ? '0' : String(proyecto.porcentajeRetencion)
    form.activo = proyecto.activo
    alertaUit.value = proyecto.alertaUit
    uitAnio.value = proyecto.uitAnio
    umbralUit.value = catalogos.umbralUit
    alertas.value = listaAlertas
    for (const { rol } of ROLES) {
      const slot = responsables[rol]
      slot.personaId = null
      slot.seleccionada = null
      slot.search = ''
      slot.opciones = iniciales.content
    }
    for (const item of lista) {
      const slot = responsables[item.rol]
      slot.personaId = item.personaId
      slot.seleccionada = {
        id: item.personaId,
        nombres: item.nombres,
        apellidos: item.apellidos,
        numeroDocumento: item.numeroDocumento,
      }
      slot.opciones = iniciales.content
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la ficha de obra.')
  } finally {
    loading.value = false
  }
}

async function onGuardar() {
  formError.value = ''
  if (!form.codigo.trim() || !form.nombre.trim()) {
    formError.value = 'Ingrese código y nombre de la obra.'
    toast.error(formError.value)
    return
  }
  const valorTexto = form.valorObra.trim().replace(',', '.')
  let valorObra: number | null = null
  if (valorTexto) {
    valorObra = Number(valorTexto)
    if (!Number.isFinite(valorObra) || valorObra < 0) {
      formError.value = 'El valor de obra debe ser un monto válido.'
      toast.error(formError.value)
      return
    }
  }
  const tasaSalud = parseTasa(form.tasaSctrSalud)
  const tasaPension = parseTasa(form.tasaSctrPension)
  if (tasaSalud === false || tasaPension === false) {
    formError.value = 'Las tasas SCTR deben ser un decimal entre 0 y 1.'
    toast.error(formError.value)
    return
  }
  const ruc = form.clienteRuc.trim()
  if (ruc && !/^\d{11}$/.test(ruc)) {
    formError.value = 'El RUC del cliente debe tener 11 dígitos.'
    toast.error(formError.value)
    return
  }
  if (form.fechaInicio && form.fechaFin && form.fechaFin < form.fechaInicio) {
    formError.value = 'La fecha de fin no puede ser menor que la de inicio.'
    toast.error(formError.value)
    return
  }
  const retencionTexto = form.porcentajeRetencion.trim().replace(',', '.')
  let porcentajeRetencion = 0
  if (retencionTexto) {
    porcentajeRetencion = Number(retencionTexto)
    if (!Number.isFinite(porcentajeRetencion) || porcentajeRetencion < 0 || porcentajeRetencion > 100) {
      formError.value = 'La retención debe estar entre 0 y 100%.'
      toast.error(formError.value)
      return
    }
  }
  submitting.value = true
  try {
    const actualizado = await actualizarProyecto(proyectoId.value, {
      codigo: form.codigo.trim(),
      nombre: form.nombre.trim(),
      ubicacion: form.ubicacion.trim() || null,
      centroCosto: form.centroCosto.trim() || null,
      valorObra,
      nivelRiesgo: form.nivelRiesgo || null,
      tasaSctrSalud: tasaSalud,
      tasaSctrPension: tasaPension,
      clienteRazon: form.clienteRazon.trim() || null,
      clienteRuc: ruc || null,
      fechaInicio: form.fechaInicio || null,
      fechaFin: form.fechaFin || null,
      estadoObra: form.estadoObra,
      porcentajeRetencion,
      activo: form.activo,
    })
    const filas = ROLES.flatMap(({ rol }) => {
      const personaId = responsables[rol].personaId
      return personaId ? [{ personaId, rol }] : []
    })
    await guardarResponsablesObra(proyectoId.value, filas)
    alertaUit.value = actualizado.alertaUit
    uitAnio.value = actualizado.uitAnio
    alertas.value = await listarAlertasObra(proyectoId.value)
    form.codigo = actualizado.codigo
    form.nombre = actualizado.nombre
    form.estadoObra = actualizado.estadoObra
    form.porcentajeRetencion =
      actualizado.porcentajeRetencion == null ? '0' : String(actualizado.porcentajeRetencion)
    form.activo = actualizado.activo
    if (actualizado.alertaUit) {
      toast.warning(
        `Obra actualizada. El valor supera 50 UIT (${actualizado.uitAnio}); es una alerta, no un bloqueo.`,
      )
    } else {
      toast.success('Obra y responsables guardados.')
    }
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo guardar la ficha.'
    formError.value = message
    toast.error(message)
  } finally {
    submitting.value = false
  }
}

watch(proyectoId, () => {
  if (Number.isFinite(proyectoId.value) && proyectoId.value > 0) {
    void load()
  }
})

watch(
  tab,
  (actual) => {
    if (almacenRol.value && actual !== 'almacen' && Number.isFinite(proyectoId.value) && proyectoId.value > 0) {
      void router.replace({ name: 'proyecto-almacen', params: { id: String(proyectoId.value) } })
    }
  },
  { immediate: true },
)

watch(accesoSearch, (q) => {
  if (gerencia.value) {
    scheduleAccesoSearch(q)
  }
})

for (const { rol } of ROLES) {
  watch(
    () => responsables[rol].search,
    (q) => scheduleSearch(rol, q),
  )
}

onMounted(() => {
  void load()
})

onUnmounted(() => {
  for (const timer of Object.values(searchTimers)) {
    if (timer) {
      clearTimeout(timer)
    }
  }
  if (accesoTimer) {
    clearTimeout(accesoTimer)
  }
})
</script>

<template>
  <section>
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Proyectos</p>
    <div class="mt-2 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          <template v-if="loading">Ficha de obra</template>
          <template v-else>{{ form.codigo }} · {{ form.nombre }}</template>
        </h2>
        <p class="mt-2 text-sm text-slate-500">
          {{ estadoObraLabel(form.estadoObra) }}
          <span v-if="!form.activo"> · Inactiva</span>
        </p>
      </div>
      <Button variant="outline" type="button" @click="router.push({ name: 'proyectos' })">Obras</Button>
    </div>

    <div v-if="alertas.length" class="mt-4 space-y-2">
      <div
        v-for="(alerta, idx) in alertas"
        :key="`${alerta.codigo}-${idx}`"
        class="rounded-lg px-4 py-3 text-sm"
        :class="alerta.severidad === 'CRITICAL'
          ? 'border border-red-300 bg-red-50 text-red-950'
          : 'border border-amber-300 bg-amber-50 text-amber-950'"
      >
        {{ alerta.mensaje }}
      </div>
    </div>

    <Tabs class="mt-5" :model-value="tab" @update:model-value="onTab">
      <TabsList class="h-auto rounded-xl border border-slate-200 bg-slate-100/85 p-1.5">
        <TabsTrigger
          v-if="tabVisible('resumen')"
          value="resumen"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Resumen
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('tareo')"
          value="tareo"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Tareo
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('planilla')"
          value="planilla"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Planilla
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('presupuesto')"
          value="presupuesto"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Presupuesto
        </TabsTrigger>
        <TabsTrigger
          value="almacen"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Almacén
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('recursos')"
          value="recursos"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Recursos
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('avances')"
          value="avances"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Avances
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('financiamiento')"
          value="financiamiento"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Financiamiento
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('rentabilidad')"
          value="rentabilidad"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Rentabilidad
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('documentos')"
          value="documentos"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Documentos
        </TabsTrigger>
        <TabsTrigger
          v-if="tabVisible('bitacora')"
          value="bitacora"
          class="h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
        >
          Bitácora
        </TabsTrigger>
      </TabsList>
    </Tabs>

    <div v-if="tab === 'resumen'" class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div v-if="loading" class="space-y-3">
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-24 w-full" />
      </div>
      <form v-else class="grid gap-4 md:grid-cols-2" @submit.prevent="onGuardar">
        <p v-if="formError" class="text-sm text-red-600 md:col-span-2">{{ formError }}</p>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-codigo">Código</label>
          <Input id="ficha-codigo" v-model="form.codigo" maxlength="20" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-nombre">Nombre</label>
          <Input id="ficha-nombre" v-model="form.nombre" maxlength="160" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-ubicacion">Ubicación</label>
          <Input id="ficha-ubicacion" v-model="form.ubicacion" maxlength="120" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-cc">Centro de costo</label>
          <Input id="ficha-cc" v-model="form.centroCosto" maxlength="40" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-cliente">Cliente</label>
          <Input id="ficha-cliente" v-model="form.clienteRazon" maxlength="160" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-ruc">RUC</label>
          <Input id="ficha-ruc" v-model="form.clienteRuc" maxlength="11" inputmode="numeric" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-inicio">Fecha de inicio</label>
          <Input id="ficha-inicio" v-model="form.fechaInicio" type="date" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-fin">Fecha de fin</label>
          <Input id="ficha-fin" v-model="form.fechaFin" type="date" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-estado">Estado de obra</label>
          <select id="ficha-estado" v-model="form.estadoObra" :class="selectClass">
            <option value="EN_EJECUCION">En ejecución</option>
            <option value="PARALIZADA">Paralizada</option>
            <option value="LIQUIDADA">Liquidada</option>
          </select>
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-retencion">Retención (%)</label>
          <Input
            id="ficha-retencion"
            v-model="form.porcentajeRetencion"
            inputmode="decimal"
            placeholder="0"
            :class="inputClass"
          />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-riesgo">Nivel de riesgo SCTR</label>
          <select id="ficha-riesgo" v-model="form.nivelRiesgo" :class="selectClass">
            <option value="">Sin definir</option>
            <option value="BAJO">Bajo</option>
            <option value="MEDIO">Medio</option>
            <option value="ALTO">Alto</option>
          </select>
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-sctr-salud">Tasa SCTR salud</label>
          <Input
            id="ficha-sctr-salud"
            v-model="form.tasaSctrSalud"
            inputmode="decimal"
            placeholder="0.0125"
            :class="inputClass"
          />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="ficha-sctr-pension">Tasa SCTR pensión</label>
          <Input
            id="ficha-sctr-pension"
            v-model="form.tasaSctrPension"
            inputmode="decimal"
            placeholder="0.0125"
            :class="inputClass"
          />
        </div>
        <div class="md:col-span-2">
          <label class="text-sm font-medium text-slate-700" for="ficha-valor">Valor de obra (S/)</label>
          <Input
            id="ficha-valor"
            v-model="form.valorObra"
            inputmode="decimal"
            placeholder="Opcional"
            :class="inputClass"
          />
          <p v-if="formAlertaUit" class="mt-1.5 text-xs text-amber-800">
            El valor supera 50 UIT ({{ money.format(umbralUit) }} en {{ uitAnio }}). Se puede
            guardar: es una alerta, no un bloqueo.
          </p>
        </div>
        <label class="flex items-center gap-2 text-sm text-slate-700 md:col-span-2" for="ficha-activo">
          <input id="ficha-activo" v-model="form.activo" type="checkbox" class="size-4 rounded border-slate-300" />
          Obra activa
        </label>

        <div class="md:col-span-2 mt-2 border-t border-slate-100 pt-4">
          <h3 class="text-base font-semibold text-palette-navy">Responsables</h3>
          <p class="mt-1 text-sm text-slate-500">
            Un responsable por rol, del padrón de Gestión Humana. Puede dejar un rol vacío.
          </p>
        </div>

        <div v-for="item in ROLES" :key="item.rol" class="md:col-span-2">
          <div class="flex items-end gap-2">
            <div class="min-w-0 flex-1">
              <label class="text-sm font-medium text-slate-700" :for="`ficha-resp-${item.rol}`">{{ item.label }}</label>
              <Combobox
                :model-value="responsables[item.rol].personaId ?? undefined"
                ignore-filter
                class="min-w-0 w-full"
                @update:model-value="(value) => onPersonaChange(item.rol, value)"
              >
                <ComboboxAnchor class="min-w-0 w-full">
                  <ComboboxTrigger as-child>
                    <Button
                      :id="`ficha-resp-${item.rol}`"
                      variant="outline"
                      class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                      type="button"
                    >
                      <span
                        class="min-w-0 flex-1 truncate text-left"
                        :class="personaLabel(responsables[item.rol].seleccionada) ? 'text-slate-900' : 'text-slate-400'"
                      >
                        {{ personaLabel(responsables[item.rol].seleccionada) || 'Buscar por nombre o documento' }}
                      </span>
                      <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                    </Button>
                  </ComboboxTrigger>
                </ComboboxAnchor>
                <ComboboxList
                  class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)"
                  align="start"
                >
                  <ComboboxInput
                    v-model="responsables[item.rol].search"
                    placeholder="Buscar persona..."
                    autocomplete="off"
                  />
                  <ComboboxEmpty>Sin resultados.</ComboboxEmpty>
                  <ComboboxGroup>
                    <ComboboxItem
                      v-for="persona in opcionesDe(item.rol)"
                      :key="persona.id"
                      :value="persona.id"
                    >
                      {{ persona.apellidos }}, {{ persona.nombres }} · {{ persona.numeroDocumento }}
                      <ComboboxItemIndicator>
                        <CheckIcon />
                      </ComboboxItemIndicator>
                    </ComboboxItem>
                  </ComboboxGroup>
                </ComboboxList>
              </Combobox>
            </div>
            <Button
              v-if="responsables[item.rol].personaId"
              variant="outline"
              class="h-10"
              type="button"
              @click="quitarResponsable(item.rol)"
            >
              Quitar
            </Button>
          </div>
        </div>

        <div class="flex justify-end gap-2 pt-2 md:col-span-2">
          <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="submitting">
            Guardar
          </Button>
        </div>
      </form>

      <div v-if="!loading && gerencia" class="mt-6 border-t border-slate-100 pt-4">
        <h3 class="text-base font-semibold text-palette-navy">Accesos</h3>
        <p class="mt-1 text-sm text-slate-500">
          Usuarios de obra (residente, supervisor o almacén). Gerencia ve todas las obras sin asignarse.
        </p>
        <div class="mt-3 flex items-end gap-2">
          <div class="min-w-0 flex-1">
            <label class="text-sm font-medium text-slate-700" for="ficha-acceso">Usuario</label>
            <Combobox
              :model-value="accesoPick || undefined"
              ignore-filter
              class="min-w-0 w-full"
              @update:model-value="onAccesoChange"
            >
              <ComboboxAnchor class="min-w-0 w-full">
                <ComboboxTrigger as-child>
                  <Button
                    id="ficha-acceso"
                    variant="outline"
                    class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                    type="button"
                  >
                    <span class="min-w-0 flex-1 truncate text-left text-slate-400">
                      Buscar por usuario
                    </span>
                    <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                  </Button>
                </ComboboxTrigger>
              </ComboboxAnchor>
              <ComboboxList
                class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)"
                align="start"
              >
                <ComboboxInput v-model="accesoSearch" placeholder="Buscar usuario..." autocomplete="off" />
                <ComboboxEmpty>Sin resultados.</ComboboxEmpty>
                <ComboboxGroup>
                  <ComboboxItem v-for="usuario in accesoOpcionesMostrar" :key="usuario.id" :value="usuario.id">
                    {{ usuario.username }} · {{ rolAccesoLabel(usuario.rol) }}
                    <ComboboxItemIndicator>
                      <CheckIcon />
                    </ComboboxItemIndicator>
                  </ComboboxItem>
                </ComboboxGroup>
              </ComboboxList>
            </Combobox>
          </div>
        </div>
        <div class="mt-3 space-y-2">
          <div
            v-for="acceso in accesos"
            :key="acceso.usuarioId"
            class="flex items-center justify-between gap-3 rounded-lg border border-slate-100 px-3 py-2"
          >
            <p class="min-w-0 truncate text-sm text-slate-800">
              {{ acceso.username }}
              <span class="text-slate-500">· {{ rolAccesoLabel(acceso.rol) }}</span>
            </p>
            <Button variant="outline" class="h-8" type="button" @click="quitarAcceso(acceso.usuarioId)">
              Quitar
            </Button>
          </div>
          <p v-if="!accesos.length" class="text-sm text-slate-500">Aún no hay usuarios asignados a esta obra.</p>
        </div>
        <div class="mt-3 flex justify-end">
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="savingAccesos"
            @click="onGuardarAccesos"
          >
            Guardar accesos
          </Button>
        </div>
      </div>
    </div>

    <div v-else class="mt-4">
      <RouterView />
    </div>
  </section>
</template>
