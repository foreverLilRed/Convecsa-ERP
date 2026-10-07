CREATE TABLE afp (
    id                  BIGSERIAL PRIMARY KEY,
    codigo              VARCHAR(40)    NOT NULL UNIQUE,
    label               VARCHAR(120)   NOT NULL,
    aporte_obligatorio  NUMERIC(5, 4)  NOT NULL,
    comision_flujo      NUMERIC(5, 4)  NOT NULL,
    prima_seguro        NUMERIC(5, 4)  NOT NULL,
    es_onp              BOOLEAN        NOT NULL DEFAULT FALSE,
    activo              BOOLEAN        NOT NULL DEFAULT TRUE
);

INSERT INTO afp (codigo, label, aporte_obligatorio, comision_flujo, prima_seguro, es_onp) VALUES
    ('HABITAT',   'AFP Habitat',   0.1000, 0.0147, 0.0137, FALSE),
    ('INTEGRA',   'AFP Integra',   0.1000, 0.0155, 0.0137, FALSE),
    ('PRIMA',     'AFP Prima',     0.1000, 0.0160, 0.0137, FALSE),
    ('PROFUTURO', 'AFP Profuturo', 0.1000, 0.0169, 0.0137, FALSE),
    ('ONP',       'ONP',           0.1300, 0.0000, 0.0000, TRUE);

ALTER TABLE contrato
    ADD COLUMN afp_id BIGINT REFERENCES afp (id),
    ADD COLUMN tiene_asignacion_familiar BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE periodo_nomina (
    id             BIGSERIAL PRIMARY KEY,
    anio           INT          NOT NULL,
    mes            INT          NOT NULL,
    estado         VARCHAR(20)  NOT NULL,
    observaciones  VARCHAR(2000),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    closed_at      TIMESTAMPTZ,
    CONSTRAINT uq_periodo_nomina_anio_mes UNIQUE (anio, mes),
    CONSTRAINT ck_periodo_nomina_mes CHECK (mes BETWEEN 1 AND 12)
);

CREATE INDEX ix_periodo_nomina_anio_mes ON periodo_nomina (anio DESC, mes DESC);

CREATE TABLE boleta_pago (
    id                       BIGSERIAL PRIMARY KEY,
    periodo_id               BIGINT         NOT NULL REFERENCES periodo_nomina (id) ON DELETE CASCADE,
    persona_id               BIGINT         NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    contrato_id              BIGINT         NOT NULL REFERENCES contrato (id) ON DELETE RESTRICT,
    afp_id                   BIGINT         NOT NULL REFERENCES afp (id) ON DELETE RESTRICT,
    dias_trabajados          INT            NOT NULL DEFAULT 30,
    aplica_asig_familiar     BOOLEAN        NOT NULL DEFAULT FALSE,
    total_haberes            NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_descuentos         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_aportes_empleador  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    neto                     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    estado                   VARCHAR(20)    NOT NULL,
    created_at               TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_boleta_pago_periodo_persona UNIQUE (periodo_id, persona_id),
    CONSTRAINT ck_boleta_pago_dias CHECK (dias_trabajados BETWEEN 0 AND 30)
);

CREATE INDEX ix_boleta_pago_periodo ON boleta_pago (periodo_id);
CREATE INDEX ix_boleta_pago_persona ON boleta_pago (persona_id);

CREATE TABLE boleta_concepto (
    id          BIGSERIAL PRIMARY KEY,
    boleta_id   BIGINT         NOT NULL REFERENCES boleta_pago (id) ON DELETE CASCADE,
    categoria   VARCHAR(30)    NOT NULL,
    codigo      VARCHAR(40)    NOT NULL,
    label       VARCHAR(160)   NOT NULL,
    monto       NUMERIC(12, 2) NOT NULL,
    orden       INT            NOT NULL,
    origen      VARCHAR(20)    NOT NULL
);

CREATE INDEX ix_boleta_concepto_boleta ON boleta_concepto (boleta_id, orden);

CREATE TABLE plantillas_boleta (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(40)  NOT NULL UNIQUE,
    titulo      VARCHAR(200) NOT NULL,
    plantilla   TEXT         NOT NULL DEFAULT '',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO plantillas_boleta (codigo, titulo, plantilla)
VALUES ('BOLETA-01', 'Boleta de pago REMYPE', '');
