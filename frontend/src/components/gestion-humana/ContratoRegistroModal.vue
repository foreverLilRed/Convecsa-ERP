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
import { obtenerCatalogosConstruccion } from '@/services/construccion/construccionService'
import { crearContrato, obtenerContratosCatalogos } from '@/services/contrato/contratoService'
import { listarPersonas } from '@/services/persona/personaService'
import type { ConstruccionCatalogos } from '@/types/construccion'
import type { CatalogoItem, ContratosCatalogos, ModalidadContrato, PlantillaCampo, PlantillaContrato } from '@/types/contrato'
import type { PersonaResumenDTO } from '@/types/persona'

type ContratoForm = {
  titulo: string
  regimenLaboralId: string
  categoriaContratoId: string
  personaId: number | null
  tipoPlantillaId: string
  fechaInicio: string
  fechaFin: string
  sueldo: string
  activo: boolean
  fechaCorte: string
  observaciones: string
  categoriaObrera: string
  especialidadBae: string
  afiliadoSindical: boolean
  hijosEscolaridad: string
  proyectoId: string
  centroCosto: string
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
    titulo: '',
    regimenLaboralId: '',
    categoriaContratoId: '',
    personaId: null,
    tipoPlantillaId: '',
    fechaInicio: '',
    fechaFin: '',
    sueldo: '',
    activo: true,
    fechaCorte: '',
    observaciones: '',
    categoriaObrera: '',
    especialidadBae: '',
    afiliadoSindical: false,
    hijosEscolaridad: '0',
    proyectoId: '',
    centroCosto: '',
  }
}

const form = reactive<ContratoForm>(emptyForm())
const extras = reactive<Record<string, string>>({})
const error = ref('')
const submitting = ref(false)
const catalogos = ref<ContratosCatalogos | null>(null)
const catalogosCc = ref<ConstruccionCatalogos | null>(null)
const personas = ref<PersonaResumenDTO[]>([])
const personaSeleccionada = ref<PersonaResumenDTO | null>(null)
const personaSearch = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null

const inputClass =
  'mt-1.5 block h-10 min-w-0 max-w-full w-full rounded-lg border border-slate-200 bg-white px-3 py-0 text-sm leading-normal text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const plantillasContrato = computed(() =>
  (catalogos.value?.plantillas ?? []).filter((item) => (item.uso ?? 'CONTRATO') === 'CONTRATO'),
)

const modalidadesFiltradas = computed<ModalidadContrato[]>(() => {
  const regimenId = Number(form.regimenLaboralId)
  if (!form.regimenLaboralId || !Number.isFinite(regimenId)) {
    return []
  }
  return (catalogos.value?.modalidadesContrato ?? []).filter((item) => item.regimenLaboralId === regimenId)
})

const modalidadSeleccionada = computed<ModalidadContrato | null>(() => {
  const id = Number(form.categoriaContratoId)
  if (!form.categoriaContratoId || !Number.isFinite(id)) {
    return null
  }
  return modalidadesFiltradas.value.find((item) => item.id === id) ?? null
})

const fechaFinRequerida = computed(() => Boolean(modalidadSeleccionada.value?.requiereFechaFin))

const regimenSeleccionado = computed(() => {
  const id = Number(form.regimenLaboralId)
  if (!form.regimenLaboralId || !Number.isFinite(id)) {
    return null
  }
  return (catalogos.value?.regimenesLaborales ?? []).find((item) => item.id === id) ?? null
})

const esConstruccionCivil = computed(() => regimenSeleccionado.value?.codigo === 'CONSTRUCCION_CIVIL')

const etiquetaSueldo = computed(() => {
  const codigo = regimenSeleccionado.value?.codigo
  if (codigo === 'CONSTRUCCION_CIVIL') {
    return 'Jornal Diario Básico'
  }
  if (codigo === 'LOCACION_SERVICIOS') {
    return 'Honorario Mensual / Total'
  }
  return 'Sueldo Mensual'
})

const plantillaSeleccionada = computed<PlantillaContrato | null>(() => {
  const id = Number(form.tipoPlantillaId)
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
  catalogosCc.value = await obtenerCatalogosConstruccion()
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
  () => form.tipoPlantillaId,
  () => {
    resetExtras(camposExtra.value)
  },
)

watch(
  () => form.regimenLaboralId,
  () => {
    form.categoriaContratoId = ''
  },
)

watch(
  () => form.categoriaContratoId,
  () => {
    if (!fechaFinRequerida.value) {
      form.fechaFin = ''
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
  if (!form.titulo.trim()) return 'Ingrese el título del contrato.'
  if (!form.regimenLaboralId) return 'Seleccione el régimen laboral.'
  if (!form.categoriaContratoId) return 'Seleccione el tipo de contrato / modalidad.'
  if (!form.tipoPlantillaId) return 'Seleccione el tipo de plantilla.'
  if (fechaFinRequerida.value && !form.fechaFin) return 'Ingrese la fecha de fin.'
  if (form.fechaInicio && form.fechaFin && form.fechaFin < form.fechaInicio) return 'La fecha de fin no puede ser menor que la de inicio.'
  const sueldo = Number(form.sueldo)
  if (!Number.isFinite(sueldo) || sueldo <= 0) return 'Ingrese un sueldo mayor a cero.'
  if (esConstruccionCivil.value) {
    if (!form.categoriaObrera) return 'Seleccione la categoría de obrero.'
    if (!form.proyectoId) return 'Seleccione la obra / proyecto.'
    const hijos = Number(form.hijosEscolaridad)
    if (!Number.isInteger(hijos) || hijos < 0 || hijos > 20) return 'Ingrese un número válido de hijos para escolaridad.'
  }
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
      titulo: form.titulo.trim(),
      categoriaContratoId: Number(form.categoriaContratoId),
      personaId: form.personaId as number,
      tipoPlantillaId: Number(form.tipoPlantillaId),
      fechaInicio: form.fechaInicio || null,
      fechaFin: fechaFinRequerida.value ? form.fechaFin || null : null,
      sueldo: Number(form.sueldo),
      activo: form.activo,
      fechaCorte: form.fechaCorte || null,
      observaciones: form.observaciones.trim() || null,
      metadata,
      categoriaObrera: esConstruccionCivil.value ? form.categoriaObrera : null,
      especialidadBae: esConstruccionCivil.value ? form.especialidadBae || null : null,
      afiliadoSindical: esConstruccionCivil.value ? form.afiliadoSindical : false,
      cuotaSindical: esConstruccionCivil.value && form.afiliadoSindical ? 40 : 0,
      hijosEscolaridad: esConstruccionCivil.value ? Number(form.hijosEscolaridad) : 0,
      proyectoId: esConstruccionCivil.value ? Number(form.proyectoId) : null,
      centroCosto: esConstruccionCivil.value ? form.centroCosto.trim() || null : null,
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

        <div class="relative flex min-h-full items-center justify-center p-0 sm:p-4 lg:p-6">
          <section
            class="modal-panel relative z-10 flex h-[100dvh] max-h-[100dvh] min-w-0 w-full flex-col overflow-hidden bg-white shadow-lg sm:h-[min(92vh,52rem)] sm:max-h-[min(92vh,52rem)] sm:max-w-4xl sm:rounded-2xl sm:border sm:border-palette-cyan/70"
            role="dialog"
            aria-modal="true"
            aria-labelledby="contrato-modal-title"
          >
            <header class="flex shrink-0 items-center justify-between gap-3 border-b border-palette-cyan/70 px-4 py-3 sm:px-6 sm:py-4">
              <div class="min-w-0">
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

            <form class="flex min-h-0 min-w-0 flex-1 flex-col px-4 py-4 sm:px-6 sm:py-5" @submit.prevent="onSubmit">
              <fieldset
                class="min-h-0 min-w-0 flex-1 overflow-x-hidden overflow-y-auto overscroll-contain pr-2"
                :disabled="submitting"
              >
                <div class="grid min-w-0 gap-x-4 gap-y-5 md:grid-cols-2">
                <div class="min-w-0 md:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="contrato-titulo">Título</label>
                  <input
                    id="contrato-titulo"
                    v-model="form.titulo"
                    type="text"
                    maxlength="200"
                    placeholder="Ej. Contrato de trabajo a plazo fijo"
                    :class="inputClass"
                  />
                </div>

                <div class="min-w-0">
                  <label class="text-sm font-medium text-slate-700" for="contrato-regimen">Régimen laboral / vínculo jurídico</label>
                  <select id="contrato-regimen" v-model="form.regimenLaboralId" :class="inputClass">
                    <option value="">Seleccione</option>
                    <option
                      v-for="item in catalogos?.regimenesLaborales ?? []"
                      :key="item.id"
                      :value="String(item.id)"
                    >
                      {{ item.label }}
                    </option>
                  </select>
                </div>

                <div class="min-w-0">
                  <label class="text-sm font-medium text-slate-700" for="contrato-modalidad">Tipo de contrato / modalidad</label>
                  <select
                    id="contrato-modalidad"
                    v-model="form.categoriaContratoId"
                    :class="inputClass"
                    :disabled="!form.regimenLaboralId"
                  >
                    <option value="">{{ form.regimenLaboralId ? 'Seleccione' : 'Seleccione primero el régimen' }}</option>
                    <option
                      v-for="item in modalidadesFiltradas"
                      :key="item.id"
                      :value="String(item.id)"
                    >
                      {{ item.label }}
                    </option>
                  </select>
                </div>

                <div class="min-w-0 md:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="contrato-plantilla">Tipo de plantilla</label>
                  <select id="contrato-plantilla" v-model="form.tipoPlantillaId" :class="inputClass">
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

                <div class="min-w-0 md:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="contrato-persona">Persona</label>
                  <Combobox
                    :model-value="form.personaId ?? undefined"
                    ignore-filter
                    class="min-w-0 w-full"
                    @update:model-value="onPersonaChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="contrato-persona"
                          variant="outline"
                          class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span class="min-w-0 flex-1 truncate text-left" :class="personaLabel ? 'text-slate-900' : 'text-slate-400'">
                            {{ personaLabel || 'Buscar por nombre o documento' }}
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

                <div class="contents">
                  <div class="min-w-0">
                    <label class="text-sm font-medium text-slate-700" for="contrato-inicio">Fecha de inicio</label>
                    <input id="contrato-inicio" v-model="form.fechaInicio" type="date" :class="inputClass" />
                  </div>
                  <div class="min-w-0">
                    <label class="text-sm font-medium text-slate-700" for="contrato-fin">Fecha de fin</label>
                    <input
                      id="contrato-fin"
                      v-model="form.fechaFin"
                      type="date"
                      :class="inputClass"
                      :disabled="Boolean(form.categoriaContratoId) && !fechaFinRequerida"
                    />
                    <p v-if="form.categoriaContratoId && !fechaFinRequerida" class="mt-1 text-xs text-slate-500">
                      Opcional en contratos a plazo indeterminado.
                    </p>
                  </div>
                </div>

                <div v-if="esConstruccionCivil" class="contents">
                  <div class="min-w-0">
                    <label class="text-sm font-medium text-slate-700" for="contrato-categoria-obrera">Categoría</label>
                    <select id="contrato-categoria-obrera" v-model="form.categoriaObrera" :class="inputClass">
                      <option value="">Seleccione</option>
                      <option
                        v-for="item in catalogosCc?.categorias ?? []"
                        :key="item.codigo"
                        :value="item.codigo"
                      >
                        {{ item.label }}
                      </option>
                    </select>
                  </div>
                  <div class="min-w-0">
                    <label class="text-sm font-medium text-slate-700" for="contrato-bae">Especialidad BAE</label>
                    <select id="contrato-bae" v-model="form.especialidadBae" :class="inputClass">
                      <option value="">Ninguna</option>
                      <option
                        v-for="item in catalogosCc?.especialidadesBae ?? []"
                        :key="item.codigo"
                        :value="item.codigo"
                      >
                        {{ item.label }}
                      </option>
                    </select>
                  </div>
                  <div class="min-w-0">
                    <label class="text-sm font-medium text-slate-700" for="contrato-proyecto">Obra / proyecto</label>
                    <select id="contrato-proyecto" v-model="form.proyectoId" :class="inputClass">
                      <option value="">Seleccione</option>
                      <option
                        v-for="item in catalogosCc?.proyectos ?? []"
                        :key="item.id"
                        :value="String(item.id)"
                      >
                        {{ item.codigo }} — {{ item.nombre }}
                      </option>
                    </select>
                  </div>
                  <div class="min-w-0">
                    <label class="text-sm font-medium text-slate-700" for="contrato-cc">Centro de costo</label>
                    <input
                      id="contrato-cc"
                      v-model="form.centroCosto"
                      maxlength="40"
                      placeholder="Vacío = centro de costo de la obra"
                      :class="inputClass"
                    />
                  </div>
                  <div class="min-w-0">
                    <label class="text-sm font-medium text-slate-700" for="contrato-hijos">Hijos (escolaridad)</label>
                    <input
                      id="contrato-hijos"
                      v-model="form.hijosEscolaridad"
                      type="number"
                      min="0"
                      max="20"
                      :class="inputClass"
                    />
                  </div>
                  <label class="flex min-w-0 items-center gap-2 self-end pb-2 text-sm text-slate-700" for="contrato-sindicato">
                    <input
                      id="contrato-sindicato"
                      v-model="form.afiliadoSindical"
                      type="checkbox"
                      class="h-4 w-4 rounded border-slate-300"
                    />
                    Afiliado sindical
                  </label>
                </div>

                <div class="min-w-0">
                  <label class="text-sm font-medium text-slate-700" for="contrato-sueldo">{{ etiquetaSueldo }}</label>
                  <input
                    id="contrato-sueldo"
                    v-model="form.sueldo"
                    type="number"
                    min="0.01"
                    step="0.01"
                    :class="inputClass"
                  />
                </div>

                <div class="min-w-0">
                  <label class="text-sm font-medium text-slate-700" for="contrato-corte">Fecha de corte</label>
                  <input id="contrato-corte" v-model="form.fechaCorte" type="date" :class="inputClass" />
                </div>

                <label class="flex min-w-0 items-center gap-2 self-end pb-2 text-sm text-slate-700" for="contrato-activo">
                  <input
                    id="contrato-activo"
                    v-model="form.activo"
                    type="checkbox"
                    class="h-4 w-4 rounded border-slate-300"
                  />
                  Contrato activo
                </label>

                <div class="min-w-0 md:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="contrato-observaciones">Observaciones</label>
                  <textarea
                    id="contrato-observaciones"
                    v-model="form.observaciones"
                    rows="3"
                    maxlength="10000"
                    class="mt-1.5 block min-h-24 min-w-0 w-full resize-y rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
                  />
                </div>

                <div v-if="camposExtra.length" class="grid min-w-0 gap-4 sm:grid-cols-2 md:col-span-2">
                  <div v-for="campo in camposExtra" :key="campo.clave" class="min-w-0">
                    <label class="block break-words text-sm font-medium text-slate-700" :for="`contrato-extra-${campo.clave}`">
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
                </div>
              </fieldset>

              <p v-if="error" id="contrato-form-error" class="mt-4 shrink-0 break-words text-sm font-medium text-red-600" role="alert">
                {{ error }}
              </p>

              <footer class="mt-4 flex shrink-0 flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
                <Button
                  variant="outline"
                  class="h-10 w-full px-4 sm:w-auto"
                  type="button"
                  :disabled="submitting"
                  @click="emit('close')"
                >
                  Cancelar
                </Button>
                <Button
                  class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:w-auto"
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
