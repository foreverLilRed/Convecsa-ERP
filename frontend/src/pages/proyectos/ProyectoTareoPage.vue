<script setup lang="ts">
import { ChevronLeftIcon, ChevronRightIcon } from '@lucide/vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import ParteDiarioRccGrid from '@/components/proyectos/ParteDiarioRccGrid.vue'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  crearPeriodoCc,
  guardarTareo,
  listarPeriodosCc,
  obtenerTareo,
} from '@/services/construccion/construccionService'
import type { PeriodoCc, TareoCcGrilla } from '@/types/construccion'
import { mesesCatalogo, periodoLabel } from '@/types/nomina'
import { fieldSelectClass } from './fieldClasses'

const selectClass = fieldSelectClass

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const periodos = ref<PeriodoCc[]>([])
const periodoId = ref<number | null>(null)
const grilla = ref<TareoCcGrilla | null>(null)
const loading = ref(true)
const saving = ref(false)
const listo = ref(false)

const hoy = new Date()
const mes = ref(hoy.getMonth() + 1)
const anio = ref(hoy.getFullYear())
const anioInput = ref<string | number>(hoy.getFullYear())
const meses = mesesCatalogo()
let cargaSeq = 0

const periodoActual = computed(() => periodos.value.find((item) => item.id === periodoId.value) ?? null)
const cerrado = computed(() => periodoActual.value?.estado === 'CERRADO')
const tituloPeriodo = computed(() => periodoLabel(anio.value, mes.value))

function iso(fecha: Date): string {
  const y = fecha.getFullYear()
  const m = String(fecha.getMonth() + 1).padStart(2, '0')
  const d = String(fecha.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function primerDia(anioSel: number, mesSel: number): string {
  return iso(new Date(anioSel, mesSel - 1, 1))
}

function ultimoDia(anioSel: number, mesSel: number): string {
  return iso(new Date(anioSel, mesSel, 0))
}

function periodoMensualDe(lista: PeriodoCc[], anioSel: number, mesSel: number): PeriodoCc | undefined {
  const desde = primerDia(anioSel, mesSel)
  return lista.find((item) => item.cadencia === 'MENSUAL' && item.fechaDesde === desde)
}

function commitAnio() {
  const n = Number(anioInput.value)
  if (!Number.isFinite(n) || n < 2000 || n > 2100) {
    anioInput.value = anio.value
    return
  }
  anio.value = n
}

function cambiarMes(delta: number) {
  const siguiente = new Date(anio.value, mes.value - 1 + delta, 1)
  anio.value = siguiente.getFullYear()
  mes.value = siguiente.getMonth() + 1
  anioInput.value = anio.value
}

async function loadPeriodos(preferId?: number) {
  periodos.value = await listarPeriodosCc(proyectoId.value)
  if (preferId && periodos.value.some((item) => item.id === preferId)) {
    periodoId.value = preferId
  }
}

async function loadTareo() {
  if (!periodoId.value) {
    grilla.value = null
    return
  }
  grilla.value = await obtenerTareo(proyectoId.value, periodoId.value)
}

async function asegurarPeriodoMensual() {
  const seq = ++cargaSeq
  const anioSel = anio.value
  const mesSel = mes.value
  const vigente = () => seq === cargaSeq && anio.value === anioSel && mes.value === mesSel
  const existente = periodoMensualDe(periodos.value, anioSel, mesSel)
  if (existente) {
    if (!vigente()) {
      return
    }
    periodoId.value = existente.id
    await loadTareo()
    return
  }
  try {
    const creado = await crearPeriodoCc(proyectoId.value, {
      fechaDesde: primerDia(anioSel, mesSel),
      fechaHasta: ultimoDia(anioSel, mesSel),
      cadencia: 'MENSUAL',
    })
    if (!vigente()) {
      return
    }
    await loadPeriodos(creado.id)
    if (!vigente()) {
      return
    }
    await loadTareo()
  } catch (err) {
    if (!vigente()) {
      return
    }
    await loadPeriodos()
    const recuperado = periodoMensualDe(periodos.value, anioSel, mesSel)
    if (recuperado) {
      periodoId.value = recuperado.id
      await loadTareo()
      return
    }
    periodoId.value = null
    grilla.value = null
    toast.error(err instanceof Error ? err.message : 'No se pudo crear el periodo mensual.')
  }
}

async function load() {
  loading.value = true
  try {
    await loadPeriodos()
    await asegurarPeriodoMensual()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el tareo.')
  } finally {
    loading.value = false
    listo.value = true
  }
}

watch([anio, mes], async () => {
  if (!listo.value) {
    return
  }
  loading.value = true
  try {
    await asegurarPeriodoMensual()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el tareo.')
  } finally {
    loading.value = false
  }
})

onMounted(() => {
  void load()
})

async function onGuardar() {
  if (!periodoId.value || !grilla.value) {
    return
  }
  saving.value = true
  try {
    const dias = grilla.value.dias.map((item) => ({
      ...item,
      horasOrd: Number(item.horasOrd) || 0,
      he60: Number(item.he60) || 0,
      he100: Number(item.he100) || 0,
      pisos: Number(item.pisos) || 0,
    }))
    grilla.value = await guardarTareo(proyectoId.value, periodoId.value, { dias })
    toast.success('Tareo guardado.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el tareo.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section>
    <div class="space-y-5 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div class="flex flex-wrap items-end gap-2">
          <Button
            variant="outline"
            size="icon"
            class="h-8 w-8"
            type="button"
            :disabled="loading || saving"
            aria-label="Mes anterior"
            @click="cambiarMes(-1)"
          >
            <ChevronLeftIcon class="cn-rtl-flip size-4" />
          </Button>
          <div>
            <label class="text-[10px] font-medium text-slate-600" for="tareo-mes">Mes</label>
            <select
              id="tareo-mes"
              v-model.number="mes"
              :class="selectClass"
              class="mt-0.5 min-w-36"
              :disabled="loading || saving"
            >
              <option v-for="item in meses" :key="item.value" :value="item.value">{{ item.label }}</option>
            </select>
          </div>
          <div>
            <label class="text-[10px] font-medium text-slate-600" for="tareo-anio">Año</label>
            <Input
              id="tareo-anio"
              v-model="anioInput"
              class="mt-0.5 w-20"
              type="number"
              min="2000"
              max="2100"
              :disabled="loading || saving"
              @change="commitAnio"
              @blur="commitAnio"
            />
          </div>
          <Button
            variant="outline"
            size="icon"
            class="h-8 w-8"
            type="button"
            :disabled="loading || saving"
            aria-label="Mes siguiente"
            @click="cambiarMes(1)"
          >
            <ChevronRightIcon class="cn-rtl-flip size-4" />
          </Button>
          <p class="pb-1 text-sm font-medium text-palette-navy">{{ tituloPeriodo }}</p>
        </div>
        <Button
          class="bg-palette-navy text-white hover:bg-palette-royal"
          type="button"
          :disabled="saving || cerrado || !grilla"
          @click="onGuardar"
        >
          Guardar tareo
        </Button>
      </div>

      <p v-if="cerrado" class="text-sm text-amber-700">El periodo está cerrado. El tareo es de solo lectura.</p>

      <div v-if="loading && !grilla" class="space-y-3">
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-24 w-full" />
      </div>

      <div v-else-if="!grilla" class="text-sm text-slate-500">
        No hay un periodo mensual para {{ tituloPeriodo }}.
      </div>

      <ParteDiarioRccGrid
        v-else
        :grilla="grilla"
        :cerrado="cerrado"
        :loading="loading"
        :anio="anio"
        :mes="mes"
      />
    </div>
  </section>
</template>
