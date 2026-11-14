<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { listarProyectos } from '@/services/construccion/construccionService'
import { listarCapacitacionesCorporativo } from '@/services/soma/somaService'
import type { Proyecto } from '@/types/construccion'
import type { SomaCapacitacion } from '@/types/soma'

const MINIMO_ANUAL = 4
const inputClass =
  'mt-1.5 block h-10 min-w-0 w-full rounded-lg border border-slate-200 bg-white px-3 py-0 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const anioLima = Number(
  new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Lima', year: 'numeric' }).format(new Date()),
)

const loading = ref(true)
const error = ref('')
const anio = ref(anioLima)
const proyectos = ref<Proyecto[]>([])
const sesiones = ref<SomaCapacitacion[]>([])

const obraPorId = computed(() => new Map(proyectos.value.map((item) => [item.id, item])))
const cumpleMinimo = computed(() => sesiones.value.length >= MINIMO_ANUAL)

function formatFecha(value: string | null | undefined): string {
  if (!value) return '—'
  return new Date(`${value}T12:00:00`).toLocaleDateString('es-PE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function obraLabel(proyectoId: number | null): string {
  if (proyectoId == null) return ''
  const obra = obraPorId.value.get(proyectoId)
  return obra ? obra.codigo : String(proyectoId)
}

function asistentesTxt(fila: SomaCapacitacion): string {
  const nombres = (fila.asistentes ?? [])
    .map((item) => item.personaNombre)
    .filter((nombre): nombre is string => Boolean(nombre))
  if (nombres.length === 0) {
    return String(fila.asistentes?.length ?? 0)
  }
  return `${fila.asistentes.length}: ${nombres.join(', ')}`
}

async function cargar() {
  loading.value = true
  error.value = ''
  try {
    const [listaObras, lista] = await Promise.all([
      listarProyectos(),
      listarCapacitacionesCorporativo(anio.value),
    ])
    proyectos.value = listaObras
    sesiones.value = lista
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'No se pudieron cargar las capacitaciones.'
    sesiones.value = []
  } finally {
    loading.value = false
  }
}

watch(anio, () => {
  void cargar()
})

onMounted(() => {
  void cargar()
})
</script>

<template>
  <section class="min-w-0 space-y-3">
    <header class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">SOMA corporativo</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Capacitaciones</h2>
        <p class="mt-2 text-sm text-slate-500">Listado anual. El alta se registra en cada obra.</p>
      </div>
      <div class="w-full sm:w-40">
        <label class="text-sm font-medium text-slate-700" for="soma-cap-anio">Año</label>
        <Input id="soma-cap-anio" v-model.number="anio" type="number" min="2000" max="2100" :class="inputClass" />
      </div>
    </header>

    <article class="rounded-2xl border border-palette-cyan/70 bg-white p-5 shadow-sm">
      <p class="text-sm font-medium text-slate-500">Mínimo Ley 29783</p>
      <div class="mt-2 flex flex-wrap items-center gap-3">
        <p class="text-3xl font-bold tabular-nums text-palette-navy">
          {{ sesiones.length }} / {{ MINIMO_ANUAL }}
        </p>
        <Badge :variant="cumpleMinimo ? 'success' : 'secondary'">
          {{ cumpleMinimo ? 'Cumple las 4 sesiones del año' : 'Pendiente alcanzar 4 sesiones' }}
        </Badge>
      </div>
    </article>

    <p v-if="error" class="text-sm text-red-700">{{ error }}</p>
    <div v-if="loading" class="space-y-2">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-40 w-full" />
    </div>
    <div v-else class="overflow-x-auto rounded-2xl border border-palette-cyan/70 bg-white">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Fecha</TableHead>
            <TableHead>Tipo</TableHead>
            <TableHead>Tema</TableHead>
            <TableHead>Horas</TableHead>
            <TableHead>Expositor</TableHead>
            <TableHead>Obra</TableHead>
            <TableHead>Asistentes</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="fila in sesiones" :key="fila.id">
            <TableCell>{{ formatFecha(fila.fecha) }}</TableCell>
            <TableCell>{{ fila.tipo }}</TableCell>
            <TableCell>{{ fila.tema }}</TableCell>
            <TableCell>{{ fila.horasDuracion }}</TableCell>
            <TableCell>{{ fila.expositorNombre }}</TableCell>
            <TableCell>{{ obraLabel(fila.proyectoId) }}</TableCell>
            <TableCell>{{ asistentesTxt(fila) }}</TableCell>
          </TableRow>
          <TableEmpty v-if="sesiones.length === 0" :colspan="7">No hay capacitaciones en el año.</TableEmpty>
        </TableBody>
      </Table>
    </div>
  </section>
</template>
