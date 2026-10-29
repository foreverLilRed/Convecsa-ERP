CREATE TABLE proyecto (
    id            BIGSERIAL PRIMARY KEY,
    codigo        VARCHAR(20)  NOT NULL UNIQUE,
    nombre        VARCHAR(160) NOT NULL,
    ubicacion     VARCHAR(120),
    centro_costo  VARCHAR(40),
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO proyecto (codigo, nombre, ubicacion, centro_costo) VALUES
    ('0000', 'BAMBAS NEGRAS', 'LIMA', 'TODOS'),
    ('2001', 'LA PEÑA BLANCA', 'CAJAMARCA', 'ADMIN'),
    ('2002', 'VIVA INMOBILIARIA', 'LIMA', 'VENTAS'),
    ('2003', 'OBRA DEMO', 'LIMA', 'MANO DE OBRA');

CREATE TABLE cc_parametro_anual (
    id                 BIGSERIAL PRIMARY KEY,
    anio               INT            NOT NULL UNIQUE,
    uit                NUMERIC(12, 2) NOT NULL,
    horas_dia          NUMERIC(5, 2)  NOT NULL DEFAULT 8,
    jornada_lv         NUMERIC(5, 2)  NOT NULL DEFAULT 8.5,
    jornada_sabado     NUMERIC(5, 2)  NOT NULL DEFAULT 5.5,
    movilidad_dia      NUMERIC(12, 2) NOT NULL,
    movilidad_feriado  NUMERIC(12, 2) NOT NULL,
    conafovicer        NUMERIC(8, 4)  NOT NULL,
    fondo_capacitacion NUMERIC(8, 4)  NOT NULL,
    essalud            NUMERIC(8, 4)  NOT NULL,
    vacaciones         NUMERIC(8, 4)  NOT NULL,
    cts                NUMERIC(8, 4)  NOT NULL,
    he60_factor        NUMERIC(8, 4)  NOT NULL DEFAULT 1.6,
    he100_factor       NUMERIC(8, 4)  NOT NULL DEFAULT 2.0,
    altura_tramo       NUMERIC(8, 4)  NOT NULL DEFAULT 0.08,
    altitud_dia        NUMERIC(12, 2) NOT NULL,
    cota_cero_dia      NUMERIC(12, 2) NOT NULL,
    altas_temp_dia     NUMERIC(12, 2) NOT NULL,
    nocturno           NUMERIC(8, 4)  NOT NULL,
    contacto_agua      NUMERIC(8, 4)  NOT NULL,
    aguas_servidas     NUMERIC(8, 4)  NOT NULL,
    bonif_ley_29351    NUMERIC(8, 4)  NOT NULL DEFAULT 0.09,
    grati_jornales     INT            NOT NULL DEFAULT 40,
    grati_meses_fp     INT            NOT NULL DEFAULT 7,
    grati_meses_nav    INT            NOT NULL DEFAULT 5,
    escolaridad_jornales INT          NOT NULL DEFAULT 30
);

INSERT INTO cc_parametro_anual (
    anio, uit, movilidad_dia, movilidad_feriado, conafovicer, fondo_capacitacion, essalud,
    vacaciones, cts, altitud_dia, cota_cero_dia, altas_temp_dia, nocturno, contacto_agua, aguas_servidas
) VALUES (
    2026, 5500, 8.60, 8.60, 0.0200, 0.0045, 0.0900,
    0.1000, 0.1500, 2.50, 1.90, 4.50, 0.2500, 0.2000, 0.2000
);

CREATE TABLE cc_categoria_jornal (
    id            BIGSERIAL PRIMARY KEY,
    parametro_id  BIGINT         NOT NULL REFERENCES cc_parametro_anual (id) ON DELETE CASCADE,
    codigo        VARCHAR(20)    NOT NULL,
    jornal        NUMERIC(12, 2) NOT NULL,
    buc           NUMERIC(8, 4)  NOT NULL,
    UNIQUE (parametro_id, codigo)
);

INSERT INTO cc_categoria_jornal (parametro_id, codigo, jornal, buc)
SELECT id, v.codigo, v.jornal, v.buc
FROM cc_parametro_anual p
CROSS JOIN (VALUES
    ('OPERARIO', 89.30, 0.3200),
    ('OFICIAL', 69.75, 0.3000),
    ('PEON', 62.80, 0.3000)
) AS v(codigo, jornal, buc)
WHERE p.anio = 2026;

CREATE TABLE cc_especialidad_bae (
    id            BIGSERIAL PRIMARY KEY,
    parametro_id  BIGINT        NOT NULL REFERENCES cc_parametro_anual (id) ON DELETE CASCADE,
    codigo        VARCHAR(30)   NOT NULL,
    label         VARCHAR(80)   NOT NULL,
    tasa          NUMERIC(8, 4) NOT NULL,
    UNIQUE (parametro_id, codigo)
);

INSERT INTO cc_especialidad_bae (parametro_id, codigo, label, tasa)
SELECT id, v.codigo, v.label, v.tasa
FROM cc_parametro_anual p
CROSS JOIN (VALUES
    ('EQUIPO_MEDIANO', 'Op. De Equipo Mediano', 0.0800),
    ('EQUIPO_PESADO', 'Op. De Equipo Pesado', 0.1000),
    ('ELECTROMECANICO', 'Electromecánico', 0.2200),
    ('TOPOGRAFO', 'Topógrafo', 0.0900),
    ('SOLDADOR_6G', 'Soldador Homologado 6G', 0.2500)
) AS v(codigo, label, tasa)
WHERE p.anio = 2026;

CREATE TABLE cc_concepto_plame (
    codigo        VARCHAR(40) PRIMARY KEY,
    codigo_plame  VARCHAR(10) NOT NULL,
    label         VARCHAR(80) NOT NULL
);

INSERT INTO cc_concepto_plame (codigo, codigo_plame, label) VALUES
    ('JORNAL_BASICO', '121', 'Jornal básico'),
    ('DOMINICAL', '115', 'Dominical'),
    ('BUC', '311', 'BUC'),
    ('MOVILIDAD', '909', 'Movilidad'),
    ('VACACIONES_10', '114', 'Vacaciones 10%'),
    ('CTS_15', '313', 'CTS 15%'),
    ('HORAS_EXTRAS', '115', 'Horas extras');

CREATE TABLE contrato_cc (
    contrato_id         BIGINT PRIMARY KEY REFERENCES contrato (id) ON DELETE CASCADE,
    categoria           VARCHAR(20) NOT NULL,
    especialidad_bae    VARCHAR(30),
    afiliado_sindical   BOOLEAN     NOT NULL DEFAULT FALSE,
    cuota_sindical      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    hijos_escolaridad   INT         NOT NULL DEFAULT 0,
    proyecto_id         BIGINT      REFERENCES proyecto (id),
    CONSTRAINT ck_contrato_cc_categoria CHECK (categoria IN ('OPERARIO', 'OFICIAL', 'PEON')),
    CONSTRAINT ck_contrato_cc_hijos CHECK (hijos_escolaridad >= 0 AND hijos_escolaridad <= 20)
);

CREATE TABLE periodo_cc (
    id            BIGSERIAL PRIMARY KEY,
    proyecto_id   BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    fecha_desde   DATE        NOT NULL,
    fecha_hasta   DATE        NOT NULL,
    nro_semana    INT         NOT NULL,
    estado        VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at     TIMESTAMPTZ,
    CONSTRAINT uq_periodo_cc UNIQUE (proyecto_id, fecha_desde, fecha_hasta),
    CONSTRAINT ck_periodo_cc_rango CHECK (fecha_hasta >= fecha_desde),
    CONSTRAINT ck_periodo_cc_estado CHECK (estado IN ('BORRADOR', 'CERRADO'))
);

CREATE INDEX ix_periodo_cc_proyecto ON periodo_cc (proyecto_id, fecha_desde DESC);

CREATE TABLE tareo_cc_dia (
    id                   BIGSERIAL PRIMARY KEY,
    periodo_id           BIGINT        NOT NULL REFERENCES periodo_cc (id) ON DELETE CASCADE,
    persona_id           BIGINT        NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    fecha                DATE          NOT NULL,
    horas_ord            NUMERIC(5, 2) NOT NULL DEFAULT 0,
    he60                 NUMERIC(5, 2) NOT NULL DEFAULT 0,
    he100                NUMERIC(5, 2) NOT NULL DEFAULT 0,
    codigo               VARCHAR(10)   NOT NULL DEFAULT 'LAB',
    altura               BOOLEAN       NOT NULL DEFAULT FALSE,
    altitud              BOOLEAN       NOT NULL DEFAULT FALSE,
    agua                 BOOLEAN       NOT NULL DEFAULT FALSE,
    altas_temp           BOOLEAN       NOT NULL DEFAULT FALSE,
    cota_cero            BOOLEAN       NOT NULL DEFAULT FALSE,
    nocturno             BOOLEAN       NOT NULL DEFAULT FALSE,
    aguas_servidas       BOOLEAN       NOT NULL DEFAULT FALSE,
    bae                  BOOLEAN       NOT NULL DEFAULT FALSE,
    movilidad            BOOLEAN       NOT NULL DEFAULT FALSE,
    pisos                INT           NOT NULL DEFAULT 0,
    CONSTRAINT uq_tareo_cc_dia UNIQUE (periodo_id, persona_id, fecha),
    CONSTRAINT ck_tareo_cc_codigo CHECK (codigo IN ('LAB', 'F', 'DSC', 'FE', 'DM', 'LIC'))
);

CREATE INDEX ix_tareo_cc_periodo ON tareo_cc_dia (periodo_id, persona_id);

CREATE TABLE boleta_cc (
    id                       BIGSERIAL PRIMARY KEY,
    periodo_id               BIGINT         NOT NULL REFERENCES periodo_cc (id) ON DELETE CASCADE,
    persona_id               BIGINT         NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    contrato_id              BIGINT         NOT NULL REFERENCES contrato (id) ON DELETE RESTRICT,
    total_haberes            NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_descuentos         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_aportes_empleador  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_afecto             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    neto                     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at               TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_boleta_cc UNIQUE (periodo_id, persona_id)
);

CREATE TABLE boleta_cc_concepto (
    id          BIGSERIAL PRIMARY KEY,
    boleta_id   BIGINT         NOT NULL REFERENCES boleta_cc (id) ON DELETE CASCADE,
    categoria   VARCHAR(30)    NOT NULL,
    codigo      VARCHAR(40)    NOT NULL,
    label       VARCHAR(160)   NOT NULL,
    monto       NUMERIC(12, 2) NOT NULL,
    orden       INT            NOT NULL
);

CREATE INDEX ix_boleta_cc_concepto ON boleta_cc_concepto (boleta_id, orden);
