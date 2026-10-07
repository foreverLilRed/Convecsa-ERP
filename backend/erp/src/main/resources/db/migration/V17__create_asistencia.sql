ALTER TABLE jornada
    ADD COLUMN horas_semanales NUMERIC(5, 2),
    ADD COLUMN horas_diarias NUMERIC(5, 2);

UPDATE jornada SET horas_semanales = 48.00 WHERE codigo = '48H_SEMANAL';
UPDATE jornada SET horas_semanales = 36.00 WHERE codigo = '36H_SEMANAL';
UPDATE jornada SET horas_semanales = 24.00 WHERE codigo = '24H_SEMANAL';
UPDATE jornada SET horas_diarias = 8.00 WHERE codigo = '8H_DIARIA';
UPDATE jornada SET horas_diarias = 8.00 WHERE codigo = '6X1';
UPDATE jornada SET horas_diarias = 8.00 WHERE codigo = '14X7';

CREATE TABLE asistencia_config (
    id           BIGINT PRIMARY KEY,
    dias_semana  VARCHAR(20) NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO asistencia_config (id, dias_semana) VALUES (1, '1,2,3,4,5');

CREATE TABLE asistencia_feriado (
    id         BIGSERIAL PRIMARY KEY,
    fecha      DATE         NOT NULL UNIQUE,
    nombre     VARCHAR(160) NOT NULL,
    laborable  BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX ix_asistencia_feriado_fecha ON asistencia_feriado (fecha);

CREATE TABLE asistencia_registro (
    id                 BIGSERIAL PRIMARY KEY,
    persona_id         BIGINT         NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    contrato_id        BIGINT         NOT NULL REFERENCES contrato (id) ON DELETE RESTRICT,
    fecha              DATE           NOT NULL,
    horas_esperadas    NUMERIC(5, 2)  NOT NULL,
    horas_trabajadas   NUMERIC(5, 2)  NOT NULL,
    estado             VARCHAR(20)    NOT NULL,
    observacion        VARCHAR(500),
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_asistencia_registro_persona_fecha UNIQUE (persona_id, fecha),
    CONSTRAINT ck_asistencia_horas CHECK (horas_trabajadas >= 0 AND horas_trabajadas <= 24)
);

CREATE INDEX ix_asistencia_registro_fecha ON asistencia_registro (fecha);
CREATE INDEX ix_asistencia_registro_persona ON asistencia_registro (persona_id);
