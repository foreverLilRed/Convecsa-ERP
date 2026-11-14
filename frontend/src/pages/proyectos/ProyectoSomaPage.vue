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
import {
  listarCuadrillasObra,
  listarEquiposObra,
  listarPersonalObra,
  listarResponsablesObra,
  listarSubcontratosObra,
  obtenerPresupuesto,
  subirDocumentoObra,
} from '@/services/construccion/construccionService'
import { listarPersonas } from '@/services/persona/personaService'
import {
  anularPolizaSctr,
  cambiarEstadoItemEpp,
  actualizarIpercObra,
  crearCapacitacionObra,
  actualizarPetarObra,
  autorizarPetarObra,
  cerrarPetarObra,
  checklistPetarObra,
  crearCharlaAtsObra,
  crearHallazgoObra,
  crearPetarObra,
  crearPreUsoObra,
  crearEventoObra,
  actualizarEventoObra,
  agregarHallazgoEventoObra,
  cerrarEventoObra,
  sincronizarDmEventoObra,
  crearInspeccionObra,
  crearEntregaEpp,
  crearIpercObra,
  crearPolizaSctr,
  desactivarIpercObra,
  descargarKardexEpp,
  faltantesTareoPetar,
  guardarNominaSctr,
  listarCapacitacionesObra,
  listarCharlasAtsObra,
  listarEmosObra,
  listarEntregasEpp,
  listarHabilitacionObra,
  listarHallazgosObra,
  listarInspeccionesObra,
  listarIpercObra,
  listarMaterialesEpp,
  listarPetarObra,
  listarPolizasSctr,
  listarPreUsoObra,
  listarEventosObra,
  obtenerEstadisticasObra,
  obtenerRequerimientoComite,
  listarMiembrosComite,
  crearMiembroComite,
  darBajaMiembroComite,
  listarActasComite,
  crearActaComite,
  listarResiduosObra,
  crearResiduoObra,
  listarMonitoreosObra,
  crearMonitoreoObra,
  obtenerResumenSoma,
  precargarCuadrillaCapacitacion,
  levantarHallazgoObra,
  marcarHallazgoEnProcesoObra,
  precargarCuadrillaCharlaAts,
  reanudarPetarObra,
  registrarEmoObra,
  suspenderPetarObra,
  sugerirNominaSctr,
} from '@/services/soma/somaService'
import type { AsignacionEquipo, CuadrillaObra, PartidaPresupuesto, PersonalObra, ResponsableObra, SubcontratoObra } from '@/types/construccion'
import type { PersonaResumenDTO } from '@/types/persona'
import type {
  AptitudEmo,
  EstadoItemEpp,
  EstadoHabilitacionSoma,
  MotivoEntregaEpp,
  NivelRiesgoIperc,
  SomaCapacitacion,
  ClasificacionHallazgoSoma,
  EstadoHallazgoSoma,
  PotencialPerdidaSoma,
  EstadoPetar,
  SomaCharlaAts,
  SomaHallazgo,
  SomaInspeccion,
  SomaPetar,
  SomaPetarFaltanteTareo,
  SomaPetarRequest,
  ResultadoPreUsoEquipo,
  SomaPreUso,
  EstadoInvestigacionSst,
  GravedadIncapacidadSst,
  CargoComiteSoma,
  RepresentacionComiteSoma,
  SomaComiteActa,
  SomaComiteMiembro,
  SomaComiteRequerimiento,
  CategoriaResiduoSoma,
  SomaMonitoreo,
  SomaResiduo,
  SomaEstadisticaAnio,
  TipoAgenteMonitoreoSoma,
  SomaEvento,
  TipoEventoSst,
  TipoSesionComiteSoma,
  TipoTrabajoPetar,
  SomaEmo,
  SomaEntregaEpp,
  SomaEppMaterial,
  SomaHabilitacion,
  SomaIpercFila,
  SomaResumenObra,
  SomaSctrNominaFila,
  SomaSctrPoliza,
  TipoCapacitacionSoma,
  TipoEmo,
  TipoInspeccionSoma,
  TipoPeligroIperc,
} from '@/types/soma'
import { nivelRiesgoIpercDesdePuntaje } from '@/types/soma'
import { fieldInputClass, fieldSelectClass, tabsListClass } from './fieldClasses'

type TabSoma = 'habilitacion' | 'resumen' | 'vigilancia' | 'sctr' | 'epp' | 'capacitaciones' | 'iperc' | 'charlaAts' | 'petar' | 'inspecciones' | 'preUso' | 'eventos' | 'comite' | 'ambiental'
type PetarChecklistDraft = { clave: string; etiqueta: string; valor: boolean }
type PetarTrabajadorDraft = { personaId: number; rolEnTarea: string }
type FiltroHabilitacion = 'TODOS' | EstadoHabilitacionSoma
type EppItemDraft = { materialId: string; cantidad: string; talla: string }
type CapAsistenteDraft = { personaId: number; nota: string; aprobado: boolean }
type PasoAtsDraft = { paso: string; peligros: string; controles: string }
type CharlaAsistenteDraft = { personaId: number; observacion: string; habilitadoSoma: boolean | null }

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

const TIPOS_PETAR: { valor: TipoTrabajoPetar; label: string }[] = [
  { valor: 'ALTURA', label: 'Trabajo en altura' },
  { valor: 'EXCAVACION_ZANJA', label: 'Excavación / zanja' },
  { valor: 'TRABAJO_CALIENTE', label: 'Trabajo en caliente' },
  { valor: 'ESPACIO_CONFINADO', label: 'Espacio confinado' },
  { valor: 'IZAJE_CARGAS', label: 'Izaje de cargas' },
  { valor: 'ENERGIA_ELECTRICA', label: 'Energía eléctrica' },
]

const ESTADOS_PETAR: { valor: EstadoPetar; label: string }[] = [
  { valor: 'BORRADOR', label: 'Borrador' },
  { valor: 'AUTORIZADO', label: 'Autorizado' },
  { valor: 'SUSPENDIDO', label: 'Suspendido' },
  { valor: 'CERRADO', label: 'Cerrado' },
]

const CHECKLIST_PREUSO: { clave: string; etiqueta: string }[] = [
  { clave: 'frenos', etiqueta: 'Frenos' },
  { clave: 'luces', etiqueta: 'Luces' },
  { clave: 'alarmaRetroceso', etiqueta: 'Alarma de retroceso' },
  { clave: 'extintor', etiqueta: 'Extintor' },
  { clave: 'cinturon', etiqueta: 'Cinturón' },
  { clave: 'sistemaHidraulico', etiqueta: 'Sistema hidráulico' },
  { clave: 'guardasSeguridad', etiqueta: 'Guardas de seguridad' },
]

const RESULTADOS_PREUSO: { valor: ResultadoPreUsoEquipo; label: string }[] = [
  { valor: 'OPERATIVO', label: 'Operativo' },
  { valor: 'OPERATIVO_CON_OBSERVACION', label: 'Operativo con observación' },
  { valor: 'INOPERATIVO_BLOQUEADO', label: 'Inoperativo / bloqueado' },
]

const MESES = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Setiembre',
  'Octubre',
  'Noviembre',
  'Diciembre',
]

const anioActual = new Date().getFullYear()
const ANIOS = [anioActual - 1, anioActual, anioActual + 1]
const money = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const qtyFmt = new Intl.NumberFormat('es-PE', { minimumFractionDigits: 0, maximumFractionDigits: 4 })

const MOTIVOS_EPP: { valor: MotivoEntregaEpp; label: string }[] = [
  { valor: 'DOTACION_INICIAL', label: 'Dotación inicial' },
  { valor: 'RENOVACION_DESGASTE', label: 'Renovación por desgaste' },
  { valor: 'REPOSICION_PERDIDA', label: 'Reposición por pérdida' },
  { valor: 'TRABAJO_ESPECIAL', label: 'Trabajo especial' },
]

const ESTADOS_EPP: { valor: EstadoItemEpp; label: string }[] = [
  { valor: 'VIGENTE', label: 'Vigente' },
  { valor: 'DEVUELTO', label: 'Devuelto' },
  { valor: 'DETERIORADO', label: 'Deteriorado' },
  { valor: 'EXTRAVIADO', label: 'Extraviado' },
]

const TIPOS_CAP: { valor: TipoCapacitacionSoma; label: string }[] = [
  { valor: 'INDUCCION_HOMBRE_NUEVO', label: 'Inducción de hombre nuevo' },
  { valor: 'CAPACITACION_PROGRAMADA', label: 'Capacitación programada' },
  { valor: 'ENTRENAMIENTO_ESPECIFICO_ALTO_RIESGO', label: 'Entrenamiento de alto riesgo' },
  { valor: 'SIMULACRO_EMERGENCIA', label: 'Simulacro de emergencia' },
]

const TIPOS_PELIGRO_IPERC: { valor: TipoPeligroIperc; label: string }[] = [
  { valor: 'LOCATIVO', label: 'Locativo' },
  { valor: 'MECANICO', label: 'Mecánico' },
  { valor: 'ELECTRICO', label: 'Eléctrico' },
  { valor: 'FISICO', label: 'Físico' },
  { valor: 'QUIMICO', label: 'Químico' },
  { valor: 'ERGONOMICO', label: 'Ergonómico' },
  { valor: 'PSICOSOCIAL', label: 'Psicosocial' },
  { valor: 'BIOLOGICO', label: 'Biológico' },
]

const ESCALA_IPERC = [1, 2, 3, 4, 5]

const TIPOS_INSPECCION: { valor: TipoInspeccionSoma; label: string }[] = [
  { valor: 'PLANEADA_MENSUAL', label: 'Planeada mensual' },
  { valor: 'INOPINADA_DIARIA', label: 'Inopinada diaria' },
  { valor: 'EPP', label: 'EPP' },
  { valor: 'EXTINTORES_EMERGENCIA', label: 'Extintores y emergencia' },
  { valor: 'INSTALACIONES_ELECTRICAS_G050', label: 'Instalaciones eléctricas G.050' },
  { valor: 'ANDAMIOS_ESCALERAS', label: 'Andamios y escaleras' },
  { valor: 'ORDEN_LIMPIEZA', label: 'Orden y limpieza' },
  { valor: 'ALMACEN_MATPEL', label: 'Almacén MATPEL' },
  { valor: 'SUBCONTRATISTA', label: 'Subcontratista' },
]

const CLASIFICACIONES_HALLAZGO: { valor: ClasificacionHallazgoSoma; label: string }[] = [
  { valor: 'ACTO_SUBESTANDAR', label: 'Acto subestándar' },
  { valor: 'CONDICION_SUBESTANDAR', label: 'Condición subestándar' },
]

const POTENCIALES_HALLAZGO: { valor: PotencialPerdidaSoma; label: string }[] = [
  { valor: 'ALTO_GRAVE', label: 'Alto / grave' },
  { valor: 'MEDIO_MODERADO', label: 'Medio / moderado' },
  { valor: 'BAJO_LEVE', label: 'Bajo / leve' },
]

const ESTADOS_HALLAZGO: { valor: EstadoHallazgoSoma | ''; label: string }[] = [
  { valor: '', label: 'Todos' },
  { valor: 'ABIERTO', label: 'Abierto' },
  { valor: 'EN_PROCESO', label: 'En proceso' },
  { valor: 'LEVANTADO', label: 'Levantado' },
]

const TIPOS_EVENTO_SST: { valor: TipoEventoSst; label: string }[] = [
  { valor: 'INCIDENTE', label: 'Incidente' },
  { valor: 'INCIDENTE_PELIGROSO', label: 'Incidente peligroso' },
  { valor: 'ACCIDENTE_LEVE', label: 'Accidente leve' },
  { valor: 'ACCIDENTE_INCAPACITANTE', label: 'Accidente incapacitante' },
  { valor: 'ACCIDENTE_MORTAL', label: 'Accidente mortal' },
  { valor: 'ENFERMEDAD_OCUPACIONAL', label: 'Enfermedad ocupacional' },
]

const GRAVEDADES_INCAPACIDAD: { valor: GravedadIncapacidadSst; label: string }[] = [
  { valor: 'TOTAL_TEMPORAL', label: 'Total temporal' },
  { valor: 'PARCIAL_PERMANENTE', label: 'Parcial permanente' },
  { valor: 'TOTAL_PERMANENTE', label: 'Total permanente' },
]

const REPRESENTACIONES_COMITE: { valor: RepresentacionComiteSoma; label: string }[] = [
  { valor: 'EMPLEADOR', label: 'Empleador' },
  { valor: 'TRABAJADORES', label: 'Trabajadores' },
]

const CARGOS_COMITE: { valor: CargoComiteSoma; label: string }[] = [
  { valor: 'PRESIDENTE', label: 'Presidente' },
  { valor: 'SECRETARIO', label: 'Secretario' },
  { valor: 'MIEMBRO', label: 'Miembro' },
  { valor: 'SUPERVISOR_SST', label: 'Supervisor SST' },
  { valor: 'OBSERVADOR_SINDICAL', label: 'Observador sindical' },
]

const TIPOS_SESION_COMITE: { valor: TipoSesionComiteSoma; label: string }[] = [
  { valor: 'INSTALACION', label: 'Instalación' },
  { valor: 'ORDINARIA', label: 'Ordinaria' },
  { valor: 'EXTRAORDINARIA', label: 'Extraordinaria' },
]

const CATEGORIAS_RESIDUO: { valor: CategoriaResiduoSoma; label: string }[] = [
  { valor: 'PELIGROSO_ROJO', label: 'Peligroso (rojo)' },
  { valor: 'NO_APROVECHABLE_NEGRO', label: 'No aprovechable (negro)' },
  { valor: 'ORGANICO_MARRON', label: 'Orgánico (marrón)' },
  { valor: 'APROVECHABLE_VERDE_AZUL_BLANCO', label: 'Aprovechable (verde/azul/blanco)' },
  { valor: 'METALICO_AMARILLO', label: 'Metálico (amarillo)' },
  { valor: 'ESCOMBROS_DESMONTE_CONSTRUCCION', label: 'Escombros / desmonte' },
]

const TIPOS_AGENTE_MONITOREO: { valor: TipoAgenteMonitoreoSoma; label: string }[] = [
  { valor: 'FISICO_RUIDO', label: 'Físico: ruido' },
  { valor: 'FISICO_ILUMINACION', label: 'Físico: iluminación' },
  { valor: 'FISICO_VIBRACION', label: 'Físico: vibración' },
  { valor: 'FISICO_ESTRES_TERMICO', label: 'Físico: estrés térmico' },
  { valor: 'QUIMICO_POLVO_GASES', label: 'Químico: polvo / gases' },
  { valor: 'BIOLOGICO', label: 'Biológico' },
  { valor: 'ERGONOMICO', label: 'Ergonómico' },
  { valor: 'PSICOSOCIAL', label: 'Psicosocial' },
  { valor: 'AMBIENTAL_AGUA_AIRE', label: 'Ambiental: agua / aire' },
]

const route = useRoute()
const proyectoId = computed(() => Number(route.params.id))
const tab = ref<TabSoma>('habilitacion')
const loading = ref(true)
const saving = ref(false)
const resumen = ref<SomaResumenObra | null>(null)
const habilitacion = ref<SomaHabilitacion | null>(null)
const fechaHabilitacion = ref(hoyIso())
const filtroHabilitacion = ref<FiltroHabilitacion>('TODOS')
const emos = ref<SomaEmo[]>([])
const personal = ref<PersonalObra[]>([])
const personaSearch = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const polizas = ref<SomaSctrPoliza[]>([])
const polizaSeleccionadaId = ref<number | null>(null)
const nominaEdicion = ref<SomaSctrNominaFila[]>([])
const sctrFileInput = ref<HTMLInputElement | null>(null)
const sctrSaving = ref(false)
const entregasEpp = ref<SomaEntregaEpp[]>([])
const materialesEpp = ref<SomaEppMaterial[]>([])
const partidas = ref<PartidaPresupuesto[]>([])
const eppPersonaSearch = ref('')
const eppSaving = ref(false)
const eppItems = ref<EppItemDraft[]>([{ materialId: '', cantidad: '', talla: '' }])
const capacitaciones = ref<SomaCapacitacion[]>([])
const cuadrillas = ref<CuadrillaObra[]>([])
const capSaving = ref(false)
const capPersonaSearch = ref('')
const capCuadrillaId = ref('')
const capFileInput = ref<HTMLInputElement | null>(null)
const capAsistentes = ref<CapAsistenteDraft[]>([])
const capAgregarPersonaId = ref<number | null>(null)
const filasIperc = ref<SomaIpercFila[]>([])
const filtroPartidaIperc = ref('')
const incluirInactivosIperc = ref(false)
const ipercSaving = ref(false)
const ipercEditandoId = ref<number | null>(null)
const charlasAts = ref<SomaCharlaAts[]>([])
const charlaSaving = ref(false)
const charlaPersonaSearch = ref('')
const charlaAgregarPersonaId = ref<number | null>(null)
const charlaExpositorSearch = ref('')
const charlaPasos = ref<PasoAtsDraft[]>([{ paso: '', peligros: '', controles: '' }])
const charlaAsistentes = ref<CharlaAsistenteDraft[]>([])
const inspecciones = ref<SomaInspeccion[]>([])
const hallazgos = ref<SomaHallazgo[]>([])
const subcontratos = ref<SubcontratoObra[]>([])
const petars = ref<SomaPetar[]>([])
const petarFaltantes = ref<SomaPetarFaltanteTareo[]>([])
const responsablesObra = ref<ResponsableObra[]>([])
const petarSaving = ref(false)
const petarChecklist = ref<PetarChecklistDraft[]>([])
const petarTrabajadores = ref<PetarTrabajadorDraft[]>([])
const petarAgregarPersonaId = ref<number | null>(null)
const petarPersonaSearch = ref('')
const petarResidenteSearch = ref('')
const petarSupervisorSearch = ref('')
const petarEditandoId = ref<number | null>(null)
const inspeccionSaving = ref(false)
const hallazgoSaving = ref(false)
const inspectorSearch = ref('')
const responsableSearch = ref('')
const filtroEstadoHallazgo = ref<EstadoHallazgoSoma | ''>('')
const filtroVencidosHallazgo = ref(false)
const equiposObra = ref<AsignacionEquipo[]>([])
const preUsos = ref<SomaPreUso[]>([])
const preUsoSaving = ref(false)
const preUsoOperadorSearch = ref('')
const preUsoChecklist = ref(CHECKLIST_PREUSO.map((item) => ({ ...item, valor: false })))
const eventos = ref<SomaEvento[]>([])
const estadisticas = ref<SomaEstadisticaAnio | null>(null)
const comiteRequerimiento = ref<SomaComiteRequerimiento | null>(null)
const comiteMiembros = ref<SomaComiteMiembro[]>([])
const comiteActas = ref<SomaComiteActa[]>([])
const comitePersonas = ref<PersonaResumenDTO[]>([])
const comitePersonaSearch = ref('')
const comitePersonaSeleccionada = ref<PersonaResumenDTO | null>(null)
const comiteMiembroSaving = ref(false)
const comiteActaSaving = ref(false)
const comiteFileInput = ref<HTMLInputElement | null>(null)
let comitePersonaTimer: ReturnType<typeof setTimeout> | null = null

const comiteMiembroForm = reactive({
  personaId: null as number | null,
  representacion: 'EMPLEADOR' as RepresentacionComiteSoma,
  cargoComite: 'SUPERVISOR_SST' as CargoComiteSoma,
  titular: true,
  fechaInicio: hoyIso(),
  fechaFin: '',
})

const comiteActaForm = reactive({
  numeroActa: '',
  tipoSesion: 'ORDINARIA' as TipoSesionComiteSoma,
  fecha: hoyIso(),
  agenda: '',
  acuerdos: '',
})
const residuos = ref<SomaResiduo[]>([])
const monitoreos = ref<SomaMonitoreo[]>([])
const residuoSaving = ref(false)
const monitoreoSaving = ref(false)
const residuoFileInput = ref<HTMLInputElement | null>(null)
const monitoreoFileInput = ref<HTMLInputElement | null>(null)

const residuoForm = reactive({
  fecha: hoyIso(),
  categoriaResiduo: 'NO_APROVECHABLE_NEGRO' as CategoriaResiduoSoma,
  descripcion: '',
  cantidadKg: '',
  volumenM3: '',
  eorsRazonSocial: '',
  eorsRuc: '',
  numeroManifiesto: '',
  destinoFinal: '',
})

const monitoreoForm = reactive({
  fecha: hoyIso(),
  tipoAgente: 'FISICO_RUIDO' as TipoAgenteMonitoreoSoma,
  parametroMedido: '',
  puntoMedicion: '',
  valorMedido: '',
  limitePermisible: '',
  unidad: '',
  laboratorioNombre: '',
})
const anioEstadistica = ref(anioActual)
const eventoSaving = ref(false)
const eventoMedidaSaving = ref(false)
const eventoEditandoId = ref<number | null>(null)
const eventoPersonaSearch = ref('')
const eventoMedidaResponsableSearch = ref('')
const causaDraft = reactive({
  actos: '',
  condiciones: '',
  factoresPersonales: '',
  factoresTrabajo: '',
})

const capForm = reactive({
  tipo: 'INDUCCION_HOMBRE_NUEVO' as TipoCapacitacionSoma,
  tema: '',
  fecha: hoyIso(),
  horasDuracion: '4',
  expositorNombre: '',
  expositorEntidad: '',
  puntajeSimulacro: '',
  observaciones: '',
})

const ipercForm = reactive({
  partidaId: '',
  actividadTarea: '',
  puestoTrabajo: '',
  rutinario: true,
  tipoPeligro: 'MECANICO' as TipoPeligroIperc,
  peligro: '',
  riesgo: '',
  consecuencia: '',
  requisitoLegal: '',
  probabilidadInicial: '3',
  severidadInicial: '3',
  controlEliminacion: '',
  controlSustitucion: '',
  controlIngenieria: '',
  controlAdministrativo: '',
  controlEpp: '',
  probabilidadResidual: '2',
  severidadResidual: '2',
})

const charlaForm = reactive({
  fecha: hoyIso(),
  horaInicio: '07:00',
  duracionMinutos: '10',
  temaCharla: '',
  cuadrillaId: '',
  partidaId: '',
  expositorPersonaId: null as number | null,
  eppVerificado: false,
  herramientasVerif: false,
  observaciones: '',
})

const inspeccionForm = reactive({
  tipo: 'INOPINADA_DIARIA' as TipoInspeccionSoma,
  fecha: hoyIso(),
  areaFrente: '',
  subcontratoId: '',
  inspectorPersonaId: null as number | null,
  puntajeCumplimiento: '',
  observaciones: '',
})

const petarForm = reactive({
  tipoTrabajo: 'ALTURA' as TipoTrabajoPetar,
  fecha: hoyIso(),
  horaInicio: '07:00',
  horaFin: '17:00',
  ubicacionFrente: '',
  descripcionTrabajo: '',
  partidaId: '',
  cuadrillaId: '',
  residentePersonaId: null as number | null,
  supervisorSsomaId: null as number | null,
  oxigenoPct: '',
  lelPct: '',
  vientoKmh: '',
})

const hallazgoForm = reactive({
  inspeccionId: '',
  clasificacion: 'CONDICION_SUBESTANDAR' as ClasificacionHallazgoSoma,
  potencialPerdida: 'MEDIO_MODERADO' as PotencialPerdidaSoma,
  descripcion: '',
  ubicacion: '',
  accionCorrectiva: '',
  responsablePersonaId: null as number | null,
  fechaDeteccion: hoyIso(),
  fechaLimite: hoyIso(),
  paralizacionFrente: false,
})

const eventoForm = reactive({
  tipoEvento: 'INCIDENTE' as TipoEventoSst,
  gravedadIncapacidad: '' as GravedadIncapacidadSst | '',
  fechaHora: '',
  lugarExacto: '',
  partidaId: '',
  personaAfectadaId: null as number | null,
  subcontratoId: '',
  descripcionEvento: '',
  parteCuerpoLesionada: '',
  naturalezaLesion: '',
  agenteCausante: '',
  numeroCertificadoDm: '',
  diasCargoEstadistico: '0',
  fechaInicioDm: '',
  fechaFinDm: '',
  notificadoMtpe: false,
  causasActos: [] as string[],
  causasCondiciones: [] as string[],
  causasFactoresPersonales: [] as string[],
  causasFactoresTrabajo: [] as string[],
})

const eventoMedidaForm = reactive({
  clasificacion: 'CONDICION_SUBESTANDAR' as ClasificacionHallazgoSoma,
  potencialPerdida: 'MEDIO_MODERADO' as PotencialPerdidaSoma,
  descripcion: '',
  ubicacion: '',
  accionCorrectiva: '',
  responsablePersonaId: null as number | null,
  fechaDeteccion: hoyIso(),
  fechaLimite: hoyIso(),
  paralizacionFrente: false,
})

const preUsoForm = reactive({
  asignacionEquipoId: '',
  fecha: hoyIso(),
  horometroLectura: '',
  operadorPersonaId: null as number | null,
  resultado: 'OPERATIVO' as ResultadoPreUsoEquipo,
  observaciones: '',
})

const eppForm = reactive({
  personaId: null as number | null,
  motivo: 'DOTACION_INICIAL' as MotivoEntregaEpp,
  fechaEntrega: hoyIso(),
  partidaId: '',
  descontarAlmacen: true,
  firmadoTrabajador: false,
  observaciones: '',
})

const form = reactive({
  personaId: null as number | null,
  tipoEmo: 'INGRESO' as TipoEmo,
  aptitud: 'APTO' as AptitudEmo,
  clinicaNombre: '',
  fechaExamen: hoyIso(),
  fechaVencimiento: masUnAnio(hoyIso()),
  restricciones: '',
})

const sctrForm = reactive({
  anio: anioActual,
  mes: new Date().getMonth() + 1,
  aseguradoraSalud: '',
  numeroPolizaSalud: '',
  aseguradoraPension: '',
  numeroPolizaPension: '',
  fechaInicio: primerDiaMes(anioActual, new Date().getMonth() + 1),
  fechaFin: ultimoDiaMes(anioActual, new Date().getMonth() + 1),
  montoPrimaSalud: '0',
  montoPrimaPension: '0',
})

const polizaSeleccionada = computed(
  () => polizas.value.find((item) => item.id === polizaSeleccionadaId.value) ?? null,
)

const personaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === form.personaId) ?? null,
)

const eppPersonaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === eppForm.personaId) ?? null,
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

const opcionesEppPersona = computed(() => {
  const q = eppPersonaSearch.value.trim().toLowerCase()
  const lista = q
    ? personal.value.filter((item) =>
        `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
      )
    : personal.value
  const seleccion = eppPersonaSeleccionada.value
  if (seleccion && !lista.some((item) => item.personaId === seleccion.personaId)) {
    return [seleccion, ...lista]
  }
  return lista
})

const capPersonaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === capAgregarPersonaId.value) ?? null,
)

const opcionesCapPersona = computed(() => {
  const ids = new Set(capAsistentes.value.map((item) => item.personaId))
  const q = capPersonaSearch.value.trim().toLowerCase()
  return personal.value.filter((item) => {
    if (ids.has(item.personaId)) {
      return false
    }
    if (!q) {
      return true
    }
    return `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q)
  })
})

const partidasVigentes = computed(() =>
  partidas.value.filter((item): item is PartidaPresupuesto & { id: number } => item.id != null),
)

const nivelInicialIpercVivo = computed(() => nivelVivo(ipercForm.probabilidadInicial, ipercForm.severidadInicial))
const nivelResidualIpercVivo = computed(() => nivelVivo(ipercForm.probabilidadResidual, ipercForm.severidadResidual))

const charlaExpositorSeleccionado = computed(
  () => personal.value.find((item) => item.personaId === charlaForm.expositorPersonaId) ?? null,
)

const opcionesCharlaExpositor = computed(() => {
  const q = charlaExpositorSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const charlaPersonaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === charlaAgregarPersonaId.value) ?? null,
)

const opcionesCharlaPersona = computed(() => {
  const ids = new Set(charlaAsistentes.value.map((item) => item.personaId))
  const q = charlaPersonaSearch.value.trim().toLowerCase()
  return personal.value.filter((item) => {
    if (ids.has(item.personaId)) {
      return false
    }
    if (!q) {
      return true
    }
    return `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q)
  })
})

const charlasAtsDelDia = computed(() =>
  charlasAts.value.filter((item) => item.fecha === charlaForm.fecha),
)

const petarsDelDia = computed(() => petars.value.filter((item) => item.fecha === petarForm.fecha))

const petarResidenteSeleccionado = computed(
  () => personal.value.find((item) => item.personaId === petarForm.residentePersonaId) ?? null,
)

const opcionesPetarResidente = computed(() => {
  const q = petarResidenteSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const petarSupervisorSeleccionado = computed(
  () => personal.value.find((item) => item.personaId === petarForm.supervisorSsomaId) ?? null,
)

const opcionesPetarSupervisor = computed(() => {
  const q = petarSupervisorSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const petarPersonaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === petarAgregarPersonaId.value) ?? null,
)

const opcionesPetarPersona = computed(() => {
  const ids = new Set(petarTrabajadores.value.map((item) => item.personaId))
  const q = petarPersonaSearch.value.trim().toLowerCase()
  return personal.value.filter((item) => {
    if (ids.has(item.personaId)) {
      return false
    }
    if (!q) {
      return true
    }
    return `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q)
  })
})

const petarExigeAtmosfera = computed(() => petarForm.tipoTrabajo === 'ESPACIO_CONFINADO')
const petarExigeViento = computed(
  () => petarForm.tipoTrabajo === 'ALTURA' || petarForm.tipoTrabajo === 'IZAJE_CARGAS',
)

const inspectorSeleccionado = computed(
  () => personal.value.find((item) => item.personaId === inspeccionForm.inspectorPersonaId) ?? null,
)

const opcionesInspector = computed(() => {
  const q = inspectorSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const responsableSeleccionado = computed(
  () => personal.value.find((item) => item.personaId === hallazgoForm.responsablePersonaId) ?? null,
)

const opcionesResponsable = computed(() => {
  const q = responsableSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const hallazgosFiltrados = computed(() =>
  hallazgos.value.filter((item) => {
    if (filtroEstadoHallazgo.value && item.estado !== filtroEstadoHallazgo.value) {
      return false
    }
    if (filtroVencidosHallazgo.value && !item.vencido) {
      return false
    }
    return true
  }),
)

const preUsosDelDia = computed(() => preUsos.value.filter((item) => item.fecha === preUsoForm.fecha))

const preUsoOperadorSeleccionado = computed(
  () => personal.value.find((item) => item.personaId === preUsoForm.operadorPersonaId) ?? null,
)

const opcionesPreUsoOperador = computed(() => {
  const q = preUsoOperadorSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const eventoPersonaSeleccionada = computed(
  () => personal.value.find((item) => item.personaId === eventoForm.personaAfectadaId) ?? null,
)

const opcionesEventoPersona = computed(() => {
  const q = eventoPersonaSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const eventoMedidaResponsableSeleccionado = computed(
  () => personal.value.find((item) => item.personaId === eventoMedidaForm.responsablePersonaId) ?? null,
)

const opcionesEventoMedidaResponsable = computed(() => {
  const q = eventoMedidaResponsableSearch.value.trim().toLowerCase()
  if (!q) {
    return personal.value
  }
  return personal.value.filter((item) =>
    `${item.apellidos} ${item.nombres} ${item.numeroDocumento}`.toLowerCase().includes(q),
  )
})

const eventoSeleccionado = computed(
  () => eventos.value.find((item) => item.id === eventoEditandoId.value) ?? null,
)

const avisoMtpeEvento = computed(
  () => eventoForm.tipoEvento === 'INCIDENTE_PELIGROSO' || eventoForm.tipoEvento === 'ACCIDENTE_MORTAL',
)

const eventoEnInvestigacion = computed(
  () => !eventoEditandoId.value || eventoSeleccionado.value?.estadoInvestigacion === 'EN_INVESTIGACION',
)

const estadisticaMesActual = computed(() => {
  const mes = new Date().getMonth() + 1
  return estadisticas.value?.meses.find((item) => item.mes === mes) ?? null
})

const filasHabilitacion = computed(() => {
  const filas = habilitacion.value?.filas ?? []
  if (filtroHabilitacion.value === 'TODOS') {
    return filas
  }
  return filas.filter((fila) => fila.estado === filtroHabilitacion.value)
})

const historialEppPorPersona = computed(() => {
  const grupos = new Map<
    number,
    { personaId: number; personaNombre: string; numeroDocumento: string; entregas: SomaEntregaEpp[] }
  >()
  for (const entrega of entregasEpp.value) {
    let grupo = grupos.get(entrega.personaId)
    if (!grupo) {
      grupo = {
        personaId: entrega.personaId,
        personaNombre: entrega.personaNombre || `Persona ${entrega.personaId}`,
        numeroDocumento: entrega.numeroDocumento || '',
        entregas: [],
      }
      grupos.set(entrega.personaId, grupo)
    }
    grupo.entregas.push(entrega)
  }
  return [...grupos.values()]
})

function hoyIso(): string {
  const fecha = new Date()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}`
}

function primerDiaMes(anio: number, mes: number): string {
  return `${anio}-${String(mes).padStart(2, '0')}-01`
}

function ultimoDiaMes(anio: number, mes: number): string {
  const fecha = new Date(anio, mes, 0)
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${anio}-${String(mes).padStart(2, '0')}-${dia}`
}

function mesLabel(mes: number): string {
  return MESES[mes - 1] ?? String(mes)
}

function n(valor: string): number {
  const parsed = Number(valor.replace(',', '.'))
  return Number.isFinite(parsed) ? parsed : 0
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

function comitePersonaLabel(persona: PersonaResumenDTO | null): string {
  if (!persona) {
    return ''
  }
  return `${persona.apellidos}, ${persona.nombres} · ${persona.numeroDocumento}`
}

function representacionComiteLabel(valor: RepresentacionComiteSoma): string {
  return REPRESENTACIONES_COMITE.find((item) => item.valor === valor)?.label ?? valor
}

function cargoComiteLabel(valor: CargoComiteSoma): string {
  return CARGOS_COMITE.find((item) => item.valor === valor)?.label ?? valor
}

function tipoSesionComiteLabel(valor: TipoSesionComiteSoma): string {
  return TIPOS_SESION_COMITE.find((item) => item.valor === valor)?.label ?? valor
}

function requerimientoComiteLabel(req: SomaComiteRequerimiento | null): string {
  if (!req) {
    return ''
  }
  if (req.tipoRequerido === 'SUBCOMITE') {
    return `Subcomité SST (≥ 20 trabajadores activos). Hay ${req.trabajadoresActivos}.`
  }
  return `Supervisor SST (< 20 trabajadores activos). Hay ${req.trabajadoresActivos}.`
}

function avisarComite(req: SomaComiteRequerimiento | null) {
  if (!req?.avisos.length) {
    return
  }
  toast.warning(req.avisos.join(' · '))
}

async function buscarPersonasComite(q: string) {
  const data = await listarPersonas({ page: 0, size: 15, q })
  comitePersonas.value = data.content
  if (
    comitePersonaSeleccionada.value &&
    !comitePersonas.value.some((item) => item.id === comitePersonaSeleccionada.value?.id)
  ) {
    comitePersonas.value = [comitePersonaSeleccionada.value, ...comitePersonas.value]
  }
}

function scheduleSearchComite(q: string) {
  if (comitePersonaTimer) {
    clearTimeout(comitePersonaTimer)
  }
  comitePersonaTimer = setTimeout(() => {
    void buscarPersonasComite(q)
  }, 300)
}

function onComitePersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(id)) {
    comiteMiembroForm.personaId = null
    comitePersonaSeleccionada.value = null
    return
  }
  comiteMiembroForm.personaId = id
  comitePersonaSeleccionada.value =
    comitePersonas.value.find((item) => item.id === id) ?? comitePersonaSeleccionada.value
}

async function recargarComite() {
  const [req, miembros, actas] = await Promise.all([
    obtenerRequerimientoComite(proyectoId.value),
    listarMiembrosComite(proyectoId.value),
    listarActasComite(proyectoId.value),
  ])
  comiteRequerimiento.value = req
  comiteMiembros.value = miembros
  comiteActas.value = actas
  avisarComite(req)
}

async function onRegistrarMiembroComite() {
  if (!comiteMiembroForm.personaId) {
    toast.error('Seleccione una persona.')
    return
  }
  if (!comiteMiembroForm.fechaInicio) {
    toast.error('Indique la fecha de inicio.')
    return
  }
  if (comiteMiembroForm.fechaFin && comiteMiembroForm.fechaFin < comiteMiembroForm.fechaInicio) {
    toast.error('La fecha de fin no puede ser menor que la de inicio.')
    return
  }
  comiteMiembroSaving.value = true
  try {
    await crearMiembroComite(proyectoId.value, {
      personaId: comiteMiembroForm.personaId,
      representacion: comiteMiembroForm.representacion,
      cargoComite: comiteMiembroForm.cargoComite,
      titular: comiteMiembroForm.titular,
      fechaInicio: comiteMiembroForm.fechaInicio,
      fechaFin: comiteMiembroForm.fechaFin || null,
    })
    toast.success('Miembro registrado.')
    comiteMiembroForm.personaId = null
    comitePersonaSeleccionada.value = null
    comiteMiembroForm.fechaInicio = hoyIso()
    comiteMiembroForm.fechaFin = ''
    comiteMiembroForm.titular = true
    await recargarComite()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el miembro.')
  } finally {
    comiteMiembroSaving.value = false
  }
}

async function onDarBajaMiembroComite(miembro: SomaComiteMiembro) {
  comiteMiembroSaving.value = true
  try {
    await darBajaMiembroComite(proyectoId.value, miembro.id)
    toast.success('Miembro dado de baja.')
    await recargarComite()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo dar de baja.')
  } finally {
    comiteMiembroSaving.value = false
  }
}

async function onRegistrarActaComite() {
  if (!comiteActaForm.numeroActa.trim()) {
    toast.error('Indique el número de acta.')
    return
  }
  if (!comiteActaForm.fecha) {
    toast.error('Indique la fecha del acta.')
    return
  }
  if (!comiteActaForm.agenda.trim()) {
    toast.error('Indique la agenda.')
    return
  }
  if (!comiteActaForm.acuerdos.trim()) {
    toast.error('Indique los acuerdos.')
    return
  }
  comiteActaSaving.value = true
  try {
    const archivo = comiteFileInput.value?.files?.[0]
    let actaDocId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'EVIDENCIA_SOMA', archivo)
      actaDocId = documento.id
    }
    await crearActaComite(proyectoId.value, {
      numeroActa: comiteActaForm.numeroActa.trim(),
      tipoSesion: comiteActaForm.tipoSesion,
      fecha: comiteActaForm.fecha,
      agenda: comiteActaForm.agenda.trim(),
      acuerdos: comiteActaForm.acuerdos.trim(),
      actaDocId,
    })
    toast.success('Acta registrada.')
    comiteActaForm.numeroActa = ''
    comiteActaForm.tipoSesion = 'ORDINARIA'
    comiteActaForm.fecha = hoyIso()
    comiteActaForm.agenda = ''
    comiteActaForm.acuerdos = ''
    if (comiteFileInput.value) {
      comiteFileInput.value.value = ''
    }
    await recargarComite()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el acta.')
  } finally {
    comiteActaSaving.value = false
  }
}

function categoriaResiduoLabel(valor: CategoriaResiduoSoma): string {
  return CATEGORIAS_RESIDUO.find((item) => item.valor === valor)?.label ?? valor
}

function tipoAgenteMonitoreoLabel(valor: TipoAgenteMonitoreoSoma): string {
  return TIPOS_AGENTE_MONITOREO.find((item) => item.valor === valor)?.label ?? valor
}

function nOpcional(valor: string): number | null {
  const trimmed = valor.trim()
  if (!trimmed) {
    return null
  }
  const parsed = Number(trimmed.replace(',', '.'))
  return Number.isFinite(parsed) ? parsed : null
}

async function recargarAmbiental() {
  const [listaResiduos, listaMonitoreos] = await Promise.all([
    listarResiduosObra(proyectoId.value),
    listarMonitoreosObra(proyectoId.value),
  ])
  residuos.value = listaResiduos
  monitoreos.value = listaMonitoreos
}

async function onRegistrarResiduo() {
  if (!residuoForm.fecha) {
    toast.error('Indique la fecha.')
    return
  }
  if (!residuoForm.descripcion.trim()) {
    toast.error('Indique la descripción.')
    return
  }
  const cantidadKg = nOpcional(residuoForm.cantidadKg)
  const volumenM3 = nOpcional(residuoForm.volumenM3)
  if ((cantidadKg == null || cantidadKg <= 0) && (volumenM3 == null || volumenM3 <= 0)) {
    toast.error('Indique una cantidad en kg o un volumen en m³ mayor que cero.')
    return
  }
  if (residuoForm.categoriaResiduo === 'PELIGROSO_ROJO') {
    if (!residuoForm.eorsRazonSocial.trim() || !residuoForm.eorsRuc.trim() || !residuoForm.numeroManifiesto.trim()) {
      toast.error('El residuo peligroso exige EO-RS (RUC, razón social) y número de manifiesto.')
      return
    }
    if (!/^\d{11}$/.test(residuoForm.eorsRuc.trim())) {
      toast.error('El RUC del EO-RS debe tener 11 dígitos.')
      return
    }
  }
  residuoSaving.value = true
  try {
    const archivo = residuoFileInput.value?.files?.[0]
    let manifiestoDocId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'MANIFIESTO_AMBIENTAL', archivo)
      manifiestoDocId = documento.id
    }
    await crearResiduoObra(proyectoId.value, {
      fecha: residuoForm.fecha,
      categoriaResiduo: residuoForm.categoriaResiduo,
      descripcion: residuoForm.descripcion.trim(),
      cantidadKg: cantidadKg != null && cantidadKg > 0 ? cantidadKg : null,
      volumenM3: volumenM3 != null && volumenM3 > 0 ? volumenM3 : null,
      eorsRazonSocial: residuoForm.eorsRazonSocial.trim() || null,
      eorsRuc: residuoForm.eorsRuc.trim() || null,
      numeroManifiesto: residuoForm.numeroManifiesto.trim() || null,
      destinoFinal: residuoForm.destinoFinal.trim() || null,
      manifiestoDocId,
    })
    toast.success('Residuo registrado.')
    residuoForm.fecha = hoyIso()
    residuoForm.descripcion = ''
    residuoForm.cantidadKg = ''
    residuoForm.volumenM3 = ''
    residuoForm.eorsRazonSocial = ''
    residuoForm.eorsRuc = ''
    residuoForm.numeroManifiesto = ''
    residuoForm.destinoFinal = ''
    if (residuoFileInput.value) {
      residuoFileInput.value.value = ''
    }
    await recargarAmbiental()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el residuo.')
  } finally {
    residuoSaving.value = false
  }
}

async function onRegistrarMonitoreo() {
  if (!monitoreoForm.fecha) {
    toast.error('Indique la fecha.')
    return
  }
  if (!monitoreoForm.parametroMedido.trim()) {
    toast.error('Indique el parámetro medido.')
    return
  }
  if (!monitoreoForm.puntoMedicion.trim()) {
    toast.error('Indique el punto de medición.')
    return
  }
  if (!monitoreoForm.unidad.trim()) {
    toast.error('Indique la unidad.')
    return
  }
  const valorMedido = nOpcional(monitoreoForm.valorMedido)
  const limitePermisible = nOpcional(monitoreoForm.limitePermisible)
  if (valorMedido == null) {
    toast.error('Indique el valor medido.')
    return
  }
  if (limitePermisible == null) {
    toast.error('Indique el límite permisible.')
    return
  }
  monitoreoSaving.value = true
  try {
    const archivo = monitoreoFileInput.value?.files?.[0]
    let informeDocId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'EVIDENCIA_SOMA', archivo)
      informeDocId = documento.id
    }
    await crearMonitoreoObra(proyectoId.value, {
      fecha: monitoreoForm.fecha,
      tipoAgente: monitoreoForm.tipoAgente,
      parametroMedido: monitoreoForm.parametroMedido.trim(),
      puntoMedicion: monitoreoForm.puntoMedicion.trim(),
      valorMedido,
      limitePermisible,
      unidad: monitoreoForm.unidad.trim(),
      laboratorioNombre: monitoreoForm.laboratorioNombre.trim() || null,
      informeDocId,
    })
    toast.success('Monitoreo registrado.')
    monitoreoForm.fecha = hoyIso()
    monitoreoForm.parametroMedido = ''
    monitoreoForm.puntoMedicion = ''
    monitoreoForm.valorMedido = ''
    monitoreoForm.limitePermisible = ''
    monitoreoForm.unidad = ''
    monitoreoForm.laboratorioNombre = ''
    if (monitoreoFileInput.value) {
      monitoreoFileInput.value.value = ''
    }
    await recargarAmbiental()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el monitoreo.')
  } finally {
    monitoreoSaving.value = false
  }
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

function estadoHabilitacionBadge(estado: EstadoHabilitacionSoma): 'success' | 'secondary' {
  return estado === 'HABILITADO' ? 'success' : 'secondary'
}

function estadoHabilitacionLabel(estado: EstadoHabilitacionSoma): string {
  return estado === 'HABILITADO' ? 'Habilitado' : 'Bloqueado'
}

function motivosLabel(motivos: string[]): string {
  return motivos.length ? motivos.join(' · ') : '—'
}

function nivelVivo(probabilidad: string, severidad: string): NivelRiesgoIperc | null {
  const p = Number(probabilidad)
  const s = Number(severidad)
  if (!Number.isInteger(p) || !Number.isInteger(s) || p < 1 || s < 1) {
    return null
  }
  return nivelRiesgoIpercDesdePuntaje(p * s)
}

function nivelIpercBadge(nivel: NivelRiesgoIperc): 'success' | 'default' | 'secondary' {
  if (nivel === 'TRIVIAL' || nivel === 'TOLERABLE') {
    return 'success'
  }
  if (nivel === 'MODERADO') {
    return 'default'
  }
  return 'secondary'
}

function nivelIpercLabel(nivel: NivelRiesgoIperc): string {
  if (nivel === 'TRIVIAL') {
    return 'Trivial'
  }
  if (nivel === 'TOLERABLE') {
    return 'Tolerable'
  }
  if (nivel === 'MODERADO') {
    return 'Moderado'
  }
  if (nivel === 'IMPORTANTE') {
    return 'Importante'
  }
  return 'Intolerable'
}

function tipoPeligroLabel(tipo: TipoPeligroIperc): string {
  return TIPOS_PELIGRO_IPERC.find((item) => item.valor === tipo)?.label ?? tipo
}

function resetIpercForm() {
  ipercEditandoId.value = null
  ipercForm.partidaId = ''
  ipercForm.actividadTarea = ''
  ipercForm.puestoTrabajo = ''
  ipercForm.rutinario = true
  ipercForm.tipoPeligro = 'MECANICO'
  ipercForm.peligro = ''
  ipercForm.riesgo = ''
  ipercForm.consecuencia = ''
  ipercForm.requisitoLegal = ''
  ipercForm.probabilidadInicial = '3'
  ipercForm.severidadInicial = '3'
  ipercForm.controlEliminacion = ''
  ipercForm.controlSustitucion = ''
  ipercForm.controlIngenieria = ''
  ipercForm.controlAdministrativo = ''
  ipercForm.controlEpp = ''
  ipercForm.probabilidadResidual = '2'
  ipercForm.severidadResidual = '2'
}

function armaIpercRequest() {
  const pIni = Number(ipercForm.probabilidadInicial)
  const sIni = Number(ipercForm.severidadInicial)
  const pRes = Number(ipercForm.probabilidadResidual)
  const sRes = Number(ipercForm.severidadResidual)
  return {
    partidaId: ipercForm.partidaId ? Number(ipercForm.partidaId) : null,
    actividadTarea: ipercForm.actividadTarea.trim(),
    puestoTrabajo: ipercForm.puestoTrabajo.trim(),
    rutinario: ipercForm.rutinario,
    tipoPeligro: ipercForm.tipoPeligro,
    peligro: ipercForm.peligro.trim(),
    riesgo: ipercForm.riesgo.trim(),
    consecuencia: ipercForm.consecuencia.trim(),
    requisitoLegal: ipercForm.requisitoLegal.trim() || null,
    probabilidadInicial: pIni,
    severidadInicial: sIni,
    controlEliminacion: ipercForm.controlEliminacion.trim() || null,
    controlSustitucion: ipercForm.controlSustitucion.trim() || null,
    controlIngenieria: ipercForm.controlIngenieria.trim() || null,
    controlAdministrativo: ipercForm.controlAdministrativo.trim() || null,
    controlEpp: ipercForm.controlEpp.trim() || null,
    probabilidadResidual: pRes,
    severidadResidual: sRes,
  }
}

async function recargarIperc() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  try {
    filasIperc.value = await listarIpercObra(
      proyectoId.value,
      filtroPartidaIperc.value ? Number(filtroPartidaIperc.value) : undefined,
      incluirInactivosIperc.value,
    )
  } catch (err) {
    filasIperc.value = []
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar el IPERC.')
  }
}

function editarIperc(fila: SomaIpercFila) {
  ipercEditandoId.value = fila.id
  ipercForm.partidaId = fila.partidaId ? String(fila.partidaId) : ''
  ipercForm.actividadTarea = fila.actividadTarea
  ipercForm.puestoTrabajo = fila.puestoTrabajo
  ipercForm.rutinario = fila.rutinario
  ipercForm.tipoPeligro = fila.tipoPeligro
  ipercForm.peligro = fila.peligro
  ipercForm.riesgo = fila.riesgo
  ipercForm.consecuencia = fila.consecuencia
  ipercForm.requisitoLegal = fila.requisitoLegal ?? ''
  ipercForm.probabilidadInicial = String(fila.probabilidadInicial)
  ipercForm.severidadInicial = String(fila.severidadInicial)
  ipercForm.controlEliminacion = fila.controlEliminacion ?? ''
  ipercForm.controlSustitucion = fila.controlSustitucion ?? ''
  ipercForm.controlIngenieria = fila.controlIngenieria ?? ''
  ipercForm.controlAdministrativo = fila.controlAdministrativo ?? ''
  ipercForm.controlEpp = fila.controlEpp ?? ''
  ipercForm.probabilidadResidual = String(fila.probabilidadResidual)
  ipercForm.severidadResidual = String(fila.severidadResidual)
}

async function onGuardarIperc() {
  const request = armaIpercRequest()
  if (!request.actividadTarea || !request.puestoTrabajo || !request.peligro || !request.riesgo || !request.consecuencia) {
    toast.error('Complete actividad, puesto, peligro, riesgo y consecuencia.')
    return
  }
  if (
    !request.controlEliminacion
    && !request.controlSustitucion
    && !request.controlIngenieria
    && !request.controlAdministrativo
    && !request.controlEpp
  ) {
    toast.error('Indique al menos un control.')
    return
  }
  ipercSaving.value = true
  try {
    if (ipercEditandoId.value) {
      await actualizarIpercObra(proyectoId.value, ipercEditandoId.value, request)
      toast.success('Fila IPERC actualizada.')
    } else {
      await crearIpercObra(proyectoId.value, request)
      toast.success('Fila IPERC registrada.')
    }
    resetIpercForm()
    await recargarIperc()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el IPERC.')
  } finally {
    ipercSaving.value = false
  }
}

async function onDesactivarIperc(fila: SomaIpercFila) {
  ipercSaving.value = true
  try {
    await desactivarIpercObra(proyectoId.value, fila.id)
    if (ipercEditandoId.value === fila.id) {
      resetIpercForm()
    }
    toast.success('Fila IPERC desactivada.')
    await recargarIperc()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo desactivar la fila.')
  } finally {
    ipercSaving.value = false
  }
}

function resetCharlaForm() {
  charlaForm.fecha = hoyIso()
  charlaForm.horaInicio = '07:00'
  charlaForm.duracionMinutos = '10'
  charlaForm.temaCharla = ''
  charlaForm.cuadrillaId = ''
  charlaForm.partidaId = ''
  charlaForm.expositorPersonaId = null
  charlaForm.eppVerificado = false
  charlaForm.herramientasVerif = false
  charlaForm.observaciones = ''
  charlaPasos.value = [{ paso: '', peligros: '', controles: '' }]
  charlaAsistentes.value = []
  charlaAgregarPersonaId.value = null
  charlaPersonaSearch.value = ''
  charlaExpositorSearch.value = ''
}

function onCharlaExpositorChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  charlaForm.expositorPersonaId = Number.isFinite(id) ? id : null
}

function onCharlaPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  charlaAgregarPersonaId.value = Number.isFinite(id) ? id : null
}

function agregarPasoAts() {
  charlaPasos.value.push({ paso: '', peligros: '', controles: '' })
}

function quitarPasoAts(index: number) {
  if (charlaPasos.value.length === 1) {
    charlaPasos.value = [{ paso: '', peligros: '', controles: '' }]
    return
  }
  charlaPasos.value.splice(index, 1)
}

function agregarAsistenteCharla() {
  if (!charlaAgregarPersonaId.value) {
    toast.error('Seleccione una persona para agregar.')
    return
  }
  if (charlaAsistentes.value.some((item) => item.personaId === charlaAgregarPersonaId.value)) {
    toast.error('La persona ya está en la lista de asistentes.')
    return
  }
  charlaAsistentes.value.push({
    personaId: charlaAgregarPersonaId.value,
    observacion: '',
    habilitadoSoma: null,
  })
  charlaAgregarPersonaId.value = null
  charlaPersonaSearch.value = ''
}

function quitarAsistenteCharla(personaId: number) {
  charlaAsistentes.value = charlaAsistentes.value.filter((item) => item.personaId !== personaId)
}

async function onPrecargarCuadrillaCharla() {
  if (!charlaForm.cuadrillaId) {
    toast.error('Seleccione una cuadrilla.')
    return
  }
  try {
    const filas = await precargarCuadrillaCharlaAts(
      proyectoId.value,
      Number(charlaForm.cuadrillaId),
      charlaForm.fecha || undefined,
    )
    const vistos = new Set(charlaAsistentes.value.map((item) => item.personaId))
    for (const fila of filas) {
      if (vistos.has(fila.personaId)) {
        continue
      }
      vistos.add(fila.personaId)
      charlaAsistentes.value.push({
        personaId: fila.personaId,
        observacion: fila.observacion ?? '',
        habilitadoSoma: fila.habilitadoSoma,
      })
    }
    if (!filas.length) {
      toast.error('La cuadrilla no tiene miembros.')
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo precargar la cuadrilla.')
  }
}

async function onRegistrarCharlaAts() {
  if (!charlaForm.fecha) {
    toast.error('Indique la fecha.')
    return
  }
  if (!charlaForm.horaInicio) {
    toast.error('Indique la hora de inicio.')
    return
  }
  if (!charlaForm.temaCharla.trim()) {
    toast.error('Indique el tema de la charla.')
    return
  }
  const duracion = n(charlaForm.duracionMinutos)
  if (!Number.isFinite(duracion) || duracion <= 0) {
    toast.error('La duración debe ser mayor que 0.')
    return
  }
  const pasos = charlaPasos.value
    .map((fila) => ({
      paso: fila.paso.trim(),
      peligros: fila.peligros.trim() || null,
      controles: fila.controles.trim() || null,
    }))
    .filter((fila) => fila.paso)
  if (!pasos.length) {
    toast.error('El ATS debe tener al menos un paso.')
    return
  }
  charlaSaving.value = true
  try {
    const creada = await crearCharlaAtsObra(proyectoId.value, {
      cuadrillaId: charlaForm.cuadrillaId ? Number(charlaForm.cuadrillaId) : null,
      partidaId: charlaForm.partidaId ? Number(charlaForm.partidaId) : null,
      fecha: charlaForm.fecha,
      horaInicio: charlaForm.horaInicio,
      duracionMinutos: duracion,
      temaCharla: charlaForm.temaCharla.trim(),
      expositorPersonaId: charlaForm.expositorPersonaId,
      pasos,
      eppVerificado: charlaForm.eppVerificado,
      herramientasVerif: charlaForm.herramientasVerif,
      observaciones: charlaForm.observaciones.trim() || null,
      asistentes: charlaAsistentes.value.map((item) => ({
        personaId: item.personaId,
        observacion: item.observacion.trim() || null,
      })),
    })
    toast.success('Charla y ATS registrados.')
    if (creada.advertencia) {
      toast.warning(creada.advertencia)
    }
    resetCharlaForm()
    charlasAts.value = await listarCharlasAtsObra(proyectoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la charla.')
  } finally {
    charlaSaving.value = false
  }
}

function horaCorta(valor: string | null | undefined): string {
  if (!valor) {
    return ''
  }
  return valor.length >= 5 ? valor.slice(0, 5) : valor
}

function tipoPetarLabel(tipo: TipoTrabajoPetar): string {
  return TIPOS_PETAR.find((item) => item.valor === tipo)?.label ?? tipo
}

function estadoPetarLabel(estado: EstadoPetar): string {
  return ESTADOS_PETAR.find((item) => item.valor === estado)?.label ?? estado
}

function prefillResponsablesPetar() {
  if (petarForm.residentePersonaId == null) {
    const residente = responsablesObra.value.find((item) => item.rol === 'RESIDENTE')
    if (residente) {
      petarForm.residentePersonaId = residente.personaId
    }
  }
  if (petarForm.supervisorSsomaId == null) {
    const supervisor = responsablesObra.value.find((item) => item.rol === 'SUPERVISOR_SSOMA')
    if (supervisor) {
      petarForm.supervisorSsomaId = supervisor.personaId
    }
  }
}

async function cargarChecklistPetar(tipo: TipoTrabajoPetar, valores?: Record<string, boolean>) {
  try {
    const items = await checklistPetarObra(proyectoId.value, tipo)
    petarChecklist.value = items.map((item) => ({
      clave: item.clave,
      etiqueta: item.etiqueta,
      valor: Boolean(valores?.[item.clave]),
    }))
  } catch {
    petarChecklist.value = []
  }
}

function resetPetarForm() {
  petarEditandoId.value = null
  petarForm.tipoTrabajo = 'ALTURA'
  petarForm.fecha = hoyIso()
  petarForm.horaInicio = '07:00'
  petarForm.horaFin = '17:00'
  petarForm.ubicacionFrente = ''
  petarForm.descripcionTrabajo = ''
  petarForm.partidaId = ''
  petarForm.cuadrillaId = ''
  petarForm.residentePersonaId = null
  petarForm.supervisorSsomaId = null
  petarForm.oxigenoPct = ''
  petarForm.lelPct = ''
  petarForm.vientoKmh = ''
  petarTrabajadores.value = []
  petarAgregarPersonaId.value = null
  petarPersonaSearch.value = ''
  petarResidenteSearch.value = ''
  petarSupervisorSearch.value = ''
  prefillResponsablesPetar()
  void cargarChecklistPetar(petarForm.tipoTrabajo)
}

function onPetarResidenteChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  petarForm.residentePersonaId = Number.isFinite(id) ? id : null
}

function onPetarSupervisorChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  petarForm.supervisorSsomaId = Number.isFinite(id) ? id : null
}

function onPetarPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  petarAgregarPersonaId.value = Number.isFinite(id) ? id : null
}

function agregarTrabajadorPetar() {
  if (!petarAgregarPersonaId.value) {
    toast.error('Seleccione una persona para agregar.')
    return
  }
  if (petarTrabajadores.value.some((item) => item.personaId === petarAgregarPersonaId.value)) {
    toast.error('La persona ya está en el PETAR.')
    return
  }
  petarTrabajadores.value.push({
    personaId: petarAgregarPersonaId.value,
    rolEnTarea: 'EJECUTANTE',
  })
  petarAgregarPersonaId.value = null
  petarPersonaSearch.value = ''
}

function quitarTrabajadorPetar(personaId: number) {
  petarTrabajadores.value = petarTrabajadores.value.filter((item) => item.personaId !== personaId)
}

function medicionTexto(mapa: Record<string, number> | undefined, clave: string): string {
  const valor = mapa?.[clave]
  return valor == null ? '' : String(valor)
}

function cargarPetarEnFormulario(petar: SomaPetar) {
  petarEditandoId.value = petar.id
  petarForm.tipoTrabajo = petar.tipoTrabajo
  petarForm.fecha = petar.fecha
  petarForm.horaInicio = horaCorta(petar.horaInicio)
  petarForm.horaFin = horaCorta(petar.horaFin)
  petarForm.ubicacionFrente = petar.ubicacionFrente
  petarForm.descripcionTrabajo = petar.descripcionTrabajo
  petarForm.partidaId = petar.partidaId ? String(petar.partidaId) : ''
  petarForm.cuadrillaId = petar.cuadrillaId ? String(petar.cuadrillaId) : ''
  petarForm.residentePersonaId = petar.residentePersonaId
  petarForm.supervisorSsomaId = petar.supervisorSsomaId
  petarForm.oxigenoPct = medicionTexto(petar.mediciones, 'oxigenoPct')
  petarForm.lelPct = medicionTexto(petar.mediciones, 'lelPct')
  petarForm.vientoKmh = medicionTexto(petar.mediciones, 'vientoKmh')
  petarTrabajadores.value = petar.trabajadores.map((item) => ({
    personaId: item.personaId,
    rolEnTarea: item.rolEnTarea || 'EJECUTANTE',
  }))
  void cargarChecklistPetar(petar.tipoTrabajo, petar.checklist)
}

function construirPetarRequest(): SomaPetarRequest | null {
  if (!petarForm.fecha) {
    toast.error('Indique la fecha del PETAR.')
    return null
  }
  if (!petarForm.horaInicio || !petarForm.horaFin) {
    toast.error('Indique hora de inicio y de fin.')
    return null
  }
  if (!petarForm.ubicacionFrente.trim()) {
    toast.error('Indique la ubicación / frente.')
    return null
  }
  if (!petarForm.descripcionTrabajo.trim()) {
    toast.error('Indique la descripción del trabajo.')
    return null
  }
  const checklist: Record<string, boolean> = {}
  for (const item of petarChecklist.value) {
    checklist[item.clave] = item.valor
  }
  const mediciones: Record<string, number> = {}
  if (petarExigeAtmosfera.value) {
    if (petarForm.oxigenoPct.trim()) {
      mediciones.oxigenoPct = n(petarForm.oxigenoPct)
    }
    if (petarForm.lelPct.trim()) {
      mediciones.lelPct = n(petarForm.lelPct)
    }
  }
  if (petarExigeViento.value && petarForm.vientoKmh.trim()) {
    mediciones.vientoKmh = n(petarForm.vientoKmh)
  }
  return {
    tipoTrabajo: petarForm.tipoTrabajo,
    partidaId: petarForm.partidaId ? Number(petarForm.partidaId) : null,
    cuadrillaId: petarForm.cuadrillaId ? Number(petarForm.cuadrillaId) : null,
    fecha: petarForm.fecha,
    horaInicio: petarForm.horaInicio,
    horaFin: petarForm.horaFin,
    ubicacionFrente: petarForm.ubicacionFrente.trim(),
    descripcionTrabajo: petarForm.descripcionTrabajo.trim(),
    checklist,
    mediciones,
    residentePersonaId: petarForm.residentePersonaId,
    supervisorSsomaId: petarForm.supervisorSsomaId,
    trabajadores: petarTrabajadores.value.map((item) => ({
      personaId: item.personaId,
      rolEnTarea: item.rolEnTarea.trim() || 'EJECUTANTE',
    })),
  }
}

async function recargarPetar() {
  const [lista, faltantes] = await Promise.all([
    listarPetarObra(proyectoId.value),
    faltantesTareoPetar(proyectoId.value),
  ])
  petars.value = lista
  petarFaltantes.value = faltantes
}

async function onGuardarPetar() {
  const request = construirPetarRequest()
  if (!request) {
    return
  }
  petarSaving.value = true
  try {
    if (petarEditandoId.value) {
      await actualizarPetarObra(proyectoId.value, petarEditandoId.value, request)
      toast.success('PETAR actualizado.')
    } else {
      await crearPetarObra(proyectoId.value, request)
      toast.success('PETAR registrado en borrador.')
    }
    resetPetarForm()
    await recargarPetar()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el PETAR.')
  } finally {
    petarSaving.value = false
  }
}

async function onTransicionPetar(petar: SomaPetar, accion: 'autorizar' | 'cerrar' | 'suspender' | 'reanudar') {
  petarSaving.value = true
  try {
    if (accion === 'autorizar') {
      await autorizarPetarObra(proyectoId.value, petar.id)
      toast.success('PETAR autorizado.')
    } else if (accion === 'cerrar') {
      await cerrarPetarObra(proyectoId.value, petar.id)
      toast.success('PETAR cerrado.')
    } else if (accion === 'suspender') {
      await suspenderPetarObra(proyectoId.value, petar.id)
      toast.success('PETAR suspendido.')
    } else {
      await reanudarPetarObra(proyectoId.value, petar.id)
      toast.success('PETAR reanudado.')
    }
    if (petarEditandoId.value === petar.id) {
      resetPetarForm()
    }
    await recargarPetar()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cambiar el estado del PETAR.')
  } finally {
    petarSaving.value = false
  }
}

function onInspectorChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  inspeccionForm.inspectorPersonaId = Number.isFinite(id) ? id : null
}

function onResponsableChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  hallazgoForm.responsablePersonaId = Number.isFinite(id) ? id : null
}

function resetInspeccionForm() {
  inspeccionForm.tipo = 'INOPINADA_DIARIA'
  inspeccionForm.fecha = hoyIso()
  inspeccionForm.areaFrente = ''
  inspeccionForm.subcontratoId = ''
  inspeccionForm.inspectorPersonaId = null
  inspeccionForm.puntajeCumplimiento = ''
  inspeccionForm.observaciones = ''
  inspectorSearch.value = ''
}

function resetHallazgoForm() {
  hallazgoForm.inspeccionId = ''
  hallazgoForm.clasificacion = 'CONDICION_SUBESTANDAR'
  hallazgoForm.potencialPerdida = 'MEDIO_MODERADO'
  hallazgoForm.descripcion = ''
  hallazgoForm.ubicacion = ''
  hallazgoForm.accionCorrectiva = ''
  hallazgoForm.responsablePersonaId = null
  hallazgoForm.fechaDeteccion = hoyIso()
  hallazgoForm.fechaLimite = hoyIso()
  hallazgoForm.paralizacionFrente = false
  responsableSearch.value = ''
}

function tipoInspeccionLabel(tipo: TipoInspeccionSoma): string {
  return TIPOS_INSPECCION.find((item) => item.valor === tipo)?.label ?? tipo
}

function clasificacionHallazgoLabel(valor: ClasificacionHallazgoSoma): string {
  return CLASIFICACIONES_HALLAZGO.find((item) => item.valor === valor)?.label ?? valor
}

function potencialHallazgoLabel(valor: PotencialPerdidaSoma): string {
  return POTENCIALES_HALLAZGO.find((item) => item.valor === valor)?.label ?? valor
}

function estadoHallazgoLabel(valor: EstadoHallazgoSoma): string {
  return ESTADOS_HALLAZGO.find((item) => item.valor === valor)?.label ?? valor
}

async function recargarInspeccionesYHallazgos() {
  const [listaInspecciones, listaHallazgos] = await Promise.all([
    listarInspeccionesObra(proyectoId.value),
    listarHallazgosObra(proyectoId.value),
  ])
  inspecciones.value = listaInspecciones
  hallazgos.value = listaHallazgos
}

async function onRegistrarInspeccion() {
  if (!inspeccionForm.fecha) {
    toast.error('Indique la fecha.')
    return
  }
  if (!inspeccionForm.areaFrente.trim()) {
    toast.error('Indique el área o frente.')
    return
  }
  if (inspeccionForm.tipo === 'SUBCONTRATISTA' && !inspeccionForm.subcontratoId) {
    toast.error('Seleccione el subcontrato.')
    return
  }
  let puntaje: number | null = null
  if (inspeccionForm.puntajeCumplimiento.trim()) {
    puntaje = n(inspeccionForm.puntajeCumplimiento)
    if (!Number.isFinite(puntaje) || puntaje < 0 || puntaje > 100) {
      toast.error('El puntaje debe estar entre 0 y 100.')
      return
    }
  }
  inspeccionSaving.value = true
  try {
    await crearInspeccionObra(proyectoId.value, {
      tipo: inspeccionForm.tipo,
      fecha: inspeccionForm.fecha,
      areaFrente: inspeccionForm.areaFrente.trim(),
      subcontratoId: inspeccionForm.subcontratoId ? Number(inspeccionForm.subcontratoId) : null,
      inspectorPersonaId: inspeccionForm.inspectorPersonaId,
      puntajeCumplimiento: puntaje,
      observaciones: inspeccionForm.observaciones.trim() || null,
    })
    toast.success('Inspección registrada.')
    resetInspeccionForm()
    await recargarInspeccionesYHallazgos()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la inspección.')
  } finally {
    inspeccionSaving.value = false
  }
}

async function onRegistrarHallazgo() {
  if (!hallazgoForm.descripcion.trim()) {
    toast.error('Indique la descripción.')
    return
  }
  if (!hallazgoForm.accionCorrectiva.trim()) {
    toast.error('Indique la acción correctiva.')
    return
  }
  if (!hallazgoForm.fechaDeteccion || !hallazgoForm.fechaLimite) {
    toast.error('Indique las fechas de detección y límite.')
    return
  }
  if (hallazgoForm.fechaLimite < hallazgoForm.fechaDeteccion) {
    toast.error('La fecha límite no puede ser anterior a la de detección.')
    return
  }
  if (hallazgoForm.paralizacionFrente && hallazgoForm.potencialPerdida !== 'ALTO_GRAVE') {
    toast.error('La paralización solo se permite con potencial alto / grave.')
    return
  }
  hallazgoSaving.value = true
  try {
    await crearHallazgoObra(proyectoId.value, {
      inspeccionId: hallazgoForm.inspeccionId ? Number(hallazgoForm.inspeccionId) : null,
      clasificacion: hallazgoForm.clasificacion,
      potencialPerdida: hallazgoForm.potencialPerdida,
      descripcion: hallazgoForm.descripcion.trim(),
      ubicacion: hallazgoForm.ubicacion.trim() || null,
      accionCorrectiva: hallazgoForm.accionCorrectiva.trim(),
      responsablePersonaId: hallazgoForm.responsablePersonaId,
      fechaDeteccion: hallazgoForm.fechaDeteccion,
      fechaLimite: hallazgoForm.fechaLimite,
      paralizacionFrente: hallazgoForm.paralizacionFrente,
    })
    toast.success(
      hallazgoForm.paralizacionFrente ? 'Hallazgo registrado y frente paralizado.' : 'Hallazgo registrado.',
    )
    resetHallazgoForm()
    await recargarInspeccionesYHallazgos()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el hallazgo.')
  } finally {
    hallazgoSaving.value = false
  }
}

async function onEnProcesoHallazgo(hallazgo: SomaHallazgo) {
  hallazgoSaving.value = true
  try {
    await marcarHallazgoEnProcesoObra(proyectoId.value, hallazgo.id)
    toast.success('Hallazgo en proceso.')
    await recargarInspeccionesYHallazgos()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo actualizar el hallazgo.')
  } finally {
    hallazgoSaving.value = false
  }
}

async function onLevantarHallazgo(hallazgo: SomaHallazgo, event: Event) {
  const input = event.target as HTMLInputElement
  const archivo = input.files?.[0]
  if (!archivo) {
    toast.error('Adjunte la evidencia de cierre.')
    return
  }
  hallazgoSaving.value = true
  try {
    const documento = await subirDocumentoObra(proyectoId.value, 'EVIDENCIA_SOMA', archivo)
    await levantarHallazgoObra(proyectoId.value, hallazgo.id, { evidenciaDespuesDocId: documento.id })
    toast.success('Hallazgo levantado.')
    await recargarInspeccionesYHallazgos()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo levantar el hallazgo.')
  } finally {
    hallazgoSaving.value = false
    input.value = ''
  }
}

function resultadoPreUsoLabel(resultado: ResultadoPreUsoEquipo): string {
  return RESULTADOS_PREUSO.find((item) => item.valor === resultado)?.label ?? resultado
}

function onPreUsoOperadorChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  preUsoForm.operadorPersonaId = Number.isFinite(id) ? id : null
}

function resetPreUsoForm() {
  preUsoForm.asignacionEquipoId = ''
  preUsoForm.fecha = hoyIso()
  preUsoForm.horometroLectura = ''
  preUsoForm.operadorPersonaId = null
  preUsoForm.resultado = 'OPERATIVO'
  preUsoForm.observaciones = ''
  preUsoOperadorSearch.value = ''
  preUsoChecklist.value = CHECKLIST_PREUSO.map((item) => ({ ...item, valor: false }))
}

async function onRegistrarPreUso() {
  if (!preUsoForm.asignacionEquipoId) {
    toast.error('Seleccione el equipo.')
    return
  }
  if (!preUsoForm.fecha) {
    toast.error('Indique la fecha.')
    return
  }
  const checklist: Record<string, boolean> = {}
  for (const item of preUsoChecklist.value) {
    checklist[item.clave] = item.valor
  }
  preUsoSaving.value = true
  try {
    await crearPreUsoObra(proyectoId.value, {
      asignacionEquipoId: Number(preUsoForm.asignacionEquipoId),
      operadorPersonaId: preUsoForm.operadorPersonaId,
      fecha: preUsoForm.fecha,
      horometroLectura: preUsoForm.horometroLectura.trim() ? n(preUsoForm.horometroLectura) : null,
      checklist,
      resultado: preUsoForm.resultado,
      observaciones: preUsoForm.observaciones.trim() || null,
    })
    toast.success('Pre-uso registrado.')
    resetPreUsoForm()
    preUsos.value = await listarPreUsoObra(proyectoId.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar el pre-uso.')
  } finally {
    preUsoSaving.value = false
  }
}

function ahoraLocalDatetime(): string {
  const fecha = new Date()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  const hora = String(fecha.getHours()).padStart(2, '0')
  const minuto = String(fecha.getMinutes()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}T${hora}:${minuto}`
}

function isoADatetimeLocal(iso: string): string {
  const fecha = new Date(iso)
  if (Number.isNaN(fecha.getTime())) {
    return ahoraLocalDatetime()
  }
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  const hora = String(fecha.getHours()).padStart(2, '0')
  const minuto = String(fecha.getMinutes()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}T${hora}:${minuto}`
}

function tipoEventoLabel(valor: TipoEventoSst): string {
  return TIPOS_EVENTO_SST.find((item) => item.valor === valor)?.label ?? valor
}

function fmtIndice(valor: number | string | null | undefined): string {
  const nro = Number(valor)
  return Number.isFinite(nro) ? nro.toFixed(2) : '0.00'
}

async function cargarEstadisticas() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  try {
    estadisticas.value = await obtenerEstadisticasObra(proyectoId.value, anioEstadistica.value)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudieron cargar las estadísticas.')
  }
}

function estadoInvestigacionLabel(valor: EstadoInvestigacionSst): string {
  return valor === 'CERRADO' ? 'Cerrado' : 'En investigación'
}

function onEventoPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  eventoForm.personaAfectadaId = Number.isFinite(id) ? id : null
}

function onEventoMedidaResponsableChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  eventoMedidaForm.responsablePersonaId = Number.isFinite(id) ? id : null
}

function agregarCausa(lista: string[], clave: keyof typeof causaDraft) {
  const valor = causaDraft[clave].trim()
  if (!valor) {
    return
  }
  lista.push(valor)
  causaDraft[clave] = ''
}

function quitarCausa(lista: string[], indice: number) {
  lista.splice(indice, 1)
}

function resetEventoMedidaForm() {
  eventoMedidaForm.clasificacion = 'CONDICION_SUBESTANDAR'
  eventoMedidaForm.potencialPerdida = 'MEDIO_MODERADO'
  eventoMedidaForm.descripcion = ''
  eventoMedidaForm.ubicacion = ''
  eventoMedidaForm.accionCorrectiva = ''
  eventoMedidaForm.responsablePersonaId = null
  eventoMedidaForm.fechaDeteccion = hoyIso()
  eventoMedidaForm.fechaLimite = hoyIso()
  eventoMedidaForm.paralizacionFrente = false
  eventoMedidaResponsableSearch.value = ''
}

function resetEventoForm() {
  eventoEditandoId.value = null
  eventoForm.tipoEvento = 'INCIDENTE'
  eventoForm.gravedadIncapacidad = ''
  eventoForm.fechaHora = ahoraLocalDatetime()
  eventoForm.lugarExacto = ''
  eventoForm.partidaId = ''
  eventoForm.personaAfectadaId = null
  eventoForm.subcontratoId = ''
  eventoForm.descripcionEvento = ''
  eventoForm.parteCuerpoLesionada = ''
  eventoForm.naturalezaLesion = ''
  eventoForm.agenteCausante = ''
  eventoForm.numeroCertificadoDm = ''
  eventoForm.diasCargoEstadistico = '0'
  eventoForm.fechaInicioDm = ''
  eventoForm.fechaFinDm = ''
  eventoForm.notificadoMtpe = false
  eventoForm.causasActos = []
  eventoForm.causasCondiciones = []
  eventoForm.causasFactoresPersonales = []
  eventoForm.causasFactoresTrabajo = []
  causaDraft.actos = ''
  causaDraft.condiciones = ''
  causaDraft.factoresPersonales = ''
  causaDraft.factoresTrabajo = ''
  eventoPersonaSearch.value = ''
  resetEventoMedidaForm()
}

function cargarEventoEnForm(fila: SomaEvento) {
  eventoEditandoId.value = fila.id
  eventoForm.tipoEvento = fila.tipoEvento
  eventoForm.gravedadIncapacidad = fila.gravedadIncapacidad ?? ''
  eventoForm.fechaHora = isoADatetimeLocal(fila.fechaHora)
  eventoForm.lugarExacto = fila.lugarExacto
  eventoForm.partidaId = fila.partidaId ? String(fila.partidaId) : ''
  eventoForm.personaAfectadaId = fila.personaAfectadaId
  eventoForm.subcontratoId = fila.subcontratoId ? String(fila.subcontratoId) : ''
  eventoForm.descripcionEvento = fila.descripcionEvento
  eventoForm.parteCuerpoLesionada = fila.parteCuerpoLesionada ?? ''
  eventoForm.naturalezaLesion = fila.naturalezaLesion ?? ''
  eventoForm.agenteCausante = fila.agenteCausante ?? ''
  eventoForm.numeroCertificadoDm = fila.numeroCertificadoDm ?? ''
  eventoForm.diasCargoEstadistico = String(fila.diasCargoEstadistico ?? 0)
  eventoForm.fechaInicioDm = fila.fechaInicioDm ?? ''
  eventoForm.fechaFinDm = fila.fechaFinDm ?? ''
  eventoForm.notificadoMtpe = fila.notificadoMtpe
  eventoForm.causasActos = [...(fila.causasInmediatas?.actos ?? [])]
  eventoForm.causasCondiciones = [...(fila.causasInmediatas?.condiciones ?? [])]
  eventoForm.causasFactoresPersonales = [...(fila.causasBasicas?.factoresPersonales ?? [])]
  eventoForm.causasFactoresTrabajo = [...(fila.causasBasicas?.factoresTrabajo ?? [])]
  eventoPersonaSearch.value = ''
  resetEventoMedidaForm()
}

function payloadEvento() {
  const cargo = Math.trunc(n(eventoForm.diasCargoEstadistico))
  return {
    tipoEvento: eventoForm.tipoEvento,
    gravedadIncapacidad:
      eventoForm.tipoEvento === 'ACCIDENTE_INCAPACITANTE' && eventoForm.gravedadIncapacidad
        ? eventoForm.gravedadIncapacidad
        : null,
    fechaHora: new Date(eventoForm.fechaHora).toISOString(),
    lugarExacto: eventoForm.lugarExacto.trim(),
    partidaId: eventoForm.partidaId ? Number(eventoForm.partidaId) : null,
    personaAfectadaId: eventoForm.personaAfectadaId,
    subcontratoId: eventoForm.subcontratoId ? Number(eventoForm.subcontratoId) : null,
    descripcionEvento: eventoForm.descripcionEvento.trim(),
    parteCuerpoLesionada: eventoForm.parteCuerpoLesionada.trim() || null,
    naturalezaLesion: eventoForm.naturalezaLesion.trim() || null,
    agenteCausante: eventoForm.agenteCausante.trim() || null,
    numeroCertificadoDm: eventoForm.numeroCertificadoDm.trim() || null,
    diasCargoEstadistico: Number.isFinite(cargo) ? cargo : 0,
    fechaInicioDm: eventoForm.fechaInicioDm || null,
    fechaFinDm: eventoForm.fechaFinDm || null,
    notificadoMtpe: eventoForm.notificadoMtpe,
    causasInmediatas: {
      actos: [...eventoForm.causasActos],
      condiciones: [...eventoForm.causasCondiciones],
    },
    causasBasicas: {
      factoresPersonales: [...eventoForm.causasFactoresPersonales],
      factoresTrabajo: [...eventoForm.causasFactoresTrabajo],
    },
  }
}

async function onGuardarEvento() {
  if (!eventoForm.fechaHora) {
    toast.error('Indique la fecha y hora.')
    return
  }
  if (Number.isNaN(new Date(eventoForm.fechaHora).getTime())) {
    toast.error('La fecha y hora no son válidas.')
    return
  }
  if (!eventoForm.lugarExacto.trim()) {
    toast.error('Indique el lugar exacto.')
    return
  }
  if (!eventoForm.descripcionEvento.trim()) {
    toast.error('Indique la descripción del evento.')
    return
  }
  if (
    eventoForm.tipoEvento === 'ACCIDENTE_INCAPACITANTE' &&
    !eventoForm.gravedadIncapacidad
  ) {
    toast.error('Indique la gravedad de la incapacidad.')
    return
  }
  if (
    eventoForm.tipoEvento !== 'INCIDENTE' &&
    !eventoForm.personaAfectadaId &&
    !eventoForm.subcontratoId
  ) {
    toast.error('Indique la persona afectada o el subcontrato.')
    return
  }
  if (eventoForm.fechaInicioDm && eventoForm.fechaFinDm && eventoForm.fechaFinDm < eventoForm.fechaInicioDm) {
    toast.error('La fecha fin de DM no puede ser anterior a la de inicio.')
    return
  }
  eventoSaving.value = true
  try {
    const request = payloadEvento()
    const guardado = eventoEditandoId.value
      ? await actualizarEventoObra(proyectoId.value, eventoEditandoId.value, request)
      : await crearEventoObra(proyectoId.value, request)
    toast.success(eventoEditandoId.value ? 'Evento actualizado.' : 'Evento registrado.')
    eventos.value = await listarEventosObra(proyectoId.value)
    const actual = eventos.value.find((item) => item.id === guardado.id)
    if (actual) {
      cargarEventoEnForm(actual)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar el evento.')
  } finally {
    eventoSaving.value = false
  }
}

async function onAgregarMedidaEvento() {
  if (!eventoEditandoId.value) {
    toast.error('Guarde el evento antes de agregar medidas.')
    return
  }
  if (!eventoMedidaForm.descripcion.trim()) {
    toast.error('Indique la descripción de la medida.')
    return
  }
  if (!eventoMedidaForm.accionCorrectiva.trim()) {
    toast.error('Indique la acción correctiva.')
    return
  }
  if (!eventoMedidaForm.fechaDeteccion || !eventoMedidaForm.fechaLimite) {
    toast.error('Indique las fechas de detección y límite.')
    return
  }
  if (eventoMedidaForm.fechaLimite < eventoMedidaForm.fechaDeteccion) {
    toast.error('La fecha límite no puede ser anterior a la detección.')
    return
  }
  if (eventoMedidaForm.paralizacionFrente && eventoMedidaForm.potencialPerdida !== 'ALTO_GRAVE') {
    toast.error('La paralización de frente exige potencial alto / grave.')
    return
  }
  eventoMedidaSaving.value = true
  try {
    await agregarHallazgoEventoObra(proyectoId.value, eventoEditandoId.value, {
      inspeccionId: null,
      clasificacion: eventoMedidaForm.clasificacion,
      potencialPerdida: eventoMedidaForm.potencialPerdida,
      descripcion: eventoMedidaForm.descripcion.trim(),
      ubicacion: eventoMedidaForm.ubicacion.trim() || null,
      accionCorrectiva: eventoMedidaForm.accionCorrectiva.trim(),
      responsablePersonaId: eventoMedidaForm.responsablePersonaId,
      fechaDeteccion: eventoMedidaForm.fechaDeteccion,
      fechaLimite: eventoMedidaForm.fechaLimite,
      paralizacionFrente: eventoMedidaForm.paralizacionFrente,
    })
    toast.success('Medida registrada.')
    resetEventoMedidaForm()
    eventos.value = await listarEventosObra(proyectoId.value)
    const actual = eventos.value.find((item) => item.id === eventoEditandoId.value)
    if (actual) {
      cargarEventoEnForm(actual)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la medida.')
  } finally {
    eventoMedidaSaving.value = false
  }
}

async function onCerrarEvento() {
  if (!eventoEditandoId.value) {
    toast.error('Seleccione un evento.')
    return
  }
  eventoSaving.value = true
  try {
    const cerrado = await cerrarEventoObra(proyectoId.value, eventoEditandoId.value)
    toast.success('Investigación cerrada.')
    eventos.value = await listarEventosObra(proyectoId.value)
    const actual = eventos.value.find((item) => item.id === cerrado.id)
    if (actual) {
      cargarEventoEnForm(actual)
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cerrar la investigación.')
  } finally {
    eventoSaving.value = false
  }
}

async function onSincronizarDmEvento() {
  if (!eventoEditandoId.value) {
    toast.error('Seleccione un evento.')
    return
  }
  eventoSaving.value = true
  try {
    const resultado = await sincronizarDmEventoObra(proyectoId.value, eventoEditandoId.value)
    const aplicados = resultado.aplicados.length
    const omitidos = resultado.omitidos
      .map((item) => `${item.fecha} (${item.motivo})`)
      .join(', ')
    toast.success(
      omitidos
        ? `Descanso médico: ${aplicados} aplicados. Omitidos: ${omitidos}.`
        : `Descanso médico: ${aplicados} aplicados.`,
    )
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo sincronizar el descanso médico.')
  } finally {
    eventoSaving.value = false
  }
}

watch(
  () => hallazgoForm.potencialPerdida,
  (valor) => {
    if (valor !== 'ALTO_GRAVE') {
      hallazgoForm.paralizacionFrente = false
    }
  },
)

watch(
  () => eventoMedidaForm.potencialPerdida,
  (valor) => {
    if (valor !== 'ALTO_GRAVE') {
      eventoMedidaForm.paralizacionFrente = false
    }
  },
)

watch(
  () => eventoForm.tipoEvento,
  (valor) => {
    if (valor !== 'ACCIDENTE_INCAPACITANTE') {
      eventoForm.gravedadIncapacidad = ''
    }
    if (valor !== 'INCIDENTE_PELIGROSO' && valor !== 'ACCIDENTE_MORTAL') {
      eventoForm.notificadoMtpe = false
    }
  },
)

async function recargarHabilitacion() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  try {
    habilitacion.value = await listarHabilitacionObra(
      proyectoId.value,
      fechaHabilitacion.value || undefined,
    )
  } catch (err) {
    habilitacion.value = null
    toast.error(err instanceof Error ? err.message : 'No se pudo cargar la habilitación.')
  }
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

function onEppPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  eppForm.personaId = Number.isFinite(id) ? id : null
}

function motivoEppLabel(motivo: MotivoEntregaEpp): string {
  return MOTIVOS_EPP.find((item) => item.valor === motivo)?.label ?? motivo
}

function estadoEppLabel(estado: EstadoItemEpp): string {
  return ESTADOS_EPP.find((item) => item.valor === estado)?.label ?? estado
}

function estadoEppBadge(estado: EstadoItemEpp): 'success' | 'default' | 'secondary' {
  if (estado === 'VIGENTE') {
    return 'success'
  }
  if (estado === 'DEVUELTO') {
    return 'default'
  }
  return 'secondary'
}

function resetEppForm() {
  eppForm.personaId = null
  eppForm.motivo = 'DOTACION_INICIAL'
  eppForm.fechaEntrega = hoyIso()
  eppForm.partidaId = ''
  eppForm.descontarAlmacen = true
  eppForm.firmadoTrabajador = false
  eppForm.observaciones = ''
  eppPersonaSearch.value = ''
  eppItems.value = [{ materialId: '', cantidad: '', talla: '' }]
}

function agregarItemEpp() {
  eppItems.value.push({ materialId: '', cantidad: '', talla: '' })
}

function quitarItemEpp(index: number) {
  if (eppItems.value.length === 1) {
    eppItems.value = [{ materialId: '', cantidad: '', talla: '' }]
    return
  }
  eppItems.value.splice(index, 1)
}

async function load() {
  if (!Number.isFinite(proyectoId.value) || proyectoId.value <= 0) {
    return
  }
  loading.value = true
  try {
    const [
      resumenObra,
      listaHabilitacion,
      listaEmos,
      listaPersonal,
      listaPolizas,
      listaEpps,
      listaMaterialesEpp,
      presupuesto,
      listaCaps,
      listaCuadrillas,
      listaIperc,
      listaCharlas,
      listaPetars,
      listaFaltantesPetar,
      listaResponsables,
      listaInspecciones,
      listaHallazgos,
      listaSubcontratos,
      listaEquipos,
      listaPreUsos,
      listaEventos,
      listaEstadisticas,
      listaComiteReq,
      listaComiteMiembros,
      listaComiteActas,
      listaComitePersonas,
      listaResiduos,
      listaMonitoreos,
    ] = await Promise.all([
      obtenerResumenSoma(proyectoId.value),
      listarHabilitacionObra(proyectoId.value, fechaHabilitacion.value || undefined),
      listarEmosObra(proyectoId.value),
      listarPersonalObra(proyectoId.value),
      listarPolizasSctr(proyectoId.value),
      listarEntregasEpp(proyectoId.value),
      listarMaterialesEpp(proyectoId.value).catch(() => []),
      obtenerPresupuesto(proyectoId.value).catch(() => null),
      listarCapacitacionesObra(proyectoId.value),
      listarCuadrillasObra(proyectoId.value).catch(() => []),
      listarIpercObra(
        proyectoId.value,
        filtroPartidaIperc.value ? Number(filtroPartidaIperc.value) : undefined,
        incluirInactivosIperc.value,
      ),
      listarCharlasAtsObra(proyectoId.value),
      listarPetarObra(proyectoId.value),
      faltantesTareoPetar(proyectoId.value),
      listarResponsablesObra(proyectoId.value).catch(() => []),
      listarInspeccionesObra(proyectoId.value),
      listarHallazgosObra(proyectoId.value),
      listarSubcontratosObra(proyectoId.value).catch(() => []),
      listarEquiposObra(proyectoId.value).catch(() => []),
      listarPreUsoObra(proyectoId.value),
      listarEventosObra(proyectoId.value),
      obtenerEstadisticasObra(proyectoId.value, anioEstadistica.value),
      obtenerRequerimientoComite(proyectoId.value),
      listarMiembrosComite(proyectoId.value),
      listarActasComite(proyectoId.value),
      listarPersonas({ page: 0, size: 15, q: '' }),
      listarResiduosObra(proyectoId.value),
      listarMonitoreosObra(proyectoId.value),
    ])
    resumen.value = resumenObra
    habilitacion.value = listaHabilitacion
    emos.value = listaEmos
    personal.value = uniquePersonal(listaPersonal)
    polizas.value = listaPolizas
    entregasEpp.value = listaEpps
    materialesEpp.value = listaMaterialesEpp
    partidas.value = presupuesto?.version?.estado === 'VIGENTE' ? presupuesto.version.partidas : []
    capacitaciones.value = listaCaps
    cuadrillas.value = listaCuadrillas
    filasIperc.value = listaIperc
    charlasAts.value = listaCharlas
    petars.value = listaPetars
    petarFaltantes.value = listaFaltantesPetar
    responsablesObra.value = listaResponsables
    inspecciones.value = listaInspecciones
    hallazgos.value = listaHallazgos
    subcontratos.value = listaSubcontratos
    equiposObra.value = listaEquipos
    preUsos.value = listaPreUsos
    eventos.value = listaEventos
    estadisticas.value = listaEstadisticas
    comiteRequerimiento.value = listaComiteReq
    comiteMiembros.value = listaComiteMiembros
    comiteActas.value = listaComiteActas
    comitePersonas.value = listaComitePersonas.content
    avisarComite(listaComiteReq)
    residuos.value = listaResiduos
    monitoreos.value = listaMonitoreos
    if (eventoEditandoId.value) {
      const actual = listaEventos.find((item) => item.id === eventoEditandoId.value)
      if (actual) {
        cargarEventoEnForm(actual)
      }
    }
    if (polizaSeleccionadaId.value && !listaPolizas.some((item) => item.id === polizaSeleccionadaId.value)) {
      polizaSeleccionadaId.value = null
      nominaEdicion.value = []
    }
    if (petarEditandoId.value == null) {
      prefillResponsablesPetar()
    }
    if (!petarChecklist.value.length) {
      await cargarChecklistPetar(petarForm.tipoTrabajo)
    }
  } catch (err) {
    resumen.value = null
    habilitacion.value = null
    emos.value = []
    personal.value = []
    polizas.value = []
    entregasEpp.value = []
    materialesEpp.value = []
    partidas.value = []
    capacitaciones.value = []
    cuadrillas.value = []
    filasIperc.value = []
    charlasAts.value = []
    petars.value = []
    petarFaltantes.value = []
    responsablesObra.value = []
    inspecciones.value = []
    hallazgos.value = []
    subcontratos.value = []
    equiposObra.value = []
    preUsos.value = []
    eventos.value = []
    estadisticas.value = null
    comiteRequerimiento.value = null
    comiteMiembros.value = []
    comiteActas.value = []
    comitePersonas.value = []
    residuos.value = []
    monitoreos.value = []
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

function sincronizarFechasSctr() {
  sctrForm.fechaInicio = primerDiaMes(sctrForm.anio, sctrForm.mes)
  sctrForm.fechaFin = ultimoDiaMes(sctrForm.anio, sctrForm.mes)
}

function resetSctrForm() {
  sctrForm.anio = anioActual
  sctrForm.mes = new Date().getMonth() + 1
  sctrForm.aseguradoraSalud = ''
  sctrForm.numeroPolizaSalud = ''
  sctrForm.aseguradoraPension = ''
  sctrForm.numeroPolizaPension = ''
  sctrForm.montoPrimaSalud = '0'
  sctrForm.montoPrimaPension = '0'
  sincronizarFechasSctr()
  if (sctrFileInput.value) {
    sctrFileInput.value.value = ''
  }
}

function seleccionarPoliza(item: SomaSctrPoliza) {
  polizaSeleccionadaId.value = item.id
  nominaEdicion.value = item.nomina.map((fila) => ({ ...fila }))
}

async function onCrearPoliza() {
  if (!sctrForm.aseguradoraSalud.trim() || !sctrForm.numeroPolizaSalud.trim()) {
    toast.error('Indique la aseguradora y el número de la póliza de salud.')
    return
  }
  if (!sctrForm.aseguradoraPension.trim() || !sctrForm.numeroPolizaPension.trim()) {
    toast.error('Indique la aseguradora y el número de la póliza de pensión.')
    return
  }
  if (sctrForm.fechaFin < sctrForm.fechaInicio) {
    toast.error('La fecha de fin no puede ser menor que la de inicio.')
    return
  }
  sctrSaving.value = true
  try {
    const archivo = sctrFileInput.value?.files?.[0]
    let documentoObraId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'SCTR_POLIZA', archivo)
      documentoObraId = documento.id
    }
    const creada = await crearPolizaSctr(proyectoId.value, {
      anio: sctrForm.anio,
      mes: sctrForm.mes,
      aseguradoraSalud: sctrForm.aseguradoraSalud.trim(),
      numeroPolizaSalud: sctrForm.numeroPolizaSalud.trim(),
      aseguradoraPension: sctrForm.aseguradoraPension.trim(),
      numeroPolizaPension: sctrForm.numeroPolizaPension.trim(),
      fechaInicio: sctrForm.fechaInicio,
      fechaFin: sctrForm.fechaFin,
      montoPrimaSalud: n(sctrForm.montoPrimaSalud),
      montoPrimaPension: n(sctrForm.montoPrimaPension),
      documentoObraId,
    })
    toast.success('Póliza SCTR registrada.')
    resetSctrForm()
    polizas.value = await listarPolizasSctr(proyectoId.value)
    seleccionarPoliza(polizas.value.find((item) => item.id === creada.id) ?? creada)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la póliza SCTR.')
  } finally {
    sctrSaving.value = false
  }
}

async function onPrecargarNomina() {
  const poliza = polizaSeleccionada.value
  if (!poliza) {
    toast.error('Seleccione una póliza.')
    return
  }
  sctrSaving.value = true
  try {
    const sugeridos = await sugerirNominaSctr(proyectoId.value, poliza.anio, poliza.mes)
    const existentes = new Map(nominaEdicion.value.map((fila) => [fila.personaId, fila]))
    for (const trabajador of sugeridos) {
      if (!existentes.has(trabajador.personaId)) {
        existentes.set(trabajador.personaId, {
          personaId: trabajador.personaId,
          personaNombre: `${trabajador.apellidos}, ${trabajador.nombres}`,
          numeroDocumento: trabajador.numeroDocumento,
          incluidoSalud: true,
          incluidoPension: true,
        })
      }
    }
    nominaEdicion.value = [...existentes.values()]
    toast.success('Nómina precargada con el personal del mes.')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo sugerir la nómina.')
  } finally {
    sctrSaving.value = false
  }
}

function quitarDeNomina(personaId: number) {
  nominaEdicion.value = nominaEdicion.value.filter((fila) => fila.personaId !== personaId)
}

async function onGuardarNomina() {
  const poliza = polizaSeleccionada.value
  if (!poliza) {
    toast.error('Seleccione una póliza.')
    return
  }
  sctrSaving.value = true
  try {
    const actualizada = await guardarNominaSctr(proyectoId.value, poliza.id, {
      filas: nominaEdicion.value.map((fila) => ({
        personaId: fila.personaId,
        incluidoSalud: fila.incluidoSalud,
        incluidoPension: fila.incluidoPension,
      })),
    })
    toast.success('Nómina SCTR guardada.')
    polizas.value = await listarPolizasSctr(proyectoId.value)
    seleccionarPoliza(polizas.value.find((item) => item.id === actualizada.id) ?? actualizada)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo guardar la nómina.')
  } finally {
    sctrSaving.value = false
  }
}

async function onAnularPoliza(item: SomaSctrPoliza) {
  if (!window.confirm(`¿Anular la póliza de ${mesLabel(item.mes)} ${item.anio}?`)) {
    return
  }
  sctrSaving.value = true
  try {
    await anularPolizaSctr(proyectoId.value, item.id)
    toast.success('Póliza anulada.')
    polizas.value = await listarPolizasSctr(proyectoId.value)
    if (polizaSeleccionadaId.value === item.id) {
      const actual = polizas.value.find((fila) => fila.id === item.id)
      if (actual) {
        seleccionarPoliza(actual)
      }
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo anular la póliza.')
  } finally {
    sctrSaving.value = false
  }
}

async function onRegistrarEpp() {
  if (!eppForm.personaId) {
    toast.error('Seleccione una persona del personal de la obra.')
    return
  }
  if (!eppForm.fechaEntrega) {
    toast.error('Indique la fecha de entrega.')
    return
  }
  if (eppForm.descontarAlmacen && !eppForm.partidaId) {
    toast.error('Seleccione la partida para descontar de almacén.')
    return
  }
  const items = eppItems.value
    .map((item) => ({
      materialId: Number(item.materialId),
      cantidad: n(item.cantidad),
      talla: item.talla.trim() || null,
    }))
    .filter((item) => Number.isFinite(item.materialId) && item.materialId > 0 && item.cantidad > 0)
  if (!items.length) {
    toast.error('Indique al menos un ítem de EPP con cantidad.')
    return
  }
  eppSaving.value = true
  try {
    await crearEntregaEpp(proyectoId.value, {
      personaId: eppForm.personaId,
      fechaEntrega: eppForm.fechaEntrega,
      motivo: eppForm.motivo,
      firmadoTrabajador: eppForm.firmadoTrabajador,
      descontarAlmacen: eppForm.descontarAlmacen,
      partidaId: eppForm.descontarAlmacen ? Number(eppForm.partidaId) : null,
      observaciones: eppForm.observaciones.trim() || null,
      items,
    })
    toast.success('Entrega de EPP registrada.')
    resetEppForm()
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la entrega de EPP.')
  } finally {
    eppSaving.value = false
  }
}

async function onCambiarEstadoEpp(itemId: number, estado: EstadoItemEpp) {
  try {
    await cambiarEstadoItemEpp(proyectoId.value, itemId, estado)
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo cambiar el estado.')
  }
}

function onEstadoSelect(itemId: number, event: Event) {
  const destino = event.target
  if (!(destino instanceof HTMLSelectElement)) {
    return
  }
  void onCambiarEstadoEpp(itemId, destino.value as EstadoItemEpp)
}

function tipoCapLabel(tipo: TipoCapacitacionSoma): string {
  return TIPOS_CAP.find((item) => item.valor === tipo)?.label ?? tipo
}

function personaDeId(personaId: number): PersonalObra | null {
  return personal.value.find((item) => item.personaId === personaId) ?? null
}

function asistenteCapLabel(personaId: number, fallback: string | null): string {
  const persona = personaDeId(personaId)
  if (persona) {
    return personaLabel(persona)
  }
  return fallback || `Persona ${personaId}`
}

function resetCapForm() {
  capForm.tipo = 'INDUCCION_HOMBRE_NUEVO'
  capForm.tema = ''
  capForm.fecha = hoyIso()
  capForm.horasDuracion = '4'
  capForm.expositorNombre = ''
  capForm.expositorEntidad = ''
  capForm.puntajeSimulacro = ''
  capForm.observaciones = ''
  capAsistentes.value = []
  capCuadrillaId.value = ''
  capPersonaSearch.value = ''
  capAgregarPersonaId.value = null
  if (capFileInput.value) {
    capFileInput.value.value = ''
  }
}

function onCapPersonaChange(value: unknown) {
  const id = typeof value === 'number' ? value : Number(value)
  capAgregarPersonaId.value = Number.isFinite(id) ? id : null
}

function agregarAsistenteCap() {
  if (!capAgregarPersonaId.value) {
    toast.error('Seleccione una persona para agregar.')
    return
  }
  if (capAsistentes.value.some((item) => item.personaId === capAgregarPersonaId.value)) {
    toast.error('La persona ya está en la lista de asistentes.')
    return
  }
  capAsistentes.value.push({ personaId: capAgregarPersonaId.value, nota: '', aprobado: true })
  capAgregarPersonaId.value = null
  capPersonaSearch.value = ''
}

function quitarAsistenteCap(personaId: number) {
  capAsistentes.value = capAsistentes.value.filter((item) => item.personaId !== personaId)
}

async function onPrecargarCuadrilla() {
  if (!capCuadrillaId.value) {
    toast.error('Seleccione una cuadrilla.')
    return
  }
  try {
    const filas = await precargarCuadrillaCapacitacion(proyectoId.value, Number(capCuadrillaId.value))
    const vistos = new Set(capAsistentes.value.map((item) => item.personaId))
    for (const fila of filas) {
      if (vistos.has(fila.personaId)) {
        continue
      }
      vistos.add(fila.personaId)
      capAsistentes.value.push({ personaId: fila.personaId, nota: '', aprobado: fila.aprobado })
    }
    if (!filas.length) {
      toast.error('La cuadrilla no tiene miembros.')
    }
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo precargar la cuadrilla.')
  }
}

async function onRegistrarCapacitacion() {
  if (!capForm.tema.trim()) {
    toast.error('Indique el tema.')
    return
  }
  if (!capForm.fecha) {
    toast.error('Indique la fecha.')
    return
  }
  if (!capForm.expositorNombre.trim()) {
    toast.error('Indique el expositor.')
    return
  }
  const horas = n(capForm.horasDuracion)
  if (!(horas > 0)) {
    toast.error('Las horas deben ser mayores que cero.')
    return
  }
  if (capForm.tipo === 'INDUCCION_HOMBRE_NUEVO' && horas < 4) {
    toast.error('La inducción de hombre nuevo debe durar al menos 4 horas.')
    return
  }
  if (!capAsistentes.value.length) {
    toast.error('Indique al menos un asistente.')
    return
  }
  capSaving.value = true
  try {
    const archivo = capFileInput.value?.files?.[0]
    let evidenciaDocId: number | null = null
    if (archivo) {
      const documento = await subirDocumentoObra(proyectoId.value, 'EVIDENCIA_SOMA', archivo)
      evidenciaDocId = documento.id
    }
    await crearCapacitacionObra(proyectoId.value, {
      tipo: capForm.tipo,
      tema: capForm.tema.trim(),
      fecha: capForm.fecha,
      horasDuracion: horas,
      expositorNombre: capForm.expositorNombre.trim(),
      expositorEntidad: capForm.expositorEntidad.trim() || null,
      puntajeSimulacro:
        capForm.tipo === 'SIMULACRO_EMERGENCIA' && capForm.puntajeSimulacro.trim()
          ? n(capForm.puntajeSimulacro)
          : null,
      observaciones: capForm.observaciones.trim() || null,
      evidenciaDocId,
      asistentes: capAsistentes.value.map((item) => ({
        personaId: item.personaId,
        nota: item.nota.trim() ? n(item.nota) : null,
        aprobado: item.aprobado,
      })),
    })
    toast.success('Capacitación registrada.')
    resetCapForm()
    await load()
  } catch (err) {
    toast.error(err instanceof Error ? err.message : 'No se pudo registrar la capacitación.')
  } finally {
    capSaving.value = false
  }
}

async function onDescargarKardex(personaId: number, nombre: string) {
  try {
    await descargarKardexEpp(proyectoId.value, personaId, `kardex-epp-${personaId}.pdf`)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : `No se pudo descargar el kardex de ${nombre}.`)
  }
}

watch(proyectoId, () => {
  void load()
})

watch(fechaHabilitacion, () => {
  if (loading.value) {
    return
  }
  void recargarHabilitacion()
})

watch([filtroPartidaIperc, incluirInactivosIperc], () => {
  if (loading.value) {
    return
  }
  void recargarIperc()
})

watch(comitePersonaSearch, (q) => {
  scheduleSearchComite(q)
})

onMounted(() => {
  if (!eventoForm.fechaHora) {
    eventoForm.fechaHora = ahoraLocalDatetime()
  }
  void load()
})
</script>

<template>
  <div class="mt-4 space-y-4">
    <Tabs v-model="tab">
      <TabsList :class="tabsListClass">
        <TabsTrigger value="habilitacion" :class="tabTriggerClass">Habilitación</TabsTrigger>
        <TabsTrigger value="resumen" :class="tabTriggerClass">Resumen</TabsTrigger>
        <TabsTrigger value="vigilancia" :class="tabTriggerClass">Vigilancia médica</TabsTrigger>
        <TabsTrigger value="sctr" :class="tabTriggerClass">SCTR</TabsTrigger>
        <TabsTrigger value="epp" :class="tabTriggerClass">EPP</TabsTrigger>
        <TabsTrigger value="capacitaciones" :class="tabTriggerClass">Capacitaciones</TabsTrigger>
        <TabsTrigger value="iperc" :class="tabTriggerClass">IPERC</TabsTrigger>
        <TabsTrigger value="charlaAts" :class="tabTriggerClass">Charla y ATS</TabsTrigger>
        <TabsTrigger value="petar" :class="tabTriggerClass">PETAR</TabsTrigger>
        <TabsTrigger value="inspecciones" :class="tabTriggerClass">Inspecciones</TabsTrigger>
        <TabsTrigger value="preUso" :class="tabTriggerClass">Pre-uso</TabsTrigger>
        <TabsTrigger value="eventos" :class="tabTriggerClass">Eventos</TabsTrigger>
        <TabsTrigger value="comite" :class="tabTriggerClass">Comité</TabsTrigger>
        <TabsTrigger value="ambiental" :class="tabTriggerClass">Ambiental</TabsTrigger>
      </TabsList>

      <TabsContent value="habilitacion" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>
        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Semáforo de habilitación</h3>
            <div class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-hab-fecha">Fecha</label>
                <Input
                  id="soma-hab-fecha"
                  v-model="fechaHabilitacion"
                  type="date"
                  :class="inputClass"
                  class="mt-1.5"
                />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-hab-filtro">Estado</label>
                <select id="soma-hab-filtro" v-model="filtroHabilitacion" :class="selectClass" class="mt-1.5">
                  <option value="TODOS">Todos</option>
                  <option value="HABILITADO">Habilitados</option>
                  <option value="BLOQUEADO">Bloqueados</option>
                </select>
              </div>
              <div>
                <p class="text-sm font-medium text-slate-500">Habilitados</p>
                <p class="mt-1 text-lg font-semibold text-palette-navy">{{ habilitacion?.habilitados ?? 0 }}</p>
              </div>
              <div>
                <p class="text-sm font-medium text-slate-500">Bloqueados</p>
                <p class="mt-1 text-lg font-semibold text-palette-navy">{{ habilitacion?.bloqueados ?? 0 }}</p>
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Persona</TableHead>
                  <TableHead>Documento</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Motivos</TableHead>
                  <TableHead>Restricciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in filasHabilitacion" :key="fila.personaId">
                  <TableCell>{{ fila.apellidos }}, {{ fila.nombres }}</TableCell>
                  <TableCell>{{ fila.numeroDocumento }}</TableCell>
                  <TableCell>
                    <Badge :variant="estadoHabilitacionBadge(fila.estado)">
                      {{ estadoHabilitacionLabel(fila.estado) }}
                    </Badge>
                  </TableCell>
                  <TableCell>{{ motivosLabel(fila.motivos) }}</TableCell>
                  <TableCell>{{ fila.restricciones || '—' }}</TableCell>
                </TableRow>
                <TableEmpty v-if="!filasHabilitacion.length" :colspan="5">
                  Sin trabajadores activos en la fecha seleccionada.
                </TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

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
            <div>
              <dt class="text-sm font-medium text-slate-500">Habilitados</dt>
              <dd class="mt-1 text-sm text-slate-900">{{ resumen?.habilitados ?? 0 }}</dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">Bloqueados</dt>
              <dd class="mt-1 text-sm text-slate-900">{{ resumen?.bloqueados ?? 0 }}</dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">EMO por vencer (30 días)</dt>
              <dd class="mt-1 text-sm text-slate-900">{{ resumen?.emosPorVencer30Dias ?? 0 }}</dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">HHT del mes</dt>
              <dd class="mt-1 text-sm text-slate-900">{{ fmtIndice(resumen?.hhtMes) }}</dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">IF / IS / IA</dt>
              <dd class="mt-1 text-sm text-slate-900">
                {{ fmtIndice(resumen?.indiceFrecuencia) }}
                /
                {{ fmtIndice(resumen?.indiceSeveridad) }}
                /
                {{ fmtIndice(resumen?.indiceAccidentabilidad) }}
              </dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">Accidentes / días perdidos</dt>
              <dd class="mt-1 text-sm text-slate-900">
                {{ resumen?.accidentesMes ?? 0 }} / {{ resumen?.diasPerdidosMes ?? 0 }}
              </dd>
            </div>
            <div>
              <dt class="text-sm font-medium text-slate-500">Días sin accidente incapacitante</dt>
              <dd class="mt-1 text-sm text-slate-900">
                {{ resumen?.diasSinAccidente == null ? 'Sin accidentes registrados' : resumen.diasSinAccidente }}
              </dd>
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

      <TabsContent value="sctr" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar póliza SCTR</h3>
            <p class="mt-1 text-sm text-slate-500">
              Una póliza por mes y obra. El certificado se sube como documento de obra. El checklist SCTR solo se
              actualiza si la póliza corresponde al mes en curso.
            </p>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4" @submit.prevent="onCrearPoliza">
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-anio">Año</label>
                <select
                  id="soma-sctr-anio"
                  v-model.number="sctrForm.anio"
                  :class="selectClass"
                  class="mt-1.5"
                  @change="sincronizarFechasSctr"
                >
                  <option v-for="anio in ANIOS" :key="anio" :value="anio">{{ anio }}</option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-mes">Mes</label>
                <select
                  id="soma-sctr-mes"
                  v-model.number="sctrForm.mes"
                  :class="selectClass"
                  class="mt-1.5"
                  @change="sincronizarFechasSctr"
                >
                  <option v-for="(nombre, index) in MESES" :key="nombre" :value="index + 1">{{ nombre }}</option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-ini">Inicio</label>
                <Input id="soma-sctr-ini" v-model="sctrForm.fechaInicio" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-fin">Fin</label>
                <Input id="soma-sctr-fin" v-model="sctrForm.fechaFin" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-aseg-salud">Aseguradora salud</label>
                <Input id="soma-sctr-aseg-salud" v-model="sctrForm.aseguradoraSalud" :class="inputClass" class="mt-1.5" maxlength="120" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-nro-salud">Póliza salud</label>
                <Input id="soma-sctr-nro-salud" v-model="sctrForm.numeroPolizaSalud" :class="inputClass" class="mt-1.5" maxlength="60" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-aseg-pen">Aseguradora pensión</label>
                <Input id="soma-sctr-aseg-pen" v-model="sctrForm.aseguradoraPension" :class="inputClass" class="mt-1.5" maxlength="120" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-nro-pen">Póliza pensión</label>
                <Input id="soma-sctr-nro-pen" v-model="sctrForm.numeroPolizaPension" :class="inputClass" class="mt-1.5" maxlength="60" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-prima-salud">Prima salud</label>
                <Input id="soma-sctr-prima-salud" v-model="sctrForm.montoPrimaSalud" :class="inputClass" class="mt-1.5" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-prima-pen">Prima pensión</label>
                <Input id="soma-sctr-prima-pen" v-model="sctrForm.montoPrimaPension" :class="inputClass" class="mt-1.5" />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-sctr-file">Póliza (PDF o imagen)</label>
                <input
                  id="soma-sctr-file"
                  ref="sctrFileInput"
                  class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                >
              </div>
              <div class="flex items-end">
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="sctrSaving"
                >
                  Registrar póliza
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="mb-3 text-base font-semibold text-palette-navy">Pólizas del mes</h3>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Periodo</TableHead>
                  <TableHead>Salud</TableHead>
                  <TableHead>Pensión</TableHead>
                  <TableHead>Prima</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="item in polizas" :key="item.id">
                  <TableCell>{{ mesLabel(item.mes) }} {{ item.anio }}</TableCell>
                  <TableCell>
                    <p>{{ item.aseguradoraSalud }}</p>
                    <p class="text-xs text-slate-500">{{ item.numeroPolizaSalud }}</p>
                  </TableCell>
                  <TableCell>
                    <p>{{ item.aseguradoraPension }}</p>
                    <p class="text-xs text-slate-500">{{ item.numeroPolizaPension }}</p>
                  </TableCell>
                  <TableCell>{{ money.format(item.montoPrimaSalud + item.montoPrimaPension) }}</TableCell>
                  <TableCell>
                    <Badge :variant="item.estado === 'VIGENTE' ? 'success' : 'secondary'">{{ item.estado === 'VIGENTE' ? 'Vigente' : 'Anulada' }}</Badge>
                  </TableCell>
                  <TableCell class="space-x-2 whitespace-nowrap">
                    <Button variant="outline" size="sm" type="button" @click="seleccionarPoliza(item)">Ver nómina</Button>
                    <Button
                      v-if="item.estado === 'VIGENTE'"
                      variant="outline"
                      size="sm"
                      type="button"
                      :disabled="sctrSaving"
                      @click="onAnularPoliza(item)"
                    >
                      Anular
                    </Button>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!polizas.length" :colspan="6">Sin pólizas SCTR en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div v-if="polizaSeleccionada" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div>
                <h3 class="text-base font-semibold text-palette-navy">
                  Nómina · {{ mesLabel(polizaSeleccionada.mes) }} {{ polizaSeleccionada.anio }}
                </h3>
                <p class="mt-1 text-sm text-slate-500">
                  Cubierto = salud y pensión marcados. Precargar trae a los trabajadores con contrato vigente en el mes.
                </p>
              </div>
              <div class="flex flex-wrap gap-2">
                <Button
                  variant="outline"
                  type="button"
                  :disabled="sctrSaving || polizaSeleccionada.estado === 'ANULADA'"
                  @click="onPrecargarNomina"
                >
                  Precargar trabajadores
                </Button>
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="button"
                  :disabled="sctrSaving || polizaSeleccionada.estado === 'ANULADA'"
                  @click="onGuardarNomina"
                >
                  Guardar nómina
                </Button>
              </div>
            </div>
            <Table class="mt-4">
              <TableHeader>
                <TableRow>
                  <TableHead>Persona</TableHead>
                  <TableHead>Documento</TableHead>
                  <TableHead class="w-24">Salud</TableHead>
                  <TableHead class="w-24">Pensión</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in nominaEdicion" :key="fila.personaId">
                  <TableCell>{{ fila.personaNombre ?? '—' }}</TableCell>
                  <TableCell>{{ fila.numeroDocumento ?? '—' }}</TableCell>
                  <TableCell>
                    <input
                      v-model="fila.incluidoSalud"
                      type="checkbox"
                      class="size-4 rounded border-slate-300"
                      :disabled="polizaSeleccionada.estado === 'ANULADA'"
                    >
                  </TableCell>
                  <TableCell>
                    <input
                      v-model="fila.incluidoPension"
                      type="checkbox"
                      class="size-4 rounded border-slate-300"
                      :disabled="polizaSeleccionada.estado === 'ANULADA'"
                    >
                  </TableCell>
                  <TableCell>
                    <Button
                      v-if="polizaSeleccionada.estado === 'VIGENTE'"
                      variant="outline"
                      size="sm"
                      type="button"
                      @click="quitarDeNomina(fila.personaId)"
                    >
                      Quitar
                    </Button>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!nominaEdicion.length" :colspan="5">Sin trabajadores en la nómina.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="epp" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar entrega de EPP</h3>
            <p class="mt-1 text-sm text-slate-500">
              El entregado por se toma del usuario autenticado. Si se descuenta de almacén se genera un vale de
              salida con la partida vigente.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onRegistrarEpp">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-persona">Persona</label>
                  <Combobox
                    :model-value="eppForm.personaId ?? undefined"
                    ignore-filter
                    class="min-w-0 w-full"
                    @update:model-value="onEppPersonaChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-epp-persona"
                          variant="outline"
                          class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(eppPersonaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(eppPersonaSeleccionada) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList
                      class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)"
                      align="start"
                    >
                      <ComboboxInput v-model="eppPersonaSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal en esta obra.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesEppPersona"
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
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-motivo">Motivo</label>
                  <select id="soma-epp-motivo" v-model="eppForm.motivo" :class="selectClass" class="mt-1.5">
                    <option v-for="item in MOTIVOS_EPP" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-fecha">Fecha</label>
                  <Input id="soma-epp-fecha" v-model="eppForm.fechaEntrega" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                    <input v-model="eppForm.descontarAlmacen" type="checkbox" />
                    Descontar de almacén
                  </label>
                </div>
                <div v-if="eppForm.descontarAlmacen">
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-partida">Partida</label>
                  <select id="soma-epp-partida" v-model="eppForm.partidaId" :class="selectClass" class="mt-1.5">
                    <option value="">Seleccione partida</option>
                    <option v-for="partida in partidasVigentes" :key="partida.id" :value="String(partida.id)">
                      {{ partida.codigo }} — {{ partida.descripcion }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                    <input v-model="eppForm.firmadoTrabajador" type="checkbox" />
                    Firmado por el trabajador
                  </label>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-epp-obs">Observaciones</label>
                  <Textarea id="soma-epp-obs" v-model="eppForm.observaciones" class="mt-1.5" maxlength="400" />
                </div>
              </div>

              <div>
                <p class="text-sm font-medium text-slate-700">Ítems</p>
                <div class="mt-2 space-y-2">
                  <div
                    v-for="(item, index) in eppItems"
                    :key="index"
                    class="grid gap-2 grid-cols-1 sm:grid-cols-[1fr_8rem_8rem_auto]"
                  >
                    <select v-model="item.materialId" :class="selectClass">
                      <option value="">Material EPP</option>
                      <option v-for="material in materialesEpp" :key="material.materialId" :value="String(material.materialId)">
                        {{ material.codigo }} — {{ material.descripcion }} (stock {{ qtyFmt.format(material.stock) }})
                      </option>
                    </select>
                    <Input v-model="item.cantidad" :class="inputClass" placeholder="Cantidad" />
                    <Input v-model="item.talla" :class="inputClass" placeholder="Talla" maxlength="20" />
                    <Button variant="outline" type="button" @click="quitarItemEpp(index)">Quitar</Button>
                  </div>
                </div>
                <Button class="mt-3" variant="outline" type="button" @click="agregarItemEpp">Agregar ítem</Button>
              </div>

              <Button
                class="bg-palette-navy text-white hover:bg-palette-royal"
                type="submit"
                :disabled="eppSaving"
              >
                Registrar entrega
              </Button>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Historial por trabajador</h3>
            <p v-if="!historialEppPorPersona.length" class="mt-3 text-sm text-slate-500">
              Aún no hay entregas de EPP en esta obra.
            </p>
            <div v-for="grupo in historialEppPorPersona" :key="grupo.personaId" class="mt-4 space-y-2">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <p class="text-sm font-medium text-slate-800">
                  {{ grupo.personaNombre }}
                  <span v-if="grupo.numeroDocumento" class="font-normal text-slate-500">· {{ grupo.numeroDocumento }}</span>
                </p>
                <Button variant="outline" size="sm" type="button" @click="onDescargarKardex(grupo.personaId, grupo.personaNombre)">
                  Kardex PDF
                </Button>
              </div>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Fecha</TableHead>
                    <TableHead>Material</TableHead>
                    <TableHead>Cantidad</TableHead>
                    <TableHead>Talla</TableHead>
                    <TableHead>Motivo</TableHead>
                    <TableHead>Vencimiento</TableHead>
                    <TableHead>Estado</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <template v-for="entrega in grupo.entregas" :key="entrega.id">
                    <TableRow v-for="item in entrega.items" :key="item.id">
                      <TableCell>{{ fechaLabel(entrega.fechaEntrega) }}</TableCell>
                      <TableCell>{{ item.materialCodigo }} — {{ item.materialDescripcion }}</TableCell>
                      <TableCell>{{ qtyFmt.format(item.cantidad) }}</TableCell>
                      <TableCell>{{ item.talla || '—' }}</TableCell>
                      <TableCell>{{ motivoEppLabel(entrega.motivo) }}</TableCell>
                      <TableCell>{{ fechaLabel(item.fechaVencimientoEstimada) }}</TableCell>
                      <TableCell>
                        <div class="flex flex-wrap items-center gap-2">
                          <Badge :variant="estadoEppBadge(item.estadoItem)">{{ estadoEppLabel(item.estadoItem) }}</Badge>
                          <select
                            :class="selectClass"
                            :value="item.estadoItem"
                            @change="onEstadoSelect(item.id, $event)"
                          >
                            <option v-for="estado in ESTADOS_EPP" :key="estado.valor" :value="estado.valor">
                              {{ estado.label }}
                            </option>
                          </select>
                        </div>
                      </TableCell>
                    </TableRow>
                  </template>
                </TableBody>
              </Table>
            </div>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="capacitaciones" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar capacitación</h3>
            <p class="mt-1 text-sm text-slate-500">
              La inducción de hombre nuevo exige al menos 4 horas. Si hay nota, se aprueba con 11 o más.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onRegistrarCapacitacion">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-tipo">Tipo</label>
                  <select id="soma-cap-tipo" v-model="capForm.tipo" :class="selectClass" class="mt-1.5">
                    <option v-for="item in TIPOS_CAP" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-tema">Tema</label>
                  <Input id="soma-cap-tema" v-model="capForm.tema" :class="inputClass" class="mt-1.5" maxlength="240" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-fecha">Fecha</label>
                  <Input id="soma-cap-fecha" v-model="capForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-horas">Horas</label>
                  <Input id="soma-cap-horas" v-model="capForm.horasDuracion" :class="inputClass" class="mt-1.5" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-expositor">Expositor</label>
                  <Input id="soma-cap-expositor" v-model="capForm.expositorNombre" :class="inputClass" class="mt-1.5" maxlength="160" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-entidad">Entidad</label>
                  <Input id="soma-cap-entidad" v-model="capForm.expositorEntidad" :class="inputClass" class="mt-1.5" maxlength="160" />
                </div>
                <div v-if="capForm.tipo === 'SIMULACRO_EMERGENCIA'">
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-puntaje">Puntaje simulacro</label>
                  <Input id="soma-cap-puntaje" v-model="capForm.puntajeSimulacro" :class="inputClass" class="mt-1.5" />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-file">Evidencia (opcional)</label>
                  <input
                    id="soma-cap-file"
                    ref="capFileInput"
                    class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                    type="file"
                  >
                </div>
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-cap-obs">Observaciones</label>
                  <Textarea id="soma-cap-obs" v-model="capForm.observaciones" class="mt-1.5" maxlength="1000" />
                </div>
              </div>

              <div>
                <p class="text-sm font-medium text-slate-700">Asistentes</p>
                <div class="mt-2 grid gap-2 grid-cols-1 sm:grid-cols-[1fr_auto] lg:grid-cols-[1fr_12rem_auto]">
                  <Combobox
                    :model-value="capAgregarPersonaId ?? undefined"
                    ignore-filter
                    class="min-w-0 w-full"
                    @update:model-value="onCapPersonaChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(capPersonaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(capPersonaSeleccionada) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="capPersonaSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesCapPersona"
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
                  <Button variant="outline" type="button" @click="agregarAsistenteCap">Agregar</Button>
                </div>
                <div class="mt-3 grid gap-2 grid-cols-1 sm:grid-cols-[1fr_auto]">
                  <select v-model="capCuadrillaId" :class="selectClass">
                    <option value="">Cuadrilla</option>
                    <option v-for="cuadrilla in cuadrillas" :key="cuadrilla.id" :value="String(cuadrilla.id)">
                      {{ cuadrilla.codigo }} — {{ cuadrilla.nombre }}
                    </option>
                  </select>
                  <Button variant="outline" type="button" @click="onPrecargarCuadrilla">Precargar cuadrilla</Button>
                </div>
                <Table class="mt-3">
                  <TableHeader>
                    <TableRow>
                      <TableHead>Persona</TableHead>
                      <TableHead>Nota</TableHead>
                      <TableHead>Aprobado</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableRow v-for="asistente in capAsistentes" :key="asistente.personaId">
                      <TableCell>{{ asistenteCapLabel(asistente.personaId, null) }}</TableCell>
                      <TableCell>
                        <Input v-model="asistente.nota" :class="inputClass" placeholder="0-20" />
                      </TableCell>
                      <TableCell>
                        <input v-model="asistente.aprobado" type="checkbox" :disabled="!!asistente.nota.trim()" class="size-4 rounded border-slate-300">
                      </TableCell>
                      <TableCell>
                        <Button variant="outline" size="sm" type="button" @click="quitarAsistenteCap(asistente.personaId)">
                          Quitar
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableEmpty v-if="!capAsistentes.length" :colspan="4">Sin asistentes.</TableEmpty>
                  </TableBody>
                </Table>
              </div>

              <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="capSaving">
                Registrar
              </Button>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Historial</h3>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Tema</TableHead>
                  <TableHead>Horas</TableHead>
                  <TableHead>Expositor</TableHead>
                  <TableHead>Asistentes</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="item in capacitaciones" :key="item.id">
                  <TableCell>{{ fechaLabel(item.fecha) }}</TableCell>
                  <TableCell>{{ tipoCapLabel(item.tipo) }}</TableCell>
                  <TableCell>{{ item.tema }}</TableCell>
                  <TableCell>{{ qtyFmt.format(item.horasDuracion) }}</TableCell>
                  <TableCell>{{ item.expositorNombre }}</TableCell>
                  <TableCell>
                    {{ item.asistentes.filter((fila) => fila.aprobado).length }}/{{ item.asistentes.length }} aprobados
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!capacitaciones.length" :colspan="6">Aún no hay capacitaciones en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="iperc" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>
        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">
              {{ ipercEditandoId ? 'Editar fila IPERC' : 'Registrar fila IPERC' }}
            </h3>
            <p class="mt-1 text-sm text-slate-500">
              El nivel se calcula con probabilidad × severidad. Debe haber al menos un control y el residual no puede
              superar el inicial.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onGuardarIperc">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-partida">Partida</label>
                  <select id="soma-iperc-partida" v-model="ipercForm.partidaId" :class="selectClass" class="mt-1.5">
                    <option value="">Sin partida</option>
                    <option v-for="partida in partidasVigentes" :key="partida.id" :value="String(partida.id)">
                      {{ partida.codigo }} — {{ partida.descripcion }}
                    </option>
                  </select>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-actividad">Actividad / tarea</label>
                  <Input
                    id="soma-iperc-actividad"
                    v-model="ipercForm.actividadTarea"
                    :class="inputClass"
                    class="mt-1.5"
                    maxlength="240"
                    required
                  />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-puesto">Puesto</label>
                  <Input
                    id="soma-iperc-puesto"
                    v-model="ipercForm.puestoTrabajo"
                    :class="inputClass"
                    class="mt-1.5"
                    maxlength="120"
                    required
                  />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-tipo">Tipo de peligro</label>
                  <select id="soma-iperc-tipo" v-model="ipercForm.tipoPeligro" :class="selectClass" class="mt-1.5">
                    <option v-for="item in TIPOS_PELIGRO_IPERC" :key="item.valor" :value="item.valor">
                      {{ item.label }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                    <input v-model="ipercForm.rutinario" type="checkbox" />
                    Rutinario
                  </label>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-peligro">Peligro</label>
                  <Input id="soma-iperc-peligro" v-model="ipercForm.peligro" :class="inputClass" class="mt-1.5" maxlength="400" required />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-riesgo">Riesgo</label>
                  <Input id="soma-iperc-riesgo" v-model="ipercForm.riesgo" :class="inputClass" class="mt-1.5" maxlength="400" required />
                </div>
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-consecuencia">Consecuencia</label>
                  <Input
                    id="soma-iperc-consecuencia"
                    v-model="ipercForm.consecuencia"
                    :class="inputClass"
                    class="mt-1.5"
                    maxlength="400"
                    required
                  />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-legal">Requisito legal</label>
                  <Input
                    id="soma-iperc-legal"
                    v-model="ipercForm.requisitoLegal"
                    :class="inputClass"
                    class="mt-1.5"
                    maxlength="200"
                  />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-p-ini">Probabilidad inicial</label>
                  <select id="soma-iperc-p-ini" v-model="ipercForm.probabilidadInicial" :class="selectClass" class="mt-1.5">
                    <option v-for="n in ESCALA_IPERC" :key="'pi-' + n" :value="String(n)">{{ n }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-s-ini">Severidad inicial</label>
                  <select id="soma-iperc-s-ini" v-model="ipercForm.severidadInicial" :class="selectClass" class="mt-1.5">
                    <option v-for="n in ESCALA_IPERC" :key="'si-' + n" :value="String(n)">{{ n }}</option>
                  </select>
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-500">Nivel inicial</p>
                  <div class="mt-2">
                    <Badge v-if="nivelInicialIpercVivo" :variant="nivelIpercBadge(nivelInicialIpercVivo)">
                      {{ nivelIpercLabel(nivelInicialIpercVivo) }}
                    </Badge>
                    <span v-else class="text-sm text-slate-500">—</span>
                  </div>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-elim">Control eliminación</label>
                  <Textarea id="soma-iperc-elim" v-model="ipercForm.controlEliminacion" class="mt-1.5 min-h-16" maxlength="500" />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-sust">Control sustitución</label>
                  <Textarea id="soma-iperc-sust" v-model="ipercForm.controlSustitucion" class="mt-1.5 min-h-16" maxlength="500" />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-ing">Control ingeniería</label>
                  <Textarea id="soma-iperc-ing" v-model="ipercForm.controlIngenieria" class="mt-1.5 min-h-16" maxlength="500" />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-adm">Control administrativo</label>
                  <Textarea id="soma-iperc-adm" v-model="ipercForm.controlAdministrativo" class="mt-1.5 min-h-16" maxlength="500" />
                </div>
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-epp">Control EPP</label>
                  <Textarea id="soma-iperc-epp" v-model="ipercForm.controlEpp" class="mt-1.5 min-h-16" maxlength="500" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-p-res">Probabilidad residual</label>
                  <select id="soma-iperc-p-res" v-model="ipercForm.probabilidadResidual" :class="selectClass" class="mt-1.5">
                    <option v-for="n in ESCALA_IPERC" :key="'pr-' + n" :value="String(n)">{{ n }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-s-res">Severidad residual</label>
                  <select id="soma-iperc-s-res" v-model="ipercForm.severidadResidual" :class="selectClass" class="mt-1.5">
                    <option v-for="n in ESCALA_IPERC" :key="'sr-' + n" :value="String(n)">{{ n }}</option>
                  </select>
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-500">Nivel residual</p>
                  <div class="mt-2">
                    <Badge v-if="nivelResidualIpercVivo" :variant="nivelIpercBadge(nivelResidualIpercVivo)">
                      {{ nivelIpercLabel(nivelResidualIpercVivo) }}
                    </Badge>
                    <span v-else class="text-sm text-slate-500">—</span>
                  </div>
                </div>
              </div>
              <div class="flex flex-wrap justify-end gap-2">
                <Button v-if="ipercEditandoId" type="button" variant="outline" :disabled="ipercSaving" @click="resetIpercForm">
                  Cancelar
                </Button>
                <Button class="bg-palette-navy text-white hover:bg-palette-royal" type="submit" :disabled="ipercSaving">
                  {{ ipercEditandoId ? 'Guardar cambios' : 'Registrar' }}
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
              <h3 class="text-base font-semibold text-palette-navy">Matriz IPERC</h3>
              <div class="grid gap-3 sm:grid-cols-2">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-iperc-filtro-partida">Partida</label>
                  <select
                    id="soma-iperc-filtro-partida"
                    v-model="filtroPartidaIperc"
                    :class="selectClass"
                    class="mt-1.5"
                  >
                    <option value="">Todas</option>
                    <option v-for="partida in partidasVigentes" :key="partida.id" :value="String(partida.id)">
                      {{ partida.codigo }} — {{ partida.descripcion }}
                    </option>
                  </select>
                </div>
                <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                  <input v-model="incluirInactivosIperc" type="checkbox" />
                  Incluir inactivos
                </label>
              </div>
            </div>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Partida</TableHead>
                  <TableHead>Actividad</TableHead>
                  <TableHead>Peligro</TableHead>
                  <TableHead>Inicial</TableHead>
                  <TableHead>Residual</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in filasIperc" :key="fila.id" :class="fila.activo ? '' : 'opacity-60'">
                  <TableCell>{{ fila.partidaCodigo ? `${fila.partidaCodigo} · ${fila.partidaDescripcion}` : 'Sin partida' }}</TableCell>
                  <TableCell>
                    <p>{{ fila.actividadTarea }}</p>
                    <p class="text-xs text-slate-500">{{ tipoPeligroLabel(fila.tipoPeligro) }} · {{ fila.puestoTrabajo }}</p>
                  </TableCell>
                  <TableCell>{{ fila.peligro }}</TableCell>
                  <TableCell>
                    <Badge :variant="nivelIpercBadge(fila.nivelRiesgoInicial)">
                      {{ nivelIpercLabel(fila.nivelRiesgoInicial) }}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge :variant="nivelIpercBadge(fila.nivelRiesgoResidual)">
                      {{ nivelIpercLabel(fila.nivelRiesgoResidual) }}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div class="flex flex-wrap justify-end gap-2">
                      <Button type="button" variant="outline" size="sm" :disabled="ipercSaving" @click="editarIperc(fila)">
                        Editar
                      </Button>
                      <Button
                        v-if="fila.activo"
                        type="button"
                        variant="outline"
                        size="sm"
                        :disabled="ipercSaving"
                        @click="onDesactivarIperc(fila)"
                      >
                        Desactivar
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!filasIperc.length" :colspan="6">Sin filas IPERC en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="charlaAts" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar charla diaria y ATS</h3>
            <p class="mt-1 text-sm text-slate-500">
              La habilitación SOMA se toma como foto del día. Si hay asistentes bloqueados, la charla se guarda igual.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onRegistrarCharlaAts">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-fecha">Fecha</label>
                  <Input id="soma-charla-fecha" v-model="charlaForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-hora">Hora</label>
                  <Input id="soma-charla-hora" v-model="charlaForm.horaInicio" type="time" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-duracion">Duración (min)</label>
                  <Input id="soma-charla-duracion" v-model="charlaForm.duracionMinutos" :class="inputClass" class="mt-1.5" />
                </div>
                <div class="sm:col-span-2 lg:col-span-1">
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-tema">Tema</label>
                  <Input id="soma-charla-tema" v-model="charlaForm.temaCharla" :class="inputClass" class="mt-1.5" maxlength="240" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-cuadrilla">Cuadrilla</label>
                  <select id="soma-charla-cuadrilla" v-model="charlaForm.cuadrillaId" :class="selectClass" class="mt-1.5">
                    <option value="">Sin cuadrilla</option>
                    <option v-for="cuadrilla in cuadrillas" :key="cuadrilla.id" :value="String(cuadrilla.id)">
                      {{ cuadrilla.codigo }} — {{ cuadrilla.nombre }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-partida">Partida</label>
                  <select id="soma-charla-partida" v-model="charlaForm.partidaId" :class="selectClass" class="mt-1.5">
                    <option value="">Sin partida</option>
                    <option v-for="partida in partidasVigentes" :key="partida.id" :value="String(partida.id)">
                      {{ partida.codigo }} — {{ partida.descripcion }}
                    </option>
                  </select>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-expositor">Expositor</label>
                  <Combobox
                    :model-value="charlaForm.expositorPersonaId ?? undefined"
                    ignore-filter
                    class="mt-1.5 min-w-0 w-full"
                    @update:model-value="onCharlaExpositorChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-charla-expositor"
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(charlaExpositorSeleccionado) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(charlaExpositorSeleccionado) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="charlaExpositorSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesCharlaExpositor"
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
                <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                  <input v-model="charlaForm.eppVerificado" type="checkbox" />
                  EPP verificado
                </label>
                <label class="flex items-center gap-2 pt-7 text-sm text-slate-600">
                  <input v-model="charlaForm.herramientasVerif" type="checkbox" />
                  Herramientas verificadas
                </label>
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-charla-obs">Observaciones</label>
                  <Textarea id="soma-charla-obs" v-model="charlaForm.observaciones" class="mt-1.5" maxlength="1000" />
                </div>
              </div>

              <div>
                <div class="flex flex-wrap items-center justify-between gap-2">
                  <p class="text-sm font-medium text-slate-700">Pasos del ATS</p>
                  <Button variant="outline" size="sm" type="button" @click="agregarPasoAts">Agregar paso</Button>
                </div>
                <Table class="mt-3">
                  <TableHeader>
                    <TableRow>
                      <TableHead>Paso</TableHead>
                      <TableHead>Peligros</TableHead>
                      <TableHead>Controles</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableRow v-for="(paso, index) in charlaPasos" :key="index">
                      <TableCell>
                        <Input v-model="paso.paso" :class="inputClass" maxlength="240" />
                      </TableCell>
                      <TableCell>
                        <Input v-model="paso.peligros" :class="inputClass" maxlength="1000" />
                      </TableCell>
                      <TableCell>
                        <Input v-model="paso.controles" :class="inputClass" maxlength="1000" />
                      </TableCell>
                      <TableCell>
                        <Button variant="outline" size="sm" type="button" @click="quitarPasoAts(index)">Quitar</Button>
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </div>

              <div>
                <p class="text-sm font-medium text-slate-700">Asistentes</p>
                <div class="mt-2 grid gap-2 grid-cols-1 sm:grid-cols-[1fr_auto]">
                  <Combobox
                    :model-value="charlaAgregarPersonaId ?? undefined"
                    ignore-filter
                    class="min-w-0 w-full"
                    @update:model-value="onCharlaPersonaChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(charlaPersonaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(charlaPersonaSeleccionada) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="charlaPersonaSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesCharlaPersona"
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
                  <Button variant="outline" type="button" @click="agregarAsistenteCharla">Agregar</Button>
                </div>
                <div class="mt-3">
                  <Button variant="outline" type="button" @click="onPrecargarCuadrillaCharla">
                    Precargar cuadrilla
                  </Button>
                </div>
                <Table class="mt-3">
                  <TableHeader>
                    <TableRow>
                      <TableHead>Persona</TableHead>
                      <TableHead>Observación</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableRow v-for="asistente in charlaAsistentes" :key="asistente.personaId">
                      <TableCell>
                        <div class="flex flex-wrap items-center gap-2">
                          <span>{{ asistenteCapLabel(asistente.personaId, null) }}</span>
                          <Badge v-if="asistente.habilitadoSoma === false" variant="secondary">No habilitado</Badge>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Input v-model="asistente.observacion" :class="inputClass" maxlength="240" />
                      </TableCell>
                      <TableCell>
                        <Button variant="outline" size="sm" type="button" @click="quitarAsistenteCharla(asistente.personaId)">
                          Quitar
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableEmpty v-if="!charlaAsistentes.length" :colspan="3">Sin asistentes.</TableEmpty>
                  </TableBody>
                </Table>
              </div>

              <Button type="submit" :disabled="charlaSaving">
                {{ charlaSaving ? 'Guardando…' : 'Registrar charla' }}
              </Button>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Charlas del día</h3>
            <p class="mt-1 text-sm text-slate-500">Se listan las charlas de la fecha del formulario. El resto de la obra queda en historial al cambiar la fecha.</p>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Hora</TableHead>
                  <TableHead>Tema</TableHead>
                  <TableHead>Cuadrilla / partida</TableHead>
                  <TableHead>Asistentes</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="charla in charlasAtsDelDia" :key="charla.id">
                  <TableCell>{{ charla.horaInicio }} · {{ charla.duracionMinutos }} min</TableCell>
                  <TableCell>
                    <p>{{ charla.temaCharla }}</p>
                    <p class="text-xs text-slate-500">{{ charla.pasos.length }} paso(s) ATS</p>
                  </TableCell>
                  <TableCell>
                    {{ charla.cuadrillaNombre || 'Sin cuadrilla' }}
                    <p class="text-xs text-slate-500">
                      {{ charla.partidaCodigo ? `${charla.partidaCodigo} · ${charla.partidaDescripcion}` : 'Sin partida' }}
                    </p>
                  </TableCell>
                  <TableCell>
                    <div class="flex flex-wrap gap-1">
                      <Badge
                        v-for="asistente in charla.asistentes"
                        :key="asistente.personaId"
                        :variant="asistente.habilitadoSoma ? 'default' : 'secondary'"
                      >
                        {{ asistente.personaNombre || `Persona ${asistente.personaId}` }}
                      </Badge>
                      <span v-if="!charla.asistentes.length" class="text-sm text-slate-500">Sin asistentes</span>
                    </div>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!charlasAtsDelDia.length" :colspan="4">Sin charlas en esta fecha.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="petar" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">
              {{ petarEditandoId ? `Editar PETAR N° ${petarEditandoId}` : 'Registrar PETAR' }}
            </h3>
            <p class="mt-1 text-sm text-slate-500">
              Se guarda en borrador. Autorizar exige trabajadores habilitados, checklist completo y mediciones del tipo.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onGuardarPetar">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-tipo">Tipo de trabajo</label>
                  <select
                    id="soma-petar-tipo"
                    v-model="petarForm.tipoTrabajo"
                    :class="selectClass"
                    class="mt-1.5"
                    required
                    @change="cargarChecklistPetar(petarForm.tipoTrabajo)"
                  >
                    <option v-for="tipo in TIPOS_PETAR" :key="tipo.valor" :value="tipo.valor">{{ tipo.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-fecha">Fecha</label>
                  <Input id="soma-petar-fecha" v-model="petarForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-hora-inicio">Hora inicio</label>
                  <Input id="soma-petar-hora-inicio" v-model="petarForm.horaInicio" type="time" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-hora-fin">Hora fin</label>
                  <Input id="soma-petar-hora-fin" v-model="petarForm.horaFin" type="time" :class="inputClass" class="mt-1.5" required />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-ubicacion">Ubicación / frente</label>
                  <Input id="soma-petar-ubicacion" v-model="petarForm.ubicacionFrente" :class="inputClass" class="mt-1.5" maxlength="200" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-cuadrilla">Cuadrilla</label>
                  <select id="soma-petar-cuadrilla" v-model="petarForm.cuadrillaId" :class="selectClass" class="mt-1.5">
                    <option value="">Sin cuadrilla</option>
                    <option v-for="cuadrilla in cuadrillas" :key="cuadrilla.id" :value="String(cuadrilla.id)">
                      {{ cuadrilla.codigo }} — {{ cuadrilla.nombre }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-partida">Partida</label>
                  <select id="soma-petar-partida" v-model="petarForm.partidaId" :class="selectClass" class="mt-1.5">
                    <option value="">Sin partida</option>
                    <option v-for="partida in partidasVigentes" :key="partida.id" :value="String(partida.id)">
                      {{ partida.codigo }} — {{ partida.descripcion }}
                    </option>
                  </select>
                </div>
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-descripcion">Descripción del trabajo</label>
                  <Textarea id="soma-petar-descripcion" v-model="petarForm.descripcionTrabajo" class="mt-1.5" maxlength="1000" required />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-residente">Residente</label>
                  <Combobox
                    :model-value="petarForm.residentePersonaId ?? undefined"
                    ignore-filter
                    class="mt-1.5 min-w-0 w-full"
                    @update:model-value="onPetarResidenteChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-petar-residente"
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(petarResidenteSeleccionado) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(petarResidenteSeleccionado) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="petarResidenteSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesPetarResidente"
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
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-supervisor">Supervisor SSOMA</label>
                  <Combobox
                    :model-value="petarForm.supervisorSsomaId ?? undefined"
                    ignore-filter
                    class="mt-1.5 min-w-0 w-full"
                    @update:model-value="onPetarSupervisorChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-petar-supervisor"
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(petarSupervisorSeleccionado) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(petarSupervisorSeleccionado) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="petarSupervisorSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesPetarSupervisor"
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
                <div v-if="petarExigeAtmosfera">
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-oxigeno">Oxígeno %</label>
                  <Input id="soma-petar-oxigeno" v-model="petarForm.oxigenoPct" :class="inputClass" class="mt-1.5" />
                </div>
                <div v-if="petarExigeAtmosfera">
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-lel">LEL %</label>
                  <Input id="soma-petar-lel" v-model="petarForm.lelPct" :class="inputClass" class="mt-1.5" />
                </div>
                <div v-if="petarExigeViento">
                  <label class="text-sm font-medium text-slate-700" for="soma-petar-viento">Viento km/h</label>
                  <Input id="soma-petar-viento" v-model="petarForm.vientoKmh" :class="inputClass" class="mt-1.5" />
                </div>
              </div>

              <div>
                <p class="text-sm font-medium text-slate-700">Checklist</p>
                <div class="mt-2 grid gap-2 sm:grid-cols-2">
                  <label v-for="item in petarChecklist" :key="item.clave" class="flex items-center gap-2 text-sm text-slate-600">
                    <input v-model="item.valor" type="checkbox" />
                    {{ item.etiqueta }}
                  </label>
                </div>
              </div>

              <div>
                <p class="text-sm font-medium text-slate-700">Trabajadores</p>
                <div class="mt-2 grid gap-2 grid-cols-1 sm:grid-cols-[1fr_auto]">
                  <Combobox
                    :model-value="petarAgregarPersonaId ?? undefined"
                    ignore-filter
                    class="min-w-0 w-full"
                    @update:model-value="onPetarPersonaChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(petarPersonaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(petarPersonaSeleccionada) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="petarPersonaSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesPetarPersona"
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
                  <Button variant="outline" type="button" @click="agregarTrabajadorPetar">Agregar</Button>
                </div>
                <Table class="mt-3">
                  <TableHeader>
                    <TableRow>
                      <TableHead>Persona</TableHead>
                      <TableHead>Rol en la tarea</TableHead>
                      <TableHead></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    <TableRow v-for="fila in petarTrabajadores" :key="fila.personaId">
                      <TableCell>{{ asistenteCapLabel(fila.personaId, null) }}</TableCell>
                      <TableCell>
                        <Input v-model="fila.rolEnTarea" :class="inputClass" maxlength="60" />
                      </TableCell>
                      <TableCell>
                        <Button variant="outline" size="sm" type="button" @click="quitarTrabajadorPetar(fila.personaId)">
                          Quitar
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableEmpty v-if="!petarTrabajadores.length" :colspan="3">Sin trabajadores.</TableEmpty>
                  </TableBody>
                </Table>
              </div>

              <div class="flex flex-wrap gap-2">
                <Button type="submit" :disabled="petarSaving">
                  {{ petarSaving ? 'Guardando…' : petarEditandoId ? 'Guardar PETAR' : 'Registrar PETAR' }}
                </Button>
                <Button v-if="petarEditandoId" variant="outline" type="button" :disabled="petarSaving" @click="resetPetarForm">
                  Cancelar edición
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">PETAR del día</h3>
            <p class="mt-1 text-sm text-slate-500">Permisos de la fecha del formulario en esta obra.</p>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>N°</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Horario</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in petarsDelDia" :key="fila.id">
                  <TableCell>{{ fila.numero }}</TableCell>
                  <TableCell>
                    <p>{{ tipoPetarLabel(fila.tipoTrabajo) }}</p>
                    <p class="text-xs text-slate-500">{{ fila.ubicacionFrente }}</p>
                  </TableCell>
                  <TableCell>{{ horaCorta(fila.horaInicio) }} – {{ horaCorta(fila.horaFin) }}</TableCell>
                  <TableCell>
                    <Badge :variant="fila.estado === 'AUTORIZADO' ? 'default' : 'secondary'">
                      {{ estadoPetarLabel(fila.estado) }}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div class="flex flex-wrap gap-2">
                      <Button
                        v-if="fila.estado === 'BORRADOR'"
                        variant="outline"
                        size="sm"
                        type="button"
                        :disabled="petarSaving"
                        @click="cargarPetarEnFormulario(fila)"
                      >
                        Editar
                      </Button>
                      <Button
                        v-if="fila.estado === 'BORRADOR'"
                        size="sm"
                        type="button"
                        :disabled="petarSaving"
                        @click="onTransicionPetar(fila, 'autorizar')"
                      >
                        Autorizar
                      </Button>
                      <Button
                        v-if="fila.estado === 'AUTORIZADO'"
                        variant="outline"
                        size="sm"
                        type="button"
                        :disabled="petarSaving"
                        @click="onTransicionPetar(fila, 'cerrar')"
                      >
                        Cerrar
                      </Button>
                      <Button
                        v-if="fila.estado === 'AUTORIZADO'"
                        variant="outline"
                        size="sm"
                        type="button"
                        :disabled="petarSaving"
                        @click="onTransicionPetar(fila, 'suspender')"
                      >
                        Suspender
                      </Button>
                      <Button
                        v-if="fila.estado === 'SUSPENDIDO'"
                        size="sm"
                        type="button"
                        :disabled="petarSaving"
                        @click="onTransicionPetar(fila, 'reanudar')"
                      >
                        Reanudar
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!petarsDelDia.length" :colspan="5">Sin PETAR en esta fecha.</TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Tareo sin PETAR</h3>
            <p class="mt-1 text-sm text-slate-500">
              Días del tareo en borrador con altura o cota cero sin PETAR autorizado o cerrado.
            </p>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Tipo requerido</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in petarFaltantes" :key="`${fila.fecha}-${fila.tipoTrabajo}`">
                  <TableCell>{{ fechaLabel(fila.fecha) }}</TableCell>
                  <TableCell>{{ tipoPetarLabel(fila.tipoTrabajo) }}</TableCell>
                </TableRow>
                <TableEmpty v-if="!petarFaltantes.length" :colspan="2">Sin faltantes de PETAR en el tareo.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="inspecciones" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar inspección</h3>
            <p class="mt-1 text-sm text-slate-500">
              La inspección a subcontratista exige un subcontrato de la obra. Los hallazgos se cargan aparte.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onRegistrarInspeccion">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-insp-tipo">Tipo</label>
                  <select id="soma-insp-tipo" v-model="inspeccionForm.tipo" :class="selectClass" class="mt-1.5">
                    <option v-for="item in TIPOS_INSPECCION" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-insp-fecha">Fecha</label>
                  <Input id="soma-insp-fecha" v-model="inspeccionForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-insp-area">Área / frente</label>
                  <Input id="soma-insp-area" v-model="inspeccionForm.areaFrente" :class="inputClass" class="mt-1.5" maxlength="160" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-insp-sub">Subcontrato</label>
                  <select id="soma-insp-sub" v-model="inspeccionForm.subcontratoId" :class="selectClass" class="mt-1.5">
                    <option value="">Sin subcontrato</option>
                    <option v-for="item in subcontratos" :key="item.id" :value="String(item.id)">
                      {{ item.especialidad }} — {{ item.subcontratistaRazon }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-insp-puntaje">Puntaje (0-100)</label>
                  <Input id="soma-insp-puntaje" v-model="inspeccionForm.puntajeCumplimiento" :class="inputClass" class="mt-1.5" />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-insp-inspector">Inspector</label>
                  <Combobox
                    :model-value="inspeccionForm.inspectorPersonaId ?? undefined"
                    ignore-filter
                    class="mt-1.5 min-w-0 w-full"
                    @update:model-value="onInspectorChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-insp-inspector"
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(inspectorSeleccionado) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(inspectorSeleccionado) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="inspectorSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesInspector"
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
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-insp-obs">Observaciones</label>
                  <Textarea id="soma-insp-obs" v-model="inspeccionForm.observaciones" class="mt-1.5" maxlength="1000" />
                </div>
              </div>
              <Button type="submit" :disabled="inspeccionSaving">
                {{ inspeccionSaving ? 'Guardando…' : 'Registrar inspección' }}
              </Button>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar hallazgo</h3>
            <p class="mt-1 text-sm text-slate-500">
              Puede nacer de una inspección o ir suelto. La paralización de frente exige potencial alto / grave.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onRegistrarHallazgo">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-insp">Inspección</label>
                  <select id="soma-hall-insp" v-model="hallazgoForm.inspeccionId" :class="selectClass" class="mt-1.5">
                    <option value="">Suelto</option>
                    <option v-for="item in inspecciones" :key="item.id" :value="String(item.id)">
                      {{ item.fecha }} · {{ item.areaFrente }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-clasif">Clasificación</label>
                  <select id="soma-hall-clasif" v-model="hallazgoForm.clasificacion" :class="selectClass" class="mt-1.5">
                    <option v-for="item in CLASIFICACIONES_HALLAZGO" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-pot">Potencial</label>
                  <select id="soma-hall-pot" v-model="hallazgoForm.potencialPerdida" :class="selectClass" class="mt-1.5">
                    <option v-for="item in POTENCIALES_HALLAZGO" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-ubi">Ubicación</label>
                  <Input id="soma-hall-ubi" v-model="hallazgoForm.ubicacion" :class="inputClass" class="mt-1.5" maxlength="200" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-det">Detección</label>
                  <Input id="soma-hall-det" v-model="hallazgoForm.fechaDeteccion" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-lim">Fecha límite</label>
                  <Input id="soma-hall-lim" v-model="hallazgoForm.fechaLimite" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-resp">Responsable</label>
                  <Combobox
                    :model-value="hallazgoForm.responsablePersonaId ?? undefined"
                    ignore-filter
                    class="mt-1.5 min-w-0 w-full"
                    @update:model-value="onResponsableChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-hall-resp"
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(responsableSeleccionado) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(responsableSeleccionado) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="responsableSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesResponsable"
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
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-desc">Descripción</label>
                  <Textarea id="soma-hall-desc" v-model="hallazgoForm.descripcion" class="mt-1.5" maxlength="1000" required />
                </div>
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-hall-acc">Acción correctiva</label>
                  <Textarea id="soma-hall-acc" v-model="hallazgoForm.accionCorrectiva" class="mt-1.5" maxlength="1000" required />
                </div>
                <label class="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    v-model="hallazgoForm.paralizacionFrente"
                    type="checkbox"
                    :disabled="hallazgoForm.potencialPerdida !== 'ALTO_GRAVE'"
                  />
                  Paralización de frente
                </label>
              </div>
              <Button type="submit" :disabled="hallazgoSaving">
                {{ hallazgoSaving ? 'Guardando…' : 'Registrar hallazgo' }}
              </Button>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Inspecciones</h3>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Área</TableHead>
                  <TableHead>Hallazgos</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in inspecciones" :key="fila.id">
                  <TableCell>{{ fila.fecha }}</TableCell>
                  <TableCell>{{ tipoInspeccionLabel(fila.tipo) }}</TableCell>
                  <TableCell>
                    <p>{{ fila.areaFrente }}</p>
                    <p class="text-xs text-slate-500">{{ fila.inspectorNombre || 'Sin inspector' }}</p>
                  </TableCell>
                  <TableCell>
                    <p v-for="item in fila.hallazgos" :key="item.id" class="text-sm">
                      {{ item.descripcion }}
                      <Badge v-if="item.vencido" variant="secondary">Vencido</Badge>
                    </p>
                    <span v-if="!fila.hallazgos.length" class="text-sm text-slate-500">Sin hallazgos</span>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!inspecciones.length" :colspan="4">Sin inspecciones en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <div class="flex flex-wrap items-end gap-3">
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-hall-filtro-estado">Estado</label>
                <select id="soma-hall-filtro-estado" v-model="filtroEstadoHallazgo" :class="selectClass" class="mt-1.5">
                  <option v-for="item in ESTADOS_HALLAZGO" :key="item.valor || 'todos'" :value="item.valor">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <label class="flex items-center gap-2 pb-2 text-sm text-slate-600">
                <input v-model="filtroVencidosHallazgo" type="checkbox" />
                Solo vencidos
              </label>
            </div>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Hallazgo</TableHead>
                  <TableHead>Potencial</TableHead>
                  <TableHead>Límite</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="item in hallazgosFiltrados" :key="item.id">
                  <TableCell>
                    <p>{{ item.descripcion }}</p>
                    <p class="text-xs text-slate-500">
                      {{ clasificacionHallazgoLabel(item.clasificacion) }}
                      · {{ item.inspeccionAreaFrente || 'Suelto' }}
                    </p>
                  </TableCell>
                  <TableCell>{{ potencialHallazgoLabel(item.potencialPerdida) }}</TableCell>
                  <TableCell>
                    {{ item.fechaLimite }}
                    <Badge v-if="item.vencido" variant="secondary">Vencido</Badge>
                  </TableCell>
                  <TableCell>{{ estadoHallazgoLabel(item.estado) }}</TableCell>
                  <TableCell>
                    <div class="flex flex-wrap justify-end gap-2">
                      <Button
                        v-if="item.estado === 'ABIERTO'"
                        type="button"
                        variant="outline"
                        size="sm"
                        :disabled="hallazgoSaving"
                        @click="onEnProcesoHallazgo(item)"
                      >
                        En proceso
                      </Button>
                      <label
                        v-if="item.estado !== 'LEVANTADO'"
                        class="inline-flex h-8 cursor-pointer items-center rounded-lg border border-slate-200 px-3 text-sm"
                      >
                        Levantar
                        <input
                          class="sr-only"
                          type="file"
                          :disabled="hallazgoSaving"
                          @change="onLevantarHallazgo(item, $event)"
                        >
                      </label>
                    </div>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!hallazgosFiltrados.length" :colspan="5">Sin hallazgos.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="preUso" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>

        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Check-list pre-uso de equipos</h3>
            <p class="mt-1 text-sm text-slate-500">
              Si algún ítem está en falso, el resultado no puede ser operativo. Un equipo inoperativo avisa en Recursos ese día.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onRegistrarPreUso">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-preuso-equipo">Equipo</label>
                  <select id="soma-preuso-equipo" v-model="preUsoForm.asignacionEquipoId" :class="selectClass" class="mt-1.5" required>
                    <option value="">Seleccione</option>
                    <option v-for="equipo in equiposObra" :key="equipo.id" :value="String(equipo.id)">
                      {{ equipo.codigo }} — {{ equipo.descripcion }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-preuso-fecha">Fecha</label>
                  <Input id="soma-preuso-fecha" v-model="preUsoForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-preuso-horometro">Horómetro</label>
                  <Input id="soma-preuso-horometro" v-model="preUsoForm.horometroLectura" :class="inputClass" class="mt-1.5" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-preuso-resultado">Resultado</label>
                  <select id="soma-preuso-resultado" v-model="preUsoForm.resultado" :class="selectClass" class="mt-1.5" required>
                    <option v-for="item in RESULTADOS_PREUSO" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-preuso-operador">Operador</label>
                  <Combobox
                    :model-value="preUsoForm.operadorPersonaId ?? undefined"
                    ignore-filter
                    class="mt-1.5 min-w-0 w-full"
                    @update:model-value="onPreUsoOperadorChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-preuso-operador"
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(preUsoOperadorSeleccionado) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(preUsoOperadorSeleccionado) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="preUsoOperadorSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesPreUsoOperador"
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
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-preuso-obs">Observaciones</label>
                  <Textarea id="soma-preuso-obs" v-model="preUsoForm.observaciones" class="mt-1.5" maxlength="600" />
                </div>
              </div>

              <div>
                <p class="text-sm font-medium text-slate-700">Checklist</p>
                <div class="mt-2 grid gap-2 sm:grid-cols-2">
                  <label v-for="item in preUsoChecklist" :key="item.clave" class="flex items-center gap-2 text-sm text-slate-600">
                    <input v-model="item.valor" type="checkbox" />
                    {{ item.etiqueta }}
                  </label>
                </div>
              </div>

              <Button type="submit" :disabled="preUsoSaving">
                {{ preUsoSaving ? 'Guardando…' : 'Registrar pre-uso' }}
              </Button>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Pre-uso del día</h3>
            <p class="mt-1 text-sm text-slate-500">Inspecciones de la fecha del formulario en esta obra.</p>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Equipo</TableHead>
                  <TableHead>Operador</TableHead>
                  <TableHead>Resultado</TableHead>
                  <TableHead>Horómetro</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in preUsosDelDia" :key="fila.id">
                  <TableCell>
                    <p>{{ fila.equipoCodigo }}</p>
                    <p class="text-xs text-slate-500">{{ fila.equipoDescripcion }}</p>
                  </TableCell>
                  <TableCell>{{ fila.operadorNombre || 'Sin operador' }}</TableCell>
                  <TableCell>
                    <Badge :variant="fila.resultado === 'INOPERATIVO_BLOQUEADO' ? 'secondary' : 'default'">
                      {{ resultadoPreUsoLabel(fila.resultado) }}
                    </Badge>
                  </TableCell>
                  <TableCell>{{ fila.horometroLectura ?? '—' }}</TableCell>
                </TableRow>
                <TableEmpty v-if="!preUsosDelDia.length" :colspan="4">Sin pre-uso en esta fecha.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="eventos" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>
        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h3 class="text-base font-semibold text-palette-navy">Estadísticas SST</h3>
                <p class="mt-1 text-sm text-slate-500">
                  HHT del tareo de la obra. IF, IS e IA con accidentes incapacitantes y mortales. El acumulado no suma índices mensuales.
                </p>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-est-anio">Año</label>
                <select
                  id="soma-est-anio"
                  v-model.number="anioEstadistica"
                  :class="selectClass"
                  class="mt-1.5"
                  @change="cargarEstadisticas"
                >
                  <option v-for="anio in ANIOS" :key="anio" :value="anio">{{ anio }}</option>
                </select>
              </div>
            </div>
            <dl class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
              <div>
                <dt class="text-sm font-medium text-slate-500">HHT del mes</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">{{ fmtIndice(estadisticaMesActual?.hht) }}</dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-slate-500">IF</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">{{ fmtIndice(estadisticaMesActual?.indiceFrecuencia) }}</dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-slate-500">IS</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">{{ fmtIndice(estadisticaMesActual?.indiceSeveridad) }}</dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-slate-500">IA</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">{{ fmtIndice(estadisticaMesActual?.indiceAccidentabilidad) }}</dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-slate-500">Días sin accidente</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">
                  {{ estadisticas?.diasSinAccidente == null ? '—' : estadisticas.diasSinAccidente }}
                </dd>
              </div>
            </dl>
            <Table class="mt-4">
              <TableHeader>
                <TableRow>
                  <TableHead>Mes</TableHead>
                  <TableHead>HHT</TableHead>
                  <TableHead>Accidentes</TableHead>
                  <TableHead>Días perdidos</TableHead>
                  <TableHead>IF</TableHead>
                  <TableHead>IS</TableHead>
                  <TableHead>IA</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in estadisticas?.meses ?? []" :key="fila.mes ?? 0">
                  <TableCell>{{ fila.mes ? mesLabel(fila.mes) : '—' }}</TableCell>
                  <TableCell>{{ fmtIndice(fila.hht) }}</TableCell>
                  <TableCell>{{ fila.accidentes }}</TableCell>
                  <TableCell>{{ fila.diasPerdidos }}</TableCell>
                  <TableCell>{{ fmtIndice(fila.indiceFrecuencia) }}</TableCell>
                  <TableCell>{{ fmtIndice(fila.indiceSeveridad) }}</TableCell>
                  <TableCell>{{ fmtIndice(fila.indiceAccidentabilidad) }}</TableCell>
                </TableRow>
                <TableRow v-if="estadisticas?.acumulado">
                  <TableCell class="font-semibold">Acumulado</TableCell>
                  <TableCell class="font-semibold">{{ fmtIndice(estadisticas.acumulado.hht) }}</TableCell>
                  <TableCell class="font-semibold">{{ estadisticas.acumulado.accidentes }}</TableCell>
                  <TableCell class="font-semibold">{{ estadisticas.acumulado.diasPerdidos }}</TableCell>
                  <TableCell class="font-semibold">{{ fmtIndice(estadisticas.acumulado.indiceFrecuencia) }}</TableCell>
                  <TableCell class="font-semibold">{{ fmtIndice(estadisticas.acumulado.indiceSeveridad) }}</TableCell>
                  <TableCell class="font-semibold">{{ fmtIndice(estadisticas.acumulado.indiceAccidentabilidad) }}</TableCell>
                </TableRow>
                <TableEmpty v-if="!estadisticas" :colspan="7">Sin estadísticas.</TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h3 class="text-base font-semibold text-palette-navy">
                  {{ eventoEditandoId ? `Evento N° ${eventoSeleccionado?.numero ?? ''}` : 'Registrar evento SST' }}
                </h3>
                <p class="mt-1 text-sm text-slate-500">
                  Datos, lesión, causas TASC y medidas correctivas. El descanso médico solo se escribe en tareo BORRADOR.
                </p>
              </div>
              <Button v-if="eventoEditandoId" type="button" variant="outline" @click="resetEventoForm">
                Nuevo evento
              </Button>
            </div>

            <form class="mt-4 space-y-6" @submit.prevent="onGuardarEvento">
              <section class="space-y-3">
                <h4 class="text-sm font-semibold text-palette-navy">Datos</h4>
                <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-tipo">Tipo</label>
                    <select id="soma-evt-tipo" v-model="eventoForm.tipoEvento" :class="selectClass" class="mt-1.5" required>
                      <option v-for="item in TIPOS_EVENTO_SST" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                    </select>
                  </div>
                  <div v-if="eventoForm.tipoEvento === 'ACCIDENTE_INCAPACITANTE'">
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-grav">Gravedad</label>
                    <select id="soma-evt-grav" v-model="eventoForm.gravedadIncapacidad" :class="selectClass" class="mt-1.5" required>
                      <option value="">Seleccione</option>
                      <option v-for="item in GRAVEDADES_INCAPACIDAD" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                    </select>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-fecha">Fecha y hora</label>
                    <Input id="soma-evt-fecha" v-model="eventoForm.fechaHora" type="datetime-local" :class="inputClass" class="mt-1.5" required />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-lugar">Lugar exacto</label>
                    <Input id="soma-evt-lugar" v-model="eventoForm.lugarExacto" :class="inputClass" class="mt-1.5" maxlength="200" required />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-partida">Partida</label>
                    <select id="soma-evt-partida" v-model="eventoForm.partidaId" :class="selectClass" class="mt-1.5">
                      <option value="">Sin partida</option>
                      <option v-for="partida in partidasVigentes" :key="partida.id" :value="String(partida.id)">
                        {{ partida.codigo }} · {{ partida.descripcion }}
                      </option>
                    </select>
                  </div>
                  <div class="sm:col-span-2">
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-persona">Persona afectada</label>
                    <Combobox
                      :model-value="eventoForm.personaAfectadaId ?? undefined"
                      ignore-filter
                      class="mt-1.5 min-w-0 w-full"
                      @update:model-value="onEventoPersonaChange"
                    >
                      <ComboboxAnchor class="min-w-0 w-full">
                        <ComboboxTrigger as-child>
                          <Button
                            id="soma-evt-persona"
                            variant="outline"
                            class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                            type="button"
                          >
                            <span
                              class="min-w-0 flex-1 truncate text-left"
                              :class="personaLabel(eventoPersonaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                            >
                              {{ personaLabel(eventoPersonaSeleccionada) || 'Buscar personal de la obra' }}
                            </span>
                            <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                          </Button>
                        </ComboboxTrigger>
                      </ComboboxAnchor>
                      <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                        <ComboboxInput v-model="eventoPersonaSearch" placeholder="Buscar persona..." autocomplete="off" />
                        <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                        <ComboboxGroup>
                          <ComboboxItem
                            v-for="persona in opcionesEventoPersona"
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
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-sub">Subcontrato</label>
                    <select id="soma-evt-sub" v-model="eventoForm.subcontratoId" :class="selectClass" class="mt-1.5">
                      <option value="">Sin subcontrato</option>
                      <option v-for="item in subcontratos" :key="item.id" :value="String(item.id)">
                        {{ item.subcontratistaRazon }}
                      </option>
                    </select>
                  </div>
                  <div class="sm:col-span-2 lg:col-span-4">
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-desc">Descripción</label>
                    <Textarea id="soma-evt-desc" v-model="eventoForm.descripcionEvento" class="mt-1.5" maxlength="2000" required />
                  </div>
                </div>
              </section>

              <section class="space-y-3">
                <h4 class="text-sm font-semibold text-palette-navy">Lesión y descanso médico</h4>
                <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-cuerpo">Parte del cuerpo</label>
                    <Input id="soma-evt-cuerpo" v-model="eventoForm.parteCuerpoLesionada" :class="inputClass" class="mt-1.5" maxlength="120" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-nat">Naturaleza</label>
                    <Input id="soma-evt-nat" v-model="eventoForm.naturalezaLesion" :class="inputClass" class="mt-1.5" maxlength="120" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-agente">Agente causante</label>
                    <Input id="soma-evt-agente" v-model="eventoForm.agenteCausante" :class="inputClass" class="mt-1.5" maxlength="160" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-cert">Certificado DM</label>
                    <Input id="soma-evt-cert" v-model="eventoForm.numeroCertificadoDm" :class="inputClass" class="mt-1.5" maxlength="60" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-dmi">Inicio DM</label>
                    <Input id="soma-evt-dmi" v-model="eventoForm.fechaInicioDm" type="date" :class="inputClass" class="mt-1.5" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-dmf">Fin DM</label>
                    <Input id="soma-evt-dmf" v-model="eventoForm.fechaFinDm" type="date" :class="inputClass" class="mt-1.5" />
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-700" for="soma-evt-cargo">Días cargo estadístico</label>
                    <Input id="soma-evt-cargo" v-model="eventoForm.diasCargoEstadistico" :class="inputClass" class="mt-1.5" />
                  </div>
                  <p v-if="eventoSeleccionado" class="self-end text-sm text-slate-500">
                    Días DM calculados: {{ eventoSeleccionado.diasDescansoMedico }}
                  </p>
                </div>
              </section>

              <section class="space-y-3">
                <h4 class="text-sm font-semibold text-palette-navy">Causas (TASC)</h4>
                <div class="grid gap-4 grid-cols-1 lg:grid-cols-2">
                  <div class="space-y-2">
                    <p class="text-sm font-medium text-slate-700">Actos</p>
                    <div class="flex gap-2">
                      <Input v-model="causaDraft.actos" :class="inputClass" maxlength="200" placeholder="Agregar acto" />
                      <Button type="button" variant="outline" @click="agregarCausa(eventoForm.causasActos, 'actos')">Agregar</Button>
                    </div>
                    <ul class="space-y-1 text-sm text-slate-600">
                      <li v-for="(item, indice) in eventoForm.causasActos" :key="`acto-${indice}`" class="flex items-center justify-between gap-2">
                        <span>{{ item }}</span>
                        <Button type="button" variant="ghost" size="sm" @click="quitarCausa(eventoForm.causasActos, indice)">Quitar</Button>
                      </li>
                    </ul>
                  </div>
                  <div class="space-y-2">
                    <p class="text-sm font-medium text-slate-700">Condiciones</p>
                    <div class="flex gap-2">
                      <Input v-model="causaDraft.condiciones" :class="inputClass" maxlength="200" placeholder="Agregar condición" />
                      <Button type="button" variant="outline" @click="agregarCausa(eventoForm.causasCondiciones, 'condiciones')">Agregar</Button>
                    </div>
                    <ul class="space-y-1 text-sm text-slate-600">
                      <li v-for="(item, indice) in eventoForm.causasCondiciones" :key="`cond-${indice}`" class="flex items-center justify-between gap-2">
                        <span>{{ item }}</span>
                        <Button type="button" variant="ghost" size="sm" @click="quitarCausa(eventoForm.causasCondiciones, indice)">Quitar</Button>
                      </li>
                    </ul>
                  </div>
                  <div class="space-y-2">
                    <p class="text-sm font-medium text-slate-700">Factores personales</p>
                    <div class="flex gap-2">
                      <Input v-model="causaDraft.factoresPersonales" :class="inputClass" maxlength="200" placeholder="Agregar factor" />
                      <Button type="button" variant="outline" @click="agregarCausa(eventoForm.causasFactoresPersonales, 'factoresPersonales')">Agregar</Button>
                    </div>
                    <ul class="space-y-1 text-sm text-slate-600">
                      <li v-for="(item, indice) in eventoForm.causasFactoresPersonales" :key="`fp-${indice}`" class="flex items-center justify-between gap-2">
                        <span>{{ item }}</span>
                        <Button type="button" variant="ghost" size="sm" @click="quitarCausa(eventoForm.causasFactoresPersonales, indice)">Quitar</Button>
                      </li>
                    </ul>
                  </div>
                  <div class="space-y-2">
                    <p class="text-sm font-medium text-slate-700">Factores de trabajo</p>
                    <div class="flex gap-2">
                      <Input v-model="causaDraft.factoresTrabajo" :class="inputClass" maxlength="200" placeholder="Agregar factor" />
                      <Button type="button" variant="outline" @click="agregarCausa(eventoForm.causasFactoresTrabajo, 'factoresTrabajo')">Agregar</Button>
                    </div>
                    <ul class="space-y-1 text-sm text-slate-600">
                      <li v-for="(item, indice) in eventoForm.causasFactoresTrabajo" :key="`ft-${indice}`" class="flex items-center justify-between gap-2">
                        <span>{{ item }}</span>
                        <Button type="button" variant="ghost" size="sm" @click="quitarCausa(eventoForm.causasFactoresTrabajo, indice)">Quitar</Button>
                      </li>
                    </ul>
                  </div>
                </div>
              </section>

              <div v-if="avisoMtpeEvento" class="rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900">
                <p class="font-medium">Notificar al MTPE dentro de las 24 h.</p>
                <label class="mt-2 flex items-center gap-2">
                  <input v-model="eventoForm.notificadoMtpe" type="checkbox" />
                  Notificado al MTPE
                </label>
              </div>

              <div class="flex flex-wrap gap-2">
                <Button type="submit" :disabled="eventoSaving || !eventoEnInvestigacion">
                  {{ eventoSaving ? 'Guardando…' : 'Guardar' }}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  :disabled="eventoSaving || !eventoEditandoId || !eventoEnInvestigacion"
                  @click="onCerrarEvento"
                >
                  Cerrar investigación
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  :disabled="eventoSaving || !eventoEditandoId"
                  @click="onSincronizarDmEvento"
                >
                  Sincronizar descanso médico
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Medidas correctivas</h3>
            <p class="mt-1 text-sm text-slate-500">
              Se crean como hallazgos SOMA y se enlazan al evento. Para cerrar, todas deben estar levantadas.
            </p>
            <form class="mt-4 space-y-4" @submit.prevent="onAgregarMedidaEvento">
              <div class="grid gap-3 grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-clasif">Clasificación</label>
                  <select id="soma-evt-med-clasif" v-model="eventoMedidaForm.clasificacion" :class="selectClass" class="mt-1.5">
                    <option v-for="item in CLASIFICACIONES_HALLAZGO" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-pot">Potencial</label>
                  <select id="soma-evt-med-pot" v-model="eventoMedidaForm.potencialPerdida" :class="selectClass" class="mt-1.5">
                    <option v-for="item in POTENCIALES_HALLAZGO" :key="item.valor" :value="item.valor">{{ item.label }}</option>
                  </select>
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-ubi">Ubicación</label>
                  <Input id="soma-evt-med-ubi" v-model="eventoMedidaForm.ubicacion" :class="inputClass" class="mt-1.5" maxlength="200" />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-det">Detección</label>
                  <Input id="soma-evt-med-det" v-model="eventoMedidaForm.fechaDeteccion" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div>
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-lim">Fecha límite</label>
                  <Input id="soma-evt-med-lim" v-model="eventoMedidaForm.fechaLimite" type="date" :class="inputClass" class="mt-1.5" required />
                </div>
                <div class="sm:col-span-2">
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-resp">Responsable</label>
                  <Combobox
                    :model-value="eventoMedidaForm.responsablePersonaId ?? undefined"
                    ignore-filter
                    class="mt-1.5 min-w-0 w-full"
                    @update:model-value="onEventoMedidaResponsableChange"
                  >
                    <ComboboxAnchor class="min-w-0 w-full">
                      <ComboboxTrigger as-child>
                        <Button
                          id="soma-evt-med-resp"
                          variant="outline"
                          class="h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                          type="button"
                        >
                          <span
                            class="min-w-0 flex-1 truncate text-left"
                            :class="personaLabel(eventoMedidaResponsableSeleccionado) ? 'text-slate-900' : 'text-slate-400'"
                          >
                            {{ personaLabel(eventoMedidaResponsableSeleccionado) || 'Buscar personal de la obra' }}
                          </span>
                          <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                        </Button>
                      </ComboboxTrigger>
                    </ComboboxAnchor>
                    <ComboboxList class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)" align="start">
                      <ComboboxInput v-model="eventoMedidaResponsableSearch" placeholder="Buscar persona..." autocomplete="off" />
                      <ComboboxEmpty>Sin personal disponible.</ComboboxEmpty>
                      <ComboboxGroup>
                        <ComboboxItem
                          v-for="persona in opcionesEventoMedidaResponsable"
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
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-desc">Descripción</label>
                  <Textarea id="soma-evt-med-desc" v-model="eventoMedidaForm.descripcion" class="mt-1.5" maxlength="1000" required />
                </div>
                <div class="sm:col-span-2 lg:col-span-4">
                  <label class="text-sm font-medium text-slate-700" for="soma-evt-med-acc">Acción correctiva</label>
                  <Textarea id="soma-evt-med-acc" v-model="eventoMedidaForm.accionCorrectiva" class="mt-1.5" maxlength="1000" required />
                </div>
                <label class="flex items-center gap-2 text-sm text-slate-600">
                  <input
                    v-model="eventoMedidaForm.paralizacionFrente"
                    type="checkbox"
                    :disabled="eventoMedidaForm.potencialPerdida !== 'ALTO_GRAVE'"
                  />
                  Paralización de frente
                </label>
              </div>
              <Button type="submit" :disabled="eventoMedidaSaving || !eventoEditandoId || !eventoEnInvestigacion">
                {{ eventoMedidaSaving ? 'Guardando…' : 'Agregar medida' }}
              </Button>
            </form>
            <Table class="mt-4">
              <TableHeader>
                <TableRow>
                  <TableHead>Descripción</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Límite</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in eventoSeleccionado?.hallazgos ?? []" :key="fila.id">
                  <TableCell>{{ fila.descripcion }}</TableCell>
                  <TableCell>
                    <Badge :variant="fila.estado === 'LEVANTADO' ? 'default' : 'secondary'">{{ fila.estado }}</Badge>
                  </TableCell>
                  <TableCell>{{ fila.fechaLimite }}</TableCell>
                </TableRow>
                <TableEmpty v-if="!(eventoSeleccionado?.hallazgos ?? []).length" :colspan="3">
                  Sin medidas enlazadas.
                </TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Eventos de la obra</h3>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>N°</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Afectado</TableHead>
                  <TableHead>Estado</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow
                  v-for="fila in eventos"
                  :key="fila.id"
                  class="cursor-pointer"
                  @click="cargarEventoEnForm(fila)"
                >
                  <TableCell>{{ fila.numero }}</TableCell>
                  <TableCell>{{ tipoEventoLabel(fila.tipoEvento) }}</TableCell>
                  <TableCell>{{ isoADatetimeLocal(fila.fechaHora).replace('T', ' ') }}</TableCell>
                  <TableCell>{{ fila.personaAfectadaNombre || fila.subcontratoNombre || '—' }}</TableCell>
                  <TableCell>
                    <Badge :variant="fila.estadoInvestigacion === 'CERRADO' ? 'secondary' : 'default'">
                      {{ estadoInvestigacionLabel(fila.estadoInvestigacion) }}
                    </Badge>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!eventos.length" :colspan="5">Sin eventos SST en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="comite" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>
        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Requerimiento</h3>
            <p class="mt-1 text-sm text-slate-500">
              {{ requerimientoComiteLabel(comiteRequerimiento) }}
            </p>
            <dl class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <dt class="text-sm font-medium text-slate-500">Tipo requerido</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">
                  {{ comiteRequerimiento?.tipoRequerido === 'SUBCOMITE' ? 'Subcomité' : 'Supervisor SST' }}
                </dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-slate-500">Trabajadores activos</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">
                  {{ comiteRequerimiento?.trabajadoresActivos ?? 0 }}
                </dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-slate-500">Titulares empleador / trabajadores</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">
                  {{ comiteRequerimiento?.titularesEmpleador ?? 0 }} /
                  {{ comiteRequerimiento?.titularesTrabajadores ?? 0 }}
                </dd>
              </div>
              <div>
                <dt class="text-sm font-medium text-slate-500">Supervisores activos</dt>
                <dd class="mt-1 text-lg font-semibold text-palette-navy">
                  {{ comiteRequerimiento?.supervisoresActivos ?? 0 }}
                </dd>
              </div>
            </dl>
            <ul v-if="comiteRequerimiento?.avisos.length" class="mt-4 space-y-1 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              <li v-for="aviso in comiteRequerimiento.avisos" :key="aviso">{{ aviso }}</li>
            </ul>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar miembro</h3>
            <p class="mt-1 text-sm text-slate-500">
              El representante del empleador puede ser externo. Los avisos de paridad no bloquean el alta.
            </p>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2" @submit.prevent="onRegistrarMiembroComite">
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-comite-persona">Persona</label>
                <Combobox
                  :model-value="comiteMiembroForm.personaId ?? undefined"
                  ignore-filter
                  class="min-w-0 w-full"
                  @update:model-value="onComitePersonaChange"
                >
                  <ComboboxAnchor class="min-w-0 w-full">
                    <ComboboxTrigger as-child>
                      <Button
                        id="soma-comite-persona"
                        variant="outline"
                        class="mt-1.5 h-10 min-w-0 w-full justify-between overflow-hidden rounded-lg border-slate-200 px-3 font-normal text-slate-900"
                        type="button"
                      >
                        <span
                          class="min-w-0 flex-1 truncate text-left"
                          :class="comitePersonaLabel(comitePersonaSeleccionada) ? 'text-slate-900' : 'text-slate-400'"
                        >
                          {{ comitePersonaLabel(comitePersonaSeleccionada) || 'Buscar persona' }}
                        </span>
                        <ChevronsUpDownIcon class="size-4 shrink-0 opacity-50" />
                      </Button>
                    </ComboboxTrigger>
                  </ComboboxAnchor>
                  <ComboboxList
                    class="z-100 max-w-[calc(100vw-2rem)] w-(--reka-combobox-trigger-width)"
                    align="start"
                  >
                    <ComboboxInput v-model="comitePersonaSearch" placeholder="Buscar persona..." autocomplete="off" />
                    <ComboboxEmpty>Sin personas.</ComboboxEmpty>
                    <ComboboxGroup>
                      <ComboboxItem
                        v-for="persona in comitePersonas"
                        :key="persona.id"
                        :value="persona.id"
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
                <label class="text-sm font-medium text-slate-700" for="soma-comite-rep">Representación</label>
                <select id="soma-comite-rep" v-model="comiteMiembroForm.representacion" :class="selectClass" class="mt-1.5">
                  <option v-for="item in REPRESENTACIONES_COMITE" :key="item.valor" :value="item.valor">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-comite-cargo">Cargo</label>
                <select id="soma-comite-cargo" v-model="comiteMiembroForm.cargoComite" :class="selectClass" class="mt-1.5">
                  <option v-for="item in CARGOS_COMITE" :key="item.valor" :value="item.valor">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-comite-inicio">Fecha inicio</label>
                <Input id="soma-comite-inicio" v-model="comiteMiembroForm.fechaInicio" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-comite-fin">Fecha fin (opcional)</label>
                <Input id="soma-comite-fin" v-model="comiteMiembroForm.fechaFin" type="date" :class="inputClass" class="mt-1.5" />
              </div>
              <label class="flex items-center gap-2 text-sm text-slate-600 sm:col-span-2">
                <input v-model="comiteMiembroForm.titular" type="checkbox" />
                Titular
              </label>
              <div>
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="comiteMiembroSaving"
                >
                  {{ comiteMiembroSaving ? 'Guardando…' : 'Registrar miembro' }}
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Miembros</h3>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Persona</TableHead>
                  <TableHead>Representación</TableHead>
                  <TableHead>Cargo</TableHead>
                  <TableHead>Titular</TableHead>
                  <TableHead>Inicio</TableHead>
                  <TableHead>Fin</TableHead>
                  <TableHead></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in comiteMiembros" :key="fila.id">
                  <TableCell>{{ fila.personaNombre || '—' }}</TableCell>
                  <TableCell>{{ representacionComiteLabel(fila.representacion) }}</TableCell>
                  <TableCell>{{ cargoComiteLabel(fila.cargoComite) }}</TableCell>
                  <TableCell>{{ fila.titular ? 'Sí' : 'Suplente' }}</TableCell>
                  <TableCell>{{ fechaLabel(fila.fechaInicio) }}</TableCell>
                  <TableCell>{{ fechaLabel(fila.fechaFin) }}</TableCell>
                  <TableCell>
                    <Button
                      v-if="fila.activo"
                      variant="outline"
                      size="sm"
                      type="button"
                      :disabled="comiteMiembroSaving"
                      @click="onDarBajaMiembroComite(fila)"
                    >
                      Baja
                    </Button>
                    <Badge v-else variant="secondary">Inactivo</Badge>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!comiteMiembros.length" :colspan="7">Sin miembros del comité en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar acta</h3>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2" @submit.prevent="onRegistrarActaComite">
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-acta-numero">Número</label>
                <Input id="soma-acta-numero" v-model="comiteActaForm.numeroActa" :class="inputClass" class="mt-1.5" maxlength="40" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-acta-tipo">Tipo de sesión</label>
                <select id="soma-acta-tipo" v-model="comiteActaForm.tipoSesion" :class="selectClass" class="mt-1.5">
                  <option v-for="item in TIPOS_SESION_COMITE" :key="item.valor" :value="item.valor">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-acta-fecha">Fecha</label>
                <Input id="soma-acta-fecha" v-model="comiteActaForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-acta-file">Adjunto (opcional)</label>
                <input
                  id="soma-acta-file"
                  ref="comiteFileInput"
                  class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                >
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-acta-agenda">Agenda</label>
                <Textarea id="soma-acta-agenda" v-model="comiteActaForm.agenda" class="mt-1.5" maxlength="1000" required />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-acta-acuerdos">Acuerdos</label>
                <Textarea id="soma-acta-acuerdos" v-model="comiteActaForm.acuerdos" class="mt-1.5" maxlength="3000" required />
              </div>
              <div>
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="comiteActaSaving"
                >
                  {{ comiteActaSaving ? 'Guardando…' : 'Registrar acta' }}
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Actas</h3>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>N°</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Agenda</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in comiteActas" :key="fila.id">
                  <TableCell>{{ fila.numeroActa }}</TableCell>
                  <TableCell>{{ tipoSesionComiteLabel(fila.tipoSesion) }}</TableCell>
                  <TableCell>{{ fechaLabel(fila.fecha) }}</TableCell>
                  <TableCell class="max-w-xs truncate">{{ fila.agenda }}</TableCell>
                </TableRow>
                <TableEmpty v-if="!comiteActas.length" :colspan="4">Sin actas en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>

      <TabsContent value="ambiental" class="mt-4 space-y-4">
        <div v-if="loading" class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
          <div class="space-y-3">
            <Skeleton class="h-10 w-full" />
            <Skeleton class="h-24 w-full" />
          </div>
        </div>
        <template v-else>
          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar residuo</h3>
            <p class="mt-1 text-sm text-slate-500">
              El peligroso (rojo) exige EO-RS y número de manifiesto. Indique kg o m³.
            </p>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2" @submit.prevent="onRegistrarResiduo">
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-fecha">Fecha</label>
                <Input id="soma-res-fecha" v-model="residuoForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-cat">Categoría</label>
                <select id="soma-res-cat" v-model="residuoForm.categoriaResiduo" :class="selectClass" class="mt-1.5">
                  <option v-for="item in CATEGORIAS_RESIDUO" :key="item.valor" :value="item.valor">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-res-desc">Descripción</label>
                <Input id="soma-res-desc" v-model="residuoForm.descripcion" :class="inputClass" class="mt-1.5" maxlength="240" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-kg">Cantidad (kg)</label>
                <Input id="soma-res-kg" v-model="residuoForm.cantidadKg" :class="inputClass" class="mt-1.5" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-m3">Volumen (m³)</label>
                <Input id="soma-res-m3" v-model="residuoForm.volumenM3" :class="inputClass" class="mt-1.5" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-eors">EO-RS razón social</label>
                <Input id="soma-res-eors" v-model="residuoForm.eorsRazonSocial" :class="inputClass" class="mt-1.5" maxlength="160" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-ruc">EO-RS RUC</label>
                <Input id="soma-res-ruc" v-model="residuoForm.eorsRuc" :class="inputClass" class="mt-1.5" maxlength="11" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-man">N° manifiesto</label>
                <Input id="soma-res-man" v-model="residuoForm.numeroManifiesto" :class="inputClass" class="mt-1.5" maxlength="60" />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-res-dest">Destino final</label>
                <Input id="soma-res-dest" v-model="residuoForm.destinoFinal" :class="inputClass" class="mt-1.5" maxlength="160" />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-res-file">Manifiesto (opcional)</label>
                <input
                  id="soma-res-file"
                  ref="residuoFileInput"
                  class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                >
              </div>
              <div>
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="residuoSaving"
                >
                  {{ residuoSaving ? 'Guardando…' : 'Registrar residuo' }}
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Residuos</h3>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Categoría</TableHead>
                  <TableHead>Descripción</TableHead>
                  <TableHead>Cantidad</TableHead>
                  <TableHead>EO-RS</TableHead>
                  <TableHead>Manifiesto</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in residuos" :key="fila.id">
                  <TableCell>{{ fechaLabel(fila.fecha) }}</TableCell>
                  <TableCell>{{ categoriaResiduoLabel(fila.categoriaResiduo) }}</TableCell>
                  <TableCell>{{ fila.descripcion }}</TableCell>
                  <TableCell>
                    <span v-if="fila.cantidadKg != null">{{ fila.cantidadKg }} kg</span>
                    <span v-if="fila.cantidadKg != null && fila.volumenM3 != null"> · </span>
                    <span v-if="fila.volumenM3 != null">{{ fila.volumenM3 }} m³</span>
                    <span v-if="fila.cantidadKg == null && fila.volumenM3 == null">—</span>
                  </TableCell>
                  <TableCell>
                    <p v-if="fila.eorsRazonSocial">{{ fila.eorsRazonSocial }}</p>
                    <p v-if="fila.eorsRuc" class="text-xs text-slate-500">{{ fila.eorsRuc }}</p>
                    <span v-if="!fila.eorsRazonSocial && !fila.eorsRuc">—</span>
                  </TableCell>
                  <TableCell>{{ fila.numeroManifiesto || '—' }}</TableCell>
                </TableRow>
                <TableEmpty v-if="!residuos.length" :colspan="6">Sin residuos registrados en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Registrar monitoreo</h3>
            <p class="mt-1 text-sm text-slate-500">
              El cumplimiento del LMP lo calcula el sistema. En iluminación el límite es un mínimo.
            </p>
            <form class="mt-4 grid gap-3 grid-cols-1 sm:grid-cols-2" @submit.prevent="onRegistrarMonitoreo">
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-fecha">Fecha</label>
                <Input id="soma-mon-fecha" v-model="monitoreoForm.fecha" type="date" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-tipo">Tipo de agente</label>
                <select id="soma-mon-tipo" v-model="monitoreoForm.tipoAgente" :class="selectClass" class="mt-1.5">
                  <option v-for="item in TIPOS_AGENTE_MONITOREO" :key="item.valor" :value="item.valor">
                    {{ item.label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-param">Parámetro</label>
                <Input id="soma-mon-param" v-model="monitoreoForm.parametroMedido" :class="inputClass" class="mt-1.5" maxlength="120" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-punto">Punto de medición</label>
                <Input id="soma-mon-punto" v-model="monitoreoForm.puntoMedicion" :class="inputClass" class="mt-1.5" maxlength="160" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-valor">Valor medido</label>
                <Input id="soma-mon-valor" v-model="monitoreoForm.valorMedido" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-lmp">Límite permisible</label>
                <Input id="soma-mon-lmp" v-model="monitoreoForm.limitePermisible" :class="inputClass" class="mt-1.5" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-unidad">Unidad</label>
                <Input id="soma-mon-unidad" v-model="monitoreoForm.unidad" :class="inputClass" class="mt-1.5" maxlength="30" required />
              </div>
              <div>
                <label class="text-sm font-medium text-slate-700" for="soma-mon-lab">Laboratorio</label>
                <Input id="soma-mon-lab" v-model="monitoreoForm.laboratorioNombre" :class="inputClass" class="mt-1.5" maxlength="160" />
              </div>
              <div class="sm:col-span-2">
                <label class="text-sm font-medium text-slate-700" for="soma-mon-file">Informe (opcional)</label>
                <input
                  id="soma-mon-file"
                  ref="monitoreoFileInput"
                  class="mt-1.5 h-10 w-full min-w-0 rounded-lg border border-slate-200 bg-white px-3 text-sm file:mr-2 file:h-full file:rounded file:border-0 file:bg-slate-100 file:px-3 file:text-sm"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png"
                >
              </div>
              <div>
                <Button
                  class="bg-palette-navy text-white hover:bg-palette-royal"
                  type="submit"
                  :disabled="monitoreoSaving"
                >
                  {{ monitoreoSaving ? 'Guardando…' : 'Registrar monitoreo' }}
                </Button>
              </div>
            </form>
          </div>

          <div class="rounded-lg border border-palette-cyan/70 bg-white px-4 py-5 sm:px-6">
            <h3 class="text-base font-semibold text-palette-navy">Monitoreos</h3>
            <Table class="mt-3">
              <TableHeader>
                <TableRow>
                  <TableHead>Fecha</TableHead>
                  <TableHead>Agente</TableHead>
                  <TableHead>Parámetro</TableHead>
                  <TableHead>Punto</TableHead>
                  <TableHead>Medido / LMP</TableHead>
                  <TableHead>Cumple</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow v-for="fila in monitoreos" :key="fila.id">
                  <TableCell>{{ fechaLabel(fila.fecha) }}</TableCell>
                  <TableCell>{{ tipoAgenteMonitoreoLabel(fila.tipoAgente) }}</TableCell>
                  <TableCell>{{ fila.parametroMedido }}</TableCell>
                  <TableCell>{{ fila.puntoMedicion }}</TableCell>
                  <TableCell>{{ fila.valorMedido }} / {{ fila.limitePermisible }} {{ fila.unidad }}</TableCell>
                  <TableCell>
                    <Badge :variant="fila.cumpleLmp ? 'success' : 'secondary'">
                      {{ fila.cumpleLmp ? 'Cumple' : 'No cumple' }}
                    </Badge>
                  </TableCell>
                </TableRow>
                <TableEmpty v-if="!monitoreos.length" :colspan="6">Sin monitoreos en esta obra.</TableEmpty>
              </TableBody>
            </Table>
          </div>
        </template>
      </TabsContent>
    </Tabs>
  </div>
</template>
