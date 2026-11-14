CREATE TABLE soma_comite_miembro (
    id             BIGSERIAL PRIMARY KEY,
    proyecto_id    BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    persona_id     BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    representacion VARCHAR(20) NOT NULL,
    cargo_comite   VARCHAR(30) NOT NULL,
    titular        BOOLEAN     NOT NULL DEFAULT TRUE,
    fecha_inicio   DATE        NOT NULL,
    fecha_fin      DATE,
    activo         BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_soma_comite_rep CHECK (representacion IN ('EMPLEADOR', 'TRABAJADORES')),
    CONSTRAINT ck_soma_comite_cargo CHECK (cargo_comite IN (
        'PRESIDENTE', 'SECRETARIO', 'MIEMBRO', 'SUPERVISOR_SST', 'OBSERVADOR_SINDICAL'
    ))
);

CREATE TABLE soma_comite_acta (
    id          BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero_acta VARCHAR(40)   NOT NULL,
    tipo_sesion VARCHAR(20)   NOT NULL,
    fecha       DATE          NOT NULL,
    agenda      VARCHAR(1000) NOT NULL,
    acuerdos    VARCHAR(3000) NOT NULL,
    acta_doc_id BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_comite_sesion CHECK (tipo_sesion IN ('INSTALACION', 'ORDINARIA', 'EXTRAORDINARIA')),
    CONSTRAINT uq_soma_comite_acta UNIQUE (proyecto_id, numero_acta)
);
