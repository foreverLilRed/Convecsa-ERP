ALTER TABLE material
    ADD COLUMN es_epp BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN tipo_epp VARCHAR(40),
    ADD COLUMN vida_util_dias INTEGER,
    ADD COLUMN norma_certificacion VARCHAR(80),
    ADD CONSTRAINT ck_material_vida_util CHECK (vida_util_dias IS NULL OR vida_util_dias > 0),
    ADD CONSTRAINT ck_material_tipo_epp CHECK (tipo_epp IS NULL OR tipo_epp IN (
        'CABEZA', 'VISUAL', 'AUDITIVO', 'RESPIRATORIO', 'MANOS', 'PIES',
        'CAIDAS_ALTURA', 'CUERPO_ROPA', 'COLECTIVO', 'EMERGENCIA'
    ));

CREATE TABLE soma_entrega_epp (
    id                 BIGSERIAL PRIMARY KEY,
    proyecto_id        BIGINT       NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    persona_id         BIGINT       NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    vale_salida_id     BIGINT       REFERENCES vale_salida (id) ON DELETE SET NULL,
    fecha_entrega      DATE         NOT NULL,
    motivo             VARCHAR(30)  NOT NULL,
    firmado_trabajador BOOLEAN      NOT NULL DEFAULT FALSE,
    entregado_por      VARCHAR(80)  NOT NULL,
    observaciones      VARCHAR(400),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_entrega_epp_motivo CHECK (motivo IN (
        'DOTACION_INICIAL', 'RENOVACION_DESGASTE', 'REPOSICION_PERDIDA', 'TRABAJO_ESPECIAL'
    ))
);

CREATE INDEX idx_soma_entrega_epp_obra_persona ON soma_entrega_epp (proyecto_id, persona_id, fecha_entrega DESC);

CREATE TABLE soma_entrega_epp_item (
    id                         BIGSERIAL PRIMARY KEY,
    entrega_id                 BIGINT         NOT NULL REFERENCES soma_entrega_epp (id) ON DELETE CASCADE,
    material_id                BIGINT         NOT NULL REFERENCES material (id),
    cantidad                   NUMERIC(14, 4) NOT NULL,
    talla                      VARCHAR(20),
    fecha_vencimiento_estimada DATE,
    estado_item                VARCHAR(20)    NOT NULL DEFAULT 'VIGENTE',
    CONSTRAINT ck_soma_epp_item_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_soma_epp_item_estado CHECK (estado_item IN (
        'VIGENTE', 'DEVUELTO', 'DETERIORADO', 'EXTRAVIADO'
    ))
);
