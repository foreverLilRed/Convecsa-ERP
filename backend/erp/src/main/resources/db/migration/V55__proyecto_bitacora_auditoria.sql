CREATE TABLE bitacora_obra (
    id           BIGSERIAL PRIMARY KEY,
    proyecto_id  BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    fecha        DATE          NOT NULL,
    texto        VARCHAR(4000) NOT NULL,
    usuario      VARCHAR(80)   NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bitacora_obra_proyecto ON bitacora_obra (proyecto_id, fecha DESC, id DESC);

CREATE TABLE auditoria_obra (
    id           BIGSERIAL PRIMARY KEY,
    proyecto_id  BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    accion       VARCHAR(40)   NOT NULL,
    entidad      VARCHAR(40)   NOT NULL,
    entidad_id   BIGINT,
    detalle      VARCHAR(1000),
    usuario      VARCHAR(80)   NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auditoria_obra_proyecto ON auditoria_obra (proyecto_id, created_at DESC, id DESC);
