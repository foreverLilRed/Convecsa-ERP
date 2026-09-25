<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

import { getSession, logout } from '@/services/auth/authService'

const router = useRouter()
const session = getSession()
const sidebarOpen = ref(false)

const navItems = [
  { name: 'dashboard', label: 'Dashboard', icon: 'home' },
  { name: 'usuarios', label: 'Usuarios', icon: 'home' },
] as const

const userLabel = computed(() => session?.username ?? 'Usuario')

function onLogout() {
  logout()
  router.push({ name: 'login' })
}

function closeSidebar() {
  sidebarOpen.value = false
}
</script>

<template>
  <div class="min-h-screen bg-palette-soft text-slate-700">
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-20 bg-palette-navy/20 lg:hidden"
      @click="closeSidebar"
    />

    <aside
      class="fixed inset-y-0 left-0 z-30 flex w-60 flex-col border-r border-palette-cyan/70 bg-white transition-transform duration-300 lg:translate-x-0"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <div class="border-b border-palette-cyan/70 px-5 py-5">
        <div class="flex items-center gap-2.5">
          <img src="@/assets/convecsa-logo.png" alt="Convecsa" class="h-8 w-auto" />
          <span
            class="rounded bg-palette-navy px-1.5 py-0.5 text-[10px] font-bold leading-none tracking-wider text-white"
            >ERP</span
          >
        </div>
      </div>

      <nav class="flex-1 space-y-1 px-3 py-4">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium text-slate-500 transition-colors duration-150 hover:bg-palette-ice hover:text-palette-navy"
          active-class="!bg-palette-ice !text-palette-navy"
          @click="closeSidebar"
        >
          <svg
            v-if="item.icon === 'home'"
            class="h-4 w-4 shrink-0 text-palette-royal"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="1.75"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z"
            />
          </svg>
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="border-t border-palette-cyan/70 px-4 py-4">
        <p class="truncate text-xs font-semibold text-palette-navy">{{ userLabel }}</p>
        <button
          class="mt-3 w-full rounded-md px-3 py-2 text-left text-sm font-medium text-slate-500 transition-colors hover:bg-palette-ice hover:text-palette-navy"
          type="button"
          @click="onLogout"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>

    <div class="lg:pl-60">
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
        <h1 class="text-sm font-semibold tracking-wide text-palette-navy">Dashboard</h1>
      </header>

      <main class="px-4 py-6 lg:px-8 lg:py-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>
