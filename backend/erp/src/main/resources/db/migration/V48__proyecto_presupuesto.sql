CREATE TABLE presupuesto_version (
    id          BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo        VARCHAR(20) NOT NULL,
    estado      VARCHAR(20) NOT NULL,
    numero      INTEGER     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_presupuesto_version_tipo
        CHECK (tipo IN ('INICIAL', 'ACTUALIZADO', 'ADICIONAL')),
    CONSTRAINT ck_presupuesto_version_estado
        CHECK (estado IN ('BORRADOR', 'VIGENTE', 'HISTORICO')),
    CONSTRAINT uq_presupuesto_version_numero UNIQUE (proyecto_id, numero)
);

CREATE INDEX idx_presupuesto_version_proyecto_estado
    ON presupuesto_version (proyecto_id, estado);

CREATE UNIQUE INDEX uq_presupuesto_version_vigente
    ON presupuesto_version (proyecto_id)
    WHERE estado = 'VIGENTE';

CREATE TABLE partida_presupuesto (
    id              BIGSERIAL PRIMARY KEY,
    version_id      BIGINT         NOT NULL REFERENCES presupuesto_version (id) ON DELETE CASCADE,
    codigo          VARCHAR(40)    NOT NULL,
    capitulo        VARCHAR(80),
    descripcion     VARCHAR(240)   NOT NULL,
    unidad          VARCHAR(20),
    metrado         NUMERIC(14, 4) NOT NULL DEFAULT 0,
    precio_unitario NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    centro_costo    VARCHAR(40),
    orden           INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT ck_partida_presupuesto_metrado CHECK (metrado >= 0),
    CONSTRAINT ck_partida_presupuesto_pu CHECK (precio_unitario >= 0),
    CONSTRAINT ck_partida_presupuesto_total CHECK (total >= 0),
    CONSTRAINT uq_partida_presupuesto_codigo UNIQUE (version_id, codigo)
);

CREATE INDEX idx_partida_presupuesto_version ON partida_presupuesto (version_id);

CREATE TABLE apu_partida (
    id              BIGSERIAL PRIMARY KEY,
    partida_id      BIGINT         NOT NULL REFERENCES partida_presupuesto (id) ON DELETE CASCADE,
    tipo            VARCHAR(30)    NOT NULL,
    codigo          VARCHAR(40),
    descripcion     VARCHAR(240)   NOT NULL,
    unidad          VARCHAR(20),
    cantidad        NUMERIC(14, 4) NOT NULL DEFAULT 0,
    precio_unitario NUMERIC(14, 2) NOT NULL DEFAULT 0,
    parcial         NUMERIC(14, 2) NOT NULL DEFAULT 0,
    orden           INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT ck_apu_partida_tipo
        CHECK (tipo IN ('MANO_OBRA', 'MATERIAL', 'EQUIPO', 'SUBCONTRATO', 'GASTOS_GENERALES')),
    CONSTRAINT ck_apu_partida_cantidad CHECK (cantidad >= 0),
    CONSTRAINT ck_apu_partida_pu CHECK (precio_unitario >= 0),
    CONSTRAINT ck_apu_partida_parcial CHECK (parcial >= 0)
);

CREATE INDEX idx_apu_partida_partida ON apu_partida (partida_id);
