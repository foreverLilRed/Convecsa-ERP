<script setup lang="ts">
import { PlusIcon } from '@lucide/vue'
import { onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'

import ContratoRegistroModal from '@/components/gestion-humana/ContratoRegistroModal.vue'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { obtenerContratosResume } from '@/services/contrato/contratoService'
import type { ContratosResume } from '@/types/contrato'

const loading = ref(true)
const resume = ref<ContratosResume | null>(null)
const modalOpen = ref(false)

async function load() {
  loading.value = true
  try {
    resume.value = await obtenerContratosResume()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el resumen de contratos.')
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
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Contratos</h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          Personas con al menos un contrato activo y personas sin contrato activo.
        </p>
      </div>
      <Button
        class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:mt-7 sm:w-auto"
        type="button"
        @click="modalOpen = true"
      >
        <PlusIcon data-icon="inline-start" />
        Nuevo contrato
      </Button>
    </div>

    <div class="mt-6 grid gap-4 sm:grid-cols-2">
      <template v-if="loading">
        <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <Skeleton class="h-4 w-32" />
          <Skeleton class="mt-4 h-10 w-16" />
        </div>
        <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <Skeleton class="h-4 w-32" />
          <Skeleton class="mt-4 h-10 w-16" />
        </div>
      </template>
      <template v-else-if="resume">
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Con contrato activo
          </p>
          <p class="mt-3 text-3xl font-bold text-palette-navy">
            {{ resume.conContratoActivo }}
          </p>
        </article>
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Sin contrato activo
          </p>
          <p class="mt-3 text-3xl font-bold text-palette-navy">
            {{ resume.sinContratoActivo }}
          </p>
        </article>
      </template>
    </div>

    <ContratoRegistroModal :open="modalOpen" @close="modalOpen = false" @saved="onSaved" />
  </section>
</template>
