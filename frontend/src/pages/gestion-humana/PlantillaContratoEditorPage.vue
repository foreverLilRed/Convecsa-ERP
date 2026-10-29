<script setup lang="ts">
import { ArrowLeftIcon, FileCode2Icon, LockIcon, PlusIcon, SaveIcon } from '@lucide/vue'
import { computed, nextTick, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import {
  actualizarPlantillaContrato,
  crearPlantillaContrato,
  obtenerPlantillaContrato,
} from '@/services/contrato/contratoService'
import { ApiError } from '@/services/http'
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

type TokenLeyenda = {
  label: string
  token: string
}

const TOKENS_PERSONA: TokenLeyenda[] = [
  { label: 'ID de persona', token: '{{trabajador_id}}' },
  { label: 'Nombres', token: '{{trabajador_nombres}}' },
  { label: 'Apellidos', token: '{{trabajador_apellidos}}' },
  { label: 'Nombre completo', token: '{{trabajador_nombre_completo}}' },
  { label: 'Apellido paterno', token: '{{trabajador_apellido_paterno}}' },
  { label: 'Apellido materno', token: '{{trabajador_apellido_materno}}' },
  { label: 'Tipo de documento', token: '{{trabajador_tipo_documento}}' },
  { label: 'Número de documento', token: '{{trabajador_numero_documento}}' },
  { label: 'Fecha de nacimiento', token: '{{trabajador_fecha_nacimiento}}' },
  { label: 'Emisión del documento', token: '{{trabajador_fecha_emision_documento}}' },
  { label: 'Caducidad del documento', token: '{{trabajador_fecha_caducidad_documento}}' },
  { label: 'Correo', token: '{{trabajador_correo}}' },
  { label: 'Teléfono', token: '{{trabajador_telefono}}' },
  { label: 'Dirección', token: '{{trabajador_direccion}}' },
  { label: 'Departamento', token: '{{trabajador_departamento}}' },
  { label: 'Provincia', token: '{{trabajador_provincia}}' },
  { label: 'Distrito', token: '{{trabajador_distrito}}' },
  { label: 'Género', token: '{{trabajador_genero}}' },
  { label: 'Grado de instrucción', token: '{{trabajador_grado_instruccion}}' },
  { label: 'Sistema de pensión', token: '{{trabajador_pension}}' },
  { label: 'CUSPP', token: '{{trabajador_cuspp}}' },
  { label: 'Tipo de comisión AFP', token: '{{trabajador_tipo_comision_afp}}' },
  { label: 'Banco principal', token: '{{trabajador_banco}}' },
  { label: 'Número de cuenta', token: '{{trabajador_numero_cuenta}}' },
  { label: 'CCI', token: '{{trabajador_cci}}' },
  { label: 'Contacto de emergencia', token: '{{trabajador_contacto_emergencia_nombre}}' },
  { label: 'Teléfono de emergencia', token: '{{trabajador_contacto_emergencia_telefono}}' },
  { label: 'Parentesco de emergencia', token: '{{trabajador_contacto_emergencia_parentesco}}' },
  { label: 'Estado activo', token: '{{trabajador_activo}}' },
  { label: 'URL de fotografía', token: '{{trabajador_foto_url}}' },
  { label: 'Vigencia del examen médico', token: '{{trabajador_fecha_vigencia_emo}}' },
  { label: 'Aptitud médica', token: '{{trabajador_aptitud_medica}}' },
  { label: 'Restricciones médicas', token: '{{trabajador_restricciones_medicas}}' },
]

const TOKENS_CONTRATO: TokenLeyenda[] = [
  { label: 'ID del contrato', token: '{{contrato_id}}' },
  { label: 'Título', token: '{{titulo}}' },
  { label: 'Categoría', token: '{{categoria_contrato}}' },
  { label: 'Código de categoría', token: '{{categoria_contrato_codigo}}' },
  { label: 'Régimen laboral', token: '{{regimen_laboral}}' },
  { label: 'Código de régimen', token: '{{regimen_laboral_codigo}}' },
  { label: 'Modalidad', token: '{{modalidad_contrato}}' },
  { label: 'Sueldo', token: '{{sueldo}}' },
  { label: 'Sueldo en letras', token: '{{sueldo_letras}}' },
  { label: 'Estado activo', token: '{{activo}}' },
  { label: 'Observaciones', token: '{{observaciones}}' },
  { label: 'Nombre de plantilla', token: '{{plantilla_nombre}}' },
  { label: 'Código de plantilla', token: '{{plantilla_codigo}}' },
]

const TOKENS_FECHAS: TokenLeyenda[] = [
  { label: 'Fecha de inicio', token: '{{fecha_inicio}}' },
  { label: 'Fecha de fin', token: '{{fecha_fin}}' },
  { label: 'Duración', token: '{{duracion}}' },
  { label: 'Fecha de corte', token: '{{fecha_corte}}' },
  { label: 'Fecha de firma', token: '{{fecha_firma}}' },
]

type Formulario = {
  codigo: string
  nombre: string
  uso: UsoPlantilla
  plantilla: string
}

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.name === 'gh-plantilla-contrato-editar')
const plantillaId = computed(() => Number(route.params.id))
const step = ref<'pin' | 'edit'>(isEdit.value ? 'pin' : 'edit')
const pin = ref('')
const error = ref('')
const loading = ref(false)
const submitting = ref(false)
const plantillaCargada = ref(false)
const metaClave = ref('')
const META_EJEMPLO = '{{meta.clave}}'
const form = reactive<Formulario>({
  codigo: '',
  nombre: '',
  uso: 'CONTRATO',
  plantilla: HTML_INICIAL,
})

const inputClass =
  'mt-1.5 block h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500'

const metaDetectados = computed(() => {
  const encontrados = new Set<string>()
  for (const match of form.plantilla.matchAll(/\{\{meta\.([a-zA-Z0-9_]+)\}\}/g)) {
    if (match[1]) encontrados.add(match[1])
  }
  return [...encontrados]
})

function metaToken(clave: string) {
  return `{{meta.${clave}}}`
}

async function insertToken(token: string) {
  const editor = document.getElementById('plantilla-editor-html') as HTMLTextAreaElement | null
  const start = editor?.selectionStart ?? form.plantilla.length
  const end = editor?.selectionEnd ?? start
  form.plantilla = `${form.plantilla.slice(0, start)}${token}${form.plantilla.slice(end)}`
  await nextTick()
  editor?.focus()
  editor?.setSelectionRange(start + token.length, start + token.length)
}

function addMeta() {
  const clave = metaClave.value.trim().toLowerCase().replace(/\s+/g, '_')
  if (!clave) {
    toast.error('Ingrese la clave del campo personalizado.')
    return
  }
  if (!/^[a-z0-9_]+$/.test(clave)) {
    toast.error('La clave solo admite letras sin tilde, números y guion bajo.')
    return
  }
  void insertToken(metaToken(clave))
  metaClave.value = ''
}

function closeWindow() {
  window.close()
  window.setTimeout(() => {
    void router.push({ name: 'gh-plantillas-contrato' })
  }, 120)
}

async function onUnlock() {
  error.value = ''
  if (!pin.value.trim()) {
    error.value = 'Ingrese el PIN de superusuario.'
    return
  }
  if (!Number.isInteger(plantillaId.value) || plantillaId.value < 1) {
    error.value = 'La plantilla indicada no es válida.'
    return
  }

  loading.value = true
  try {
    if (!plantillaCargada.value) {
      const detalle = await obtenerPlantillaContrato(plantillaId.value)
      form.codigo = detalle.codigo
      form.nombre = detalle.nombre
      form.uso = detalle.uso
      form.plantilla = detalle.plantilla
      plantillaCargada.value = true
    }
    step.value = 'edit'
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo cargar la plantilla.'
    error.value = message
    toast.error(message)
  } finally {
    loading.value = false
  }
}

function validate(): string | null {
  if (!isEdit.value && !form.codigo.trim()) return 'Ingrese el código de la plantilla.'
  if (!isEdit.value && !/^[A-Za-z0-9_-]+$/.test(form.codigo.trim())) {
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
    if (isEdit.value) {
      await actualizarPlantillaContrato(plantillaId.value, {
        nombre: form.nombre.trim(),
        plantilla: form.plantilla,
        uso: form.uso,
        pin: pin.value.trim(),
      })
      toast.success('Plantilla actualizada.')
    } else {
      await crearPlantillaContrato({
        codigo: form.codigo.trim(),
        nombre: form.nombre.trim(),
        plantilla: form.plantilla,
        uso: form.uso,
      })
      toast.success('Plantilla creada.')
    }
    closeWindow()
  } catch (err) {
    if (err instanceof ApiError && err.status === 403 && isEdit.value) {
      step.value = 'pin'
      pin.value = ''
      error.value = 'PIN de superusuario incorrecto.'
      toast.error(error.value)
      return
    }
    const message = err instanceof Error ? err.message : 'No se pudo guardar la plantilla.'
    error.value = message
    toast.error(message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="min-w-0 pb-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div class="min-w-0">
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">
          Gestión Humana / Plantillas de Contrato
        </p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          {{ isEdit ? 'Editar plantilla' : 'Nueva plantilla' }}
        </h2>
        <p class="mt-3 max-w-3xl text-sm leading-relaxed text-slate-500">
          Configure la plantilla HTML y compruebe el resultado en la vista previa antes de guardarla.
        </p>
      </div>
      <Button variant="outline" class="h-10 w-full px-4 sm:w-auto" type="button" @click="closeWindow">
        <ArrowLeftIcon data-icon="inline-start" />
        Cerrar pestaña
      </Button>
    </div>

    <div
      v-if="step === 'pin'"
      class="mx-auto mt-8 max-w-md rounded-2xl border border-palette-cyan/70 bg-white p-6 shadow-sm"
    >
      <div class="flex size-11 items-center justify-center rounded-xl bg-sky-50 text-palette-royal">
        <LockIcon class="size-5" />
      </div>
      <h3 class="mt-4 text-lg font-bold text-palette-navy">Edición restringida</h3>
      <p class="mt-2 text-sm leading-relaxed text-slate-500">
        Ingrese el PIN de superusuario para cargar y editar esta plantilla.
      </p>
      <form class="mt-5 space-y-4" @submit.prevent="onUnlock">
        <div>
          <label class="text-sm font-medium text-slate-700" for="plantilla-editor-pin">PIN</label>
          <input
            id="plantilla-editor-pin"
            v-model="pin"
            type="password"
            inputmode="numeric"
            autocomplete="off"
            maxlength="12"
            placeholder="••••"
            :class="inputClass"
            :disabled="loading"
            autofocus
          />
        </div>
        <p v-if="error" class="text-sm font-medium text-red-600" role="alert">{{ error }}</p>
        <Button class="h-10 w-full bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="loading">
          <LockIcon v-if="!loading" data-icon="inline-start" />
          {{ loading ? 'Cargando…' : 'Desbloquear edición' }}
        </Button>
      </form>
    </div>

    <form v-else class="mt-3 space-y-2.5" @submit.prevent="onSubmit">
      <fieldset class="space-y-2.5" :disabled="submitting">
        <div class="rounded-2xl border border-palette-cyan/70 bg-white p-4 shadow-sm sm:p-6">
          <div class="flex items-center gap-3">
            <div class="flex size-10 shrink-0 items-center justify-center rounded-xl bg-sky-50 text-palette-royal">
              <FileCode2Icon class="size-5" />
            </div>
            <div>
              <h3 class="font-semibold text-palette-navy">Datos de la plantilla</h3>
              <p class="text-sm text-slate-500">Información utilizada para identificarla dentro del ERP.</p>
            </div>
          </div>

          <div class="mt-5 grid gap-2 lg:grid-cols-3">
            <div>
              <label class="text-sm font-medium text-slate-700" for="plantilla-editor-codigo">Código</label>
              <input
                id="plantilla-editor-codigo"
                v-model="form.codigo"
                type="text"
                maxlength="40"
                placeholder="CONTRATO_PLAZO_FIJO"
                :class="inputClass"
                :disabled="isEdit"
              />
            </div>
            <div>
              <label class="text-sm font-medium text-slate-700" for="plantilla-editor-nombre">Nombre</label>
              <input
                id="plantilla-editor-nombre"
                v-model="form.nombre"
                type="text"
                maxlength="160"
                placeholder="Contrato a plazo fijo"
                :class="inputClass"
              />
            </div>
            <div>
              <label class="text-sm font-medium text-slate-700" for="plantilla-editor-uso">Uso</label>
              <select id="plantilla-editor-uso" v-model="form.uso" :class="inputClass">
                <option value="CONTRATO">Contrato</option>
                <option value="ADENDA">Adenda</option>
              </select>
            </div>
          </div>
        </div>

        <div class="rounded-2xl border border-palette-cyan/70 bg-white p-4 shadow-sm sm:p-6">
          <div>
            <h3 class="font-semibold text-palette-navy">Leyenda de variables disponibles</h3>
            <p class="mt-1 text-sm leading-relaxed text-slate-500">
              Haga clic en cualquier variable para insertarla en la posición actual del editor HTML.
              Al generar el documento se reemplazará con la información del contrato y de la persona asociada.
            </p>
          </div>

          <div class="mt-5 grid gap-2 xl:grid-cols-3">
            <details class="group rounded-xl border border-slate-200 bg-slate-50/60 p-4" open>
              <summary class="cursor-pointer list-none font-semibold text-palette-navy">
                Datos de la persona
                <span class="ml-1 text-xs font-normal text-slate-500">({{ TOKENS_PERSONA.length }})</span>
              </summary>
              <p class="mt-1 text-xs leading-relaxed text-slate-500">
                Se obtienen automáticamente de la ficha de la persona seleccionada en el contrato.
              </p>
              <div class="mt-3 max-h-72 space-y-1.5 overflow-y-auto pr-1">
                <button
                  v-for="item in TOKENS_PERSONA"
                  :key="item.token"
                  type="button"
                  class="flex w-full items-center justify-between gap-3 rounded-lg border border-transparent px-2.5 py-2 text-left transition hover:border-sky-200 hover:bg-white"
                  :title="`Insertar ${item.token}`"
                  @click="insertToken(item.token)"
                >
                  <span class="text-xs text-slate-600">{{ item.label }}</span>
                  <code class="max-w-[58%] truncate text-[11px] text-sky-700">{{ item.token }}</code>
                </button>
              </div>
            </details>

            <details class="group rounded-xl border border-slate-200 bg-slate-50/60 p-4" open>
              <summary class="cursor-pointer list-none font-semibold text-palette-navy">
                Datos generales del contrato
                <span class="ml-1 text-xs font-normal text-slate-500">({{ TOKENS_CONTRATO.length }})</span>
              </summary>
              <p class="mt-1 text-xs leading-relaxed text-slate-500">
                Incluye título, categoría, sueldo, estado, observaciones y la plantilla utilizada.
              </p>
              <div class="mt-3 max-h-72 space-y-1.5 overflow-y-auto pr-1">
                <button
                  v-for="item in TOKENS_CONTRATO"
                  :key="item.token"
                  type="button"
                  class="flex w-full items-center justify-between gap-3 rounded-lg border border-transparent px-2.5 py-2 text-left transition hover:border-sky-200 hover:bg-white"
                  :title="`Insertar ${item.token}`"
                  @click="insertToken(item.token)"
                >
                  <span class="text-xs text-slate-600">{{ item.label }}</span>
                  <code class="max-w-[58%] truncate text-[11px] text-sky-700">{{ item.token }}</code>
                </button>
              </div>
            </details>

            <details class="group rounded-xl border border-slate-200 bg-slate-50/60 p-4" open>
              <summary class="cursor-pointer list-none font-semibold text-palette-navy">
                Fechas y vigencia
                <span class="ml-1 text-xs font-normal text-slate-500">({{ TOKENS_FECHAS.length }})</span>
              </summary>
              <p class="mt-1 text-xs leading-relaxed text-slate-500">
                Las fechas de inicio y fin son opcionales y se completan al registrar cada contrato.
              </p>
              <div class="mt-3 max-h-72 space-y-1.5 overflow-y-auto pr-1">
                <button
                  v-for="item in TOKENS_FECHAS"
                  :key="item.token"
                  type="button"
                  class="flex w-full items-center justify-between gap-3 rounded-lg border border-transparent px-2.5 py-2 text-left transition hover:border-sky-200 hover:bg-white"
                  :title="`Insertar ${item.token}`"
                  @click="insertToken(item.token)"
                >
                  <span class="text-xs text-slate-600">{{ item.label }}</span>
                  <code class="max-w-[58%] truncate text-[11px] text-sky-700">{{ item.token }}</code>
                </button>
              </div>
            </details>
          </div>

          <div class="mt-4 rounded-xl border border-dashed border-sky-200 bg-sky-50/50 p-4">
            <div class="grid gap-2 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
              <div>
                <label class="text-sm font-semibold text-palette-navy" for="plantilla-meta-clave">
                  Campo personalizado (metadata)
                </label>
                <p class="mt-1 text-xs leading-relaxed text-slate-500">
                  Escriba una clave propia. El formulario del contrato solicitará este dato y el documento lo usará como
                  <code class="text-sky-700">{{ META_EJEMPLO }}</code>.
                </p>
                <input
                  id="plantilla-meta-clave"
                  v-model="metaClave"
                  type="text"
                  maxlength="80"
                  placeholder="Ej. cargo_especifico"
                  :class="inputClass"
                  @keydown.enter.prevent="addMeta"
                />
              </div>
              <Button variant="outline" class="h-10 w-full bg-white px-4 lg:w-auto" type="button" @click="addMeta">
                <PlusIcon data-icon="inline-start" />
                Insertar campo
              </Button>
            </div>

            <div v-if="metaDetectados.length" class="mt-4 border-t border-sky-100 pt-3">
              <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Campos personalizados detectados
              </p>
              <div class="mt-2 flex flex-wrap gap-2">
                <button
                  v-for="clave in metaDetectados"
                  :key="clave"
                  type="button"
                  class="rounded-full border border-sky-200 bg-white px-3 py-1.5 font-mono text-xs text-sky-700 hover:bg-sky-100"
                  :title="'Insertar ' + metaToken(clave)"
                  @click="insertToken(metaToken(clave))"
                >
                  {{ metaToken(clave) }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="rounded-2xl border border-palette-cyan/70 bg-white p-4 shadow-sm sm:p-6">
          <div>
            <div>
              <h3 class="font-semibold text-palette-navy">Contenido y vista previa</h3>
              <p class="mt-1 text-sm text-slate-500">Los cambios en el HTML se reflejan inmediatamente.</p>
            </div>
          </div>

          <div class="mt-5 grid min-h-[38rem] gap-5 xl:grid-cols-2">
            <div class="flex min-h-[32rem] min-w-0 flex-col">
              <label class="text-sm font-medium text-slate-700" for="plantilla-editor-html">HTML</label>
              <Textarea
                id="plantilla-editor-html"
                v-model="form.plantilla"
                class="mt-1.5 min-h-[32rem] flex-1 resize-y font-mono text-xs leading-relaxed"
                spellcheck="false"
              />
            </div>
            <div class="flex min-h-[32rem] min-w-0 flex-col">
              <p class="text-sm font-medium text-slate-700">Vista previa</p>
              <iframe
                title="Vista previa de la plantilla"
                class="mt-1.5 min-h-[32rem] w-full flex-1 rounded-xl border border-slate-200 bg-white"
                sandbox="allow-same-origin"
                :srcdoc="form.plantilla"
              />
            </div>
          </div>
        </div>
      </fieldset>

      <p v-if="error" id="plantilla-editor-error" class="text-sm font-medium text-red-600" role="alert">
        {{ error }}
      </p>

      <footer class="sticky bottom-3 z-10 flex flex-col-reverse gap-3 rounded-xl border border-slate-200 bg-white/95 p-3 shadow-lg backdrop-blur sm:flex-row sm:justify-end">
        <Button variant="outline" class="h-10 w-full px-4 sm:w-auto" type="button" :disabled="submitting" @click="closeWindow">
          Cancelar
        </Button>
        <Button
          class="h-10 w-full bg-palette-navy px-5 text-white hover:bg-palette-royal sm:w-auto"
          type="submit"
          :disabled="submitting"
          :aria-describedby="error ? 'plantilla-editor-error' : undefined"
        >
          <SaveIcon v-if="!submitting" data-icon="inline-start" />
          {{ submitting ? 'Guardando…' : isEdit ? 'Guardar cambios' : 'Crear plantilla' }}
        </Button>
      </footer>
    </form>
  </section>
</template>
