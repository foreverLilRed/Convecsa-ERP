CREATE TABLE contrato (
    id          BIGSERIAL PRIMARY KEY,
    persona_id  BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    estado      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_contrato_persona ON contrato (persona_id);
