ALTER TABLE factura_obra
    DROP CONSTRAINT ck_factura_obra_estado;

ALTER TABLE factura_obra
    ADD CONSTRAINT ck_factura_obra_estado
        CHECK (estado IN ('EMITIDA', 'PARCIAL', 'COBRADA', 'CEDIDA'));

ALTER TABLE factura_obra
    ADD COLUMN fecha_vencimiento DATE;

CREATE TABLE cobranza_obra (
    id                BIGSERIAL PRIMARY KEY,
    proyecto_id       BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    factura_id        BIGINT         NOT NULL REFERENCES factura_obra (id) ON DELETE CASCADE,
    fecha             DATE           NOT NULL,
    banco             VARCHAR(80),
    numero_operacion  VARCHAR(40),
    importe           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    detraccion        NUMERIC(14, 2) NOT NULL DEFAULT 0,
    observaciones     VARCHAR(400),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_cobranza_importe CHECK (importe >= 0),
    CONSTRAINT ck_cobranza_detraccion CHECK (detraccion >= 0),
    CONSTRAINT ck_cobranza_aplicado CHECK (importe + detraccion > 0)
);

CREATE INDEX idx_cobranza_obra_proyecto ON cobranza_obra (proyecto_id, fecha);
CREATE INDEX idx_cobranza_obra_factura ON cobranza_obra (factura_id);

CREATE TABLE liberacion_retencion (
    id                BIGSERIAL PRIMARY KEY,
    proyecto_id       BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo              VARCHAR(20)    NOT NULL,
    fecha             DATE           NOT NULL,
    importe           NUMERIC(14, 2) NOT NULL,
    entidad           VARCHAR(160),
    numero_documento  VARCHAR(40),
    observaciones     VARCHAR(400),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_liberacion_tipo CHECK (tipo IN ('EFECTIVO', 'CARTA_FIANZA')),
    CONSTRAINT ck_liberacion_importe CHECK (importe > 0)
);

CREATE INDEX idx_liberacion_retencion_proyecto ON liberacion_retencion (proyecto_id, fecha);

CREATE TABLE operacion_factoring (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    factura_id           BIGINT         NOT NULL REFERENCES factura_obra (id) ON DELETE CASCADE,
    entidad_razon        VARCHAR(160)   NOT NULL,
    entidad_ruc          VARCHAR(11),
    fecha_cesion         DATE           NOT NULL,
    tasa_descuento       NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    comision             NUMERIC(14, 2) NOT NULL DEFAULT 0,
    igv_comision         NUMERIC(14, 2) NOT NULL DEFAULT 0,
    importe_nominal      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    descuento            NUMERIC(14, 2) NOT NULL DEFAULT 0,
    importe_neto         NUMERIC(14, 2) NOT NULL DEFAULT 0,
    fecha_pago_esperada  DATE,
    fecha_adelanto       DATE,
    estado               VARCHAR(20)    NOT NULL,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_factoring_factura UNIQUE (factura_id),
    CONSTRAINT ck_factoring_estado
        CHECK (estado IN ('CEDIDA', 'ADELANTADA', 'COBRADA', 'IMPAGA', 'REVERTIDA')),
    CONSTRAINT ck_factoring_tasa CHECK (tasa_descuento >= 0 AND tasa_descuento <= 100),
    CONSTRAINT ck_factoring_comision CHECK (comision >= 0)
);

CREATE INDEX idx_operacion_factoring_proyecto ON operacion_factoring (proyecto_id, estado);
