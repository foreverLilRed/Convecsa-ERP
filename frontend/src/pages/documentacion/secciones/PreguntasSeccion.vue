<template>
  <section>
    <h2 id="preguntas" class="text-2xl font-bold tracking-tight text-palette-navy sm:text-3xl">Preguntas frecuentes</h2>

    <h3 id="faq-obra" class="mt-8 text-lg font-semibold text-palette-navy">No veo una obra</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El listado de Obras solo muestra proyectos a los que su usuario tiene acceso. Pida a Superadmin o Admin que
      abra la ficha → Resumen → Accesos, agregue su usuario y pulse <strong>Guardar accesos</strong>. Gerencia ve
      todas las obras. Si la obra está inactiva, puede no operar igual que una en ejecución: revise el interruptor
      Activo y el estado (en ejecución, paralizada, liquidada).
    </p>

    <h3 id="faq-periodo" class="mt-8 text-lg font-semibold text-palette-navy">Periodo cerrado</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Hay varios “cierres” y no se revierten desde la pantalla:
    </p>
    <ul class="mt-3 list-disc space-y-1.5 pl-5 text-sm leading-relaxed text-slate-600">
      <li><strong>Periodo de nómina administrativa:</strong> no se editan boletas ni se regenera. El asiento queda definitivo.</li>
      <li><strong>Periodo RCC de la obra:</strong> el tareo de ese rango se bloquea y no se vuelve a liquidar.</li>
      <li><strong>Declaración mensual:</strong> el paquete PLAME/AFPnet queda cerrado.</li>
      <li><strong>Avance del mes</strong> y <strong>cierre de inventario</strong> congelan metrado y valor de stock.</li>
    </ul>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Si detecta un error, corríjalo en el siguiente periodo (concepto extra, ajuste de almacén, adicional de
      presupuesto) o contacte a gerencia. No intente “reabrir” borrando datos.
    </p>

    <h3 id="faq-almacen" class="mt-8 text-lg font-semibold text-palette-navy">Rol Almacén</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Un usuario con rol Almacén, al entrar a una obra, es redirigido a la pestaña Almacén. Solo ve el Kardex
      (stock y movimientos). No aparecen Requerimientos ni Compras, ni los bloques de vale de salida, ajuste o
      cierre de mes. El catálogo de materiales es de solo lectura. Para crear una OC o un vale, use un usuario
      Residente, Supervisor o gerencia.
    </p>

    <h3 id="faq-soma" class="mt-8 text-lg font-semibold text-palette-navy">No aparece el menú SOMA</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      El grupo <strong>SOMA</strong> del menú izquierdo solo se muestra a Superadmin, Admin e Ingeniero SOMA. Si
      entra por URL a <em>/soma/dashboard</em> con otro rol, el sistema lo devuelve al dashboard. Residente y
      Supervisor operan SOMA desde la <strong>ficha de la obra → pestaña SOMA</strong>, que es el registro de campo.
      Este manual (Documentación) tampoco aparece fuera de Superadmin y Admin.
    </p>

    <h3 id="faq-descargas" class="mt-8 text-lg font-semibold text-palette-navy">Descargas y documentos</h3>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Boletas PDF, PLAME, formatos SUNAFIL, MOF, contratos HTML y archivos de la pestaña Documentos se descargan con
      la sesión iniciada. No copie la URL a un correo externo esperando que se abra sin login: el archivo viaja con
      el token del usuario. Si una descarga falla, verifique que sigue logueado y que Firmas Digitales esté
      configurado cuando el documento lleve membrete.
    </p>
    <p class="mt-3 text-sm leading-relaxed text-slate-600">
      Toda evidencia SOMA es un documento de obra: necesita proyecto. No se adjuntan archivos “sueltos” a nivel
      empresa.
    </p>
  </section>
</template>
