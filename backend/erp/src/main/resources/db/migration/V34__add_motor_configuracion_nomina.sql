ALTER TABLE persona ADD COLUMN tipo_comision_afp VARCHAR(20) NOT NULL DEFAULT 'FLUJO';

CREATE TABLE nomina_configuracion (
    id BIGINT PRIMARY KEY,
    tasa_essalud NUMERIC(7,6) NOT NULL,
    tasa_sctr_salud NUMERIC(7,6) NOT NULL,
    tasa_sctr_pension NUMERIC(7,6) NOT NULL,
    tasa_vida_ley NUMERIC(7,6) NOT NULL,
    tasa_essalud_vida NUMERIC(7,6) NOT NULL
);

INSERT INTO nomina_configuracion (id, tasa_essalud, tasa_sctr_salud, tasa_sctr_pension, tasa_vida_ley, tasa_essalud_vida)
VALUES (1, 0.090000, 0.000000, 0.000000, 0.000000, 0.000000);
