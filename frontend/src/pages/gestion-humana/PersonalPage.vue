<script setup lang="ts">
import { ref } from 'vue'

import PersonaListado from '@/components/gestion-humana/PersonaListado.vue'
import PersonaRegistroModal from '@/components/gestion-humana/PersonaRegistroModal.vue'

const modalOpen = ref(false)
const listadoKey = ref(0)

function onPersonaCreated() {
  modalOpen.value = false
  listadoKey.value += 1
}
</script>

<template>
  <section>
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Gestión Humana</p>
    <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Personal</h2>
    <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
      Consulte el padrón de personal y registre nuevas personas.
    </p>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
      <PersonaListado :reload-key="listadoKey">
        <template #actions>
          <button
            class="inline-flex items-center justify-center rounded-lg bg-palette-navy px-4 py-2 text-sm font-semibold text-white hover:bg-palette-royal"
            type="button"
            @click="modalOpen = true"
          >
            Registrar persona
          </button>
        </template>
      </PersonaListado>
    </div>

    <PersonaRegistroModal :open="modalOpen" @close="modalOpen = false" @created="onPersonaCreated" />
  </section>
</template>
