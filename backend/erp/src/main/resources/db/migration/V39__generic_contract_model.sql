CREATE TABLE categoria_contrato (
    id      BIGSERIAL PRIMARY KEY,
    codigo  VARCHAR(50)  NOT NULL UNIQUE,
    label   VARCHAR(160) NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO categoria_contrato (codigo, label) VALUES
    ('REGIMEN_COMUN', 'Contrato laboral - régimen común'),
    ('CONSTRUCCION_CIVIL', 'Contrato laboral - construcción civil'),
    ('PLAZO_INDETERMINADO', 'Contrato a plazo indeterminado'),
    ('PLAZO_FIJO', 'Contrato sujeto a modalidad (plazo fijo)'),
    ('TIEMPO_PARCIAL', 'Contrato a tiempo parcial'),
    ('LOCACION_SERVICIOS', 'Locación de servicios');

ALTER TABLE contrato
    ADD COLUMN titulo VARCHAR(200),
    ADD COLUMN categoria_contrato_id BIGINT REFERENCES categoria_contrato (id),
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE contrato c
SET categoria_contrato_id = COALESCE(
        (SELECT cc.id
         FROM categoria_contrato cc
         JOIN tipo_plantilla tp ON tp.codigo = cc.codigo
         WHERE tp.id = c.tipo_plantilla_id),
        (SELECT id FROM categoria_contrato WHERE codigo = 'REGIMEN_COMUN')
    ),
    titulo = COALESCE(NULLIF(c.metadata ->> 'titulo', ''), NULLIF(c.cargo, ''), 'Contrato laboral'),
    activo = c.estado = 'ACTIVO',
    sueldo = COALESCE(c.sueldo, 0),
    metadata = COALESCE(c.metadata, '{}'::jsonb)
        || jsonb_strip_nulls(jsonb_build_object(
            'estado_anterior', c.estado,
            'cargo', c.cargo,
            'area', c.area,
            'proyecto', c.proyecto,
            'ubicacion_obra', c.ubicacion_obra,
            'ciudad_firma', c.ciudad_firma,
            'numero', c.numero,
            'jornada_id', c.jornada_id::text,
            'metodo_pago_id', c.metodo_pago_id::text,
            'causa_objetiva_id', c.causa_objetiva_id::text,
            'afp_id', c.afp_id::text,
            'tiene_asignacion_familiar', c.tiene_asignacion_familiar::text,
            'tipo_plantilla_anterior_id', c.tipo_plantilla_id::text,
            'contrato_origen_id', c.contrato_origen_id::text
        ));

DO $$
DECLARE
    plantilla_fallback BIGINT;
BEGIN
    SELECT id INTO plantilla_fallback
    FROM plantillas_contrato
    WHERE activo = TRUE
    ORDER BY id
    LIMIT 1;

    IF plantilla_fallback IS NULL AND EXISTS (SELECT 1 FROM contrato) THEN
        RAISE EXCEPTION 'No existe una plantilla de contrato activa para migrar los contratos';
    END IF;

    UPDATE contrato
    SET plantilla_contrato_id = plantilla_fallback
    WHERE plantilla_contrato_id IS NULL;
END $$;

ALTER TABLE contrato
    DROP CONSTRAINT IF EXISTS contrato_tipo_plantilla_id_fkey,
    DROP COLUMN tipo_plantilla_id;

ALTER TABLE contrato
    RENAME COLUMN plantilla_contrato_id TO tipo_plantilla_id;

ALTER TABLE contrato
    ALTER COLUMN titulo SET NOT NULL,
    ALTER COLUMN categoria_contrato_id SET NOT NULL,
    ALTER COLUMN sueldo SET NOT NULL,
    ALTER COLUMN tipo_plantilla_id SET NOT NULL,
    ALTER COLUMN observaciones TYPE TEXT;

ALTER TABLE contrato
    DROP COLUMN estado,
    DROP COLUMN jornada_id,
    DROP COLUMN metodo_pago_id,
    DROP COLUMN causa_objetiva_id,
    DROP COLUMN cargo,
    DROP COLUMN afp_id,
    DROP COLUMN tiene_asignacion_familiar,
    DROP COLUMN numero,
    DROP COLUMN contrato_origen_id,
    DROP COLUMN proyecto,
    DROP COLUMN ubicacion_obra,
    DROP COLUMN area,
    DROP COLUMN ciudad_firma,
    DROP COLUMN created_at,
    DROP COLUMN updated_at;

DROP TABLE tipo_plantilla;

CREATE INDEX ix_contrato_persona_activo ON contrato (persona_id, activo);
CREATE INDEX ix_contrato_categoria ON contrato (categoria_contrato_id);
