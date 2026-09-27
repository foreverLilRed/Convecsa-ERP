<script setup lang="ts">
import { PlusIcon } from '@lucide/vue'
import { ref } from 'vue'

import PersonaListado from '@/components/gestion-humana/PersonaListado.vue'
import PersonaRegistroModal from '@/components/gestion-humana/PersonaRegistroModal.vue'
import { Button } from '@/components/ui/button'

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

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <PersonaListado :reload-key="listadoKey">
        <template #actions>
          <Button
            class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:w-auto"
            type="button"
            @click="modalOpen = true"
          >
            <PlusIcon data-icon="inline-start" />
            Registrar persona
          </Button>
        </template>
      </PersonaListado>
    </div>

    <PersonaRegistroModal :open="modalOpen" @close="modalOpen = false" @created="onPersonaCreated" />
  </section>
</template>
