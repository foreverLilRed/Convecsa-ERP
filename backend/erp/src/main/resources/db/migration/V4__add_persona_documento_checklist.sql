CREATE TABLE persona_documento_checklist (
    id              BIGSERIAL PRIMARY KEY,
    persona_id      BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    tipo_documento  VARCHAR(40) NOT NULL,
    criticidad      VARCHAR(20) NOT NULL,
    estado          VARCHAR(20) NOT NULL,
    CONSTRAINT uq_persona_documento_tipo UNIQUE (persona_id, tipo_documento)
);

CREATE INDEX ix_persona_documento_checklist_persona ON persona_documento_checklist (persona_id);

INSERT INTO persona_documento_checklist (persona_id, tipo_documento, criticidad, estado)
SELECT p.id, t.tipo, t.criticidad, 'PENDIENTE'
FROM persona p
CROSS JOIN (
    VALUES
        ('DNI_2_CARAS', 'CRITICO'),
        ('CV_CERTIFICADO_TRABAJO', 'NORMAL'),
        ('CARNET_RETCC', 'NORMAL'),
        ('CUL', 'NORMAL'),
        ('ANTECEDENTES_POLICIALES', 'NORMAL'),
        ('ANTECEDENTES_JUDICIALES', 'NORMAL'),
        ('ANTECEDENTES_PENALES', 'NORMAL'),
        ('CARNET_VACUNACION', 'NORMAL'),
        ('EMO_APTO_VIGENTE', 'CRITICO'),
        ('SCTR', 'CRITICO'),
        ('POLIZA_VIDA_LEY', 'NORMAL'),
        ('CHECKLIST_INGRESO', 'NORMAL'),
        ('FICHA_TRABAJADOR', 'NORMAL'),
        ('ALTA_T_REGISTRO', 'CRITICO')
) AS t(tipo, criticidad);
