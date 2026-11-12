ALTER TABLE usuarios DROP CONSTRAINT ck_usuarios_rol;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_rol CHECK (rol IN (
    'SUPERADMIN', 'ADMIN', 'SUPERVISOR', 'RESIDENTE', 'ALMACEN'
));

CREATE TABLE usuario_proyecto (
    id           BIGSERIAL PRIMARY KEY,
    usuario_id   BIGINT        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    proyecto_id  BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuario_proyecto UNIQUE (usuario_id, proyecto_id)
);

CREATE INDEX idx_usuario_proyecto_usuario ON usuario_proyecto (usuario_id);
CREATE INDEX idx_usuario_proyecto_proyecto ON usuario_proyecto (proyecto_id);
