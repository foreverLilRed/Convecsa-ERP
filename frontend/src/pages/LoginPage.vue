<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { login } from '@/services/auth/authService'
import type { RequestLogin } from '@/types/auth'

const router = useRouter()

const form = reactive({
  email: '',
  password: '',
  rememberMe: false,
})

const loading = ref(false)
const error = ref('')

async function onSubmit() {
  error.value = ''
  loading.value = true

  const request: RequestLogin = {
    username: form.email.trim(),
    password: form.password,
  }

  try {
    await login(request, form.rememberMe)
    await router.push({ name: 'dashboard' })
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'No se pudo iniciar sesión.'
    loading.value = false
  }
}

function onSsoClick() {
  // TODO: flujo Microsoft Entra ID / SSO
}
</script>

<template>
  <main class="relative min-h-screen w-full flex flex-col lg:flex-row">
    <div
      v-if="loading"
      class="absolute inset-0 z-20 flex flex-col items-center justify-center bg-white/70 backdrop-blur-sm transition-opacity duration-300"
    >
      <div
        class="h-11 w-11 rounded-full border-[3px] border-sky-200 border-t-sky-600 animate-spin"
        aria-hidden="true"
      />
      <p class="mt-4 text-sm font-semibold text-slate-600">Ingresando…</p>
    </div>
    <section
      class="lg:w-[48%] xl:w-[46%] hero-gradient text-white flex flex-col justify-between p-8 sm:p-12 lg:p-16 xl:p-20 relative overflow-hidden"
    >
      <div class="space-y-12 z-10">
        <div class="flex flex-col">
          <div class="flex items-center space-x-3.5">
            <img
              src="@/assets/convecsa-logo.png"
              alt="Convecsa"
              class="h-[54px] sm:h-[66px] w-auto"
            />
            <span
              class="bg-blue-600 text-white text-[10px] font-bold px-1.5 py-0.5 rounded tracking-wider leading-none"
              >ERP</span
            >
          </div>
          <span class="text-[10px] font-semibold tracking-widest text-sky-200/75 uppercase mt-1.5"
            >Construcción Civil</span
          >
        </div>

        <div class="pt-8 sm:pt-14 max-w-xl">
          <div class="inline-block text-sky-300 font-bold text-xs tracking-widest uppercase mb-4 opacity-90">
            ERP INTEGRAL DE OBRA
          </div>
          <h1
            class="text-3xl sm:text-4xl lg:text-[42px] leading-tight font-extrabold text-white tracking-tight"
          >
            Operaciones en obra, decisiones más rápidas
          </h1>
          <p class="mt-5 text-base sm:text-lg text-slate-300/90 font-normal leading-relaxed max-w-lg">
            Gestione presupuestos, avance físico, inventario de obra, compras y reportes financieros desde un solo
            lugar. Diseñado para equipos que necesitan control y visibilidad en tiempo real.
          </p>
        </div>
      </div>

      <div class="absolute -right-24 -bottom-24 w-96 h-96 rounded-full bg-sky-500/5 blur-3xl pointer-events-none" />

      <div class="pt-12 z-10 border-t border-white/10 mt-12 flex flex-col gap-1">
        <p class="text-xs text-slate-400 font-medium">Convecsa ERP © 2026</p>
        <p class="text-[11px] text-slate-500">Infraestructura de Gestión y Construcción Civil</p>
      </div>
    </section>

    <section
      class="lg:w-[52%] xl:w-[54%] bg-white flex flex-col justify-between p-6 sm:p-12 lg:p-16 xl:p-20 text-slate-800"
    >
      <div class="hidden lg:block" />

      <div class="w-full max-w-md mx-auto my-auto py-8">
        <header class="mb-8">
          <h2 class="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">Iniciar sesión</h2>
          <p class="mt-2 text-sm text-slate-500 leading-normal">
            Ingrese su usuario o correo corporativo y contraseña para acceder al panel.
          </p>
        </header>

        <form class="space-y-5" @submit.prevent="onSubmit">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5 uppercase tracking-wide" for="email">
              Usuario o correo
            </label>
            <div class="relative rounded-lg shadow-sm">
              <input
                id="email"
                v-model="form.email"
                class="block w-full rounded-lg border border-sky-500/80 px-3.5 py-2.5 text-slate-900 text-sm placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20 transition duration-150"
                name="username"
                placeholder="usuario o nombre@convecsa.com"
                required
                type="text"
                autocomplete="username"
                :disabled="loading"
              />
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5 uppercase tracking-wide" for="password">
              Contraseña
            </label>
            <div class="relative rounded-lg shadow-sm">
              <input
                id="password"
                v-model="form.password"
                class="block w-full rounded-lg border border-slate-200 px-3.5 py-2.5 text-slate-900 text-sm placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20 transition duration-150 bg-slate-50/40"
                name="password"
                placeholder="••••••••"
                required
                type="password"
                autocomplete="current-password"
                :disabled="loading"
              />
            </div>
          </div>

          <div class="flex items-center justify-between pt-1">
            <label class="flex items-center text-sm cursor-pointer select-none">
              <input
                v-model="form.rememberMe"
                class="h-4 w-4 rounded border-slate-300 text-sky-600 focus:ring-sky-500 focus:ring-offset-0"
                name="remember-me"
                type="checkbox"
              />
              <span class="ml-2 text-xs sm:text-sm text-slate-600">Recordarme en este equipo</span>
            </label>
            <a
              class="text-xs sm:text-sm font-semibold text-sky-600 hover:text-sky-700 hover:underline transition-colors duration-150"
              href="#"
            >
              ¿Olvidó su contraseña?
            </a>
          </div>

          <p v-if="error" class="text-sm font-medium text-red-600">{{ error }}</p>

          <div class="pt-2">
            <button
              class="w-full inline-flex justify-center items-center py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-semibold text-white bg-brand-blue hover:bg-[#0369a1] active:bg-[#075985] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-sky-500 transition-all duration-150 disabled:cursor-not-allowed disabled:opacity-60"
              type="submit"
              :disabled="loading"
            >
              <span
                v-if="loading"
                class="mr-2 inline-block h-4 w-4 rounded-full border-2 border-white/40 border-t-white animate-spin"
                aria-hidden="true"
              />
              {{ loading ? 'Ingresando…' : 'Entrar' }}
            </button>
          </div>

          <div class="pt-3">
            <div class="relative my-4">
              <div class="absolute inset-0 flex items-center">
                <div class="w-full border-t border-slate-200" />
              </div>
              <div class="relative flex justify-center text-xs uppercase">
                <span class="bg-white px-2 text-slate-400 font-medium">O continuar con</span>
              </div>
            </div>
            <button
              class="w-full inline-flex justify-center items-center gap-2 py-2.5 px-4 border border-slate-200 rounded-lg shadow-xs text-xs sm:text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 active:bg-slate-100 transition-all duration-150 disabled:cursor-not-allowed disabled:opacity-60"
              type="button"
              :disabled="loading"
              @click="onSsoClick"
            >
              <svg class="w-4 h-4 text-sky-700 shrink-0" fill="none" viewBox="0 0 21 21" xmlns="http://www.w3.org/2000/svg">
                <path d="M1 1H10V10H1V1Z" fill="#F25022" />
                <path d="M11 1H20V10H11V1Z" fill="#7FBA00" />
                <path d="M1 11H10V20H1V11Z" fill="#00A4EF" />
                <path d="M11 11H20V20H11V11Z" fill="#FFB900" />
              </svg>
              Acceder con Microsoft Entra ID / SSO
            </button>
          </div>
        </form>
      </div>

      <footer class="text-center py-4">
        <p class="text-xs text-slate-400 font-normal">
          Desarrollado para <span class="font-medium text-sky-600">Convecsa Grupo</span> • Soporte TI Corporativo
        </p>
      </footer>
    </section>
  </main>
</template>
