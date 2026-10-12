ALTER TABLE plantillas_onboarding
    ADD COLUMN ambito VARCHAR(20) NOT NULL DEFAULT 'ONBOARDING';

ALTER TABLE plantillas_onboarding
    ADD CONSTRAINT ck_plantillas_onboarding_ambito
        CHECK (ambito IN ('ONBOARDING', 'OFFBOARDING', 'AMBOS'));

UPDATE plantillas_onboarding
SET ambito = 'AMBOS'
WHERE codigo IN ('DOC-11', 'DOC-12');

INSERT INTO plantillas_onboarding (codigo, categoria, titulo, descripcion, base_legal, plantilla, ambito)
VALUES
    (
        'DOC-13',
        'Cese / Liquidación',
        'Liquidación de Beneficios Sociales',
        'Liquidación de CTS trunca, gratificación trunca y vacaciones truncas bajo régimen MYPE, con montos del cese.',
        'D.L. 728 / D.L. 1086 (Régimen Especial MYPE)',
        '',
        'OFFBOARDING'
    ),
    (
        'DOC-14',
        'Cese / Liquidación',
        'Finiquito / Conformidad de cese',
        'Constancia de conformidad de cese, liquidación y entrega de documentos al término del vínculo laboral.',
        'Art. 16° TUO D.L. 728 / Gestión de Personas CONVECSA',
        '',
        'OFFBOARDING'
    );

CREATE TABLE ceses (
    id           BIGSERIAL PRIMARY KEY,
    contrato_id  BIGINT      NOT NULL UNIQUE REFERENCES contrato (id) ON DELETE RESTRICT,
    persona_id   BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    fecha_cese   DATE        NOT NULL,
    tipo_cese    VARCHAR(30) NOT NULL,
    motivo       TEXT,
    liquidacion  JSONB       NOT NULL DEFAULT '{}'::jsonb,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_ceses_tipo CHECK (tipo_cese IN ('RENUNCIA', 'VENCIMIENTO_PLAZO', 'MUTUO_ACUERDO', 'DESPIDO'))
);

CREATE INDEX ix_ceses_persona ON ceses (persona_id);
CREATE INDEX ix_ceses_created_at ON ceses (created_at DESC);

ALTER TABLE generaciones_onboarding
    ADD COLUMN cese_id BIGINT REFERENCES ceses (id);

CREATE INDEX ix_generaciones_onboarding_cese ON generaciones_onboarding (cese_id);
