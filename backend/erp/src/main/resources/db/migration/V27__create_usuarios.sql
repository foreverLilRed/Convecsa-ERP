CREATE TABLE usuarios (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(80)  NOT NULL,
    correo        VARCHAR(180) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    rol           VARCHAR(20)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuarios_username UNIQUE (username),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT ck_usuarios_rol CHECK (rol IN ('SUPERADMIN', 'ADMIN', 'SUPERVISOR'))
);

-- Contraseña temporal: Convecsa2026!
INSERT INTO usuarios (username, correo, password_hash, rol, activo)
VALUES (
    'superadmin',
    'ethan.b@example.com',
    '$2b$10$Ummi/R/Giwc8a30El2jROerG8zn1qQQNEO3QHJ5VD2IZSXRkRGL5S',
    'SUPERADMIN',
    TRUE
);
