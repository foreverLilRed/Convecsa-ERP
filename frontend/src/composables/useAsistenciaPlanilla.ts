import { computed, onMounted, onUnmounted, reactive, ref, watch, type Ref } from 'vue'
import { toast } from 'vue-sonner'

import {
  actualizarPlanilla,
  generarPlanilla,
  obtenerPlanilla,
} from '@/services/asistencia/asistenciaService'
import { periodoLabel } from '@/types/nomina'
import type {
  AsistenciaFila,
  AsistenciaPlanilla,
  AsistenciaRegistro,
  CodigoTareo,
  EstadoAsistencia,
} from '@/types/asistencia'

export type AsistenciaEdit = {
  horasTrabajadas: string
  estado: EstadoAsistencia
  codigo: CodigoTareo
  minutosExtras25: string
  minutosExtras35: string
  minutosTardanza: string
}

export type FilaVista = AsistenciaFila & {
  porFecha: Record<string, AsistenciaRegistro>
}

export type UseAsistenciaPlanillaOptions = {
  initialAnio?: number
  initialMes?: number
  updateDocumentTitle?: boolean
}

const CODIGOS_SIN_HORAS: CodigoTareo[] = [
  'D',
  'F',
  'DM',
  'SIT',
  'MAT_PRE',
  'MAT_POST',
  'VAC',
  'VAC_COMP',
  'LCG',
  'LSG',
  'PAT',
  'FI',
]

export function useAsistenciaPlanilla(options: UseAsistenciaPlanillaOptions = {}) {
  const hoy = new Date()
  const anio = ref(options.initialAnio ?? hoy.getFullYear())
  const mes = ref(options.initialMes ?? hoy.getMonth() + 1)
  const loading = ref(true)
  const acting = ref(false)
  const saving = ref(false)
  const planilla = ref<AsistenciaPlanilla | null>(null)
  const edits = reactive<Record<number, AsistenciaEdit>>({})
  const estadoVivo = ref('')
  let skipPeriodoWatch = false

  const hoyIso = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`

  const dirtyCount = computed(() => Object.keys(edits).length)
  const tituloPeriodo = computed(() => periodoLabel(anio.value, mes.value))

  const filasVista = computed<FilaVista[]>(() => {
    if (!planilla.value) {
      return []
    }
    return planilla.value.filas.map((fila) => ({
      ...fila,
      porFecha: Object.fromEntries(fila.registros.map((registro) => [registro.fecha, registro])),
    }))
  })

  const totalHoras = computed(() => {
    if (!planilla.value) {
      return 0
    }
    let suma = 0
    for (const fila of planilla.value.filas) {
      for (const registro of fila.registros) {
        suma += horasDe(registro)
      }
    }
    return Math.round(suma * 100) / 100
  })

  const totalFaltas = computed(() => {
    if (!planilla.value) {
      return 0
    }
    let n = 0
    for (const fila of planilla.value.filas) {
      for (const registro of fila.registros) {
        if (estadoDe(registro) === 'FALTA') {
          n += 1
        }
      }
    }
    return n
  })

  function horasDe(registro: AsistenciaRegistro): number {
    const edit = edits[registro.id]
    if (edit) {
      const valor = Number(edit.horasTrabajadas)
      return Number.isFinite(valor) ? valor : 0
    }
    return Number(registro.horasTrabajadas)
  }

  function estadoDe(registro: AsistenciaRegistro): EstadoAsistencia {
    return edits[registro.id]?.estado ?? registro.estado
  }

  function codigoDe(registro: AsistenciaRegistro): CodigoTareo {
    return edits[registro.id]?.codigo ?? registro.codigo
  }

  function minutosDe(
    registro: AsistenciaRegistro,
    campo: 'minutosExtras25' | 'minutosExtras35' | 'minutosTardanza',
  ): number {
    const valor = edits[registro.id]?.[campo] ?? String(registro[campo])
    const numero = Number(valor)
    return Number.isFinite(numero) ? numero : 0
  }

  function horasBloqueadas(codigo: CodigoTareo): boolean {
    return CODIGOS_SIN_HORAS.includes(codigo)
  }

  function totalFila(registros: AsistenciaRegistro[]): number {
    return Math.round(registros.reduce((acc, registro) => acc + horasDe(registro), 0) * 100) / 100
  }

  function estadoClase(estado: EstadoAsistencia): string {
    switch (estado) {
      case 'ASISTIO':
        return 'border-l-3 border-l-emerald-700 bg-emerald-100'
      case 'FALTA':
        return 'border-l-3 border-l-red-700 bg-red-100'
      case 'PERMISO':
        return 'border-l-3 border-l-amber-700 bg-amber-100'
      default:
        return 'border-l-3 border-l-slate-500 bg-slate-200'
    }
  }

  function estadoSelectClase(estado: EstadoAsistencia): string {
    switch (estado) {
      case 'ASISTIO':
        return 'border-emerald-400 bg-emerald-50'
      case 'FALTA':
        return 'border-red-400 bg-red-50'
      case 'PERMISO':
        return 'border-amber-400 bg-amber-50'
      default:
        return 'border-slate-400 bg-slate-100'
    }
  }

  function onHoras(registro: AsistenciaRegistro, esperado: number, value: string) {
    markEdit(registro, value, estadoDe(registro), codigoDe(registro), esperado)
  }

  function onCodigo(registro: AsistenciaRegistro, esperado: number, codigo: CodigoTareo) {
    let horas = String(horasDe(registro))
    if (CODIGOS_SIN_HORAS.includes(codigo)) {
      horas = '0'
    } else if (codigo === 'A' && Number(horas) === 0) {
      horas = String(esperado)
    }
    markEdit(registro, horas, estadoDe(registro), codigo, esperado)
  }

  function markEdit(
    registro: AsistenciaRegistro,
    horasTrabajadas: string,
    estado: EstadoAsistencia,
    codigo: CodigoTareo,
    _esperado: number,
  ) {
    const previo = edits[registro.id]
    edits[registro.id] = {
      horasTrabajadas,
      estado,
      codigo,
      minutosExtras25: previo?.minutosExtras25 ?? String(registro.minutosExtras25),
      minutosExtras35: previo?.minutosExtras35 ?? String(registro.minutosExtras35),
      minutosTardanza: previo?.minutosTardanza ?? String(registro.minutosTardanza),
    }
    limpiarSiSinCambios(registro)
  }

  function onMinutos(
    registro: AsistenciaRegistro,
    campo: 'minutosExtras25' | 'minutosExtras35' | 'minutosTardanza',
    valor: string,
  ) {
    markEdit(registro, String(horasDe(registro)), estadoDe(registro), codigoDe(registro), 0)
    if (!edits[registro.id]) {
      edits[registro.id] = {
        horasTrabajadas: String(horasDe(registro)),
        estado: estadoDe(registro),
        codigo: codigoDe(registro),
        minutosExtras25: String(registro.minutosExtras25),
        minutosExtras35: String(registro.minutosExtras35),
        minutosTardanza: String(registro.minutosTardanza),
      }
    }
    edits[registro.id]![campo] = valor
    limpiarSiSinCambios(registro)
  }

  function limpiarSiSinCambios(registro: AsistenciaRegistro) {
    const edit = edits[registro.id]
    if (!edit) {
      return
    }
    if (
      Number(edit.horasTrabajadas) === Number(registro.horasTrabajadas)
      && edit.estado === registro.estado
      && edit.codigo === registro.codigo
      && Number(edit.minutosExtras25) === registro.minutosExtras25
      && Number(edit.minutosExtras35) === registro.minutosExtras35
      && Number(edit.minutosTardanza) === registro.minutosTardanza
    ) {
      delete edits[registro.id]
    }
  }

  function clearEdits() {
    for (const key of Object.keys(edits)) {
      delete edits[Number(key)]
    }
  }

  function onDescartar() {
    if (dirtyCount.value === 0) {
      return
    }
    if (!window.confirm('¿Descartar los cambios sin guardar?')) {
      return
    }
    clearEdits()
    estadoVivo.value = 'Cambios descartados'
  }

  function cambiarPeriodo(delta: number) {
    const siguiente = new Date(anio.value, mes.value - 1 + delta, 1)
    anio.value = siguiente.getFullYear()
    mes.value = siguiente.getMonth() + 1
  }

  function confirmarCambioPeriodo(): boolean {
    if (dirtyCount.value === 0) {
      return true
    }
    return window.confirm('Hay cambios sin guardar. Si continúa se perderán. ¿Cambiar de mes?')
  }

  async function load() {
    loading.value = true
    estadoVivo.value = 'Cargando planilla de asistencia'
    try {
      planilla.value = await obtenerPlanilla(anio.value, mes.value)
      clearEdits()
      estadoVivo.value = planilla.value.generado
        ? `Planilla de ${tituloPeriodo.value} con ${planilla.value.filas.length} personas`
        : `Sin planilla generada para ${tituloPeriodo.value}`
      if (options.updateDocumentTitle) {
        document.title = `Planilla · ${tituloPeriodo.value}`
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'No se pudo cargar la asistencia.'
      estadoVivo.value = message
      toast.error(message)
    } finally {
      loading.value = false
    }
  }

  async function onGenerar() {
    if (planilla.value?.generado) {
      const ok = window.confirm(
        'Ya hay registros este mes. Solo se crearán los días o personas que falten; no se pisarán las horas ya marcadas. ¿Continuar?',
      )
      if (!ok) {
        return
      }
    }
    acting.value = true
    estadoVivo.value = 'Generando planilla'
    try {
      planilla.value = await generarPlanilla(anio.value, mes.value)
      clearEdits()
      estadoVivo.value = 'Planilla generada'
      toast.success('Planilla generada.')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'No se pudo generar la planilla.'
      estadoVivo.value = message
      toast.error(message)
    } finally {
      acting.value = false
    }
  }

  async function onGuardar() {
    const items = Object.entries(edits).map(([id, value]) => ({
      id: Number(id),
      horasTrabajadas: Number(value.horasTrabajadas),
      estado: value.estado,
      codigo: value.codigo,
      minutosExtras25: Number(value.minutosExtras25),
      minutosExtras35: Number(value.minutosExtras35),
      minutosTardanza: Number(value.minutosTardanza),
    }))
    if (!items.length) {
      return
    }
    for (const item of items) {
      if (!Number.isFinite(item.horasTrabajadas) || item.horasTrabajadas < 0 || item.horasTrabajadas > 24) {
        toast.error('Las horas deben estar entre 0 y 24.')
        return
      }
      if (
        [item.minutosExtras25, item.minutosExtras35, item.minutosTardanza].some(
          (minutos) => !Number.isInteger(minutos) || minutos < 0 || minutos > 1440,
        )
      ) {
        toast.error('Los minutos deben ser enteros entre 0 y 1440.')
        return
      }
    }
    saving.value = true
    estadoVivo.value = 'Guardando asistencia'
    try {
      planilla.value = await actualizarPlanilla(items)
      clearEdits()
      estadoVivo.value = 'Asistencia guardada'
      toast.success('Asistencia guardada.')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'No se pudo guardar la asistencia.'
      estadoVivo.value = message
      toast.error(message)
    } finally {
      saving.value = false
    }
  }

  function onWindowKeydown(event: KeyboardEvent) {
    if (!(event.ctrlKey || event.metaKey) || event.key.toLowerCase() !== 's' || event.repeat) {
      return
    }
    if (dirtyCount.value > 0 && !saving.value) {
      event.preventDefault()
      void onGuardar()
    }
  }

  function onBeforeUnload(event: BeforeUnloadEvent) {
    if (dirtyCount.value > 0) {
      event.preventDefault()
      event.returnValue = ''
    }
  }

  function setPeriodo(nuevoAnio: number, nuevoMes: number) {
    if (anio.value === nuevoAnio && mes.value === nuevoMes) {
      return
    }
    skipPeriodoWatch = true
    anio.value = nuevoAnio
    mes.value = nuevoMes
    void load()
  }

  watch([anio, mes], (_siguiente, anterior) => {
    if (skipPeriodoWatch) {
      skipPeriodoWatch = false
      return
    }
    if (anterior && dirtyCount.value > 0 && !confirmarCambioPeriodo()) {
      skipPeriodoWatch = true
      anio.value = anterior[0]
      mes.value = anterior[1]
      return
    }
    void load()
  })

  onMounted(() => {
    window.addEventListener('keydown', onWindowKeydown)
    window.addEventListener('beforeunload', onBeforeUnload)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', onWindowKeydown)
    window.removeEventListener('beforeunload', onBeforeUnload)
  })

  void load()

  return {
    anio: anio as Ref<number>,
    mes: mes as Ref<number>,
    loading,
    acting,
    saving,
    planilla,
    edits,
    estadoVivo,
    hoyIso,
    dirtyCount,
    tituloPeriodo,
    filasVista,
    totalHoras,
    totalFaltas,
    horasDe,
    estadoDe,
    codigoDe,
    minutosDe,
    horasBloqueadas,
    totalFila,
    estadoClase,
    estadoSelectClase,
    onHoras,
    onCodigo,
    onMinutos,
    onDescartar,
    cambiarPeriodo,
    load,
    onGenerar,
    onGuardar,
    setPeriodo,
    clearEdits,
  }
}
