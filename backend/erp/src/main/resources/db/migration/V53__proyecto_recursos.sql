CREATE TABLE asignacion_equipo (
    id                BIGSERIAL PRIMARY KEY,
    proyecto_id       BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    codigo            VARCHAR(40)    NOT NULL,
    descripcion       VARCHAR(240)   NOT NULL,
    origen            VARCHAR(20)    NOT NULL,
    proveedor_razon   VARCHAR(160),
    proveedor_ruc     VARCHAR(11),
    fecha_ingreso     DATE           NOT NULL,
    fecha_salida      DATE,
    modo_tarifa       VARCHAR(10)    NOT NULL,
    tarifa            NUMERIC(14, 2) NOT NULL,
    partida_id        BIGINT         REFERENCES partida_presupuesto (id),
    centro_costo      VARCHAR(40),
    observaciones     VARCHAR(400),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_asignacion_equipo_origen CHECK (origen IN ('PROPIO', 'ALQUILADO')),
    CONSTRAINT ck_asignacion_equipo_modo CHECK (modo_tarifa IN ('HORA', 'DIA')),
    CONSTRAINT ck_asignacion_equipo_tarifa CHECK (tarifa >= 0),
    CONSTRAINT ck_asignacion_equipo_fechas CHECK (fecha_salida IS NULL OR fecha_salida >= fecha_ingreso)
);

CREATE INDEX idx_asignacion_equipo_proyecto ON asignacion_equipo (proyecto_id, fecha_ingreso);

CREATE TABLE imputacion_equipo (
    id                BIGSERIAL PRIMARY KEY,
    asignacion_id     BIGINT         NOT NULL REFERENCES asignacion_equipo (id) ON DELETE CASCADE,
    proyecto_id       BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    fecha             DATE           NOT NULL,
    cantidad          NUMERIC(14, 4) NOT NULL,
    tarifa            NUMERIC(14, 2) NOT NULL,
    costo_total       NUMERIC(14, 2) NOT NULL,
    partida_id        BIGINT         REFERENCES partida_presupuesto (id),
    centro_costo      VARCHAR(40),
    observaciones     VARCHAR(400),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_imputacion_equipo_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_imputacion_equipo_tarifa CHECK (tarifa >= 0),
    CONSTRAINT ck_imputacion_equipo_costo CHECK (costo_total >= 0)
);

CREATE INDEX idx_imputacion_equipo_proyecto ON imputacion_equipo (proyecto_id, fecha);
CREATE INDEX idx_imputacion_equipo_asignacion ON imputacion_equipo (asignacion_id, fecha);

CREATE TABLE subcontrato_obra (
    id                     BIGSERIAL PRIMARY KEY,
    proyecto_id            BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    especialidad           VARCHAR(160)   NOT NULL,
    subcontratista_razon   VARCHAR(160)   NOT NULL,
    subcontratista_ruc     VARCHAR(11),
    monto_contratado       NUMERIC(14, 2) NOT NULL,
    fecha_inicio           DATE,
    fecha_fin              DATE,
    partida_id             BIGINT         REFERENCES partida_presupuesto (id),
    centro_costo           VARCHAR(40),
    observaciones          VARCHAR(400),
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_subcontrato_monto CHECK (monto_contratado > 0),
    CONSTRAINT ck_subcontrato_fechas CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX idx_subcontrato_obra_proyecto ON subcontrato_obra (proyecto_id);

CREATE TABLE subcontrato_avance (
    id                BIGSERIAL PRIMARY KEY,
    subcontrato_id    BIGINT         NOT NULL REFERENCES subcontrato_obra (id) ON DELETE CASCADE,
    proyecto_id       BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    fecha             DATE           NOT NULL,
    monto             NUMERIC(14, 2) NOT NULL,
    partida_id        BIGINT         REFERENCES partida_presupuesto (id),
    centro_costo      VARCHAR(40),
    observaciones     VARCHAR(400),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_subcontrato_avance_monto CHECK (monto > 0)
);

CREATE INDEX idx_subcontrato_avance_proyecto ON subcontrato_avance (proyecto_id, fecha);
CREATE INDEX idx_subcontrato_avance_subcontrato ON subcontrato_avance (subcontrato_id, fecha);
