CREATE TABLE soma_inspeccion_equipo (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    asignacion_equipo_id BIGINT         NOT NULL REFERENCES asignacion_equipo (id) ON DELETE CASCADE,
    operador_persona_id  BIGINT         REFERENCES persona (id) ON DELETE SET NULL,
    fecha                DATE           NOT NULL,
    horometro_lectura    NUMERIC(12, 2),
    checklist_json       JSONB          NOT NULL DEFAULT '{}'::jsonb,
    resultado            VARCHAR(30)    NOT NULL,
    observaciones        VARCHAR(600),
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_insp_equipo_res CHECK (resultado IN (
        'OPERATIVO', 'OPERATIVO_CON_OBSERVACION', 'INOPERATIVO_BLOQUEADO'
    ))
);

CREATE INDEX idx_soma_insp_equipo_asig ON soma_inspeccion_equipo (asignacion_equipo_id, fecha DESC);
