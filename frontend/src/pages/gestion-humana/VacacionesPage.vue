<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Textarea } from '@/components/ui/textarea'
import { obtenerUltimoContrato } from '@/services/contrato/contratoService'
import { listarPersonas } from '@/services/persona/personaService'
import {
  abrirDocumentoVacacion,
  aprobarVacacion,
  crearVacacion,
  eliminarVacacion,
  listarVacaciones,
  obtenerResumenVacaciones,
  obtenerSaldoVacaciones,
  rechazarVacacion,
} from '@/services/vacacion/vacacionService'
import type { ContratoDTO } from '@/types/contrato'
import type { PagedResponse, PersonaResumenDTO } from '@/types/persona'
import {
  TIPOS_VACACION,
  type EstadoVacacion,
  type TipoVacacion,
  type VacacionDTO,
  type VacacionResumen,
  type VacacionSaldo,
} from '@/types/vacacion'

const selectClass =
  'mt-1.5 block h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-900 outline-none focus:border-sky-600 focus:ring-2 focus:ring-sky-500/20 disabled:bg-slate-100'
const PAGE_SIZE = 15
const loading = ref(true)
const saving = ref(false)
const page = ref(0)
const data = ref<PagedResponse<VacacionDTO> | null>(null)
const resumen = ref<VacacionResumen | null>(null)
const personas = ref<PersonaResumenDTO[]>([])
const contrato = ref<ContratoDTO | null>(null)
const saldo = ref<VacacionSaldo | null>(null)
const q = ref('')
const filtroTipo = ref<TipoVacacion | ''>('')
const filtroEstado = ref<EstadoVacacion | ''>('')
const confirmacion = ref<{ titulo: string; detalle: string; ejecutar: () => Promise<void> } | null>(null)
let saldoTimer: ReturnType<typeof setTimeout> | undefined
let searchTimer: ReturnType<typeof setTimeout> | undefined

const form = reactive({
  personaId: 0,
  tipo: 'ORDINARIA' as TipoVacacion,
  periodoInicio: '',
  periodoFin: '',
  diasDerechoPeriodo: 30,
  diasSolicitados: 1,
  fechaInicio: '',
  observacion: '',
})

const fechaFinCalculada = computed(() => sumarDias(form.fechaInicio, form.diasSolicitados - 1))
const fechaRetornoCalculada = computed(() => sumarDias(form.fechaInicio, form.diasSolicitados))
const personaSeleccionada = computed(() => personas.value.find((item) => item.id === form.personaId) ?? null)

const resumenCards = computed(() => [
  { label: 'Solicitudes', value: resumen.value?.totalSolicitudes ?? 0 },
  { label: 'Pendientes', value: resumen.value?.pendientes ?? 0 },
  { label: 'Aprobadas', value: resumen.value?.aprobadas ?? 0 },
  { label: 'Días aprobados', value: resumen.value?.diasAprobados ?? 0 },
])

function sumarDias(value: string, days: number): string {
  if (!value || days < 0 || !Number.isFinite(days)) return ''
  const date = new Date(`${value}T12:00:00`)
  if (Number.isNaN(date.getTime())) return ''
  date.setDate(date.getDate() + days)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function formatFecha(value: string | null): string {
  if (!value) return '—'
  return new Date(`${value}T12:00:00`).toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' })
}

function tipoLabel(tipo: TipoVacacion): string {
  return TIPOS_VACACION.find((item) => item.value === tipo)?.label ?? tipo
}

function estadoLabel(item: VacacionDTO): string {
  if (item.gozada) return 'Gozada'
  if (item.estado === 'APROBADA') return 'Aprobada / programada'
  if (item.estado === 'RECHAZADA') return 'Rechazada'
  return 'Pendiente'
}

function estadoClass(item: VacacionDTO): string {
  if (item.gozada) return 'border-emerald-200 bg-emerald-50 text-emerald-800'
  if (item.estado === 'APROBADA') return 'border-sky-200 bg-sky-50 text-sky-800'
  if (item.estado === 'RECHAZADA') return 'border-red-200 bg-red-50 text-red-800'
  return 'border-amber-200 bg-amber-50 text-amber-900'
}

function sugerirPeriodo(fechaIngreso: string | null) {
  if (!fechaIngreso) {
    const year = new Date().getFullYear()
    form.periodoInicio = `${year}-01-01`
    form.periodoFin = `${year}-12-31`
    return
  }
  const ingreso = new Date(`${fechaIngreso}T12:00:00`)
  const hoy = new Date()
  let year = hoy.getFullYear()
  const aniversario = new Date(year, ingreso.getMonth(), ingreso.getDate(), 12)
  if (hoy < aniversario) year -= 1
  const inicio = new Date(year, ingreso.getMonth(), ingreso.getDate(), 12)
  const fin = new Date(year + 1, ingreso.getMonth(), ingreso.getDate(), 12)
  fin.setDate(fin.getDate() - 1)
  form.periodoInicio = fechaIsoLocal(inicio)
  form.periodoFin = fechaIsoLocal(fin)
}

function fechaIsoLocal(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

async function cargarPersonaContrato() {
  contrato.value = null
  saldo.value = null
  if (!form.personaId) return
  try {
    contrato.value = await obtenerUltimoContrato(form.personaId)
    sugerirPeriodo(contrato.value.fechaInicio)
    const configurados = Number(contrato.value.metadata?.dias_vacaciones_anuales)
    form.diasDerechoPeriodo = Number.isFinite(configurados) && configurados > 0 ? configurados : 30
    await cargarSaldo()
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'La persona no tiene un contrato activo.')
  }
}

async function cargarSaldo() {
  if (!form.personaId || !form.periodoInicio || !form.periodoFin || form.diasDerechoPeriodo < 1) {
    saldo.value = null
    return
  }
  try {
    saldo.value = await obtenerSaldoVacaciones(
      form.personaId,
      form.periodoInicio,
      form.periodoFin,
      form.diasDerechoPeriodo,
    )
  } catch {
    saldo.value = null
  }
}

async function cargarListado() {
  loading.value = true
  try {
    const [nextData, nextResumen] = await Promise.all([
      listarVacaciones({ page: page.value, size: PAGE_SIZE, q: q.value, tipo: filtroTipo.value, estado: filtroEstado.value }),
      obtenerResumenVacaciones(),
    ])
    data.value = nextData
    resumen.value = nextResumen
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo cargar vacaciones.')
  } finally {
    loading.value = false
  }
}

async function cargarPersonas() {
  try {
    const response = await listarPersonas({ page: 0, size: 100, sort: 'apellidos,asc' })
    personas.value = response.content.filter((item) => item.activo)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo cargar el personal.')
  }
}

async function guardar() {
  if (!form.personaId || !form.fechaInicio || !form.periodoInicio || !form.periodoFin) {
    toast.error('Complete la persona, el periodo acumulativo y la fecha de inicio.')
    return
  }
  saving.value = true
  try {
    await crearVacacion({
      personaId: form.personaId,
      tipo: form.tipo,
      periodoInicio: form.periodoInicio,
      periodoFin: form.periodoFin,
      diasDerechoPeriodo: Number(form.diasDerechoPeriodo),
      diasSolicitados: Number(form.diasSolicitados),
      fechaInicio: form.fechaInicio,
      observacion: form.observacion.trim() || null,
    })
    toast.success('Solicitud registrada. Queda pendiente de aprobación.')
    form.fechaInicio = ''
    form.diasSolicitados = 1
    form.observacion = ''
    page.value = 0
    await Promise.all([cargarListado(), cargarSaldo()])
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo registrar la solicitud.')
  } finally {
    saving.value = false
  }
}

function pedirConfirmacion(titulo: string, detalle: string, ejecutar: () => Promise<void>) {
  confirmacion.value = { titulo, detalle, ejecutar }
}

async function ejecutarConfirmacion() {
  const accion = confirmacion.value
  if (!accion) return
  saving.value = true
  try {
    await accion.ejecutar()
    confirmacion.value = null
    await Promise.all([cargarListado(), cargarSaldo()])
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo completar la acción.')
  } finally {
    saving.value = false
  }
}

watch(() => form.personaId, () => void cargarPersonaContrato())
watch(
  () => [form.periodoInicio, form.periodoFin, form.diasDerechoPeriodo],
  () => {
    clearTimeout(saldoTimer)
    saldoTimer = setTimeout(() => void cargarSaldo(), 250)
  },
)
watch([q, filtroTipo, filtroEstado], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 0
    void cargarListado()
  }, 300)
})

onMounted(() => {
  void Promise.all([cargarPersonas(), cargarListado()])
})
</script>

<template>
  <section class="min-w-0 space-y-3 overflow-x-hidden pb-8">
    <header>
      <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Vacaciones</h2>
      <p class="mt-2 max-w-2xl text-sm leading-relaxed text-slate-500">
        Registre solicitudes, apruebe convenios y sincronice el tareo. Las fechas de término y retorno se calculan solas.
      </p>
    </header>

    <dl class="grid gap-2 sm:grid-cols-2 xl:grid-cols-4">
      <div
        v-for="card in resumenCards"
        :key="card.label"
        class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-3"
      >
        <dt class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ card.label }}</dt>
        <dd class="mt-1 text-2xl font-bold tabular-nums text-palette-navy">{{ card.value }}</dd>
      </div>
    </dl>

    <div class="grid items-start gap-3 xl:grid-cols-[minmax(18rem,24rem)_minmax(0,1fr)]">
      <form
        class="rounded-lg border border-palette-cyan/70 bg-white p-4 sm:p-5"
        @submit.prevent="guardar"
      >
        <div class="border-b border-slate-100 pb-3">
          <h3 class="text-base font-semibold text-palette-navy">Nueva solicitud</h3>
          <p class="mt-1 text-xs text-slate-500">D. L. 1405 · D. S. 002-2019-TR</p>
        </div>

        <div class="mt-4 space-y-3">
          <label class="block text-sm font-medium text-slate-700">
            Persona
            <select v-model.number="form.personaId" :class="selectClass" required>
              <option :value="0" disabled>Seleccione una persona</option>
              <option v-for="persona in personas" :key="persona.id" :value="persona.id">
                {{ persona.apellidos }}, {{ persona.nombres }} · {{ persona.numeroDocumento }}
              </option>
            </select>
          </label>

          <div
            v-if="personaSeleccionada"
            class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-xs text-slate-600"
          >
            <p>
              <span class="font-semibold text-slate-800">Contrato:</span>
              {{ contrato?.titulo ?? 'Consultando…' }}
            </p>
            <p class="mt-1">
              <span class="font-semibold text-slate-800">Ingreso:</span>
              {{ formatFecha(contrato?.fechaInicio ?? null) }}
              ·
              <span class="font-semibold text-slate-800">Cargo:</span>
              {{ contrato?.metadata?.cargo || 'Sin cargo registrado' }}
            </p>
          </div>

          <label class="block text-sm font-medium text-slate-700">
            Tipo de vacaciones
            <select v-model="form.tipo" :class="selectClass">
              <option v-for="tipo in TIPOS_VACACION" :key="tipo.value" :value="tipo.value">
                {{ tipo.label }}
              </option>
            </select>
          </label>

          <fieldset class="rounded-lg border border-slate-200 bg-slate-50 p-3">
            <legend class="px-1 text-xs font-semibold uppercase tracking-wide text-slate-600">
              Periodo acumulativo
            </legend>
            <div class="grid gap-3 sm:grid-cols-2">
              <label class="text-xs font-medium text-slate-600">
                Desde
                <Input v-model="form.periodoInicio" type="date" class="mt-1 h-10" required />
              </label>
              <label class="text-xs font-medium text-slate-600">
                Hasta
                <Input v-model="form.periodoFin" type="date" class="mt-1 h-10" required />
              </label>
            </div>
            <label class="mt-3 block text-xs font-medium text-slate-600">
              Días de derecho del periodo
              <Input
                v-model.number="form.diasDerechoPeriodo"
                type="number"
                min="1"
                max="60"
                class="mt-1 h-10"
                required
              />
            </label>
            <div v-if="saldo" class="mt-3 grid grid-cols-3 gap-2 text-center">
              <div class="rounded-md border border-slate-200 bg-white px-2 py-2">
                <p class="text-[10px] font-semibold uppercase tracking-wide text-slate-500">Aprobados</p>
                <p class="mt-0.5 text-sm font-bold tabular-nums text-slate-800">{{ saldo.diasAprobados }}</p>
              </div>
              <div class="rounded-md border border-amber-200 bg-amber-50 px-2 py-2">
                <p class="text-[10px] font-semibold uppercase tracking-wide text-amber-800">Pendientes</p>
                <p class="mt-0.5 text-sm font-bold tabular-nums text-amber-900">{{ saldo.diasPendientes }}</p>
              </div>
              <div class="rounded-md border border-emerald-200 bg-emerald-50 px-2 py-2">
                <p class="text-[10px] font-semibold uppercase tracking-wide text-emerald-800">Disponibles</p>
                <p class="mt-0.5 text-sm font-bold tabular-nums text-emerald-900">{{ saldo.diasDisponibles }}</p>
              </div>
            </div>
          </fieldset>

          <div class="grid gap-3 sm:grid-cols-2">
            <label class="text-sm font-medium text-slate-700">
              Días calendario
              <Input
                v-model.number="form.diasSolicitados"
                type="number"
                min="1"
                max="30"
                class="mt-1.5 h-10"
                required
              />
            </label>
            <label class="text-sm font-medium text-slate-700">
              Fecha de inicio
              <Input v-model="form.fechaInicio" type="date" class="mt-1.5 h-10" required />
            </label>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-palette-ice/40 px-3 py-2.5 text-sm">
            <div class="flex justify-between gap-3">
              <span class="text-slate-600">Último día</span>
              <strong class="tabular-nums text-palette-navy">{{ formatFecha(fechaFinCalculada) }}</strong>
            </div>
            <div class="mt-1.5 flex justify-between gap-3">
              <span class="text-slate-600">Retorno laboral</span>
              <strong class="tabular-nums text-emerald-700">{{ formatFecha(fechaRetornoCalculada) }}</strong>
            </div>
          </div>

          <label class="block text-sm font-medium text-slate-700">
            Observaciones / sustento
            <Textarea
              v-model="form.observacion"
              class="mt-1.5 min-h-20"
              maxlength="2000"
              placeholder="Acuerdo, sustento o detalle interno"
            />
          </label>
        </div>

        <Button
          class="mt-4 h-10 w-full bg-palette-navy text-white hover:bg-palette-royal"
          type="submit"
          :disabled="saving || !contrato"
        >
          {{ saving ? 'Registrando…' : 'Registrar solicitud' }}
        </Button>
      </form>

      <div class="min-w-0 rounded-lg border border-palette-cyan/70 bg-white">
        <div class="border-b border-slate-100 px-4 py-4 sm:px-5">
          <h3 class="text-base font-semibold text-palette-navy">Solicitudes</h3>
          <p class="mt-1 text-sm text-slate-500">Filtre por persona, tipo o estado.</p>
          <div class="mt-3 grid gap-2 md:grid-cols-[minmax(0,1fr)_10rem_10rem]">
            <Input v-model="q" type="search" class="h-10" placeholder="Nombre o documento" />
            <select v-model="filtroTipo" :class="`${selectClass} mt-0`">
              <option value="">Todos los tipos</option>
              <option v-for="tipo in TIPOS_VACACION" :key="tipo.value" :value="tipo.value">
                {{ tipo.label }}
              </option>
            </select>
            <select v-model="filtroEstado" :class="`${selectClass} mt-0`">
              <option value="">Todos los estados</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="APROBADA">Aprobada</option>
              <option value="RECHAZADA">Rechazada</option>
            </select>
          </div>
        </div>

        <div v-if="loading && !data" class="space-y-3 p-5">
          <Skeleton v-for="item in 5" :key="item" class="h-14 w-full" />
        </div>
        <div v-else class="overflow-x-auto">
          <Table class="min-w-[56rem]">
            <TableHeader>
              <TableRow>
                <TableHead>Persona</TableHead>
                <TableHead>Tipo / periodo</TableHead>
                <TableHead>Descanso</TableHead>
                <TableHead>Saldo</TableHead>
                <TableHead>Estado</TableHead>
                <TableHead class="text-right">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableEmpty v-if="!data?.content.length" :colspan="6">
                No hay solicitudes con los filtros elegidos.
              </TableEmpty>
              <TableRow v-for="item in data?.content" :key="item.id">
                <TableCell>
                  <p class="font-semibold text-slate-900">{{ item.personaNombreCompleto }}</p>
                  <p class="mt-1 text-xs text-slate-500">
                    {{ item.personaNumeroDocumento }} · {{ item.cargo || 'Sin cargo' }}
                  </p>
                </TableCell>
                <TableCell>
                  <Badge variant="secondary">{{ tipoLabel(item.tipo) }}</Badge>
                  <p class="mt-2 text-xs text-slate-500">
                    {{ formatFecha(item.periodoInicio) }} – {{ formatFecha(item.periodoFin) }}
                  </p>
                </TableCell>
                <TableCell>
                  <p class="font-medium text-slate-800">{{ item.diasSolicitados }} días</p>
                  <p class="mt-1 text-xs text-slate-500">
                    {{ formatFecha(item.fechaInicio) }} – {{ formatFecha(item.fechaFin) }}
                  </p>
                  <p class="mt-1 text-xs font-medium text-emerald-700">
                    Retorno: {{ formatFecha(item.fechaRetorno) }}
                  </p>
                </TableCell>
                <TableCell>
                  <p class="font-semibold text-slate-800">{{ item.diasDisponiblesPeriodo }} disponibles</p>
                  <p class="mt-1 text-xs text-slate-500">
                    {{ item.diasUsadosPeriodo }} de {{ item.diasDerechoPeriodo }} aprobados
                  </p>
                </TableCell>
                <TableCell>
                  <span
                    class="inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold"
                    :class="estadoClass(item)"
                  >
                    {{ estadoLabel(item) }}
                  </span>
                </TableCell>
                <TableCell class="text-right">
                  <div class="flex flex-wrap justify-end gap-1">
                    <Button variant="outline" size="sm" type="button" @click="abrirDocumentoVacacion(item.id)">
                      Documento
                    </Button>
                    <template v-if="item.estado === 'PENDIENTE'">
                      <Button
                        size="sm"
                        class="bg-emerald-700 text-white hover:bg-emerald-800"
                        type="button"
                        @click="pedirConfirmacion(
                          'Aprobar vacaciones',
                          `Se marcarán ${item.diasSolicitados} días como VAC en el tareo existente.`,
                          async () => {
                            await aprobarVacacion(item.id)
                            toast.success('Vacaciones aprobadas y tareo sincronizado.')
                          },
                        )"
                      >
                        Aprobar
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        class="border-red-200 text-red-700 hover:bg-red-50"
                        type="button"
                        @click="pedirConfirmacion(
                          'Rechazar solicitud',
                          'La solicitud quedará registrada como rechazada y no afectará el tareo.',
                          async () => {
                            await rechazarVacacion(item.id)
                            toast.success('Solicitud rechazada.')
                          },
                        )"
                      >
                        Rechazar
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        class="text-slate-500"
                        type="button"
                        @click="pedirConfirmacion(
                          'Eliminar solicitud',
                          'Esta acción elimina definitivamente la solicitud pendiente.',
                          async () => {
                            await eliminarVacacion(item.id)
                            toast.success('Solicitud eliminada.')
                          },
                        )"
                      >
                        Eliminar
                      </Button>
                    </template>
                  </div>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>

        <footer
          v-if="data && data.totalPages > 1"
          class="flex flex-col gap-2 border-t border-slate-100 px-4 py-3 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between"
        >
          <span>Página {{ data.page + 1 }} de {{ data.totalPages }} · {{ data.totalElements }} registros</span>
          <div class="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              type="button"
              :disabled="data.first"
              @click="page--; cargarListado()"
            >
              Anterior
            </Button>
            <Button
              variant="outline"
              size="sm"
              type="button"
              :disabled="data.last"
              @click="page++; cargarListado()"
            >
              Siguiente
            </Button>
          </div>
        </footer>
      </div>
    </div>

    <aside class="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-950">
      <p class="font-semibold text-amber-900">Integración con tareo y nómina</p>
      <p class="mt-1 leading-relaxed text-amber-950/90">
        Las solicitudes nacen pendientes. Al aprobarlas se reserva el saldo del periodo y se marca el tareo
        existente con <strong>VAC</strong>. Si el mes aún no tiene tareo, las marcas se aplican al generarlo.
        En nómina los días VAC mantienen la remuneración básica.
      </p>
    </aside>

    <Teleport to="body">
      <div v-if="confirmacion" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <button
          class="absolute inset-0 bg-slate-950/45"
          type="button"
          aria-label="Cerrar confirmación"
          @click="!saving && (confirmacion = null)"
        />
        <section
          class="relative w-full max-w-md rounded-xl border border-slate-200 bg-white p-5 shadow-xl"
          role="dialog"
          aria-modal="true"
        >
          <h3 class="text-lg font-semibold text-palette-navy">{{ confirmacion.titulo }}</h3>
          <p class="mt-2 text-sm leading-relaxed text-slate-600">{{ confirmacion.detalle }}</p>
          <div class="mt-5 flex justify-end gap-2">
            <Button variant="outline" type="button" :disabled="saving" @click="confirmacion = null">
              Cancelar
            </Button>
            <Button
              class="bg-palette-navy text-white hover:bg-palette-royal"
              type="button"
              :disabled="saving"
              @click="ejecutarConfirmacion"
            >
              Confirmar
            </Button>
          </div>
        </section>
      </div>
    </Teleport>
  </section>
</template>
