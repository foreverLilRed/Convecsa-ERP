CREATE TABLE generaciones_onboarding (
    id          BIGSERIAL PRIMARY KEY,
    persona_id  BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    contrato_id BIGINT      NOT NULL REFERENCES contrato (id) ON DELETE RESTRICT,
    codigos     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_generaciones_onboarding_created_at ON generaciones_onboarding (created_at DESC);
CREATE INDEX ix_generaciones_onboarding_persona ON generaciones_onboarding (persona_id);
