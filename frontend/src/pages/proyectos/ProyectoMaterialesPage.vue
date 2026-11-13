<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
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
import { fieldInputClass, fieldSelectClass } from './fieldClasses'
import { getSession } from '@/services/auth/authService'
import {
  actualizarMaterial,
  crearMaterial,
  listarMateriales,
} from '@/services/construccion/construccionService'
import { esAlmacen } from '@/types/auth'
import type { MaterialCatalogo, TipoEpp } from '@/types/construccion'

const inputClass = `mt-1.5 ${fieldInputClass}`
const selectClass = `mt-1.5 ${fieldSelectClass}`

const TIPOS_EPP: { valor: TipoEpp; label: string }[] = [
  { valor: 'CABEZA', label: 'Cabeza' },
  { valor: 'VISUAL', label: 'Visual' },
  { valor: 'AUDITIVO', label: 'Auditivo' },
  { valor: 'RESPIRATORIO', label: 'Respiratorio' },
  { valor: 'MANOS', label: 'Manos' },
  { valor: 'PIES', label: 'Pies' },
  { valor: 'CAIDAS_ALTURA', label: 'Caídas de altura' },
  { valor: 'CUERPO_ROPA', label: 'Cuerpo / ropa' },
  { valor: 'COLECTIVO', label: 'Colectivo' },
  { valor: 'EMERGENCIA', label: 'Emergencia' },
]

const qtyFmt = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 0, maximumFractionDigits: 4 })
const loading = ref(true)
const saving = ref(false)
const materiales = ref<MaterialCatalogo[]>([])
const puedeEditar = computed(() => !esAlmacen(getSession()?.rol))

const materialForm = reactive({
  id: null as number | null,
  codigo: '',
  descripcion: '',
  unidad: '',
  familia: '',
  stockMinimo: '0',
  critico: false,
  activo: true,
  esEpp: false,
  tipoEpp: '' as TipoEpp | '',
  vidaUtilDias: '',
  normaCertificacion: '',
})

onMounted(() => {
  void load()
})

async function load() {
  loading.value = true
  try {
    materiales.value = await listarMateriales()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el catálogo.')
  } finally {
    loading.value = false
  }
}

function limpiarMaterial() {
  materialForm.id = null
  materialForm.codigo = ''
  materialForm.descripcion = ''
  materialForm.unidad = ''
  materialForm.familia = ''
  materialForm.stockMinimo = '0'
  materialForm.critico = false
  materialForm.activo = true
  materialForm.esEpp = false
  materialForm.tipoEpp = ''
  materialForm.vidaUtilDias = ''
  materialForm.normaCertificacion = ''
}

function editarMaterial(item: MaterialCatalogo) {
  materialForm.id = item.id
  materialForm.codigo = item.codigo
  materialForm.descripcion = item.descripcion
  materialForm.unidad = item.unidad ?? ''
  materialForm.familia = item.familia ?? ''
  materialForm.stockMinimo = String(item.stockMinimo)
  materialForm.critico = item.critico
  materialForm.activo = item.activo
  materialForm.esEpp = item.esEpp
  materialForm.tipoEpp = item.tipoEpp ?? ''
  materialForm.vidaUtilDias = item.vidaUtilDias == null ? '' : String(item.vidaUtilDias)
  materialForm.normaCertificacion = item.normaCertificacion ?? ''
}

async function onGuardarMaterial() {
  if (!puedeEditar.value) {
    return
  }
  if (materialForm.esEpp && !materialForm.tipoEpp) {
    toast.error('Indique el tipo de EPP.')
    return
  }
  saving.value = true
  try {
    const request = {
      codigo: materialForm.codigo,
      descripcion: materialForm.descripcion,
      unidad: materialForm.unidad || null,
      familia: materialForm.familia || null,
      stockMinimo: Number(materialForm.stockMinimo || '0'),
      critico: materialForm.critico,
      activo: materialForm.activo,
      esEpp: materialForm.esEpp,
      tipoEpp: materialForm.esEpp ? (materialForm.tipoEpp as TipoEpp) : null,
      vidaUtilDias: materialForm.esEpp && materialForm.vidaUtilDias ? Number(materialForm.vidaUtilDias) : null,
      normaCertificacion: materialForm.esEpp ? materialForm.normaCertificacion || null : null,
    }
    if (materialForm.id) {
      await actualizarMaterial(materialForm.id, request)
      toast.success('Material actualizado.')
    } else {
      await crearMaterial(request)
      toast.success('Material creado.')
    }
    limpiarMaterial()
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el material.')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section>
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Proyectos</p>
    <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">
      Catálogo de materiales
    </h2>
    <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
      Catálogo de empresa para requerimientos, compras y kardex de cada obra.
    </p>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
      <div v-if="loading" class="space-y-3">
        <Skeleton class="h-10 w-full" />
        <Skeleton class="h-24 w-full" />
      </div>
      <template v-else>
        <div v-if="puedeEditar" class="mb-4 grid gap-3 grid-cols-1 sm:grid-cols-2 xl:grid-cols-3">
          <div>
            <label class="text-sm font-medium text-slate-700" for="mat-codigo">Código</label>
            <Input id="mat-codigo" v-model="materialForm.codigo" :class="inputClass" placeholder="Código" />
          </div>
          <div class="sm:col-span-2">
            <label class="text-sm font-medium text-slate-700" for="mat-desc">Descripción</label>
            <Input id="mat-desc" v-model="materialForm.descripcion" :class="inputClass" placeholder="Descripción" />
          </div>
          <div>
            <label class="text-sm font-medium text-slate-700" for="mat-unidad">Unidad</label>
            <Input id="mat-unidad" v-model="materialForm.unidad" :class="inputClass" placeholder="Unidad" />
          </div>
          <div>
            <label class="text-sm font-medium text-slate-700" for="mat-familia">Familia</label>
            <Input id="mat-familia" v-model="materialForm.familia" :class="inputClass" placeholder="Familia" />
          </div>
          <div>
            <label class="text-sm font-medium text-slate-700" for="mat-minimo">Stock mínimo</label>
            <Input id="mat-minimo" v-model="materialForm.stockMinimo" :class="inputClass" placeholder="0" />
          </div>
          <template v-if="materialForm.esEpp">
            <div>
              <label class="text-sm font-medium text-slate-700" for="mat-tipo-epp">Tipo de EPP</label>
              <select id="mat-tipo-epp" v-model="materialForm.tipoEpp" :class="selectClass">
                <option value="">Seleccione tipo</option>
                <option v-for="tipo in TIPOS_EPP" :key="tipo.valor" :value="tipo.valor">{{ tipo.label }}</option>
              </select>
            </div>
            <div>
              <label class="text-sm font-medium text-slate-700" for="mat-vida-util">Vida útil (días)</label>
              <Input id="mat-vida-util" v-model="materialForm.vidaUtilDias" :class="inputClass" placeholder="Opcional" />
            </div>
            <div>
              <label class="text-sm font-medium text-slate-700" for="mat-norma">Norma / certificación</label>
              <Input id="mat-norma" v-model="materialForm.normaCertificacion" :class="inputClass" placeholder="Opcional" maxlength="80" />
            </div>
          </template>
        </div>
        <div v-if="puedeEditar" class="mb-4 flex flex-wrap items-center gap-3">
          <label class="flex items-center gap-2 text-sm text-slate-600">
            <input v-model="materialForm.critico" type="checkbox" />
            Crítico
          </label>
          <label class="flex items-center gap-2 text-sm text-slate-600">
            <input v-model="materialForm.activo" type="checkbox" />
            Activo
          </label>
          <label class="flex items-center gap-2 text-sm text-slate-600">
            <input v-model="materialForm.esEpp" type="checkbox" />
            Es EPP
          </label>
          <Button
            class="bg-palette-navy text-white hover:bg-palette-royal"
            type="button"
            :disabled="saving"
            @click="onGuardarMaterial"
          >
            {{ materialForm.id ? 'Actualizar' : 'Crear material' }}
          </Button>
          <Button v-if="materialForm.id" variant="outline" type="button" @click="limpiarMaterial">Nuevo</Button>
        </div>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead>Unidad</TableHead>
              <TableHead>Familia</TableHead>
              <TableHead>Mínimo</TableHead>
              <TableHead>EPP</TableHead>
              <TableHead v-if="puedeEditar"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="item in materiales" :key="item.id">
              <TableCell>{{ item.codigo }}</TableCell>
              <TableCell>{{ item.descripcion }}</TableCell>
              <TableCell>{{ item.unidad }}</TableCell>
              <TableCell>{{ item.familia }}</TableCell>
              <TableCell>{{ qtyFmt.format(item.stockMinimo) }}</TableCell>
              <TableCell>{{ item.esEpp ? (TIPOS_EPP.find((tipo) => tipo.valor === item.tipoEpp)?.label ?? 'Sí') : '—' }}</TableCell>
              <TableCell v-if="puedeEditar">
                <Button variant="outline" size="sm" type="button" @click="editarMaterial(item)">Editar</Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!materiales.length" :colspan="puedeEditar ? 7 : 6">
              Aún no hay materiales en el catálogo.
            </TableEmpty>
          </TableBody>
        </Table>
      </template>
    </div>
  </section>
</template>
