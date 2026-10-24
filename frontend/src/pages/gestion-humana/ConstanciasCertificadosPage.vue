<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon, PrinterIcon } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { listarContratos } from '@/services/contrato/contratoService'
import { openAuthenticatedRequest } from '@/services/http'
import type { ContratoDTO } from '@/types/contrato'
import type { PagedResponse } from '@/types/persona'

const PAGE_SIZE = 15
const page = ref(0)
const loading = ref(true)
const openingId = ref<number | null>(null)
const data = ref<PagedResponse<ContratoDTO> | null>(null)
const totalPages = computed(() => data.value?.totalPages ?? 0)

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

onMounted(() => void cargar())
</script>

<template>
  <section class="space-y-3">
    <div class="rounded-2xl bg-gradient-to-r from-palette-navy to-palette-royal p-6 text-white shadow-sm sm:p-8">
      <h2 class="text-2xl font-bold sm:text-3xl">Constancias y certificados</h2>
      <p class="mt-2 max-w-2xl text-sm text-blue-50">Genera una constancia de trabajo con los datos de la persona y su contrato. El cargo, proyecto y período se completan automáticamente.</p>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white shadow-sm">
      <div class="border-b border-slate-200 p-5">
        <h3 class="font-semibold text-palette-navy">Constancia de trabajo</h3>
        <p class="mt-1 text-sm text-slate-500">Elige el contrato correspondiente. La constancia se abrirá lista para imprimir o guardar como PDF.</p>
      </div>
      <div v-if="loading" class="space-y-3 p-5"><Skeleton v-for="n in 4" :key="n" class="h-16 w-full" /></div>
      <Table v-else>
        <TableHeader><TableRow><TableHead>Trabajador</TableHead><TableHead>Cargo y plantilla</TableHead><TableHead>Período</TableHead><TableHead>Estado</TableHead><TableHead class="text-right">Documento</TableHead></TableRow></TableHeader>
        <TableBody>
          <TableEmpty v-if="!data?.content.length" :colspan="5">No hay contratos para generar constancias.</TableEmpty>
          <TableRow v-for="contrato in data?.content" :key="contrato.id">
            <TableCell><p class="font-semibold text-slate-900">{{ contrato.personaNombreCompleto }}</p><p class="text-xs text-slate-500">{{ contrato.personaNumeroDocumento }}</p></TableCell>
            <TableCell><p class="font-medium text-slate-800">{{ cargo(contrato) }}</p><p class="text-xs text-slate-500">{{ contrato.tipoPlantillaNombre || contrato.categoriaContratoNombre }}</p></TableCell>
            <TableCell class="text-sm text-slate-600">{{ fecha(contrato.fechaInicio) }} – {{ finContrato(contrato) }}</TableCell>
            <TableCell><Badge variant="secondary">{{ contrato.activo ? 'Activo' : 'Finalizado' }}</Badge></TableCell>
            <TableCell class="text-right"><Button size="sm" :disabled="openingId === contrato.id" @click="abrir(contrato)"><PrinterIcon class="mr-2 size-4" />Generar constancia</Button></TableCell>
          </TableRow>
        </TableBody>
      </Table>
      <div v-if="totalPages > 1" class="flex items-center justify-end gap-3 border-t border-slate-200 p-4 text-sm text-slate-600">
        <span>Página {{ page + 1 }} de {{ totalPages }}</span>
        <Button variant="outline" size="sm" :disabled="page === 0 || loading" aria-label="Página anterior" @click="cambiarPagina(-1)"><ChevronLeftIcon class="size-4" /></Button>
        <Button variant="outline" size="sm" :disabled="page + 1 >= totalPages || loading" aria-label="Página siguiente" @click="cambiarPagina(1)"><ChevronRightIcon class="size-4" /></Button>
      </div>
    </div>
  </section>
</template>
