<script setup lang="ts">
import { PlusIcon } from '@lucide/vue'
import { onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'

import PlantillaRegistroModal from '@/components/gestion-humana/PlantillaRegistroModal.vue'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { listarPlantillasContrato } from '@/services/contrato/contratoService'
import type { PlantillaContrato } from '@/types/contrato'

const loading = ref(true)
const plantillas = ref<PlantillaContrato[]>([])
const modalOpen = ref(false)

async function load() {
  loading.value = true
  try {
    plantillas.value = await listarPlantillasContrato()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las plantillas.')
  } finally {
    loading.value = false
  }
}

function onSaved() {
  modalOpen.value = false
  void load()
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          Plantillas de Contrato
        </h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Cree plantillas HTML. Al guardar, estarán disponibles en Nuevo contrato.
        </p>
      </div>
      <Button
        class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:mt-7 sm:w-auto"
        type="button"
        @click="modalOpen = true"
      >
        <PlusIcon data-icon="inline-start" />
        Nueva plantilla
      </Button>
    </div>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <div
        v-if="loading"
        class="overflow-hidden rounded-lg border border-palette-cyan/60"
        aria-busy="true"
      >
        <span class="sr-only">Cargando plantillas</span>
        <div class="space-y-3 p-4">
          <Skeleton class="h-9 w-full" />
          <Skeleton v-for="row in 4" :key="row" class="h-11 w-full" />
        </div>
      </div>

      <div v-else class="overflow-hidden rounded-lg border border-palette-cyan/60">
        <Table>
          <TableCaption class="sr-only">Listado de plantillas de contrato.</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead class="px-3">Código</TableHead>
              <TableHead class="px-3">Nombre</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!plantillas.length" :colspan="2">
              <span class="text-slate-500">No hay plantillas registradas.</span>
            </TableEmpty>
            <TableRow v-for="plantilla in plantillas" :key="plantilla.id">
              <TableCell class="px-3 py-3 font-medium">{{ plantilla.codigo }}</TableCell>
              <TableCell class="px-3 py-3">{{ plantilla.nombre }}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </div>

    <PlantillaRegistroModal :open="modalOpen" @close="modalOpen = false" @saved="onSaved" />
  </section>
</template>
