CREATE TABLE evaluacion_desempeno (
    id               BIGSERIAL PRIMARY KEY,
    persona_id       BIGINT         NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    periodo          VARCHAR(16)    NOT NULL,
    seguridad_sst    SMALLINT       NOT NULL,
    productividad    SMALLINT       NOT NULL,
    calidad_tecnica  SMALLINT       NOT NULL,
    puntualidad      SMALLINT       NOT NULL,
    trabajo_equipo   SMALLINT       NOT NULL,
    meta_cumplida    SMALLINT       NOT NULL,
    comentarios      VARCHAR(4000),
    estado           VARCHAR(20)    NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_evaluacion_desempeno_persona_periodo UNIQUE (persona_id, periodo),
    CONSTRAINT ck_eval_seguridad CHECK (seguridad_sst BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_productividad CHECK (productividad BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_calidad CHECK (calidad_tecnica BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_puntualidad CHECK (puntualidad BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_equipo CHECK (trabajo_equipo BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_meta CHECK (meta_cumplida BETWEEN 0 AND 100),
    CONSTRAINT ck_eval_estado CHECK (estado IN ('PENDIENTE', 'COMPLETADA'))
);

CREATE INDEX ix_evaluacion_desempeno_periodo ON evaluacion_desempeno (periodo);
CREATE INDEX ix_evaluacion_desempeno_persona ON evaluacion_desempeno (persona_id);
