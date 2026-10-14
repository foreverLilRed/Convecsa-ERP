CREATE TABLE afp_tasa (
    id                          BIGSERIAL PRIMARY KEY,
    afp_id                      BIGINT         NOT NULL REFERENCES afp (id) ON DELETE RESTRICT,
    tipo_comision               VARCHAR(20)    NOT NULL,
    aporte_obligatorio          NUMERIC(7, 6)  NOT NULL,
    comision                    NUMERIC(7, 6)  NOT NULL DEFAULT 0,
    prima_seguro                NUMERIC(7, 6)  NOT NULL DEFAULT 0,
    tope_remuneracion_asegurable NUMERIC(12, 2),
    activo                      BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_afp_tasa UNIQUE (afp_id, tipo_comision),
    CONSTRAINT ck_afp_tasa_tipo_comision CHECK (tipo_comision IN ('FLUJO', 'MIXTA', 'NO_APLICA'))
);

INSERT INTO afp_tasa (afp_id, tipo_comision, aporte_obligatorio, comision, prima_seguro, tope_remuneracion_asegurable)
SELECT id, 'FLUJO', 0.100000, 0.014700, 0.013700, 12209.11 FROM afp WHERE codigo = 'HABITAT'
UNION ALL SELECT id, 'MIXTA', 0.100000, 0.000000, 0.013700, 12209.11 FROM afp WHERE codigo = 'HABITAT'
UNION ALL SELECT id, 'FLUJO', 0.100000, 0.015500, 0.013700, 12209.11 FROM afp WHERE codigo = 'INTEGRA'
UNION ALL SELECT id, 'MIXTA', 0.100000, 0.000000, 0.013700, 12209.11 FROM afp WHERE codigo = 'INTEGRA'
UNION ALL SELECT id, 'FLUJO', 0.100000, 0.016000, 0.013700, 12209.11 FROM afp WHERE codigo = 'PRIMA'
UNION ALL SELECT id, 'MIXTA', 0.100000, 0.000000, 0.013700, 12209.11 FROM afp WHERE codigo = 'PRIMA'
UNION ALL SELECT id, 'FLUJO', 0.100000, 0.016900, 0.013700, 12209.11 FROM afp WHERE codigo = 'PROFUTURO'
UNION ALL SELECT id, 'MIXTA', 0.100000, 0.000000, 0.013700, 12209.11 FROM afp WHERE codigo = 'PROFUTURO'
UNION ALL SELECT id, 'NO_APLICA', 0.130000, 0.000000, 0.000000, NULL FROM afp WHERE codigo = 'ONP';

CREATE TABLE renta_quinta_categoria (
    id              BIGSERIAL PRIMARY KEY,
    orden           SMALLINT       NOT NULL UNIQUE,
    desde_uit       NUMERIC(6, 2) NOT NULL,
    hasta_uit       NUMERIC(6, 2),
    tasa            NUMERIC(7, 6) NOT NULL,
    vigente_desde   DATE           NOT NULL,
    vigente_hasta   DATE,
    CONSTRAINT ck_renta_quinta_tramo CHECK (hasta_uit IS NULL OR hasta_uit > desde_uit)
);

CREATE TABLE renta_quinta_configuracion (
    anio            INT            PRIMARY KEY,
    valor_uit       NUMERIC(12, 2) NOT NULL,
    deduccion_uit   NUMERIC(6, 2)  NOT NULL,
    CONSTRAINT ck_renta_quinta_valor_uit CHECK (valor_uit > 0),
    CONSTRAINT ck_renta_quinta_deduccion CHECK (deduccion_uit >= 0)
);

INSERT INTO renta_quinta_configuracion (anio, valor_uit, deduccion_uit)
VALUES (2026, 5500.00, 7.00);

INSERT INTO renta_quinta_categoria (orden, desde_uit, hasta_uit, tasa, vigente_desde) VALUES
    (1, 0, 5, 0.080000, DATE '2026-01-01'),
    (2, 5, 20, 0.140000, DATE '2026-01-01'),
    (3, 20, 35, 0.170000, DATE '2026-01-01'),
    (4, 35, 45, 0.200000, DATE '2026-01-01'),
    (5, 45, NULL, 0.300000, DATE '2026-01-01');

CREATE TABLE catalogo_maestro (
    id              BIGSERIAL PRIMARY KEY,
    categoria       VARCHAR(40)  NOT NULL,
    codigo          VARCHAR(40)  NOT NULL,
    etiqueta        VARCHAR(180) NOT NULL,
    orden           SMALLINT     NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_catalogo_maestro_categoria_codigo UNIQUE (categoria, codigo)
);

INSERT INTO catalogo_maestro (categoria, codigo, etiqueta, orden) VALUES
    ('SISTEMA_PENSION', 'AFP_HABITAT_FLUJO', 'AFP HABITAT FLUJO', 1),
    ('SISTEMA_PENSION', 'AFP_HABITAT_MIXTA', 'AFP HABITAT MIXTA', 2),
    ('SISTEMA_PENSION', 'AFP_INTEGRA_FLUJO', 'AFP INTEGRA FLUJO', 3),
    ('SISTEMA_PENSION', 'AFP_INTEGRA_MIXTA', 'AFP INTEGRA MIXTA', 4),
    ('SISTEMA_PENSION', 'AFP_PRIMA_FLUJO', 'AFP PRIMA FLUJO', 5),
    ('SISTEMA_PENSION', 'AFP_PRIMA_MIXTA', 'AFP PRIMA MIXTA', 6),
    ('SISTEMA_PENSION', 'AFP_PROFUTURO_FLUJO', 'AFP PROFUTURO FLUJO', 7),
    ('SISTEMA_PENSION', 'AFP_PROFUTURO_MIXTA', 'AFP PROFUTURO MIXTA', 8),
    ('SISTEMA_PENSION', 'ONP', 'ONP', 9),
    ('SISTEMA_PENSION', 'JUBILADO', 'JUBILADO', 10),
    ('SISTEMA_PENSION', 'POR_AFILIAR', 'POR AFILIAR', 11),
    ('REGIMEN_LABORAL', 'REGIMEN_GENERAL', 'REGIMEN GENERAL', 1),
    ('REGIMEN_LABORAL', 'PEQUENA_EMPRESA', 'PEQUEÑA EMPRESA', 2),
    ('REGIMEN_LABORAL', 'MICROEMPRESA', 'MICROEMPRESA', 3),
    ('TIPO_DOCUMENTO', '01', 'DOCUMENTO NACIONAL DE IDENTIDAD', 1),
    ('TIPO_DOCUMENTO', '04', 'CARNÉ DE EXTRANJERÍA', 2),
    ('TIPO_DOCUMENTO', '07', 'PASAPORTE', 3),
    ('TIPO_DOCUMENTO', '09', 'CARNÉ DE SOLICITANTE DE REFUGIO', 4),
    ('TIPO_DOCUMENTO', '11', 'PARTIDA DE NACIMIENTO', 5),
    ('TIPO_DOCUMENTO', '22', 'CARNÉ DE IDENTIDAD - RELACIONES EXTERIORES', 6),
    ('TIPO_DOCUMENTO', '23', 'CARNÉ DE PERMISO TEMPORAL DE PERMANENCIA', 7),
    ('TIPO_DOCUMENTO', '24', 'DOCUMENTO DE IDENTIDAD EXTRANJERO', 8),
    ('TIPO_CONTRATO', 'INDETERMINADO', 'Contrato indeterminado', 1),
    ('TIPO_CONTRATO', 'TEMPORAL', 'Contrato de naturaleza temporal', 2),
    ('TIPO_CONTRATO', 'INCREMENTO_ACTIVIDAD', 'Contrato por incremento de actividad', 3),
    ('TIPO_CONTRATO', 'OBRA_DETERMINADA', 'Contrato por obra determinada', 4),
    ('TIPO_CONTRATO', 'TEMPORADA', 'Contrato por temporada', 5),
    ('TIPO_CONTRATO', 'INTERMITENTE', 'Contrato intermitente', 6),
    ('TIPO_CONTRATO', 'PLAZO_FIJO', 'Contrato Plazo Fijo', 7),
    ('TIPO_CONTRATO', 'PLAZO_INDETERMINADO', 'Plazo Indeterminado', 8),
    ('SEGURO', 'ESSALUD', 'ESSALUD', 1),
    ('SEGURO', 'EPS', 'EPS', 2),
    ('SEGURO', 'SIS', 'SIS', 3),
    ('ESSALUD_VIDA', 'TRABAJADOR', 'TRABAJADOR', 1),
    ('ESSALUD_VIDA', 'EMPLEADOR', 'EMPLEADOR', 2),
    ('ESSALUD_VIDA', 'AMBOS', 'AMBOS', 3),
    ('SITUACION_LABORAL', 'ACTIVO_SUBSIDIADO', 'ACTIVO O SUBSIDIADO', 1),
    ('SITUACION_LABORAL', 'BAJA', 'BAJA', 2),
    ('CENTRO_COSTO', '94', 'ADMIN', 1),
    ('CENTRO_COSTO', '95', 'VENTAS', 2),
    ('CENTRO_COSTO', '92', 'PRODUCCION', 3);
