CREATE TABLE vacacion_solicitud (
    id                      BIGSERIAL PRIMARY KEY,
    persona_id              BIGINT       NOT NULL REFERENCES persona (id),
    contrato_id             BIGINT       NOT NULL REFERENCES contrato (id),
    tipo                     VARCHAR(20)  NOT NULL,
    estado                   VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    periodo_inicio           DATE         NOT NULL,
    periodo_fin              DATE         NOT NULL,
    dias_derecho_periodo     INTEGER      NOT NULL DEFAULT 30,
    dias_solicitados         INTEGER      NOT NULL,
    fecha_inicio             DATE         NOT NULL,
    fecha_fin                DATE         NOT NULL,
    fecha_retorno            DATE         NOT NULL,
    observacion              TEXT,
    aprobada_en              TIMESTAMPTZ,
    rechazada_en             TIMESTAMPTZ,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_vacacion_tipo CHECK (tipo IN ('ORDINARIA', 'ADELANTADA', 'FRACCIONADA')),
    CONSTRAINT ck_vacacion_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    CONSTRAINT ck_vacacion_dias_solicitados CHECK (dias_solicitados BETWEEN 1 AND 30),
    CONSTRAINT ck_vacacion_dias_derecho CHECK (dias_derecho_periodo BETWEEN 1 AND 60),
    CONSTRAINT ck_vacacion_periodo CHECK (periodo_fin >= periodo_inicio),
    CONSTRAINT ck_vacacion_fechas CHECK (fecha_fin >= fecha_inicio AND fecha_retorno > fecha_fin)
);

CREATE INDEX ix_vacacion_persona_periodo
    ON vacacion_solicitud (persona_id, periodo_inicio, periodo_fin);
CREATE INDEX ix_vacacion_estado_fechas
    ON vacacion_solicitud (estado, fecha_inicio, fecha_fin);

-- Separa los conceptos generados por el tareo de los ajustes manuales. Esto permite
-- recalcular una boleta después de aprobar vacaciones sin duplicar horas o descuentos.
UPDATE boleta_concepto
SET origen = 'TAREO'
WHERE origen = 'MANUAL'
  AND (
      label LIKE 'Horas extras 25% (%'
      OR label LIKE 'Horas extras 35% (%'
      OR label LIKE 'Tardanzas (%'
      OR label = 'Comisión'
      OR label LIKE 'Comisión: %'
      OR label = 'Trabajo nocturno (sobretasa 35%)'
      OR label LIKE 'Trabajo nocturno (sobretasa 35%): %'
      OR label = 'Asignación de alimentación'
      OR label LIKE 'Asignación de alimentación: %'
      OR label = 'Adelanto de quincena'
      OR label LIKE 'Adelanto de quincena: %'
      OR label = 'Cuota sindical'
      OR label LIKE 'Cuota sindical: %'
      OR label = 'Descuento judicial'
      OR label LIKE 'Descuento judicial: %'
      OR label = 'Otro ingreso'
      OR label LIKE 'Otro ingreso: %'
      OR label = 'Otro descuento'
      OR label LIKE 'Otro descuento: %'
  );
