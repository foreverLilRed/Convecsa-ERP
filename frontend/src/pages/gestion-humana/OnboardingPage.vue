<script setup lang="ts">
import { CheckIcon, ChevronsUpDownIcon, EyeIcon, PrinterIcon } from '@lucide/vue'
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
import { Skeleton } from '@/components/ui/skeleton'
import { listarPlantillasOnboarding } from '@/services/onboarding/onboardingService'
import { listarPersonas } from '@/services/persona/personaService'
import { onboardingDocumentoUrl, type PlantillaOnboarding } from '@/types/onboarding'
import type { PersonaResumenDTO } from '@/types/persona'

type Preset = {
  id: string
  label: string
  badge: string
  description: string
  categorias?: readonly string[]
  codigos?: readonly string[]
  todos?: boolean
}

const PRESETS: readonly Preset[] = [
  {
    id: 'todo',
    label: 'Todo el expediente',
    badge: '12 documentos',
    description: 'Ingreso, contractuales, SST, habilitación y cierre.',
    todos: true,
  },
  {
    id: 'ingreso',
    label: 'Pack de ingreso',
    badge: '10 documentos',
    description: 'Documentos indispensables de incorporación (DOC-01 a DOC-10).',
    codigos: ['DOC-01', 'DOC-02', 'DOC-03', 'DOC-04', 'DOC-05', 'DOC-06', 'DOC-07', 'DOC-08', 'DOC-09', 'DOC-10'],
  },
  {
    id: 'contratacion',
    label: 'Contrato, adenda y MOF',
    badge: '3 documentos',
    description: 'Contrato de trabajo, adenda de prórroga y manual de funciones.',
    categorias: ['Contratación'],
  },
  {
    id: 'legal',
    label: 'Legal SUNAFIL',
    badge: '3 documentos',
    description: 'AFP/ONP, renta de quinta y constancia de RIT/RISST.',
    categorias: ['Legal SUNAFIL'],
  },
  {
    id: 'sst',
    label: 'Seguridad SST',
    badge: '2 documentos',
    description: 'Recomendaciones SST y acta de EPP con inducción.',
    categorias: ['Seguridad SST'],
  },
  {
    id: 'habilitacion',
    label: 'Ficha y checklist',
    badge: '2 documentos',
    description: 'Ficha del trabajador y checklist de 14 requisitos.',
    categorias: ['Habilitación'],
  },
  {
    id: 'cierre',
    label: 'Cierre / finiquito',
    badge: '2 documentos',
    description: 'Certificado de trabajo y certificado de no adeudo.',
    categorias: ['Adicional / Cierre'],
  },
]

const loading = ref(true)
const plantillas = ref<PlantillaOnboarding[]>([])
const seleccionados = ref<string[]>([])
const personas = ref<PersonaResumenDTO[]>([])
const personaSeleccionada = ref<PersonaResumenDTO | null>(null)
const personaSearch = ref('')
const rangoDesde = ref(1)
const rangoHasta = ref(1)
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

const totalSeleccionados = computed(() => seleccionados.value.length)

const puedeGenerar = computed(
  () => !loading.value && Boolean(personaSeleccionada.value) && seleccionados.value.length > 0,
)

const estadoSeleccion = computed(() => {
  const persona = personaSeleccionada.value
  const docs = `${totalSeleccionados.value} de ${plantillas.value.length} documentos`
  if (!persona) {
    return `${docs}. Falta seleccionar una persona.`
  }
  return `${docs}. Persona: ${persona.apellidos}, ${persona.nombres}.`
})

function presetCodigos(preset: Preset): string[] {
  if (preset.todos) {
    return plantillas.value.map((item) => item.codigo)
  }
  if (preset.codigos) {
    const set = new Set(preset.codigos)
    return plantillas.value.filter((item) => set.has(item.codigo)).map((item) => item.codigo)
  }
  if (preset.categorias) {
    return plantillas.value
      .filter((item) => preset.categorias!.includes(item.categoria))
      .map((item) => item.codigo)
  }
  return []
}

function presetActivo(preset: Preset): boolean {
  const codigos = presetCodigos(preset)
  if (codigos.length === 0) {
    return false
  }
  return (
    codigos.every((codigo) => seleccionados.value.includes(codigo)) &&
    seleccionados.value.length === codigos.length
  )
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
}

function toggleCodigo(codigo: string) {
  if (seleccionados.value.includes(codigo)) {
    seleccionados.value = seleccionados.value.filter((item) => item !== codigo)
    return
  }
  seleccionados.value = [...seleccionados.value, codigo]
}

function seleccionarTodo() {
  seleccionados.value = plantillas.value.map((item) => item.codigo)
}

function limpiarSeleccion() {
  seleccionados.value = []
}

function aplicarPreset(preset: Preset) {
  const codigos = presetCodigos(preset)
  seleccionados.value = codigos
  toast.success(`${preset.label}: ${codigos.length} documentos.`)
}

function aplicarRango() {
  const inicio = Math.min(rangoDesde.value, rangoHasta.value)
  const fin = Math.max(rangoDesde.value, rangoHasta.value)
  seleccionados.value = plantillas.value
    .filter((_, index) => {
      const orden = index + 1
      return orden >= inicio && orden <= fin
    })
    .map((item) => item.codigo)
}

function abrirDocumento(codigos: string[]) {
  const persona = personaSeleccionada.value
  if (!persona) {
    toast.error('Seleccione una persona con contrato activo.')
    return
  }
  if (codigos.length === 0) {
    toast.error('Seleccione al menos un documento.')
    return
  }
  window.open(onboardingDocumentoUrl(persona.id, codigos), '_blank', 'noopener,noreferrer')
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
    const [lista] = await Promise.all([listarPlantillasOnboarding(), buscarPersonas('')])
    plantillas.value = lista
    seleccionados.value = lista.map((item) => item.codigo)
    const total = lista.length
    rangoDesde.value = 1
    rangoHasta.value = total || 1
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las plantillas.')
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
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Onboarding</h2>
        <p id="onboarding-ayuda" class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Elija la persona con contrato activo y un paquete. Luego ajuste documentos si hace falta y genere el HTML.
        </p>
      </div>
      <Button
        class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:mt-7 sm:w-auto"
        type="button"
        :disabled="!puedeGenerar"
        aria-describedby="onboarding-ayuda onboarding-estado"
        @click="abrirDocumento(seleccionados)"
      >
        <PrinterIcon data-icon="inline-start" />
        Generar seleccionados
      </Button>
    </div>

    <p id="onboarding-estado" class="sr-only" aria-live="polite">{{ estadoSeleccion }}</p>

    <div class="mt-6 grid gap-4 lg:grid-cols-2">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
        <h3 id="onboarding-persona-titulo" class="text-sm font-semibold text-palette-navy">1. Persona</h3>
        <p id="onboarding-persona-ayuda" class="mt-1 text-xs text-slate-500">
          Solo aparecen personas con contrato activo. Los campos de cada documento se asocian a esa persona y a su
          contrato.
        </p>
        <Combobox
          :model-value="personaSeleccionada?.id"
          ignore-filter
          class="mt-3 w-full"
          @update:model-value="onPersonaChange"
        >
          <ComboboxAnchor class="w-full">
            <ComboboxTrigger as-child>
              <Button
                id="onboarding-persona"
                variant="outline"
                class="h-10 w-full justify-between px-3 font-normal text-slate-900"
                type="button"
                aria-labelledby="onboarding-persona-titulo"
                aria-describedby="onboarding-persona-ayuda"
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

        <div
          v-if="personaSeleccionada"
          class="mt-4 rounded-lg border border-palette-cyan/60 bg-palette-cyan/10 px-3 py-3"
        >
          <p class="text-sm font-semibold text-palette-navy">
            {{ personaSeleccionada.apellidos }}, {{ personaSeleccionada.nombres }}
          </p>
          <p class="mt-1 text-xs text-slate-600">
            {{ personaSeleccionada.tipoDocumento }} {{ personaSeleccionada.numeroDocumento }}
            <span aria-hidden="true"> · </span>
            {{ personaSeleccionada.correo }}
          </p>
          <p class="mt-0.5 text-xs text-slate-500">
            {{ personaSeleccionada.distrito }}, {{ personaSeleccionada.provincia }}
          </p>
        </div>
        <p v-else class="mt-4 text-xs text-slate-500">
          Seleccione una persona para habilitar Ver, Imprimir y Generar.
        </p>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h3 class="text-sm font-semibold text-palette-navy">2. Paquetes</h3>
            <p class="mt-1 text-xs text-slate-500">Paquetes predefinidos. También puede marcar documentos abajo.</p>
          </div>
          <div class="flex flex-wrap gap-2">
            <Button variant="outline" size="sm" type="button" :disabled="loading" @click="seleccionarTodo">
              Seleccionar todo
            </Button>
            <Button variant="outline" size="sm" type="button" :disabled="loading" @click="limpiarSeleccion">
              Limpiar
            </Button>
          </div>
        </div>
        <div class="mt-4 grid grid-cols-1 gap-2 sm:grid-cols-2" role="group" aria-label="Paquetes de documentos">
          <Button
            v-for="preset in PRESETS"
            :key="preset.id"
            variant="outline"
            type="button"
            class="h-auto items-start justify-start whitespace-normal px-3 py-3 text-left"
            :class="
              presetActivo(preset) ? 'border-palette-royal bg-palette-cyan/20 text-palette-navy ring-2 ring-palette-royal/20' : ''
            "
            :disabled="loading"
            :aria-pressed="presetActivo(preset)"
            @click="aplicarPreset(preset)"
          >
            <span class="block w-full">
              <span class="flex items-start justify-between gap-2">
                <span class="text-xs font-semibold">{{ preset.label }}</span>
                <span class="shrink-0 rounded border border-slate-200 bg-white px-1.5 py-0.5 text-[10px] font-semibold text-slate-600">
                  {{ preset.badge }}
                </span>
              </span>
              <span class="mt-1 block text-[11px] font-normal leading-snug text-slate-500">
                {{ preset.description }}
              </span>
            </span>
          </Button>
        </div>
      </div>
    </div>

    <div class="mt-4 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <h3 class="text-sm font-semibold text-palette-navy">3. Rango</h3>
      <p class="mt-1 text-xs text-slate-500">Tramo continuo según el orden de código, si no usa un paquete.</p>
      <div class="mt-3 flex flex-wrap items-end gap-3">
        <label class="text-sm text-slate-700" for="onboarding-rango-desde">
          Desde
          <select
            id="onboarding-rango-desde"
            v-model.number="rangoDesde"
            class="mt-1.5 block rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
          >
            <option v-for="(item, index) in plantillas" :key="`desde-${item.codigo}`" :value="index + 1">
              {{ index + 1 }}. {{ item.codigo }}
            </option>
          </select>
        </label>
        <label class="text-sm text-slate-700" for="onboarding-rango-hasta">
          Hasta
          <select
            id="onboarding-rango-hasta"
            v-model.number="rangoHasta"
            class="mt-1.5 block rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
          >
            <option v-for="(item, index) in plantillas" :key="`hasta-${item.codigo}`" :value="index + 1">
              {{ index + 1 }}. {{ item.codigo }}
            </option>
          </select>
        </label>
        <Button
          class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          :disabled="loading || plantillas.length === 0"
          @click="aplicarRango"
        >
          Aplicar rango
        </Button>
      </div>
    </div>

    <div class="mt-4 overflow-hidden rounded-lg border border-palette-cyan/70 bg-white">
      <div class="flex flex-col gap-2 border-b border-palette-cyan/60 px-4 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <div>
          <h3 class="text-sm font-semibold text-palette-navy">4. Documentos</h3>
          <p class="mt-1 text-xs text-slate-500">Lista completa. Marque o desmarque cada uno antes de generar.</p>
        </div>
        <p class="text-xs font-semibold text-slate-600" aria-live="polite">
          {{ totalSeleccionados }} de {{ plantillas.length }} seleccionados
        </p>
      </div>

      <div v-if="loading" class="space-y-3 p-4" aria-busy="true">
        <span class="sr-only">Cargando plantillas</span>
        <Skeleton class="h-9 w-full" />
        <Skeleton v-for="row in 6" :key="row" class="h-16 w-full" />
      </div>

      <ul v-else class="divide-y divide-palette-cyan/40">
        <li
          v-for="plantilla in plantillas"
          :key="plantilla.codigo"
          class="flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-start sm:justify-between sm:px-6"
          :class="seleccionados.includes(plantilla.codigo) ? 'bg-palette-cyan/10' : ''"
        >
          <div class="flex items-start gap-3">
            <input
              :id="`onboarding-doc-${plantilla.codigo}`"
              class="mt-0.5 h-4 w-4 rounded border-slate-300 text-palette-navy focus:ring-palette-royal focus:ring-offset-0"
              type="checkbox"
              :checked="seleccionados.includes(plantilla.codigo)"
              :aria-describedby="`onboarding-doc-desc-${plantilla.codigo}`"
              @change="toggleCodigo(plantilla.codigo)"
            />
            <div>
              <label
                class="flex cursor-pointer flex-wrap items-center gap-2"
                :for="`onboarding-doc-${plantilla.codigo}`"
              >
                <span class="font-mono text-xs font-semibold text-slate-500">{{ plantilla.codigo }}</span>
                <span class="text-sm font-semibold text-palette-navy">{{ plantilla.titulo }}</span>
                <span class="rounded-full border border-palette-cyan/80 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-palette-royal">
                  {{ plantilla.categoria }}
                </span>
              </label>
              <p :id="`onboarding-doc-desc-${plantilla.codigo}`" class="mt-1 text-xs leading-relaxed text-slate-500">
                {{ plantilla.descripcion }}
              </p>
              <p class="mt-1 text-[11px] text-slate-400">
                Base legal: <span class="font-medium text-slate-600">{{ plantilla.baseLegal }}</span>
              </p>
            </div>
          </div>
          <div class="flex shrink-0 items-center gap-2 self-end sm:self-center">
            <Button
              variant="outline"
              size="sm"
              type="button"
              :disabled="!personaSeleccionada"
              :aria-label="`Ver ${plantilla.codigo}`"
              @click="abrirDocumento([plantilla.codigo])"
            >
              <EyeIcon data-icon="inline-start" />
              Ver
            </Button>
            <Button
              class="bg-palette-navy text-white hover:bg-palette-royal"
              size="sm"
              type="button"
              :disabled="!personaSeleccionada"
              :aria-label="`Imprimir ${plantilla.codigo}`"
              @click="abrirDocumento([plantilla.codigo])"
            >
              <PrinterIcon data-icon="inline-start" />
              Imprimir solo este
            </Button>
          </div>
        </li>
      </ul>
    </div>
  </section>
</template>
