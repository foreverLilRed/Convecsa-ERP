<template>
  <section>
    <h2 id="introduccion" class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Introducción</h2>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Este manual describe cómo usar Convecsa ERP en el día a día de la constructora: personal, planillas, obras,
      almacén, valorizaciones y seguridad (SOMA). Los nombres de botones y pestañas coinciden con los de la pantalla.
    </p>

    <h3 id="que-es" class="mt-8 text-lg font-semibold text-palette-navy">Qué es el ERP</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El sistema cubre tres bloques que conviven en la misma sesión:
    </p>
    <ul class="mt-3 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>
        <strong>Gestión Humana:</strong> padrón de personas, contratos, asistencia administrativa, nómina REMYPE,
        declaración mensual, vacaciones, constancias y MOF.
      </li>
      <li>
        <strong>Proyectos:</strong> obras de construcción civil, tareo diario, planilla RCC, presupuesto, almacén,
        recursos, avances, financiamiento y documentos de obra.
      </li>
      <li>
        <strong>SOMA:</strong> seguridad, salud ocupacional y medio ambiente en obra, más un tablero corporativo y
        formatos SUNAFIL (R.M. 050-2013-TR).
      </li>
    </ul>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Hay dos planillas distintas y no se mezclan: la de
      <strong>régimen administrativo</strong> (menú Gestión Humana → Nóminas y Asistencia) y la de
      <strong>construcción civil</strong> (ficha de obra → Tareo y Planilla). El personal de oficina se liquida por
      mes; el obrero de obra se tareo por día y se liquida por periodo de la obra (semanal, quincenal o mensual).
    </p>

    <h3 id="ingreso" class="mt-8 text-lg font-semibold text-palette-navy">Ingreso y sesión</h3>
    <ol class="mt-3 list-decimal space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li>Abra el ERP en el navegador. Si no hay sesión, aparece <strong>Iniciar sesión</strong>.</li>
      <li>Escriba su <strong>usuario o correo</strong> corporativo y la <strong>contraseña</strong>.</li>
      <li>
        Marque <strong>Recordarme en este equipo</strong> si desea que la sesión se conserve al cerrar el navegador.
        Si no lo marca, la sesión dura solo mientras el navegador esté abierto.
      </li>
      <li>Pulse el botón de ingreso. El sistema abre el dashboard.</li>
    </ol>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Para salir, use <strong>Cerrar sesión</strong> al pie del menú izquierdo. El enlace público
      <em>/invitar/persona/…</em> no pide usuario: sirve para que un candidato complete su ficha con un token enviado
      desde Personal.
    </p>

    <h3 id="menu" class="mt-8 text-lg font-semibold text-palette-navy">Menú y navegación</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El menú izquierdo está siempre expandido y agrupado. En escritorio permanece fijo; en celular se abre con el
      ícono de menú de la barra superior. Los grupos actuales son:
    </p>
    <ul class="mt-3 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li><strong>Gestión Humana</strong> — 12 pantallas de personal y planilla administrativa.</li>
      <li><strong>Proyectos</strong> — listado de obras y catálogo de materiales. La ficha de cada obra se abre al pulsar una fila.</li>
      <li><strong>SOMA</strong> — tablero corporativo, vigilancia médica, capacitaciones y formatos SUNAFIL. Solo gerencia e ingeniero SOMA.</li>
      <li><strong>Documentación</strong> — este manual. Solo Superadmin y Admin.</li>
    </ul>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El título de la barra superior cambia según la pantalla. Dentro de una obra, las pestañas (Resumen, Tareo,
      Planilla, Presupuesto, Almacén, etc.) sustituyen al menú lateral para moverse entre módulos de esa obra.
    </p>

    <h3 id="roles" class="mt-8 text-lg font-semibold text-palette-navy">Roles y permisos</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Cada usuario tiene un solo rol. Además, gerencia puede asignar <strong>accesos por obra</strong> en la ficha
      (sección Accesos). Sin acceso a una obra, esa obra no aparece o no se puede operar.
    </p>
    <div class="mt-4 overflow-x-auto">
      <table class="w-full min-w-[32rem] border-collapse text-left text-sm">
        <thead>
          <tr class="border-b border-slate-200 text-xs uppercase tracking-wide text-slate-500">
            <th class="py-2 pr-3 font-semibold">Rol</th>
            <th class="py-2 font-semibold">Qué puede hacer en el sistema</th>
          </tr>
        </thead>
        <tbody class="text-slate-600">
          <tr class="border-b border-slate-100 align-top">
            <td class="py-2 pr-3 font-medium text-palette-navy">SUPERADMIN / ADMIN</td>
            <td class="py-2">Gerencia. Alta de obras, accesos, todo Gestión Humana, SOMA corporativo y este manual.</td>
          </tr>
          <tr class="border-b border-slate-100 align-top">
            <td class="py-2 pr-3 font-medium text-palette-navy">INGENIERO_SOMA</td>
            <td class="py-2">Menú SOMA corporativo y operación SOMA en las obras a las que tenga acceso.</td>
          </tr>
          <tr class="border-b border-slate-100 align-top">
            <td class="py-2 pr-3 font-medium text-palette-navy">SUPERVISOR / RESIDENTE</td>
            <td class="py-2">Operación de las obras asignadas (tareo, planilla, presupuesto, avances, SOMA de obra). No ven el menú SOMA corporativo ni este manual.</td>
          </tr>
          <tr class="align-top">
            <td class="py-2 pr-3 font-medium text-palette-navy">ALMACEN</td>
            <td class="py-2">En una obra solo entra a la pestaña Almacén (Kardex). No crea requerimientos ni órdenes de compra, ni registra vales, ajustes o cierre de inventario. El catálogo de materiales es de solo lectura.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <h3 id="dashboard" class="mt-8 text-lg font-semibold text-palette-navy">Dashboard</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Al iniciar sesión se abre el panel con tarjetas de personas, contratos, nómina del mes y asistencia. Use
      <strong>Actualizar datos</strong> para recargar. Cada tarjeta enlaza a Personal, Contratos, Nóminas o Asistencia.
    </p>
  </section>
</template>
