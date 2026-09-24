<script setup lang="ts">
import { useRouter } from 'vue-router'

import { getSession, logout } from '@/services/auth/authService'

const router = useRouter()
const session = getSession()

function onLogout() {
  logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-800">
    <header class="border-b border-slate-200 bg-white">
      <div class="mx-auto flex max-w-5xl items-center justify-between px-6 py-4">
        <div class="flex items-center gap-2">
          <span class="text-lg font-extrabold tracking-wide text-brand-navy">CONVECSA</span>
          <span
            class="rounded bg-blue-600 px-1.5 py-0.5 text-[10px] font-bold leading-none tracking-wider text-white"
            >ERP</span
          >
        </div>
        <button
          class="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50"
          type="button"
          @click="onLogout"
        >
          Cerrar sesión
        </button>
      </div>
    </header>

    <main class="mx-auto max-w-5xl px-6 py-10">
      <p class="text-xs font-semibold uppercase tracking-widest text-sky-600">Panel principal</p>
      <h1 class="mt-2 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
        Bienvenido, {{ session?.username ?? 'usuario' }}
      </h1>
      <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
        Sesión iniciada. Esta vista es un placeholder mientras se construye el dashboard operativo.
      </p>
    </main>
  </div>
</template>
