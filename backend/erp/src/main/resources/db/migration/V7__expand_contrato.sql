CREATE TABLE jornada (
    id     BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(40)  NOT NULL UNIQUE,
    label  VARCHAR(120) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE causa_objetiva (
    id     BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(80)  NOT NULL UNIQUE,
    label  VARCHAR(255) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE tipo_plantilla (
    id     BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(40)  NOT NULL UNIQUE,
    label  VARCHAR(120) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE metodo_pago (
    id     BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(40)  NOT NULL UNIQUE,
    label  VARCHAR(120) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO jornada (codigo, label) VALUES
    ('48H_SEMANAL', '48 horas semanales'),
    ('36H_SEMANAL', '36 horas semanales'),
    ('24H_SEMANAL', '24 horas semanales'),
    ('8H_DIARIA', '8 horas diarias'),
    ('6X1', '6x1 (seis días / un descanso)'),
    ('14X7', '14x7 (catorce días / siete descanso)');

INSERT INTO causa_objetiva (codigo, label) VALUES
    ('OSC_SELLOS_CORTAFUEGO', 'OSC Instalación de Sellos Cortafuego y Descenso Vertical'),
    ('OSC_PINTURA_RECUBRIMIENTO', 'OSC Suministro e Instalación de Pintura y Recubrimiento'),
    ('OSC_LIMPIEZA_DESCENSO', 'OSC Limpieza y Pintura de Descenso Vertical'),
    ('OSC_TERRAZO', 'OSC Suministro e Instalación de Terrazo');

INSERT INTO tipo_plantilla (codigo, label) VALUES
    ('CONSTRUCCION_CIVIL', 'Construcción Civil'),
    ('REGIMEN_COMUN', 'Régimen Común');

INSERT INTO metodo_pago (codigo, label) VALUES
    ('SEMANAL', 'Semanal'),
    ('QUINCENAL', 'Quincenal'),
    ('MENSUAL', 'Mensual'),
    ('DEPOSITO', 'Depósito bancario'),
    ('EFECTIVO', 'Efectivo');

UPDATE contrato SET estado = 'RESCINDIDO' WHERE estado = 'INACTIVO';

ALTER TABLE contrato
    ADD COLUMN fecha_inicio        DATE,
    ADD COLUMN fecha_fin           DATE,
    ADD COLUMN fecha_corte         DATE,
    ADD COLUMN tipo_plantilla_id   BIGINT REFERENCES tipo_plantilla (id),
    ADD COLUMN jornada_id          BIGINT REFERENCES jornada (id),
    ADD COLUMN metodo_pago_id      BIGINT REFERENCES metodo_pago (id),
    ADD COLUMN causa_objetiva_id   BIGINT REFERENCES causa_objetiva (id),
    ADD COLUMN cargo               VARCHAR(160),
    ADD COLUMN sueldo              NUMERIC(12, 2),
    ADD COLUMN numero              VARCHAR(40),
    ADD COLUMN contrato_origen_id  BIGINT REFERENCES contrato (id),
    ADD COLUMN observaciones       VARCHAR(2000);
