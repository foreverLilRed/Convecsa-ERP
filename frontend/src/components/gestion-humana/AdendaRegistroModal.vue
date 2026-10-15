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
import { Textarea } from '@/components/ui/textarea'
import { crearAdenda, listarContratos, obtenerContratosCatalogos } from '@/services/contrato/contratoService'
import {
  duracionAdendaLabel,
  MOTIVOS_ADENDA,
  type CatalogoItem,
  type ContratoDTO,
  type ContratosCatalogos,
  type PlantillaCampo,
  type PlantillaContrato,
} from '@/types/contrato'

type AdendaForm = {
  contratoId: number | null
  plantillaContratoId: string
  fechaFinAnterior: string
  fechaFinNueva: string
  sueldo: string
  motivo: string
  ciudadEmision: string
  fechaEmision: string
}

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

function emptyForm(): AdendaForm {
  return {
    contratoId: null,
    plantillaContratoId: '',
    fechaFinAnterior: '',
    fechaFinNueva: '',
    sueldo: '',
    motivo: MOTIVOS_ADENDA[0],
    ciudadEmision: 'Lima',
    fechaEmision: new Date().toISOString().slice(0, 10),
  }
}

const form = reactive<AdendaForm>(emptyForm())
const extras = reactive<Record<string, string>>({})
const error = ref('')
const submitting = ref(false)
const catalogos = ref<ContratosCatalogos | null>(null)
const contratos = ref<ContratoDTO[]>([])
const contratoSeleccionado = ref<ContratoDTO | null>(null)
const contratoSearch = ref('')

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const plantillasAdenda = computed(() =>
  (catalogos.value?.plantillas ?? []).filter((item) => item.uso === 'ADENDA'),
)

const plantillaSeleccionada = computed<PlantillaContrato | null>(() => {
  const id = Number(form.plantillaContratoId)
  if (!Number.isFinite(id)) {
    return null
  }
  return plantillasAdenda.value.find((item) => item.id === id) ?? null
})

const camposExtra = computed(() => plantillaSeleccionada.value?.metadata ?? [])

const duracion = computed(() => duracionAdendaLabel(form.fechaFinAnterior, form.fechaFinNueva))

const contratoLabel = computed(() => {
  const contrato = contratoSeleccionado.value
  if (!contrato) {
    return ''
  }
  return `${contrato.personaApellidos}, ${contrato.personaNombres} · ${contrato.numeroDocumento}`
})

const contratosOpciones = computed(() => {
  const q = contratoSearch.value.trim().toLowerCase()
  const base = contratos.value
  const filtrados = q
    ? base.filter((item) =>
        `${item.personaApellidos} ${item.personaNombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
      )
    : base
  const seleccion = contratoSeleccionado.value
  if (!seleccion) {
    return filtrados
  }
  if (filtrados.some((item) => item.id === seleccion.id)) {
    return filtrados
  }
  return [seleccion, ...filtrados]
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
    extras[campo.clave] = ''
  }
}

function resetForm() {
  Object.assign(form, emptyForm())
  resetExtras([])
  error.value = ''
  submitting.value = false
  contratoSeleccionado.value = null
  contratoSearch.value = ''
}

function plusThreeMonths(iso: string): string {
  const parsed = new Date(`${iso}T12:00:00`)
  if (Number.isNaN(parsed.getTime())) {
    return ''
  }
  parsed.setMonth(parsed.getMonth() + 3)
  return parsed.toISOString().slice(0, 10)
}

function aplicarContrato(contrato: ContratoDTO) {
  form.contratoId = contrato.id
  form.fechaFinAnterior = contrato.fechaFin ?? ''
  form.sueldo = contrato.sueldo != null ? String(contrato.sueldo) : ''
  form.ciudadEmision = contrato.ciudadFirma?.trim() || 'Lima'
  form.fechaFinNueva = contrato.fechaFin ? plusThreeMonths(contrato.fechaFin) : ''
}

watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) {
      resetForm()
      lockBodyScroll()
      document.addEventListener('keydown', onDocumentKeydown)
      try {
        const [nextCatalogos, nextContratos] = await Promise.all([
          obtenerContratosCatalogos(),
          listarContratos(0, 100, 'ACTIVO'),
        ])
        catalogos.value = nextCatalogos
        contratos.value = nextContratos.content
        if (plantillasAdenda.value.length === 1) {
          form.plantillaContratoId = String(plantillasAdenda.value[0]!.id)
        }
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

function onContratoChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(id)) {
    form.contratoId = null
    contratoSeleccionado.value = null
    form.fechaFinAnterior = ''
    return
  }
  const contrato = contratosOpciones.value.find((item) => item.id === id) ?? contratoSeleccionado.value
  contratoSeleccionado.value = contrato ?? null
  if (contrato) {
    aplicarContrato(contrato)
  }
}

function validate(): string | null {
  if (!form.contratoId) return 'Seleccione el contrato a prorrogar.'
  if (!form.plantillaContratoId) return 'Seleccione la plantilla de adenda.'
  if (!form.fechaFinAnterior) return 'El contrato no tiene fecha de fin.'
  if (!form.fechaFinNueva) return 'Ingrese la nueva fecha de término.'
  if (form.fechaFinNueva <= form.fechaFinAnterior) {
    return 'La nueva fecha de término debe ser posterior al vencimiento actual.'
  }
  const sueldo = Number(form.sueldo)
  if (!Number.isFinite(sueldo) || sueldo <= 0) return 'Ingrese un sueldo mayor a cero.'
  if (!form.motivo.trim()) return 'Indique el motivo de la prórroga.'
  if (!form.fechaEmision) return 'Ingrese la fecha de emisión.'
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
    await crearAdenda({
      contratoId: form.contratoId as number,
      plantillaContratoId: Number(form.plantillaContratoId),
      fechaFinNueva: form.fechaFinNueva,
      sueldo: Number(form.sueldo),
      motivo: form.motivo.trim(),
      ciudadEmision: form.ciudadEmision.trim() || null,
      fechaEmision: form.fechaEmision,
      metadata,
    })
    toast.success('Adenda registrada.')
    emit('saved')
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear la adenda.'
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
  if (campo.tipo === 'date') return 'date'
  if (campo.tipo === 'number') return 'number'
  return 'text'
}

onUnmounted(() => {
  document.removeEventListener('keydown', onDocumentKeydown)
  unlockBodyScroll()
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
      <div v-if="open" class="modal-root fixed inset-0 z-50 overflow-y-auto">
        <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="onOverlayClick" />
        <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
          <section
            class="modal-panel relative z-10 flex h-[min(92vh,48rem)] w-full max-w-3xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
            role="dialog"
            aria-modal="true"
            aria-labelledby="adenda-modal-title"
          >
            <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
              <div>
                <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                  Gestión Humana / Contratos
                </p>
                <h2 id="adenda-modal-title" class="mt-1 text-lg font-bold text-palette-navy">Nueva adenda</h2>
              </div>
              <Button variant="ghost" size="icon" type="button" aria-label="Cerrar" :disabled="submitting" @click="emit('close')">
                <XIcon />
              </Button>
            </header>

            <form class="flex min-h-0 flex-1 flex-col px-5 py-5" @submit.prevent="onSubmit">
              <fieldset class="min-h-0 flex-1 space-y-4 overflow-y-auto pr-1" :disabled="submitting">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="adenda-contrato">Contrato activo</label>
                  <Combobox :model-value="form.contratoId ?? undefined" ignore-filter class="w-full" @update:model-value="onContratoChange">
                    <ComboboxAnchor class="w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="adenda-contrato"
                          variant="outline"
                          class="mt-1.5 h-10 w-full justify-between px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span class="truncate" :class="contratoLabel ? 'text-slate-900' : 'text-slate-400'">
                            {{ contratoLabel || 'Buscar por persona o documento' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="contratoSearch" placeholder="Buscar contrato..." autocomplete="off" />
                      <ComboboxEmpty>Sin contratos activos.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem v-for="item in contratosOpciones" :key="item.id" :value="item.id">
                          {{ item.personaApellidos }}, {{ item.personaNombres }} · {{ item.numeroDocumento }}
                          <ComboboxItemIndicator>
                            <CheckIcon />
                          </ComboboxItemIndicator>
                        </ComboboxItem>
                      </ComboboxGroup>
                    </ComboboxList>
                  </Combobox>
                  <p v-if="contratoSeleccionado" class="mt-2 text-sm text-slate-600">
                    <template v-if="contratoSeleccionado.cargo">{{ contratoSeleccionado.cargo }}</template>
                    <template v-if="contratoSeleccionado.proyecto"> · {{ contratoSeleccionado.proyecto }}</template>
                  </p>
                </div>

                <div>
                  <label class="text-sm font-medium text-slate-700" for="adenda-plantilla">Plantilla</label>
                  <select id="adenda-plantilla" v-model="form.plantillaContratoId" :class="inputClass">
                    <option value="">Seleccione</option>
                    <option v-for="item in plantillasAdenda" :key="item.id" :value="String(item.id)">
                      {{ item.nombre }}
                    </option>
                  </select>
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="adenda-fin-previa">Vencimiento actual</label>
                    <input id="adenda-fin-previa" :value="form.fechaFinAnterior" type="date" :class="inputClass" disabled />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="adenda-fin-nueva">Nueva fecha de término</label>
                    <input id="adenda-fin-nueva" v-model="form.fechaFinNueva" type="date" :class="inputClass" />
                  </div>
                </div>

                <p
                  v-if="duracion"
                  class="rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-900"
                  role="status"
                >
                  Duración de la adenda: <strong>{{ duracion }}</strong>
                </p>

                <div class="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="adenda-sueldo">Sueldo</label>
                    <input id="adenda-sueldo" v-model="form.sueldo" type="number" min="0.01" step="0.01" :class="inputClass" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="adenda-emision">Fecha de emisión</label>
                    <input id="adenda-emision" v-model="form.fechaEmision" type="date" :class="inputClass" />
                  </div>
                </div>

                <div>
                  <label class="text-sm font-medium text-slate-700" for="adenda-ciudad">Ciudad de emisión</label>
                  <input id="adenda-ciudad" v-model="form.ciudadEmision" type="text" maxlength="80" :class="inputClass" />
                </div>

                <div>
                  <p class="text-sm font-medium text-slate-700">Motivo de la prórroga</p>
                  <div class="mt-2 flex flex-wrap gap-2">
                    <button
                      v-for="preset in MOTIVOS_ADENDA"
                      :key="preset"
                      type="button"
                      class="rounded-md border px-2.5 py-1 text-left text-xs"
                      :class="form.motivo === preset
                        ? 'border-palette-royal bg-palette-ice font-semibold text-palette-navy'
                        : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'"
                      @click="form.motivo = preset"
                    >
                      {{ preset }}
                    </button>
                  </div>
                  <label class="sr-only" for="adenda-motivo">Motivo</label>
                  <Textarea id="adenda-motivo" v-model="form.motivo" class="mt-2 min-h-20" maxlength="2000" />
                </div>

                <div v-if="camposExtra.length" class="grid gap-4 sm:grid-cols-2">
                  <div v-for="campo in camposExtra" :key="campo.clave">
                    <label class="text-sm font-medium text-slate-700" :for="`adenda-extra-${campo.clave}`">
                      {{ campo.label }}
                    </label>
                    <select
                      v-if="campo.tipo === 'catalogo'"
                      :id="`adenda-extra-${campo.clave}`"
                      v-model="extras[campo.clave]"
                      :class="inputClass"
                    >
                      <option value="">Seleccione</option>
                      <option v-for="item in opcionesCatalogo(campo)" :key="item.id" :value="String(item.id)">
                        {{ item.label }}
                      </option>
                    </select>
                    <input
                      v-else
                      :id="`adenda-extra-${campo.clave}`"
                      v-model="extras[campo.clave]"
                      :type="inputType(campo)"
                      :class="inputClass"
                    />
                  </div>
                </div>
              </fieldset>

              <p v-if="error" id="adenda-form-error" class="mt-4 shrink-0 text-sm font-medium text-red-600" role="alert">
                {{ error }}
              </p>

              <footer class="mt-4 flex shrink-0 flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
                <Button variant="outline" class="h-10 px-4" type="button" :disabled="submitting" @click="emit('close')">
                  Cancelar
                </Button>
                <Button
                  class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="submitting"
                  :aria-describedby="error ? 'adenda-form-error' : undefined"
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
