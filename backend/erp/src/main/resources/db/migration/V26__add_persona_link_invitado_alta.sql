ALTER TABLE persona_link_invitado
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'EDICION',
    ADD COLUMN apellidos VARCHAR(120),
    ADD COLUMN nombres VARCHAR(120),
    ADD COLUMN tipo_documento VARCHAR(20),
    ADD COLUMN numero_documento VARCHAR(30);

ALTER TABLE persona_link_invitado
    ALTER COLUMN persona_id DROP NOT NULL;

ALTER TABLE persona_link_invitado
    ADD CONSTRAINT ck_persona_link_invitado_tipo CHECK (tipo IN ('EDICION', 'ALTA'));

ALTER TABLE persona_link_invitado
    ADD CONSTRAINT ck_persona_link_invitado_edicion CHECK (tipo <> 'EDICION' OR persona_id IS NOT NULL);

ALTER TABLE persona_link_invitado
    ADD CONSTRAINT ck_persona_link_invitado_alta CHECK (
        tipo <> 'ALTA'
        OR (
            apellidos IS NOT NULL
            AND nombres IS NOT NULL
            AND tipo_documento IS NOT NULL
            AND numero_documento IS NOT NULL
        )
    );
