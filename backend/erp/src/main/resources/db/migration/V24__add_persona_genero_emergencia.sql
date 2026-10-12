ALTER TABLE persona
    ADD COLUMN genero VARCHAR(20) NOT NULL DEFAULT 'NO_ESPECIFICA',
    ADD COLUMN contacto_emergencia_nombre VARCHAR(160),
    ADD COLUMN contacto_emergencia_telefono VARCHAR(30),
    ADD COLUMN contacto_emergencia_parentesco VARCHAR(80);

ALTER TABLE persona
    ADD CONSTRAINT ck_persona_genero CHECK (genero IN ('MASCULINO', 'FEMENINO', 'NO_ESPECIFICA'));
