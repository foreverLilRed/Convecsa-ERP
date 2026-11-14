<script setup lang="ts">
import { DownloadIcon } from '@lucide/vue'
import { onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { listarProyectos } from '@/services/construccion/construccionService'
import { descargarFormatoSunafil } from '@/services/soma/somaService'
import type { Proyecto } from '@/types/construccion'

const selectClass =
  'mt-1.5 block h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-900 outline-none focus:border-sky-600 focus:ring-2 focus:ring-sky-500/20 disabled:bg-slate-100'
const inputClass =
  'mt-1.5 block h-10 min-w-0 w-full rounded-lg border border-slate-200 bg-white px-3 py-0 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const FORMATOS = [
  { n: 1, label: '1 — Accidentes, enfermedades e incidentes' },
  { n: 2, label: '2 — Exámenes médico ocupacionales' },
  { n: 3, label: '3 — Monitoreo de agentes' },
  { n: 4, label: '4 — Inspecciones internas de SST' },
  { n: 5, label: '5 — Estadísticas HHT, IF, IS, IA' },
  { n: 6, label: '6 — Equipos de seguridad o emergencia' },
  { n: 7, label: '7 — Inducción, capacitación y simulacros' },
  { n: 8, label: '8 — Auditorías del SG-SST' },
] as const

const anioLima = Number(
  new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Lima', year: 'numeric' }).format(new Date()),
)

const proyectos = ref<Proyecto[]>([])
const downloading = ref(false)
const form = reactive({
  proyectoId: '' as string,
  anio: String(anioLima),
  mes: '',
  formato: '1',
  tipo: 'pdf' as 'pdf' | 'xlsx',
})

async function cargarObras() {
  try {
    proyectos.value = await listarProyectos()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las obras.')
  }
}

async function descargar() {
  const formato = Number(form.formato)
  const anio = form.anio.trim() === '' ? null : Number(form.anio)
  const mes = form.mes.trim() === '' ? null : Number(form.mes)
  if (mes != null && anio == null) {
    toast.error('El mes exige el año.')
    return
  }
  downloading.value = true
  try {
    await descargarFormatoSunafil({
      formato,
      tipo: form.tipo,
      proyectoId: form.proyectoId === '' ? null : Number(form.proyectoId),
      anio,
      mes,
      filename: `sunafil-formato-${formato}.${form.tipo}`,
    })
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo descargar el formato.')
  } finally {
    downloading.value = false
  }
}

onMounted(() => {
  void cargarObras()
})
</script>

<template>
  <section class="min-w-0 space-y-3">
    <header>
      <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">SOMA corporativo</p>
      <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Formatos SUNAFIL</h2>
      <p class="mt-2 text-sm text-slate-500">
        Descarga PDF o Excel de los registros R.M. 050-2013-TR. Sin obra se exporta el alcance corporativo.
      </p>
    </header>

    <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm sm:p-6">
      <form class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3" @submit.prevent="descargar">
        <div>
          <label class="text-sm font-medium text-slate-700" for="soma-fmt-obra">Obra (opcional)</label>
          <select id="soma-fmt-obra" v-model="form.proyectoId" :class="selectClass">
            <option value="">Corporativo</option>
            <option v-for="obra in proyectos" :key="obra.id" :value="String(obra.id)">
              {{ obra.codigo }} — {{ obra.nombre }}
            </option>
          </select>
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="soma-fmt-anio">Año</label>
          <Input id="soma-fmt-anio" v-model="form.anio" type="number" min="2000" max="2100" :class="inputClass" />
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="soma-fmt-mes">Mes</label>
          <select id="soma-fmt-mes" v-model="form.mes" :class="selectClass">
            <option value="">Todo el año</option>
            <option v-for="n in 12" :key="n" :value="String(n)">{{ n }}</option>
          </select>
        </div>
        <div class="sm:col-span-2">
          <label class="text-sm font-medium text-slate-700" for="soma-fmt-formato">Formato</label>
          <select id="soma-fmt-formato" v-model="form.formato" :class="selectClass">
            <option v-for="item in FORMATOS" :key="item.n" :value="String(item.n)">{{ item.label }}</option>
          </select>
        </div>
        <div>
          <label class="text-sm font-medium text-slate-700" for="soma-fmt-tipo">Tipo</label>
          <select id="soma-fmt-tipo" v-model="form.tipo" :class="selectClass">
            <option value="pdf">PDF</option>
            <option value="xlsx">Excel</option>
          </select>
        </div>
        <div class="flex items-end sm:col-span-2 xl:col-span-3">
          <Button class="h-10 px-4" type="submit" :disabled="downloading">
            <DownloadIcon data-icon="inline-start" />
            {{ downloading ? 'Descargando…' : 'Descargar' }}
          </Button>
        </div>
      </form>
    </article>
  </section>
</template>
