ALTER TABLE proyecto
    ADD COLUMN cliente_razon VARCHAR(160),
    ADD COLUMN cliente_ruc VARCHAR(11),
    ADD COLUMN fecha_inicio DATE,
    ADD COLUMN fecha_fin DATE,
    ADD COLUMN estado_obra VARCHAR(20) NOT NULL DEFAULT 'EN_EJECUCION';

ALTER TABLE proyecto
    ADD CONSTRAINT ck_proyecto_estado_obra
        CHECK (estado_obra IN ('EN_EJECUCION', 'PARALIZADA', 'LIQUIDADA')),
    ADD CONSTRAINT ck_proyecto_vigencia
        CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio);

CREATE TABLE proyecto_responsable (
    id          BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    persona_id  BIGINT      NOT NULL REFERENCES persona (id),
    rol         VARCHAR(30) NOT NULL,
    CONSTRAINT ck_proyecto_responsable_rol
        CHECK (rol IN ('RESIDENTE', 'MAESTRO_OBRA', 'SUPERVISOR_SSOMA')),
    CONSTRAINT uq_proyecto_responsable_rol UNIQUE (proyecto_id, rol)
);

CREATE INDEX idx_proyecto_responsable_persona ON proyecto_responsable (persona_id);
