<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  crearPeriodoCc,
  guardarTareo,
  listarPeriodosCc,
  obtenerProyecto,
  obtenerTareo,
} from '@/services/construccion/construccionService'
import {
  CODIGOS_TAREO,
  type CodigoTareoCc,
  type PeriodoCc,
  type Proyecto,
  type TareoCcDia,
  type TareoCcGrilla,
} from '@/types/construccion'

const inputClass =
  'h-8 w-full rounded border border-slate-200 bg-white px-1.5 text-xs text-slate-900 focus:border-sky-600 focus:outline-none focus:ring-1 focus:ring-sky-500/30'
const selectClass = inputClass

const route = useRoute()
const router = useRouter()
const proyectoId = computed(() => Number(route.params.id))
const proyecto = ref<Proyecto | null>(null)
const periodos = ref<PeriodoCc[]>([])
const periodoId = ref<number | null>(null)
const grilla = ref<TareoCcGrilla | null>(null)
const loading = ref(true)
const saving = ref(false)
const creating = ref(false)
const form = reactive({
  fechaDesde: lunesDe(new Date()),
  fechaHasta: domingoDe(new Date()),
})

const periodoActual = computed(() => periodos.value.find((item) => item.id === periodoId.value) ?? null)
const cerrado = computed(() => periodoActual.value?.estado === 'CERRADO')

function lunesDe(fecha: Date): string {
  const copy = new Date(fecha)
  const day = copy.getDay()
  const diff = day === 0 ? -6 : 1 - day
  copy.setDate(copy.getDate() + diff)
  return iso(copy)
}

function domingoDe(fecha: Date): string {
  const lunes = new Date(`${lunesDe(fecha)}T12:00:00`)
  lunes.setDate(lunes.getDate() + 6)
  return iso(lunes)
}

function iso(fecha: Date): string {
  const y = fecha.getFullYear()
  const m = String(fecha.getMonth() + 1).padStart(2, '0')
  const d = String(fecha.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function clave(personaId: number, fecha: string): string {
  return `${personaId}|${fecha}`
}

function diaDe(personaId: number, fecha: string): TareoCcDia | undefined {
  return grilla.value?.dias.find((item) => item.personaId === personaId && item.fecha === fecha)
}

function etiquetaFecha(fecha: string): string {
  const [y, m, d] = fecha.split('-')
  const date = new Date(Number(y), Number(m) - 1, Number(d))
  const dia = date.toLocaleDateString('es-PE', { weekday: 'short' })
  return `${dia} ${d}/${m}`
}

async function loadProyecto() {
  proyecto.value = await obtenerProyecto(proyectoId.value)
}

async function loadPeriodos(preferId?: number) {
  periodos.value = await listarPeriodosCc(proyectoId.value)
  if (preferId && periodos.value.some((item) => item.id === preferId)) {
    periodoId.value = preferId
    return
  }
  if (!periodoId.value && periodos.value.length) {
    periodoId.value = periodos.value[0]?.id ?? null
  }
}

async function loadTareo() {
  if (!periodoId.value) {
    grilla.value = null
    return
  }
  grilla.value = await obtenerTareo(proyectoId.value, periodoId.value)
}

async function load() {
  loading.value = true
  try {
    await loadProyecto()
    await loadPeriodos()
    await loadTareo()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el tareo.')
  } finally {
    loading.value = false
  }
}

watch(periodoId, async (id, prev) => {
  if (id && id !== prev && !loading.value) {
    try {
      await loadTareo()
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'No se pudo cargar el tareo.')
    }
  }
})

onMounted(() => {
  void load()
})

async function onCrearPeriodo() {
  if (!form.fechaDesde || !form.fechaHasta || form.fechaHasta < form.fechaDesde) {
    toast.error('Ingrese un rango de fechas válido.')
    return
  }
  creating.value = true
  try {
    const creado = await crearPeriodoCc(proyectoId.value, {
      fechaDesde: form.fechaDesde,
      fechaHasta: form.fechaHasta,
    })
    toast.success('Periodo creado.')
    await loadPeriodos(creado.id)
    await loadTareo()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo crear el periodo.')
  } finally {
    creating.value = false
  }
}

function onCodigoChange(personaId: number, fecha: string, codigo: string) {
  const dia = diaDe(personaId, fecha)
  if (!dia) {
    return
  }
  dia.codigo = codigo as CodigoTareoCc
  if (codigo === 'F' || codigo === 'DSC') {
    dia.horasOrd = 0
    dia.movilidad = false
  }
}

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
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Proyectos</p>
    <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
      Tareo {{ proyecto ? `· ${proyecto.codigo}` : '' }}
    </h2>
    <p class="mt-3 max-w-3xl text-sm leading-relaxed text-slate-500">
      Marque horas y códigos por día para el personal RCC asignado a esta obra. El domingo se liquida
      como promedio de lunes a sábado.
    </p>

    <div class="mt-5 flex flex-wrap gap-2">
      <Button variant="outline" type="button" @click="router.push({ name: 'proyectos' })">Obras</Button>
      <Button
        variant="outline"
        type="button"
        @click="router.push({ name: 'proyecto-planilla', params: { id: String(proyectoId) } })"
      >
        Planilla
      </Button>
    </div>

    <div class="mt-6 space-y-5 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div class="grid gap-3 sm:grid-cols-3">
          <div>
            <label class="text-sm font-medium text-slate-700" for="tareo-periodo">Periodo</label>
            <select id="tareo-periodo" v-model.number="periodoId" :class="selectClass + ' mt-1.5 h-10'">
              <option :value="null">Sin periodos</option>
              <option v-for="item in periodos" :key="item.id" :value="item.id">
                Sem. {{ item.nroSemana }} · {{ item.fechaDesde }} → {{ item.fechaHasta }}
                ({{ item.estado === 'CERRADO' ? 'Cerrado' : 'Borrador' }})
              </option>
            </select>
          </div>
          <div>
            <label class="text-sm font-medium text-slate-700" for="tareo-desde">Desde</label>
            <input id="tareo-desde" v-model="form.fechaDesde" type="date" :class="inputClass + ' mt-1.5 h-10'" />
          </div>
          <div>
            <label class="text-sm font-medium text-slate-700" for="tareo-hasta">Hasta</label>
            <input id="tareo-hasta" v-model="form.fechaHasta" type="date" :class="inputClass + ' mt-1.5 h-10'" />
          </div>
        </div>
        <div class="flex flex-wrap gap-2">
          <Button variant="outline" type="button" :disabled="creating" @click="onCrearPeriodo">
            Nuevo periodo
          </Button>
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="saving || cerrado || !grilla"
            @click="onGuardar"
          >
            Guardar tareo
          </Button>
        </div>
      </div>

      <p v-if="cerrado" class="text-sm text-amber-700">El periodo está cerrado. El tareo es de solo lectura.</p>

      <div v-if="loading" class="space-y-3">
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-24 w-full" />
      </div>

      <div v-else-if="!grilla" class="text-sm text-slate-500">Cree un periodo para registrar el tareo.</div>

      <div v-else class="overflow-x-auto rounded-lg border border-palette-cyan/60">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead class="sticky left-0 z-10 bg-white min-w-48">Trabajador</TableHead>
              <TableHead v-for="fecha in grilla.fechas" :key="fecha" class="min-w-36 text-center">
                {{ etiquetaFecha(fecha) }}
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableEmpty v-if="!grilla.personal.length" :colspan="1 + grilla.fechas.length">
              No hay personal RCC asignado a esta obra. Asígnalo en el alta de contrato.
            </TableEmpty>
            <TableRow v-for="persona in grilla.personal" v-else :key="persona.personaId">
              <TableCell class="sticky left-0 z-10 bg-white align-top">
                <p class="font-medium">{{ persona.apellidos }}, {{ persona.nombres }}</p>
                <p class="text-xs text-slate-500">{{ persona.numeroDocumento }} · {{ persona.categoria }}</p>
              </TableCell>
              <TableCell v-for="fecha in grilla.fechas" :key="clave(persona.personaId, fecha)" class="align-top">
                <div v-if="diaDe(persona.personaId, fecha)" class="grid gap-1">
                  <select
                    :value="diaDe(persona.personaId, fecha)!.codigo"
                    :disabled="cerrado"
                    :class="selectClass"
                    @change="onCodigoChange(persona.personaId, fecha, ($event.target as HTMLSelectElement).value)"
                  >
                    <option v-for="item in CODIGOS_TAREO" :key="item.codigo" :value="item.codigo">
                      {{ item.label }}
                    </option>
                  </select>
                  <input
                    v-model.number="diaDe(persona.personaId, fecha)!.horasOrd"
                    type="number"
                    min="0"
                    step="0.5"
                    :disabled="cerrado"
                    :class="inputClass"
                    aria-label="Horas ordinarias"
                  />
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.bae" type="checkbox" :disabled="cerrado" />
                    BAE
                  </label>
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.movilidad" type="checkbox" :disabled="cerrado" />
                    Mov.
                  </label>
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.altura" type="checkbox" :disabled="cerrado" />
                    Altura
                  </label>
                  <input
                    v-model.number="diaDe(persona.personaId, fecha)!.pisos"
                    type="number"
                    min="0"
                    :disabled="cerrado"
                    :class="inputClass"
                    placeholder="Pisos"
                    aria-label="Pisos"
                  />
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.altitud" type="checkbox" :disabled="cerrado" />
                    Altitud
                  </label>
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.cotaCero" type="checkbox" :disabled="cerrado" />
                    Cota 0
                  </label>
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.altasTemp" type="checkbox" :disabled="cerrado" />
                    Temp.
                  </label>
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.agua" type="checkbox" :disabled="cerrado" />
                    Agua
                  </label>
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.aguasServidas" type="checkbox" :disabled="cerrado" />
                    A. serv.
                  </label>
                  <label class="flex items-center gap-1 text-[10px] text-slate-600">
                    <input v-model="diaDe(persona.personaId, fecha)!.nocturno" type="checkbox" :disabled="cerrado" />
                    Noct.
                  </label>
                  <input
                    v-model.number="diaDe(persona.personaId, fecha)!.he60"
                    type="number"
                    min="0"
                    step="0.5"
                    :disabled="cerrado"
                    :class="inputClass"
                    placeholder="HE 60%"
                    aria-label="Horas extras 60%"
                  />
                  <input
                    v-model.number="diaDe(persona.personaId, fecha)!.he100"
                    type="number"
                    min="0"
                    step="0.5"
                    :disabled="cerrado"
                    :class="inputClass"
                    placeholder="HE 100%"
                    aria-label="Horas extras 100%"
                  />
                </div>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </div>
  </section>
</template>
