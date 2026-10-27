CREATE TABLE obra (
    id                          BIGSERIAL PRIMARY KEY,
    codigo                      VARCHAR(40)  NOT NULL,
    nombre                      VARCHAR(200) NOT NULL,
    cliente_nombre              VARCHAR(200) NOT NULL,
    contratista_principal       VARCHAR(200),
    ubicacion                   VARCHAR(300),
    fecha_inicio_planificada    DATE,
    fecha_fin_planificada       DATE,
    fecha_inicio_real           DATE,
    fecha_fin_real              DATE,
    monto_contratado            NUMERIC(18, 2),
    moneda                      VARCHAR(3)   NOT NULL DEFAULT 'PEN',
    responsable_nombre          VARCHAR(200),
    estado                      VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_obra_codigo UNIQUE (codigo),
    CONSTRAINT ck_obra_estado CHECK (estado IN (
        'BORRADOR', 'PLANIFICADA', 'EN_EJECUCION', 'SUSPENDIDA', 'CERRADA', 'CANCELADA'
    )),
    CONSTRAINT ck_obra_moneda CHECK (char_length(moneda) = 3),
    CONSTRAINT ck_obra_monto CHECK (monto_contratado IS NULL OR monto_contratado >= 0),
    CONSTRAINT ck_obra_fechas_planificadas CHECK (
        fecha_inicio_planificada IS NULL
        OR fecha_fin_planificada IS NULL
        OR fecha_fin_planificada >= fecha_inicio_planificada
    )
);

CREATE INDEX ix_obra_estado ON obra (estado);
CREATE INDEX ix_obra_cliente ON obra (cliente_nombre);
CREATE INDEX ix_obra_nombre ON obra (nombre);
