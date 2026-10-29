<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'

import AppSidebar from '@/layouts/AppSidebar.vue'
import { getNavLabelByRouteName } from '@/layouts/nav'
import { getSession, logout } from '@/services/auth/authService'

const router = useRouter()
const route = useRoute()
const session = getSession()
const sidebarOpen = ref(false)
const sidebarCollapsed = ref(localStorage.getItem('convecsa.sidebar.collapsed') === 'true')

const userLabel = computed(() => session?.username ?? 'Usuario')
const pageTitle = computed(() => getNavLabelByRouteName(route.name))

function onLogout() {
  logout()
  router.push({ name: 'login' })
}

function closeSidebar() {
  sidebarOpen.value = false
}

function toggleSidebarCollapsed() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

watch(sidebarCollapsed, (collapsed) => {
  localStorage.setItem('convecsa.sidebar.collapsed', String(collapsed))
})
</script>

<template>
  <div class="min-h-screen bg-palette-soft text-slate-700">
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-20 bg-palette-navy/20 lg:hidden"
      @click="closeSidebar"
    />

    <AppSidebar
      :open="sidebarOpen"
      :collapsed="sidebarCollapsed"
      :username="userLabel"
      @close="closeSidebar"
      @logout="onLogout"
      @toggle-collapse="toggleSidebarCollapsed"
    />

    <div class="transition-[padding] duration-300" :class="sidebarCollapsed ? 'lg:pl-20' : 'lg:pl-64'">
      <header class="flex items-center gap-3 border-b border-palette-cyan/60 bg-white px-4 py-3 lg:px-8">
        <button
          class="inline-flex h-9 w-9 items-center justify-center rounded-md text-palette-navy hover:bg-palette-ice lg:hidden"
          type="button"
          aria-label="Abrir menú"
          @click="sidebarOpen = true"
        >
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 7h16M4 12h16M4 17h16" />
          </svg>
        </button>
        <h1 class="text-sm font-semibold tracking-wide text-palette-navy">{{ pageTitle }}</h1>
      </header>

      <main class="px-3 py-3 lg:px-4 lg:py-4">
        <RouterView />
      </main>
    </div>
  </div>
</template>
