<template>
  <section>
    <h2 id="soma" class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">SOMA</h2>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      SOMA cubre Seguridad, Salud Ocupacional y Medio Ambiente en obra, alineado a la Ley 29783 y su reglamento
      (D.S. 005-2012-TR), al D.S. 011-2019-TR (SST en construcción), a la R.M. 050-2013-TR (registros referenciales)
      y a la Norma G.050.
    </p>

    <h3 id="soma-marco" class="mt-8 text-lg font-semibold text-palette-navy">Marco y dos vistas</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Hay dos formas de entrar, y no se duplican datos:
    </p>
    <ul class="mt-3 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>
        <strong>Menú SOMA</strong> (corporativo): Tablero SST, Vigilancia médica y SCTR, Capacitaciones y Formatos
        SUNAFIL. Solo Superadmin, Admin e Ingeniero SOMA. Consulta y descarga a nivel empresa (o filtrando una obra).
      </li>
      <li>
        <strong>Ficha de obra → pestaña SOMA:</strong> operación de campo. Aquí se registran EMO, SCTR, EPP,
        capacitaciones, IPERC, charla/ATS, PETAR, inspecciones, pre-uso, eventos, comité y ambiental.
      </li>
    </ul>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El semáforo de habilitación (pase de ingreso) cruza EMO vigente y apto, SCTR de la obra y, cuando aplica,
      inducción de hombre nuevo. Un trabajador NO APTO o con EMO vencido no debe ingresar a obra.
    </p>

    <h3 id="soma-tablero" class="mt-8 text-lg font-semibold text-palette-navy">Tablero SST</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: SOMA → Tablero SST</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Indicadores del año (zona horaria Lima): horas-hombre trabajadas (HHT), accidentes, índices IF / IS / IA, días
      sin accidente, hallazgos abiertos y obras con alerta crítica. Pulse <strong>Actualizar</strong>. Las HHT de
      obra salen del tareo RCC; las corporativas suman tareo de obra más asistencia administrativa, sin duplicar a
      quien ya tiene contrato CC.
    </p>
    <ul class="mt-3 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li><strong>IF</strong> (frecuencia): accidentes incapacitantes × 1&nbsp;000&nbsp;000 / HHT.</li>
      <li><strong>IS</strong> (severidad): días perdidos × 1&nbsp;000&nbsp;000 / HHT.</li>
      <li><strong>IA</strong> (accidentabilidad): IF × IS / 1&nbsp;000.</li>
    </ul>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Gerencia ve el consolidado. El ingeniero SOMA ve las obras a las que tiene acceso.
    </p>

    <h3 id="soma-vigilancia" class="mt-8 text-lg font-semibold text-palette-navy">Vigilancia médica y SCTR</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: SOMA → Vigilancia médica y SCTR</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Lista los EMO que vencen en los próximos 30 días. Filtre por obra. Es consulta: el alta o la renovación del
      examen se hace en la ficha de la obra, pestaña SOMA → Vigilancia médica.
    </p>

    <h3 id="soma-caps-corp" class="mt-8 text-lg font-semibold text-palette-navy">Capacitaciones corporativas</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: SOMA → Capacitaciones</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Listado anual de todas las obras y el indicador de horas mínimas de la Ley 29783. El alta de una capacitación
      (inducción, programada, alto riesgo o simulacro) se hace en la obra. Esta vista no crea registros.
    </p>

    <h3 id="soma-formatos" class="mt-8 text-lg font-semibold text-palette-navy">Formatos SUNAFIL</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: SOMA → Formatos SUNAFIL · R.M. 050-2013-TR</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Elija formato 1 a 8, PDF o Excel.</li>
      <li>Obra opcional: si la deja vacía, el alcance es corporativo.</li>
      <li>Año (por defecto el año Lima). El mes solo si el formato lo pide; el mes exige año.</li>
      <li>Descargue. El archivo viaja autenticado (misma sesión); no se abre un enlace público.</li>
    </ol>
    <div class="mt-4 overflow-x-auto">
      <table class="w-full min-w-[28rem] border-collapse text-left text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-xs uppercase tracking-wide text-slate-500">
            <th class="py-2 pr-3 font-semibold">N°</th>
            <th class="py-2 font-semibold">Registro</th>
          </tr>
        </thead>
        <tbody class="text-slate-600">
          <tr class="border-b border-slate-100"><td class="py-2 pr-3">1</td><td class="py-2">Accidentes, enfermedades e incidentes</td></tr>
          <tr class="border-b border-slate-100"><td class="py-2 pr-3">2</td><td class="py-2">Exámenes médico ocupacionales</td></tr>
          <tr class="border-b border-slate-100"><td class="py-2 pr-3">3</td><td class="py-2">Monitoreo de agentes</td></tr>
          <tr class="border-b border-slate-100"><td class="py-2 pr-3">4</td><td class="py-2">Inspecciones internas de SST</td></tr>
          <tr class="border-b border-slate-100"><td class="py-2 pr-3">5</td><td class="py-2">Estadísticas HHT, IF, IS, IA</td></tr>
          <tr class="border-b border-slate-100"><td class="py-2 pr-3">6</td><td class="py-2">Equipos de seguridad o emergencia</td></tr>
          <tr class="border-b border-slate-100"><td class="py-2 pr-3">7</td><td class="py-2">Inducción, capacitación y simulacros</td></tr>
          <tr><td class="py-2 pr-3">8</td><td class="py-2">Auditorías del SG-SST</td></tr>
        </tbody>
      </table>
    </div>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El formato 8 se entrega vacío si aún no hay auditorías cargadas en el sistema. Un número mayor a 8 es inválido.
    </p>

    <h3 id="soma-obra" class="mt-8 text-lg font-semibold text-palette-navy">SOMA por obra</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ficha de obra → pestaña SOMA</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Use estas pestañas internas. En cada una hay formularios de alta, listados y, cuando aplica, PDF.
    </p>
    <ul class="mt-3 list-disc space-y-2 pl-5 text-sm leading-relaxed text-slate-600">
      <li>
        <strong>Habilitación:</strong> semáforo por trabajador (EMO, SCTR, inducción). Es el pase de ingreso.
      </li>
      <li>
        <strong>Resumen:</strong> indicadores de la obra (HHT, índices, hallazgos abiertos).
      </li>
      <li>
        <strong>Vigilancia médica:</strong> registre EMO de ingreso, periódico, retiro, cambio de puesto o
        reincorporación. Aptitud: Apto, Apto con restricción, Observado o No apto. Adjunte resultado. El último EMO
        vigente se refleja en la ficha de la persona.
      </li>
      <li>
        <strong>SCTR:</strong> pólizas mensuales de salud y pensión según el nivel de riesgo de la obra.
      </li>
      <li>
        <strong>EPP:</strong> entrega desde materiales marcados como EPP. Motivo: dotación inicial, renovación por
        desgaste, reposición por pérdida o trabajo especial. Estados: vigente, devuelto, deteriorado, extraviado.
        El descuento de stock genera un vale de almacén. Puede descargar kardex PDF.
      </li>
      <li>
        <strong>Capacitaciones:</strong> inducción de hombre nuevo (mínimo 4 horas), capacitación programada,
        entrenamiento de alto riesgo o simulacro. Cargue asistentes (puede partir de una cuadrilla).
      </li>
      <li>
        <strong>IPERC:</strong> matriz por partida. Tipos de peligro: locativo, mecánico, eléctrico, físico, químico,
        ergonómico, psicosocial, biológico. Evalúe probabilidad y severidad y defina controles.
      </li>
      <li>
        <strong>Charla y ATS:</strong> charla diaria de cinco minutos y análisis de trabajo seguro de la jornada,
        con asistentes.
      </li>
      <li>
        <strong>PETAR:</strong> permiso para trabajo de alto riesgo (altura, excavación/zanja, caliente, espacio
        confinado, izaje, energía eléctrica). Estados: borrador, autorizado, suspendido, cerrado. Cruza con el tareo
        del día.
      </li>
      <li>
        <strong>Inspecciones:</strong> planeada mensual, inopinada, EPP, extintores, instalaciones G.050, andamios,
        orden y limpieza, almacén MATPEL o subcontratista. Los hallazgos se clasifican (acto o condición subestándar),
        con potencial alto/medio/bajo y estado abierto, en proceso o levantado.
      </li>
      <li>
        <strong>Pre-uso:</strong> check-list del equipo asignado a la obra. Resultado: operativo, operativo con
        observación o inoperativo/bloqueado.
      </li>
      <li>
        <strong>Eventos:</strong> incidente, incidente peligroso, accidente leve, incapacitante, mortal o enfermedad
        ocupacional. Investigue, adjunte evidencias y cierre la investigación. Alimentan IF/IS/IA y el formato 1.
      </li>
      <li>
        <strong>Comité:</strong> miembros del comité o subcomité SST, supervisor y actas. Guarde la nómina.
      </li>
      <li>
        <strong>Ambiental:</strong> residuos y monitoreos de agentes (ruido, polvo, etc.). Alimentan el formato 3.
      </li>
    </ul>
  </section>
</template>
