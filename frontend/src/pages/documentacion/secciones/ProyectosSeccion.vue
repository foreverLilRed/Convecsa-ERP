<template>
  <section>
    <h2 id="proyectos" class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Proyectos y obras</h2>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El menú <strong>Proyectos</strong> tiene dos entradas: el listado de obras y el catálogo global de materiales.
      El resto de la operación (tareo, planilla RCC, almacén, valorizaciones) ocurre dentro de la
      <strong>ficha de obra</strong>.
    </p>

    <h3 id="obras" class="mt-8 text-lg font-semibold text-palette-navy">Obras</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Proyectos → Obras</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>
        Superadmin o Admin pulsan <strong>Nueva obra</strong>, completan código, nombre, cliente, fechas y datos
        SCTR, y guardan.
      </li>
      <li>Pulse una fila para abrir la ficha. El rol Almacén entra directo a la pestaña Almacén.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Solo aparecen las obras a las que el usuario tiene acceso. Gerencia ve todas y asigna accesos en el Resumen de
      la ficha.
    </p>

    <h3 id="materiales" class="mt-8 text-lg font-semibold text-palette-navy">Catálogo de materiales</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Proyectos → Catálogo de materiales</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Catálogo único de la empresa (cemento, acero, EPP, etc.). Marque un ítem como EPP cuando sea equipo de
      protección: SOMA lo usa al entregar EPP y descuenta stock por vale de salida. El rol Almacén consulta el
      catálogo; no crea ni edita.
    </p>

    <h3 id="ficha" class="mt-8 text-lg font-semibold text-palette-navy">Ficha de obra</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Obras → fila de la obra</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Encabezado con código y nombre, alertas (incluye alertas SOMA críticas o de vencimiento) y pestañas:
      Resumen, Tareo, Planilla, Presupuesto, Almacén, Recursos, Avances, Financiamiento, Rentabilidad, Documentos,
      Bitácora y SOMA. El rol Almacén solo ve Almacén.
    </p>
    <p class="mt-3 text-sm font-medium text-palette-navy">Resumen</p>
    <ul class="mt-2 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Edite código, nombre, ubicación, centro de costo, cliente, RUC, fechas, estado, retención y valor de obra.</li>
      <li>Complete nivel de riesgo SCTR y tasas de salud y pensión. Guardar.</li>
      <li>Asigne responsables: Residente, Maestro de obra y Supervisor SSOMA.</li>
      <li>Gerencia: sección <strong>Accesos</strong> para dar o quitar usuarios de esa obra y <strong>Guardar accesos</strong>.</li>
      <li>Botón <strong>Obras</strong> para volver al listado.</li>
    </ul>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Estados típicos de obra: en ejecución, paralizada o liquidada. El Supervisor SSOMA se propone como firmante por
      defecto en registros SOMA.
    </p>

    <h3 id="tareo" class="mt-8 text-lg font-semibold text-palette-navy">Tareo RCC</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Tareo · régimen de construcción civil</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El jornal se calcula por día, no por mes. Jornada de referencia: 48 horas semanales. En la grilla se marcan
      horas ordinarias, extras al 60&nbsp;% (primeras dos), extras al 100&nbsp;% y códigos (laborado, falta, descanso,
      feriado, descanso médico, licencia). También hay banderas diarias de altura, cota cero, agua, etc., que disparan
      bonificaciones.
    </p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Elija mes y año. Si no hay periodo, créelo.</li>
      <li>Complete el parte diario de cada obrero con contrato CC en esa obra.</li>
      <li>Pulse <strong>Guardar tareo</strong>. Si el periodo está cerrado, la grilla queda bloqueada.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Una falta injustificada reduce el descanso dominical (un sexto). Las horas-hombre de SOMA (HHT) salen de este
      tareo, no de la asistencia administrativa.
    </p>

    <h3 id="planilla-rcc" class="mt-8 text-lg font-semibold text-palette-navy">Planilla RCC</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Planilla</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Liquida el periodo de tareo: jornal básico por categoría, BUC, BAE, dominical, horas extras, indemnización CTS
      (15&nbsp;%), vacaciones truncas (10&nbsp;%), gratificaciones proporcionales, CONAFOVICER (2&nbsp;%), fondo de
      capacitación (0,45&nbsp;%), AFP/ONP y SCTR de la obra.
    </p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Seleccione el periodo (cadencia y rango de fechas).</li>
      <li>Pulse <strong>Liquidar</strong> para generar boletas RCC.</li>
      <li>Revise extras por boleta y pulse <strong>Guardar extras</strong> si corrige conceptos.</li>
      <li>Descargue PDF o HTML, <strong>PDF lote</strong>, <strong>Enviar correo</strong> y archivos CCI / BCP / Interbank.</li>
      <li>Pulse <strong>Cerrar periodo</strong>. El tareo de ese rango queda bloqueado.</li>
    </ol>

    <h3 id="presupuesto" class="mt-8 text-lg font-semibold text-palette-navy">Presupuesto</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Presupuesto</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Pulse <strong>Crear presupuesto</strong> (o <strong>Nueva versión</strong> si ya hay una vigente: Actualizado o Adicional).</li>
      <li>Cargue partidas y APU, o <strong>importe Excel</strong> / use la plantilla de descarga.</li>
      <li>Pulse <strong>Marcar vigente</strong> cuando el borrador esté listo. Solo una versión vigente alimenta almacén y comparativos.</li>
      <li>Revise el comparativo meta vs real (consumo de materiales y costos).</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Los requerimientos de almacén se pueden sugerir desde el APU de la versión vigente. IPERC, ATS y PETAR pueden
      colgarse de una partida.
    </p>

    <h3 id="almacen" class="mt-8 text-lg font-semibold text-palette-navy">Almacén</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Almacén</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Cada obra tiene su almacén. El sistema avisa quiebres y materiales críticos sin stock. Pestañas:
    </p>
    <p class="mt-3 text-sm font-medium text-palette-navy">Requerimientos</p>
    <ol class="mt-2 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li><strong>Nuevo</strong> o elija un REQ existente.</li>
      <li>Opcional: <strong>Sugerir desde APU</strong>.</li>
      <li>Cargue materiales, partidas y cantidades. <strong>Guardar</strong> (estado Borrador).</li>
      <li><strong>Aprobar</strong> para que Compras lo tome. <strong>Anular</strong> si ya no aplica.</li>
    </ol>
    <p class="mt-3 text-sm font-medium text-palette-navy">Compras</p>
    <ol class="mt-2 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li><strong>Nueva OC</strong>. Puede copiar un requerimiento aprobado.</li>
      <li>Proveedor, RUC, ítems y precios. <strong>Guardar</strong> y luego <strong>Emitir</strong>.</li>
      <li>En Recepción: guía opcional, cantidades y <strong>Recepcionar</strong> (ingresa al kardex al costo).</li>
      <li>Si sobra mercadería, use <strong>Devolver</strong> al proveedor. <strong>Cerrar</strong> la OC cuando no quede pendiente.</li>
    </ol>
    <p class="mt-3 text-sm font-medium text-palette-navy">Kardex</p>
    <ul class="mt-2 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Stock, costo promedio ponderado (CMP) y valor.</li>
      <li>
        <strong>Vale de salida:</strong> elija partida vigente, material y cantidad; <strong>Registrar vale</strong>.
        Es el único camino para descontar stock (también lo usa la entrega de EPP en SOMA). <strong>Devolver</strong>
        revierte un vale.
      </li>
      <li><strong>Ajustar</strong> con cantidad positiva o negativa y observación.</li>
      <li><strong>Cerrar mes</strong> con año y mes para congelar el valor de inventario.</li>
      <li>La tabla de movimientos muestra el historial.</li>
    </ul>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El rol Almacén solo ve Kardex (consulta). No ve Requerimientos ni Compras, ni registra vales, ajustes o cierre.
    </p>

    <h3 id="recursos" class="mt-8 text-lg font-semibold text-palette-navy">Recursos</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Recursos</p>
    <ul class="mt-3 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li><strong>Personal en obra:</strong> contratos CC vigentes asignados a este proyecto.</li>
      <li><strong>Cuadrillas:</strong> <em>Nueva cuadrilla</em>, agregue miembros, edite o elimine. Las capacitaciones SOMA pueden precargar asistentes desde la cuadrilla.</li>
      <li><strong>Equipos:</strong> asigne equipo, impute horas o días a partidas. El check-list pre-uso SOMA cuelga de estas asignaciones.</li>
      <li><strong>Subcontratos:</strong> registre subcontratista y avance. Las inspecciones a terceros en SOMA se vinculan aquí.</li>
    </ul>

    <h3 id="avances" class="mt-8 text-lg font-semibold text-palette-navy">Avances</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Avances</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Registre metrado del mes por partida y pulse <strong>Guardar avance</strong>. <strong>Cerrar mes</strong> cuando el parte físico esté cerrado.</li>
      <li>Complete fechas del cronograma y <strong>Guardar cronograma</strong>.</li>
      <li>
        Valorizaciones: elija tipo Regular (u otro disponible), pulse <strong>Generar</strong>, revise, presente,
        registre observación del cliente, <strong>Aprobar</strong> y luego facturar desde Financiamiento.
      </li>
      <li>Cargue adicionales o deductivos (código, metrado, precio).</li>
      <li>Revise la curva S (programado vs ejecutado).</li>
    </ol>

    <h3 id="financiamiento" class="mt-8 text-lg font-semibold text-palette-navy">Financiamiento</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Financiamiento</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">Pestañas internas: Facturas, Factoring, Retenciones y Caja.</p>
    <ul class="mt-3 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li><strong>Facturas:</strong> las que salen de valorizaciones aprobadas. Registre cobranza y fecha de vencimiento (<strong>Guardar fecha</strong>).</li>
      <li><strong>Factoring:</strong> ceder, adelantar, cobrar, marcar impago o revertir la operación.</li>
      <li><strong>Retenciones:</strong> fondo de garantía de la obra; libere cuando el contrato lo permita.</li>
      <li><strong>Caja:</strong> flujo de caja de la obra (cobranzas, retenciones, factoring).</li>
    </ul>

    <h3 id="rentabilidad" class="mt-8 text-lg font-semibold text-palette-navy">Rentabilidad</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Rentabilidad</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Filtre el periodo. El margen compara ingresos (valorizaciones / facturas) contra costos (planilla RCC, consumo
      de materiales por vales, equipos, adelantos de factoring y asientos de costo). Use esta vista para gerencia de
      obra, no para reemplazar la contabilidad oficial.
    </p>

    <h3 id="documentos-obra" class="mt-8 text-lg font-semibold text-palette-navy">Documentos</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Documentos</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Suba archivos por tipo: Contrato, Plano, Acta, Valorización o Cesión de factoring. Liste, descargue o elimine.
      Las evidencias SOMA (EMO, actas, fotos de inspección) también se guardan como documento de obra: siempre van
      atadas a un proyecto.
    </p>

    <h3 id="bitacora" class="mt-8 text-lg font-semibold text-palette-navy">Bitácora</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Pestaña Bitácora</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Pestaña de notas diarias (crear o eliminar texto libre) y pestaña de auditoría (quién hizo qué en la obra:
      cierres, valorizaciones, movimientos SOMA). La bitácora es el cuaderno de obra; la auditoría no se edita.
    </p>
  </section>
</template>
