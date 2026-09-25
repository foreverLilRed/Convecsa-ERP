<script setup lang="ts">
import { getSession } from '@/services/auth/authService'

const session = getSession()
</script>

<template>
  <section>
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Panel principal</p>
    <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
      Bienvenido, {{ session?.username ?? 'usuario' }}
    </h2>
    <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
      Sesión iniciada. Esta vista es un placeholder mientras se construye el dashboard operativo.
    </p>
  </section>
</template>
