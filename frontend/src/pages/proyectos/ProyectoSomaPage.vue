<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import { Skeleton } from '@/components/ui/skeleton'
import { obtenerResumenSoma } from '@/services/soma/somaService'
import type { SomaResumenObra } from '@/types/soma'

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const loading = ref(true)
const resumen = ref<SomaResumenObra | null>(null)

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    resumen.value = await obtenerResumenSoma(proyectoId.value)
  } catch (err) {
    resumen.value = null
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el resumen SOMA.')
  } finally {
    loading.value = false
  }
}

watch(proyectoId, () => {
  void load()
})

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="space-y-3">
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-16 w-full" />
      </div>
    </div>

    <div v-else class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <h3 class="text-base font-semibold text-palette-navy">Resumen SOMA</h3>
      <dl class="mt-4 grid gap-4 sm:grid-cols-2">
        <div>
          <dt class="text-sm font-medium text-slate-500">Obra</dt>
          <dd class="mt-1 text-sm text-slate-900">
            {{ resumen ? `${resumen.codigoObra} · ${resumen.nombreObra}` : '—' }}
          </dd>
        </div>
        <div>
          <dt class="text-sm font-medium text-slate-500">Supervisor SSOMA</dt>
          <dd class="mt-1 text-sm text-slate-900">{{ resumen?.supervisorSsoma ?? 'Sin asignar' }}</dd>
        </div>
      </dl>
      <p class="mt-4 text-sm text-slate-500">
        Las secciones de vigilancia médica, SCTR, EPP y campo se irán habilitando en las siguientes entregas.
      </p>
    </div>
  </div>
</template>
