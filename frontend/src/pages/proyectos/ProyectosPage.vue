<script setup lang="ts">
import { PlusIcon, XIcon } from '@lucide/vue'
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
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
  crearProyecto,
  listarProyectos,
  obtenerCatalogosConstruccion,
} from '@/services/construccion/construccionService'
import type { ConstruccionCatalogos, Proyecto } from '@/types/construccion'

const inputClass =
  'mt-1.5 block h-10 min-w-0 w-full rounded-lg border border-slate-200 bg-white px-3 py-0 text-sm text-slate-900 placeholder-slate-400 focus:border-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-500/20'

const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const router = useRouter()
const loading = ref(true)
const proyectos = ref<Proyecto[]>([])
const catalogos = ref<ConstruccionCatalogos | null>(null)
const modalOpen = ref(false)
const submitting = ref(false)
const formError = ref('')
const form = reactive({
  codigo: '',
  nombre: '',
  ubicacion: '',
  centroCosto: '',
  valorObra: '',
})

const activos = computed(() => proyectos.value.filter((item) => item.activo).length)
const hayAlertaUit = computed(() => proyectos.value.some((item) => item.alertaUit))
const umbralUit = computed(() => catalogos.value?.umbralUit ?? 275000)
const uitAnio = computed(() => catalogos.value?.uitAnio ?? proyectos.value[0]?.uitAnio ?? new Date().getFullYear())
const formAlertaUit = computed(() => {
  const valor = Number(form.valorObra.replace(',', '.'))
  return form.valorObra.trim() !== '' && Number.isFinite(valor) && valor > umbralUit.value
})

async function load() {
  loading.value = true
  try {
    const [lista, cats] = await Promise.all([listarProyectos(), obtenerCatalogosConstruccion()])
    proyectos.value = lista
    catalogos.value = cats
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las obras.')
  } finally {
    loading.value = false
  }
}

function formatValor(valor: number | null): string {
  return valor == null ? '—' : money.format(valor)
}

function lockBodyScroll() {
  const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth
  document.body.style.overflow = 'hidden'
  document.body.style.paddingRight = scrollbarWidth > 0 ? `${scrollbarWidth}px` : ''
}

function unlockBodyScroll() {
  document.body.style.overflow = ''
  document.body.style.paddingRight = ''
}

function onDocumentKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && modalOpen.value && !submitting.value) {
    modalOpen.value = false
  }
}

watch(modalOpen, (isOpen) => {
  if (isOpen) {
    form.codigo = ''
    form.nombre = ''
    form.ubicacion = ''
    form.centroCosto = ''
    form.valorObra = ''
    formError.value = ''
    submitting.value = false
    lockBodyScroll()
    document.addEventListener('keydown', onDocumentKeydown)
  } else {
    document.removeEventListener('keydown', onDocumentKeydown)
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onDocumentKeydown)
  unlockBodyScroll()
})

onMounted(() => {
  void load()
})

async function onCrear() {
  formError.value = ''
  if (!form.codigo.trim() || !form.nombre.trim()) {
    formError.value = 'Ingrese código y nombre de la obra.'
    toast.error(formError.value)
    return
  }
  const valorTexto = form.valorObra.trim().replace(',', '.')
  let valorObra: number | null = null
  if (valorTexto) {
    valorObra = Number(valorTexto)
    if (!Number.isFinite(valorObra) || valorObra < 0) {
      formError.value = 'El valor de obra debe ser un monto válido.'
      toast.error(formError.value)
      return
    }
  }
  submitting.value = true
  try {
    const creado = await crearProyecto({
      codigo: form.codigo.trim(),
      nombre: form.nombre.trim(),
      ubicacion: form.ubicacion.trim() || null,
      centroCosto: form.centroCosto.trim() || null,
      valorObra,
    })
    if (creado.alertaUit) {
      toast.warning(
        `Obra registrada. El valor supera 50 UIT (${creado.uitAnio}); es una alerta, no un bloqueo.`,
      )
    } else {
      toast.success('Obra registrada.')
    }
    modalOpen.value = false
    await load()
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear la obra.'
    formError.value = message
    toast.error(message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section>
    <p class="text-xs font-semibold uppercase tracking-widest text-palette-royal">Proyectos</p>
    <h2 class="mt-2 text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Obras</h2>
    <p class="mt-3 max-w-2xl text-sm leading-relaxed text-slate-500">
      Obras de construcción civil para tareo semanal y liquidación de planilla RCC. El personal se
      asigna al registrar un contrato del régimen Construcción Civil.
    </p>

    <div
      v-if="hayAlertaUit"
      class="mt-4 rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-950"
    >
      Hay obras cuyo valor supera 50 UIT ({{ uitAnio }}). Es una alerta informativa: no bloquea el
      tareo ni la liquidación.
    </div>

    <div class="mt-6 rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6 sm:py-6">
      <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p class="text-sm text-slate-500">
          {{ loading ? 'Cargando…' : `${activos} obra${activos === 1 ? '' : 's'} activa${activos === 1 ? '' : 's'}` }}
        </p>
        <Button
          class="h-10 bg-palette-navy px-4 text-white hover:bg-palette-royal"
          type="button"
          @click="modalOpen = true"
        >
          <PlusIcon data-icon="inline-start" />
          Nueva obra
        </Button>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Código</TableHead>
            <TableHead>Nombre</TableHead>
            <TableHead>Ubicación</TableHead>
            <TableHead>Centro de costo</TableHead>
            <TableHead>Valor de obra</TableHead>
            <TableHead>Estado</TableHead>
            <TableHead class="text-right">Acciones</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-if="loading">
            <TableCell colspan="7"><Skeleton class="h-8 w-full" /></TableCell>
          </TableRow>
          <TableEmpty v-else-if="proyectos.length === 0" :colspan="7">
            No hay obras registradas.
          </TableEmpty>
          <TableRow v-for="proyecto in proyectos" v-else :key="proyecto.id">
            <TableCell class="font-medium">{{ proyecto.codigo }}</TableCell>
            <TableCell>{{ proyecto.nombre }}</TableCell>
            <TableCell>{{ proyecto.ubicacion || '—' }}</TableCell>
            <TableCell>
              {{ formatValor(proyecto.valorObra) }}
              <span
                v-if="proyecto.alertaUit"
                class="ml-2 inline-flex rounded bg-amber-100 px-1.5 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-amber-800"
              >
                UIT &gt; 50
              </span>
            </TableCell>
            <TableCell>{{ proyecto.activo ? 'Activa' : 'Inactiva' }}</TableCell>
            <TableCell class="text-right">
              <div class="flex justify-end gap-2">
                <Button
                  variant="outline"
                  class="h-8 px-3 text-xs"
                  type="button"
                  @click="router.push({ name: 'proyecto-tareo', params: { id: String(proyecto.id) } })"
                >
                  Tareo
                </Button>
                <Button
                  variant="outline"
                  class="h-8 px-3 text-xs"
                  type="button"
                  @click="router.push({ name: 'proyecto-planilla', params: { id: String(proyecto.id) } })"
                >
                  Planilla
                </Button>
              </div>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <Teleport to="body">
      <Transition name="modal" :duration="{ enter: 240, leave: 220 }" @after-leave="unlockBodyScroll">
        <div v-if="modalOpen" class="fixed inset-0 z-50 overflow-y-auto">
          <div class="fixed inset-0 bg-palette-navy/40" @click="!submitting && (modalOpen = false)" />
          <div class="relative flex min-h-full items-center justify-center p-4">
            <section
              class="relative z-10 w-full max-w-lg rounded-2xl border border-palette-cyan/70 bg-white shadow-lg"
              role="dialog"
              aria-modal="true"
            >
              <header class="flex items-center justify-between border-b border-palette-cyan/70 px-5 py-4">
                <h2 class="text-lg font-bold text-palette-navy">Nueva obra</h2>
                <Button variant="ghost" size="icon" type="button" :disabled="submitting" @click="modalOpen = false">
                  <XIcon />
                </Button>
              </header>
              <form class="grid gap-4 px-5 py-5" @submit.prevent="onCrear">
                <p v-if="formError" class="text-sm text-red-600">{{ formError }}</p>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="obra-codigo">Código</label>
                  <input id="obra-codigo" v-model="form.codigo" maxlength="20" :class="inputClass" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="obra-nombre">Nombre</label>
                  <input id="obra-nombre" v-model="form.nombre" maxlength="160" :class="inputClass" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="obra-ubicacion">Ubicación</label>
                  <input id="obra-ubicacion" v-model="form.ubicacion" maxlength="120" :class="inputClass" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="obra-cc">Centro de costo</label>
                  <input id="obra-cc" v-model="form.centroCosto" maxlength="40" :class="inputClass" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="obra-valor">Valor de obra (S/)</label>
                  <input
                    id="obra-valor"
                    v-model="form.valorObra"
                    inputmode="decimal"
                    placeholder="Opcional"
                    :class="inputClass"
                  />
                  <p v-if="formAlertaUit" class="mt-1.5 text-xs text-amber-800">
                    El valor supera 50 UIT ({{ money.format(umbralUit) }} en {{ uitAnio }}). Se puede
                    guardar: es una alerta, no un bloqueo.
                  </p>
                </div>
                <div class="flex justify-end gap-2 pt-2">
                  <Button variant="outline" type="button" :disabled="submitting" @click="modalOpen = false">
                    Cancelar
                  </Button>
                  <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="submitting">
                    Guardar
                  </Button>
                </div>
              </form>
            </section>
          </div>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>
