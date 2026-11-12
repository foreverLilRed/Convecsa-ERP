CREATE TABLE soma_sctr_poliza (
    id                    BIGSERIAL PRIMARY KEY,
    proyecto_id           BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    anio                  INTEGER        NOT NULL,
    mes                   INTEGER        NOT NULL,
    aseguradora_salud     VARCHAR(120)   NOT NULL,
    numero_poliza_salud   VARCHAR(60)    NOT NULL,
    aseguradora_pension   VARCHAR(120)   NOT NULL,
    numero_poliza_pension VARCHAR(60)    NOT NULL,
    fecha_inicio          DATE           NOT NULL,
    fecha_fin             DATE           NOT NULL,
    monto_prima_salud     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    monto_prima_pension   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    documento_obra_id     BIGINT         REFERENCES documento_obra (id) ON DELETE SET NULL,
    estado                VARCHAR(20)    NOT NULL DEFAULT 'VIGENTE',
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_sctr_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_soma_sctr_estado CHECK (estado IN ('VIGENTE', 'ANULADA')),
    CONSTRAINT ck_soma_sctr_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT ck_soma_sctr_primas CHECK (monto_prima_salud >= 0 AND monto_prima_pension >= 0),
    CONSTRAINT uq_soma_sctr_obra_mes UNIQUE (proyecto_id, anio, mes)
);

CREATE TABLE soma_sctr_nomina (
    id               BIGSERIAL PRIMARY KEY,
    poliza_id        BIGINT      NOT NULL REFERENCES soma_sctr_poliza (id) ON DELETE CASCADE,
    persona_id       BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    incluido_salud   BOOLEAN     NOT NULL DEFAULT TRUE,
    incluido_pension BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_soma_sctr_nomina_persona UNIQUE (poliza_id, persona_id)
);

CREATE INDEX idx_soma_sctr_nomina_persona ON soma_sctr_nomina (persona_id);
