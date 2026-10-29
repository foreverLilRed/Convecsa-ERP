<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => (typeof route.meta.title === 'string' ? route.meta.title : 'Módulo'))
</script>

<template>
  <section>
    <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">{{ title }}</h2>
    <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
      Vista placeholder de {{ title.toLowerCase() }}.
    </p>
  </section>
</template>
