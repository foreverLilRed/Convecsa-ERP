CREATE TABLE soma_evento_sst (
    id                     BIGSERIAL PRIMARY KEY,
    proyecto_id            BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero                 INTEGER       NOT NULL,
    tipo_evento            VARCHAR(40)   NOT NULL,
    gravedad_incapacidad   VARCHAR(30),
    fecha_hora             TIMESTAMPTZ   NOT NULL,
    lugar_exacto           VARCHAR(200)  NOT NULL,
    partida_id             BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    persona_afectada_id    BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    subcontrato_id         BIGINT        REFERENCES subcontrato_obra (id) ON DELETE SET NULL,
    descripcion_evento     VARCHAR(2000) NOT NULL,
    parte_cuerpo_lesionada VARCHAR(120),
    naturaleza_lesion      VARCHAR(120),
    agente_causante        VARCHAR(160),
    numero_certificado_dm  VARCHAR(60),
    dias_descanso_medico   INTEGER       NOT NULL DEFAULT 0,
    dias_cargo_estadistico INTEGER       NOT NULL DEFAULT 0,
    fecha_inicio_dm        DATE,
    fecha_fin_dm           DATE,
    causas_inmediatas_json JSONB         NOT NULL DEFAULT '{}'::jsonb,
    causas_basicas_json    JSONB         NOT NULL DEFAULT '{}'::jsonb,
    notificado_mtpe        BOOLEAN       NOT NULL DEFAULT FALSE,
    estado_investigacion   VARCHAR(20)   NOT NULL DEFAULT 'EN_INVESTIGACION',
    informe_doc_id         BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_soma_evento_numero UNIQUE (proyecto_id, numero),
    CONSTRAINT ck_soma_evento_tipo CHECK (tipo_evento IN (
        'INCIDENTE', 'INCIDENTE_PELIGROSO', 'ACCIDENTE_LEVE',
        'ACCIDENTE_INCAPACITANTE', 'ACCIDENTE_MORTAL', 'ENFERMEDAD_OCUPACIONAL'
    )),
    CONSTRAINT ck_soma_evento_gravedad CHECK (
        (tipo_evento = 'ACCIDENTE_INCAPACITANTE' AND gravedad_incapacidad IN ('TOTAL_TEMPORAL', 'PARCIAL_PERMANENTE', 'TOTAL_PERMANENTE'))
        OR (tipo_evento <> 'ACCIDENTE_INCAPACITANTE' AND gravedad_incapacidad IS NULL)
    ),
    CONSTRAINT ck_soma_evento_estado CHECK (estado_investigacion IN ('EN_INVESTIGACION', 'CERRADO')),
    CONSTRAINT ck_soma_evento_dias CHECK (dias_descanso_medico >= 0 AND dias_cargo_estadistico >= 0),
    CONSTRAINT ck_soma_evento_dm CHECK (fecha_fin_dm IS NULL OR fecha_inicio_dm IS NULL OR fecha_fin_dm >= fecha_inicio_dm)
);

CREATE INDEX idx_soma_evento_obra_fecha ON soma_evento_sst (proyecto_id, fecha_hora DESC);

CREATE TABLE soma_evento_hallazgo (
    evento_id   BIGINT NOT NULL REFERENCES soma_evento_sst (id) ON DELETE CASCADE,
    hallazgo_id BIGINT NOT NULL REFERENCES soma_hallazgo (id) ON DELETE CASCADE,
    PRIMARY KEY (evento_id, hallazgo_id)
);
