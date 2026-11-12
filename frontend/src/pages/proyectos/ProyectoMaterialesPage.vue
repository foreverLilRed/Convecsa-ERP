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
import { getSession } from '@/services/auth/authService'
import {
  actualizarMaterial,
  crearMaterial,
  listarMateriales,
} from '@/services/construccion/construccionService'
import { esAlmacen } from '@/types/auth'
import type { MaterialCatalogo } from '@/types/construccion'

const inputClass =
  'h-8 rounded-lg border-slate-200 bg-white px-2 text-sm text-slate-900 placeholder-slate-400 focus-visible:border-sky-600 focus-visible:ring-sky-500/20'

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
}

async function onGuardarMaterial() {
  if (!puedeEditar.value) {
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
        <div v-if="puedeEditar" class="mb-4 grid gap-3 sm:grid-cols-6">
          <Input v-model="materialForm.codigo" :class="inputClass" placeholder="Código" />
          <Input v-model="materialForm.descripcion" class="sm:col-span-2" :class="inputClass" placeholder="Descripción" />
          <Input v-model="materialForm.unidad" :class="inputClass" placeholder="Unidad" />
          <Input v-model="materialForm.familia" :class="inputClass" placeholder="Familia" />
          <Input v-model="materialForm.stockMinimo" :class="inputClass" placeholder="Stock mínimo" />
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
              <TableCell v-if="puedeEditar">
                <Button variant="outline" size="sm" type="button" @click="editarMaterial(item)">Editar</Button>
              </TableCell>
            </TableRow>
            <TableEmpty v-if="!materiales.length" :colspan="puedeEditar ? 6 : 5">
              Aún no hay materiales en el catálogo.
            </TableEmpty>
          </TableBody>
        </Table>
      </template>
    </div>
  </section>
</template>
