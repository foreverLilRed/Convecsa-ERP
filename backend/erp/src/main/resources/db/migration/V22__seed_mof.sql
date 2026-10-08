-- Semilla MOF desde maqueta
INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P005',
    'RESIDENTE DE OBRA',
    'STAFF DE OBRA',
    'Representar a CONVECSA S.A.C. ante el cliente y contratista principal, garantizando la ejecución integral del proyecto conforme a los contratos, especificaciones técnicas, plazos, costos, calidad y estándares de seguridad establecidos, asegurando la rentabilidad del proyecto y la satisfacción del cliente.',
    'Gerente de Proyectos y Operaciones',
    'Asistente de Residencia, Supervisores de Producción, Supervisor de Calidad, Supervisor SSOMA'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Representar legal y técnicamente a CONVECSA S.A.C. ante el cliente, contratista principal y entidades fiscalizadoras.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 1, 'Ejercer la dirección técnica y administrativa total del proyecto, coordinando todos los recursos asignados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 2, 'Asegurar el cumplimiento contractual de alcances, plazos, costos, calidad y seguridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 3, 'Aprobar y controlar el presupuesto del proyecto, autorizando gastos dentro de los límites establecidos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 4, 'Liderar la implementación del Sistema Integrado de Gestión (SIG) en el proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 5, 'Establecer y mantener relaciones institucionales con el cliente, contratistas y entidades relacionadas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 6, 'Dirigir y supervisar al equipo de supervisores y personal administrativo de obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 7, 'Aprobar planes de trabajo, cronogramas, solicitudes de materiales y requerimientos de personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 8, 'Revisar, aprobar y suscribir actas de entrega, valorizaciones, adicionales de obra y documentación contractual.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 9, 'Presidir reuniones de coordinación con el cliente y contratista, gestionando la resolución de conflictos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 10, 'Garantizar la documentación técnica y legal del proyecto, incluyendo entrega de dossiers finales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 11, 'Reportar periódicamente a la Gerencia sobre avance físico, financiero, desviaciones y riesgos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 12, 'Tomar decisiones operativas y técnicas de emergencia para salvaguardar al personal y calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 13, 'Coordinar con Asesoría Jurídica en temas contractuales, reclamos y disputas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 14, 'Evaluar el desempeño del personal bajo su cargo y proponer acciones de mejora.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Ingeniería Civil, Arquitectura o carrera afín (completa).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Colegiatura y habilitación profesional vigente.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Especialización en Gestión de Proyectos (PMP o similar).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'EDUCACION', 3, 'Normativa de construcción, contrataciones con el Estado y normas ISO.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 5 años en dirección o residencia de obras.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 1, 'Experiencia en proyectos de pintura, acabados o especialidades.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 2, 'Gestión de contratos con el Estado o grandes empresas privadas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Liderazgo estratégico y toma de decisiones bajo presión.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Negociación y manejo de conflictos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Visión comercial y orientación a resultados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Comunicación asertiva y relaciones interpersonales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Integridad, ética profesional y responsabilidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Planificación estratégica y gestión del cambio.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P005';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P006',
    'ASISTENTE DE RESIDENCIA',
    'STAFF DE OBRA',
    'Apoyar al Residente de Obra en la gestión administrativa, técnica y documentaria del proyecto, asegurando el flujo de información, control de documentos y seguimiento de actividades críticas para el cumplimiento de objetivos del proyecto.',
    'Residente de Obra',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Asistir al Residente en la elaboración de informes técnicos, actas, cronogramas y reportes de avance.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 1, 'Controlar y archivar la documentación técnica del proyecto (planos, especificaciones, órdenes de cambio).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 2, 'Elaborar y actualizar registros de control de obras: metrados, valorizaciones y estado de pagos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 3, 'Coordinar la logística de reuniones con el cliente, incluyendo minutas y seguimiento de compromisos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 4, 'Supervisar el cumplimiento de cronogramas y alertar al Residente sobre desviaciones o riesgos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 5, 'Apoyar en la elaboración de adicionales de obra, ampliaciones de plazo y reclamos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 6, 'Mantener actualizado el sistema de gestión documentaria del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 7, 'Coordinar con el Administrador de Obra temas de personal, asistencia y logística.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 8, 'Realizar inspecciones periódicas para verificar cumplimiento de especificaciones técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 9, 'Apoyar en la preparación de documentación de entrega de obras y dossier final.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 10, 'Sustituir temporalmente al Residente de Obra en sus ausencias programadas si se designa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 11, 'Atender requerimientos del cliente y contratista, derivando al Residente cuando corresponda.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Ingeniería Civil, Arquitectura o afín (mínimo 8vo ciclo o egresado).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'MS Project, Primavera y ofimática avanzada.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Normativa de construcción y contrataciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 2 años en asistencia de residencia o supervisión de obras.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 1, 'Experiencia en proyectos de construcción o especialidades.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Organización y gestión documentaria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Proactividad y orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Comunicación efectiva y trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Capacidad de análisis y síntesis.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Integridad y responsabilidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Manejo de múltiples tareas bajo presión.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P006';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P007',
    'SUPERVISOR DE PRODUCCIÓN',
    'STAFF DE OBRA',
    'Dirigir y controlar la ejecución de los trabajos de producción en campo, garantizando el cumplimiento de programas de obra, estándares de calidad, seguridad y productividad, optimizando el uso de recursos humanos, materiales y equipos.',
    'Residente de Obra / Supervisor de Obra',
    'Asistente de Producción, Cuadrillas de Técnicos y Auxiliares de campo'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Ejecutar y supervisar directamente los trabajos de producción conforme a planos y cronogramas aprobados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 1, 'Liderar y coordinar las cuadrillas de técnicos especialistas y auxiliares de campo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 2, 'Programar diariamente las actividades de producción, asignando personal, materiales y equipos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 3, 'Controlar la productividad de las cuadrillas, registrando avances y reportando desviaciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 4, 'Verificar la disponibilidad y correcto uso de materiales, herramientas y equipos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 5, 'Asegurar el cumplimiento de procedimientos de calidad, coordinando con el Supervisor de Calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 6, 'Implementar y hacer cumplir las normas de seguridad, coordinando con el Supervisor SSOMA.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 7, 'Realizar briefings diarios de seguridad (charlas de 5 minutos) antes del inicio de labores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 8, 'Inspeccionar permanentemente los trabajos, detectando y corrigiendo desviaciones técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 9, 'Coordinar con el Residente la solución de problemas técnicos o logísticos que afecten la producción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 10, 'Controlar la asistencia, puntualidad y disciplina del personal a su cargo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 11, 'Verificar la correcta aplicación de técnicas constructivas según especificaciones de fabricantes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 12, 'Mantener orden y limpieza en áreas de trabajo, asegurando la protección de áreas terminadas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 13, 'Reportar diariamente el avance de obra, incidencias y requerimientos de materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 14, 'Cierre de dossier semanal y mensual.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Estudios técnicos o universitarios en Construcción Civil o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Técnicas de pintura industrial, sellos cortafuego, terrazo y trabajos en altura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Capacitaciones en gestión de producción o lean construction.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en supervisión de producción en obras o especialidades.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 1, 'Experiencia en pintura, recubrimientos o acabados (deseable).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Liderazgo de equipos de trabajo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Orientación a resultados y metas de producción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Toma de decisiones rápida y efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Comunicación directa y clara.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Resolución de problemas operativos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Compromiso con la seguridad y calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P007';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P008',
    'ASISTENTE DE PRODUCCIÓN',
    'STAFF DE OBRA',
    'Apoyar al Supervisor de Producción en el control y seguimiento de las actividades de obra, asegurando la disponibilidad de materiales, herramientas y documentación técnica necesaria para el cumplimiento de los programas de producción.',
    'Supervisor de Producción',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Asistir al Supervisor de Producción en la programación y control diario de actividades.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 1, 'Realizar pedidos de materiales, herramientas y equipos, haciendo seguimiento de entrega.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 2, 'Controlar el inventario de materiales en obra, registrando ingresos, salidas y stocks.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 3, 'Levantar metrados de avance de obra y elaborar reportes diarios de producción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 4, 'Verificar que el personal cuente con los EPPs necesarios antes del inicio de labores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 5, 'Apoyar en la coordinación de movilidad del personal y logística de materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 6, 'Mantener actualizados los planos de obra con cambios y versiones vigentes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 7, 'Supervisar la correcta señalización y protección de áreas de trabajo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 8, 'Registrar la asistencia del personal de producción y reportar novedades.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 9, 'Colaborar en la preparación de áreas para inspecciones de calidad o seguridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 10, 'Documentar incidencias de producción y apoyar en la elaboración de informes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 11, 'Coordinar con el Almacén de Obra la recepción y control de materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 12, 'Cierre de dossier semanal y mensual.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Estudios técnicos en Construcción Civil, Edificaciones o afín.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Ofimática y herramientas de gestión de inventarios.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Conocimiento básico de metrados y especificaciones técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 1 año en asistencia de producción o supervisión de obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Organización y orden.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Proactividad y dinamismo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Responsabilidad y puntualidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Comunicación efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P008B',
    'SUPERVISOR DE CALIDAD (QA/QC)',
    'STAFF DE OBRA',
    'Asegurar que todos los procesos constructivos y materiales aplicados cumplan con las especificaciones técnicas del proyecto, normativas UL/NFPA y estándares de calidad de CONVECSA S.A.C.',
    'Residente de Obra',
    'Asistente de Calidad'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Verificar que todas las instalaciones cumplan con las normativas UL/NFPA.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 1, 'Elaborar y mantener actualizado el Dossier de Calidad del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 2, 'Realizar pruebas de adherencia y liberación de frentes de trabajo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 3, 'Gestionar las no conformidades y acciones correctivas durante la ejecución.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 4, 'Supervisar la recepción y ensayos de laboratorio de materiales de recubrimiento y sellado.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 5, 'Cierre de dossier semanal y mensual.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Ingeniería Civil, Química o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Especialización en Sistemas de Gestión de Calidad ISO 9001.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Certificación en inspección de recubrimientos o sellos cortafuego (NACE/SSPC o equivalente).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en control de calidad en obras de infraestructura o edificación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Rigurosidad técnica y criterio normativo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Orientación al detalle y precisión.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Manejo de no conformidades y auditorías.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Liderazgo en control de calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P008B';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P009',
    'ASISTENTE DE CALIDAD',
    'STAFF DE OBRA',
    'Apoyar al Supervisor de Calidad en la implementación del Sistema de Gestión de Calidad, realizando inspecciones, registros y control documentario para garantizar que los procesos y productos cumplan con los estándares y especificaciones establecidas.',
    'Supervisor de Calidad (QA/QC)',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Asistir en la elaboración y actualización de Planes de Calidad y procedimientos del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 1, 'Realizar inspecciones de calidad en proceso y recepción de materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 2, 'Verificar la trazabilidad de materiales y equipos de medición utilizados en obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 3, 'Controlar documentación de calidad: protocolos, certificados, informes de laboratorio.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 4, 'Fotodocumentar el avance de obra y trabajos críticos para respaldo de calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 5, 'Apoyar en la elaboración de dossiers de calidad y entregables documentarios al cliente.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 6, 'Identificar y reportar no conformidades, apoyando en el seguimiento de acciones correctivas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 7, 'Verificar que los trabajos se ejecuten conforme a planos, especificaciones y normas técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 8, 'Mantener organizados los registros de calidad para disponibilidad en auditorías.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 9, 'Coordinar con los Supervisores de Producción la programación de inspecciones puntuales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 10, 'Colaborar en la capacitación del personal en temas de control de calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 11, 'Controlar el estado de calibración de equipos de medición y ensayo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 12, 'Cierre de dossier semanal y mensual.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Ingeniería Civil, Arquitectura, Química o afines (en curso o egresado).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Norma ISO 9001 y herramientas de control de calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Ofimática y sistemas de gestión documentaria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 1 año en control de calidad en obras de construcción o industria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Orientación al detalle y rigurosidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Ética profesional e integridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Análisis y resolución de problemas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Comunicación técnica efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Organización y sistematización.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P009B',
    'SUPERVISOR SSOMA',
    'STAFF DE OBRA',
    'Implementar, supervisar y hacer cumplir el Plan de Seguridad, Salud Ocupacional y Medio Ambiente conforme a la Ley N° 29783 y normativas sectoriales de construcción.',
    'Residente de Obra',
    'Asistente SSOMA'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Implementar el Plan de Seguridad y Salud en el Trabajo (Ley N° 29783).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 1, 'Supervisar trabajos de alto riesgo, incluyendo descenso vertical en altura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 2, 'Dictar charlas de inducción y capacitaciones periódicas al personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 3, 'Asegurar el uso correcto de los EPP por todo el personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 4, 'Investigar y reportar incidentes, implementando medidas preventivas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 5, 'Cierre de dossier semanal y mensual.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Ingeniero Higienista, Industrial, Civil o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Diplomado o Maestría en Seguridad y Salud Ocupacional.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Colegiatura y habilitación profesional.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en supervisión SSOMA en obras de alto riesgo o construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Liderazgo preventivo y tolerancia cero al riesgo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Manejo de emergencias y primeros auxilios.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Capacidad de instrucción y concientización.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P009B';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P010',
    'ASISTENTE SSOMA',
    'STAFF DE OBRA',
    'Apoyar al Supervisor SSOMA en la implementación, mantenimiento y seguimiento del Sistema de Gestión SSOMA, asegurando el cumplimiento de normativa legal y estándares corporativos para prevenir incidentes y proteger la salud de los trabajadores.',
    'Supervisor SSOMA',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Asistir en la elaboración y actualización del Plan SSOMA del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 1, 'Realizar inspecciones de seguridad periódicas identificando condiciones y actos inseguros.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 2, 'Verificar el correcto uso de EPPs por parte de todo el personal en obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 3, 'Mantener actualizados los registros SSOMA: inspecciones, capacitaciones, incidentes y estadísticas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 4, 'Colaborar en la investigación de incidentes y accidentes, levantando informes preliminares.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 5, 'Apoyar en la organización de capacitaciones y simulacros de emergencia.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 6, 'Controlar la vigencia de certificados médicos ocupacionales y capacitaciones del personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 7, 'Verificar la señalización, demarcación y orden de áreas de trabajo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 8, 'Asistir en la evaluación de riesgos y elaboración de procedimientos de trabajo seguro (PETS).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 9, 'Mantener el botiquín de primeros auxilios y coordinar la atención de emergencias menores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 10, 'Controlar la disposición adecuada de residuos y prácticas ambientales en obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 11, 'Reportar diariamente al Supervisor SSOMA sobre hallazgos, incumplimientos y acciones correctivas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 12, 'Cierre de dossier semanal y mensual.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Estudios técnicos en Seguridad Industrial, Ingeniería Industrial o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Curso de seguridad y salud en el trabajo (mínimo 80 horas).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Normativa legal SSOMA (DS 005-2012-TR y modificatorias).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 1 año en funciones de seguridad industrial en construcción o industria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Proactividad en la identificación de riesgos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Comunicación asertiva para sensibilización en seguridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Responsabilidad y compromiso con la preservación de la vida.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Orientación al detalle en inspecciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Trabajo en equipo y liderazgo de influencia.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Ética profesional.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P010';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P011',
    'JEFE DE OFICINA TÉCNICA',
    'ADMINISTRACIÓN',
    'Dirigir y coordinar las actividades técnicas de la Oficina Técnica, asegurando la correcta elaboración de propuestas, presupuestos, diseños técnicos y documentación de proyectos, garantizando la viabilidad técnica y económica de las ofertas.',
    'Gerente de Proyectos y Operaciones',
    'Asistente de Oficina Técnica, Especialistas Técnicos'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Liderar el proceso de elaboración de propuestas técnicas y económicas para licitaciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 1, 'Revisar y aprobar los diseños técnicos, especificaciones y soluciones constructivas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 2, 'Coordinar con proveedores y fabricantes la obtención de información técnica y precios.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 3, 'Asegurar que las propuestas cumplan con los requerimientos del cliente y normativa aplicable.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 4, 'Dirigir el desarrollo de ingeniería de detalle para proyectos adjudicados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 5, 'Mantener actualizada la base de datos de precios unitarios, rendimientos y normas técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 6, 'Supervisar la elaboración de cronogramas de ejecución y planes de trabajo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 7, 'Brindar soporte técnico al Residente de Obra en la interpretación de planos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 8, 'Coordinar con el área comercial la elaboración de ofertas y negociaciones técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 9, 'Implementar mejoras en los procesos de diseño y elaboración de presupuestos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 10, 'Evaluar nuevas tecnologías, materiales y técnicas constructivas para innovación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 11, 'Controlar la calidad de los entregables técnicos de la Oficina Técnica.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 12, 'Representar a CONVECSA en reuniones técnicas con clientes y contratistas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Ingeniería Civil, Arquitectura o carrera afín.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Especialización en gestión de proyectos, costos o diseño constructivo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'AutoCAD, Revit, software de presupuestos y ofimática.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 4 años en oficina técnica, presupuestos o diseño de proyectos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 1, 'Experiencia en proyectos de pintura, acabados o especialidades constructivas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Pensamiento analítico y resolución de problemas técnicos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Orientación a resultados y cumplimiento de plazos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Liderazgo técnico y capacidad de coordinación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Comunicación técnica efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Innovación y mejora continua.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Integridad y responsabilidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P011';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P012',
    'ASISTENTE DE OFICINA TÉCNICA',
    'ADMINISTRACIÓN',
    'Apoyar al Jefe de Oficina Técnica en la elaboración de propuestas, presupuestos, diseños y documentación técnica, garantizando la precisión y oportunidad de los entregables para la generación de nuevos negocios y ejecución de proyectos.',
    'Jefe de Oficina Técnica',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Elaborar presupuestos y análisis de precios unitarios para propuestas comerciales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 1, 'Desarrollar planos, detalles constructivos y especificaciones técnicas bajo supervisión.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 2, 'Realizar levantamientos de datos en campo para toma de información de proyectos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 3, 'Mantener actualizadas las bases de datos de insumos, precios y proveedores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 4, 'Apoyar en la elaboración de cronogramas y planes de trabajo de propuestas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 5, 'Coordinar con proveedores la solicitud de cotizaciones y fichas técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 6, 'Verificar que la documentación técnica cumpla con los requerimientos de los clientes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 7, 'Archivar y sistematizar la documentación técnica de proyectos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 8, 'Asistir en la preparación de presentaciones técnicas para clientes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 9, 'Colaborar en la ingeniería de detalle de proyectos adjudicados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 10, 'Realizar cálculos de metrados y consumos de materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Ingeniería Civil, Arquitectura o afines (en curso o egresado).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'AutoCAD, software de presupuestos y ofimática.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Conocimiento de metrados y especificaciones técnicas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 1 año en oficina técnica o elaboración de presupuestos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Precisión y orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Proactividad y aprendizaje continuo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Organización y gestión del tiempo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Responsabilidad y compromiso.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Comunicación efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P012';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P013',
    'ADMINISTRADOR(A) DE OBRA',
    'ADMINISTRACIÓN',
    'Gestionar los recursos administrativos, financieros y logísticos del proyecto, asegurando el correcto flujo de caja, control de gastos, gestión de personal y provisionamiento de recursos necesarios para la ejecución eficiente de la obra.',
    'Residente de Obra / Jefatura Administrativa',
    'Asistente Administrativo/RRHH'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Administrar los recursos financieros del proyecto, controlando ingresos y egresos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 1, 'Elaborar y controlar el flujo de caja del proyecto, reportando desviaciones al Residente.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 2, 'Gestionar los pagos a proveedores, contratistas y personal con documentación de respaldo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 3, 'Controlar la documentación contable y financiera: facturas, recibos y vouchers.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 4, 'Administrar el personal de obra: contrataciones, altas/bajas, asistencia y planillas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 5, 'Coordinar con RRHH la gestión de beneficios sociales y trámites laborales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 6, 'Gestionar la logística de almacén: recepción, inventarios y distribución de materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 7, 'Controlar los activos fijos y equipos asignados al proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 8, 'Elaborar reportes financieros y administrativos periódicos para la Gerencia.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 9, 'Asegurar el cumplimiento de obligaciones tributarias y laborales del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 10, 'Gestionar los servicios generales: movilidad, hospedaje, alimentación y servicios básicos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 11, 'Coordinar con el Cliente y Contratista temas administrativos y de pagos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 12, 'Apoyar en la elaboración de valorizaciones y facturación al cliente.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Administración, Contabilidad, Economía o carrera afín.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Normativa tributaria, laboral y de seguridad social.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Software contable y ofimática avanzada.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en administración de obras o proyectos de construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Gestión financiera y control de recursos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Organización y planificación administrativa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Ética profesional y confidencialidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Negociación con proveedores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Trabajo bajo presión y cumplimiento de plazos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Comunicación efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P013';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P014',
    'ESPECIALISTA EN RECURSOS HUMANOS',
    'ADMINISTRACIÓN',
    'Gestionar el capital humano de CONVECSA, asegurando la disponibilidad de personal calificado, el desarrollo de competencias, el clima laboral positivo y el cumplimiento de la normativa laboral.',
    'Jefatura Administrativa / Gerencia General',
    'Asistente Administrativo/RRHH'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Planificar y ejecutar los procesos de reclutamiento, selección e inducción de personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 1, 'Administrar la nómina, beneficios sociales y obligaciones laborales de la empresa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 2, 'Diseñar e implementar programas de capacitación y desarrollo profesional.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 3, 'Gestionar el sistema de evaluación de desempeño y plan de carrera.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 4, 'Asegurar el cumplimiento de la normativa laboral y de seguridad social.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 5, 'Administrar contratos de trabajo, registros de personal y archivo de legajos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 6, 'Gestionar relaciones laborales, mediando en conflictos y aplicando procedimiento disciplinario.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 7, 'Implementar acciones para mejorar el clima organizacional.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 8, 'Coordinar con el Supervisor SSOMA la gestión de seguridad y salud del personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 9, 'Elaborar reportes de gestión de RRHH y estadísticas de personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 10, 'Administrar el sistema de control de asistencia y tardanzas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 11, 'Gestionar trámites ante entidades públicas (SUNAT, ESSALUD, ONP, AFP, etc.).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 12, 'Implementar políticas de bienestar social y responsabilidad social empresarial.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Psicología, Administración de RRHH, Derecho Laboral o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Especialización en gestión del talento o normativa laboral.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Ley de Productividad y Competitividad Laboral y normativa conexa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en gestión de RRHH en empresas de construcción o industria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Orientación al servicio y empatía.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Comunicación interpersonal efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Manejo de conflictos y negociación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Ética profesional y confidencialidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Orientación a resultados y mejora continua.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Liderazgo de influencia.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P014';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P015',
    'ASISTENTE ADMINISTRATIVO / RRHH',
    'ADMINISTRACIÓN',
    'Brindar soporte administrativo en las áreas de gestión de personal, tesorería, logística y documentación, asegurando el correcto funcionamiento de los procesos operativos.',
    'Administrador de Obra / Especialista en RRHH',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Registrar y actualizar la información del personal en el sistema de planillas y asistencia.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 1, 'Apoyar en la elaboración de boletas de pago, liquidaciones y trámites de beneficios sociales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 2, 'Atender la central telefónica, correos electrónicos y público interno y externo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 3, 'Manejar la documentación administrativa: recepción, distribución, archivo y seguimiento.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 4, 'Apoyar en la logística de compras menores, servicios y suministros de oficina.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 5, 'Controlar el archivo de contratos, actas, informes y documentación legal del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 6, 'Asistir en la organización de reuniones, capacitaciones y eventos corporativos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 7, 'Registrar movimientos de caja chica y rendiciones de gastos menores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 8, 'Apoyar en el proceso de reclutamiento: convocatoria, recepción de CVs y citación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 9, 'Mantener actualizados los registros de personal y entregar reportes periódicos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 10, 'Coordinar la movilidad y hospedaje del personal cuando sea requerido.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Administración, Secretariado Ejecutivo, Recursos Humanos o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Software de planillas, ofimática y sistemas de gestión documentaria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 1 año en asistencia administrativa o áreas de RRHH.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Organización y orden.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Discreción y confidencialidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Atención al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Proactividad y servicio al cliente interno.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Responsabilidad y puntualidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P015';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P016',
    'CONTADOR(A)',
    'ADMINISTRACIÓN',
    'Dirigir y controlar la gestión contable, financiera y tributaria de CONVECSA, asegurando la registración oportuna y precisa de las operaciones y el cumplimiento de obligaciones fiscales.',
    'Gerencia General / Jefatura Administrativa',
    'Asistente Contable'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Dirigir el área contable, estableciendo procedimientos y controles internos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 1, 'Supervisar la registración contable de operaciones y conciliaciones bancarias.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 2, 'Elaborar estados financieros mensuales, trimestrales y anuales conforme a NIIF.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 3, 'Asegurar el cumplimiento de obligaciones tributarias: declaraciones, pagos y retenciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 4, 'Coordinar con el Administrador de Obra el control financiero de proyectos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 5, 'Preparar y presentar la declaración anual de impuestos y auditorías.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 6, 'Analizar la situación financiera de la empresa y proponer mejoras.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 7, 'Controlar cuentas por cobrar y por pagar, asegurando la salud del flujo de caja.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 8, 'Elaborar reportes financieros para la Gerencia y Directorio.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 9, 'Mantener actualizada la información en el sistema contable.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 10, 'Asesorar en aspectos tributarios y contables a las demás áreas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 11, 'Coordinar con Asesoría Jurídica en temas fiscales y contractuales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Contabilidad, Ciencias Económicas o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Colegiatura vigente en el Colegio de Contadores Públicos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'NIIF, normativa tributaria y laboral peruana.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'EDUCACION', 3, 'Especialización en finanzas, tributación o auditoría (deseable).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 4 años en contabilidad general, preferiblemente en construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Análisis financiero y contable.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Ética profesional e integridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Orientación al detalle y precisión.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Cumplimiento de normas y plazos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Comunicación técnica efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Liderazgo de equipos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P016';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P017',
    'ASISTENTE CONTABLE',
    'ADMINISTRACIÓN',
    'Apoyar en la registración contable, conciliaciones y procesamiento de documentos financieros, asegurando la actualización oportuna de los registros y el respaldo documentario de las operaciones.',
    'Contador(a)',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Registrar operaciones contables en el sistema: compras, ventas, gastos, caja y bancos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 1, 'Realizar conciliaciones bancarias y verificación de estados de cuenta.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 2, 'Clasificar y codificar documentos contables según el plan de cuentas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 3, 'Apoyar en la elaboración de declaraciones tributarias mensuales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 4, 'Controlar la documentación de respaldo: facturas, recibos y comprobantes de pago.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 5, 'Registrar cuentas por cobrar y por pagar, realizando seguimiento de vencimientos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 6, 'Archivar y sistematizar la documentación contable física y digital.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 7, 'Apoyar en la elaboración de reportes financieros básicos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 8, 'Atender requerimientos de SUNAT y otras entidades reguladoras.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 9, 'Colaborar en la preparación de información para auditorías.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Contabilidad o afines (en curso o egresado).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Normativa tributaria básica y plan contable general empresarial.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Software contable y ofimática.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 1 año en áreas contables o administrativas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Orden y sistematización.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Ética y confidencialidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Responsabilidad y cumplimiento de plazos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Proactividad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P017';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P018',
    'ADMINISTRADOR(A) - CONTABLE',
    'ADMINISTRACIÓN',
    'Ejercer funciones combinadas de administración de obra y contabilidad de proyecto, integrando la gestión operativa, financiera y documentaria del proyecto.',
    'Gerente de Proyectos / Jefatura Administrativa',
    'Asistente Administrativo (según proyecto)'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Gestionar recursos operativos, logísticos y de personal del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 1, 'Controlar asistencia, planillas y beneficios del personal de obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 2, 'Administrar almacén, inventarios y activos fijos del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 3, 'Gestionar servicios generales: movilidad, hospedaje y alimentación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 4, 'Registrar operaciones contables del proyecto en sistema.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 5, 'Controlar flujo de caja, cuentas por cobrar y por pagar.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 6, 'Elaborar reportes financieros del proyecto y valorizaciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 7, 'Asegurar cumplimiento de obligaciones tributarias del proyecto.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 8, 'Elaborar y controlar el presupuesto de gastos generales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 9, 'Integrar información administrativa y financiera para reportes gerenciales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 10, 'Coordinar con Contabilidad Central la consolidación de información.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 11, 'Gestionar documentación contractual, de adquisiciones y pagos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Administración, Contabilidad o carrera afín.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Contabilidad de costos, presupuestos y normativa tributaria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Software contable, de gestión de proyectos y ofimática.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en administración de obras y contabilidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 1, 'Experiencia en proyectos de construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Versatilidad y multitasking.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Pensamiento analítico financiero.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Organización y gestión del tiempo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Toma de decisiones operativas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Ética profesional.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Orientación a resultados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P018';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P019',
    'PRACTICANTE PRE-PROFESIONAL / PROFESIONAL',
    'HÍBRIDOS',
    'Desarrollar competencias profesionales mediante la aplicación práctica de conocimientos académicos en proyectos reales de la empresa, apoyando las actividades del área asignada bajo supervisión.',
    'Según área de asignación',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Ejecutar actividades técnicas, administrativas o de gestión según el área de asignación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 1, 'Elaborar informes, análisis y documentación bajo supervisión del profesional a cargo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 2, 'Participar en levantamientos de información, inspecciones o actividades de campo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 3, 'Apoyar en la sistematización de información y bases de datos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 4, 'Asistir en la elaboración de presentaciones, propuestas o reportes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 5, 'Cumplir con el plan de prácticas establecido por la universidad y la empresa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 6, 'Aplicar los conocimientos teóricos en situaciones prácticas del sector construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 7, 'Mantener actitud de aprendizaje, proactividad y compromiso con las tareas asignadas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 8, 'Respetar las normas de seguridad, calidad y medio ambiente de la empresa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 9, 'Presentar informes de avance y evaluaciones según los requisitos institucionales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Estudiante universitario avanzado (7mo ciclo) o egresado.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Ingeniería Civil, Arquitectura, Administración, Contabilidad, Seguridad o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Conocimientos teóricos de la especialidad según área de prácticas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'No requiere experiencia previa (pre-profesional).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 1, 'Máximo 1 año de experiencia (profesional en formación).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Proactividad y deseo de aprendizaje.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Responsabilidad y compromiso.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Adaptabilidad al entorno de construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Comunicación efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P019';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P020',
    'TÉCNICO INSTALADOR DE SELLOS CORTAFUEGO',
    'TECNICOS',
    'Ejecutar la instalación de sistemas de sellos cortafuego (pastillas, masillas, barreras, selladores intumescentes) conforme a especificaciones técnicas, normas NFPA y estándares de calidad y seguridad.',
    'Supervisor de Producción',
    'Auxiliares de campo (según asignación)'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Instalar sellos cortafuego en penetraciones de tuberías, cables, ductos y aberturas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 1, 'Preparar superficies: limpieza, aplicación de imprimantes y acondicionamiento de sustratos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 2, 'Aplicar masillas intumescentes, selladores elastoméricos y sistemas de barrera contra fuego.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 3, 'Instalar pastillas, colchones y dispositivos de sellado según diseño aprobado.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 4, 'Verificar la correcta aplicación según fichas técnicas de fabricantes y normas NFPA.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 5, 'Realizar pruebas de estanqueidad y verificación de espesores de aplicación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 6, 'Mantener limpieza y orden en el área de trabajo, protegiendo instalaciones aledañas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 7, 'Utilizar correctamente los EPPs específicos: guantes químicos, gafas, respiradores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 8, 'Operar herramientas manuales y eléctricas de manera segura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 9, 'Reportar al Supervisor de Producción sobre avances, problemas técnicos o faltantes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 10, 'Colaborar en la preparación de áreas para inspecciones de calidad y seguridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 11, 'Mantener actualizados los conocimientos en nuevos productos y técnicas de instalación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Construcción Civil, Electricidad, Mecánica o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Certificación en instalación de sellos cortafuego (3M, Hilti, Promat, etc.).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Normas NFPA, UL y estándares de protección pasiva contra incendios.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 2 años en instalación de sellos cortafuego o sistemas de protección contra incendios.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Precisión y orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Cumplimiento de estándares de calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Conciencia de seguridad industrial.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Responsabilidad y puntualidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Adaptabilidad a diferentes entornos de obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P020';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P021',
    'TÉCNICO INSTALADOR DE PINTURA',
    'TECNICOS',
    'Ejecutar trabajos de pintura industrial, decorativa y protectora conforme a especificaciones técnicas, estándares de calidad y normas de seguridad, garantizando acabados profesionales.',
    'Supervisor de Producción',
    'Auxiliar de Instalador de Pintura'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Preparar superficies mediante lijado, limpieza, desengrasado y eliminación de irregularidades.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 1, 'Aplicar imprimantes, fondos, pinturas de acabado y recubrimientos protectores.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 2, 'Ejecutar técnicas de aplicación: brocha, rodillo, pistola airless, air-assisted según especificaciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 3, 'Mezclar y preparar pinturas, respetando proporciones y tiempos de inducción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 4, 'Aplicar pinturas en interiores, exteriores, en altura y superficies especiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 5, 'Respetar especificaciones de espesores de película seca y número de manos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 6, 'Proteger áreas no pintables con cintas, plásticos y elementos de protección.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 7, 'Utilizar EPPs adecuados: mascarillas con filtros orgánicos, guantes, overoles.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 8, 'Realizar trabajos en altura utilizando andamios, plataformas o arnés según normativa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 9, 'Mantener herramientas y equipos de aplicación en óptimas condiciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 10, 'Reportar al Supervisor sobre avances, defectos de sustrato o problemas de aplicación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 11, 'Cumplir con tiempos de secado, curado y protección de trabajos terminados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Pintura Decorativa, Construcción Civil o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Certificaciones de fabricantes de pinturas (Comex, Sherwin Williams, PPG, etc.).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Técnicas de aplicación, preparación de superficies y tipos de pinturas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en pintura industrial o decorativa en obras de construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Pulcritud y acabado de calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Conocimiento de técnicas y materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Cumplimiento de estándares de seguridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Responsabilidad y puntualidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P021';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P022',
    'TÉCNICO DE DESCENSO VERTICAL (TRABAJOS EN ALTURA)',
    'TECNICOS',
    'Ejecutar trabajos de pintura, sellado, limpieza y mantenimiento en fachadas, estructuras elevadas y sitios de difícil acceso utilizando técnicas de descenso vertical con cuerdas.',
    'Supervisor de Producción',
    'Auxiliar de Descenso Vertical'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Realizar trabajos de pintura, sellado y mantenimiento en altura mediante técnicas de descenso vertical.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 1, 'Inspeccionar y preparar los equipos de protección contra caídas (arnés, cuerdas, descensores, anclajes).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 2, 'Montar sistemas de anclaje y líneas de vida temporales conforme a procedimientos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 3, 'Verificar la integridad de equipos antes de cada uso y reportar desgastes o daños.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 4, 'Ejecutar maniobras de ascenso, descenso y posicionamiento con técnicas seguras.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 5, 'Aplicar pinturas, selladores y recubrimientos en fachadas y estructuras elevadas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 6, 'Rescatar compañeros en emergencias utilizando técnicas de rescate vertical.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 7, 'Coordinar con el Supervisor SSOMA la implementación de permisos de trabajo en altura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 8, 'Mantener comunicación constante con el equipo de superficie durante operaciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 9, 'Documentar condiciones de los puntos de anclaje y estructuras intervenidas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 10, 'Participar en simulacros de rescate y capacitaciones de actualización.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 11, 'Cumplir estrictamente con procedimientos de trabajo seguro en altura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Construcción, Electricidad o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Certificación en trabajos en altura y rescate vertical (IRATA, SPRAT o similar).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Curso de primeros auxilios y rescate básico.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'EDUCACION', 3, 'Normativa de seguridad para trabajos en altura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 2 años en descenso vertical o trabajos en altura con cuerdas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 1, 'Experiencia en pintura o sellado en fachadas (deseable).' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Conciencia de seguridad extrema.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Físicamente apto y sin vértigo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Precisión en trabajos técnicos en altura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Trabajo en equipo y comunicación efectiva.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Responsabilidad y disciplina.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Capacidad de respuesta en emergencias.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P022';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P023',
    'TÉCNICO INSTALADOR DE TERRAZO',
    'TECNICOS',
    'Ejecutar la instalación de pisos y revestimientos de terrazo, granito y materiales pétreos similares, garantizando la calidad del acabado, nivelación, pulido y brillado conforme a especificaciones técnicas.',
    'Supervisor de Producción',
    'Auxiliar de Instalador de Terrazo'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Preparar superficies de recepción: nivelación, limpieza y acondicionamiento.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 1, 'Instalar divisiones o juntas de control en terrazo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 2, 'Colocar y nivelar la mezcla de terrazo según espesores especificados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 3, 'Compactar, nivelar y alisar la superficie de terrazo recién colocado.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 4, 'Realizar el desencofrado y curado del terrazo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 5, 'Ejecutar procesos de pulido, brillado y cristalizado de superficies.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 6, 'Aplicar selladores y productos de protección para acabado final.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 7, 'Realizar reparaciones de fisuras, agujeros y defectos en pisos existentes.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 8, 'Operar máquinas pulidoras, fresadoras y herramientas específicas de manera segura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 9, 'Verificar niveles, escuadras y alineaciones durante la instalación.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 10, 'Proteger trabajos terminados hasta su entrega final.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 11, 'Mantener limpieza del área de trabajo y eliminación de residuos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 12, 'Reportar al Supervisor sobre avances y requerimientos de materiales.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Construcción Civil, Instalaciones de Pisos o afines.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Técnicas de colocación, pulido y mantenimiento de terrazo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'EDUCACION', 2, 'Manejo de maquinaria pulidora y herramientas de corte.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 3 años en instalación de terrazo, granito o pisos pétreos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Pulcritud y acabado de alta calidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Precisión en niveles y alineaciones.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Manejo cuidadoso de maquinaria.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'COMPETENCIA', 3, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'COMPETENCIA', 4, 'Responsabilidad y puntualidad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023'
    UNION ALL
    SELECT id, 'COMPETENCIA', 5, 'Orientación al detalle.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P023';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P024',
    'AUXILIAR DE DESCENSO VERTICAL',
    'AUXILIARES',
    'Brindar apoyo técnico y logístico en tierra y en puntos de anclaje para los trabajos de descenso vertical, velando por la seguridad y la correcta disposición de insumos.',
    'Técnico de Descenso Vertical / Supervisor de Producción',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Apoyar al técnico de descenso vertical en el traslado y preparación de equipos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 1, 'Verificar el estado de arneses, sogas y equipos de protección antes del uso.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 2, 'Mantener despejada y señalizada el área de trabajo en altura.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 3, 'Registrar el uso y estado del equipo de protección personal.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 4, 'Participar en las charlas de seguridad y cumplir las instrucciones del Supervisor SSOMA.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Secundaria completa o técnico en Construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Capacitaciones en Trabajos en Altura y PETAR.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 1 año en asistencia de trabajos en altura o construcción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Atención a instrucciones de seguridad.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Trabajo colaborativo y vigía activo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024'
    UNION ALL
    SELECT id, 'COMPETENCIA', 2, 'Responsabilidad en el manejo de equipos de protección.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P024';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P025',
    'AUXILIAR DE INSTALADOR DE PINTURA',
    'AUXILIARES',
    'Asistir al técnico especialista en las labores de preparación de superficies, masillado, mezcla de materiales y mantenimiento de equipos de pintura.',
    'Técnico Instalador de Pintura / Supervisor de Producción',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Preparar materiales, herramientas y equipos de pintura para el técnico.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 1, 'Realizar el masillado y lijado de superficies en áreas asignadas.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 2, 'Aplicar pintura en zonas de fácil acceso bajo supervisión directa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 3, 'Mantener el orden y limpieza del área de trabajo al término de cada jornada.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 4, 'Reportar el consumo de materiales al Supervisor de Producción.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Secundaria completa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Capacitaciones en manejo seguro de insumos químicos y EPP.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 6 meses en auxiliares de pintura o acabados.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Dinamismo, orden y pulcritud.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P025';

INSERT INTO mof_puesto (codigo, cargo, grupo, proposito, superior, subordinados)
VALUES (
    'CVSA-MOF-P026',
    'AUXILIAR DE INSTALADOR DE TERRAZO',
    'AUXILIARES',
    'Apoyar en la mezcla, vertido, limpieza y desescombro en los procesos de vaciado y pulido de terrazo.',
    'Técnico Instalador de Terrazo / Supervisor de Producción',
    'Ninguno'
);
INSERT INTO mof_funcion (puesto_id, orden, texto)
    SELECT id, 0, 'Apoyar en la preparación de la mezcla de terrazo según dosificación del técnico.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 1, 'Realizar la limpieza y acondicionamiento de la base antes del vaciado.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 2, 'Transportar materiales y herramientas dentro del área de trabajo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 3, 'Apoyar en el proceso de pulido mecánico bajo supervisión directa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 4, 'Mantener el orden del área y clasificar los residuos de obra.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026';
INSERT INTO mof_requisito (puesto_id, tipo, orden, texto)
    SELECT id, 'EDUCACION', 0, 'Secundaria completa.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 'EDUCACION', 1, 'Capacitación en uso de EPP e higiene industrial.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 'EXPERIENCIA', 0, 'Mínimo 6 meses en auxiliares de albañilería o pisos.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 'COMPETENCIA', 0, 'Esfuerzo físico constante y disciplina.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026'
    UNION ALL
    SELECT id, 'COMPETENCIA', 1, 'Trabajo en equipo.' FROM mof_puesto WHERE codigo = 'CVSA-MOF-P026';

