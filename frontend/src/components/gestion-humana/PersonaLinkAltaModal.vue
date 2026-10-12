<script setup lang="ts">
import { XIcon } from '@lucide/vue'
import { computed, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { crearLinkAlta } from '@/services/persona/personaService'
import {
  DOCUMENTO_NUMERO_REGLAS,
  maxLengthNumeroDocumento,
  mensajeNumeroDocumentoInvalido,
  sanitizarNumeroDocumento,
  TIPOS_DOCUMENTO,
  type TipoDocumento,
} from '@/types/persona'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const form = reactive({
  apellidos: '',
  nombres: '',
  tipoDocumento: 'DNI' as TipoDocumento,
  numeroDocumento: '',
})
const submitting = ref(false)
const error = ref('')

const reglaDocumento = computed(() => DOCUMENTO_NUMERO_REGLAS[form.tipoDocumento])
const maxDocumento = computed(() => maxLengthNumeroDocumento(form.tipoDocumento))

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

function resetForm() {
  form.apellidos = ''
  form.nombres = ''
  form.tipoDocumento = 'DNI'
  form.numeroDocumento = ''
  error.value = ''
  submitting.value = false
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
  () => form.tipoDocumento,
  () => {
    form.numeroDocumento = sanitizarNumeroDocumento(form.tipoDocumento, form.numeroDocumento)
  },
)

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      resetForm()
      lockBodyScroll()
      document.addEventListener('keydown', onDocumentKeydown)
    } else {
      document.removeEventListener('keydown', onDocumentKeydown)
    }
  },
)

function applyNumeroDocumento(target: HTMLInputElement, value: string) {
  const sanitizado = sanitizarNumeroDocumento(form.tipoDocumento, value)
  target.value = sanitizado
  form.numeroDocumento = sanitizado
}

function onNumeroDocumentoBeforeInput(event: InputEvent) {
  if (!event.data) {
    return
  }
  const permitido = reglaDocumento.value.digitsOnly ? /^\d+$/ : /^[A-Za-z0-9]+$/
  if (!permitido.test(event.data)) {
    event.preventDefault()
  }
}

function onNumeroDocumentoInput(event: Event) {
  applyNumeroDocumento(event.target as HTMLInputElement, (event.target as HTMLInputElement).value)
}

function onNumeroDocumentoPaste(event: ClipboardEvent) {
  event.preventDefault()
  const target = event.target as HTMLInputElement
  const pasted = event.clipboardData?.getData('text') ?? ''
  applyNumeroDocumento(target, `${target.value}${pasted}`)
}

function onOverlayClick() {
  if (!submitting.value) {
    emit('close')
  }
}

async function onSubmit() {
  error.value = ''
  if (!form.apellidos.trim()) {
    error.value = 'Ingrese los apellidos.'
    toast.error(error.value)
    return
  }
  if (!form.nombres.trim()) {
    error.value = 'Ingrese los nombres.'
    toast.error(error.value)
    return
  }
  const documentoInvalido = mensajeNumeroDocumentoInvalido(form.tipoDocumento, form.numeroDocumento)
  if (documentoInvalido) {
    error.value = documentoInvalido
    toast.error(documentoInvalido)
    return
  }

  submitting.value = true
  try {
    const link = await crearLinkAlta({
      apellidos: form.apellidos.trim(),
      nombres: form.nombres.trim(),
      tipoDocumento: form.tipoDocumento,
      numeroDocumento: form.numeroDocumento.trim(),
    })
    const url = `${window.location.origin}/invitar/persona/${link.token}`
    try {
      await navigator.clipboard.writeText(url)
      toast.success('Link de alta copiado. Válido 3 días.')
    } catch {
      toast.success(`Link de alta (válido 3 días): ${url}`)
    }
    emit('close')
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'No se pudo crear el link de alta.'
    toast.error(error.value)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
      <div v-if="open" class="modal-root fixed inset-0 z-50 overflow-y-auto">
        <div class="modal-backdrop fixed inset-0 bg-palette-navy/40" @click="onOverlayClick" />
        <div class="relative flex min-h-full items-center justify-center p-4 sm:p-6">
          <section
            class="modal-panel relative z-10 w-full max-w-lg overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
            role="dialog"
            aria-modal="true"
            aria-labelledby="link-alta-title"
          >
            <header class="flex items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
              <div>
                <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                  Gestión Humana / Personal
                </p>
                <h2 id="link-alta-title" class="mt-1 text-lg font-bold text-palette-navy">
                  Link de alta
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

            <form class="space-y-4 px-5 py-5" @submit.prevent="onSubmit">
              <p class="text-sm text-slate-500">
                Estos datos quedan fijos. Quien reciba el enlace completa el resto de la ficha.
              </p>
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
                <input
                  :value="form.numeroDocumento"
                  :class="inputClass"
                  type="text"
                  :inputmode="reglaDocumento.digitsOnly ? 'numeric' : 'text'"
                  :maxlength="maxDocumento"
                  :placeholder="reglaDocumento.hint"
                  autocomplete="off"
                  @beforeinput="onNumeroDocumentoBeforeInput"
                  @input="onNumeroDocumentoInput"
                  @paste="onNumeroDocumentoPaste"
                />
                <span class="mt-1 block text-[11px] font-normal normal-case tracking-normal text-slate-400">
                  {{ reglaDocumento.hint }}
                </span>
              </label>

              <p v-if="error" class="text-sm font-medium text-red-600" role="alert">{{ error }}</p>

              <footer class="flex flex-col-reverse gap-3 border-t border-palette-cyan/70 pt-4 sm:flex-row sm:justify-end">
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
                >
                  {{ submitting ? 'Creando…' : 'Crear y copiar link' }}
                </Button>
              </footer>
            </form>
          </section>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
