<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  descargarDocumentoObra,
  eliminarDocumentoObra,
  listarDocumentosObra,
  subirDocumentoObra,
} from '@/services/construccion/construccionService'
import type { DocumentoObra, TipoDocumentoObra } from '@/types/construccion'

const selectClass =
  'h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-sm text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-500/30'

const inputClass =
  'h-8 rounded-lg border-slate-200 bg-white px-2 text-sm text-slate-900 placeholder-slate-400 focus-visible:border-sky-600 focus-visible:ring-sky-500/20'

const TIPOS: { valor: TipoDocumentoObra; label: string }[] = [
  { valor: 'CONTRATO', label: 'Contrato' },
  { valor: 'PLANO', label: 'Plano' },
  { valor: 'ACTA', label: 'Acta' },
  { valor: 'VALORIZACION', label: 'Valorización' },
  { valor: 'CESION_FACTORING', label: 'Cesión de factoring' },
]

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const loading = ref(true)
const saving = ref(false)
const documentos = ref<DocumentoObra[]>([])
const fileInput = ref<HTMLInputElement | null>(null)

const form = reactive({
  tipo: 'CONTRATO' as TipoDocumentoObra,
  observaciones: '',
})

function tipoLabel(tipo: TipoDocumentoObra): string {
  return TIPOS.find((item) => item.valor === tipo)?.label ?? tipo
}

function tamanoLabel(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function fechaLabel(valor: string): string {
  const fecha = new Date(valor)
  if (Number.isNaN(fecha.getTime())) return valor
  return fecha.toLocaleString('es-PE')
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    documentos.value = await listarDocumentosObra(proyectoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los documentos.')
  } finally {
    loading.value = false
  }
}

async function onSubir() {
  const archivo = fileInput.value?.files?.[0]
  if (!archivo) {
    toast.error('Seleccione un archivo PDF, JPEG o PNG.')
    return
  }
  saving.value = true
  try {
    await subirDocumentoObra(proyectoId.value, form.tipo, archivo, form.observaciones)
    form.observaciones = ''
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    toast.success('Documento cargado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo subir el documento.')
  } finally {
    saving.value = false
  }
}

async function onDescargar(item: DocumentoObra) {
  try {
    await descargarDocumentoObra(proyectoId.value, item.id, item.nombreOriginal)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo descargar el documento.')
  }
}

async function onEliminar(item: DocumentoObra) {
  if (!window.confirm(`¿Eliminar ${item.nombreOriginal}?`)) {
    return
  }
  saving.value = true
  try {
    await eliminarDocumentoObra(proyectoId.value, item.id)
    toast.success('Documento eliminado.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo eliminar el documento.')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="grid gap-3 sm:grid-cols-4">
        <select v-model="form.tipo" :class="selectClass">
          <option v-for="item in TIPOS" :key="item.valor" :value="item.valor">{{ item.label }}</option>
        </select>
        <input
          ref="fileInput"
          class="h-8 w-full rounded-lg border border-slate-200 bg-white px-2 text-sm file:mr-2 file:rounded file:border-0 file:bg-slate-100 file:px-2 file:py-1 file:text-xs"
          type="file"
          accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
        >
        <Input v-model="form.observaciones" :class="inputClass" maxlength="400" placeholder="Observaciones" />
        <Button
          class="bg-palette-navy text-white hover:bg-palette-royal"
          type="button"
          :disabled="saving"
          @click="onSubir"
        >
          Subir
        </Button>
      </div>
    </div>

    <div v-if="loading" class="space-y-3">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <div v-else class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Tipo</TableHead>
            <TableHead>Nombre</TableHead>
            <TableHead>Tamaño</TableHead>
            <TableHead>Fecha</TableHead>
            <TableHead></TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="item in documentos" :key="item.id">
            <TableCell>
              <Badge variant="outline">{{ tipoLabel(item.tipo) }}</Badge>
            </TableCell>
            <TableCell>
              <p>{{ item.nombreOriginal }}</p>
              <p v-if="item.observaciones" class="text-xs text-slate-500">{{ item.observaciones }}</p>
            </TableCell>
            <TableCell>{{ tamanoLabel(item.tamanoBytes) }}</TableCell>
            <TableCell>{{ fechaLabel(item.createdAt) }}</TableCell>
            <TableCell class="space-x-2 whitespace-nowrap">
              <Button variant="outline" size="sm" type="button" @click="onDescargar(item)">Descargar</Button>
              <Button variant="outline" size="sm" type="button" :disabled="saving" @click="onEliminar(item)">
                Eliminar
              </Button>
            </TableCell>
          </TableRow>
          <TableEmpty v-if="!documentos.length" :colspan="5">Sin documentos.</TableEmpty>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
