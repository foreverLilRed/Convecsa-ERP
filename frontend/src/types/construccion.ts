export type NivelRiesgoObra = 'BAJO' | 'MEDIO' | 'ALTO'

export type EstadoObra = 'EN_EJECUCION' | 'PARALIZADA' | 'LIQUIDADA'

export type RolResponsableObra = 'RESIDENTE' | 'MAESTRO_OBRA' | 'SUPERVISOR_SSOMA'

export type CadenciaPeriodoCc = 'SEMANAL' | 'QUINCENAL' | 'MENSUAL'

export type BancoDispersion = 'CCI' | 'BCP' | 'INTERBANK'

export type Proyecto = {
  id: number
  codigo: string
  nombre: string
  ubicacion: string | null
  centroCosto: string | null
  valorObra: number | null
  nivelRiesgo: NivelRiesgoObra | null
  tasaSctrSalud: number | null
  tasaSctrPension: number | null
  clienteRazon: string | null
  clienteRuc: string | null
  fechaInicio: string | null
  fechaFin: string | null
  estadoObra: EstadoObra
  porcentajeRetencion: number
  activo: boolean
  alertaUit: boolean
  uitAnio: number
}

export type ProyectoRequest = {
  codigo: string
  nombre: string
  ubicacion?: string | null
  centroCosto?: string | null
  valorObra?: number | null
  nivelRiesgo?: NivelRiesgoObra | null
  tasaSctrSalud?: number | null
  tasaSctrPension?: number | null
  clienteRazon?: string | null
  clienteRuc?: string | null
  fechaInicio?: string | null
  fechaFin?: string | null
  estadoObra?: EstadoObra
  porcentajeRetencion?: number | null
  activo?: boolean
}

export type ResponsableObra = {
  id: number
  personaId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  rol: RolResponsableObra
}

export type ProyectoResponsableRequest = {
  personaId: number
  rol: RolResponsableObra
}

export type TipoVersionPresupuesto = 'INICIAL' | 'ACTUALIZADO' | 'ADICIONAL'

export type EstadoVersionPresupuesto = 'BORRADOR' | 'VIGENTE' | 'HISTORICO'

export type TipoRecursoApu = 'MANO_OBRA' | 'MATERIAL' | 'EQUIPO' | 'SUBCONTRATO' | 'GASTOS_GENERALES'

export type ApuPartida = {
  id: number | null
  tipo: TipoRecursoApu
  codigo: string | null
  descripcion: string
  unidad: string | null
  cantidad: number
  precioUnitario: number
  parcial: number
  orden: number
}

export type PartidaPresupuesto = {
  id: number | null
  codigo: string
  capitulo: string | null
  descripcion: string
  unidad: string | null
  metrado: number
  precioUnitario: number
  total: number
  centroCosto: string | null
  orden: number
  apu: ApuPartida[]
}

export type PresupuestoVersionResumen = {
  id: number
  tipo: TipoVersionPresupuesto
  estado: EstadoVersionPresupuesto
  numero: number
  createdAt: string
  cantidadPartidas: number
}

export type PresupuestoVersion = {
  id: number
  proyectoId: number
  tipo: TipoVersionPresupuesto
  estado: EstadoVersionPresupuesto
  numero: number
  createdAt: string
  partidas: PartidaPresupuesto[]
}

export type Presupuesto = {
  versiones: PresupuestoVersionResumen[]
  version: PresupuestoVersion | null
}

export type ApuPartidaRequest = {
  tipo: TipoRecursoApu
  codigo?: string | null
  descripcion: string
  unidad?: string | null
  cantidad?: number | null
  precioUnitario?: number | null
}

export type PartidaPresupuestoRequest = {
  codigo: string
  capitulo?: string | null
  descripcion: string
  unidad?: string | null
  metrado?: number | null
  precioUnitario?: number | null
  centroCosto?: string | null
  apu?: ApuPartidaRequest[]
}

export type ComparativoCentro = {
  centroCosto: string
  metaMo: number
  realMo: number
  desvio: number
  metaMaterial: number
  realMaterial: number
  desvioMaterial: number
  metaEquipo: number
  realEquipo: number
  desvioEquipo: number
  metaSubcontrato: number
  realSubcontrato: number
  desvioSubcontrato: number
}

export type ComparativoPresupuesto = {
  metaMo: number
  realMo: number
  desvio: number
  desvioPct: number
  metaMaterial: number
  realMaterial: number
  desvioMaterial: number
  desvioPctMaterial: number
  metaEquipo: number
  realEquipo: number
  desvioEquipo: number
  desvioPctEquipo: number
  metaSubcontrato: number
  realSubcontrato: number
  desvioSubcontrato: number
  desvioPctSubcontrato: number
  centros: ComparativoCentro[]
}

export type ConstruccionCatalogos = {
  categorias: { id: number; codigo: string; label: string }[]
  especialidadesBae: { id: number; codigo: string; label: string }[]
  proyectos: Proyecto[]
  anioParametros: number
  uitAnio: number
  uit: number
  umbralUit: number
}

export type PersonalObra = {
  personaId: number
  contratoId: number
  nombres: string
  apellidos: string
  numeroDocumento: string
  categoria: 'OPERARIO' | 'OFICIAL' | 'PEON'
  especialidadBae: string | null
  hijosEscolaridad: number
  afiliadoSindical: boolean
  centroCosto: string | null
  fechaIngreso: string | null
  fechaCese: string | null
}

export type PeriodoCc = {
  id: number
  proyectoId: number
  proyectoCodigo: string
  proyectoNombre: string
  fechaDesde: string
  fechaHasta: string
  nroSemana: number
  cadencia: CadenciaPeriodoCc
  estado: 'BORRADOR' | 'CERRADO'
  createdAt: string
  closedAt: string | null
  cantidadBoletas: number
}

export type PeriodoCcRequest = {
  fechaDesde: string
  fechaHasta: string
  nroSemana?: number
  cadencia?: CadenciaPeriodoCc
}

export type CodigoTareoCc = 'LAB' | 'F' | 'DSC' | 'FE' | 'DM' | 'LIC'

export type TareoCcDia = {
  personaId: number
  fecha: string
  horasOrd: number
  he60: number
  he100: number
  codigo: CodigoTareoCc
  altura: boolean
  altitud: boolean
  agua: boolean
  altasTemp: boolean
  cotaCero: boolean
  nocturno: boolean
  aguasServidas: boolean
  bae: boolean
  movilidad: boolean
  pisos: number
}

export type TareoCcGrilla = {
  periodo: PeriodoCc
  fechas: string[]
  personal: PersonalObra[]
  dias: TareoCcDia[]
  jornadaLv: number
  jornadaSabado: number
}

export type BoletaCcResumen = {
  id: number
  personaId: number
  personaNombreCompleto: string
  numeroDocumento: string
  categoria: string | null
  totalHaberes: number
  totalDescuentos: number
  neto: number
}

export type BoletaCcConcepto = {
  categoria: 'HABER' | 'DESCUENTO' | 'APORTE_EMPLEADOR'
  codigo: string
  label: string
  monto: number
  origen?: 'CALCULADO' | 'MANUAL' | 'TAREO'
  orden: number
}

export type BoletaCc = {
  id: number
  periodoId: number
  personaId: number
  contratoId: number
  personaNombres: string
  personaApellidos: string
  numeroDocumento: string
  totalHaberes: number
  totalDescuentos: number
  totalAportesEmpleador: number
  totalAfecto: number
  neto: number
  tarifaDia: number
  tarifaHora: number
  createdAt: string
  conceptos: BoletaCcConcepto[]
}

export type EstadoRequerimientoObra = 'BORRADOR' | 'APROBADO' | 'ATENDIDO' | 'ANULADO'

export type EstadoOrdenCompra = 'BORRADOR' | 'EMITIDA' | 'RECEPCIONADA' | 'CERRADA'

export type TipoMovimientoAlmacen =
  | 'ENTRADA'
  | 'SALIDA'
  | 'AJUSTE'
  | 'DEVOLUCION_PROVEEDOR'
  | 'DEVOLUCION_ALMACEN'

export type MaterialCatalogo = {
  id: number
  codigo: string
  descripcion: string
  unidad: string | null
  familia: string | null
  stockMinimo: number
  critico: boolean
  activo: boolean
}

export type MaterialRequest = {
  codigo: string
  descripcion: string
  unidad?: string | null
  familia?: string | null
  stockMinimo?: number | null
  critico?: boolean
  activo?: boolean
}

export type StockItem = {
  materialId: number
  codigo: string
  descripcion: string
  unidad: string | null
  familia: string | null
  stockMinimo: number
  critico: boolean
  activo: boolean
  cantidad: number
  costoPromedio: number
  valor: number
  quiebre: boolean
  criticoSinStock: boolean
}

export type AlmacenObra = {
  id: number
  proyectoId: number
  nombre: string
  quiebres: number
  criticosSinStock: number
  stock: StockItem[]
}

export type MovimientoAlmacen = {
  id: number
  materialId: number
  codigoMaterial: string
  descripcionMaterial: string
  tipo: TipoMovimientoAlmacen
  cantidad: number
  costoUnitario: number
  costoTotal: number
  saldoCantidad: number
  costoPromedio: number
  partidaId: number | null
  codigoPartida: string | null
  centroCosto: string | null
  recepcionId: number | null
  valeId: number | null
  ordenCompraId: number | null
  fecha: string
  observaciones: string | null
  createdAt: string
}

export type RequerimientoItem = {
  id: number
  materialId: number
  codigoMaterial: string
  descripcionMaterial: string
  unidad: string | null
  partidaId: number | null
  codigoPartida: string | null
  descripcionPartida: string | null
  centroCosto: string | null
  cantidad: number
  cantidadAtendida: number
}

export type RequerimientoObra = {
  id: number
  proyectoId: number
  numero: number
  estado: EstadoRequerimientoObra
  fecha: string
  observaciones: string | null
  createdAt: string
  items: RequerimientoItem[]
}

export type RequerimientoItemRequest = {
  materialId: number
  partidaId?: number | null
  cantidad: number
}

export type RequerimientoRequest = {
  fecha?: string | null
  observaciones?: string | null
  items: RequerimientoItemRequest[]
}

export type SugeridoApu = {
  materialId: number | null
  codigo: string | null
  descripcion: string
  unidad: string | null
  partidaId: number
  codigoPartida: string
  descripcionPartida: string
  centroCosto: string
  cantidad: number
}

export type OrdenCompraItem = {
  id: number
  materialId: number
  codigoMaterial: string
  descripcionMaterial: string
  unidad: string | null
  requerimientoItemId: number | null
  cantidad: number
  precioUnitario: number
  cantidadRecibida: number
  cantidadPendiente: number
}

export type RecepcionItem = {
  id: number
  ordenCompraItemId: number
  materialId: number
  codigoMaterial: string
  cantidad: number
}

export type RecepcionAlmacen = {
  id: number
  numeroGuia: string | null
  fecha: string
  observaciones: string | null
  createdAt: string
  items: RecepcionItem[]
}

export type OrdenCompra = {
  id: number
  proyectoId: number
  numero: number
  estado: EstadoOrdenCompra
  proveedorRazon: string | null
  proveedorRuc: string | null
  plazoEntrega: string | null
  observaciones: string | null
  createdAt: string
  items: OrdenCompraItem[]
  recepciones: RecepcionAlmacen[]
}

export type OrdenCompraItemRequest = {
  materialId: number
  requerimientoItemId?: number | null
  cantidad: number
  precioUnitario?: number | null
}

export type OrdenCompraRequest = {
  requerimientoId?: number | null
  proveedorRazon?: string | null
  proveedorRuc?: string | null
  plazoEntrega?: string | null
  observaciones?: string | null
  items: OrdenCompraItemRequest[]
}

export type RecepcionRequest = {
  numeroGuia?: string | null
  fecha?: string | null
  observaciones?: string | null
  items: { ordenCompraItemId: number; cantidad: number }[]
}

export type ValeSalidaItem = {
  id: number
  materialId: number
  codigoMaterial: string
  descripcionMaterial: string
  unidad: string | null
  cantidad: number
  costoUnitario: number
  costoTotal: number
}

export type ValeSalida = {
  id: number
  almacenId: number
  numero: number
  fecha: string
  partidaId: number | null
  codigoPartida: string | null
  descripcionPartida: string | null
  centroCosto: string | null
  observaciones: string | null
  createdAt: string
  costoTotal: number
  items: ValeSalidaItem[]
}

export type ValeSalidaRequest = {
  fecha?: string | null
  partidaId: number
  observaciones?: string | null
  items: { materialId: number; cantidad: number }[]
}

export type InventarioCierreItem = {
  materialId: number
  codigo: string
  descripcion: string
  unidad: string | null
  cantidad: number
  costoPromedio: number
  valor: number
}

export type InventarioCierre = {
  anio: number
  mes: number
  createdAt: string | null
  valorTotal: number
  items: InventarioCierreItem[]
}

export type EstadoAvanceObra = 'BORRADOR' | 'CERRADO'

export type TipoAdicionalObra = 'ADICIONAL' | 'DEDUCTIVO'

export type EstadoAdicionalObra = 'BORRADOR' | 'VIGENTE' | 'ANULADO'

export type TipoValorizacion = 'REGULAR' | 'ADICIONAL' | 'DEDUCTIVO'

export type EstadoValorizacion = 'BORRADOR' | 'PRESENTADA' | 'OBSERVADA' | 'APROBADA'

export type EstadoFacturaObra = 'EMITIDA' | 'PARCIAL' | 'COBRADA' | 'CEDIDA'

export type TipoLiberacionRetencion = 'EFECTIVO' | 'CARTA_FIANZA'

export type EstadoFactoring = 'CEDIDA' | 'ADELANTADA' | 'COBRADA' | 'IMPAGA' | 'REVERTIDA'

export type CronogramaPartida = {
  partidaId: number
  codigo: string
  descripcion: string
  metradoMeta: number
  fechaInicio: string | null
  fechaFin: string | null
  desvioPlazoPct: number
}

export type AvancePartida = {
  partidaId: number
  codigo: string
  descripcion: string
  unidad: string | null
  centroCosto: string | null
  metradoMeta: number
  metradoAnterior: number
  metradoPeriodo: number
  metradoAcumulado: number
  desvio: number
}

export type AvanceObra = {
  id: number | null
  proyectoId: number
  anio: number
  mes: number
  estado: EstadoAvanceObra
  createdAt: string | null
  closedAt: string | null
  items: AvancePartida[]
}

export type AdicionalObra = {
  id: number
  proyectoId: number
  tipo: TipoAdicionalObra
  estado: EstadoAdicionalObra
  partidaId: number | null
  codigo: string
  descripcion: string
  unidad: string | null
  metrado: number
  precioUnitario: number
  total: number
  createdAt: string | null
}

export type AdicionalObraRequest = {
  tipo: TipoAdicionalObra
  partidaId?: number | null
  codigo: string
  descripcion: string
  unidad?: string | null
  metrado: number
  precioUnitario: number
}

export type FacturaObra = {
  id: number
  valorizacionId: number
  numero: number
  fecha: string
  fechaVencimiento: string | null
  clienteRazon: string | null
  clienteRuc: string | null
  subtotal: number
  igv: number
  total: number
  saldo: number | null
  estado: EstadoFacturaObra
  createdAt: string | null
  cobranzas?: CobranzaObra[] | null
  factoring?: OperacionFactoring | null
}

export type CobranzaObra = {
  id: number
  proyectoId: number
  facturaId: number
  facturaNumero: number
  fecha: string
  banco: string | null
  numeroOperacion: string | null
  importe: number
  detraccion: number
  aplicado: number
  observaciones: string | null
  createdAt: string | null
}

export type CobranzaObraRequest = {
  facturaId: number
  fecha: string
  banco?: string | null
  numeroOperacion?: string | null
  importe: number
  detraccion?: number
  observaciones?: string | null
}

export type LiberacionRetencion = {
  id: number
  proyectoId: number
  tipo: TipoLiberacionRetencion
  fecha: string
  importe: number
  entidad: string | null
  numeroDocumento: string | null
  observaciones: string | null
  createdAt: string | null
}

export type RetencionesObra = {
  retenidoAcumulado: number
  liberado: number
  cartaFianza: number
  saldo: number
  movimientos: LiberacionRetencion[]
}

export type OperacionFactoring = {
  id: number
  proyectoId: number
  facturaId: number
  facturaNumero: number
  entidadRazon: string
  entidadRuc: string | null
  fechaCesion: string
  tasaDescuento: number
  comision: number
  igvComision: number
  importeNominal: number
  descuento: number
  importeNeto: number
  fechaPagoEsperada: string | null
  fechaAdelanto: string | null
  estado: EstadoFactoring
  createdAt: string | null
}

export type OperacionFactoringRequest = {
  facturaId: number
  entidadRazon: string
  entidadRuc?: string | null
  fechaCesion: string
  tasaDescuento: number
  comision: number
  fechaPagoEsperada?: string | null
}

export type FlujoCajaPunto = {
  anio: number
  mes: number
  ingresos: number
  egresos: number
  costoFactoring: number
  neto: number
}

export type FlujoCaja = {
  porCobrar: number
  retenido: number
  cedido: number
  ingresos: number
  egresos: number
  costoFactoring: number
  neto: number
  puntos: FlujoCajaPunto[]
}

export type RentabilidadPunto = {
  anio: number
  mes: number
  ingreso: number
  costoMo: number
  costoMateriales: number
  costoEquipos: number
  costoSubcontratos: number
  costoFactoring: number
  margenBruto: number
  margenNeto: number
  margenBrutoAcumulado: number
  margenNetoAcumulado: number
}

export type RentabilidadObra = {
  ingreso: number
  costoMo: number
  costoMateriales: number
  costoEquipos: number
  costoSubcontratos: number
  costoFactoring: number
  margenBruto: number
  margenNeto: number
  puntos: RentabilidadPunto[]
}

export type OrigenEquipo = 'PROPIO' | 'ALQUILADO'

export type ModoTarifaEquipo = 'HORA' | 'DIA'

export type AsignacionEquipo = {
  id: number
  proyectoId: number
  codigo: string
  descripcion: string
  origen: OrigenEquipo
  proveedorRazon: string | null
  proveedorRuc: string | null
  fechaIngreso: string
  fechaSalida: string | null
  modoTarifa: ModoTarifaEquipo
  tarifa: number
  partidaId: number | null
  codigoPartida: string | null
  descripcionPartida: string | null
  centroCosto: string | null
  observaciones: string | null
  createdAt: string
  costoImputado: number
  cantidadImputada: number
}

export type AsignacionEquipoRequest = {
  codigo: string
  descripcion: string
  origen: OrigenEquipo
  proveedorRazon?: string | null
  proveedorRuc?: string | null
  fechaIngreso: string
  fechaSalida?: string | null
  modoTarifa: ModoTarifaEquipo
  tarifa: number
  partidaId?: number | null
  observaciones?: string | null
}

export type ImputacionEquipo = {
  id: number
  asignacionId: number
  proyectoId: number
  fecha: string
  cantidad: number
  tarifa: number
  costoTotal: number
  partidaId: number | null
  codigoPartida: string | null
  descripcionPartida: string | null
  centroCosto: string | null
  observaciones: string | null
  createdAt: string
}

export type ImputacionEquipoRequest = {
  fecha: string
  cantidad: number
  partidaId?: number | null
  observaciones?: string | null
}

export type SubcontratoObra = {
  id: number
  proyectoId: number
  especialidad: string
  subcontratistaRazon: string
  subcontratistaRuc: string | null
  montoContratado: number
  fechaInicio: string | null
  fechaFin: string | null
  partidaId: number | null
  codigoPartida: string | null
  descripcionPartida: string | null
  centroCosto: string | null
  observaciones: string | null
  createdAt: string
  montoAvanzado: number
  saldo: number
}

export type SubcontratoObraRequest = {
  especialidad: string
  subcontratistaRazon: string
  subcontratistaRuc?: string | null
  montoContratado: number
  fechaInicio?: string | null
  fechaFin?: string | null
  partidaId?: number | null
  observaciones?: string | null
}

export type SubcontratoAvance = {
  id: number
  subcontratoId: number
  proyectoId: number
  fecha: string
  monto: number
  partidaId: number | null
  codigoPartida: string | null
  descripcionPartida: string | null
  centroCosto: string | null
  observaciones: string | null
  createdAt: string
}

export type SubcontratoAvanceRequest = {
  fecha: string
  monto: number
  partidaId?: number | null
  observaciones?: string | null
}

export type AsientoObraLinea = {
  cuenta: string
  descripcion: string
  auxiliar: string | null
  debe: number
  haber: number
}

export type AsientoObraBloque = {
  codigo: string
  titulo: string
  lineas: AsientoObraLinea[]
  totalDebe: number
  totalHaber: number
}

export type AsientoObra = {
  desde: string
  hasta: string
  generadoAt: string
  bloques: AsientoObraBloque[]
  totalDebe: number
  totalHaber: number
  diferencia: number
}

export type AsientosObra = {
  materiales: AsientoObra
  factoring: AsientoObra
}

export type TipoDocumentoObra = 'CONTRATO' | 'PLANO' | 'ACTA' | 'VALORIZACION' | 'CESION_FACTORING'

export type DocumentoObra = {
  id: number
  proyectoId: number
  tipo: TipoDocumentoObra
  nombreOriginal: string
  mime: string
  tamanoBytes: number
  observaciones: string | null
  createdAt: string
}

export type SeveridadAlertaObra = 'INFO' | 'WARNING' | 'CRITICAL'

export type AlertaObra = {
  codigo: string
  severidad: SeveridadAlertaObra
  mensaje: string
}

export type ValorizacionItem = {
  id: number | null
  partidaId: number | null
  adicionalId: number | null
  codigo: string
  descripcion: string
  unidad: string | null
  centroCosto: string | null
  metradoAnterior: number
  metradoPeriodo: number
  metradoAcumulado: number
  precioUnitario: number
  totalPeriodo: number
  totalAcumulado: number
}

export type Valorizacion = {
  id: number
  proyectoId: number
  numero: number
  tipo: TipoValorizacion
  estado: EstadoValorizacion
  anio: number
  mes: number
  porcentajeRetencion: number
  montoBruto: number
  montoRetencion: number
  montoNeto: number
  observaciones: string | null
  createdAt: string | null
  items: ValorizacionItem[]
  factura: FacturaObra | null
}

export type CurvaSPunto = {
  anio: number
  mes: number
  programadoFisico: number
  ejecutadoFisico: number
  programadoEconomico: number
  ejecutadoEconomico: number
  desvioFisico: number
  desvioEconomico: number
}

export type CurvaS = {
  metradoMeta: number
  montoMeta: number
  desvioPlazoPct: number
  puntos: CurvaSPunto[]
}

export const CODIGOS_TAREO: { codigo: CodigoTareoCc; label: string }[] = [
  { codigo: 'LAB', label: 'Labor' },
  { codigo: 'F', label: 'Falta' },
  { codigo: 'DSC', label: 'Descanso' },
  { codigo: 'FE', label: 'Feriado' },
  { codigo: 'DM', label: 'D. médico' },
  { codigo: 'LIC', label: 'Licencia' },
]
