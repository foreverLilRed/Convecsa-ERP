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
import { contratoDocumentoUrl, type ContratosCatalogos } from '@/types/contrato'
import type { PersonaResumenDTO } from '@/types/persona'

type ContratoForm = {
  personaId: number | null
  tipoPlantillaId: string
  causaObjetivaId: string
  cargo: string
  area: string
  proyecto: string
  ubicacionObra: string
  fechaInicio: string
  fechaFin: string
  jornadaId: string
  sueldo: string
  metodoPagoId: string
  ciudadFirma: string
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
    tipoPlantillaId: '',
    causaObjetivaId: '',
    cargo: '',
    area: '',
    proyecto: '',
    ubicacionObra: '',
    fechaInicio: '',
    fechaFin: '',
    jornadaId: '',
    sueldo: '',
    metodoPagoId: '',
    ciudadFirma: 'Lima',
  }
}

const form = reactive<ContratoForm>(emptyForm())
const error = ref('')
const submitting = ref(false)
const catalogos = ref<ContratosCatalogos | null>(null)
const personas = ref<PersonaResumenDTO[]>([])
const personaSeleccionada = ref<PersonaResumenDTO | null>(null)
const personaSearch = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

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

function resetForm() {
  Object.assign(form, emptyForm())
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
  if (!form.tipoPlantillaId) return 'Seleccione la plantilla.'
  if (!form.causaObjetivaId) return 'Seleccione la causa objetiva.'
  if (!form.cargo.trim()) return 'Ingrese el cargo.'
  if (!form.area.trim()) return 'Ingrese el área.'
  if (!form.proyecto.trim()) return 'Ingrese el proyecto.'
  if (!form.ubicacionObra.trim()) return 'Ingrese la ubicación de la obra.'
  if (!form.fechaInicio) return 'Ingrese la fecha de inicio.'
  if (!form.fechaFin) return 'Ingrese la fecha de fin.'
  if (form.fechaFin < form.fechaInicio) return 'La fecha de fin no puede ser menor que la de inicio.'
  if (!form.jornadaId) return 'Seleccione la jornada.'
  const sueldo = Number(form.sueldo)
  if (!Number.isFinite(sueldo) || sueldo <= 0) return 'Ingrese un sueldo mayor a cero.'
  if (!form.metodoPagoId) return 'Seleccione el método de pago.'
  if (!form.ciudadFirma.trim()) return 'Ingrese la ciudad de firma.'
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
    const created = await crearContrato({
      personaId: form.personaId as number,
      tipoPlantillaId: Number(form.tipoPlantillaId),
      causaObjetivaId: Number(form.causaObjetivaId),
      cargo: form.cargo.trim(),
      area: form.area.trim(),
      proyecto: form.proyecto.trim(),
      ubicacionObra: form.ubicacionObra.trim(),
      fechaInicio: form.fechaInicio,
      fechaFin: form.fechaFin,
      fechaCorte: null,
      jornadaId: Number(form.jornadaId),
      sueldo: Number(form.sueldo),
      metodoPagoId: Number(form.metodoPagoId),
      ciudadFirma: form.ciudadFirma.trim(),
      numero: null,
      observaciones: null,
    })
    toast.success('Contrato creado.')
    window.open(contratoDocumentoUrl(created.id), '_blank', 'noopener,noreferrer')
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
                    <ComboboxList class="z-80 w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="personaSearch" placeholder="Buscar persona..." />
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

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-plantilla">Plantilla</label>
                    <select id="contrato-plantilla" v-model="form.tipoPlantillaId" :class="inputClass">
                      <option value="">Seleccione</option>
                      <option
                        v-for="item in catalogos?.tiposPlantilla ?? []"
                        :key="item.id"
                        :value="String(item.id)"
                      >
                        {{ item.label }}
                      </option>
                    </select>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-causa">Causa objetiva</label>
                    <select id="contrato-causa" v-model="form.causaObjetivaId" :class="inputClass">
                      <option value="">Seleccione</option>
                      <option
                        v-for="item in catalogos?.causasObjetivas ?? []"
                        :key="item.id"
                        :value="String(item.id)"
                      >
                        {{ item.label }}
                      </option>
                    </select>
                  </div>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-cargo">Cargo</label>
                    <input id="contrato-cargo" v-model="form.cargo" type="text" :class="inputClass" maxlength="160" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-area">Área</label>
                    <input id="contrato-area" v-model="form.area" type="text" :class="inputClass" maxlength="160" />
                  </div>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-proyecto">Proyecto</label>
                    <input id="contrato-proyecto" v-model="form.proyecto" type="text" :class="inputClass" maxlength="200" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-ubicacion">Ubicación de obra</label>
                    <input id="contrato-ubicacion" v-model="form.ubicacionObra" type="text" :class="inputClass" maxlength="200" />
                  </div>
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

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-jornada">Jornada</label>
                    <select id="contrato-jornada" v-model="form.jornadaId" :class="inputClass">
                      <option value="">Seleccione</option>
                      <option
                        v-for="item in catalogos?.jornadas ?? []"
                        :key="item.id"
                        :value="String(item.id)"
                      >
                        {{ item.label }}
                      </option>
                    </select>
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
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-pago">Método de pago</label>
                    <select id="contrato-pago" v-model="form.metodoPagoId" :class="inputClass">
                      <option value="">Seleccione</option>
                      <option
                        v-for="item in catalogos?.metodosPago ?? []"
                        :key="item.id"
                        :value="String(item.id)"
                      >
                        {{ item.label }}
                      </option>
                    </select>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="contrato-ciudad">Ciudad de firma</label>
                    <input id="contrato-ciudad" v-model="form.ciudadFirma" type="text" :class="inputClass" maxlength="80" />
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
