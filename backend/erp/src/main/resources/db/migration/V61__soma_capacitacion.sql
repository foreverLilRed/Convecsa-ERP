CREATE TABLE soma_capacitacion (
    id                BIGSERIAL PRIMARY KEY,
    proyecto_id       BIGINT        REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo              VARCHAR(40)   NOT NULL,
    tema              VARCHAR(240)  NOT NULL,
    fecha             DATE          NOT NULL,
    horas_duracion    NUMERIC(5, 2) NOT NULL,
    expositor_nombre  VARCHAR(160)  NOT NULL,
    expositor_entidad VARCHAR(160),
    puntaje_simulacro NUMERIC(5, 2),
    observaciones     VARCHAR(1000),
    evidencia_doc_id  BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_capacitacion_tipo CHECK (tipo IN (
        'INDUCCION_HOMBRE_NUEVO', 'CAPACITACION_PROGRAMADA',
        'ENTRENAMIENTO_ESPECIFICO_ALTO_RIESGO', 'SIMULACRO_EMERGENCIA'
    )),
    CONSTRAINT ck_soma_capacitacion_horas CHECK (horas_duracion > 0),
    CONSTRAINT ck_soma_capacitacion_doc_requiere_obra CHECK (evidencia_doc_id IS NULL OR proyecto_id IS NOT NULL)
);

CREATE INDEX idx_soma_capacitacion_obra ON soma_capacitacion (proyecto_id, fecha DESC);

CREATE TABLE soma_capacitacion_asistente (
    id              BIGSERIAL PRIMARY KEY,
    capacitacion_id BIGINT       NOT NULL REFERENCES soma_capacitacion (id) ON DELETE CASCADE,
    persona_id      BIGINT       NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    nota            NUMERIC(5, 2),
    aprobado        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_soma_cap_asistente_nota CHECK (nota IS NULL OR nota BETWEEN 0 AND 20),
    CONSTRAINT uq_soma_capacitacion_asistente UNIQUE (capacitacion_id, persona_id)
);

CREATE INDEX idx_soma_cap_asistente_persona ON soma_capacitacion_asistente (persona_id);
