<script setup lang="ts">
import { DownloadIcon, PlusIcon, SaveIcon, SearchIcon, Trash2Icon } from '@lucide/vue'
import { computed, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { openAuthenticatedDocument } from '@/services/http'
import { guardarMofPuesto, listarMofPuestos, obtenerMofPuesto } from '@/services/mof/mofService'
import {
  MOF_GRUPO_LABELS,
  MOF_GRUPO_ORDEN,
  mofDocumentoCompletoUrl,
  mofDocumentoUrl,
  type MofPuesto,
  type MofPuestoResumen,
} from '@/types/mof'

const loading = ref(true)
const loadingDetalle = ref(false)
const saving = ref(false)
const puestos = ref<MofPuestoResumen[]>([])
const selectedId = ref<number | null>(null)
const search = ref('')
const detalle = ref<MofPuesto | null>(null)
const proposito = ref('')
const funciones = ref<string[]>([])

const filtrados = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) {
    return puestos.value
  }
  return puestos.value.filter((item) =>
    `${item.cargo} ${item.codigo} ${item.grupo}`.toLowerCase().includes(q),
  )
})

const grupos = computed(() => {
  const map = new Map<string, MofPuestoResumen[]>()
  for (const item of filtrados.value) {
    const lista = map.get(item.grupo) ?? []
    lista.push(item)
    map.set(item.grupo, lista)
  }
  const keys = [
    ...MOF_GRUPO_ORDEN.filter((grupo) => map.has(grupo)),
    ...[...map.keys()].filter((grupo) => !MOF_GRUPO_ORDEN.includes(grupo as (typeof MOF_GRUPO_ORDEN)[number])),
  ]
  return keys.map((grupo) => ({
    grupo,
    label: MOF_GRUPO_LABELS[grupo] ?? grupo,
    items: map.get(grupo) ?? [],
  }))
})

async function loadLista() {
  loading.value = true
  try {
    puestos.value = await listarMofPuestos()
    if (selectedId.value == null && puestos.value.length) {
      selectedId.value = puestos.value[0]!.id
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar los puestos MOF.')
  } finally {
    loading.value = false
  }
}

async function loadDetalle(id: number) {
  loadingDetalle.value = true
  try {
    const next = await obtenerMofPuesto(id)
    detalle.value = next
    proposito.value = next.proposito
    funciones.value = [...next.funciones]
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la ficha MOF.')
    detalle.value = null
  } finally {
    loadingDetalle.value = false
  }
}

watch(selectedId, (id) => {
  if (id != null) {
    void loadDetalle(id)
  }
})

function agregarFuncion() {
  funciones.value.push('')
}

function quitarFuncion(index: number) {
  funciones.value.splice(index, 1)
}

async function guardar() {
  if (selectedId.value == null) {
    return
  }
  const textoProposito = proposito.value.trim()
  if (!textoProposito) {
    toast.error('Indique el propósito del cargo.')
    return
  }
  const textos = funciones.value.map((item) => item.trim()).filter(Boolean)
  if (!textos.length) {
    toast.error('Agregue al menos una función.')
    return
  }
  saving.value = true
  try {
    const saved = await guardarMofPuesto(selectedId.value, {
      proposito: textoProposito,
      funciones: textos,
    })
    detalle.value = saved
    proposito.value = saved.proposito
    funciones.value = [...saved.funciones]
    toast.success('Ficha MOF guardada.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la ficha.')
  } finally {
    saving.value = false
  }
}

function descargarCompleto() {
  void openAuthenticatedDocument(mofDocumentoCompletoUrl())
}

function descargarCargo() {
  if (selectedId.value == null) {
    return
  }
  void openAuthenticatedDocument(mofDocumentoUrl(selectedId.value))
}

onMounted(() => {
  void loadLista()
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          Manual de Organización y Funciones
        </h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
          CVSA-MOF-THSSB · Rev. 00 · Estándar corporativo CONVECSA S.A.C.
        </p>
      </div>
      <Button
        class="h-10 w-full shrink-0 bg-palette-navy px-4 text-white hover:bg-palette-royal sm:w-auto"
        type="button"
        @click="descargarCompleto"
      >
        <DownloadIcon data-icon="inline-start" />
        Exportar MOF completo
      </Button>
    </div>

    <div class="mt-3 grid w-full min-w-0 grid-cols-1 overflow-hidden rounded-lg border border-palette-cyan/70 bg-white md:grid-cols-[minmax(15rem,20rem)_minmax(0,1fr)]">
      <aside class="min-w-0 border-b border-palette-cyan/60 bg-slate-50 md:border-r md:border-b-0">
        <div class="border-b border-slate-200 bg-white p-3">
          <label class="sr-only" for="mof-buscar">Buscar cargo</label>
          <div class="relative">
            <SearchIcon class="pointer-events-none absolute top-2.5 left-2.5 size-4 text-slate-400" />
            <Input
              id="mof-buscar"
              v-model="search"
              class="h-10 bg-white pl-8"
              placeholder="Buscar cargo o código"
            />
          </div>
        </div>
        <div class="max-h-[20rem] space-y-3 overflow-y-auto p-3 md:max-h-[min(70vh,42rem)]">
          <template v-if="loading">
            <Skeleton v-for="row in 8" :key="row" class="h-10 w-full" />
          </template>
          <p v-else-if="!grupos.length" class="px-2 py-8 text-center text-sm text-slate-500">
            No se encontraron cargos.
          </p>
          <div v-for="grupo in grupos" v-else :key="grupo.grupo" class="space-y-1">
            <p class="rounded-md bg-slate-200/70 px-2 py-1 text-[10px] font-bold tracking-wider text-slate-600 uppercase">
              {{ grupo.label }}
            </p>
            <button
              v-for="item in grupo.items"
              :key="item.id"
              type="button"
              class="flex w-full items-center justify-between gap-2 rounded-lg px-3 py-2 text-left text-sm transition-colors"
              :class="item.id === selectedId
                ? 'bg-palette-navy font-semibold text-white'
                : 'text-slate-700 hover:bg-slate-200/80'"
              @click="selectedId = item.id"
            >
              <span class="min-w-0 truncate">{{ item.cargo }}</span>
              <span
                class="shrink-0 rounded px-1.5 py-0.5 font-mono text-[10px] font-semibold"
                :class="item.id === selectedId ? 'bg-white/15 text-white' : 'bg-slate-200 text-slate-500'"
              >
                {{ item.codigo.replace('CVSA-MOF-', '') }}
              </span>
            </button>
          </div>
        </div>
      </aside>

      <div class="min-w-0">
        <div v-if="loadingDetalle && !detalle" class="space-y-4 p-6">
          <Skeleton class="h-16 w-full" />
          <Skeleton class="h-28 w-full" />
          <Skeleton class="h-40 w-full" />
        </div>
        <div v-else-if="detalle" class="space-y-3 p-5 sm:p-6">
          <div class="overflow-hidden rounded-lg border-2 border-slate-800">
            <div class="grid divide-y divide-slate-300 md:grid-cols-12 md:divide-x md:divide-y-0">
              <div class="flex items-center justify-center bg-white p-3 md:col-span-3">
                <span class="text-lg font-black tracking-tight text-palette-royal">CONVECSA</span>
              </div>
              <div class="flex flex-col justify-center bg-slate-50 p-3 text-center md:col-span-6">
                <span class="text-[10px] font-extrabold tracking-widest text-slate-700 uppercase">CONVECSA S.A.C.</span>
                <p class="text-sm font-black tracking-tight text-palette-royal">Manual de Organización y Funciones</p>
                <span class="text-[9px] font-semibold tracking-wider text-slate-500 uppercase">Sistema de Gestión de Calidad (SGC)</span>
              </div>
              <div class="space-y-1 bg-white p-3 text-[10px] text-slate-600 md:col-span-3">
                <div class="flex justify-between border-b border-slate-200 pb-0.5">
                  <span class="font-bold text-slate-500">Código</span>
                  <span class="font-mono font-bold text-slate-800">CVSA-MOF</span>
                </div>
                <div class="flex justify-between border-b border-slate-200 pb-0.5">
                  <span class="font-bold text-slate-500">Versión</span>
                  <span class="font-bold text-slate-800">Rev. 00</span>
                </div>
                <div class="flex justify-between">
                  <span class="font-bold text-slate-500">Fecha</span>
                  <span class="font-bold text-slate-800">01/09/2025</span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-3 border-b border-slate-200 pb-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-md border border-sky-200 bg-sky-50 px-2 py-0.5 font-mono text-xs font-bold text-sky-700">
                  {{ detalle.codigo }}
                </span>
                <span class="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-bold text-slate-600 uppercase">
                  {{ detalle.grupo }}
                </span>
              </div>
              <h3 class="mt-1 text-2xl font-bold text-palette-navy">{{ detalle.cargo }}</h3>
            </div>
            <Button variant="outline" class="h-10 px-3" type="button" @click="descargarCargo">
              <DownloadIcon data-icon="inline-start" />
              Descargar cargo
            </Button>
          </div>

          <div class="space-y-2 rounded-lg border border-sky-200 bg-sky-50/70 p-4">
            <label class="text-xs font-bold tracking-wider text-sky-900 uppercase" for="mof-proposito">
              Propósito general del cargo
            </label>
            <Textarea id="mof-proposito" v-model="proposito" class="min-h-24 bg-white" :disabled="saving" />
          </div>

          <div class="grid gap-2 md:grid-cols-2">
            <div class="rounded-lg border border-slate-200 bg-slate-50 p-3.5">
              <p class="text-[10px] font-bold text-slate-500 uppercase">Superior jerárquico</p>
              <p class="mt-1 text-sm font-semibold text-slate-800">{{ detalle.superior }}</p>
            </div>
            <div class="rounded-lg border border-slate-200 bg-slate-50 p-3.5">
              <p class="text-[10px] font-bold text-slate-500 uppercase">Subordinados directos</p>
              <p class="mt-1 text-sm font-semibold text-slate-800">{{ detalle.subordinados }}</p>
            </div>
          </div>

          <div class="space-y-3">
            <div class="flex items-center justify-between border-b border-slate-200 pb-2">
              <h4 class="text-xs font-bold tracking-wider text-slate-800 uppercase">
                Funciones principales ({{ funciones.length }})
              </h4>
              <Button class="h-8 bg-palette-navy px-3 text-white hover:bg-palette-royal" type="button" :disabled="saving" @click="agregarFuncion">
                <PlusIcon data-icon="inline-start" />
                Agregar función
              </Button>
            </div>
            <div v-if="!funciones.length" class="rounded-lg border border-dashed border-slate-300 px-3 py-6 text-center text-sm text-slate-500">
              No hay funciones. Agregue la primera.
            </div>
            <div v-for="(_, index) in funciones" :key="index" class="flex items-start gap-2 rounded-lg border border-slate-200 bg-slate-50 p-2.5">
              <span class="w-6 pt-2 text-center text-xs font-bold text-palette-royal">
                {{ String.fromCharCode(97 + (index % 26)) }}.
              </span>
              <Textarea v-model="funciones[index]" class="min-h-16 flex-1 bg-white" :disabled="saving" />
              <Button
                variant="ghost"
                size="icon"
                class="mt-1 text-slate-400 hover:text-red-600"
                type="button"
                :disabled="saving"
                :aria-label="`Eliminar función ${index + 1}`"
                @click="quitarFuncion(index)"
              >
                <Trash2Icon />
              </Button>
            </div>
          </div>

          <div class="grid gap-2 pt-1 md:grid-cols-3">
            <div class="rounded-lg border border-sky-200 bg-sky-50/70 p-3.5">
              <p class="border-b border-sky-200 pb-1 text-[11px] font-bold tracking-wide text-sky-800 uppercase">Educación</p>
              <ul class="mt-2 space-y-1.5 text-xs text-slate-700">
                <li v-for="item in detalle.educacion" :key="item" class="flex gap-1.5">
                  <span class="text-sky-500">•</span>
                  <span>{{ item }}</span>
                </li>
              </ul>
            </div>
            <div class="rounded-lg border border-amber-200 bg-amber-50/70 p-3.5">
              <p class="border-b border-amber-200 pb-1 text-[11px] font-bold tracking-wide text-amber-900 uppercase">Experiencia</p>
              <ul class="mt-2 space-y-1.5 text-xs text-amber-950">
                <li v-for="item in detalle.experiencia" :key="item" class="flex gap-1.5">
                  <span class="text-amber-600">•</span>
                  <span>{{ item }}</span>
                </li>
              </ul>
            </div>
            <div class="rounded-lg border border-emerald-200 bg-emerald-50/70 p-3.5">
              <p class="border-b border-emerald-200 pb-1 text-[11px] font-bold tracking-wide text-emerald-900 uppercase">Competencias</p>
              <ul class="mt-2 space-y-1.5 text-xs text-emerald-950">
                <li v-for="item in detalle.competencias" :key="item" class="flex gap-1.5">
                  <span class="text-emerald-600">•</span>
                  <span>{{ item }}</span>
                </li>
              </ul>
            </div>
          </div>

          <div class="flex justify-end border-t border-slate-200 pt-4">
            <Button
              class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
              type="button"
              :disabled="saving || loadingDetalle"
              @click="guardar"
            >
              <SaveIcon data-icon="inline-start" />
              {{ saving ? 'Guardando…' : 'Guardar' }}
            </Button>
          </div>
        </div>
        <p v-else class="p-8 text-center text-sm text-slate-500">Seleccione un cargo del listado.</p>
      </div>
    </div>
  </section>
</template>
