ALTER TABLE asistencia_registro
    ADD COLUMN codigo VARCHAR(20) NOT NULL DEFAULT 'A',
    ADD COLUMN minutos_extras_25 INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN minutos_extras_35 INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN minutos_tardanza INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_asistencia_minutos_no_negativos CHECK (
        minutos_extras_25 >= 0 AND minutos_extras_35 >= 0 AND minutos_tardanza >= 0
    );

CREATE TABLE asistencia_concepto_variable (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL REFERENCES persona(id) ON DELETE CASCADE,
    contrato_id BIGINT NOT NULL REFERENCES contrato(id) ON DELETE RESTRICT,
    anio INTEGER NOT NULL,
    mes INTEGER NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    detalle VARCHAR(300),
    CONSTRAINT ck_asistencia_concepto_periodo CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_asistencia_concepto_monto CHECK (monto >= 0)
);

CREATE INDEX ix_asistencia_concepto_periodo_persona
    ON asistencia_concepto_variable(anio, mes, persona_id);
