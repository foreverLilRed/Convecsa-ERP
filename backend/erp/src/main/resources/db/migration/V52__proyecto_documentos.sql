CREATE TABLE documento_obra (
    id                BIGSERIAL PRIMARY KEY,
    proyecto_id       BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo              VARCHAR(30)    NOT NULL,
    nombre_original   VARCHAR(240)   NOT NULL,
    mime              VARCHAR(80)    NOT NULL,
    tamano_bytes      BIGINT         NOT NULL,
    ruta_relativa     VARCHAR(400)   NOT NULL,
    observaciones     VARCHAR(400),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_documento_obra_ruta UNIQUE (ruta_relativa),
    CONSTRAINT ck_documento_obra_tipo CHECK (tipo IN (
        'CONTRATO', 'PLANO', 'ACTA', 'VALORIZACION', 'CESION_FACTORING'
    )),
    CONSTRAINT ck_documento_obra_tamano CHECK (tamano_bytes > 0)
);

CREATE INDEX idx_documento_obra_proyecto ON documento_obra (proyecto_id, created_at DESC);
