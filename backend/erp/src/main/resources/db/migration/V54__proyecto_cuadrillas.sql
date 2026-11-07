CREATE TABLE cuadrilla_obra (
    id             BIGSERIAL PRIMARY KEY,
    proyecto_id    BIGINT       NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    codigo         VARCHAR(40)  NOT NULL,
    nombre         VARCHAR(160) NOT NULL,
    centro_costo   VARCHAR(40),
    observaciones  VARCHAR(400),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cuadrilla_obra_codigo UNIQUE (proyecto_id, codigo)
);

CREATE INDEX idx_cuadrilla_obra_proyecto ON cuadrilla_obra (proyecto_id, codigo);

CREATE TABLE cuadrilla_miembro (
    id            BIGSERIAL PRIMARY KEY,
    cuadrilla_id  BIGINT      NOT NULL REFERENCES cuadrilla_obra (id) ON DELETE CASCADE,
    proyecto_id   BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    persona_id    BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cuadrilla_miembro_persona UNIQUE (proyecto_id, persona_id),
    CONSTRAINT uq_cuadrilla_miembro_cuadrilla UNIQUE (cuadrilla_id, persona_id)
);

CREATE INDEX idx_cuadrilla_miembro_proyecto ON cuadrilla_miembro (proyecto_id);
CREATE INDEX idx_cuadrilla_miembro_cuadrilla ON cuadrilla_miembro (cuadrilla_id);
