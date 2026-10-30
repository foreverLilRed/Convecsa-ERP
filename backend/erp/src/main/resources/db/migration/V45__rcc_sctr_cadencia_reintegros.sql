ALTER TABLE proyecto
    ADD COLUMN nivel_riesgo VARCHAR(10),
    ADD COLUMN tasa_sctr_salud NUMERIC(7, 6) NOT NULL DEFAULT 0,
    ADD COLUMN tasa_sctr_pension NUMERIC(7, 6) NOT NULL DEFAULT 0;

ALTER TABLE contrato_cc
    ADD COLUMN centro_costo VARCHAR(40);

ALTER TABLE periodo_cc
    ADD COLUMN cadencia VARCHAR(20) NOT NULL DEFAULT 'SEMANAL';

ALTER TABLE boleta_cc_concepto
    ADD COLUMN origen VARCHAR(20) NOT NULL DEFAULT 'CALCULADO';

ALTER TABLE consolidado_cc
    ADD COLUMN centro_costo VARCHAR(40);

CREATE TABLE boleta_cc_envio_correo (
    id            BIGSERIAL PRIMARY KEY,
    boleta_id     BIGINT         NOT NULL REFERENCES boleta_cc (id) ON DELETE CASCADE,
    destinatario  VARCHAR(180)   NOT NULL,
    estado        VARCHAR(20)    NOT NULL,
    detalle       VARCHAR(1000),
    enviado_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_boleta_cc_envio_boleta ON boleta_cc_envio_correo (boleta_id);
