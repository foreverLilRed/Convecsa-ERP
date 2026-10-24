<script setup lang="ts">
import { ChevronLeftIcon, PrinterIcon } from '@lucide/vue'
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { guardarEvaluacionDesempeno, obtenerColaboradorDesempeno } from '@/services/desempeno/desempenoService'
import {
  COMPETENCIAS,
  estadoEvaluacionLabel,
  iniciales,
  periodoActual,
  periodoLabel,
  promedioCompetencias,
  type ColaboradorDesempenoDetalle,
  type CompetenciaCampo,
  type EstadoEvaluacionDesempeno,
  type EvaluacionDesempeno,
} from '@/types/desempeno'

const inputClass =
  'mt-1.5 block w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const saving = ref(false)
const fotoFallida = ref(false)
const detalle = ref<ColaboradorDesempenoDetalle | null>(null)
const form = reactive({
  seguridadSst: 3,
  productividad: 3,
  calidadTecnica: 3,
  puntualidad: 3,
  trabajoEquipo: 3,
  metaCumplida: 0,
  comentarios: '',
  estado: 'PENDIENTE' as EstadoEvaluacionDesempeno,
})
const snapshot = ref('')

const personaId = computed(() => Number(route.params.personaId))
const periodo = computed(() => {
  const query = route.query.periodo
  return typeof query === 'string' && query.length ? query : periodoActual()
})
const promedio = computed(() => promedioCompetencias(form))
const dirty = computed(() => JSON.stringify(form) !== snapshot.value)
const nombreCompleto = computed(() =>
  detalle.value ? `${detalle.value.apellidos}, ${detalle.value.nombres}` : '',
)

function snapshotFrom(evaluacion: EvaluacionDesempeno) {
  form.seguridadSst = evaluacion.seguridadSst
  form.productividad = evaluacion.productividad
  form.calidadTecnica = evaluacion.calidadTecnica
  form.puntualidad = evaluacion.puntualidad
  form.trabajoEquipo = evaluacion.trabajoEquipo
  form.metaCumplida = evaluacion.metaCumplida
  form.comentarios = evaluacion.comentarios ?? ''
  form.estado = evaluacion.estado
  snapshot.value = JSON.stringify(form)
}

async function load() {
  if (!Number.isFinite(personaId.value)) {
    toast.error('Persona inválida.')
    return
  }
  loading.value = true
  fotoFallida.value = false
  try {
    detalle.value = await obtenerColaboradorDesempeno(personaId.value, periodo.value)
    snapshotFrom(detalle.value.evaluacion)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la evaluación.')
  } finally {
    loading.value = false
  }
}

function setCompetencia(campo: CompetenciaCampo, valor: number) {
  form[campo] = valor
}

async function onGuardar() {
  saving.value = true
  try {
    detalle.value = await guardarEvaluacionDesempeno(personaId.value, {
      periodo: periodo.value,
      seguridadSst: form.seguridadSst,
      productividad: form.productividad,
      calidadTecnica: form.calidadTecnica,
      puntualidad: form.puntualidad,
      trabajoEquipo: form.trabajoEquipo,
      metaCumplida: form.metaCumplida,
      comentarios: form.comentarios,
      estado: form.estado,
    })
    snapshotFrom(detalle.value.evaluacion)
    toast.success('Evaluación guardada.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la evaluación.')
  } finally {
    saving.value = false
  }
}

function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
}

function onImprimir() {
  if (!detalle.value) {
    return
  }
  const filas = COMPETENCIAS.map((item) => {
    const valor = form[item.campo]
    return `<tr><td><strong>${escapeHtml(item.label)}</strong></td><td>${escapeHtml(item.desc)}</td><td style="text-align:center">${valor} / 5</td></tr>`
  }).join('')
  const comentarios = form.comentarios.trim() || 'Sin comentarios.'
  const html = `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Evaluacion_desempeno_${escapeHtml(detalle.value.numeroDocumento)}</title>
  <style>
    @page { size: A4 portrait; margin: 15mm; }
    body { font-family: Arial, sans-serif; font-size: 11px; color: #0f2744; }
    .header { border-bottom: 2px solid #0f2744; padding-bottom: 10px; margin-bottom: 15px; }
    .title { font-size: 16px; font-weight: bold; color: #0f2744; }
    .box { border: 1px solid #8cd2f5; border-radius: 6px; padding: 10px; margin-bottom: 15px; }
    table { width: 100%; border-collapse: collapse; margin-top: 8px; }
    th, td { border: 1px solid #cbd5e1; padding: 6px; text-align: left; }
    th { background-color: #e8f7fd; }
    .score { font-weight: bold; color: #0f2744; font-size: 14px; }
  </style>
</head>
<body>
  <div class="header">
    <div class="title">CONVECSA S.A.C. · Ficha de evaluación de desempeño</div>
    <div>Periodo: ${escapeHtml(periodoLabel(periodo.value))}</div>
  </div>
  <div class="box">
    <strong>Colaborador:</strong> ${escapeHtml(nombreCompleto.value)}<br>
    <strong>Documento:</strong> ${escapeHtml(detalle.value.numeroDocumento)}
    ${detalle.value.cargo ? ` | <strong>Cargo:</strong> ${escapeHtml(detalle.value.cargo)}` : ''}<br>
    ${detalle.value.proyecto ? `<strong>Proyecto / obra:</strong> ${escapeHtml(detalle.value.proyecto)}` : ''}
  </div>
  <div class="box">
    <h3>1. Evaluación de competencias (escala 1 a 5)</h3>
    <table>
      <thead><tr><th>Competencia</th><th>Criterio</th><th style="width:80px;text-align:center">Puntuación</th></tr></thead>
      <tbody>${filas}</tbody>
    </table>
    <p style="margin-top:10px"><strong>Promedio de competencias:</strong> <span class="score">${promedio.value} / 5.0</span></p>
  </div>
  <div class="box">
    <h3>2. Cumplimiento de metas y objetivos (OKR)</h3>
    <p><strong>Porcentaje de cumplimiento:</strong> ${form.metaCumplida}%</p>
    <p><strong>Comentarios de retroalimentación:</strong></p>
    <p style="font-style:italic;background:#f8fafc;padding:8px;border-left:3px solid #1f4ea3">${escapeHtml(comentarios)}</p>
  </div>
  <table style="margin-top:40px;border:none">
    <tr style="border:none">
      <td style="border:none;text-align:center;width:50%">_____________________________<br><strong>Firma del evaluador</strong></td>
      <td style="border:none;text-align:center;width:50%">_____________________________<br><strong>Firma del colaborador</strong></td>
    </tr>
  </table>
</body>
</html>`
  const ventana = window.open('', '_blank', 'noopener,noreferrer')
  if (!ventana) {
    toast.error('El navegador bloqueó la ventana de impresión.')
    return
  }
  ventana.document.write(html)
  ventana.document.close()
  ventana.focus()
  ventana.print()
}

function onBeforeUnload(event: BeforeUnloadEvent) {
  if (dirty.value) {
    event.preventDefault()
    event.returnValue = ''
  }
}

function volver() {
  if (dirty.value && !window.confirm('Hay cambios sin guardar. Si continúa se perderán. ¿Volver al listado?')) {
    return
  }
  router.push({ name: 'gh-kpis', query: { periodo: periodo.value } })
}

watch(() => [personaId.value, periodo.value], () => {
  void load()
})

onMounted(() => {
  window.addEventListener('beforeunload', onBeforeUnload)
  void load()
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', onBeforeUnload)
})
</script>

<template>
  <section>
    <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
      <div>
        <h2 class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
          {{ loading && !detalle ? 'Evaluación' : nombreCompleto }}
        </h2>
        <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-600">
          Competencias laborales, cumplimiento de objetivos y retroalimentación del periodo {{ periodoLabel(periodo) }}.
        </p>
      </div>
      <div class="flex flex-wrap gap-2 sm:mt-7">
        <Button variant="outline" class="h-10 px-4" type="button" @click="volver">
          <ChevronLeftIcon data-icon="inline-start" class="cn-rtl-flip" />
          Colaboradores
        </Button>
        <Button
          variant="outline"
          class="h-10 px-4"
          type="button"
          :disabled="!detalle"
          @click="onImprimir"
        >
          <PrinterIcon data-icon="inline-start" />
          Imprimir ficha
        </Button>
      </div>
    </div>

    <div v-if="loading && !detalle" class="mt-3 space-y-3" aria-busy="true">
      <span class="sr-only">Cargando evaluación</span>
      <Skeleton class="h-28 w-full" />
      <Skeleton class="h-64 w-full" />
    </div>

    <template v-else-if="detalle">
      <div
        v-if="dirty"
        class="mt-4 rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-950"
        role="status"
      >
        Hay cambios sin guardar.
      </div>

      <div class="mt-3 grid gap-2 lg:grid-cols-[minmax(0,1fr)_16rem]">
        <article class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="flex items-start gap-3 border-b border-palette-cyan/50 pb-4">
            <span
              class="flex size-14 shrink-0 items-center justify-center overflow-hidden rounded-full bg-palette-ice text-sm font-bold text-palette-navy"
              aria-hidden="true"
            >
              <img
                v-if="detalle.fotoUrl && !fotoFallida"
                :src="detalle.fotoUrl"
                alt=""
                class="size-14 object-cover"
                @error="fotoFallida = true"
              />
              <span v-else>{{ iniciales(detalle.nombres, detalle.apellidos) }}</span>
            </span>
            <div class="min-w-0">
              <p class="text-xs font-semibold uppercase tracking-wide text-palette-royal">Evaluando a</p>
              <h3 class="mt-1 text-lg font-bold text-palette-navy">{{ nombreCompleto }}</h3>
              <p class="text-sm text-slate-600">
                {{ detalle.numeroDocumento }}
                <template v-if="detalle.cargo"> · {{ detalle.cargo }}</template>
                <template v-if="detalle.proyecto"> · {{ detalle.proyecto }}</template>
              </p>
            </div>
          </div>

          <div class="mt-5 space-y-3">
            <h4 class="text-sm font-semibold text-palette-navy">Competencias laborales y de seguridad</h4>
            <p class="text-sm text-slate-600">Escala de 1 a 5. El promedio se calcula al instante.</p>
            <div
              v-for="item in COMPETENCIAS"
              :key="item.campo"
              class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-3 sm:flex sm:items-center sm:justify-between sm:gap-4"
            >
              <div>
                <p class="text-sm font-semibold text-slate-800">{{ item.label }}</p>
                <p class="text-xs text-slate-600">{{ item.desc }}</p>
              </div>
              <div
                class="mt-2 flex shrink-0 gap-1 sm:mt-0"
                role="group"
                :aria-label="item.label"
              >
                <button
                  v-for="valor in 5"
                  :key="valor"
                  type="button"
                  class="h-8 w-8 rounded-md text-xs font-bold focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/40"
                  :class="valor <= form[item.campo]
                    ? 'bg-amber-400 text-slate-900'
                    : 'border border-slate-300 bg-white text-slate-500 hover:bg-slate-100'"
                  :aria-pressed="valor === form[item.campo]"
                  :aria-label="`${valor} de 5 en ${item.label}`"
                  @click="setCompetencia(item.campo, valor)"
                >
                  {{ valor }}
                </button>
              </div>
            </div>
          </div>

          <div class="mt-5 rounded-lg border border-palette-cyan/60 bg-palette-ice/40 px-4 py-4">
            <div class="flex items-center justify-between gap-3">
              <label class="text-sm font-semibold text-palette-navy" for="desempeno-meta">
                Cumplimiento de objetivos del proyecto (OKR)
              </label>
              <span class="text-sm font-bold tabular-nums text-palette-navy">{{ form.metaCumplida }}%</span>
            </div>
            <input
              id="desempeno-meta"
              v-model.number="form.metaCumplida"
              type="range"
              min="0"
              max="100"
              step="1"
              class="mt-3 w-full accent-palette-royal"
              :aria-valuetext="`${form.metaCumplida} por ciento`"
            />
          </div>

          <div class="mt-5">
            <label class="text-sm font-medium text-slate-700" for="desempeno-comentarios">
              Comentarios y compromisos de mejora
            </label>
            <Textarea
              id="desempeno-comentarios"
              v-model="form.comentarios"
              class="mt-1.5 min-h-24"
              maxlength="4000"
              placeholder="Fortalezas y áreas de mejora observadas"
            />
          </div>

          <div class="mt-5 max-w-xs">
            <label class="text-sm font-medium text-slate-700" for="desempeno-estado">Estado</label>
            <select id="desempeno-estado" v-model="form.estado" :class="inputClass">
              <option value="PENDIENTE">Pendiente</option>
              <option value="COMPLETADA">Completada</option>
            </select>
          </div>

          <div class="mt-3 flex justify-end">
            <Button
              class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
              type="button"
              :disabled="saving || !dirty"
              :aria-busy="saving"
              @click="onGuardar"
            >
              {{ saving ? 'Guardando…' : 'Guardar evaluación' }}
            </Button>
          </div>
        </article>

        <aside class="h-fit rounded-lg border border-palette-cyan/70 bg-white px-4 py-5">
          <p class="text-xs font-semibold uppercase tracking-wide text-slate-600">Puntaje global</p>
          <p class="mt-2 text-3xl font-bold tabular-nums text-palette-navy">
            {{ promedio }}
            <span class="text-base font-medium text-slate-600">/ 5</span>
          </p>
          <p class="mt-2 text-sm font-medium" :class="form.estado === 'COMPLETADA' ? 'text-emerald-800' : 'text-amber-800'">
            {{ estadoEvaluacionLabel(form.estado) }}
          </p>
          <p class="mt-3 text-sm text-slate-600">
            Media de las cinco competencias. El cumplimiento de objetivos ({{ form.metaCumplida }}%) se registra aparte.
          </p>
        </aside>
      </div>
    </template>
  </section>
</template>
