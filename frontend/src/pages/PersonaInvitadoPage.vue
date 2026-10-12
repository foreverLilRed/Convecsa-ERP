<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import PersonaRegistroModal from '@/components/gestion-humana/PersonaRegistroModal.vue'
import { obtenerPersonaInvitado } from '@/services/persona/personaService'
import type { PersonaDTO } from '@/types/persona'

const route = useRoute()
const token = computed(() => String(route.params.token ?? ''))
const loading = ref(true)
const error = ref('')
const persona = ref<PersonaDTO | null>(null)
const saved = ref(false)

async function load() {
  saved.value = false
  persona.value = null
  error.value = ''
  if (!token.value) {
    error.value = 'El enlace no es válido o ya venció.'
    loading.value = false
    return
  }
  loading.value = true
  try {
    persona.value = await obtenerPersonaInvitado(token.value)
  } catch {
    error.value = 'El enlace no es válido o ya venció.'
  } finally {
    loading.value = false
  }
}

watch(token, load, { immediate: true })

function onSaved() {
  saved.value = true
}
</script>

<template>
  <main class="min-h-screen bg-slate-50">
    <header class="border-b border-slate-200 bg-white px-5 py-4">
      <div class="mx-auto flex max-w-5xl items-center gap-3">
        <img src="@/assets/convecsa-logo.png" alt="Convecsa" class="h-10 w-auto" />
        <div>
          <p class="text-[11px] font-semibold uppercase tracking-widest text-palette-royal">
            Ficha de personal
          </p>
          <h1 class="text-lg font-bold text-palette-navy">Completar datos</h1>
        </div>
      </div>
    </header>

    <div class="mx-auto max-w-3xl px-5 py-10">
      <p v-if="loading" class="text-sm text-slate-500">Cargando ficha…</p>
      <div
        v-else-if="error"
        class="rounded-xl border border-slate-200 bg-white px-5 py-8 text-center"
      >
        <h2 class="text-lg font-bold text-palette-navy">Enlace no disponible</h2>
        <p class="mt-2 text-sm text-slate-500">{{ error }}</p>
      </div>
      <div
        v-else-if="saved"
        class="rounded-xl border border-slate-200 bg-white px-5 py-8 text-center"
      >
        <h2 class="text-lg font-bold text-palette-navy">Datos guardados</h2>
        <p class="mt-2 text-sm text-slate-500">Ya puede cerrar esta ventana.</p>
      </div>
    </div>

    <PersonaRegistroModal
      v-if="persona && !saved"
      :open="true"
      :persona="persona"
      :guest-token="token"
      @saved="onSaved"
    />
  </main>
</template>
