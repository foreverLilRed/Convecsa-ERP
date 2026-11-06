CREATE TABLE material (
    id           BIGSERIAL PRIMARY KEY,
    codigo       VARCHAR(40)    NOT NULL,
    descripcion  VARCHAR(240)   NOT NULL,
    unidad       VARCHAR(20),
    familia      VARCHAR(40),
    stock_minimo NUMERIC(14, 4) NOT NULL DEFAULT 0,
    critico      BOOLEAN        NOT NULL DEFAULT FALSE,
    activo       BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_material_codigo UNIQUE (codigo),
    CONSTRAINT ck_material_stock_minimo CHECK (stock_minimo >= 0)
);

CREATE TABLE almacen_obra (
    id          BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    nombre      VARCHAR(80) NOT NULL,
    CONSTRAINT uq_almacen_obra_proyecto UNIQUE (proyecto_id)
);

CREATE TABLE requerimiento_obra (
    id            BIGSERIAL PRIMARY KEY,
    proyecto_id   BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero        INTEGER     NOT NULL,
    estado        VARCHAR(20) NOT NULL,
    fecha         DATE        NOT NULL,
    observaciones VARCHAR(400),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_requerimiento_obra_estado
        CHECK (estado IN ('BORRADOR', 'APROBADO', 'ATENDIDO', 'ANULADO')),
    CONSTRAINT uq_requerimiento_obra_numero UNIQUE (proyecto_id, numero)
);

CREATE INDEX idx_requerimiento_obra_proyecto ON requerimiento_obra (proyecto_id, estado);

CREATE TABLE requerimiento_item (
    id                  BIGSERIAL PRIMARY KEY,
    requerimiento_id    BIGINT         NOT NULL REFERENCES requerimiento_obra (id) ON DELETE CASCADE,
    material_id         BIGINT         NOT NULL REFERENCES material (id),
    partida_id          BIGINT         REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    codigo_partida      VARCHAR(40),
    descripcion_partida VARCHAR(240),
    centro_costo        VARCHAR(40),
    cantidad            NUMERIC(14, 4) NOT NULL,
    cantidad_atendida   NUMERIC(14, 4) NOT NULL DEFAULT 0,
    orden               INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT ck_requerimiento_item_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_requerimiento_item_atendida CHECK (cantidad_atendida >= 0)
);

CREATE INDEX idx_requerimiento_item_req ON requerimiento_item (requerimiento_id);

CREATE TABLE orden_compra (
    id              BIGSERIAL PRIMARY KEY,
    proyecto_id     BIGINT       NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero          INTEGER      NOT NULL,
    estado          VARCHAR(20)  NOT NULL,
    proveedor_razon VARCHAR(160),
    proveedor_ruc   VARCHAR(11),
    plazo_entrega   DATE,
    observaciones   VARCHAR(400),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_orden_compra_estado
        CHECK (estado IN ('BORRADOR', 'EMITIDA', 'RECEPCIONADA', 'CERRADA')),
    CONSTRAINT uq_orden_compra_numero UNIQUE (proyecto_id, numero)
);

CREATE INDEX idx_orden_compra_proyecto ON orden_compra (proyecto_id, estado);

CREATE TABLE orden_compra_item (
    id                    BIGSERIAL PRIMARY KEY,
    orden_compra_id       BIGINT         NOT NULL REFERENCES orden_compra (id) ON DELETE CASCADE,
    material_id           BIGINT         NOT NULL REFERENCES material (id),
    requerimiento_item_id BIGINT         REFERENCES requerimiento_item (id) ON DELETE SET NULL,
    cantidad              NUMERIC(14, 4) NOT NULL,
    precio_unitario       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    cantidad_recibida     NUMERIC(14, 4) NOT NULL DEFAULT 0,
    orden                 INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT ck_orden_compra_item_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_orden_compra_item_pu CHECK (precio_unitario >= 0),
    CONSTRAINT ck_orden_compra_item_recibida CHECK (cantidad_recibida >= 0)
);

CREATE INDEX idx_orden_compra_item_oc ON orden_compra_item (orden_compra_id);

CREATE TABLE recepcion_almacen (
    id              BIGSERIAL PRIMARY KEY,
    orden_compra_id BIGINT      NOT NULL REFERENCES orden_compra (id) ON DELETE CASCADE,
    almacen_id      BIGINT      NOT NULL REFERENCES almacen_obra (id) ON DELETE CASCADE,
    numero_guia     VARCHAR(40),
    fecha           DATE        NOT NULL,
    observaciones   VARCHAR(400),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recepcion_almacen_oc ON recepcion_almacen (orden_compra_id);

CREATE TABLE recepcion_item (
    id                   BIGSERIAL PRIMARY KEY,
    recepcion_id         BIGINT         NOT NULL REFERENCES recepcion_almacen (id) ON DELETE CASCADE,
    orden_compra_item_id BIGINT         NOT NULL REFERENCES orden_compra_item (id),
    cantidad             NUMERIC(14, 4) NOT NULL,
    CONSTRAINT ck_recepcion_item_cantidad CHECK (cantidad > 0)
);

CREATE TABLE vale_salida (
    id                  BIGSERIAL PRIMARY KEY,
    almacen_id          BIGINT      NOT NULL REFERENCES almacen_obra (id) ON DELETE CASCADE,
    numero              INTEGER     NOT NULL,
    fecha               DATE        NOT NULL,
    partida_id          BIGINT      REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    codigo_partida      VARCHAR(40),
    descripcion_partida VARCHAR(240),
    centro_costo        VARCHAR(40),
    observaciones       VARCHAR(400),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_vale_salida_numero UNIQUE (almacen_id, numero)
);

CREATE INDEX idx_vale_salida_almacen ON vale_salida (almacen_id);

CREATE TABLE vale_salida_item (
    id             BIGSERIAL PRIMARY KEY,
    vale_id        BIGINT         NOT NULL REFERENCES vale_salida (id) ON DELETE CASCADE,
    material_id    BIGINT         NOT NULL REFERENCES material (id),
    cantidad       NUMERIC(14, 4) NOT NULL,
    costo_unitario NUMERIC(14, 2) NOT NULL DEFAULT 0,
    costo_total    NUMERIC(14, 2) NOT NULL DEFAULT 0,
    orden          INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT ck_vale_salida_item_cantidad CHECK (cantidad > 0)
);

CREATE TABLE movimiento_almacen (
    id              BIGSERIAL PRIMARY KEY,
    almacen_id      BIGINT         NOT NULL REFERENCES almacen_obra (id) ON DELETE CASCADE,
    material_id     BIGINT         NOT NULL REFERENCES material (id),
    tipo            VARCHAR(30)    NOT NULL,
    cantidad        NUMERIC(14, 4) NOT NULL,
    costo_unitario  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    costo_total     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    saldo_cantidad  NUMERIC(14, 4) NOT NULL,
    costo_promedio  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    partida_id      BIGINT         REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    centro_costo    VARCHAR(40),
    recepcion_id    BIGINT         REFERENCES recepcion_almacen (id) ON DELETE SET NULL,
    vale_id         BIGINT         REFERENCES vale_salida (id) ON DELETE SET NULL,
    orden_compra_id BIGINT         REFERENCES orden_compra (id) ON DELETE SET NULL,
    fecha           DATE           NOT NULL,
    observaciones   VARCHAR(400),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_movimiento_almacen_tipo
        CHECK (tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'DEVOLUCION_PROVEEDOR', 'DEVOLUCION_ALMACEN')),
    CONSTRAINT ck_movimiento_almacen_cantidad CHECK (cantidad <> 0)
);

CREATE INDEX idx_movimiento_almacen_kardex
    ON movimiento_almacen (almacen_id, material_id, id);

CREATE TABLE inventario_cierre (
    id             BIGSERIAL PRIMARY KEY,
    almacen_id     BIGINT         NOT NULL REFERENCES almacen_obra (id) ON DELETE CASCADE,
    anio           INTEGER        NOT NULL,
    mes            INTEGER        NOT NULL,
    material_id    BIGINT         NOT NULL REFERENCES material (id),
    cantidad       NUMERIC(14, 4) NOT NULL,
    costo_promedio NUMERIC(14, 2) NOT NULL,
    valor          NUMERIC(14, 2) NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_inventario_cierre_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT uq_inventario_cierre UNIQUE (almacen_id, anio, mes, material_id)
);

CREATE INDEX idx_inventario_cierre_almacen
    ON inventario_cierre (almacen_id, anio, mes);
