<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
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
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import {
  crearNotaBitacora,
  eliminarNotaBitacora,
  listarAuditoriaObra,
  listarBitacoraObra,
} from '@/services/construccion/construccionService'
import type { AuditoriaObra, BitacoraObra } from '@/types/construccion'
import { fieldInputClass, tabsListClass } from './fieldClasses'

type TabBitacora = 'diario' | 'auditoria'

const ACCION_LABEL: Record<string, string> = {
  GUARDAR_TAREO: 'Guardó tareo',
  CREAR_VERSION_PRESUPUESTO: 'Creó versión de presupuesto',
  GUARDAR_PARTIDAS: 'Guardó partidas',
  VIGENTAR_PRESUPUESTO: 'Vigentó presupuesto',
  IMPORTAR_PRESUPUESTO: 'Importó presupuesto',
  CREAR_VALORIZACION: 'Creó valorización',
  PRESENTAR_VALORIZACION: 'Presentó valorización',
  OBSERVAR_VALORIZACION: 'Observó valorización',
  APROBAR_VALORIZACION: 'Aprobó valorización',
  FACTURAR_VALORIZACION: 'Facturó valorización',
  CEDER_FACTURA: 'Cedió factura',
  ADELANTAR_FACTORING: 'Adelantó factoring',
  COBRAR_FACTORING: 'Cobró factoring',
  IMPAGAR_FACTORING: 'Marcó factoring impago',
  REVERTIR_FACTORING: 'Revirtió factoring',
}

const inputClass = fieldInputClass

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const tab = ref<TabBitacora>('diario')
const loading = ref(true)
const saving = ref(false)
const notas = ref<BitacoraObra[]>([])
const auditoria = ref<AuditoriaObra[]>([])

const form = reactive({
  fecha: hoyIso(),
  texto: '',
})

function hoyIso(): string {
  const ahora = new Date()
  const mes = String(ahora.getMonth() + 1).padStart(2, '0')
  const dia = String(ahora.getDate()).padStart(2, '0')
  return `${ahora.getFullYear()}-${mes}-${dia}`
}

function fechaObraLabel(valor: string): string {
  const partes = valor.split('-')
  if (partes.length !== 3) return valor
  return `${partes[2]}/${partes[1]}/${partes[0]}`
}

function fechaHoraLabel(valor: string): string {
  const fecha = new Date(valor)
  if (Number.isNaN(fecha.getTime())) return valor
  return fecha.toLocaleString('es-PE')
}

function accionLabel(accion: string): string {
  return ACCION_LABEL[accion] ?? accion
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    const [listaNotas, listaAuditoria] = await Promise.all([
      listarBitacoraObra(proyectoId.value),
      listarAuditoriaObra(proyectoId.value),
    ])
    notas.value = listaNotas
    auditoria.value = listaAuditoria
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la bitácora.')
  } finally {
    loading.value = false
  }
}

async function onRegistrar() {
  const texto = form.texto.trim()
  if (!form.fecha) {
    toast.error('Indique la fecha de obra.')
    return
  }
  if (!texto) {
    toast.error('El texto es obligatorio.')
    return
  }
  saving.value = true
  try {
    await crearNotaBitacora(proyectoId.value, { fecha: form.fecha, texto })
    form.texto = ''
    toast.success('Nota registrada.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la nota.')
  } finally {
    saving.value = false
  }
}

async function onEliminar(item: BitacoraObra) {
  if (!window.confirm('¿Quitar esta nota de la bitácora?')) {
    return
  }
  saving.value = true
  try {
    await eliminarNotaBitacora(proyectoId.value, item.id)
    toast.success('Nota eliminada.')
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo eliminar la nota.')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs :model-value="tab" @update:model-value="(value) => (tab = String(value) as TabBitacora)">
      <TabsList :class="tabsListClass">
        <TabsTrigger value="diario" class="h-9 rounded-lg px-4 text-sm">Diario</TabsTrigger>
        <TabsTrigger value="auditoria" class="h-9 rounded-lg px-4 text-sm">Auditoría</TabsTrigger>
      </TabsList>
    </Tabs>

    <div v-if="loading" class="space-y-3">
      <Skeleton class="h-10 w-full" />
      <Skeleton class="h-24 w-full" />
    </div>

    <template v-else-if="tab === 'diario'">
      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <div class="grid gap-3 sm:grid-cols-[10rem_1fr_auto]">
          <Input v-model="form.fecha" :class="inputClass" type="date" />
          <Textarea
            v-model="form.texto"
            class="min-h-16"
            maxlength="4000"
            placeholder="Nota del día de obra"
          />
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal sm:self-start"
            type="button"
            :disabled="saving"
            @click="onRegistrar"
          >
            Registrar
          </Button>
        </div>
      </div>

      <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Usuario</TableHead>
              <TableHead>Fecha de obra</TableHead>
              <TableHead>Texto</TableHead>
              <TableHead></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in notas" :key="item.id">
              <TableCell>{{ item.usuario }}</TableCell>
              <TableCell>{{ fechaObraLabel(item.fecha) }}</TableCell>
              <TableCell class="whitespace-pre-wrap">{{ item.texto }}</TableCell>
              <TableCell class="whitespace-nowrap">
                <Button variant="outline" size="sm" type="button" :disabled="saving" @click="onEliminar(item)">
                  Quitar
                </Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!notas.length" :colspan="4">Sin notas.</TableEmpty>
          </TableBody>
        </Table>
      </div>
    </template>

    <div v-else class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Cuándo</TableHead>
            <TableHead>Usuario</TableHead>
            <TableHead>Acción</TableHead>
            <TableHead>Detalle</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="item in auditoria" :key="item.id">
            <TableCell>{{ fechaHoraLabel(item.createdAt) }}</TableCell>
            <TableCell>{{ item.usuario }}</TableCell>
            <TableCell>{{ accionLabel(item.accion) }}</TableCell>
            <TableCell>{{ item.detalle ?? '—' }}</TableCell>
          </TableRow>
          <TableEmpty v-if="!auditoria.length" :colspan="4">Sin movimientos auditados.</TableEmpty>
        </TableBody>
      </Table>
    </div>
  </div>
</template>
