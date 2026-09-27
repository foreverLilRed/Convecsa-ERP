CREATE TABLE persona (
    id                  BIGSERIAL PRIMARY KEY,
    apellidos           VARCHAR(120) NOT NULL,
    nombres             VARCHAR(120) NOT NULL,
    tipo_documento      VARCHAR(20)  NOT NULL,
    numero_documento    VARCHAR(30)  NOT NULL,
    fecha_nacimiento    DATE         NOT NULL,
    fecha_emision_dni   DATE,
    fecha_caducidad_dni DATE,
    correo              VARCHAR(180) NOT NULL,
    correo_secundario   VARCHAR(180),
    grado_instruccion   VARCHAR(40)  NOT NULL,
    direccion           VARCHAR(255) NOT NULL,
    departamento        VARCHAR(80)  NOT NULL,
    provincia           VARCHAR(80)  NOT NULL,
    distrito            VARCHAR(80)  NOT NULL,
    residencia          VARCHAR(120) NOT NULL,
    foto_url            VARCHAR(500),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_persona_documento UNIQUE (tipo_documento, numero_documento)
);

CREATE TABLE persona_celular (
    id         BIGSERIAL PRIMARY KEY,
    persona_id BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    numero     VARCHAR(20) NOT NULL
);

CREATE INDEX ix_persona_celular_persona ON persona_celular (persona_id);

CREATE TABLE persona_cuenta_bancaria (
    id            BIGSERIAL PRIMARY KEY,
    persona_id    BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    banco         VARCHAR(40) NOT NULL,
    numero_cuenta VARCHAR(40) NOT NULL,
    cci           CHAR(20)    NOT NULL,
    principal     BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE INDEX ix_persona_cuenta_persona ON persona_cuenta_bancaria (persona_id);

CREATE UNIQUE INDEX uq_persona_cuenta_principal
    ON persona_cuenta_bancaria (persona_id)
    WHERE principal = TRUE;
