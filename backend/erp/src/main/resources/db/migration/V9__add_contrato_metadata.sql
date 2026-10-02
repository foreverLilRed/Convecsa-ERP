ALTER TABLE contrato
    ADD COLUMN metadata JSONB NOT NULL DEFAULT '{}'::jsonb;
