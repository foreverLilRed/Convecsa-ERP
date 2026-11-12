<script setup lang="ts">
import { CheckIcon, ChevronsUpDownIcon } from '@lucide/vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Combobox,
  ComboboxAnchor,
  ComboboxEmpty,
  ComboboxGroup,
  ComboboxInput,
  ComboboxItem,
  ComboboxItemIndicator,
  ComboboxList,
  ComboboxTrigger,
} from '@/components/ui/combobox'
import { Input } from '@/components/ui/input'
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import { listarPersonalObra, subirDocumentoObra } from '@/services/construccion/construccionService'
import { listarEmosObra, obtenerResumenSoma, registrarEmoObra } from '@/services/soma/somaService'
import type { PersonalObra } from '@/types/construccion'
import type { AptitudEmo, SomaEmo, SomaResumenObra, TipoEmo } from '@/types/soma'
import { fieldInputClass, fieldSelectClass, tabsListClass } from './fieldClasses'

type TabSoma = 'resumen' | 'vigilancia'

const inputClass = fieldInputClass
const selectClass = fieldSelectClass
const tabTriggerClass =
  'h-9 rounded-lg px-4 text-sm text-slate-500 data-active:bg-white data-active:text-palette-navy data-active:shadow-sm'

const TIPOS_EMO: { valor: TipoEmo; label: string }[] = [
  { valor: 'INGRESO', label: 'Ingreso' },
  { valor: 'PERIODICO', label: 'Periódico' },
  { valor: 'RETIRO', label: 'Retiro' },
  { valor: 'CAMBIO_PUESTO', label: 'Cambio de puesto' },
  { valor: 'REINCORPORACION', label: 'Reincorporación' },
]

const APTITUDES: { valor: AptitudEmo; label: string }[] = [
  { valor: 'APTO', label: 'Apto' },
  { valor: 'APTO_CON_RESTRICCION', label: 'Apto con restricción' },
  { valor: 'OBSERVADO', label: 'Observado' },
  { valor: 'NO_APTO', label: 'No apto' },
]

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const tab = ref<TabSoma>('resumen')
const loading = ref(true)
const saving = ref(false)
const resumen = ref<SomaResumenObra | null>(null)
const emos = ref<SomaEmo[]>([])
const personal = ref<PersonalObra[]>([])
const personaSearch = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

const form = reactive({
  personaId: null as number | null,
  tipoEmo: 'INGRESO' as TipoEmo,
  aptitud: 'APTO' as AptitudEmo,
  clinicaNombre: '',
  fechaExamen: hoyIso(),
  fechaVencimiento: masUnAnio(hoyIso()),
  restricciones: '',
})

const personaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === form.personaId) ?? null,
)

const opcionesPersona = computed(() => {
  const q = personaSearch.value.trim().toLowerCase()
  const lista = q
    ? personal.value.filter((item) =>
        `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
      )
    : personal.value
  const seleccion = personaSeleccionada.value
  if (seleccion && !lista.some((item) => item.personaId === seleccion.personaId)) {
    return [seleccion, ...lista]
  }
  return lista
})

function hoyIso(): string {
  const fecha = new Date()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}`
}

function masUnAnio(iso: string): string {
  const [anio, mes, dia] = iso.split('-').map(Number)
  if (!anio || !mes || !dia) {
    return iso
  }
  return `${anio + 1}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`
}

function personaLabel(persona: PersonalObra | null): string {
  if (!persona) {
    return ''
  }
  return `${persona.apellidos}, ${persona.nombres} · ${persona.numeroDocumento}`
}

function tipoLabel(tipo: TipoEmo): string {
  return TIPOS_EMO.find((item) => item.valor === tipo)?.label ?? tipo
}

function aptitudLabel(aptitud: AptitudEmo): string {
  return APTITUDES.find((item) => item.valor === aptitud)?.label ?? aptitud
}

function fechaLabel(valor: string | null | undefined): string {
  if (!valor) {
    return '—'
  }
  const [anio, mes, dia] = valor.split('-')
  if (!anio || !mes || !dia) {
    return valor
  }
  return `${dia}/${mes}/${anio}`
}

function emoVencido(item: SomaEmo): boolean {
  return item.fechaVencimiento < hoyIso()
}

function aptitudBadgeVariant(item: SomaEmo): 'success' | 'default' | 'secondary' {
  if (emoVencido(item) || item.aptitud === 'NO_APTO' || item.aptitud === 'OBSERVADO') {
    return 'secondary'
  }
  if (item.aptitud === 'APTO') {
    return 'success'
  }
  return 'default'
}

function uniquePersonal(lista: PersonalObra[]): PersonalObra[] {
  const vistos = new Set<number>()
  return lista.filter((item) => {
    if (vistos.has(item.personaId)) {
      return false
    }
    vistos.add(item.personaId)
    return true
  })
}

function resetForm() {
  form.personaId = null
  form.tipoEmo = 'INGRESO'
  form.aptitud = 'APTO'
  form.clinicaNombre = ''
  form.fechaExamen = hoyIso()
  form.fechaVencimiento = masUnAnio(hoyIso())
  form.restricciones = ''
  personaSearch.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function onPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  form.personaId = Number.isFinite(id) ? id : null
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    const [resumenObra, listaEmos, listaPersonal] = await Promise.all([
      obtenerResumenSoma(proyectoId.value),
      listarEmosObra(proyectoId.value),
      listarPersonalObra(proyectoId.value),
    ])
    resumen.value = resumenObra
    emos.value = listaEmos
    personal.value = uniquePersonal(listaPersonal)
  } catch (err) {
    resumen.value = null
    emos.value = []
    personal.value = []
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar SOMA.')
  } finally {
    loading.value = false
  }
}

async function onRegistrar() {
  if (!form.personaId) {
    toast.error('Seleccione una persona del personal de la obra.')
    return
  }
  if (!form.clinicaNombre.trim()) {
    toast.error('Indique el nombre de la clínica.')
    return
  }
  if (!form.fechaExamen || !form.fechaVencimiento) {
    toast.error('Indique las fechas de examen y vencimiento.')
    return
  }
  if (form.fechaVencimiento < form.fechaExamen) {
    toast.error('La fecha de vencimiento no puede ser menor que la de examen.')
    return
  }
  saving.value = true
  try {
    const archivo = fileInput.value?.files?.[0]
    let documentoObraId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'CERTIFICADO_EMO', archivo)
      documentoObraId = documento.id
    }
    await registrarEmoObra(proyectoId.value, {
      personaId: form.personaId,
      tipoEmo: form.tipoEmo,
      clinicaNombre: form.clinicaNombre.trim(),
      fechaExamen: form.fechaExamen,
      fechaVencimiento: form.fechaVencimiento,
      aptitud: form.aptitud,
      restricciones: form.restricciones.trim() || null,
      documentoObraId,
    })
    toast.success('EMO registrado.')
    resetForm()
    emos.value = await listarEmosObra(proyectoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el EMO.')
  } finally {
    saving.value = false
  }
}

watch(proyectoId, () => {
  void load()
})

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs v-model="tab">
      <TabsList :class="tabsListClass">
        <TabsTrigger value="resumen" :class="tabTriggerClass">Resumen</TabsTrigger>
        <TabsTrigger value="vigilancia" :class="tabTriggerClass">Vigilancia médica</TabsTrigger>
      </TabsList>

      <TabsContent value="resumen" class="mt-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-16 w-full" />
          </div>
        </div>
        <div v-else class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <h3 class="text-base font-semibold text-palette-navy">Resumen SOMA</h3>
          <dl class="mt-4 grid gap-4 sm:grid-cols-2">
            <div>
              <dt class="text-sm font-medium text-slate-500">Obra</dt>
              <dd class="mt-1 text-sm text-slate-900">
                {{ resumen ? `${resumen.codigoObra} · ${resumen.nombreObra}` : '—' }}
              </dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">Supervisor SSOMA</dt>
              <dd class="mt-1 text-sm text-slate-900">{{ resumen?.supervisorSsoma ?? 'Sin asignar' }}</dd>
            </div>
          </dl>
        </div>
      </TabsContent>

      <TabsContent value="vigilancia" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar EMO</h3>
            <p class="mt-1 text-sm text-slate-500">
              El certificado CAMO se sube como documento de obra. Solo el último examen sincroniza la aptitud de la persona.
            </p>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2" @submit.prevent="onRegistrar">
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-persona">Persona</label>
                <Combobox
                  :model-value="form.personaId ?? undefined"
                  ignore-filter
                  class="min-w-0 w-full"
                  @update:model-value="onPersonaChange"
                >
                  <ComboboxAnchor class="min-w-0 w-full">
                    <ComboboxTrigger as-child>
                      <Button
                        id="soma-emo-persona"
                        variant="outline"
                        class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                        type="button"
                      >
                        <span
                          class="min-w-0 flex-1 truncate text-left"
                          :class="personaLabel(personaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                        >
                          {{ personaLabel(personaSeleccionada) || 'Buscar personal de la obra' }}
                        </span>
                        <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                      </Button>
                    </ComboboxTrigger>
                  </ComboboxAnchor>
                  <ComboboxList
                    class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)"
                    align="start"
                  >
                    <ComboboxInput v-model="personaSearch" placeholder="Buscar persona..." autocomplete="off" />
                    <ComboboxEmpty>Sin personal en esta obra.</ComboboxEmpty>
                    <ComboboxGroup>
                      <ComboboxItem
                        v-for="persona in opcionesPersona"
                        :key="persona.personaId"
                        :value="persona.personaId"
                      >
                        {{ persona.apellidos }}, {{ persona.nombres }} · {{ persona.numeroDocumento }}
                        <ComboboxItemIndicator>
                          <CheckIcon />
                        </ComboboxItemIndicator>
                      </ComboboxItem>
                    </ComboboxGroup>
                  </ComboboxList>
                </Combobox>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-tipo">Tipo</label>
                <select id="soma-emo-tipo" v-model="form.tipoEmo" :class="selectClass" class="mt-1.5">
                  <option v-for="item in TIPOS_EMO" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-aptitud">Aptitud</label>
                <select id="soma-emo-aptitud" v-model="form.aptitud" :class="selectClass" class="mt-1.5">
                  <option v-for="item in APTITUDES" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                </select>
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-clinica">Clínica</label>
                <Input
                  id="soma-emo-clinica"
                  v-model="form.clinicaNombre"
                  :class="inputClass"
                  class="mt-1.5"
                  maxlength="160"
                  required
                />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-examen">Fecha de examen</label>
                <Input
                  id="soma-emo-examen"
                  v-model="form.fechaExamen"
                  type="date"
                  :class="inputClass"
                  class="mt-1.5"
                  required
                />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-emo-vence">Vencimiento</label>
                <Input
                  id="soma-emo-vence"
                  v-model="form.fechaVencimiento"
                  type="date"
                  :class="inputClass"
                  class="mt-1.5"
                  required
                />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-restricciones">Restricciones</label>
                <Textarea id="soma-emo-restricciones" v-model="form.restricciones" class="mt-1.5 min-h-16" maxlength="2000" />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-emo-camo">Certificado CAMO</label>
                <input
                  id="soma-emo-camo"
                  ref="fileInput"
                  class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                >
              </div>
              <div class="flex justify-end sm:col-span-2">
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="saving"
                >
                  Registrar EMO
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="mb-3 text-base font-semibold text-palette-navy">Historial de EMO</h3>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Persona</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Aptitud</TableHead>
                  <TableHead>Examen</TableHead>
                  <TableHead>Vence</TableHead>
                  <TableHead>Clínica</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="item in emos" :key="item.id">
                  <TableCell>{{ item.personaNombre ?? '—' }}</TableCell>
                  <TableCell>{{ tipoLabel(item.tipoEmo) }}</TableCell>
                  <TableCell>
                    <div class="flex flex-wrap gap-1">
                      <Badge :variant="aptitudBadgeVariant(item)">{{ aptitudLabel(item.aptitud) }}</Badge>
                      <Badge v-if="emoVencido(item)" variant="secondary">Vencido</Badge>
                    </div>
                  </TableCell>
                  <TableCell>{{ fechaLabel(item.fechaExamen) }}</TableCell>
                  <TableCell>{{ fechaLabel(item.fechaVencimiento) }}</TableCell>
                  <TableCell>
                    <p>{{ item.clinicaNombre }}</p>
                    <p v-if="item.restricciones" class="text-xs text-slate-500">{{ item.restricciones }}</p>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!emos.length" :colspan="6">Sin exámenes médicos registrados en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>
    </Tabs>
  </div>
</template>
