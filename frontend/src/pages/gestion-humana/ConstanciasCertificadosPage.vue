<script setup lang="ts">
import { PenLineIcon, XIcon } from '@lucide/vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { toast } from 'vue-sonner'
import {
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogOverlay,
  DialogPortal,
  DialogRoot,
  DialogTitle,
} from 'reka-ui'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { getSession } from '@/services/auth/authService'
import { listarContratos } from '@/services/contrato/contratoService'
import { openAuthenticatedRequest } from '@/services/http'
import {
  guardarConfiguracionDocumento,
  obtenerConfiguracionDocumento,
} from '@/services/nomina/nominaService'
import { esGerencia } from '@/types/auth'
import type { ContratoDTO } from '@/types/contrato'
import type { NominaDocumentoConfiguracion } from '@/types/nomina'
import type { PagedResponse } from '@/types/persona'

const PAGE_SIZE = 15
const MAX_IMAGE_BYTES = 2 * 1024 * 1024
const page = ref(0)
const loading = ref(true)
const openingId = ref<number | null>(null)
const data = ref<PagedResponse<ContratoDTO> | null>(null)
const totalPages = computed(() => data.value?.totalPages ?? 0)
const gerencia = computed(() => esGerencia(getSession()?.rol))

const configOpen = ref(false)
const configLoading = ref(false)
const configSaving = ref(false)
const config = reactive<NominaDocumentoConfiguracion>({
  razonSocial: '',
  ruc: '',
  domicilio: '',
  representante: null,
  logoDataUrl: null,
  firmaDataUrl: null,
})
const representante = ref('')

function fecha(value: string | null): string {
  if (!value) return '—'
  return new Date(`${value}T12:00:00`).toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' })
}

function finContrato(contrato: ContratoDTO): string {
  const fin = contrato.fechaCorte || contrato.fechaFin
  return fin ? fecha(fin) : contrato.activo ? 'Vigente' : '—'
}

function cargo(contrato: ContratoDTO): string {
  return contrato.metadata?.cargo?.trim() || contrato.titulo
}

async function cargar() {
  loading.value = true
  try {
    data.value = await listarContratos(page.value, PAGE_SIZE)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudieron cargar los contratos.')
  } finally {
    loading.value = false
  }
}

async function abrir(contrato: ContratoDTO) {
  openingId.value = contrato.id
  try {
    await openAuthenticatedRequest(`/api/gestion-humana/contratos/${contrato.id}/constancia-trabajo`)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo generar la constancia.')
  } finally {
    openingId.value = null
  }
}

function cambiarPagina(direccion: number) {
  page.value += direccion
  void cargar()
}

function assignConfig(value: NominaDocumentoConfiguracion) {
  Object.assign(config, value)
  representante.value = value.representante ?? ''
}

async function cargarConfig() {
  configLoading.value = true
  try {
    assignConfig(await obtenerConfiguracionDocumento())
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo cargar la firma del administrador.')
    configOpen.value = false
  } finally {
    configLoading.value = false
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

async function onFirma(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    config.firmaDataUrl = await readImage(file)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'Imagen inválida.')
    input.value = ''
  }
}

async function guardarFirma() {
  configSaving.value = true
  try {
    assignConfig(
      await guardarConfiguracionDocumento({
        ...config,
        representante: representante.value.trim() || null,
      }),
    )
    toast.success('Firma del administrador guardada.')
    configOpen.value = false
  } catch (error) {
    toast.error(error instanceof Error ? error.message : 'No se pudo guardar la firma.')
  } finally {
    configSaving.value = false
  }
}

watch(configOpen, (abierto) => {
  if (abierto) {
    void cargarConfig()
  }
})

onMounted(() => void cargar())
</script>

<template>
  <section class="min-w-0 space-y-3">
    <header>
      <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
        Constancias y certificados
      </h2>
      <p class="mt-2 max-w-2xl text-sm leading-relaxed text-slate-500">
        Genere una constancia de trabajo con los datos de la persona y su contrato. El cargo, proyecto y período se completan automáticamente.
      </p>
    </header>

    <div class="rounded-lg border border-palette-cyan/70 bg-white">
      <div class="flex flex-wrap items-start justify-between gap-3 border-b border-slate-100 px-4 py-4 sm:px-5">
        <div>
          <h3 class="text-base font-semibold text-palette-navy">Constancia de trabajo</h3>
          <p class="mt-1 text-sm text-slate-500">
            Elija el contrato correspondiente. El documento se abrirá listo para imprimir o guardar como PDF.
          </p>
        </div>
        <Button
          v-if="gerencia"
          variant="outline"
          class="h-9 bg-white"
          type="button"
          @click="configOpen = true"
        >
          <PenLineIcon data-icon="inline-start" class="size-4" />
          Configurar firma
        </Button>
      </div>

      <div v-if="loading" class="space-y-3 p-5">
        <Skeleton v-for="n in 4" :key="n" class="h-14 w-full" />
      </div>

      <div v-else class="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Trabajador</TableHead>
              <TableHead>Cargo y plantilla</TableHead>
              <TableHead>Período</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead class="text-right">Documento</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!data?.content.length" :colspan="5">
              No hay contratos para generar constancias.
            </TableEmpty>
            <TableRow v-for="contrato in data?.content" :key="contrato.id">
              <TableCell>
                <p class="font-semibold text-slate-900">{{ contrato.personaNombreCompleto }}</p>
                <p class="mt-1 text-xs text-slate-500">{{ contrato.personaNumeroDocumento }}</p>
              </TableCell>
              <TableCell>
                <p class="font-medium text-slate-800">{{ cargo(contrato) }}</p>
                <p class="mt-1 text-xs text-slate-500">
                  {{ contrato.tipoPlantillaNombre || contrato.categoriaContratoNombre }}
                </p>
              </TableCell>
              <TableCell class="text-sm text-slate-600">
                {{ fecha(contrato.fechaInicio) }} – {{ finContrato(contrato) }}
              </TableCell>
              <TableCell>
                <Badge variant="secondary">{{ contrato.activo ? 'Activo' : 'Finalizado' }}</Badge>
              </TableCell>
              <TableCell class="text-right">
                <Button
                  size="sm"
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="button"
                  :disabled="openingId === contrato.id"
                  @click="abrir(contrato)"
                >
                  {{ openingId === contrato.id ? 'Generando…' : 'Generar constancia' }}
                </Button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>

      <footer
        v-if="totalPages > 1"
        class="flex flex-col gap-2 border-t border-slate-100 px-4 py-3 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between"
      >
        <span>Página {{ page + 1 }} de {{ totalPages }}</span>
        <div class="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            type="button"
            :disabled="page === 0 || loading"
            @click="cambiarPagina(-1)"
          >
            Anterior
          </Button>
          <Button
            variant="outline"
            size="sm"
            type="button"
            :disabled="page + 1 >= totalPages || loading"
            @click="cambiarPagina(1)"
          >
            Siguiente
          </Button>
        </div>
      </footer>
    </div>

    <DialogRoot v-model:open="configOpen">
      <DialogPortal>
        <DialogOverlay
          class="fixed inset-0 z-[90] bg-palette-navy/45 backdrop-blur-[1px] data-[state=closed]:animate-out data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0"
        />
        <DialogContent
          class="fixed top-1/2 left-1/2 z-[100] flex max-h-[calc(100vh-2rem)] w-[calc(100%-2rem)] max-w-lg -translate-x-1/2 -translate-y-1/2 flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl data-[state=closed]:animate-out data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95"
        >
          <header class="flex items-start justify-between gap-4 border-b border-slate-200 px-5 py-4">
            <div>
              <DialogTitle class="text-lg font-semibold text-palette-navy">
                Firma del administrador
              </DialogTitle>
              <DialogDescription class="mt-1 text-sm leading-6 text-slate-600">
                El nombre y la firma se imprimen al pie de cada constancia de trabajo.
              </DialogDescription>
            </div>
            <DialogClose as-child>
              <Button variant="ghost" size="icon" class="shrink-0" type="button" aria-label="Cerrar">
                <XIcon />
              </Button>
            </DialogClose>
          </header>

          <div class="overflow-y-auto px-5 py-4">
            <div v-if="configLoading" class="space-y-3">
              <Skeleton class="h-10 w-full" />
              <Skeleton class="h-32 w-full" />
            </div>
            <form v-else class="space-y-4" @submit.prevent="guardarFirma">
              <label class="block text-sm font-medium text-slate-700" for="constancia-representante">
                Representante
                <Input
                  id="constancia-representante"
                  v-model="representante"
                  class="mt-1.5"
                  maxlength="200"
                  autocomplete="name"
                  :disabled="configSaving"
                />
              </label>
              <div>
                <p class="text-sm font-medium text-slate-700">Firma digitalizada</p>
                <p class="mt-1 text-xs text-slate-500">PNG o JPG, máximo 2 MB. Fondo blanco o transparente.</p>
                <div class="mt-3 flex h-32 items-center justify-center rounded-lg border border-dashed border-slate-300 bg-slate-50 p-3">
                  <img
                    v-if="config.firmaDataUrl"
                    :src="config.firmaDataUrl"
                    alt="Vista previa de la firma"
                    class="max-h-full max-w-full object-contain"
                  />
                  <span v-else class="text-sm text-slate-400">Sin firma</span>
                </div>
                <div class="mt-3 flex flex-wrap gap-2">
                  <label class="cursor-pointer rounded-md border border-slate-200 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
                    Seleccionar firma
                    <input
                      type="file"
                      accept="image/png,image/jpeg"
                      class="sr-only"
                      :disabled="configSaving"
                      @change="onFirma"
                    />
                  </label>
                  <Button
                    v-if="config.firmaDataUrl"
                    variant="outline"
                    type="button"
                    :disabled="configSaving"
                    @click="config.firmaDataUrl = null"
                  >
                    Quitar
                  </Button>
                </div>
              </div>
              <div class="flex justify-end gap-2 border-t border-slate-100 pt-4">
                <DialogClose as-child>
                  <Button variant="outline" type="button" :disabled="configSaving">Cancelar</Button>
                </DialogClose>
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="configSaving || configLoading"
                >
                  {{ configSaving ? 'Guardando…' : 'Guardar' }}
                </Button>
              </div>
            </form>
          </div>
        </DialogContent>
      </DialogPortal>
    </DialogRoot>
  </section>
</template>
