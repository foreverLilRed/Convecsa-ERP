<script setup lang="ts">
import { PlusIcon } from '@lucide/vue'
import { ref } from 'vue'
import { toast } from 'vue-sonner'

import PersonaLinkAltaModal from '@/components/gestion-humana/PersonaLinkAltaModal.vue'
import PersonaListado from '@/components/gestion-humana/PersonaListado.vue'
import PersonaRegistroModal from '@/components/gestion-humana/PersonaRegistroModal.vue'
import { Button } from '@/components/ui/button'
import { obtenerPersona } from '@/services/persona/personaService'
import type { PersonaDTO } from '@/types/persona'

const modalOpen = ref(false)
const altaOpen = ref(false)
const listadoKey = ref(0)
const personaEditar = ref<PersonaDTO | null>(null)

function openCreate() {
  personaEditar.value = null
  modalOpen.value = true
}

async function onEdit(id: number) {
  try {
    personaEditar.value = await obtenerPersona(id)
    modalOpen.value = true
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la persona.')
  }
}

function onModalClose() {
  modalOpen.value = false
  personaEditar.value = null
}

function onPersonaSaved() {
  modalOpen.value = false
  personaEditar.value = null
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
      <PersonaListado :reload-key="listadoKey" @edit="onEdit">
        <template #actions>
          <div class="flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
            <Button
              variant="outline"
              class="h-10 w-full px-4 sm:w-auto"
              type="button"
              @click="altaOpen = true"
            >
              Link de alta
            </Button>
            <Button
              class="h-10 w-full bg-palette-navy px-4 text-white hover:bg-palette-royal sm:w-auto"
              type="button"
              @click="openCreate"
            >
              <PlusIcon data-icon="inline-start" />
              Registrar persona
            </Button>
          </div>
        </template>
      </PersonaListado>
    </div>

    <PersonaRegistroModal
      :open="modalOpen"
      :persona="personaEditar"
      @close="onModalClose"
      @saved="onPersonaSaved"
    />
    <PersonaLinkAltaModal :open="altaOpen" @close="altaOpen = false" />
  </section>
</template>
