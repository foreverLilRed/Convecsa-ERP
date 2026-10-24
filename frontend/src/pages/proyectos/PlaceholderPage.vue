<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => (typeof route.meta.title === 'string' ? route.meta.title : 'Módulo'))
const description = computed(() =>
  typeof route.meta.description === 'string'
    ? route.meta.description
    : `Vista placeholder de ${title.value.toLowerCase()}. Próximamente se conectará al backend.`,
)
</script>

<template>
  <section>
    <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">{{ title }}</h2>
    <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
      {{ description }}
    </p>
    <div class="mt-3 rounded-lg border border-dashed border-palette-cyan/80 bg-palette-ice/40 px-4 py-8 text-center">
      <p class="text-sm text-slate-600">
        Módulo en preparación dentro de <strong class="text-palette-navy">Proyectos</strong>.
      </p>
    </div>
  </section>
</template>
