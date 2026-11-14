<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  guardarConfiguracionDocumento,
  obtenerConfiguracionDocumento,
} from '@/services/nomina/nominaService'
import type { NominaDocumentoConfiguracion } from '@/types/nomina'

const MAX_IMAGE_BYTES = 2 * 1024 * 1024
const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const loading = ref(true)
const saving = ref(false)
const form = reactive<NominaDocumentoConfiguracion>({
  razonSocial: '',
  ruc: '',
  domicilio: '',
  representante: null,
  logoDataUrl: null,
  firmaDataUrl: null,
})

function assign(value: NominaDocumentoConfiguracion) {
  Object.assign(form, value)
}

async function load() {
  loading.value = true
  try {
    assign(await obtenerConfiguracionDocumento())
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la configuración.')
  } finally {
    loading.value = false
  }
}

function readImage(file: File): Promise<string> {
  if (!['image/png', 'image/jpeg'].includes(file.type)) {
    return Promise.reject(new Error('Seleccione una imagen PNG o JPG.'))
  }
  if (file.size > MAX_IMAGE_BYTES) {
    return Promise.reject(new Error('La imagen no debe superar 2 MB.'))
  }
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(new Error('No se pudo leer la imagen.'))
    reader.readAsDataURL(file)
  })
}

async function onImage(event: Event, target: 'logoDataUrl' | 'firmaDataUrl') {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    form[target] = await readImage(file)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'Imagen inválida.')
    input.value = ''
  }
}

async function save() {
  saving.value = true
  try {
    assign(await guardarConfiguracionDocumento({ ...form }))
    toast.success('Datos corporativos guardados.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la configuración.')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="w-full min-w-0">
    <div class="w-full min-w-0">
      <h1 class="text-2xl font-bold text-palette-navy">Datos corporativos de boletas</h1>
      <p class="mt-2 max-w-3xl text-sm text-slate-500">
        Estos datos, el logo y la firma se incorporan en las boletas PDF.
      </p>

      <div v-if="loading" class="mt-3 space-y-4 rounded-xl border border-palette-cyan/70 bg-white p-6">
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-44 w-full" />
      </div>

      <form
        v-else
        class="mt-3 rounded-xl border border-palette-cyan/70 bg-white p-5 sm:p-6"
        @submit.prevent="save"
      >
        <fieldset :disabled="saving" class="space-y-2.5">
          <div class="grid gap-2 sm:grid-cols-2">
            <div>
              <label for="corp-razon" class="text-sm font-medium text-slate-700">Razón social</label>
              <input id="corp-razon" v-model.trim="form.razonSocial" required maxlength="200" :class="inputClass" />
            </div>
            <div>
              <label for="corp-ruc" class="text-sm font-medium text-slate-700">RUC</label>
              <input id="corp-ruc" v-model.trim="form.ruc" required maxlength="20" :class="inputClass" />
            </div>
          </div>
          <div>
            <label for="corp-domicilio" class="text-sm font-medium text-slate-700">Domicilio fiscal</label>
            <input id="corp-domicilio" v-model.trim="form.domicilio" required maxlength="300" :class="inputClass" />
          </div>
          <div>
            <label for="corp-representante" class="text-sm font-medium text-slate-700">Representante legal</label>
            <input id="corp-representante" v-model.trim="form.representante" maxlength="200" :class="inputClass" />
          </div>

          <div class="grid gap-2.5 border-t border-slate-100 pt-5 sm:grid-cols-2">
            <article>
              <h2 class="text-sm font-semibold text-palette-navy">Logo de la empresa</h2>
              <p class="mt-1 text-xs text-slate-500">PNG o JPG, máximo 2 MB.</p>
              <div class="mt-3 flex h-32 items-center justify-center rounded-lg border border-dashed border-slate-300 bg-slate-50 p-3">
                <img v-if="form.logoDataUrl" :src="form.logoDataUrl" alt="Vista previa del logo" class="max-h-full max-w-full object-contain" />
                <span v-else class="text-sm text-slate-400">Sin logo</span>
              </div>
              <div class="mt-3 flex flex-wrap gap-2">
                <label class="cursor-pointer rounded-md border border-slate-200 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
                  Seleccionar logo
                  <input type="file" accept="image/png,image/jpeg" class="sr-only" @change="onImage($event, 'logoDataUrl')" />
                </label>
                <Button v-if="form.logoDataUrl" variant="outline" type="button" @click="form.logoDataUrl = null">Quitar</Button>
              </div>
            </article>

            <article>
              <h2 class="text-sm font-semibold text-palette-navy">Firma digitalizada</h2>
              <p class="mt-1 text-xs text-slate-500">Use una imagen con fondo blanco o transparente.</p>
              <div class="mt-3 flex h-32 items-center justify-center rounded-lg border border-dashed border-slate-300 bg-slate-50 p-3">
                <img v-if="form.firmaDataUrl" :src="form.firmaDataUrl" alt="Vista previa de la firma" class="max-h-full max-w-full object-contain" />
                <span v-else class="text-sm text-slate-400">Sin firma</span>
              </div>
              <div class="mt-3 flex flex-wrap gap-2">
                <label class="cursor-pointer rounded-md border border-slate-200 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
                  Seleccionar firma
                  <input type="file" accept="image/png,image/jpeg" class="sr-only" @change="onImage($event, 'firmaDataUrl')" />
                </label>
                <Button v-if="form.firmaDataUrl" variant="outline" type="button" @click="form.firmaDataUrl = null">Quitar</Button>
              </div>
            </article>
          </div>
        </fieldset>

        <div class="mt-3 flex justify-end border-t border-slate-100 pt-5">
          <Button class="h-10 bg-palette-navy px-5 text-white hover:bg-palette-royal" type="submit" :disabled="saving">
            {{ saving ? 'Guardando…' : 'Guardar configuración' }}
          </Button>
        </div>
      </form>
    </div>
  </section>
</template>
