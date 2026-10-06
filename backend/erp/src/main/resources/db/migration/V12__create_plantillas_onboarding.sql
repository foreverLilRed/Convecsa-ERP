CREATE TABLE plantillas_onboarding (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(40)  NOT NULL UNIQUE,
    categoria   VARCHAR(80)  NOT NULL,
    titulo      VARCHAR(200) NOT NULL,
    descripcion TEXT         NOT NULL,
    base_legal  TEXT         NOT NULL,
    plantilla   TEXT         NOT NULL DEFAULT '',
    activo      BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO plantillas_onboarding (codigo, categoria, titulo, descripcion, base_legal, plantilla)
VALUES
    (
        'DOC-01',
        'Contratación',
        'Contrato de Trabajo a Plazo Fijo por Servicio Específico',
        'Contrato laboral oficial con causa objetiva, salario, jornada, lugar de obra y pacto de movilidad.',
        'D.L. 728 Art. 63° / Régimen Especial MYPE D.L. 1086',
        ''
    ),
    (
        'DOC-02',
        'Contratación',
        'Adenda de Prórroga / Renovación de Contrato Laboral',
        'Plantilla Adenda N° 1. Prorroga del periodo de vigencia del contrato de trabajo con ratificación de cláusulas.',
        'D.L. 728 Art. 63° / Ampliación Temporal de Servicio',
        ''
    ),
    (
        'DOC-03',
        'Contratación',
        'Manual de Organización y Funciones del Puesto (MOF)',
        'Descripción detallada de responsabilidades, líneas de reporte, competencias y funciones clave.',
        'Sistema de Gestión de Talento CONVECSA S.A.C.',
        ''
    ),
    (
        'DOC-04',
        'Legal SUNAFIL',
        'Declaración Jurada de Elección del Sistema Previsional (AFP / ONP)',
        'Manifestación de afiliación previsional (AFP Habitat, Prima, Integra, Profuturo u ONP) y constancia de boletín.',
        'Ley Nº 28991, D.S. Nº 009-2008-TR y D.U. Nº 054-2002',
        ''
    ),
    (
        'DOC-05',
        'Legal SUNAFIL',
        'Declaración Jurada de Impuesto a la Renta de 5ta Categoría',
        'Declaración jurada sobre percepción de rentas de otros empleadores para retención tributaria SUNAT.',
        'Art. 44° D.S. Nº 179-2004-EF (TUO Ley de Impuesto a la Renta)',
        ''
    ),
    (
        'DOC-06',
        'Legal SUNAFIL',
        'Constancia de Recepción de RIT y RISST',
        'Cargo de entrega del Reglamento Interno de Trabajo y Reglamento Interno de Seguridad y Salud en el Trabajo.',
        'D.S. 039-91-TR, D.S. 005-2012-TR y Art. 35° Ley 29783',
        ''
    ),
    (
        'DOC-07',
        'Seguridad SST',
        'Recomendaciones de Seguridad y Salud en el Trabajo (SST)',
        'Medidas preventivas, riesgos inherentes al puesto, uso obligatorio de EPP y deberes en seguridad física.',
        'Art. 35° Ley N° 29783 (Obligatorio en Anexo a Contrato)',
        ''
    ),
    (
        'DOC-08',
        'Seguridad SST',
        'Acta de Entrega de EPP e Inducción de Seguridad en el Trabajo',
        'Entrega formal de los 9 EPPs normativos de CONVECSA y constancia de charla de inducción SST (4 horas).',
        'G.050 Seguridad durante la Construcción / Ley 29783',
        ''
    ),
    (
        'DOC-09',
        'Habilitación',
        'Ficha del Trabajador (Datos Personales y Habilitación)',
        'Ficha integral del trabajador: datos familiares, contactos de emergencia, bancos de haberes y CTS, nivel educativo y experiencia.',
        'CVSA-PRH.07.F02 / Formato SUNAFIL de Habilitación',
        ''
    ),
    (
        'DOC-10',
        'Habilitación',
        'Checklist Documental de Ingreso (14 Requisitos SUNAFIL)',
        'Acreditación formal de DNI, CV documentado, RETCC, CUL, antecedentes, EMO vigente, SCTR, T-Registro y legajo.',
        'CVSA-PRH.07.F01 / Fiscalización Laboral',
        ''
    ),
    (
        'DOC-11',
        'Adicional / Cierre',
        'Certificado / Constancia de Trabajo Oficial',
        'Acreditación oficial de tiempo de servicio, cargo desempeñado y conducta laboral intachable.',
        'Art. 7° D.S. Nº 001-98-TR',
        ''
    ),
    (
        'DOC-12',
        'Adicional / Cierre',
        'Certificado de No Adeudo de Bienes, EPP y Herramientas',
        'Constancia de conformidad y devolución de fotocheck, indumentaria, equipos de seguridad y herramientas.',
        'Gestión de Activos y Entrega de Puesto CONVECSA',
        ''
    );
