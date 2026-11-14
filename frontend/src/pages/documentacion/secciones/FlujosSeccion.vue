<template>
  <section>
    <h2 id="flujos" class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Flujos de trabajo</h2>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Los módulos se usan en orden. Si salta un paso (por ejemplo liquidar sin tareo, o declarar sin cerrar planillas)
      el sistema calculará vacío o rechazará la operación.
    </p>

    <h3 id="flujo-cierre" class="mt-8 text-lg font-semibold text-palette-navy">Cierre mensual administrativo</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Para personal de oficina (REMYPE) y para consolidar también la planilla RCC del mes:
    </p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>
        <strong>Alta.</strong> Personal → Registrar persona (o Link de alta). Complete régimen, AFP/ONP y, si es
        obrero, categoría CC.
      </li>
      <li>
        <strong>Contrato.</strong> Contratos → Nuevo contrato. Sin contrato vigente no hay asistencia ni boleta.
      </li>
      <li>
        <strong>Onboarding</strong> si corresponde el expediente de ingreso (packs DOC).
      </li>
      <li>
        <strong>Asistencia</strong> del mes: feriados, generar planilla, marcar días, conceptos variables, guardar.
      </li>
      <li>
        <strong>Nóminas</strong> → Nuevo periodo → Generar boletas → revisar extras → Recalcular si cambió el tareo →
        PDF / correo / dispersión → Cerrar periodo.
      </li>
      <li>
        En paralelo, en cada obra: tareo RCC del mes → Liquidar → Cerrar periodo de planilla RCC.
      </li>
      <li>
        <strong>Declaración mensual</strong> → crear → Consolidar RCC → PLAME, AFPnet, asiento y bancos → Cerrar
        declaración.
      </li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Firmas Digitales debe estar configurado antes de emitir boletas PDF. No mezcle el cierre de Nóminas con el de
      Planilla RCC: son periodos distintos; la declaración los junta.
    </p>

    <h3 id="flujo-obra" class="mt-8 text-lg font-semibold text-palette-navy">Ciclo de una obra</h3>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>
        <strong>Crear la obra</strong> (gerencia) y asignar accesos, residente, maestro y supervisor SSOMA. Cargar
        riesgo y tasas SCTR.
      </li>
      <li>
        <strong>Presupuesto:</strong> crear versión, partidas y APU, marcar vigente.
      </li>
      <li>
        <strong>Contratos CC</strong> de los obreros apuntando a esta obra. Armar cuadrillas en Recursos.
      </li>
      <li>
        <strong>SOMA de ingreso:</strong> EMO de ingreso apto, SCTR del mes, inducción de hombre nuevo, entrega de EPP
        (vale de almacén). El semáforo de Habilitación debe estar en verde antes del primer tareo.
      </li>
      <li>
        <strong>Tareo</strong> diario. Charla/ATS y PETAR el mismo día si hay trabajo de alto riesgo. Pre-uso de equipos.
      </li>
      <li>
        <strong>Almacén:</strong> requerimiento (sugerido desde APU) → aprobar → OC → emitir → recepcionar → vale de
        salida a partida. Cerrar mes de inventario.
      </li>
      <li>
        <strong>Planilla RCC:</strong> liquidar el periodo del tareo, boletas, correo, dispersión, cerrar.
      </li>
      <li>
        <strong>Avances:</strong> metrado del mes, cerrar mes físico, generar valorización, presentar, atender
        observación, aprobar.
      </li>
      <li>
        <strong>Financiamiento:</strong> factura de la valorización, cobranza o cesión a factoring, retenciones, flujo
        de caja.
      </li>
      <li>
        <strong>Rentabilidad</strong> del periodo. Subir contrato, planos y actas en Documentos. Anotar el día en
        Bitácora.
      </li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Las inspecciones y hallazgos SOMA, los eventos SST y el comité se registran en paralelo durante toda la
      ejecución; no esperar al cierre de mes. Los formatos SUNAFIL se descargan cuando fiscalización o el archivo
      legal los pidan.
    </p>
  </section>
</template>
