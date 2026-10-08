CREATE TABLE mof_puesto (
    id            BIGSERIAL PRIMARY KEY,
    codigo        VARCHAR(40)  NOT NULL,
    cargo         VARCHAR(160) NOT NULL,
    grupo         VARCHAR(40)  NOT NULL,
    proposito     TEXT         NOT NULL,
    superior      VARCHAR(240) NOT NULL,
    subordinados  VARCHAR(500) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_mof_puesto_codigo UNIQUE (codigo)
);

CREATE TABLE mof_funcion (
    id         BIGSERIAL PRIMARY KEY,
    puesto_id  BIGINT  NOT NULL REFERENCES mof_puesto (id) ON DELETE CASCADE,
    orden      INTEGER NOT NULL,
    texto      TEXT    NOT NULL
);

CREATE INDEX ix_mof_funcion_puesto ON mof_funcion (puesto_id);

CREATE TABLE mof_requisito (
    id         BIGSERIAL PRIMARY KEY,
    puesto_id  BIGINT      NOT NULL REFERENCES mof_puesto (id) ON DELETE CASCADE,
    tipo       VARCHAR(20) NOT NULL,
    orden      INTEGER     NOT NULL,
    texto      TEXT        NOT NULL,
    CONSTRAINT ck_mof_requisito_tipo CHECK (tipo IN ('EDUCACION', 'EXPERIENCIA', 'COMPETENCIA'))
);

CREATE INDEX ix_mof_requisito_puesto ON mof_requisito (puesto_id);
