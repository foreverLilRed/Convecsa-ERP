<script setup lang="ts">
import { XIcon } from '@lucide/vue'
import { onUnmounted, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { crearPlantillaContrato } from '@/services/contrato/contratoService'
import type { UsoPlantilla } from '@/types/contrato'

const HTML_INICIAL = `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"/>
  <title>Contrato - {{trabajador_nombres}} {{trabajador_apellidos}}</title>
  <style>
    body { font-family: sans-serif; color: #111827; padding: 24px; }
  </style>
</head>
<body>
  <h1>Contrato</h1>
  <p>Trabajador: <strong>{{trabajador_nombres}} {{trabajador_apellidos}}</strong> · DNI {{trabajador_dni}}</p>
  <p>Inicio: {{fecha_inicio}} · Fin: {{fecha_fin}} · Duración: {{duracion}}</p>
  <p>Sueldo: S/ {{sueldo}} ({{sueldo_letras}})</p>
</body>
</html>
`

const TOKEN_EJEMPLOS = {
  nombres: '{{trabajador_nombres}}',
  fecha: '{{fecha_inicio}}',
  sueldo: '{{sueldo}}',
  meta: '{{meta.clave}}',
} as const

const props = defineProps<{
  open: boolean
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

function emptyForm(): Formulario {
  return {
    codigo: '',
    nombre: '',
    uso: 'CONTRATO',
    plantilla: HTML_INICIAL,
  }
}

const form = reactive<Formulario>(emptyForm())
const error = ref('')
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
  if (event.key === 'Escape' && props.open && !submitting.value) {
    emit('close')
  }
}

function resetForm() {
  Object.assign(form, emptyForm())
  error.value = ''
  submitting.value = false
}

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

function validate(): string | null {
  if (!form.codigo.trim()) return 'Ingrese el código de la plantilla.'
  if (!/^[A-Za-z0-9_-]+$/.test(form.codigo.trim())) {
    return 'El código solo admite letras, números, guion y guion bajo.'
  }
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

  submitting.value = true
  try {
    await crearPlantillaContrato({
      codigo: form.codigo.trim(),
      nombre: form.nombre.trim(),
      plantilla: form.plantilla,
      uso: form.uso,
    })
    toast.success('Plantilla creada.')
    emit('saved')
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear la plantilla.'
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
            class="modal-panel relative z-10 flex h-[min(92vh,56rem)] w-full max-w-6xl flex-col overflow-hidden rounded-xl border border-palette-cyan/70 bg-white shadow-lg"
            role="dialog"
            aria-modal="true"
            aria-labelledby="plantilla-modal-title"
          >
            <header class="flex shrink-0 items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
              <div>
                <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
                  Gestión Humana / Plantillas de Contrato
                </p>
                <h2 id="plantilla-modal-title" class="mt-1 text-lg font-bold text-palette-navy">
                  Nueva plantilla
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
                    <label class="text-sm font-medium text-slate-700" for="plantilla-codigo">Código</label>
                    <input
                      id="plantilla-codigo"
                      v-model="form.codigo"
                      type="text"
                      maxlength="40"
                      placeholder="CSE"
                      :class="inputClass"
                    />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="plantilla-nombre">Nombre</label>
                    <input
                      id="plantilla-nombre"
                      v-model="form.nombre"
                      type="text"
                      maxlength="160"
                      placeholder="Servicio específico"
                      :class="inputClass"
                    />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="plantilla-uso">Uso</label>
                    <select id="plantilla-uso" v-model="form.uso" :class="inputClass">
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
                    <label class="text-sm font-medium text-slate-700" for="plantilla-html">HTML</label>
                    <Textarea
                      id="plantilla-html"
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

              <p v-if="error" id="plantilla-form-error" class="mt-4 shrink-0 text-sm font-medium text-red-600" role="alert">
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
                  :aria-describedby="error ? 'plantilla-form-error' : undefined"
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
