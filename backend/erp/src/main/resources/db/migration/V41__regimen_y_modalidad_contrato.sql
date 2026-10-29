CREATE TABLE regimen_laboral (
    id     BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(40)  NOT NULL UNIQUE,
    label  VARCHAR(160) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO regimen_laboral (codigo, label) VALUES
    ('REMYPE_PEQUENA', 'REMYPE - Pequeña Empresa'),
    ('REMYPE_MICRO', 'REMYPE - Microempresa'),
    ('REGIMEN_GENERAL', 'Régimen General (D.L. 728)'),
    ('CONSTRUCCION_CIVIL', 'Régimen Construcción Civil (RCC)'),
    ('LOCACION_SERVICIOS', 'Locación de Servicios (Civil / 4ta Categoría)');

ALTER TABLE categoria_contrato
    ADD COLUMN regimen_laboral_id BIGINT REFERENCES regimen_laboral (id),
    ADD COLUMN requiere_fecha_fin BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE categoria_contrato cc
SET regimen_laboral_id = rl.id,
    requiere_fecha_fin = CASE cc.codigo
        WHEN 'PLAZO_INDETERMINADO' THEN FALSE
        ELSE TRUE
    END
FROM regimen_laboral rl
WHERE (cc.codigo IN ('REGIMEN_COMUN', 'PLAZO_FIJO', 'PLAZO_INDETERMINADO', 'TIEMPO_PARCIAL')
        AND rl.codigo = 'REGIMEN_GENERAL')
   OR (cc.codigo = 'CONSTRUCCION_CIVIL' AND rl.codigo = 'CONSTRUCCION_CIVIL')
   OR (cc.codigo = 'LOCACION_SERVICIOS' AND rl.codigo = 'LOCACION_SERVICIOS');

INSERT INTO categoria_contrato (codigo, label, regimen_laboral_id, requiere_fecha_fin)
SELECT v.codigo, v.label, rl.id, v.requiere_fecha_fin
FROM (VALUES
    ('RP_PLAZO_FIJO_OBRA', 'Plazo Fijo - Por Obra Determinada o Servicio Específico', 'REMYPE_PEQUENA', TRUE),
    ('RP_PLAZO_FIJO_INICIO', 'Plazo Fijo - Por Inicio o Incremento de Actividad', 'REMYPE_PEQUENA', TRUE),
    ('RP_PLAZO_FIJO_MERCADO', 'Plazo Fijo - Por Necesidad de Mercado', 'REMYPE_PEQUENA', TRUE),
    ('RP_PLAZO_FIJO_SUPLENCIA', 'Plazo Fijo - De Suplencia', 'REMYPE_PEQUENA', TRUE),
    ('RP_PLAZO_INDETERMINADO', 'A Plazo Indeterminado', 'REMYPE_PEQUENA', FALSE),
    ('RP_TIEMPO_PARCIAL', 'A Tiempo Parcial (Part-Time)', 'REMYPE_PEQUENA', TRUE),
    ('RM_PLAZO_FIJO_OBRA', 'Plazo Fijo - Por Obra Determinada o Servicio Específico', 'REMYPE_MICRO', TRUE),
    ('RM_PLAZO_FIJO_INICIO', 'Plazo Fijo - Por Inicio o Incremento de Actividad', 'REMYPE_MICRO', TRUE),
    ('RM_PLAZO_FIJO_MERCADO', 'Plazo Fijo - Por Necesidad de Mercado', 'REMYPE_MICRO', TRUE),
    ('RM_PLAZO_FIJO_SUPLENCIA', 'Plazo Fijo - De Suplencia', 'REMYPE_MICRO', TRUE),
    ('RM_PLAZO_INDETERMINADO', 'A Plazo Indeterminado', 'REMYPE_MICRO', FALSE),
    ('RM_TIEMPO_PARCIAL', 'A Tiempo Parcial (Part-Time)', 'REMYPE_MICRO', TRUE),
    ('RG_PLAZO_FIJO_OBRA', 'Plazo Fijo - Por Obra Determinada o Servicio Específico', 'REGIMEN_GENERAL', TRUE),
    ('RG_PLAZO_FIJO_INICIO', 'Plazo Fijo - Por Inicio o Incremento de Actividad', 'REGIMEN_GENERAL', TRUE),
    ('RG_PLAZO_FIJO_MERCADO', 'Plazo Fijo - Por Necesidad de Mercado', 'REGIMEN_GENERAL', TRUE),
    ('RG_PLAZO_FIJO_SUPLENCIA', 'Plazo Fijo - De Suplencia', 'REGIMEN_GENERAL', TRUE),
    ('RG_PLAZO_INDETERMINADO', 'A Plazo Indeterminado', 'REGIMEN_GENERAL', FALSE),
    ('RG_TIEMPO_PARCIAL', 'A Tiempo Parcial (Part-Time)', 'REGIMEN_GENERAL', TRUE),
    ('RCC_OBRA_PARTIDA', 'Contrato por Obra o Partida Específica (RCC)', 'CONSTRUCCION_CIVIL', TRUE),
    ('LOC_HONORARIOS', 'Locación de Servicios Profesionales / Técnicos (Recibo por Honorarios)', 'LOCACION_SERVICIOS', TRUE)
) AS v(codigo, label, regimen_codigo, requiere_fecha_fin)
JOIN regimen_laboral rl ON rl.codigo = v.regimen_codigo;

UPDATE contrato c
SET categoria_contrato_id = destino.id
FROM categoria_contrato origen
JOIN categoria_contrato destino ON destino.codigo = CASE origen.codigo
    WHEN 'REGIMEN_COMUN' THEN 'RG_PLAZO_FIJO_OBRA'
    WHEN 'PLAZO_FIJO' THEN 'RG_PLAZO_FIJO_OBRA'
    WHEN 'PLAZO_INDETERMINADO' THEN 'RG_PLAZO_INDETERMINADO'
    WHEN 'TIEMPO_PARCIAL' THEN 'RG_TIEMPO_PARCIAL'
    WHEN 'CONSTRUCCION_CIVIL' THEN 'RCC_OBRA_PARTIDA'
    WHEN 'LOCACION_SERVICIOS' THEN 'LOC_HONORARIOS'
END
WHERE c.categoria_contrato_id = origen.id
  AND origen.codigo IN (
      'REGIMEN_COMUN',
      'PLAZO_FIJO',
      'PLAZO_INDETERMINADO',
      'TIEMPO_PARCIAL',
      'CONSTRUCCION_CIVIL',
      'LOCACION_SERVICIOS'
  );

UPDATE categoria_contrato
SET activo = FALSE
WHERE codigo IN (
    'REGIMEN_COMUN',
    'PLAZO_FIJO',
    'PLAZO_INDETERMINADO',
    'TIEMPO_PARCIAL',
    'CONSTRUCCION_CIVIL',
    'LOCACION_SERVICIOS'
);

ALTER TABLE categoria_contrato
    ALTER COLUMN regimen_laboral_id SET NOT NULL;

CREATE INDEX ix_categoria_contrato_regimen ON categoria_contrato (regimen_laboral_id);
