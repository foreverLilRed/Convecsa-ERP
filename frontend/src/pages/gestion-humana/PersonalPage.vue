<script setup lang="ts">
import { ref } from 'vue'

import PersonaRegistroModal from '@/components/gestion-humana/PersonaRegistroModal.vue'

const tabs = [
  { id: 'ingreso', label: 'Ingreso' },
  { id: 'listado', label: 'Listado' },
  { id: 'contratos', label: 'Contratos' },
] as const

const activeTab = ref<(typeof tabs)[number]['id']>('ingreso')
const modalOpen = ref(false)

function onPersonaCreated() {
  modalOpen.value = false
}
</script>

<template>
  <section>
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
    <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Personal</h2>
    <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
      Ingreso, listado y contratos se gestionan en esta misma vista.
    </p>

    <div class="mt-6 flex gap-1 border-b border-palette-cyan/70">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="px-3 py-2 text-sm font-medium transition-colors"
        :class="
          activeTab === tab.id
            ? 'border-b-2 border-palette-navy text-palette-navy'
            : 'text-slate-500 hover:text-palette-navy'
        "
        type="button"
        @click="activeTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
      <div v-if="activeTab === 'ingreso'" class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <p class="text-sm text-slate-500">Registre una nueva persona en el padrón de personal.</p>
        <button
          class="inline-flex items-center justify-center rounded-lg bg-palette-navy px-4 py-2 text-sm font-semibold text-white hover:bg-palette-royal"
          type="button"
          @click="modalOpen = true"
        >
          Registrar persona
        </button>
      </div>
      <p v-else class="text-sm text-slate-500">
        Vista placeholder de {{ tabs.find((tab) => tab.id === activeTab)?.label.toLowerCase() }} de personal.
      </p>
    </div>

    <PersonaRegistroModal :open="modalOpen" @close="modalOpen = false" @created="onPersonaCreated" />
  </section>
</template>
