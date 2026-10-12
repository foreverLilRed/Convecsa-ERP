CREATE TABLE persona_link_invitado (
    id         BIGSERIAL PRIMARY KEY,
    persona_id BIGINT         NOT NULL REFERENCES persona (id) ON DELETE RESTRICT,
    token      VARCHAR(64)    NOT NULL,
    creado_en  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    expira_en  TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uq_persona_link_invitado_token UNIQUE (token)
);

CREATE INDEX ix_persona_link_invitado_persona ON persona_link_invitado (persona_id);
