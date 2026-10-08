ALTER TABLE plantillas_contrato
    ADD COLUMN uso VARCHAR(20) NOT NULL DEFAULT 'CONTRATO';

ALTER TABLE plantillas_contrato
    ADD CONSTRAINT ck_plantillas_contrato_uso CHECK (uso IN ('CONTRATO', 'ADENDA'));

CREATE TABLE adenda (
    id                     BIGSERIAL PRIMARY KEY,
    contrato_id            BIGINT         NOT NULL REFERENCES contrato (id) ON DELETE RESTRICT,
    plantilla_contrato_id  BIGINT         NOT NULL REFERENCES plantillas_contrato (id),
    numero                 INTEGER        NOT NULL,
    fecha_fin_anterior     DATE           NOT NULL,
    fecha_fin_nueva        DATE           NOT NULL,
    sueldo                 NUMERIC(12, 2) NOT NULL,
    motivo                 VARCHAR(2000)  NOT NULL,
    ciudad_emision         VARCHAR(80),
    fecha_emision          DATE           NOT NULL,
    metadata               JSONB          NOT NULL DEFAULT '{}'::jsonb,
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_adenda_contrato_numero UNIQUE (contrato_id, numero),
    CONSTRAINT ck_adenda_fechas CHECK (fecha_fin_nueva > fecha_fin_anterior)
);

CREATE INDEX ix_adenda_contrato ON adenda (contrato_id);

INSERT INTO plantillas_contrato (codigo, nombre, uso, plantilla, metadata)
VALUES (
    'ADENDA_PRORROGA',
    'Adenda de prórroga',
    'ADENDA',
    $html$
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"/>
  <title>Adenda - {{trabajador_nombres}} {{trabajador_apellidos}}</title>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
    * { margin: 0; padding: 0; box-sizing: border-box; }
    @page { size: A4 portrait; margin: 0; }
    html, body { width: 210mm; background: #f3f4f6; }
    body { font-family: 'Inter', sans-serif; font-size: 8pt; line-height: 1.35; color: #111827; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    .pg { width: 210mm; min-height: 297mm; margin: 10px auto; background: #fff; padding: 12mm 14mm 10mm; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
    .print-bar { background: #0f2744; color: #fff; padding: 8px 12px; display: flex; align-items: center; justify-content: space-between; font-size: 8.5px; margin-bottom: 10px; border-radius: 4px; }
    .print-btn { background: #fff; color: #0f2744; border: none; border-radius: 4px; padding: 5px 12px; font-size: 8.5px; font-weight: 700; cursor: pointer; }
    .hdr { text-align: center; border-bottom: 1.5px solid #1f2937; padding-bottom: 6px; margin-bottom: 10px; }
    .doc-t { font-size: 12pt; font-weight: 800; text-transform: uppercase; color: #0f2744; }
    .ct { font-size: 8pt; font-weight: 700; text-transform: uppercase; border-bottom: 1px solid #d1d5db; padding-bottom: 2px; margin: 10px 0 6px; }
    p { margin-bottom: 6px; text-align: justify; }
    .sigs { margin-top: 28px; display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
    .sb { text-align: center; }
    .sl { border-top: 1.2px solid #1f2937; margin-top: 48px; margin-bottom: 4px; }
    .sn { font-weight: 700; font-size: 8pt; text-transform: uppercase; }
    .sd { font-size: 7pt; color: #4b5563; }
    @media print {
      html, body { background: #fff !important; }
      .pg { box-shadow: none; margin: 0; }
      .print-bar, .no-print { display: none !important; }
    }
  </style>
</head>
<body>
  <div class="pg">
    <div class="print-bar no-print">
      <span>Ctrl+P para exportar a PDF · tamaño A4</span>
      <button class="print-btn" type="button" onclick="window.print()">Guardar PDF / Imprimir</button>
    </div>
    <div class="hdr">
      <div class="doc-t">Adenda al Contrato de Trabajo — Prórroga de Plazo</div>
      <div style="font-size: 8pt; color: #4b5563; margin-top: 4px;">D.Leg. Nº 1086 · D.S. Nº 003-97-TR</div>
    </div>
    <div class="ct">Partes</div>
    <p>Conste por el presente documento la Adenda al Contrato de Trabajo sujeto a Modalidad por Servicio Específico, suscrito entre <strong>{{empleador_razon}}</strong>, con RUC Nº {{empleador_ruc}} y domicilio fiscal en {{empleador_domicilio}}, debidamente representada por el señor <strong>{{empleador_representante}}</strong> con D.N.I. N° {{empleador_dni_representante}}, a quien se denominará EL EMPLEADOR; y de la otra parte <strong>{{trabajador_nombres}} {{trabajador_apellidos}}</strong>, identificado con documento N° <strong>{{trabajador_dni}}</strong>, quien se desempeña en el cargo de <strong>{{cargo}}</strong> para el proyecto <strong>{{proyecto}}</strong><span>{{ubicacion_obra}}</span>, a quien se denominará EL TRABAJADOR.</p>
    <div class="ct">Prórroga</div>
    <p>Las partes acuerdan prorrogar el contrato cuyo vencimiento era el <strong>{{fecha_fin_anterior}}</strong>, extendiendo su vigencia hasta el <strong>{{fecha_fin_nueva}}</strong> (duración de la adenda: <strong>{{duracion_adenda}}</strong>).</p>
    <p>La remuneración mensual convenida es de <strong>S/ {{sueldo}}</strong> ({{sueldo_letras}}).</p>
    <div class="ct">Motivo</div>
    <p>{{motivo}}</p>
    <p>En todo lo no modificado por esta adenda, siguen vigentes las cláusulas del contrato original.</p>
    <p>Firmado en la ciudad de <strong>{{ciudad_firma}}</strong>, a los {{fecha_firma}}.</p>
    <div class="sigs">
      <div class="sb">
        <div class="sl"></div>
        <div class="sn">{{empleador_razon}}</div>
        <div class="sd">EL EMPLEADOR · RUC {{empleador_ruc}}</div>
      </div>
      <div class="sb">
        <div class="sl"></div>
        <div class="sn">{{trabajador_nombres}} {{trabajador_apellidos}}</div>
        <div class="sd">EL TRABAJADOR · {{trabajador_dni}}</div>
      </div>
    </div>
  </div>
</body>
</html>
$html$,
    '[]'::jsonb
);
