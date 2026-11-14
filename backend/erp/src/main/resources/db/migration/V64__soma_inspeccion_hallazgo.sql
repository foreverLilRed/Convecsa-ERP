CREATE TABLE soma_inspeccion (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo                 VARCHAR(40)   NOT NULL,
    fecha                DATE          NOT NULL,
    area_frente          VARCHAR(160)  NOT NULL,
    subcontrato_id       BIGINT        REFERENCES subcontrato_obra (id) ON DELETE SET NULL,
    inspector_persona_id BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    puntaje_cumplimiento NUMERIC(5, 2),
    observaciones        VARCHAR(1000),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_inspeccion_tipo CHECK (tipo IN (
        'PLANEADA_MENSUAL', 'INOPINADA_DIARIA', 'EPP', 'EXTINTORES_EMERGENCIA',
        'INSTALACIONES_ELECTRICAS_G050', 'ANDAMIOS_ESCALERAS', 'ORDEN_LIMPIEZA',
        'ALMACEN_MATPEL', 'SUBCONTRATISTA'
    )),
    CONSTRAINT ck_soma_inspeccion_puntaje CHECK (puntaje_cumplimiento IS NULL OR puntaje_cumplimiento BETWEEN 0 AND 100),
    CONSTRAINT ck_soma_inspeccion_subcontrato CHECK (tipo <> 'SUBCONTRATISTA' OR subcontrato_id IS NOT NULL)
);

CREATE INDEX idx_soma_inspeccion_obra ON soma_inspeccion (proyecto_id, fecha DESC);

CREATE TABLE soma_hallazgo (
    id                       BIGSERIAL PRIMARY KEY,
    proyecto_id              BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    inspeccion_id            BIGINT        REFERENCES soma_inspeccion (id) ON DELETE SET NULL,
    clasificacion            VARCHAR(30)   NOT NULL,
    potencial_perdida        VARCHAR(20)   NOT NULL,
    descripcion              VARCHAR(1000) NOT NULL,
    ubicacion                VARCHAR(200),
    accion_correctiva        VARCHAR(1000) NOT NULL,
    responsable_persona_id   BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    fecha_deteccion          DATE          NOT NULL,
    fecha_limite             DATE          NOT NULL,
    fecha_cierre             DATE,
    paralizacion_frente      BOOLEAN       NOT NULL DEFAULT FALSE,
    evidencia_antes_doc_id   BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    evidencia_despues_doc_id BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    estado                   VARCHAR(20)   NOT NULL DEFAULT 'ABIERTO',
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_hallazgo_clasif CHECK (clasificacion IN ('ACTO_SUBESTANDAR', 'CONDICION_SUBESTANDAR')),
    CONSTRAINT ck_soma_hallazgo_potencial CHECK (potencial_perdida IN ('ALTO_GRAVE', 'MEDIO_MODERADO', 'BAJO_LEVE')),
    CONSTRAINT ck_soma_hallazgo_estado CHECK (estado IN ('ABIERTO', 'EN_PROCESO', 'LEVANTADO')),
    CONSTRAINT ck_soma_hallazgo_fechas CHECK (fecha_limite >= fecha_deteccion),
    CONSTRAINT ck_soma_hallazgo_cierre CHECK (estado <> 'LEVANTADO' OR fecha_cierre IS NOT NULL)
);

CREATE INDEX idx_soma_hallazgo_obra_estado ON soma_hallazgo (proyecto_id, estado, fecha_limite);
