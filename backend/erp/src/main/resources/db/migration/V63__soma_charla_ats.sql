CREATE TABLE soma_charla_ats (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    cuadrilla_id         BIGINT        REFERENCES cuadrilla_obra (id) ON DELETE SET NULL,
    partida_id           BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    fecha                DATE          NOT NULL,
    hora_inicio          TIME          NOT NULL,
    duracion_minutos     INTEGER       NOT NULL DEFAULT 10,
    tema_charla          VARCHAR(240)  NOT NULL,
    expositor_persona_id BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    supervisor_ssoma_id  BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    pasos_ats_json       JSONB         NOT NULL DEFAULT '[]'::jsonb,
    epp_verificado       BOOLEAN       NOT NULL DEFAULT FALSE,
    herramientas_verif   BOOLEAN       NOT NULL DEFAULT FALSE,
    observaciones        VARCHAR(1000),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_charla_duracion CHECK (duracion_minutos > 0)
);

CREATE INDEX idx_soma_charla_ats_obra_fecha ON soma_charla_ats (proyecto_id, fecha DESC);

CREATE TABLE soma_charla_asistente (
    id              BIGSERIAL PRIMARY KEY,
    charla_id       BIGINT       NOT NULL REFERENCES soma_charla_ats (id) ON DELETE CASCADE,
    persona_id      BIGINT       NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    habilitado_soma BOOLEAN      NOT NULL,
    observacion     VARCHAR(240),
    CONSTRAINT uq_soma_charla_asistente UNIQUE (charla_id, persona_id)
);
