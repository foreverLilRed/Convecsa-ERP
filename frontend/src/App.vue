<script setup lang="ts">
import { RouterView } from 'vue-router'
import 'vue-sonner/style.css'

import { Toaster } from '@/components/ui/sonner'
</script>

<template>
  <Toaster class="pointer-events-auto" position="top-right" rich-colors />
  <RouterView v-slot="{ Component }">
    <Transition name="page" mode="out-in">
      <component :is="Component" />
    </Transition>
  </RouterView>
</template>
