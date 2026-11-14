<script setup lang="ts">
import { PrinterIcon } from '@lucide/vue'
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { Button } from '@/components/ui/button'
import FlujosSeccion from '@/pages/documentacion/secciones/FlujosSeccion.vue'
import GestionHumanaSeccion from '@/pages/documentacion/secciones/GestionHumanaSeccion.vue'
import IntroSeccion from '@/pages/documentacion/secciones/IntroSeccion.vue'
import PreguntasSeccion from '@/pages/documentacion/secciones/PreguntasSeccion.vue'
import ProyectosSeccion from '@/pages/documentacion/secciones/ProyectosSeccion.vue'
import SomaSeccion from '@/pages/documentacion/secciones/SomaSeccion.vue'
import { seccionesManual } from '@/pages/documentacion/secciones/indice'

const route = useRoute()
const router = useRouter()

const hashActivo = computed(() => (typeof route.hash === 'string' ? route.hash.replace('#', '') : ''))

function irA(id: string) {
  void router.replace({ hash: `#${id}` })
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function imprimir() {
  window.print()
}

onMounted(() => {
  document.body.classList.add('manual-en-pantalla')
  const id = hashActivo.value
  if (id) {
    document.getElementById(id)?.scrollIntoView({ behavior: 'auto', block: 'start' })
  }
})

onUnmounted(() => {
  document.body.classList.remove('manual-en-pantalla')
})
</script>

<template>
  <section class="min-w-0 space-y-4">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Manual interno</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Manual de uso del ERP</h2>
        <p class="mt-2 max-w-2xl text-sm text-slate-500">
          Guía operativa de Convecsa ERP para personal administrativo y gerencia. Describe las pantallas tal como
          aparecen en el sistema: botones, pestañas y restricciones por rol.
        </p>
      </div>
      <Button class="no-print h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal" type="button" @click="imprimir">
        <PrinterIcon data-icon="inline-start" />
        Imprimir
      </Button>
    </div>

    <div class="lg:grid lg:grid-cols-[16rem_minmax(0,1fr)] lg:items-start lg:gap-6">
      <aside class="indice no-print mb-4 rounded-2xl border border-palette-cyan/70 bg-white p-4 shadow-sm lg:sticky lg:top-4 lg:mb-0">
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Índice</p>
        <nav class="mt-3 space-y-3" aria-label="Índice del manual">
          <div v-for="seccion in seccionesManual" :key="seccion.id">
            <button
              class="w-full rounded-md px-2 py-1 text-left text-sm font-semibold text-palette-navy hover:bg-palette-ice"
              :class="hashActivo === seccion.id ? 'bg-palette-ice' : ''"
              type="button"
              @click="irA(seccion.id)"
            >
              {{ seccion.titulo }}
            </button>
            <ul class="mt-1 space-y-0.5 pl-2">
              <li v-for="sub in seccion.subsecciones" :key="sub.id">
                <button
                  class="w-full rounded-md px-2 py-0.5 text-left text-xs text-slate-500 hover:bg-palette-ice hover:text-palette-navy"
                  :class="hashActivo === sub.id ? 'bg-palette-ice font-medium text-palette-navy' : ''"
                  type="button"
                  @click="irA(sub.id)"
                >
                  {{ sub.titulo }}
                </button>
              </li>
            </ul>
          </div>
        </nav>
      </aside>

      <article class="manual min-w-0 space-y-12 rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm sm:p-8">
        <IntroSeccion />
        <GestionHumanaSeccion />
        <ProyectosSeccion />
        <SomaSeccion />
        <FlujosSeccion />
        <PreguntasSeccion />
      </article>
    </div>
  </section>
</template>

<style scoped>
.manual :deep(h2) {
  scroll-margin-top: 5.5rem;
}

.manual :deep(h3) {
  scroll-margin-top: 5.5rem;
}

@media print {
  .no-print {
    display: none !important;
  }

  .manual {
    border: none;
    box-shadow: none;
    padding: 0;
  }
}
</style>

<style>
@media print {
  body.manual-en-pantalla aside,
  body.manual-en-pantalla header {
    display: none !important;
  }

  body.manual-en-pantalla .lg\:pl-64 {
    padding-left: 0 !important;
  }

  body.manual-en-pantalla main {
    padding: 0 !important;
  }
}
</style>
