INSERT INTO cc_concepto_plame (codigo, codigo_plame, label) VALUES
    ('FERIA_TRABAJADO', '0107', 'Trabajo en feriado'),
    ('DESCANSO_TRABAJADO', '0107', 'Descanso trabajado'),
    ('REM_FERIADO', '0115', 'Rem. feriado'),
    ('GRATIFICACION', '0407', 'Gratificación'),
    ('BONIF_LEY_29351', '0313', 'Bonif. Ley 29351'),
    ('ESCOLARIDAD', '0211', 'Escolaridad'),
    ('HE_60', '1001', 'Horas extras 60%'),
    ('HE_100', '1002', 'Horas extras 100%'),
    ('INDEMNIZACION_HE', '0507', 'Indemnización HE 15%'),
    ('BAE', '1003', 'BAE'),
    ('ALTURA', '1004', 'Altura'),
    ('ALTITUD', '0303', 'Altitud'),
    ('COTA_CERO', '1005', 'Cota cero'),
    ('ALTAS_TEMP', '1006', 'Altas temperaturas'),
    ('CONTACTO_AGUA', '0310', 'Contacto con agua'),
    ('AGUAS_SERVIDAS', '1007', 'Aguas servidas'),
    ('NOCTURNO', '0309', 'Nocturno'),
    ('ONP', '0607', 'ONP'),
    ('COMISION_AFP', '0601', 'Comisión AFP'),
    ('PRIMA_AFP', '0606', 'Prima AFP'),
    ('APORTE_AFP', '0608', 'Aporte AFP'),
    ('CONAFOVICER', '0602', 'CONAFOVICER'),
    ('RENTA_5TA', '0605', 'Renta 5ta'),
    ('CUOTA_SINDICAL', '0702', 'Cuota sindical'),
    ('ESSALUD', '0804', 'EsSalud'),
    ('FONDO_CAPACITACION', '0809', 'Fondo capacitación'),
    ('SCTR_SALUD', '0806', 'SCTR salud'),
    ('SCTR_PENSION', '0813', 'SCTR pensión')
ON CONFLICT (codigo) DO UPDATE SET
    codigo_plame = EXCLUDED.codigo_plame,
    label = EXCLUDED.label;

UPDATE cc_concepto_plame SET codigo_plame = '0121' WHERE codigo = 'JORNAL_BASICO';
UPDATE cc_concepto_plame SET codigo_plame = '0115' WHERE codigo = 'DOMINICAL';
UPDATE cc_concepto_plame SET codigo_plame = '0311' WHERE codigo = 'BUC';
UPDATE cc_concepto_plame SET codigo_plame = '0909' WHERE codigo = 'MOVILIDAD';
UPDATE cc_concepto_plame SET codigo_plame = '0114' WHERE codigo = 'VACACIONES_10';
UPDATE cc_concepto_plame SET codigo_plame = '0904' WHERE codigo = 'CTS_15';
UPDATE cc_concepto_plame SET codigo_plame = '0115' WHERE codigo = 'HORAS_EXTRAS';

CREATE TABLE concepto_plame_admin (
    codigo        VARCHAR(40) PRIMARY KEY,
    codigo_plame  VARCHAR(10) NOT NULL,
    label         VARCHAR(80) NOT NULL
);

INSERT INTO concepto_plame_admin (codigo, codigo_plame, label) VALUES
    ('SUELDO_BASICO', '0121', 'Sueldo básico'),
    ('ASIG_FAMILIAR', '0201', 'Asignación familiar'),
    ('APORTE_OBLIG', '0608', 'Aporte AFP'),
    ('COMISION_AFP', '0601', 'Comisión AFP'),
    ('PRIMA_SEGURO', '0606', 'Prima AFP'),
    ('APORTE_ONP', '0607', 'ONP'),
    ('ESSALUD', '0804', 'EsSalud'),
    ('SCTR_SALUD', '0806', 'SCTR salud'),
    ('SCTR_PENSION', '0813', 'SCTR pensión'),
    ('VIDA_LEY', '0803', 'Vida ley'),
    ('ESSALUD_VIDA', '0815', 'EsSalud + vida'),
    ('EXTRA', '0904', 'Concepto extra');

CREATE TABLE declaracion_mensual (
    id            BIGSERIAL PRIMARY KEY,
    anio          INT         NOT NULL,
    mes           INT         NOT NULL,
    estado        VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at     TIMESTAMPTZ,
    CONSTRAINT uq_declaracion_mensual UNIQUE (anio, mes),
    CONSTRAINT ck_declaracion_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_declaracion_estado CHECK (estado IN ('BORRADOR', 'CERRADO'))
);

CREATE TABLE consolidado_cc (
    id                       BIGSERIAL PRIMARY KEY,
    declaracion_id           BIGINT         NOT NULL REFERENCES declaracion_mensual (id) ON DELETE CASCADE,
    persona_id               BIGINT         NOT NULL REFERENCES persona (id) ON DELETE RESTRICT,
    proyecto_id              BIGINT         REFERENCES proyecto (id),
    total_haberes            NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_descuentos         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_aportes_empleador  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_afecto             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    neto                     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    horas_ord                NUMERIC(8, 2)  NOT NULL DEFAULT 0,
    he60                     NUMERIC(8, 2)  NOT NULL DEFAULT 0,
    he100                    NUMERIC(8, 2)  NOT NULL DEFAULT 0,
    dias_lab                 INT            NOT NULL DEFAULT 0,
    dias_falta               INT            NOT NULL DEFAULT 0,
    dias_dm                  INT            NOT NULL DEFAULT 0,
    dias_fe                  INT            NOT NULL DEFAULT 0,
    dias_lic                 INT            NOT NULL DEFAULT 0,
    CONSTRAINT uq_consolidado_cc UNIQUE (declaracion_id, persona_id)
);

CREATE INDEX ix_consolidado_cc_decl ON consolidado_cc (declaracion_id);

CREATE TABLE consolidado_cc_concepto (
    id              BIGSERIAL PRIMARY KEY,
    consolidado_id  BIGINT         NOT NULL REFERENCES consolidado_cc (id) ON DELETE CASCADE,
    categoria       VARCHAR(30)    NOT NULL,
    codigo          VARCHAR(40)    NOT NULL,
    label           VARCHAR(160)   NOT NULL,
    monto           NUMERIC(12, 2) NOT NULL,
    orden           INT            NOT NULL
);

CREATE INDEX ix_consolidado_cc_concepto ON consolidado_cc_concepto (consolidado_id, orden);

CREATE TABLE recibo_honorario (
    id                      BIGSERIAL PRIMARY KEY,
    declaracion_id          BIGINT         NOT NULL REFERENCES declaracion_mensual (id) ON DELETE CASCADE,
    persona_id              BIGINT         REFERENCES persona (id),
    tipo_documento          VARCHAR(10)    NOT NULL,
    numero_documento        VARCHAR(20)    NOT NULL,
    apellido_paterno        VARCHAR(80),
    apellido_materno        VARCHAR(80),
    nombres                 VARCHAR(120),
    domiciliado             BOOLEAN        NOT NULL DEFAULT TRUE,
    tipo_comprobante        VARCHAR(10)    NOT NULL DEFAULT 'R',
    serie                   VARCHAR(20)    NOT NULL,
    numero                  VARCHAR(20)    NOT NULL,
    monto                   NUMERIC(12, 2) NOT NULL,
    fecha_emision           DATE,
    fecha_pago              DATE,
    retencion_cuarta        BOOLEAN        NOT NULL DEFAULT FALSE,
    retencion_pension       BOOLEAN        NOT NULL DEFAULT FALSE,
    importe_aporte_pension  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_recibo_honorario UNIQUE (declaracion_id, tipo_documento, numero_documento, serie, numero)
);

CREATE INDEX ix_recibo_honorario_decl ON recibo_honorario (declaracion_id);
