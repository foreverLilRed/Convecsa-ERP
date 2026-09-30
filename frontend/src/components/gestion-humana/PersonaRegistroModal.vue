<script setup lang="ts">
import { XIcon } from '@lucide/vue'
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import UbigeoCombobox from '@/components/gestion-humana/UbigeoCombobox.vue'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import {
  DEPARTAMENTOS_PERU,
  findDepartamento,
  findProvincia,
} from '@/data/ubigeo-peru'
import { crearPersona, actualizarPersona } from '@/services/persona/personaService'
import {
  APTITUDES_MEDICAS,
  BANCOS,
  createEmptyPersonaForm,
  GRADOS_INSTRUCCION,
  TIPOS_DOCUMENTO,
  TIPOS_IMAGEN,
  toPersonaForm,
  toPersonaRequest,
  vigenciaEmoLabel,
  type PersonaDTO,
  type PersonaForm,
} from '@/types/persona'

type SeccionPersona = 'identidad' | 'experiencia' | 'banco' | 'salud' | 'documentos'

const SECCIONES: { value: SeccionPersona; label: string }[] = [
  { value: 'identidad', label: 'Identidad' },
  { value: 'experiencia', label: 'Experiencia laboral' },
  { value: 'banco', label: 'Banco y pensiones' },
  { value: 'salud', label: 'Salud y SOMA' },
  { value: 'documentos', label: 'Checklist de documentos' },
]

const props = defineProps<{
  open: boolean
  persona?: PersonaDTO | null
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const form = reactive<PersonaForm>(createEmptyPersonaForm())
const error = ref('')
const submitting = ref(false)
const fotoInput = ref<HTMLInputElement | null>(null)
const seccionActiva = ref<SeccionPersona>('identidad')
const isEdit = computed(() => Boolean(props.persona?.id))
const vigenciaEmo = computed(() => vigenciaEmoLabel(form.fechaVigenciaEmo))

const provinciasDisponibles = computed(() => {
  return findDepartamento(form.departamento)?.provincias.map((provincia) => provincia.nombre) ?? []
})

const distritosDisponibles = computed(() => {
  return findProvincia(form.departamento, form.provincia)?.distritos ?? []
})

const provinciaHabilitada = computed(() => form.departamento.trim().length > 0)
const distritoHabilitado = computed(() => form.provincia.trim().length > 0)

function onDepartamentoInput() {
  form.provincia = ''
  form.distrito = ''
}

function onProvinciaInput() {
  form.distrito = ''
}

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

function revokeFotoUrl() {
  if (form.fotoUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(form.fotoUrl)
  }
}

function fillForm(source: PersonaForm) {
  revokeFotoUrl()
  Object.assign(form, source)
  error.value = ''
  submitting.value = false
  seccionActiva.value = 'identidad'
  if (fotoInput.value) {
    fotoInput.value.value = ''
  }
}

function resetForm() {
  fillForm(createEmptyPersonaForm())
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

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      if (props.persona) {
        fillForm(toPersonaForm(props.persona))
      } else {
        resetForm()
      }
      lockBodyScroll()
      document.addEventListener('keydown', onDocumentKeydown)
    } else {
      document.removeEventListener('keydown', onDocumentKeydown)
    }
  },
)

onUnmounted(() => {
  document.removeEventListener('keydown', onDocumentKeydown)
  revokeFotoUrl()
  unlockBodyScroll()
})

function onFotoChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }

  if (!TIPOS_IMAGEN.includes(file.type as (typeof TIPOS_IMAGEN)[number])) {
    error.value = 'Solo se admiten imágenes (JPG, PNG, WEBP o GIF).'
    input.value = ''
    return
  }

  revokeFotoUrl()
  form.fotoUrl = URL.createObjectURL(file)
  error.value = ''
}

function clearFoto() {
  revokeFotoUrl()
  form.fotoUrl = null
  if (fotoInput.value) {
    fotoInput.value.value = ''
  }
}

function addCelular() {
  form.celulares.push('')
}

function removeCelular(index: number) {
  if (form.celulares.length === 1) {
    return
  }
  form.celulares.splice(index, 1)
}

function addCuenta() {
  form.cuentasBancarias.push({
    banco: '',
    numeroCuenta: '',
    cci: '',
    principal: form.cuentasBancarias.length === 0,
  })
}

function removeCuenta(index: number) {
  const cuenta = form.cuentasBancarias[index]
  if (!cuenta) {
    return
  }
  const wasPrincipal = cuenta.principal
  form.cuentasBancarias.splice(index, 1)
  if (wasPrincipal && form.cuentasBancarias[0]) {
    form.cuentasBancarias[0].principal = true
  }
}

function setCuentaPrincipal(index: number) {
  form.cuentasBancarias.forEach((cuenta, cuentaIndex) => {
    cuenta.principal = cuentaIndex === index
  })
}

function addExperiencia() {
  form.experienciasLaborales.push({
    organizacion: '',
    cargo: '',
    fechaInicioLaboral: '',
    fechaFin: '',
  })
}

function removeExperiencia(index: number) {
  form.experienciasLaborales.splice(index, 1)
}

function onlyDigits(value: string): string {
  return value.replace(/\D/g, '')
}

function isAllowedDigitKey(event: KeyboardEvent): boolean {
  if (event.ctrlKey || event.metaKey || event.altKey) {
    return true
  }
  if (event.key.length !== 1) {
    return true
  }
  return /\d/.test(event.key)
}

function onDigitKeydown(event: KeyboardEvent) {
  if (!isAllowedDigitKey(event)) {
    event.preventDefault()
  }
}

function onDigitBeforeInput(event: InputEvent) {
  if (event.data && /\D/.test(event.data)) {
    event.preventDefault()
  }
}

function applyDigits(target: HTMLInputElement, value: string, max?: number): string {
  let digits = onlyDigits(value)
  if (max !== undefined) {
    digits = digits.slice(0, max)
  }
  target.value = digits
  return digits
}

function onNumeroCuentaInput(index: number, event: Event) {
  const cuenta = form.cuentasBancarias[index]
  if (!cuenta) {
    return
  }
  const target = event.target as HTMLInputElement
  cuenta.numeroCuenta = applyDigits(target, target.value)
}

function onCciInput(index: number, event: Event) {
  const cuenta = form.cuentasBancarias[index]
  if (!cuenta) {
    return
  }
  const target = event.target as HTMLInputElement
  cuenta.cci = applyDigits(target, target.value, 20)
}

function onNumeroCuentaPaste(index: number, event: ClipboardEvent) {
  const cuenta = form.cuentasBancarias[index]
  if (!cuenta) {
    return
  }
  event.preventDefault()
  const target = event.target as HTMLInputElement
  const pasted = event.clipboardData?.getData('text') ?? ''
  cuenta.numeroCuenta = applyDigits(target, `${target.value}${pasted}`)
}

function onCciPaste(index: number, event: ClipboardEvent) {
  const cuenta = form.cuentasBancarias[index]
  if (!cuenta) {
    return
  }
  event.preventDefault()
  const target = event.target as HTMLInputElement
  const pasted = event.clipboardData?.getData('text') ?? ''
  cuenta.cci = applyDigits(target, `${target.value}${pasted}`, 20)
}

function validate(): { message: string; seccion: SeccionPersona } | null {
  if (!form.apellidos.trim()) return { message: 'Ingrese los apellidos.', seccion: 'identidad' }
  if (!form.nombres.trim()) return { message: 'Ingrese los nombres.', seccion: 'identidad' }
  if (!form.numeroDocumento.trim()) return { message: 'Ingrese el número de documento.', seccion: 'identidad' }
  if (!form.fechaNacimiento) return { message: 'Ingrese la fecha de nacimiento.', seccion: 'identidad' }
  if (form.tipoDocumento === 'DNI' && !form.fechaEmisionDni) {
    return { message: 'Ingrese la fecha de emisión del DNI.', seccion: 'identidad' }
  }
  if (form.tipoDocumento === 'DNI' && !form.fechaCaducidadDni) {
    return { message: 'Ingrese la fecha de caducidad del DNI.', seccion: 'identidad' }
  }
  if (!form.celulares.some((celular) => celular.trim())) {
    return { message: 'Ingrese al menos un celular.', seccion: 'identidad' }
  }
  if (!form.correo.trim()) return { message: 'Ingrese el correo electrónico.', seccion: 'identidad' }
  if (!form.gradoInstruccion) return { message: 'Seleccione el grado de instrucción.', seccion: 'identidad' }
  if (!form.direccion.trim()) return { message: 'Ingrese la dirección completa.', seccion: 'identidad' }
  if (!form.departamento.trim()) return { message: 'Ingrese el departamento.', seccion: 'identidad' }
  if (!form.provincia.trim()) return { message: 'Ingrese la provincia.', seccion: 'identidad' }
  if (!form.distrito.trim()) return { message: 'Ingrese el distrito.', seccion: 'identidad' }
  if (!form.residencia.trim()) return { message: 'Ingrese la residencia.', seccion: 'identidad' }

  for (const [index, experiencia] of form.experienciasLaborales.entries()) {
    if (!experiencia.organizacion.trim() || !experiencia.cargo.trim() || !experiencia.fechaInicioLaboral) {
      return {
        message: `Complete organización, cargo e inicio laboral en la fila ${index + 1}.`,
        seccion: 'experiencia',
      }
    }
    if (experiencia.fechaFin && experiencia.fechaFin < experiencia.fechaInicioLaboral) {
      return {
        message: `La fecha de fin de la fila ${index + 1} no puede ser menor que la de inicio.`,
        seccion: 'experiencia',
      }
    }
  }

  for (const [index, cuenta] of form.cuentasBancarias.entries()) {
    if (!cuenta.banco || !cuenta.numeroCuenta.trim()) {
      return { message: `Complete banco y número de cuenta en la fila ${index + 1}.`, seccion: 'banco' }
    }
    if (!/^\d+$/.test(cuenta.numeroCuenta)) {
      return { message: `El número de cuenta de la fila ${index + 1} solo admite números.`, seccion: 'banco' }
    }
    if (cuenta.cci.length !== 20) {
      return { message: `El CCI de la fila ${index + 1} debe tener exactamente 20 dígitos.`, seccion: 'banco' }
    }
  }

  if (
    form.cuentasBancarias.length > 0 &&
    form.cuentasBancarias.filter((cuenta) => cuenta.principal).length !== 1
  ) {
    return { message: 'Marque una sola cuenta como principal.', seccion: 'banco' }
  }

  if (!form.fechaVigenciaEmo) {
    return { message: 'Ingrese la fecha de vigencia de EMO.', seccion: 'salud' }
  }
  if (!form.aptitudMedica) {
    return { message: 'Seleccione la aptitud médica ocupacional.', seccion: 'salud' }
  }

  return null
}

async function onSubmit() {
  error.value = ''
  const invalid = validate()
  if (invalid) {
    seccionActiva.value = invalid.seccion
    error.value = invalid.message
    toast.error(invalid.message)
    return
  }

  submitting.value = true
  try {
    const request = toPersonaRequest(form)
    const save = isEdit.value && props.persona
      ? actualizarPersona(props.persona.id, request)
      : crearPersona(request)
    await toast.promise(save, {
      loading: isEdit.value ? 'Guardando cambios…' : 'Guardando persona…',
      success: isEdit.value ? 'Persona actualizada' : 'Persona registrada',
      error: (err: unknown) =>
        err instanceof Error
          ? err.message
          : isEdit.value
            ? 'No se pudo actualizar la persona.'
            : 'No se pudo registrar la persona.',
    })?.unwrap()
    emit('saved')
  } catch (err) {
    error.value = err instanceof Error
      ? err.message
      : isEdit.value
        ? 'No se pudo actualizar la persona.'
        : 'No se pudo registrar la persona.'
  } finally {
    submitting.value = false
  }
}

function onOverlayClick() {
  if (submitting.value) {
    return
  }
  emit('close')
}
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
        class="modal-panel relative z-10 flex h-[min(92vh,52rem)] w-full max-w-[96vw] flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
        role="dialog"
        aria-modal="true"
        aria-labelledby="persona-modal-title"
      >
        <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
          <div>
            <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
              Gestión Humana / Personal
            </p>
            <h2 id="persona-modal-title" class="mt-1 text-lg font-bold text-palette-navy">
              {{ isEdit ? 'Editar persona' : 'Registrar persona' }}
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
          <Tabs v-model="seccionActiva" class="min-h-0 flex-1 gap-6">
            <div class="shrink-0 overflow-x-auto pb-2">
            <TabsList
              class="inline-flex h-auto w-max min-w-full gap-1 rounded-xl border border-slate-200 bg-slate-100/85 p-1.5 sm:min-w-0"
            >
              <TabsTrigger
                v-for="seccion in SECCIONES"
                :key="seccion.value"
                :value="seccion.value"
                class="h-9 flex-none rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm"
              >
                {{ seccion.label }}
              </TabsTrigger>
            </TabsList>
            </div>

            <div class="min-h-0 flex-1 overflow-y-auto pr-1">
            <TabsContent value="identidad" class="space-y-8">
              <div class="grid items-start gap-8 md:grid-cols-2">
                <fieldset>
                  <legend class="text-sm font-semibold text-palette-navy">Datos personales</legend>
                  <div class="mt-4 grid gap-4 sm:grid-cols-2">
                    <div class="flex flex-wrap items-center gap-4 sm:col-span-2">
                      <div
                        class="flex h-24 w-24 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-palette-cyan/70 bg-palette-soft"
                      >
                        <img
                          v-if="form.fotoUrl"
                          :src="form.fotoUrl"
                          alt="Foto de la persona"
                          class="h-full w-full object-cover"
                        />
                        <span v-else class="px-2 text-center text-[10px] font-medium text-slate-400">Sin foto</span>
                      </div>
                      <div>
                        <p class="text-xs font-semibold uppercase tracking-wide text-slate-700">Foto</p>
                        <input
                          ref="fotoInput"
                          class="mt-1.5 block w-full text-sm text-slate-600 file:mr-3 file:rounded-md file:border-0 file:bg-palette-ice file:px-3 file:py-1.5 file:text-xs file:font-semibold file:text-palette-navy hover:file:bg-palette-cyan/40"
                          type="file"
                          accept="image/jpeg,image/png,image/webp,image/gif"
                          @change="onFotoChange"
                        />
                        <p class="mt-1 text-[11px] text-slate-400">JPG, PNG, WEBP o GIF</p>
                        <button
                          v-if="form.fotoUrl"
                          class="mt-1 text-xs font-medium text-slate-500 hover:text-palette-navy"
                          type="button"
                          @click="clearFoto"
                        >
                          Quitar foto
                        </button>
                      </div>
                    </div>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Apellidos
                      <input v-model="form.apellidos" :class="inputClass" type="text" autocomplete="family-name" />
                    </label>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Nombres
                      <input v-model="form.nombres" :class="inputClass" type="text" autocomplete="given-name" />
                    </label>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Tipo de documento
                      <select v-model="form.tipoDocumento" :class="inputClass">
                        <option v-for="tipo in TIPOS_DOCUMENTO" :key="tipo" :value="tipo">{{ tipo }}</option>
                      </select>
                    </label>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Número de documento
                      <input v-model="form.numeroDocumento" :class="inputClass" type="text" />
                    </label>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Fecha de nacimiento
                      <input v-model="form.fechaNacimiento" :class="inputClass" type="date" />
                    </label>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Fecha de emisión del DNI
                      <input v-model="form.fechaEmisionDni" :class="inputClass" type="date" />
                    </label>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Fecha de caducidad del DNI
                      <input v-model="form.fechaCaducidadDni" :class="inputClass" type="date" />
                    </label>
                    <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                      Grado de instrucción
                      <select v-model="form.gradoInstruccion" :class="inputClass">
                        <option value="" disabled>Seleccione</option>
                        <option v-for="grado in GRADOS_INSTRUCCION" :key="grado.value" :value="grado.value">
                          {{ grado.label }}
                        </option>
                      </select>
                    </label>
                  </div>
                </fieldset>

                <div class="space-y-8">
                  <fieldset>
                    <legend class="text-sm font-semibold text-palette-navy">Contacto</legend>
                    <div class="mt-4 space-y-3">
                      <div>
                        <p class="text-xs font-semibold uppercase tracking-wide text-slate-700">Celulares</p>
                        <div
                          v-for="(_, index) in form.celulares"
                          :key="index"
                          class="mt-2 flex items-center gap-2"
                        >
                          <input
                            v-model="form.celulares[index]"
                            :class="inputClass"
                            class="mt-0!"
                            type="tel"
                            :placeholder="`Celular ${index + 1}`"
                          />
                          <Button
                            v-if="form.celulares.length > 1"
                            variant="ghost"
                            size="sm"
                            class="h-9 shrink-0 px-3"
                            type="button"
                            @click="removeCelular(index)"
                          >
                            Quitar
                          </Button>
                        </div>
                        <Button
                          variant="link"
                          class="mt-1 h-auto px-0 text-palette-royal"
                          type="button"
                          @click="addCelular"
                        >
                          Agregar celular
                        </Button>
                      </div>

                      <div class="grid gap-4 sm:grid-cols-2">
                        <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                          Correo electrónico
                          <input v-model="form.correo" :class="inputClass" type="email" autocomplete="email" />
                        </label>
                        <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                          Correo secundario
                          <input v-model="form.correoSecundario" :class="inputClass" type="email" />
                        </label>
                      </div>
                    </div>
                  </fieldset>

                  <fieldset>
                    <legend class="text-sm font-semibold text-palette-navy">Domicilio</legend>
                    <div class="mt-4 grid gap-4 sm:grid-cols-2">
                      <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                        Departamento
                        <UbigeoCombobox
                          v-model="form.departamento"
                          :options="DEPARTAMENTOS_PERU"
                          placeholder="Seleccione o escriba"
                          @update:model-value="onDepartamentoInput"
                        />
                      </label>
                      <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                        Provincia
                        <UbigeoCombobox
                          v-model="form.provincia"
                          :options="provinciasDisponibles"
                          placeholder="Seleccione o escriba"
                          :disabled="!provinciaHabilitada"
                          @update:model-value="onProvinciaInput"
                        />
                      </label>
                      <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                        Distrito
                        <UbigeoCombobox
                          v-model="form.distrito"
                          :options="distritosDisponibles"
                          placeholder="Seleccione o escriba"
                          :disabled="!distritoHabilitado"
                        />
                      </label>
                      <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                        Residencia
                        <input v-model="form.residencia" :class="inputClass" type="text" />
                      </label>
                      <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700 sm:col-span-2">
                        Dirección completa
                        <input v-model="form.direccion" :class="inputClass" type="text" />
                      </label>
                    </div>
                  </fieldset>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="experiencia">
              <fieldset>
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <legend class="text-sm font-semibold text-palette-navy">Experiencia laboral</legend>
                    <p class="mt-1 text-xs text-slate-500">
                      Puede registrar varios trabajos. La fecha de fin es opcional si sigue laborando.
                    </p>
                  </div>
                  <Button variant="outline" class="h-9 shrink-0 px-3" type="button" @click="addExperiencia">
                    Agregar experiencia
                  </Button>
                </div>

                <div v-if="form.experienciasLaborales.length === 0" class="mt-4 text-sm text-slate-400">
                  Sin experiencias registradas.
                </div>

                <div
                  v-for="(experiencia, index) in form.experienciasLaborales"
                  :key="index"
                  class="mt-3 grid gap-3 rounded-lg border border-palette-cyan/60 bg-palette-soft/60 p-3 sm:grid-cols-2"
                >
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Organización
                    <input v-model="experiencia.organizacion" :class="inputClass" type="text" />
                  </label>
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Cargo
                    <input v-model="experiencia.cargo" :class="inputClass" type="text" />
                  </label>
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Inicio laboral
                    <input v-model="experiencia.fechaInicioLaboral" :class="inputClass" type="date" />
                  </label>
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Fecha fin
                    <input v-model="experiencia.fechaFin" :class="inputClass" type="date" />
                  </label>
                  <div class="flex justify-end sm:col-span-2">
                    <Button variant="ghost" size="sm" class="h-8 px-3" type="button" @click="removeExperiencia(index)">
                      Quitar
                    </Button>
                  </div>
                </div>
              </fieldset>
            </TabsContent>

            <TabsContent value="banco">
              <fieldset>
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <legend class="text-sm font-semibold text-palette-navy">Cuentas bancarias</legend>
                    <p class="mt-1 text-xs text-slate-500">
                      Puede asociar varios bancos. El DNI de la persona se usa para entidades como BCP.
                    </p>
                  </div>
                  <Button variant="outline" class="h-9 shrink-0 px-3" type="button" @click="addCuenta">
                    Agregar cuenta
                  </Button>
                </div>

                <div v-if="form.cuentasBancarias.length === 0" class="mt-4 text-sm text-slate-400">
                  Sin cuentas asociadas.
                </div>

                <div
                  v-for="(cuenta, index) in form.cuentasBancarias"
                  :key="index"
                  class="mt-3 grid gap-3 rounded-lg border border-palette-cyan/60 bg-palette-soft/60 p-3 sm:grid-cols-2"
                >
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Banco
                    <select v-model="cuenta.banco" :class="inputClass">
                      <option value="" disabled>Seleccione</option>
                      <option v-for="banco in BANCOS" :key="banco.value" :value="banco.value">
                        {{ banco.label }}
                      </option>
                    </select>
                  </label>
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Número de cuenta
                    <input
                      :value="cuenta.numeroCuenta"
                      :class="inputClass"
                      type="text"
                      inputmode="numeric"
                      pattern="[0-9]*"
                      autocomplete="off"
                      @keydown="onDigitKeydown"
                      @beforeinput="onDigitBeforeInput"
                      @paste="onNumeroCuentaPaste(index, $event)"
                      @input="onNumeroCuentaInput(index, $event)"
                    />
                  </label>
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    CCI
                    <input
                      :value="cuenta.cci"
                      :class="inputClass"
                      type="text"
                      inputmode="numeric"
                      pattern="[0-9]{20}"
                      maxlength="20"
                      minlength="20"
                      autocomplete="off"
                      @keydown="onDigitKeydown"
                      @beforeinput="onDigitBeforeInput"
                      @paste="onCciPaste(index, $event)"
                      @input="onCciInput(index, $event)"
                    />
                    <span class="mt-1 block text-[11px] font-normal normal-case tracking-normal text-slate-400">
                      {{ cuenta.cci.length }}/20 dígitos
                    </span>
                  </label>
                  <div class="flex items-center justify-between gap-3 sm:col-span-2">
                    <label class="flex cursor-pointer items-center text-sm text-slate-600">
                      <input
                        class="h-4 w-4 rounded border-slate-300 text-palette-navy focus:ring-palette-royal focus:ring-offset-0"
                        type="checkbox"
                        :checked="cuenta.principal"
                        @change="setCuentaPrincipal(index)"
                      />
                      <span class="ml-2 text-xs font-semibold uppercase tracking-wide">
                        Cuenta principal
                      </span>
                    </label>
                    <Button variant="ghost" size="sm" class="h-8 px-3" type="button" @click="removeCuenta(index)">
                      Quitar cuenta
                    </Button>
                  </div>
                </div>
              </fieldset>
            </TabsContent>

            <TabsContent value="salud">
              <fieldset>
                <legend class="text-sm font-semibold text-palette-navy">Salud y SOMA</legend>
                <p class="mt-1 text-xs text-slate-500">
                  La vigencia se calcula automáticamente a partir de la fecha de EMO.
                </p>
                <div class="mt-4 grid gap-4 sm:grid-cols-2">
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Fecha de vigencia de EMO
                    <input v-model="form.fechaVigenciaEmo" :class="inputClass" type="date" />
                  </label>
                  <div class="block text-xs font-semibold uppercase tracking-wide text-slate-700">
                    Vigencia
                    <p class="mt-1.5 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-medium normal-case tracking-normal text-slate-800">
                      {{ vigenciaEmo }}
                    </p>
                  </div>
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700 sm:col-span-2">
                    Aptitud médica ocupacional
                    <select v-model="form.aptitudMedica" :class="inputClass">
                      <option value="" disabled>Seleccione</option>
                      <option v-for="aptitud in APTITUDES_MEDICAS" :key="aptitud.value" :value="aptitud.value">
                        {{ aptitud.label }}
                      </option>
                    </select>
                  </label>
                  <label class="block text-xs font-semibold uppercase tracking-wide text-slate-700 sm:col-span-2">
                    Restricciones médicas / observaciones
                    <Textarea
                      v-model="form.restriccionesMedicas"
                      class="mt-1.5 min-h-24"
                      placeholder="Opcional"
                    />
                  </label>
                </div>
              </fieldset>
            </TabsContent>

            <TabsContent value="documentos">
              <p class="rounded-lg border border-dashed border-palette-cyan/70 bg-palette-soft/50 px-4 py-12 text-center text-sm text-slate-500">
                Checklist de documentos se implementará más adelante.
              </p>
            </TabsContent>
            </div>
          </Tabs>

          <p v-if="error" id="persona-form-error" class="mt-4 shrink-0 text-sm font-medium text-red-600" role="alert">
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
              :aria-describedby="error ? 'persona-form-error' : undefined"
            >
              {{ submitting ? 'Guardando…' : isEdit ? 'Guardar cambios' : 'Guardar' }}
            </Button>
          </footer>
        </form>
      </section>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
