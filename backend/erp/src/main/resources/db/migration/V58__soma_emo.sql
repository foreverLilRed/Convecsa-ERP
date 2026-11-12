CREATE TABLE soma_emo_examen (
    id                BIGSERIAL PRIMARY KEY,
    persona_id        BIGINT        NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    proyecto_id       BIGINT        REFERENCES proyecto (id) ON DELETE SET NULL,
    tipo_emo          VARCHAR(30)   NOT NULL,
    clinica_nombre    VARCHAR(160)  NOT NULL,
    clinica_ruc       VARCHAR(11),
    medico_evaluador  VARCHAR(160),
    cmp_medico        VARCHAR(30),
    fecha_examen      DATE          NOT NULL,
    fecha_vencimiento DATE          NOT NULL,
    aptitud           VARCHAR(30)   NOT NULL,
    restricciones     VARCHAR(2000),
    interconsultas    VARCHAR(1000),
    observaciones     VARCHAR(1000),
    documento_obra_id BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_emo_tipo CHECK (tipo_emo IN (
        'INGRESO', 'PERIODICO', 'RETIRO', 'CAMBIO_PUESTO', 'REINCORPORACION'
    )),
    CONSTRAINT ck_soma_emo_aptitud CHECK (aptitud IN (
        'APTO', 'APTO_CON_RESTRICCION', 'OBSERVADO', 'NO_APTO'
    )),
    CONSTRAINT ck_soma_emo_fechas CHECK (fecha_vencimiento >= fecha_examen),
    CONSTRAINT ck_soma_emo_doc_requiere_obra CHECK (documento_obra_id IS NULL OR proyecto_id IS NOT NULL)
);

CREATE INDEX idx_soma_emo_persona ON soma_emo_examen (persona_id, fecha_examen DESC);
CREATE INDEX idx_soma_emo_vencimiento ON soma_emo_examen (fecha_vencimiento);
