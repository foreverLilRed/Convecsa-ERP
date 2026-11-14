export type ManualSubseccion = {
  id: string
  titulo: string
}

export type ManualSeccion = {
  id: string
  titulo: string
  subsecciones: ManualSubseccion[]
}

export const seccionesManual: ManualSeccion[] = [
  {
    id: 'introduccion',
    titulo: 'Introducción',
    subsecciones: [
      { id: 'que-es', titulo: 'Qué es el ERP' },
      { id: 'ingreso', titulo: 'Ingreso y sesión' },
      { id: 'menu', titulo: 'Menú y navegación' },
      { id: 'roles', titulo: 'Roles y permisos' },
      { id: 'dashboard', titulo: 'Dashboard' },
    ],
  },
  {
    id: 'gestion-humana',
    titulo: 'Gestión Humana',
    subsecciones: [
      { id: 'gh-personal', titulo: 'Personal' },
      { id: 'gh-contratos', titulo: 'Contratos' },
      { id: 'gh-plantillas', titulo: 'Plantillas de contrato' },
      { id: 'gh-onboarding', titulo: 'Onboarding y offboarding' },
      { id: 'gh-nominas', titulo: 'Nóminas' },
      { id: 'gh-declaraciones', titulo: 'Declaración mensual' },
      { id: 'gh-asistencia', titulo: 'Asistencia' },
      { id: 'gh-kpis', titulo: 'KPIs y desempeño' },
      { id: 'gh-mof', titulo: 'Manuales MOF' },
      { id: 'gh-vacaciones', titulo: 'Vacaciones' },
      { id: 'gh-constancias', titulo: 'Constancias y certificados' },
      { id: 'gh-firmas', titulo: 'Firmas digitales' },
    ],
  },
  {
    id: 'proyectos',
    titulo: 'Proyectos y obras',
    subsecciones: [
      { id: 'obras', titulo: 'Obras' },
      { id: 'materiales', titulo: 'Catálogo de materiales' },
      { id: 'ficha', titulo: 'Ficha de obra' },
      { id: 'tareo', titulo: 'Tareo RCC' },
      { id: 'planilla-rcc', titulo: 'Planilla RCC' },
      { id: 'presupuesto', titulo: 'Presupuesto' },
      { id: 'almacen', titulo: 'Almacén' },
      { id: 'recursos', titulo: 'Recursos' },
      { id: 'avances', titulo: 'Avances' },
      { id: 'financiamiento', titulo: 'Financiamiento' },
      { id: 'rentabilidad', titulo: 'Rentabilidad' },
      { id: 'documentos-obra', titulo: 'Documentos' },
      { id: 'bitacora', titulo: 'Bitácora' },
    ],
  },
  {
    id: 'soma',
    titulo: 'SOMA',
    subsecciones: [
      { id: 'soma-marco', titulo: 'Marco y dos vistas' },
      { id: 'soma-tablero', titulo: 'Tablero SST' },
      { id: 'soma-vigilancia', titulo: 'Vigilancia médica y SCTR' },
      { id: 'soma-caps-corp', titulo: 'Capacitaciones corporativas' },
      { id: 'soma-formatos', titulo: 'Formatos SUNAFIL' },
      { id: 'soma-obra', titulo: 'SOMA por obra' },
    ],
  },
  {
    id: 'flujos',
    titulo: 'Flujos de trabajo',
    subsecciones: [
      { id: 'flujo-cierre', titulo: 'Cierre mensual administrativo' },
      { id: 'flujo-obra', titulo: 'Ciclo de una obra' },
    ],
  },
  {
    id: 'preguntas',
    titulo: 'Preguntas frecuentes',
    subsecciones: [
      { id: 'faq-obra', titulo: 'No veo una obra' },
      { id: 'faq-periodo', titulo: 'Periodo cerrado' },
      { id: 'faq-almacen', titulo: 'Rol Almacén' },
      { id: 'faq-soma', titulo: 'No aparece el menú SOMA' },
      { id: 'faq-descargas', titulo: 'Descargas y documentos' },
    ],
  },
]
