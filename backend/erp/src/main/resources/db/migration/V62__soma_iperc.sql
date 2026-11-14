CREATE TABLE soma_iperc_fila (
    id                     BIGSERIAL PRIMARY KEY,
    proyecto_id            BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    partida_id             BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    actividad_tarea        VARCHAR(240)  NOT NULL,
    puesto_trabajo         VARCHAR(120)  NOT NULL,
    rutinario              BOOLEAN       NOT NULL DEFAULT TRUE,
    tipo_peligro           VARCHAR(30)   NOT NULL,
    peligro                VARCHAR(400)  NOT NULL,
    riesgo                 VARCHAR(400)  NOT NULL,
    consecuencia           VARCHAR(400)  NOT NULL,
    requisito_legal        VARCHAR(200),
    probabilidad_inicial   INTEGER       NOT NULL,
    severidad_inicial      INTEGER       NOT NULL,
    nivel_riesgo_inicial   VARCHAR(20)   NOT NULL,
    control_eliminacion    VARCHAR(500),
    control_sustitucion    VARCHAR(500),
    control_ingenieria     VARCHAR(500),
    control_administrativo VARCHAR(500),
    control_epp            VARCHAR(500),
    probabilidad_residual  INTEGER       NOT NULL,
    severidad_residual     INTEGER       NOT NULL,
    nivel_riesgo_residual  VARCHAR(20)   NOT NULL,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_iperc_tipo_peligro CHECK (tipo_peligro IN (
        'LOCATIVO', 'MECANICO', 'ELECTRICO', 'FISICO', 'QUIMICO', 'ERGONOMICO', 'PSICOSOCIAL', 'BIOLOGICO'
    )),
    CONSTRAINT ck_soma_iperc_nivel_ini CHECK (nivel_riesgo_inicial IN ('TRIVIAL', 'TOLERABLE', 'MODERADO', 'IMPORTANTE', 'INTOLERABLE')),
    CONSTRAINT ck_soma_iperc_nivel_res CHECK (nivel_riesgo_residual IN ('TRIVIAL', 'TOLERABLE', 'MODERADO', 'IMPORTANTE', 'INTOLERABLE')),
    CONSTRAINT ck_soma_iperc_prob_ini CHECK (probabilidad_inicial BETWEEN 1 AND 5),
    CONSTRAINT ck_soma_iperc_sev_ini CHECK (severidad_inicial BETWEEN 1 AND 5),
    CONSTRAINT ck_soma_iperc_prob_res CHECK (probabilidad_residual BETWEEN 1 AND 5),
    CONSTRAINT ck_soma_iperc_sev_res CHECK (severidad_residual BETWEEN 1 AND 5)
);

CREATE INDEX idx_soma_iperc_proyecto ON soma_iperc_fila (proyecto_id, partida_id);
