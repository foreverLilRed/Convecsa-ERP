ALTER TABLE proyecto
    ADD COLUMN porcentaje_retencion NUMERIC(5, 2) NOT NULL DEFAULT 0;

ALTER TABLE proyecto
    ADD CONSTRAINT ck_proyecto_retencion
        CHECK (porcentaje_retencion >= 0 AND porcentaje_retencion <= 100);

CREATE TABLE cronograma_partida (
    id           BIGSERIAL PRIMARY KEY,
    proyecto_id  BIGINT       NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    partida_id   BIGINT       NOT NULL REFERENCES partida_presupuesto (id) ON DELETE CASCADE,
    codigo       VARCHAR(40)  NOT NULL,
    descripcion  VARCHAR(240) NOT NULL,
    fecha_inicio DATE,
    fecha_fin    DATE,
    CONSTRAINT uq_cronograma_partida UNIQUE (proyecto_id, partida_id),
    CONSTRAINT ck_cronograma_fechas
        CHECK (fecha_inicio IS NULL OR fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX idx_cronograma_partida_proyecto ON cronograma_partida (proyecto_id);

CREATE TABLE avance_obra (
    id         BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    anio        INTEGER     NOT NULL,
    mes         INTEGER     NOT NULL,
    estado      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at   TIMESTAMPTZ,
    CONSTRAINT ck_avance_obra_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_avance_obra_estado CHECK (estado IN ('BORRADOR', 'CERRADO')),
    CONSTRAINT uq_avance_obra_mes UNIQUE (proyecto_id, anio, mes)
);

CREATE INDEX idx_avance_obra_proyecto ON avance_obra (proyecto_id, anio, mes);

CREATE TABLE avance_partida (
    id                 BIGSERIAL PRIMARY KEY,
    avance_id          BIGINT         NOT NULL REFERENCES avance_obra (id) ON DELETE CASCADE,
    partida_id         BIGINT         REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    codigo             VARCHAR(40)    NOT NULL,
    descripcion        VARCHAR(240)   NOT NULL,
    unidad             VARCHAR(20),
    centro_costo       VARCHAR(40),
    metrado_meta       NUMERIC(14, 4) NOT NULL DEFAULT 0,
    metrado_periodo    NUMERIC(14, 4) NOT NULL DEFAULT 0,
    metrado_acumulado  NUMERIC(14, 4) NOT NULL DEFAULT 0,
    orden              INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT ck_avance_partida_periodo CHECK (metrado_periodo >= 0)
);

CREATE INDEX idx_avance_partida_avance ON avance_partida (avance_id);

CREATE TABLE adicional_obra (
    id              BIGSERIAL PRIMARY KEY,
    proyecto_id     BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo            VARCHAR(20)    NOT NULL,
    estado          VARCHAR(20)    NOT NULL,
    partida_id      BIGINT         REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    codigo          VARCHAR(40)    NOT NULL,
    descripcion     VARCHAR(240)   NOT NULL,
    unidad          VARCHAR(20),
    metrado         NUMERIC(14, 4) NOT NULL DEFAULT 0,
    precio_unitario NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_adicional_tipo CHECK (tipo IN ('ADICIONAL', 'DEDUCTIVO')),
    CONSTRAINT ck_adicional_estado CHECK (estado IN ('BORRADOR', 'VIGENTE', 'ANULADO')),
    CONSTRAINT ck_adicional_metrado CHECK (metrado >= 0)
);

CREATE INDEX idx_adicional_obra_proyecto ON adicional_obra (proyecto_id, estado);

CREATE TABLE valorizacion (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero               INTEGER        NOT NULL,
    tipo                 VARCHAR(20)    NOT NULL,
    estado               VARCHAR(20)    NOT NULL,
    anio                 INTEGER        NOT NULL,
    mes                  INTEGER        NOT NULL,
    porcentaje_retencion NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    monto_bruto          NUMERIC(14, 2) NOT NULL DEFAULT 0,
    monto_retencion      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    monto_neto           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    observaciones        VARCHAR(400),
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_valorizacion_tipo CHECK (tipo IN ('REGULAR', 'ADICIONAL', 'DEDUCTIVO')),
    CONSTRAINT ck_valorizacion_estado
        CHECK (estado IN ('BORRADOR', 'PRESENTADA', 'OBSERVADA', 'APROBADA')),
    CONSTRAINT ck_valorizacion_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT uq_valorizacion_numero UNIQUE (proyecto_id, numero)
);

CREATE UNIQUE INDEX uq_valorizacion_regular_mes
    ON valorizacion (proyecto_id, anio, mes)
    WHERE tipo = 'REGULAR';

CREATE INDEX idx_valorizacion_proyecto ON valorizacion (proyecto_id, estado);

CREATE TABLE valorizacion_item (
    id                BIGSERIAL PRIMARY KEY,
    valorizacion_id   BIGINT         NOT NULL REFERENCES valorizacion (id) ON DELETE CASCADE,
    partida_id        BIGINT         REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    adicional_id      BIGINT         REFERENCES adicional_obra (id) ON DELETE SET NULL,
    codigo            VARCHAR(40)    NOT NULL,
    descripcion       VARCHAR(240)   NOT NULL,
    unidad            VARCHAR(20),
    centro_costo      VARCHAR(40),
    metrado_anterior  NUMERIC(14, 4) NOT NULL DEFAULT 0,
    metrado_periodo   NUMERIC(14, 4) NOT NULL DEFAULT 0,
    metrado_acumulado NUMERIC(14, 4) NOT NULL DEFAULT 0,
    precio_unitario   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_periodo     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_acumulado   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    orden             INTEGER        NOT NULL DEFAULT 0
);

CREATE INDEX idx_valorizacion_item_val ON valorizacion_item (valorizacion_id);

CREATE TABLE factura_obra (
    id              BIGSERIAL PRIMARY KEY,
    proyecto_id     BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    valorizacion_id BIGINT         NOT NULL REFERENCES valorizacion (id) ON DELETE CASCADE,
    numero          INTEGER        NOT NULL,
    fecha           DATE           NOT NULL,
    cliente_razon   VARCHAR(160),
    cliente_ruc     VARCHAR(11),
    subtotal        NUMERIC(14, 2) NOT NULL DEFAULT 0,
    igv             NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    estado          VARCHAR(20)    NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_factura_obra_valorizacion UNIQUE (valorizacion_id),
    CONSTRAINT uq_factura_obra_numero UNIQUE (proyecto_id, numero),
    CONSTRAINT ck_factura_obra_estado CHECK (estado IN ('EMITIDA'))
);
