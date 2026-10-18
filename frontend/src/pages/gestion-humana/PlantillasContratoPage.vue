<script setup lang="ts">
import { PencilIcon, PlusIcon } from '@lucide/vue'
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

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
const router = useRouter()

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

function openRoute(name: 'gh-plantilla-contrato-nueva' | 'gh-plantilla-contrato-editar', id?: number) {
  const route = router.resolve({ name, params: id == null ? undefined : { id } })
  const opened = window.open(route.href, '_blank')
  if (!opened) {
    toast.error('El navegador bloqueó la nueva pestaña. Permita ventanas emergentes para este sitio.')
    return
  }
  opened.opener = null
}

function onEdit(id: number) {
  openRoute('gh-plantilla-contrato-editar', id)
}

function onWindowFocus() {
  void load()
}

onMounted(() => {
  void load()
  window.addEventListener('focus', onWindowFocus)
})

onUnmounted(() => {
  window.removeEventListener('focus', onWindowFocus)
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
          Cree plantillas HTML de contrato o adenda. Al guardar, estarán disponibles en el formulario correspondiente.
        </p>
      </div>
      <Button
        class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:mt-7 sm:w-auto"
        type="button"
        @click="openRoute('gh-plantilla-contrato-nueva')"
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
              <TableHead class="px-3">Uso</TableHead>
              <TableHead class="px-3 text-right">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!plantillas.length" :colspan="4">
              <span class="text-slate-500">No hay plantillas registradas.</span>
            </TableEmpty>
            <TableRow v-for="plantilla in plantillas" :key="plantilla.id">
              <TableCell class="px-3 py-3 font-medium">{{ plantilla.codigo }}</TableCell>
              <TableCell class="px-3 py-3">{{ plantilla.nombre }}</TableCell>
              <TableCell class="px-3 py-3">{{ plantilla.uso === 'ADENDA' ? 'Adenda' : 'Contrato' }}</TableCell>
              <TableCell class="px-3 py-3 text-right">
                <Button
                  variant="outline"
                  size="sm"
                  type="button"
                  :aria-label="`Editar plantilla ${plantilla.codigo}`"
                  @click="onEdit(plantilla.id)"
                >
                  <PencilIcon data-icon="inline-start" />
                  Editar
                </Button>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </div>
  </section>
</template>
