<script setup lang="ts">
import { CheckIcon, ChevronsUpDownIcon, XIcon } from '@lucide/vue'
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
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
import { crearContrato, obtenerContratosCatalogos } from '@/services/contrato/contratoService'
import { listarPersonas } from '@/services/persona/personaService'
import type { CatalogoItem, ContratosCatalogos, PlantillaCampo, PlantillaContrato } from '@/types/contrato'
import type { PersonaResumenDTO } from '@/types/persona'

type ContratoForm = {
  personaId: number | null
  plantillaContratoId: string
  fechaInicio: string
  fechaFin: string
  sueldo: string
  afpId: string
  tieneAsignacionFamiliar: boolean
}

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

function emptyForm(): ContratoForm {
  return {
    personaId: null,
    plantillaContratoId: '',
    fechaInicio: '',
    fechaFin: '',
    sueldo: '',
    afpId: '',
    tieneAsignacionFamiliar: false,
  }
}

const form = reactive<ContratoForm>(emptyForm())
const extras = reactive<Record<string, string>>({})
const error = ref('')
const submitting = ref(false)
const catalogos = ref<ContratosCatalogos | null>(null)
const personas = ref<PersonaResumenDTO[]>([])
const personaSeleccionada = ref<PersonaResumenDTO | null>(null)
const personaSearch = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const plantillasContrato = computed(() =>
  (catalogos.value?.plantillas ?? []).filter((item) => (item.uso ?? 'CONTRATO') === 'CONTRATO'),
)

const plantillaSeleccionada = computed<PlantillaContrato | null>(() => {
  const id = Number(form.plantillaContratoId)
  if (!Number.isFinite(id)) {
    return null
  }
  return plantillasContrato.value.find((item) => item.id === id) ?? null
})

const camposExtra = computed(() => plantillaSeleccionada.value?.metadata ?? [])

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

function opcionesCatalogo(campo: PlantillaCampo): CatalogoItem[] {
  if (!catalogos.value || campo.tipo !== 'catalogo') {
    return []
  }
  if (campo.catalogo === 'jornadas') {
    return catalogos.value.jornadas
  }
  if (campo.catalogo === 'causasObjetivas') {
    return catalogos.value.causasObjetivas
  }
  if (campo.catalogo === 'metodosPago') {
    return catalogos.value.metodosPago
  }
  return []
}

function lockBodyScroll() {
  const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth
  document.body.style.overflow = 'hidden'
  document.body.style.paddingRight = scrollbarWidth > 0 ? `${scrollbarWidth}px` : ''
}

function unlockBodyScroll() {
  document.body.style.overflow = ''
  document.body.style.paddingRight = ''
}

function onDocumentKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && props.open && !submitting.value) {
    emit('close')
  }
}

function resetExtras(campos: PlantillaCampo[]) {
  for (const clave of Object.keys(extras)) {
    delete extras[clave]
  }
  for (const campo of campos) {
    extras[campo.clave] = campo.clave === 'ciudad_firma' ? 'Lima' : ''
  }
}

function resetForm() {
  Object.assign(form, emptyForm())
  resetExtras([])
  error.value = ''
  submitting.value = false
  personaSeleccionada.value = null
  personaSearch.value = ''
}

async function buscarPersonas(q: string) {
  const data = await listarPersonas({ page: 0, size: 15, q })
  personas.value = data.content
}

async function loadCatalogos() {
  catalogos.value = await obtenerContratosCatalogos()
}

watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) {
      resetForm()
      lockBodyScroll()
      document.addEventListener('keydown', onDocumentKeydown)
      try {
        await Promise.all([loadCatalogos(), buscarPersonas('')])
      } catch (err) {
        toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los catálogos.')
      }
    } else {
      document.removeEventListener('keydown', onDocumentKeydown)
    }
  },
)

watch(
  () => form.plantillaContratoId,
  () => {
    resetExtras(camposExtra.value)
  },
)

watch(personaSearch, (query) => {
  if (!props.open) {
    return
  }
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    void buscarPersonas(query)
  }, 300)
})

function onPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(id)) {
    form.personaId = null
    personaSeleccionada.value = null
    return
  }
  form.personaId = id
  personaSeleccionada.value = personasOpciones.value.find((item) => item.id === id) ?? personaSeleccionada.value
}

function validate(): string | null {
  if (!form.personaId) return 'Seleccione una persona del listado.'
  if (!form.plantillaContratoId) return 'Seleccione la plantilla.'
  if (!form.fechaInicio) return 'Ingrese la fecha de inicio.'
  if (!form.fechaFin) return 'Ingrese la fecha de fin.'
  if (form.fechaFin < form.fechaInicio) return 'La fecha de fin no puede ser menor que la de inicio.'
  const sueldo = Number(form.sueldo)
  if (!Number.isFinite(sueldo) || sueldo <= 0) return 'Ingrese un sueldo mayor a cero.'
  for (const campo of camposExtra.value) {
    const valor = extras[campo.clave]?.trim() ?? ''
    if (campo.requerido && !valor) {
      return `Complete el campo ${campo.label}.`
    }
  }
  return null
}

async function onSubmit() {
  error.value = ''
  const invalid = validate()
  if (invalid) {
    error.value = invalid
    toast.error(invalid)
    return
  }

  submitting.value = true
  try {
    const metadata: Record<string, string> = {}
    for (const campo of camposExtra.value) {
      const valor = extras[campo.clave]?.trim() ?? ''
      if (valor) {
        metadata[campo.clave] = valor
      }
    }
    await crearContrato({
      personaId: form.personaId as number,
      plantillaContratoId: Number(form.plantillaContratoId),
      fechaInicio: form.fechaInicio,
      fechaFin: form.fechaFin,
      sueldo: Number(form.sueldo),
      afpId: form.afpId ? Number(form.afpId) : null,
      tieneAsignacionFamiliar: form.tieneAsignacionFamiliar,
      metadata,
    })
    toast.success('Contrato creado.')
    emit('saved')
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear el contrato.'
    error.value = message
    toast.error(message)
  } finally {
    submitting.value = false
  }
}

function onOverlayClick() {
  if (!submitting.value) {
    emit('close')
  }
}

function inputType(campo: PlantillaCampo): string {
  if (campo.tipo === 'date') {
    return 'date'
  }
  if (campo.tipo === 'number') {
    return 'number'
  }
  return 'text'
}

onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  document.removeEventListener('keydown', onDocumentKeydown)
  unlockBodyScroll()
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
      <div
        v-if="open"
        class="modal-root fixed inset-0 z-50 overflow-y-auto"
      >
        <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="onOverlayClick" />

        <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
          <section
            class="modal-panel relative z-10 flex h-[min(92vh,48rem)] w-full max-w-3xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
            role="dialog"
            aria-modal="true"
            aria-labelledby="contrato-modal-title"
          >
            <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
              <div>
                <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                  Gestión Humana / Contratos
                </p>
                <h2 id="contrato-modal-title" class="mt-1 text-lg font-bold text-palette-navy">
                  Nuevo contrato
                </h2>
              </div>
              <Button
                variant="ghost"
                size="icon"
                class="shrink-0"
                type="button"
                aria-label="Cerrar"
                :disabled="submitting"
                @click="emit('close')"
              >
                <XIcon />
              </Button>
            </header>

            <form class="flex min-h-0 flex-1 flex-col px-5 py-5" @submit.prevent="onSubmit">
              <fieldset class="min-h-0 flex-1 space-y-4 overflow-y-auto pr-1" :disabled="submitting">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="contrato-persona">Persona</label>
                  <Combobox
                    :model-value="form.personaId ?? undefined"
                    ignore-filter
                    class="w-full"
                    @update:model-value="onPersonaChange"
                  >
                    <ComboboxAnchor class="w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="contrato-persona"
                          variant="outline"
                          class="mt-1.5 h-10 w-full justify-between px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span class="truncate" :class="personaLabel ? 'text-slate-900' : 'text-slate-400'">
                            {{ personaLabel || 'Buscar por nombre o documento' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput
                        v-model="personaSearch"
                        placeholder="Buscar persona..."
                        autocomplete="off"
                      />
                      <ComboboxEmpty>Sin resultados.</ComboboxEmpty>
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
                </div>

                <div>
                  <label class="text-sm font-medium text-slate-700" for="contrato-plantilla">Plantilla</label>
                  <select id="contrato-plantilla" v-model="form.plantillaContratoId" :class="inputClass">
                    <option value="">Seleccione</option>
                    <option
                      v-for="item in plantillasContrato"
                      :key="item.id"
                      :value="String(item.id)"
                    >
                      {{ item.nombre }}
                    </option>
                  </select>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-inicio">Fecha de inicio</label>
                    <input id="contrato-inicio" v-model="form.fechaInicio" type="date" :class="inputClass" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-fin">Fecha de fin</label>
                    <input id="contrato-fin" v-model="form.fechaFin" type="date" :class="inputClass" />
                  </div>
                </div>

                <div>
                  <label class="text-sm font-medium text-slate-700" for="contrato-sueldo">Sueldo</label>
                  <input
                    id="contrato-sueldo"
                    v-model="form.sueldo"
                    type="number"
                    min="0.01"
                    step="0.01"
                    :class="inputClass"
                  />
                </div>

                <div>
                  <label class="text-sm font-medium text-slate-700" for="contrato-afp">AFP / ONP</label>
                  <select id="contrato-afp" v-model="form.afpId" :class="inputClass">
                    <option value="">ONP por defecto al generar nómina</option>
                    <option
                      v-for="item in catalogos?.afps ?? []"
                      :key="item.id"
                      :value="String(item.id)"
                    >
                      {{ item.label }}
                    </option>
                  </select>
                </div>

                <label class="flex items-center gap-2 text-sm text-slate-700" for="contrato-asig">
                  <input
                    id="contrato-asig"
                    v-model="form.tieneAsignacionFamiliar"
                    type="checkbox"
                    class="h-4 w-4 rounded border-slate-300"
                  />
                  Tiene asignación familiar
                </label>

                <div v-if="camposExtra.length" class="grid gap-4 sm:grid-cols-2">
                  <div v-for="campo in camposExtra" :key="campo.clave">
                    <label class="text-sm font-medium text-slate-700" :for="`contrato-extra-${campo.clave}`">
                      {{ campo.label }}
                    </label>
                    <select
                      v-if="campo.tipo === 'catalogo'"
                      :id="`contrato-extra-${campo.clave}`"
                      v-model="extras[campo.clave]"
                      :class="inputClass"
                    >
                      <option value="">Seleccione</option>
                      <option
                        v-for="item in opcionesCatalogo(campo)"
                        :key="item.id"
                        :value="String(item.id)"
                      >
                        {{ item.label }}
                      </option>
                    </select>
                    <input
                      v-else
                      :id="`contrato-extra-${campo.clave}`"
                      v-model="extras[campo.clave]"
                      :type="inputType(campo)"
                      :class="inputClass"
                    />
                  </div>
                </div>
              </fieldset>

              <p v-if="error" id="contrato-form-error" class="mt-4 shrink-0 text-sm font-medium text-red-600" role="alert">
                {{ error }}
              </p>

              <footer class="mt-4 flex shrink-0 flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
                <Button
                  variant="outline"
                  class="h-10 px-4"
                  type="button"
                  :disabled="submitting"
                  @click="emit('close')"
                >
                  Cancelar
                </Button>
                <Button
                  class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="submitting"
                  :aria-describedby="error ? 'contrato-form-error' : undefined"
                >
                  {{ submitting ? 'Guardando…' : 'Guardar' }}
                </Button>
              </footer>
            </form>
          </section>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
