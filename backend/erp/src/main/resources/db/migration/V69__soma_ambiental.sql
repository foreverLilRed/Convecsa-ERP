CREATE TABLE soma_residuo_registro (
    id                BIGSERIAL PRIMARY KEY,
    proyecto_id       BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    fecha             DATE           NOT NULL,
    categoria_residuo VARCHAR(40)    NOT NULL,
    descripcion       VARCHAR(240)   NOT NULL,
    cantidad_kg       NUMERIC(14, 2),
    volumen_m3        NUMERIC(14, 2),
    eors_razon_social VARCHAR(160),
    eors_ruc          VARCHAR(11),
    numero_manifiesto VARCHAR(60),
    destino_final     VARCHAR(160),
    manifiesto_doc_id BIGINT         REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_residuo_cat CHECK (categoria_residuo IN (
        'PELIGROSO_ROJO', 'NO_APROVECHABLE_NEGRO', 'ORGANICO_MARRON',
        'APROVECHABLE_VERDE_AZUL_BLANCO', 'METALICO_AMARILLO', 'ESCOMBROS_DESMONTE_CONSTRUCCION'
    )),
    CONSTRAINT ck_soma_residuo_cantidad CHECK (
        (cantidad_kg IS NOT NULL AND cantidad_kg > 0) OR (volumen_m3 IS NOT NULL AND volumen_m3 > 0)
    )
);

CREATE TABLE soma_monitoreo (
    id                 BIGSERIAL PRIMARY KEY,
    proyecto_id        BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    fecha              DATE           NOT NULL,
    tipo_agente        VARCHAR(40)    NOT NULL,
    parametro_medido   VARCHAR(120)   NOT NULL,
    punto_medicion     VARCHAR(160)   NOT NULL,
    valor_medido       NUMERIC(14, 4) NOT NULL,
    limite_permisible  NUMERIC(14, 4) NOT NULL,
    unidad             VARCHAR(30)    NOT NULL,
    cumple_lmp         BOOLEAN        NOT NULL,
    laboratorio_nombre VARCHAR(160),
    informe_doc_id     BIGINT         REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_monitoreo_agente CHECK (tipo_agente IN (
        'FISICO_RUIDO', 'FISICO_ILUMINACION', 'FISICO_VIBRACION', 'FISICO_ESTRES_TERMICO',
        'QUIMICO_POLVO_GASES', 'BIOLOGICO', 'ERGONOMICO', 'PSICOSOCIAL', 'AMBIENTAL_AGUA_AIRE'
    ))
);
