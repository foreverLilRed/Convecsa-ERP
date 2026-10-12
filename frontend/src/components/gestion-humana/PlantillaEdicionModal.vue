<script setup lang="ts">
import { LockIcon, XIcon } from '@lucide/vue'
import { onUnmounted, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { ApiError } from '@/services/http'
import {
  actualizarPlantillaContrato,
  obtenerPlantillaContrato,
} from '@/services/contrato/contratoService'
import type { UsoPlantilla } from '@/types/contrato'

const TOKEN_EJEMPLOS = {
  nombres: '{{trabajador_nombres}}',
  fecha: '{{fecha_inicio}}',
  sueldo: '{{sueldo}}',
  meta: '{{meta.clave}}',
} as const

const props = defineProps<{
  open: boolean
  plantillaId: number | null
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

type Formulario = {
  codigo: string
  nombre: string
  uso: UsoPlantilla
  plantilla: string
}

const form = reactive<Formulario>({
  codigo: '',
  nombre: '',
  uso: 'CONTRATO',
  plantilla: '',
})

const step = ref<'pin' | 'edit'>('pin')
const pin = ref('')
const error = ref('')
const unlocking = ref(false)
const submitting = ref(false)

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

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
  if (event.key === 'Escape' && props.open && !submitting.value && !unlocking.value) {
    emit('close')
  }
}

function resetState() {
  step.value = 'pin'
  pin.value = ''
  error.value = ''
  unlocking.value = false
  submitting.value = false
  form.codigo = ''
  form.nombre = ''
  form.uso = 'CONTRATO'
  form.plantilla = ''
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      resetState()
      lockBodyScroll()
      document.addEventListener('keydown', onDocumentKeydown)
    } else {
      document.removeEventListener('keydown', onDocumentKeydown)
    }
  },
)

async function onUnlock() {
  error.value = ''
  if (!pin.value.trim()) {
    error.value = 'Ingrese el PIN de superusuario.'
    return
  }
  if (props.plantillaId == null) {
    return
  }

  unlocking.value = true
  try {
    const detalle = await obtenerPlantillaContrato(props.plantillaId)
    form.codigo = detalle.codigo
    form.nombre = detalle.nombre
    form.uso = detalle.uso
    form.plantilla = detalle.plantilla
    step.value = 'edit'
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo cargar la plantilla.'
    error.value = message
    toast.error(message)
  } finally {
    unlocking.value = false
  }
}

function validate(): string | null {
  if (!form.nombre.trim()) return 'Ingrese el nombre de la plantilla.'
  if (!form.plantilla.trim()) return 'Ingrese el HTML de la plantilla.'
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
  if (props.plantillaId == null) {
    return
  }

  submitting.value = true
  try {
    await actualizarPlantillaContrato(props.plantillaId, {
      nombre: form.nombre.trim(),
      plantilla: form.plantilla,
      uso: form.uso,
      pin: pin.value.trim(),
    })
    toast.success('Plantilla actualizada.')
    emit('saved')
  } catch (err) {
    if (err instanceof ApiError && err.status === 403) {
      step.value = 'pin'
      pin.value = ''
      error.value = 'PIN de superusuario incorrecto.'
      toast.error(error.value)
      return
    }
    const message = err instanceof Error ? err.message : 'No se pudo actualizar la plantilla.'
    error.value = message
    toast.error(message)
  } finally {
    submitting.value = false
  }
}

function onOverlayClick() {
  if (!submitting.value && !unlocking.value) {
    emit('close')
  }
}

onUnmounted(() => {
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
            v-if="step === 'pin'"
            class="modal-panel relative z-10 w-full max-w-md rounded-xl border border-palette-cyan/70 bg-white p-6 shadow-lg"
            role="dialog"
            aria-modal="true"
            aria-labelledby="plantilla-pin-title"
          >
            <header class="flex items-start justify-between">
              <div>
                <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                  Edición restringida
                </p>
                <h2 id="plantilla-pin-title" class="mt-1 text-lg font-bold text-palette-navy">
                  PIN de superusuario
                </h2>
              </div>
              <Button
                variant="ghost"
                size="icon"
                class="shrink-0"
                type="button"
                aria-label="Cerrar"
                :disabled="unlocking"
                @click="emit('close')"
              >
                <XIcon />
              </Button>
            </header>

            <p class="mt-3 text-sm leading-relaxed text-slate-500">
              Editar una plantilla de contrato o adenda requiere el PIN de superusuario.
            </p>

            <form class="mt-5 space-y-4" @submit.prevent="onUnlock">
              <div>
                <label class="text-sm font-medium text-slate-700" for="plantilla-pin">PIN</label>
                <input
                  id="plantilla-pin"
                  v-model="pin"
                  type="password"
                  inputmode="numeric"
                  autocomplete="off"
                  maxlength="12"
                  placeholder="••••"
                  :class="inputClass"
                  :disabled="unlocking"
                />
              </div>

              <p v-if="error" class="text-sm font-medium text-red-600" role="alert">{{ error }}</p>

              <Button
                class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal"
                type="submit"
                :disabled="unlocking"
              >
                <LockIcon v-if="!unlocking" data-icon="inline-start" />
                {{ unlocking ? 'Verificando…' : 'Desbloquear edición' }}
              </Button>
            </form>
          </section>

          <section
            v-else
            class="modal-panel relative z-10 flex h-[min(92vh,56rem)] w-full max-w-6xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
            role="dialog"
            aria-modal="true"
            aria-labelledby="plantilla-edit-modal-title"
          >
            <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
              <div>
                <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                  Gestión Humana / Plantillas de Contrato
                </p>
                <h2 id="plantilla-edit-modal-title" class="mt-1 text-lg font-bold text-palette-navy">
                  Editar plantilla · {{ form.codigo }}
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
                <div class="grid gap-4 sm:grid-cols-3">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="plantilla-edit-codigo">Código</label>
                    <input
                      id="plantilla-edit-codigo"
                      :value="form.codigo"
                      type="text"
                      :class="inputClass"
                      disabled
                      readonly
                    />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="plantilla-edit-nombre">Nombre</label>
                    <input
                      id="plantilla-edit-nombre"
                      v-model="form.nombre"
                      type="text"
                      maxlength="160"
                      :class="inputClass"
                    />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="plantilla-edit-uso">Uso</label>
                    <select id="plantilla-edit-uso" v-model="form.uso" :class="inputClass">
                      <option value="CONTRATO">Contrato</option>
                      <option value="ADENDA">Adenda</option>
                    </select>
                  </div>
                </div>

                <p class="text-xs leading-relaxed text-slate-500">
                  Tokens fijos: <code class="text-slate-700">{{ TOKEN_EJEMPLOS.nombres }}</code>,
                  <code class="text-slate-700">{{ TOKEN_EJEMPLOS.fecha }}</code>,
                  <code class="text-slate-700">{{ TOKEN_EJEMPLOS.sueldo }}</code>.
                  Extras: <code class="text-slate-700">{{ TOKEN_EJEMPLOS.meta }}</code>.
                </p>

                <div class="grid min-h-[28rem] gap-4 lg:grid-cols-2">
                  <div class="flex min-h-0 flex-col">
                    <label class="text-sm font-medium text-slate-700" for="plantilla-edit-html">HTML</label>
                    <Textarea
                      id="plantilla-edit-html"
                      v-model="form.plantilla"
                      class="mt-1.5 min-h-[24rem] flex-1 font-mono text-xs"
                    />
                  </div>
                  <div class="flex min-h-0 flex-col">
                    <p class="text-sm font-medium text-slate-700">Vista previa</p>
                    <iframe
                      title="Vista previa de la plantilla"
                      class="mt-1.5 min-h-[24rem] w-full flex-1 rounded-lg border border-slate-200 bg-white"
                      :srcdoc="form.plantilla"
                    />
                  </div>
                </div>
              </fieldset>

              <p v-if="error" id="plantilla-edit-form-error" class="mt-4 shrink-0 text-sm font-medium text-red-600" role="alert">
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
                  :aria-describedby="error ? 'plantilla-edit-form-error' : undefined"
                >
                  {{ submitting ? 'Guardando…' : 'Guardar cambios' }}
                </Button>
              </footer>
            </form>
          </section>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
