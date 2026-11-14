<template>
  <section>
    <h2 id="gestion-humana" class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Gestión Humana</h2>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Todas las pantallas de este bloque están en el menú <strong>Gestión Humana</strong>. Sirven para el personal
      administrativo (régimen REMYPE) y para el padrón común de personas que también trabaja en obras.
    </p>

    <h3 id="gh-personal" class="mt-8 text-lg font-semibold text-palette-navy">Personal</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Personal</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Es el padrón de la empresa. Busque, ordene y pagine el listado. Acciones:
    </p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>
        <strong>Registrar persona:</strong> abre el formulario de ficha (datos personales, contacto, régimen,
        AFP/ONP, datos de construcción civil si aplica). Guarde para crear el registro.
      </li>
      <li>
        Pulse una fila o el botón de edición para actualizar una ficha ya existente.
      </li>
      <li>
        <strong>Link de alta:</strong> genera un enlace para que la persona complete sus datos sin usuario del ERP.
        El destinatario abre <em>/invitar/persona/…</em> y envía el formulario.
      </li>
      <li>
        <strong>Exportar AFPnet:</strong> descarga el archivo de afiliados para el proceso de AFP (no sustituye la
        declaración mensual).
      </li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Un obrero de obra también vive aquí. El régimen de construcción civil, categoría (Operario, Oficial, Peón) y
      especialidad se capturan en la ficha; la liquidación semanal se hace en la ficha de la obra, no en Nóminas.
    </p>

    <h3 id="gh-contratos" class="mt-8 text-lg font-semibold text-palette-navy">Contratos</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Contratos</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Pestañas <strong>Contratos</strong> y <strong>Adendas</strong>. El resumen muestra contratos activos y personas
      sin contrato. Para contratar:
    </p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Pulse <strong>Nuevo contrato</strong>. Elija persona, plantilla, fechas, cargo y régimen.</li>
      <li>Guarde. Luego puede abrir el documento HTML del contrato.</li>
      <li>Para prórrogas o cambios, pestaña Adendas → <strong>Nueva adenda</strong> y descargue el documento.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Sin contrato vigente no hay asistencia, nómina ni tareo de obra para esa persona. En construcción civil el
      contrato queda vinculado a un proyecto (obra) y centro de costo.
    </p>

    <h3 id="gh-plantillas" class="mt-8 text-lg font-semibold text-palette-navy">Plantillas de contrato</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Plantillas de Contrato</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Pulse <strong>Nueva plantilla</strong> (o <strong>Editar</strong> en una existente). Se abre el editor en otra pestaña.</li>
      <li>Redacte el HTML e inserte tokens de persona y contrato (el editor lista los disponibles).</li>
      <li>Use la vista previa y pulse <strong>Guardar</strong>.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Las plantillas alimentan tanto el contrato como la adenda. Sin plantilla no se genera el documento.
    </p>

    <h3 id="gh-onboarding" class="mt-8 text-lg font-semibold text-palette-navy">Onboarding y offboarding</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Onboarding y Offboarding</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Pestaña <strong>Onboarding</strong> (ingreso) y pestaña <strong>Offboarding</strong> (cese y liquidación).
    </p>
    <p class="mt-3 text-sm font-medium text-palette-navy">Onboarding</p>
    <ol class="mt-2 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Elija la persona y el rango de fechas.</li>
      <li>
        Seleccione un pack: <strong>Todo el expediente</strong>, <strong>Pack de ingreso</strong>,
        <strong>Contrato, adenda y MOF</strong>, <strong>Legal SUNAFIL</strong> o <strong>Seguridad SST</strong>.
      </li>
      <li>Genere el expediente. Luego puede ver, descargar o imprimir cada generación.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El <strong>DOC-01</strong> (contrato) no se edita en Onboarding: usa la plantilla del contrato activo, la misma
      de <strong>Plantillas de Contrato</strong>. El <strong>DOC-02</strong> usa la plantilla de la última adenda si
      existe; si no hay adenda, queda el modelo del expediente.
    </p>
    <p class="mt-3 text-sm font-medium text-palette-navy">Offboarding</p>
    <p class="mt-2 text-sm leading-relaxed text-slate-600">
      En el panel de ceses registre el cese, calcule la liquidación y genere los documentos de salida a partir de las
      plantillas de offboarding.
    </p>

    <h3 id="gh-nominas" class="mt-8 text-lg font-semibold text-palette-navy">Nóminas</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Nóminas · Régimen Pequeña Empresa (REMYPE)</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Esta pantalla <strong>no liquida obreros de construcción civil</strong>. Para eso use la ficha de la obra
      (Planilla). El flujo administrativo es:
    </p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Pulse <strong>Nuevo periodo</strong>, indique año, mes y observaciones, y cree el periodo en estado Borrador.</li>
      <li>Abra el periodo. Pulse <strong>Generar boletas</strong> (usa asistencia y contratos vigentes).</li>
      <li>Revise cada boleta. Puede editar días, AFP y conceptos extra; luego recálcule.</li>
      <li>Si cambió asistencia, use <strong>Recalcular todas</strong>.</li>
      <li>Descargue PDF individual o por lote, imprima y envíe por correo.</li>
      <li>Revise el asiento contable (preliminar mientras el periodo está abierto) y la dispersión bancaria.</li>
      <li>Pulse <strong>Cerrar periodo</strong>. El asiento pasa a definitivo y ya no se editan boletas.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El neto descuenta AFP/ONP; EsSalud es aporte del empleador. Configure el membrete y la firma de las boletas en
      Firmas Digitales antes de emitir PDF.
    </p>

    <h3 id="gh-declaraciones" class="mt-8 text-lg font-semibold text-palette-navy">Declaración mensual</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Declaración mensual</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Cree una declaración asociada al periodo de nómina.</li>
      <li>En el detalle pulse <strong>Consolidar RCC</strong> para incorporar la planilla de construcción civil del mes.</li>
      <li>Importe recibos por honorarios y asigne DNI cuando corresponda.</li>
      <li>
        Descargue <strong>PLAME ZIP</strong>, <strong>AFPnet</strong>, <strong>Asiento PDF</strong> y archivos de
        dispersión (<strong>CCI</strong>, <strong>BCP</strong>, <strong>Interbank</strong>).
      </li>
      <li>Pulse <strong>Cerrar declaración</strong> cuando el paquete esté listo para presentar.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El cierre de declaración es independiente del cierre de cada periodo de nómina o de planilla RCC. Conviene
      cerrar primero las planillas de obra y el periodo administrativo del mes.
    </p>

    <h3 id="gh-asistencia" class="mt-8 text-lg font-semibold text-palette-navy">Asistencia</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Asistencia</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Controla el tareo del personal administrativo (no el parte diario de obra). Navegue mes y año.
    </p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Configure días laborables y feriados del mes.</li>
      <li>Pulse generar planilla de tareo para el mes.</li>
      <li>Abra la planilla a pantalla completa para marcar asistencia, faltas, permisos y descansos por día.</li>
      <li>Guarde. Hay un tutorial de ayuda en esa grilla.</li>
      <li>Registre conceptos variables del mes (bonos, descuentos) que la nómina tomará al generar boletas.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Si el periodo de nómina ya está cerrado, cambiar asistencia no recalcula boletas hasta que exista un periodo
      abierto y se pulse Recalcular.
    </p>

    <h3 id="gh-kpis" class="mt-8 text-lg font-semibold text-palette-navy">KPIs y desempeño</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → KPIs y desempeño</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Filtre por trimestre y año, o busque por colaborador.</li>
      <li>Abra el detalle de la persona.</li>
      <li>Califique competencias de 1 a 5, indique si la meta se cumplió, escriba comentarios y estado.</li>
      <li>Guarde. Puede imprimir la evaluación.</li>
    </ol>

    <h3 id="gh-mof" class="mt-8 text-lg font-semibold text-palette-navy">Manuales MOF</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Manuales MOF</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Busque el cargo.</li>
      <li>Edite propósito y funciones, agregue o quite funciones y pulse <strong>Guardar</strong>.</li>
      <li>Descargue el MOF del puesto o use <strong>Exportar MOF completo</strong> para toda la empresa.</li>
    </ol>

    <h3 id="gh-vacaciones" class="mt-8 text-lg font-semibold text-palette-navy">Vacaciones</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Vacaciones</p>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Filtre por tipo y estado, o busque por persona.</li>
      <li>Pulse <strong>Nueva solicitud</strong>: el sistema muestra saldo y contrato vigente.</li>
      <li>Apruebe, rechace o elimine según corresponda.</li>
      <li>Abra el documento HTML de la solicitud cuando lo necesite el expediente.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      En construcción civil las vacaciones se pagan de forma proporcional en cada boleta RCC (10&nbsp;% sobre jornal
      básico y extras). Esta pantalla cubre solicitudes del régimen administrativo.
    </p>

    <h3 id="gh-constancias" class="mt-8 text-lg font-semibold text-palette-navy">Constancias y certificados</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Constancias y Certificados</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Listado de contratos. En la fila correspondiente genere y abra la <strong>Constancia de trabajo</strong>. El
      documento usa los datos del contrato y de Firmas Digitales.
    </p>

    <h3 id="gh-firmas" class="mt-8 text-lg font-semibold text-palette-navy">Firmas digitales</h3>
    <p class="mt-2 text-xs font-medium text-slate-400">Ruta: Gestión Humana → Firmas Digitales</p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Configure razón social, RUC, domicilio, representante, logo y firma que aparecen en boletas PDF, constancias y
      formatos. Pulse <strong>Guardar</strong> después de cada cambio. Sin esta configuración, los PDF salen sin
      membrete corporativo.
    </p>
  </section>
</template>
