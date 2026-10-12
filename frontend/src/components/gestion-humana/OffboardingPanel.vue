<script setup lang="ts">
import {
  CheckIcon,
  ChevronsUpDownIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  DownloadIcon,
  PrinterIcon,
} from '@lucide/vue'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
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
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
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
import { openAuthenticatedDocument } from '@/services/http'
import {
  crearGeneracionOffboarding,
  documentoOffboardingUrl,
  listarCeses,
  listarPlantillasOffboarding,
  previewLiquidacion,
  registrarCese,
} from '@/services/offboarding/offboardingService'
import { listarPersonas } from '@/services/persona/personaService'
import type { Cese, LiquidacionPreview, TipoCese } from '@/types/offboarding'
import { TIPOS_CESE } from '@/types/offboarding'
import type { PlantillaOnboarding } from '@/types/onboarding'
import type { PagedResponse, PersonaResumenDTO } from '@/types/persona'

const PAGE_SIZE = 20
const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const loading = ref(true)
const plantillas = ref<PlantillaOnboarding[]>([])
const seleccionados = ref<string[]>([])
const personas = ref<PersonaResumenDTO[]>([])
const personaSeleccionada = ref<PersonaResumenDTO | null>(null)
const personaSearch = ref('')
const fechaCese = ref('')
const tipoCese = ref<TipoCese>('RENUNCIA')
const motivo = ref('')
const preview = ref<LiquidacionPreview | null>(null)
const cts = ref('')
const gratificacion = ref('')
const vacaciones = ref('')
const calculating = ref(false)
const registering = ref(false)
const submitting = ref(false)
const ceseRegistrado = ref<Cese | null>(null)
const historialLoading = ref(true)
const historial = ref<PagedResponse<Cese> | null>(null)
const historialPage = ref(0)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const personaLabel = computed(() => {
  const persona = personaSeleccionada.value
  if (!persona) {
    return ''
  }
  return `${persona.apellidos}, ${persona.nombres} · ${persona.numeroDocumento}`
})

const personasOpciones = computed(() => {
  const seleccion = personaSeleccionada.value
  if (!seleccion) {
    return personas.value
  }
  if (personas.value.some((item) => item.id === seleccion.id)) {
    return personas.value
  }
  return [seleccion, ...personas.value]
})

const totalEditado = computed(() => {
  const a = Number(cts.value) || 0
  const b = Number(gratificacion.value) || 0
  const c = Number(vacaciones.value) || 0
  return a + b + c
})

const puedeCalcular = computed(
  () => Boolean(personaSeleccionada.value) && Boolean(fechaCese.value) && !calculating.value,
)

const puedeRegistrar = computed(
  () =>
    Boolean(preview.value) &&
    Boolean(personaSeleccionada.value) &&
    Boolean(fechaCese.value) &&
    !registering.value &&
    !ceseRegistrado.value,
)

const puedeGenerar = computed(
  () => Boolean(ceseRegistrado.value) && seleccionados.value.length > 0 && !submitting.value,
)

const historialDisplayPage = computed(() => historialPage.value + 1)
const historialTotalPages = computed(() => historial.value?.totalPages ?? 0)
const historialTotal = computed(() => historial.value?.totalElements ?? 0)

function formatMoneda(valor: number): string {
  return valor.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatFecha(value: string): string {
  if (!value) {
    return ''
  }
  const parsed = new Date(`${value}T12:00:00`)
  if (Number.isNaN(parsed.getTime())) {
    return value
  }
  return parsed.toLocaleDateString('es-PE')
}

function formatFechaHora(value: string): string {
  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) {
    return value
  }
  return parsed.toLocaleString('es-PE')
}

function tipoLabel(tipo: TipoCese): string {
  return TIPOS_CESE.find((item) => item.value === tipo)?.label ?? tipo
}

async function buscarPersonas(q: string) {
  const data = await listarPersonas({ page: 0, size: 15, q, conContratoActivo: true })
  personas.value = data.content
}

function onPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(id)) {
    personaSeleccionada.value = null
    return
  }
  personaSeleccionada.value =
    personasOpciones.value.find((item) => item.id === id) ?? personaSeleccionada.value
  resetCalculo()
}

function resetCalculo() {
  preview.value = null
  cts.value = ''
  gratificacion.value = ''
  vacaciones.value = ''
  ceseRegistrado.value = null
}

watch([fechaCese, tipoCese], () => {
  if (ceseRegistrado.value) {
    return
  }
  preview.value = null
})

function toggleCodigo(codigo: string) {
  if (seleccionados.value.includes(codigo)) {
    seleccionados.value = seleccionados.value.filter((item) => item !== codigo)
    return
  }
  seleccionados.value = [...seleccionados.value, codigo]
}

async function calcular() {
  const persona = personaSeleccionada.value
  if (!persona || !fechaCese.value) {
    toast.error('Seleccione persona y fecha de cese.')
    return
  }
  calculating.value = true
  try {
    const data = await previewLiquidacion(persona.id, fechaCese.value)
    preview.value = data
    cts.value = String(data.cts)
    gratificacion.value = String(data.gratificacion)
    vacaciones.value = String(data.vacaciones)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo calcular la liquidación.')
  } finally {
    calculating.value = false
  }
}

async function onRegistrar() {
  const persona = personaSeleccionada.value
  if (!persona || !preview.value) {
    return
  }
  registering.value = true
  try {
    ceseRegistrado.value = await registrarCese({
      personaId: persona.id,
      fechaCese: fechaCese.value,
      tipoCese: tipoCese.value,
      motivo: motivo.value.trim() || undefined,
      cts: Number(cts.value),
      gratificacion: Number(gratificacion.value),
      vacaciones: Number(vacaciones.value),
    })
    toast.success('Cese registrado. El contrato quedó rescindido.')
    historialPage.value = 0
    await loadHistorial()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el cese.')
  } finally {
    registering.value = false
  }
}

async function generarPaquete(modo: 'descarga' | 'imprimir') {
  const cese = ceseRegistrado.value
  if (!cese || seleccionados.value.length === 0) {
    toast.error('Registre el cese y seleccione al menos un documento.')
    return
  }
  submitting.value = true
  try {
    const creada = await crearGeneracionOffboarding(cese.id, seleccionados.value)
    await openAuthenticatedDocument(
      documentoOffboardingUrl(creada.id, {
        descarga: modo === 'descarga',
        imprimir: modo === 'imprimir',
      }),
    )
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo generar el expediente de cese.')
  } finally {
    submitting.value = false
  }
}

async function regenerar(ceseId: number, modo: 'descarga' | 'imprimir') {
  try {
    const creada = await crearGeneracionOffboarding(
      ceseId,
      plantillas.value.map((item) => item.codigo),
    )
    await openAuthenticatedDocument(
      documentoOffboardingUrl(creada.id, {
        descarga: modo === 'descarga',
        imprimir: modo === 'imprimir',
      }),
    )
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo regenerar el expediente.')
  }
}

async function loadHistorial() {
  historialLoading.value = true
  try {
    historial.value = await listarCeses(historialPage.value, PAGE_SIZE)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los ceses.')
  } finally {
    historialLoading.value = false
  }
}

function onHistorialPageChange(nextPage: number) {
  historialPage.value = Math.max(0, nextPage - 1)
  void loadHistorial()
}

watch(personaSearch, (query) => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    void buscarPersonas(query)
  }, 300)
})

onMounted(async () => {
  loading.value = true
  try {
    const [lista] = await Promise.all([listarPlantillasOffboarding(), buscarPersonas(''), loadHistorial()])
    plantillas.value = lista
    seleccionados.value = lista.map((item) => item.codigo)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las plantillas de cese.')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})
</script>

<template>
  <div>
    <p class="text-sm leading-relaxed text-slate-500">
      Registre el cese (el contrato pasa a rescindido), ajuste la liquidación MYPE y genere certificado de trabajo,
      no adeudo, liquidación y finiquito.
    </p>

    <div class="mt-4 grid gap-4 lg:grid-cols-2">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
        <h3 class="text-sm font-semibold text-palette-navy">1. Persona y cese</h3>
        <p class="mt-1 text-xs text-slate-500">Solo aparecen personas con contrato activo.</p>

        <Combobox
          :model-value="personaSeleccionada?.id"
          ignore-filter
          class="mt-3 w-full"
          @update:model-value="onPersonaChange"
        >
          <ComboboxAnchor class="w-full">
            <ComboboxTrigger as-child>
              <Button
                variant="outline"
                class="h-10 w-full justify-between px-3 font-normal text-slate-900"
                type="button"
              >
                <span class="truncate" :class="personaLabel ? 'text-slate-900' : 'text-slate-400'">
                  {{ personaLabel || 'Buscar por nombre o documento' }}
                </span>
                <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
              </Button>
            </ComboboxTrigger>
          </ComboboxAnchor>
          <ComboboxList class="z-50 w-(--reka-combobox-trigger-width)" align="start">
            <ComboboxInput v-model="personaSearch" placeholder="Buscar persona..." autocomplete="off" />
            <ComboboxEmpty>No hay personas con contrato activo.</ComboboxEmpty>
            <ComboboxGroup>
              <ComboboxItem
                v-for="persona in personasOpciones"
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

        <div class="mt-4 grid gap-3 sm:grid-cols-2">
          <div>
            <label class="text-sm font-medium text-slate-700" for="offboarding-fecha">Fecha de cese</label>
            <input id="offboarding-fecha" v-model="fechaCese" type="date" :class="inputClass" />
          </div>
          <div>
            <label class="text-sm font-medium text-slate-700" for="offboarding-tipo">Tipo de cese</label>
            <select id="offboarding-tipo" v-model="tipoCese" :class="inputClass">
              <option v-for="tipo in TIPOS_CESE" :key="tipo.value" :value="tipo.value">
                {{ tipo.label }}
              </option>
            </select>
          </div>
        </div>
        <div class="mt-3">
          <label class="text-sm font-medium text-slate-700" for="offboarding-motivo">Motivo</label>
          <input
            id="offboarding-motivo"
            v-model="motivo"
            type="text"
            maxlength="500"
            placeholder="Opcional"
            :class="inputClass"
          />
        </div>
        <Button
          class="mt-4 h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="!puedeCalcular"
          @click="calcular"
        >
          {{ calculating ? 'Calculando…' : 'Calcular liquidación' }}
        </Button>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
        <h3 class="text-sm font-semibold text-palette-navy">2. Liquidación MYPE</h3>
        <p class="mt-1 text-xs text-slate-500">
          CTS, gratificación y vacaciones truncas. Puede ajustar los montos antes de registrar.
        </p>

        <div v-if="!preview" class="mt-6 text-sm text-slate-500">
          Calcule la liquidación para ver y editar los importes.
        </div>
        <div v-else class="mt-4 space-y-3">
          <p class="text-xs text-slate-500">
            Servicio: <span class="font-medium text-slate-700">{{ preview.tiempoServicio }}</span>
            · Inicio {{ formatFecha(preview.fechaInicio) }}
            · Sueldo S/ {{ formatMoneda(preview.sueldo) }}
          </p>
          <div class="grid gap-3 sm:grid-cols-3">
            <div>
              <label class="text-sm font-medium text-slate-700" for="offboarding-cts">CTS trunca</label>
              <input id="offboarding-cts" v-model="cts" type="number" min="0" step="0.01" :class="inputClass" />
            </div>
            <div>
              <label class="text-sm font-medium text-slate-700" for="offboarding-grati">Gratificación trunca</label>
              <input
                id="offboarding-grati"
                v-model="gratificacion"
                type="number"
                min="0"
                step="0.01"
                :class="inputClass"
              />
            </div>
            <div>
              <label class="text-sm font-medium text-slate-700" for="offboarding-vac">Vacaciones truncas</label>
              <input
                id="offboarding-vac"
                v-model="vacaciones"
                type="number"
                min="0"
                step="0.01"
                :class="inputClass"
              />
            </div>
          </div>
          <p class="text-sm font-semibold text-palette-navy">
            Total: S/ {{ formatMoneda(totalEditado) }}
          </p>
          <Button
            class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
            type="button"
            :disabled="!puedeRegistrar"
            @click="onRegistrar"
          >
            {{ registering ? 'Registrando…' : ceseRegistrado ? 'Cese registrado' : 'Registrar cese' }}
          </Button>
          <p v-if="ceseRegistrado" class="text-xs text-emerald-700">
            Cese #{{ ceseRegistrado.id }} registrado. Ya puede generar los documentos.
          </p>
        </div>
      </div>
    </div>

    <div class="mt-4 overflow-hidden rounded-lg border border-palette-cyan/70 bg-white">
      <div class="flex flex-col gap-2 border-b border-palette-cyan/60 px-4 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <div>
          <h3 class="text-sm font-semibold text-palette-navy">3. Documentos de cese</h3>
          <p class="mt-1 text-xs text-slate-500">
            DOC-11 y DOC-12 se reutilizan. DOC-13 y DOC-14 son liquidación y finiquito.
          </p>
        </div>
        <div class="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            type="button"
            :disabled="!puedeGenerar"
            @click="generarPaquete('descarga')"
          >
            <DownloadIcon data-icon="inline-start" />
            Descargar
          </Button>
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            size="sm"
            type="button"
            :disabled="!puedeGenerar"
            @click="generarPaquete('imprimir')"
          >
            <PrinterIcon data-icon="inline-start" />
            Imprimir
          </Button>
        </div>
      </div>

      <div v-if="loading" class="space-y-3 p-4" aria-busy="true">
        <Skeleton class="h-9 w-full" />
        <Skeleton v-for="row in 4" :key="row" class="h-14 w-full" />
      </div>
      <ul v-else class="divide-y divide-palette-cyan/40">
        <li
          v-for="plantilla in plantillas"
          :key="plantilla.codigo"
          class="flex items-start gap-3 px-4 py-4 sm:px-6"
          :class="seleccionados.includes(plantilla.codigo) ? 'bg-palette-cyan/10' : ''"
        >
          <input
            :id="`offboarding-doc-${plantilla.codigo}`"
            class="mt-0.5 h-4 w-4 rounded border-slate-300 text-palette-navy focus:ring-palette-royal focus:ring-offset-0"
            type="checkbox"
            :checked="seleccionados.includes(plantilla.codigo)"
            @change="toggleCodigo(plantilla.codigo)"
          />
          <label class="cursor-pointer" :for="`offboarding-doc-${plantilla.codigo}`">
            <span class="font-mono text-xs font-semibold text-slate-500">{{ plantilla.codigo }}</span>
            <span class="ml-2 text-sm font-semibold text-palette-navy">{{ plantilla.titulo }}</span>
            <p class="mt-1 text-xs text-slate-500">{{ plantilla.descripcion }}</p>
          </label>
        </li>
      </ul>
    </div>

    <div class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <h3 class="text-base font-semibold text-palette-navy">Historial de ceses</h3>
      <p class="mt-1 text-sm text-slate-500">Puede volver a generar el paquete completo de un cese ya registrado.</p>

      <div
        v-if="historialLoading && !historial"
        class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60"
        aria-busy="true"
      >
        <div class="space-y-3 p-4">
          <Skeleton class="h-9 w-full" />
          <Skeleton v-for="row in 4" :key="row" class="h-11 w-full" />
        </div>
      </div>
      <div v-else class="mt-5 overflow-hidden rounded-lg border border-palette-cyan/60">
        <Table>
          <TableCaption class="sr-only">Listado de ceses.</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead class="px-3">Fecha</TableHead>
              <TableHead class="px-3">Persona</TableHead>
              <TableHead class="px-3">Tipo</TableHead>
              <TableHead class="px-3">Cese</TableHead>
              <TableHead class="px-3">Total</TableHead>
              <TableHead class="px-3"><span class="sr-only">Acciones</span></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!historial?.content.length" :colspan="6">
              <span class="text-slate-500">No hay ceses registrados.</span>
            </TableEmpty>
            <TableRow v-for="item in historial?.content ?? []" :key="item.id">
              <TableCell class="px-3 py-3 whitespace-nowrap text-slate-500">
                {{ formatFechaHora(item.createdAt) }}
              </TableCell>
              <TableCell class="px-3 py-3 font-medium">
                {{ item.personaApellidos }}, {{ item.personaNombres }}
              </TableCell>
              <TableCell class="px-3 py-3">{{ tipoLabel(item.tipoCese) }}</TableCell>
              <TableCell class="px-3 py-3">{{ formatFecha(item.fechaCese) }}</TableCell>
              <TableCell class="px-3 py-3">S/ {{ formatMoneda(item.liquidacion?.total ?? 0) }}</TableCell>
              <TableCell class="px-3 py-3">
                <div class="flex justify-end gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    class="h-8 px-3"
                    type="button"
                    @click="regenerar(item.id, 'descarga')"
                  >
                    <DownloadIcon data-icon="inline-start" />
                    Descargar
                  </Button>
                  <Button
                    class="h-8 bg-palette-navy px-3 text-white hover:bg-palette-royal"
                    size="sm"
                    type="button"
                    @click="regenerar(item.id, 'imprimir')"
                  >
                    <PrinterIcon data-icon="inline-start" />
                    Imprimir
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>

      <div class="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p class="text-sm text-slate-500">
          Página {{ historialDisplayPage }} de {{ Math.max(historialTotalPages, 1) }} · {{ historialTotal }} registros
        </p>
        <Pagination
          v-if="historialTotalPages > 1"
          v-slot="{ page: current }"
          class="mx-0 w-auto justify-start sm:justify-end"
          :page="historialDisplayPage"
          :items-per-page="PAGE_SIZE"
          :total="historialTotal"
          :disabled="historialLoading"
          @update:page="onHistorialPageChange"
        >
          <PaginationContent v-slot="{ items }">
            <PaginationPrevious>
              <ChevronLeftIcon data-icon="inline-start" class="cn-rtl-flip" />
              <span>Anterior</span>
            </PaginationPrevious>
            <template v-for="(item, index) in items" :key="index">
              <PaginationItem
                v-if="item.type === 'page'"
                :value="item.value"
                :is-active="item.value === current"
              >
                {{ item.value }}
              </PaginationItem>
              <PaginationEllipsis v-else-if="item.type === 'ellipsis'" :index="index" />
            </template>
            <PaginationNext>
              <span>Siguiente</span>
              <ChevronRightIcon data-icon="inline-end" class="cn-rtl-flip" />
            </PaginationNext>
          </PaginationContent>
        </Pagination>
      </div>
    </div>
  </div>
</template>
