<script setup lang="ts">
import { CheckIcon, ChevronsUpDownIcon } from '@lucide/vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import { Badge } from '@/components/ui/badge'
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
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import { listarPersonalObra, obtenerPresupuesto, subirDocumentoObra } from '@/services/construccion/construccionService'
import {
  anularPolizaSctr,
  cambiarEstadoItemEpp,
  crearEntregaEpp,
  crearPolizaSctr,
  descargarKardexEpp,
  guardarNominaSctr,
  listarEmosObra,
  listarEntregasEpp,
  listarMaterialesEpp,
  listarPolizasSctr,
  obtenerResumenSoma,
  registrarEmoObra,
  sugerirNominaSctr,
} from '@/services/soma/somaService'
import type { PartidaPresupuesto, PersonalObra } from '@/types/construccion'
import type {
  AptitudEmo,
  EstadoItemEpp,
  MotivoEntregaEpp,
  SomaEmo,
  SomaEntregaEpp,
  SomaEppMaterial,
  SomaResumenObra,
  SomaSctrNominaFila,
  SomaSctrPoliza,
  TipoEmo,
} from '@/types/soma'
import { fieldInputClass, fieldSelectClass, tabsListClass } from './fieldClasses'

type TabSoma = 'resumen' | 'vigilancia' | 'sctr' | 'epp'
type EppItemDraft = { materialId: string; cantidad: string; talla: string }

const inputClass = fieldInputClass
const selectClass = fieldSelectClass
const tabTriggerClass =
  'h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm'

const TIPOS_EMO: { valor: TipoEmo; label: string }[] = [
  { valor: 'INGRESO', label: 'Ingreso' },
  { valor: 'PERIODICO', label: 'Periódico' },
  { valor: 'RETIRO', label: 'Retiro' },
  { valor: 'CAMBIO_PUESTO', label: 'Cambio de puesto' },
  { valor: 'REINCORPORACION', label: 'Reincorporación' },
]

const APTITUDES: { valor: AptitudEmo; label: string }[] = [
  { valor: 'APTO', label: 'Apto' },
  { valor: 'APTO_CON_RESTRICCION', label: 'Apto con restricción' },
  { valor: 'OBSERVADO', label: 'Observado' },
  { valor: 'NO_APTO', label: 'No apto' },
]

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

const anioActual = new Date().getFullYear()
const ANIOS = [anioActual - 1, anioActual, anioActual + 1]
const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const qtyFmt = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 0, maximumFractionDigits: 4 })

const MOTIVOS_EPP: { valor: MotivoEntregaEpp; label: string }[] = [
  { valor: 'DOTACION_INICIAL', label: 'Dotación inicial' },
  { valor: 'RENOVACION_DESGASTE', label: 'Renovación por desgaste' },
  { valor: 'REPOSICION_PERDIDA', label: 'Reposición por pérdida' },
  { valor: 'TRABAJO_ESPECIAL', label: 'Trabajo especial' },
]

const ESTADOS_EPP: { valor: EstadoItemEpp; label: string }[] = [
  { valor: 'VIGENTE', label: 'Vigente' },
  { valor: 'DEVUELTO', label: 'Devuelto' },
  { valor: 'DETERIORADO', label: 'Deteriorado' },
  { valor: 'EXTRAVIADO', label: 'Extraviado' },
]

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const tab = ref<TabSoma>('resumen')
const loading = ref(true)
const saving = ref(false)
const resumen = ref<SomaResumenObra | null>(null)
const emos = ref<SomaEmo[]>([])
const personal = ref<PersonalObra[]>([])
const personaSearch = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const polizas = ref<SomaSctrPoliza[]>([])
const polizaSeleccionadaId = ref<number | null>(null)
const nominaEdicion = ref<SomaSctrNominaFila[]>([])
const sctrFileInput = ref<HTMLInputElement | null>(null)
const sctrSaving = ref(false)
const entregasEpp = ref<SomaEntregaEpp[]>([])
const materialesEpp = ref<SomaEppMaterial[]>([])
const partidas = ref<PartidaPresupuesto[]>([])
const eppPersonaSearch = ref('')
const eppSaving = ref(false)
const eppItems = ref<EppItemDraft[]>([{ materialId: '', cantidad: '', talla: '' }])

const eppForm = reactive({
  personaId: null as number | null,
  motivo: 'DOTACION_INICIAL' as MotivoEntregaEpp,
  fechaEntrega: hoyIso(),
  partidaId: '',
  descontarAlmacen: true,
  firmadoTrabajador: false,
  observaciones: '',
})

const form = reactive({
  personaId: null as number | null,
  tipoEmo: 'INGRESO' as TipoEmo,
  aptitud: 'APTO' as AptitudEmo,
  clinicaNombre: '',
  fechaExamen: hoyIso(),
  fechaVencimiento: masUnAnio(hoyIso()),
  restricciones: '',
})

const sctrForm = reactive({
  anio: anioActual,
  mes: new Date().getMonth() + 1,
  aseguradoraSalud: '',
  numeroPolizaSalud: '',
  aseguradoraPension: '',
  numeroPolizaPension: '',
  fechaInicio: primerDiaMes(anioActual, new Date().getMonth() + 1),
  fechaFin: ultimoDiaMes(anioActual, new Date().getMonth() + 1),
  montoPrimaSalud: '0',
  montoPrimaPension: '0',
})

const polizaSeleccionada = computed(
  () => polizas.value.find((item) => item.id === polizaSeleccionadaId.value) ?? null,
)

const personaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === form.personaId) ?? null,
)

const eppPersonaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === eppForm.personaId) ?? null,
)

const opcionesPersona = computed(() => {
  const q = personaSearch.value.trim().toLowerCase()
  const lista = q
    ? personal.value.filter((item) =>
        `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
      )
    : personal.value
  const seleccion = personaSeleccionada.value
  if (seleccion && !lista.some((item) => item.personaId === seleccion.personaId)) {
    return [seleccion, ...lista]
  }
  return lista
})

const opcionesEppPersona = computed(() => {
  const q = eppPersonaSearch.value.trim().toLowerCase()
  const lista = q
    ? personal.value.filter((item) =>
        `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
      )
    : personal.value
  const seleccion = eppPersonaSeleccionada.value
  if (seleccion && !lista.some((item) => item.personaId === seleccion.personaId)) {
    return [seleccion, ...lista]
  }
  return lista
})

const partidasVigentes = computed(() =>
  partidas.value.filter((item): item is PartidaPresupuesto & { id: number } => item.id != null),
)

const historialEppPorPersona = computed(() => {
  const grupos = new Map<
    number,
    { personaId: number; personaNombre: string; numeroDocumento: string; entregas: SomaEntregaEpp[] }
  >()
  for (const entrega of entregasEpp.value) {
    let grupo = grupos.get(entrega.personaId)
    if (!grupo) {
      grupo = {
        personaId: entrega.personaId,
        personaNombre: entrega.personaNombre || `Persona ${entrega.personaId}`,
        numeroDocumento: entrega.numeroDocumento || '',
        entregas: [],
      }
      grupos.set(entrega.personaId, grupo)
    }
    grupo.entregas.push(entrega)
  }
  return [...grupos.values()]
})

function hoyIso(): string {
  const fecha = new Date()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}`
}

function primerDiaMes(anio: number, mes: number): string {
  return `${anio}-${String(mes).padStart(2, '0')}-01`
}

function ultimoDiaMes(anio: number, mes: number): string {
  const fecha = new Date(anio, mes, 0)
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${anio}-${String(mes).padStart(2, '0')}-${dia}`
}

function mesLabel(mes: number): string {
  return MESES[mes - 1] ?? String(mes)
}

function n(valor: string): number {
  const parsed = Number(valor.replace(',', '.'))
  return Number.isFinite(parsed) ? parsed : 0
}

function masUnAnio(iso: string): string {
  const [anio, mes, dia] = iso.split('-').map(Number)
  if (!anio || !mes || !dia) {
    return iso
  }
  return `${anio + 1}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`
}

function personaLabel(persona: PersonalObra | null): string {
  if (!persona) {
    return ''
  }
  return `${persona.apellidos}, ${persona.nombres} · ${persona.numeroDocumento}`
}

function tipoLabel(tipo: TipoEmo): string {
  return TIPOS_EMO.find((item) => item.valor === tipo)?.label ?? tipo
}

function aptitudLabel(aptitud: AptitudEmo): string {
  return APTITUDES.find((item) => item.valor === aptitud)?.label ?? aptitud
}

function fechaLabel(valor: string | null | undefined): string {
  if (!valor) {
    return '—'
  }
  const [anio, mes, dia] = valor.split('-')
  if (!anio || !mes || !dia) {
    return valor
  }
  return `${dia}/${mes}/${anio}`
}

function emoVencido(item: SomaEmo): boolean {
  return item.fechaVencimiento < hoyIso()
}

function aptitudBadgeVariant(item: SomaEmo): 'success' | 'default' | 'secondary' {
  if (emoVencido(item) || item.aptitud === 'NO_APTO' || item.aptitud === 'OBSERVADO') {
    return 'secondary'
  }
  if (item.aptitud === 'APTO') {
    return 'success'
  }
  return 'default'
}

function uniquePersonal(lista: PersonalObra[]): PersonalObra[] {
  const vistos = new Set<number>()
  return lista.filter((item) => {
    if (vistos.has(item.personaId)) {
      return false
    }
    vistos.add(item.personaId)
    return true
  })
}

function resetForm() {
  form.personaId = null
  form.tipoEmo = 'INGRESO'
  form.aptitud = 'APTO'
  form.clinicaNombre = ''
  form.fechaExamen = hoyIso()
  form.fechaVencimiento = masUnAnio(hoyIso())
  form.restricciones = ''
  personaSearch.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function onPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  form.personaId = Number.isFinite(id) ? id : null
}

function onEppPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  eppForm.personaId = Number.isFinite(id) ? id : null
}

function motivoEppLabel(motivo: MotivoEntregaEpp): string {
  return MOTIVOS_EPP.find((item) => item.valor === motivo)?.label ?? motivo
}

function estadoEppLabel(estado: EstadoItemEpp): string {
  return ESTADOS_EPP.find((item) => item.valor === estado)?.label ?? estado
}

function estadoEppBadge(estado: EstadoItemEpp): 'success' | 'default' | 'secondary' {
  if (estado === 'VIGENTE') {
    return 'success'
  }
  if (estado === 'DEVUELTO') {
    return 'default'
  }
  return 'secondary'
}

function resetEppForm() {
  eppForm.personaId = null
  eppForm.motivo = 'DOTACION_INICIAL'
  eppForm.fechaEntrega = hoyIso()
  eppForm.partidaId = ''
  eppForm.descontarAlmacen = true
  eppForm.firmadoTrabajador = false
  eppForm.observaciones = ''
  eppPersonaSearch.value = ''
  eppItems.value = [{ materialId: '', cantidad: '', talla: '' }]
}

function agregarItemEpp() {
  eppItems.value.push({ materialId: '', cantidad: '', talla: '' })
}

function quitarItemEpp(index: number) {
  if (eppItems.value.length === 1) {
    eppItems.value = [{ materialId: '', cantidad: '', talla: '' }]
    return
  }
  eppItems.value.splice(index, 1)
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    const [resumenObra, listaEmos, listaPersonal, listaPolizas, listaEpps, listaMaterialesEpp, presupuesto] =
      await Promise.all([
        obtenerResumenSoma(proyectoId.value),
        listarEmosObra(proyectoId.value),
        listarPersonalObra(proyectoId.value),
        listarPolizasSctr(proyectoId.value),
        listarEntregasEpp(proyectoId.value),
        listarMaterialesEpp(proyectoId.value),
        obtenerPresupuesto(proyectoId.value).catch(() => null),
      ])
    resumen.value = resumenObra
    emos.value = listaEmos
    personal.value = uniquePersonal(listaPersonal)
    polizas.value = listaPolizas
    entregasEpp.value = listaEpps
    materialesEpp.value = listaMaterialesEpp
    partidas.value = presupuesto?.version?.estado === 'VIGENTE' ? presupuesto.version.partidas : []
    if (polizaSeleccionadaId.value && !listaPolizas.some((item) => item.id === polizaSeleccionadaId.value)) {
      polizaSeleccionadaId.value = null
      nominaEdicion.value = []
    }
  } catch (err) {
    resumen.value = null
    emos.value = []
    personal.value = []
    polizas.value = []
    entregasEpp.value = []
    materialesEpp.value = []
    partidas.value = []
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar SOMA.')
  } finally {
    loading.value = false
  }
}

async function onRegistrar() {
  if (!form.personaId) {
    toast.error('Seleccione una persona del personal de la obra.')
    return
  }
  if (!form.clinicaNombre.trim()) {
    toast.error('Indique el nombre de la clínica.')
    return
  }
  if (!form.fechaExamen || !form.fechaVencimiento) {
    toast.error('Indique las fechas de examen y vencimiento.')
    return
  }
  if (form.fechaVencimiento < form.fechaExamen) {
    toast.error('La fecha de vencimiento no puede ser menor que la de examen.')
    return
  }
  saving.value = true
  try {
    const archivo = fileInput.value?.files?.[0]
    let documentoObraId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'CERTIFICADO_EMO', archivo)
      documentoObraId = documento.id
    }
    await registrarEmoObra(proyectoId.value, {
      personaId: form.personaId,
      tipoEmo: form.tipoEmo,
      clinicaNombre: form.clinicaNombre.trim(),
      fechaExamen: form.fechaExamen,
      fechaVencimiento: form.fechaVencimiento,
      aptitud: form.aptitud,
      restricciones: form.restricciones.trim() || null,
      documentoObraId,
    })
    toast.success('EMO registrado.')
    resetForm()
    emos.value = await listarEmosObra(proyectoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el EMO.')
  } finally {
    saving.value = false
  }
}

function sincronizarFechasSctr() {
  sctrForm.fechaInicio = primerDiaMes(sctrForm.anio, sctrForm.mes)
  sctrForm.fechaFin = ultimoDiaMes(sctrForm.anio, sctrForm.mes)
}

function resetSctrForm() {
  sctrForm.anio = anioActual
  sctrForm.mes = new Date().getMonth() + 1
  sctrForm.aseguradoraSalud = ''
  sctrForm.numeroPolizaSalud = ''
  sctrForm.aseguradoraPension = ''
  sctrForm.numeroPolizaPension = ''
  sctrForm.montoPrimaSalud = '0'
  sctrForm.montoPrimaPension = '0'
  sincronizarFechasSctr()
  if (sctrFileInput.value) {
    sctrFileInput.value.value = ''
  }
}

function seleccionarPoliza(item: SomaSctrPoliza) {
  polizaSeleccionadaId.value = item.id
  nominaEdicion.value = item.nomina.map((fila) => ({ ...fila }))
}

async function onCrearPoliza() {
  if (!sctrForm.aseguradoraSalud.trim() || !sctrForm.numeroPolizaSalud.trim()) {
    toast.error('Indique la aseguradora y el número de la póliza de salud.')
    return
  }
  if (!sctrForm.aseguradoraPension.trim() || !sctrForm.numeroPolizaPension.trim()) {
    toast.error('Indique la aseguradora y el número de la póliza de pensión.')
    return
  }
  if (sctrForm.fechaFin < sctrForm.fechaInicio) {
    toast.error('La fecha de fin no puede ser menor que la de inicio.')
    return
  }
  sctrSaving.value = true
  try {
    const archivo = sctrFileInput.value?.files?.[0]
    let documentoObraId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'SCTR_POLIZA', archivo)
      documentoObraId = documento.id
    }
    const creada = await crearPolizaSctr(proyectoId.value, {
      anio: sctrForm.anio,
      mes: sctrForm.mes,
      aseguradoraSalud: sctrForm.aseguradoraSalud.trim(),
      numeroPolizaSalud: sctrForm.numeroPolizaSalud.trim(),
      aseguradoraPension: sctrForm.aseguradoraPension.trim(),
      numeroPolizaPension: sctrForm.numeroPolizaPension.trim(),
      fechaInicio: sctrForm.fechaInicio,
      fechaFin: sctrForm.fechaFin,
      montoPrimaSalud: n(sctrForm.montoPrimaSalud),
      montoPrimaPension: n(sctrForm.montoPrimaPension),
      documentoObraId,
    })
    toast.success('Póliza SCTR registrada.')
    resetSctrForm()
    polizas.value = await listarPolizasSctr(proyectoId.value)
    seleccionarPoliza(polizas.value.find((item) => item.id === creada.id) ?? creada)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la póliza SCTR.')
  } finally {
    sctrSaving.value = false
  }
}

async function onPrecargarNomina() {
  const poliza = polizaSeleccionada.value
  if (!poliza) {
    toast.error('Seleccione una póliza.')
    return
  }
  sctrSaving.value = true
  try {
    const sugeridos = await sugerirNominaSctr(proyectoId.value, poliza.anio, poliza.mes)
    const existentes = new Map(nominaEdicion.value.map((fila) => [fila.personaId, fila]))
    for (const trabajador of sugeridos) {
      if (!existentes.has(trabajador.personaId)) {
        existentes.set(trabajador.personaId, {
          personaId: trabajador.personaId,
          personaNombre: `${trabajador.apellidos}, ${trabajador.nombres}`,
          numeroDocumento: trabajador.numeroDocumento,
          incluidoSalud: true,
          incluidoPension: true,
        })
      }
    }
    nominaEdicion.value = [...existentes.values()]
    toast.success('Nómina precargada con el personal del mes.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo sugerir la nómina.')
  } finally {
    sctrSaving.value = false
  }
}

function quitarDeNomina(personaId: number) {
  nominaEdicion.value = nominaEdicion.value.filter((fila) => fila.personaId !== personaId)
}

async function onGuardarNomina() {
  const poliza = polizaSeleccionada.value
  if (!poliza) {
    toast.error('Seleccione una póliza.')
    return
  }
  sctrSaving.value = true
  try {
    const actualizada = await guardarNominaSctr(proyectoId.value, poliza.id, {
      filas: nominaEdicion.value.map((fila) => ({
        personaId: fila.personaId,
        incluidoSalud: fila.incluidoSalud,
        incluidoPension: fila.incluidoPension,
      })),
    })
    toast.success('Nómina SCTR guardada.')
    polizas.value = await listarPolizasSctr(proyectoId.value)
    seleccionarPoliza(polizas.value.find((item) => item.id === actualizada.id) ?? actualizada)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la nómina.')
  } finally {
    sctrSaving.value = false
  }
}

async function onAnularPoliza(item: SomaSctrPoliza) {
  if (!window.confirm(`¿Anular la póliza de ${mesLabel(item.mes)} ${item.anio}?`)) {
    return
  }
  sctrSaving.value = true
  try {
    await anularPolizaSctr(proyectoId.value, item.id)
    toast.success('Póliza anulada.')
    polizas.value = await listarPolizasSctr(proyectoId.value)
    if (polizaSeleccionadaId.value === item.id) {
      const actual = polizas.value.find((fila) => fila.id === item.id)
      if (actual) {
        seleccionarPoliza(actual)
      }
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo anular la póliza.')
  } finally {
    sctrSaving.value = false
  }
}

async function onRegistrarEpp() {
  if (!eppForm.personaId) {
    toast.error('Seleccione una persona del personal de la obra.')
    return
  }
  if (!eppForm.fechaEntrega) {
    toast.error('Indique la fecha de entrega.')
    return
  }
  if (eppForm.descontarAlmacen && !eppForm.partidaId) {
    toast.error('Seleccione la partida para descontar de almacén.')
    return
  }
  const items = eppItems.value
    .map((item) => ({
      materialId: Number(item.materialId),
      cantidad: n(item.cantidad),
      talla: item.talla.trim() || null,
    }))
    .filter((item) => Number.isFinite(item.materialId) && item.materialId > 0 && item.cantidad > 0)
  if (!items.length) {
    toast.error('Indique al menos un ítem de EPP con cantidad.')
    return
  }
  eppSaving.value = true
  try {
    await crearEntregaEpp(proyectoId.value, {
      personaId: eppForm.personaId,
      fechaEntrega: eppForm.fechaEntrega,
      motivo: eppForm.motivo,
      firmadoTrabajador: eppForm.firmadoTrabajador,
      descontarAlmacen: eppForm.descontarAlmacen,
      partidaId: eppForm.descontarAlmacen ? Number(eppForm.partidaId) : null,
      observaciones: eppForm.observaciones.trim() || null,
      items,
    })
    toast.success('Entrega de EPP registrada.')
    resetEppForm()
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la entrega de EPP.')
  } finally {
    eppSaving.value = false
  }
}

async function onCambiarEstadoEpp(itemId: number, estado: EstadoItemEpp) {
  try {
    await cambiarEstadoItemEpp(proyectoId.value, itemId, estado)
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cambiar el estado.')
  }
}

function onEstadoSelect(itemId: number, event: Event) {
  const destino = event.target
  if (!(destino instanceof HTMLSelectElement)) {
    return
  }
  void onCambiarEstadoEpp(itemId, destino.value as EstadoItemEpp)
}

async function onDescargarKardex(personaId: number, nombre: string) {
  try {
    await descargarKardexEpp(proyectoId.value, personaId, `kardex-epp-${personaId}.pdf`)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : `No se pudo descargar el kardex de ${nombre}.`)
  }
}

watch(proyectoId, () => {
  void load()
})

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs v-model="tab">
      <TabsList :class="tabsListClass">
        <TabsTrigger value="resumen" :class="tabTriggerClass">Resumen</TabsTrigger>
        <TabsTrigger value="vigilancia" :class="tabTriggerClass">Vigilancia médica</TabsTrigger>
        <TabsTrigger value="sctr" :class="tabTriggerClass">SCTR</TabsTrigger>
        <TabsTrigger value="epp" :class="tabTriggerClass">EPP</TabsTrigger>
      </TabsList>

      <TabsContent value="resumen" class="mt-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-16 w-full" />
          </div>
        </div>
        <div v-else class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <h3 class="text-base font-semibold text-palette-navy">Resumen SOMA</h3>
          <dl class="mt-4 grid gap-4 sm:grid-cols-2">
            <div>
              <dt class="text-sm font-medium text-slate-500">Obra</dt>
              <dd class="mt-1 text-sm text-slate-900">
                {{ resumen ? `${resumen.codigoObra} · ${resumen.nombreObra}` : '—' }}
              </dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">Supervisor SSOMA</dt>
              <dd class="mt-1 text-sm text-slate-900">{{ resumen?.supervisorSsoma ?? 'Sin asignar' }}</dd>
            </div>
          </dl>
        </div>
      </TabsContent>

      <TabsContent value="vigilancia" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar EMO</h3>
            <p class="mt-1 text-sm text-slate-500">
              El certificado CAMO se sube como documento de obra. Solo el último examen sincroniza la aptitud de la persona.
            </p>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2" @submit.prevent="onRegistrar">
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-persona">Persona</label>
                <Combobox
                  :model-value="form.personaId ?? undefined"
                  ignore-filter
                  class="min-w-0 w-full"
                  @update:model-value="onPersonaChange"
                >
                  <ComboboxAnchor class="min-w-0 w-full">
                    <ComboboxTrigger as-child>
                      <Button
                        id="soma-emo-persona"
                        variant="outline"
                        class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                        type="button"
                      >
                        <span
                          class="min-w-0 flex-1 truncate text-left"
                          :class="personaLabel(personaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                        >
                          {{ personaLabel(personaSeleccionada) || 'Buscar personal de la obra' }}
                        </span>
                        <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                      </Button>
                    </ComboboxTrigger>
                  </ComboboxAnchor>
                  <ComboboxList
                    class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)"
                    align="start"
                  >
                    <ComboboxInput v-model="personaSearch" placeholder="Buscar persona..." autocomplete="off" />
                    <ComboboxEmpty>Sin personal en esta obra.</ComboboxEmpty>
                    <ComboboxGroup>
                      <ComboboxItem
                        v-for="persona in opcionesPersona"
                        :key="persona.personaId"
                        :value="persona.personaId"
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
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-tipo">Tipo</label>
                <select id="soma-emo-tipo" v-model="form.tipoEmo" :class="selectClass" class="mt-1.5">
                  <option v-for="item in TIPOS_EMO" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-aptitud">Aptitud</label>
                <select id="soma-emo-aptitud" v-model="form.aptitud" :class="selectClass" class="mt-1.5">
                  <option v-for="item in APTITUDES" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                </select>
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-clinica">Clínica</label>
                <Input
                  id="soma-emo-clinica"
                  v-model="form.clinicaNombre"
                  :class="inputClass"
                  class="mt-1.5"
                  maxlength="160"
                  required
                />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-examen">Fecha de examen</label>
                <Input
                  id="soma-emo-examen"
                  v-model="form.fechaExamen"
                  type="date"
                  :class="inputClass"
                  class="mt-1.5"
                  required
                />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-vence">Vencimiento</label>
                <Input
                  id="soma-emo-vence"
                  v-model="form.fechaVencimiento"
                  type="date"
                  :class="inputClass"
                  class="mt-1.5"
                  required
                />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-restricciones">Restricciones</label>
                <Textarea id="soma-emo-restricciones" v-model="form.restricciones" class="mt-1.5 min-h-16" maxlength="2000" />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-camo">Certificado CAMO</label>
                <input
                  id="soma-emo-camo"
                  ref="fileInput"
                  class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                >
              </div>
              <div class="flex justify-end sm:col-span-2">
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="saving"
                >
                  Registrar EMO
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="mb-3 text-base font-semibold text-palette-navy">Historial de EMO</h3>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Persona</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Aptitud</TableHead>
                  <TableHead>Examen</TableHead>
                  <TableHead>Vence</TableHead>
                  <TableHead>Clínica</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="item in emos" :key="item.id">
                  <TableCell>{{ item.personaNombre ?? '—' }}</TableCell>
                  <TableCell>{{ tipoLabel(item.tipoEmo) }}</TableCell>
                  <TableCell>
                    <div class="flex flex-wrap gap-1">
                      <Badge :variant="aptitudBadgeVariant(item)">{{ aptitudLabel(item.aptitud) }}</Badge>
                      <Badge v-if="emoVencido(item)" variant="secondary">Vencido</Badge>
                    </div>
                  </TableCell>
                  <TableCell>{{ fechaLabel(item.fechaExamen) }}</TableCell>
                  <TableCell>{{ fechaLabel(item.fechaVencimiento) }}</TableCell>
                  <TableCell>
                    <p>{{ item.clinicaNombre }}</p>
                    <p v-if="item.restricciones" class="text-xs text-slate-500">{{ item.restricciones }}</p>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!emos.length" :colspan="6">Sin exámenes médicos registrados en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="sctr" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar póliza SCTR</h3>
            <p class="mt-1 text-sm text-slate-500">
              Una póliza por mes y obra. El certificado se sube como documento de obra. El checklist SCTR solo se
              actualiza si la póliza corresponde al mes en curso.
            </p>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4" @submit.prevent="onCrearPoliza">
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-anio">Año</label>
                <select
                  id="soma-sctr-anio"
                  v-model.number="sctrForm.anio"
                  :class="selectClass"
                  class="mt-1.5"
                  @change="sincronizarFechasSctr"
                >
                  <option v-for="anio in ANIOS" :key="anio" :value="anio">{{ anio }}</option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-mes">Mes</label>
                <select
                  id="soma-sctr-mes"
                  v-model.number="sctrForm.mes"
                  :class="selectClass"
                  class="mt-1.5"
                  @change="sincronizarFechasSctr"
                >
                  <option v-for="(nombre, index) in MESES" :key="nombre" :value="index + 1">{{ nombre }}</option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-ini">Inicio</label>
                <Input id="soma-sctr-ini" v-model="sctrForm.fechaInicio" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-fin">Fin</label>
                <Input id="soma-sctr-fin" v-model="sctrForm.fechaFin" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-aseg-salud">Aseguradora salud</label>
                <Input id="soma-sctr-aseg-salud" v-model="sctrForm.aseguradoraSalud" :class="inputClass" class="mt-1.5" maxlength="120" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-nro-salud">Póliza salud</label>
                <Input id="soma-sctr-nro-salud" v-model="sctrForm.numeroPolizaSalud" :class="inputClass" class="mt-1.5" maxlength="60" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-aseg-pen">Aseguradora pensión</label>
                <Input id="soma-sctr-aseg-pen" v-model="sctrForm.aseguradoraPension" :class="inputClass" class="mt-1.5" maxlength="120" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-nro-pen">Póliza pensión</label>
                <Input id="soma-sctr-nro-pen" v-model="sctrForm.numeroPolizaPension" :class="inputClass" class="mt-1.5" maxlength="60" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-prima-salud">Prima salud</label>
                <Input id="soma-sctr-prima-salud" v-model="sctrForm.montoPrimaSalud" :class="inputClass" class="mt-1.5" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-prima-pen">Prima pensión</label>
                <Input id="soma-sctr-prima-pen" v-model="sctrForm.montoPrimaPension" :class="inputClass" class="mt-1.5" />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-file">Póliza (PDF o imagen)</label>
                <input
                  id="soma-sctr-file"
                  ref="sctrFileInput"
                  class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                >
              </div>
              <div class="flex items-end">
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="sctrSaving"
                >
                  Registrar póliza
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="mb-3 text-base font-semibold text-palette-navy">Pólizas del mes</h3>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Periodo</TableHead>
                  <TableHead>Salud</TableHead>
                  <TableHead>Pensión</TableHead>
                  <TableHead>Prima</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="item in polizas" :key="item.id">
                  <TableCell>{{ mesLabel(item.mes) }} {{ item.anio }}</TableCell>
                  <TableCell>
                    <p>{{ item.aseguradoraSalud }}</p>
                    <p class="text-xs text-slate-500">{{ item.numeroPolizaSalud }}</p>
                  </TableCell>
                  <TableCell>
                    <p>{{ item.aseguradoraPension }}</p>
                    <p class="text-xs text-slate-500">{{ item.numeroPolizaPension }}</p>
                  </TableCell>
                  <TableCell>{{ money.format(item.montoPrimaSalud + item.montoPrimaPension) }}</TableCell>
                  <TableCell>
                    <Badge :variant="item.estado === 'VIGENTE' ? 'success' : 'secondary'">{{ item.estado === 'VIGENTE' ? 'Vigente' : 'Anulada' }}</Badge>
                  </TableCell>
                  <TableCell class="space-x-2 whitespace-nowrap">
                    <Button variant="outline" size="sm" type="button" @click="seleccionarPoliza(item)">Ver nómina</Button>
                    <Button
                      v-if="item.estado === 'VIGENTE'"
                      variant="outline"
                      size="sm"
                      type="button"
                      :disabled="sctrSaving"
                      @click="onAnularPoliza(item)"
                    >
                      Anular
                    </Button>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!polizas.length" :colspan="6">Sin pólizas SCTR en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div v-if="polizaSeleccionada" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div>
                <h3 class="text-base font-semibold text-palette-navy">
                  Nómina · {{ mesLabel(polizaSeleccionada.mes) }} {{ polizaSeleccionada.anio }}
                </h3>
                <p class="mt-1 text-sm text-slate-500">
                  Cubierto = salud y pensión marcados. Precargar trae a los trabajadores con contrato vigente en el mes.
                </p>
              </div>
              <div class="flex flex-wrap gap-2">
                <Button
                  variant="outline"
                  type="button"
                  :disabled="sctrSaving || polizaSeleccionada.estado === 'ANULADA'"
                  @click="onPrecargarNomina"
                >
                  Precargar trabajadores
                </Button>
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="button"
                  :disabled="sctrSaving || polizaSeleccionada.estado === 'ANULADA'"
                  @click="onGuardarNomina"
                >
                  Guardar nómina
                </Button>
              </div>
            </div>
            <Table class="mt-4">
              <TableHeader>
                <TableRow>
                  <TableHead>Persona</TableHead>
                  <TableHead>Documento</TableHead>
                  <TableHead class="w-24">Salud</TableHead>
                  <TableHead class="w-24">Pensión</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in nominaEdicion" :key="fila.personaId">
                  <TableCell>{{ fila.personaNombre ?? '—' }}</TableCell>
                  <TableCell>{{ fila.numeroDocumento ?? '—' }}</TableCell>
                  <TableCell>
                    <input
                      v-model="fila.incluidoSalud"
                      type="checkbox"
                      class="size-4 rounded border-slate-300"
                      :disabled="polizaSeleccionada.estado === 'ANULADA'"
                    >
                  </TableCell>
                  <TableCell>
                    <input
                      v-model="fila.incluidoPension"
                      type="checkbox"
                      class="size-4 rounded border-slate-300"
                      :disabled="polizaSeleccionada.estado === 'ANULADA'"
                    >
                  </TableCell>
                  <TableCell>
                    <Button
                      v-if="polizaSeleccionada.estado === 'VIGENTE'"
                      variant="outline"
                      size="sm"
                      type="button"
                      @click="quitarDeNomina(fila.personaId)"
                    >
                      Quitar
                    </Button>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!nominaEdicion.length" :colspan="5">Sin trabajadores en la nómina.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="epp" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar entrega de EPP</h3>
            <p class="mt-1 text-sm text-slate-500">
              El entregado por se toma del usuario autenticado. Si se descuenta de almacén se genera un vale de
              salida con la partida vigente.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onRegistrarEpp">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-persona">Persona</label>
                  <Combobox
                    :model-value="eppForm.personaId ?? undefined"
                    ignore-filter
                    class="min-w-0 w-full"
                    @update:model-value="onEppPersonaChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-epp-persona"
                          variant="outline"
                          class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(eppPersonaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(eppPersonaSeleccionada) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList
                      class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)"
                      align="start"
                    >
                      <ComboboxInput v-model="eppPersonaSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal en esta obra.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesEppPersona"
                          :key="persona.personaId"
                          :value="persona.personaId"
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
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-motivo">Motivo</label>
                  <select id="soma-epp-motivo" v-model="eppForm.motivo" :class="selectClass" class="mt-1.5">
                    <option v-for="item in MOTIVOS_EPP" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-fecha">Fecha</label>
                  <Input id="soma-epp-fecha" v-model="eppForm.fechaEntrega" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                    <input v-model="eppForm.descontarAlmacen" type="checkbox" />
                    Descontar de almacén
                  </label>
                </div>
                <div v-if="eppForm.descontarAlmacen">
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-partida">Partida</label>
                  <select id="soma-epp-partida" v-model="eppForm.partidaId" :class="selectClass" class="mt-1.5">
                    <option value="">Seleccione partida</option>
                    <option v-for="partida in partidasVigentes" :key="partida.id" :value="String(partida.id)">
                      {{ partida.codigo }} — {{ partida.descripcion }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                    <input v-model="eppForm.firmadoTrabajador" type="checkbox" />
                    Firmado por el trabajador
                  </label>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-obs">Observaciones</label>
                  <Textarea id="soma-epp-obs" v-model="eppForm.observaciones" class="mt-1.5" maxlength="400" />
                </div>
              </div>

              <div>
                <p class="text-sm font-medium text-slate-700">Ítems</p>
                <div class="mt-2 space-y-2">
                  <div
                    v-for="(item, index) in eppItems"
                    :key="index"
                    class="grid gap-2 grid-cols-1 sm:grid-cols-[1fr_8rem_8rem_auto]"
                  >
                    <select v-model="item.materialId" :class="selectClass">
                      <option value="">Material EPP</option>
                      <option v-for="material in materialesEpp" :key="material.materialId" :value="String(material.materialId)">
                        {{ material.codigo }} — {{ material.descripcion }} (stock {{ qtyFmt.format(material.stock) }})
                      </option>
                    </select>
                    <Input v-model="item.cantidad" :class="inputClass" placeholder="Cantidad" />
                    <Input v-model="item.talla" :class="inputClass" placeholder="Talla" maxlength="20" />
                    <Button variant="outline" type="button" @click="quitarItemEpp(index)">Quitar</Button>
                  </div>
                </div>
                <Button class="mt-3" variant="outline" type="button" @click="agregarItemEpp">Agregar ítem</Button>
              </div>

              <Button
                class="bg-palette-navy text-white hover:bg-palette-royal"
                type="submit"
                :disabled="eppSaving"
              >
                Registrar entrega
              </Button>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Historial por trabajador</h3>
            <p v-if="!historialEppPorPersona.length" class="mt-3 text-sm text-slate-500">
              Aún no hay entregas de EPP en esta obra.
            </p>
            <div v-for="grupo in historialEppPorPersona" :key="grupo.personaId" class="mt-4 space-y-2">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <p class="text-sm font-medium text-slate-800">
                  {{ grupo.personaNombre }}
                  <span v-if="grupo.numeroDocumento" class="font-normal text-slate-500">· {{ grupo.numeroDocumento }}</span>
                </p>
                <Button variant="outline" size="sm" type="button" @click="onDescargarKardex(grupo.personaId, grupo.personaNombre)">
                  Kardex PDF
                </Button>
              </div>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Fecha</TableHead>
                    <TableHead>Material</TableHead>
                    <TableHead>Cantidad</TableHead>
                    <TableHead>Talla</TableHead>
                    <TableHead>Motivo</TableHead>
                    <TableHead>Vencimiento</TableHead>
                    <TableHead>Estado</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <template v-for="entrega in grupo.entregas" :key="entrega.id">
                    <TableRow v-for="item in entrega.items" :key="item.id">
                      <TableCell>{{ fechaLabel(entrega.fechaEntrega) }}</TableCell>
                      <TableCell>{{ item.materialCodigo }} — {{ item.materialDescripcion }}</TableCell>
                      <TableCell>{{ qtyFmt.format(item.cantidad) }}</TableCell>
                      <TableCell>{{ item.talla || '—' }}</TableCell>
                      <TableCell>{{ motivoEppLabel(entrega.motivo) }}</TableCell>
                      <TableCell>{{ fechaLabel(item.fechaVencimientoEstimada) }}</TableCell>
                      <TableCell>
                        <div class="flex flex-wrap items-center gap-2">
                          <Badge :variant="estadoEppBadge(item.estadoItem)">{{ estadoEppLabel(item.estadoItem) }}</Badge>
                          <select
                            :class="selectClass"
                            :value="item.estadoItem"
                            @change="onEstadoSelect(item.id, $event)"
                          >
                            <option v-for="estado in ESTADOS_EPP" :key="estado.valor" :value="estado.valor">
                              {{ estado.label }}
                            </option>
                          </select>
                        </div>
                      </TableCell>
                    </TableRow>
                  </template>
                </TableBody>
              </Table>
            </div>
          </div>
        </template>
      </TabsContent>
    </Tabs>
  </div>
</template>
