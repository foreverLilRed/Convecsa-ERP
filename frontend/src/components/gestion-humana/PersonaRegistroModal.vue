<script setup lang="ts">
import { computed, onUnmounted, reactive, ref, watch } from 'vue'

import UbigeoCombobox from '@/components/gestion-humana/UbigeoCombobox.vue'
import {
  DEPARTAMENTOS_PERU,
  findDepartamento,
  findProvincia,
} from '@/data/ubigeo-peru'
import { crearPersona } from '@/services/persona/personaService'
import {
  BANCOS,
  createEmptyPersonaForm,
  GRADOS_INSTRUCCION,
  TIPOS_DOCUMENTO,
  TIPOS_IMAGEN,
  toPersonaRequest,
  type PersonaForm,
} from '@/types/persona'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
  created: []
}>()

const form = reactive<PersonaForm>(createEmptyPersonaForm())
const error = ref('')
const submitting = ref(false)
const fotoInput = ref<HTMLInputElement | null>(null)

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
  if (form.fotoUrl) {
    URL.revokeObjectURL(form.fotoUrl)
  }
}

function resetForm() {
  revokeFotoUrl()
  Object.assign(form, createEmptyPersonaForm())
  error.value = ''
  submitting.value = false
  if (fotoInput.value) {
    fotoInput.value.value = ''
  }
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

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      resetForm()
      lockBodyScroll()
    }
  },
)

onUnmounted(() => {
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

function validate(): string | null {
  if (!form.apellidos.trim()) return 'Ingrese los apellidos.'
  if (!form.nombres.trim()) return 'Ingrese los nombres.'
  if (!form.numeroDocumento.trim()) return 'Ingrese el número de documento.'
  if (!form.fechaNacimiento) return 'Ingrese la fecha de nacimiento.'
  if (form.tipoDocumento === 'DNI' && !form.fechaEmisionDni) {
    return 'Ingrese la fecha de emisión del DNI.'
  }
  if (form.tipoDocumento === 'DNI' && !form.fechaCaducidadDni) {
    return 'Ingrese la fecha de caducidad del DNI.'
  }
  if (!form.celulares.some((celular) => celular.trim())) {
    return 'Ingrese al menos un celular.'
  }
  if (!form.correo.trim()) return 'Ingrese el correo electrónico.'
  if (!form.gradoInstruccion) return 'Seleccione el grado de instrucción.'
  if (!form.direccion.trim()) return 'Ingrese la dirección completa.'
  if (!form.departamento.trim()) return 'Ingrese el departamento.'
  if (!form.provincia.trim()) return 'Ingrese la provincia.'
  if (!form.distrito.trim()) return 'Ingrese el distrito.'
  if (!form.residencia.trim()) return 'Ingrese la residencia.'

  for (const [index, cuenta] of form.cuentasBancarias.entries()) {
    if (!cuenta.banco || !cuenta.numeroCuenta.trim()) {
      return `Complete banco y número de cuenta en la fila ${index + 1}.`
    }
    if (!/^\d+$/.test(cuenta.numeroCuenta)) {
      return `El número de cuenta de la fila ${index + 1} solo admite números.`
    }
    if (cuenta.cci.length !== 20) {
      return `El CCI de la fila ${index + 1} debe tener exactamente 20 dígitos.`
    }
  }

  if (
    form.cuentasBancarias.length > 0 &&
    form.cuentasBancarias.filter((cuenta) => cuenta.principal).length !== 1
  ) {
    return 'Marque una sola cuenta como principal.'
  }

  return null
}

async function onSubmit() {
  error.value = ''
  const message = validate()
  if (message) {
    error.value = message
    return
  }

  submitting.value = true
  try {
    await crearPersona(toPersonaRequest(form))
    emit('created')
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'No se pudo registrar la persona.'
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

        <div class="relative flex min-h-full items-start justify-center p-4 sm:p-6">
      <section
        class="modal-panel relative z-10 my-4 w-full max-w-[96vw] rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
        role="dialog"
        aria-modal="true"
        aria-labelledby="persona-modal-title"
      >
        <header class="flex items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
          <div>
            <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
              Gestión Humana / Personal
            </p>
            <h2 id="persona-modal-title" class="mt-1 text-lg font-bold text-palette-navy">
              Registrar persona
            </h2>
          </div>
          <button
            class="inline-flex h-8 w-8 items-center justify-center rounded-md text-slate-500 hover:bg-palette-ice hover:text-palette-navy disabled:cursor-not-allowed disabled:opacity-50"
            type="button"
            aria-label="Cerrar"
            :disabled="submitting"
            @click="emit('close')"
          >
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l12 12M18 6 6 18" />
            </svg>
          </button>
        </header>

        <form class="max-h-[75vh] space-y-8 overflow-y-auto px-5 py-5" @submit.prevent="onSubmit">
          <div class="grid items-start gap-8 md:grid-cols-2">
          <fieldset>
            <legend class="text-sm font-semibold text-palette-navy">Datos personales</legend>
            <div class="mt-4 grid gap-4 sm:grid-cols-2">
              <div class="sm:col-span-2 flex flex-wrap items-center gap-4">
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
                    class="!mt-0"
                    type="tel"
                    :placeholder="`Celular ${index + 1}`"
                  />
                  <button
                    v-if="form.celulares.length > 1"
                    class="shrink-0 rounded-md px-2 py-2 text-xs font-medium text-slate-500 hover:bg-palette-ice hover:text-palette-navy"
                    type="button"
                    @click="removeCelular(index)"
                  >
                    Quitar
                  </button>
                </div>
                <button
                  class="mt-2 text-xs font-semibold text-palette-royal hover:text-palette-navy"
                  type="button"
                  @click="addCelular"
                >
                  Agregar celular
                </button>
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

          <fieldset>
            <div class="flex items-center justify-between">
              <legend class="text-sm font-semibold text-palette-navy">Cuentas bancarias</legend>
              <button
                class="text-xs font-semibold text-palette-royal hover:text-palette-navy"
                type="button"
                @click="addCuenta"
              >
                Agregar cuenta
              </button>
            </div>
            <p class="mt-1 text-xs text-slate-500">
              Puede asociar varios bancos. El DNI de la persona se usa para entidades como BCP.
            </p>

            <div v-if="form.cuentasBancarias.length === 0" class="mt-3 text-sm text-slate-400">
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
                <button
                  class="text-xs font-medium text-slate-500 hover:text-palette-navy"
                  type="button"
                  @click="removeCuenta(index)"
                >
                  Quitar cuenta
                </button>
              </div>
            </div>
          </fieldset>

          <p v-if="error" class="text-sm font-medium text-red-600">{{ error }}</p>

          <footer class="flex justify-end gap-2 border-t border-palette-cyan/70 pt-4">
            <button
              class="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
              type="button"
              :disabled="submitting"
              @click="emit('close')"
            >
              Cancelar
            </button>
            <button
              class="rounded-lg bg-palette-navy px-4 py-2 text-sm font-semibold text-white hover:bg-palette-royal disabled:cursor-not-allowed disabled:opacity-50"
              type="submit"
              :disabled="submitting"
            >
              {{ submitting ? 'Guardando…' : 'Guardar' }}
            </button>
          </footer>
        </form>
      </section>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
