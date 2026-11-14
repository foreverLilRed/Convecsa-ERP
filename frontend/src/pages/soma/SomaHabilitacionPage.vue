<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { listarProyectos } from '@/services/construccion/construccionService'
import { listarEmosCorporativo } from '@/services/soma/somaService'
import type { Proyecto } from '@/types/construccion'
import type { AptitudEmo, SomaEmo } from '@/types/soma'

const selectClass =
  'mt-1.5 block h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-900 outline-none focus:border-sky-600 focus:ring-2 focus:ring-sky-500/20 disabled:bg-slate-100'

const loading = ref(true)
const error = ref('')
const proyectos = ref<Proyecto[]>([])
const emos = ref<SomaEmo[]>([])
const filtroObra = ref<string>('')

const obrasVisibles = computed(() => new Set(proyectos.value.map((item) => item.id)))
const obraPorId = computed(() => new Map(proyectos.value.map((item) => [item.id, item])))

const filas = computed(() => {
  const obraId = filtroObra.value === '' ? null : Number(filtroObra.value)
  return emos.value.filter((item) => {
    if (item.proyectoId != null && !obrasVisibles.value.has(item.proyectoId)) {
      return false
    }
    if (obraId != null && item.proyectoId !== obraId) {
      return false
    }
    return true
  })
})

function fechaLimaMasDias(dias: number): string {
  const hoy = new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Lima' }).format(new Date())
  const date = new Date(`${hoy}T12:00:00`)
  date.setDate(date.getDate() + dias)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatFecha(value: string | null | undefined): string {
  if (!value) return '—'
  return new Date(`${value}T12:00:00`).toLocaleDateString('es-PE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function obraLabel(proyectoId: number | null): string {
  if (proyectoId == null) return '—'
  const obra = obraPorId.value.get(proyectoId)
  return obra ? `${obra.codigo} — ${obra.nombre}` : String(proyectoId)
}

function aptitudVariant(aptitud: AptitudEmo): 'default' | 'success' | 'secondary' {
  if (aptitud === 'APTO') return 'success'
  if (aptitud === 'APTO_CON_RESTRICCION') return 'default'
  return 'secondary'
}

async function cargar() {
  loading.value = true
  error.value = ''
  try {
    const vencenAntesDe = fechaLimaMasDias(30)
    const [listaObras, listaEmos] = await Promise.all([
      listarProyectos(),
      listarEmosCorporativo(vencenAntesDe),
    ])
    proyectos.value = listaObras
    emos.value = listaEmos
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'No se pudieron cargar los EMO.'
    proyectos.value = []
    emos.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void cargar()
})
</script>

<template>
  <section class="min-w-0 space-y-3">
    <header class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">SOMA corporativo</p>
        <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Vigilancia médica y SCTR</h2>
        <p class="mt-2 text-sm text-slate-500">EMO que vencen en los próximos 30 días en las obras visibles.</p>
      </div>
      <div class="w-full sm:w-72">
        <label class="text-sm font-medium text-slate-700" for="soma-hab-obra">Obra</label>
        <select id="soma-hab-obra" v-model="filtroObra" :class="selectClass">
          <option value="">Todas las obras visibles</option>
          <option v-for="obra in proyectos" :key="obra.id" :value="String(obra.id)">
            {{ obra.codigo }} — {{ obra.nombre }}
          </option>
        </select>
      </div>
    </header>

    <p v-if="error" class="text-sm text-red-700">{{ error }}</p>
    <div v-if="loading" class="space-y-2">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-40 w-full" />
    </div>
    <div v-else class="overflow-x-auto rounded-2xl border border-palette-cyan/70 bg-white">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Obra</TableHead>
            <TableHead>Trabajador</TableHead>
            <TableHead>Tipo</TableHead>
            <TableHead>Vencimiento</TableHead>
            <TableHead>Aptitud</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="fila in filas" :key="fila.id">
            <TableCell>{{ obraLabel(fila.proyectoId) }}</TableCell>
            <TableCell>{{ fila.personaNombre ?? '—' }}</TableCell>
            <TableCell>{{ fila.tipoEmo }}</TableCell>
            <TableCell>{{ formatFecha(fila.fechaVencimiento) }}</TableCell>
            <TableCell>
              <Badge :variant="aptitudVariant(fila.aptitud)">{{ fila.aptitud }}</Badge>
            </TableCell>
          </TableRow>
          <TableEmpty v-if="filas.length === 0" :colspan="5">No hay EMO por vencer en el filtro actual.</TableEmpty>
        </TableBody>
      </Table>
    </div>
  </section>
</template>
