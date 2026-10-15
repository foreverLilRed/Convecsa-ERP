CREATE TABLE nomina_documento_configuracion (
    id BIGINT PRIMARY KEY,
    razon_social VARCHAR(200) NOT NULL,
    ruc VARCHAR(20) NOT NULL,
    domicilio VARCHAR(300) NOT NULL,
    representante VARCHAR(200),
    logo_data_url TEXT,
    firma_data_url TEXT
);

INSERT INTO nomina_documento_configuracion
    (id, razon_social, ruc, domicilio, representante)
VALUES
    (1, 'CONVECSA S.A.C.', '20601414067', 'Calle Van Gogh 345, Urb. San Borja, Lima', NULL);

CREATE TABLE boleta_envio_correo (
    id BIGSERIAL PRIMARY KEY,
    boleta_id BIGINT NOT NULL REFERENCES boleta_pago(id) ON DELETE CASCADE,
    destinatario VARCHAR(180) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    detalle VARCHAR(1000),
    enviado_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_boleta_envio_correo_boleta ON boleta_envio_correo(boleta_id);
