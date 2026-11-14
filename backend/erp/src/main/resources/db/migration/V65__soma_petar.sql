CREATE TABLE soma_petar (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero               INTEGER       NOT NULL,
    tipo_trabajo         VARCHAR(30)   NOT NULL,
    partida_id           BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    cuadrilla_id         BIGINT        REFERENCES cuadrilla_obra (id) ON DELETE SET NULL,
    fecha                DATE          NOT NULL,
    hora_inicio          TIME          NOT NULL,
    hora_fin             TIME          NOT NULL,
    ubicacion_frente     VARCHAR(200)  NOT NULL,
    descripcion_trabajo  VARCHAR(1000) NOT NULL,
    checklist_json       JSONB         NOT NULL DEFAULT '{}'::jsonb,
    mediciones_json      JSONB         NOT NULL DEFAULT '{}'::jsonb,
    residente_persona_id BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    supervisor_ssoma_id  BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'BORRADOR',
    autorizado_por       VARCHAR(80),
    autorizado_at        TIMESTAMPTZ,
    documento_obra_id    BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_soma_petar_numero UNIQUE (proyecto_id, numero),
    CONSTRAINT ck_soma_petar_tipo CHECK (tipo_trabajo IN (
        'ALTURA', 'EXCAVACION_ZANJA', 'TRABAJO_CALIENTE', 'ESPACIO_CONFINADO', 'IZAJE_CARGAS', 'ENERGIA_ELECTRICA'
    )),
    CONSTRAINT ck_soma_petar_estado CHECK (estado IN ('BORRADOR', 'AUTORIZADO', 'CERRADO', 'SUSPENDIDO')),
    CONSTRAINT ck_soma_petar_horas CHECK (hora_fin > hora_inicio)
);

CREATE INDEX idx_soma_petar_obra_fecha ON soma_petar (proyecto_id, fecha DESC, tipo_trabajo);

CREATE TABLE soma_petar_trabajador (
    id           BIGSERIAL PRIMARY KEY,
    petar_id     BIGINT      NOT NULL REFERENCES soma_petar (id) ON DELETE CASCADE,
    persona_id   BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    rol_en_tarea VARCHAR(60) NOT NULL DEFAULT 'EJECUTANTE',
    CONSTRAINT uq_soma_petar_trabajador UNIQUE (petar_id, persona_id)
);
