CREATE TABLE persona_experiencia_laboral (
    id                   BIGSERIAL PRIMARY KEY,
    persona_id           BIGINT       NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    organizacion         VARCHAR(160) NOT NULL,
    cargo                VARCHAR(160) NOT NULL,
    fecha_inicio_laboral DATE         NOT NULL,
    fecha_fin            DATE
);

CREATE INDEX ix_persona_experiencia_persona ON persona_experiencia_laboral (persona_id);
