# Módulo SOMA (Seguridad, Salud Ocupacional y Medio Ambiente) — Especificación ejecutable por agente

> **Cómo usar este documento.** Está escrito para que un agente de desarrollo lo implemente **por entregas pequeñas y verificables**. Cada entrega (sección 5) es un corte vertical (migración + backend + frontend + tests) que deja el sistema compilando y usable. El agente toma **una entrega por vez**, en orden, y no empieza la siguiente sin cumplir su "Definición de terminado".

---

## 0. Estado de avance

El agente actualiza esta tabla al cerrar cada entrega (estado: `Pendiente`, `En curso`, `Hecho`). Es la única parte del documento que el agente debe editar.

| # | Entrega | Migración | Estado |
| :- | :--- | :--- | :--- |
| S0 | Base: rol `INGENIERO_SOMA`, paquete `soma`, pestaña SOMA vacía | V57 | Hecho |
| S1 | Historial EMO y sincronización con `Persona` | V58 | Hecho |
| S2 | Pólizas SCTR mensuales por obra | V59 | Hecho |
| S3 | Entrega de EPP integrada con almacén | V60 | Hecho |
| S4 | Capacitaciones e inducción de hombre nuevo | V61 | Hecho |
| S5 | Semáforo de habilitación (pase de ingreso a obra) | — | Hecho |
| S6 | Matriz IPERC por partida | V62 | Hecho |
| S7 | Charla diaria y ATS | V63 | Hecho |
| S8 | PETAR y cruce con tareo | V65 | Hecho |
| S9 | Inspecciones y hallazgos | V64 | Hecho |
| S10 | Check-list pre-uso de equipos | V66 | Hecho |
| S11 | Eventos SST (incidentes, accidentes, enfermedades) | V67 | Hecho |
| S12 | Estadísticas HHT, IF, IS, IA | — | Hecho |
| S13 | Comité / Subcomité / Supervisor SST | V68 | Pendiente |
| S14 | Gestión ambiental (residuos y monitoreos) | V69 | Pendiente |
| S15 | Alertas SOMA en la ficha de obra | — | Pendiente |
| S16 | Formatos SUNAFIL (R.M. 050-2013-TR) en PDF y Excel | — | Pendiente |
| S17 | Vistas corporativas `/soma/*` y menú | — | Pendiente |

> **Numeración Flyway:** los números de la columna "Migración" suponen que la última versión existente es `V56`. Antes de crear un archivo, el agente lista `backend/erp/src/main/resources/db/migration/` y usa **el siguiente número libre**, manteniendo el sufijo descriptivo (`__soma_...`).

---

## 1. Contexto

Convecsa es una constructora peruana. El módulo SOMA debe cubrir la gestión de seguridad y salud en obra según:

- Ley N° 29783 (Ley de Seguridad y Salud en el Trabajo) y su reglamento, D.S. N° 005-2012-TR.
- D.S. N° 011-2019-TR (Reglamento de SST para el Sector Construcción).
- R.M. N° 050-2013-TR (formatos referenciales de los registros obligatorios).
- Norma Técnica G.050 "Seguridad durante la Construcción".

El ERP ya resuelve buena parte de la base (personas, checklist documental, obras, almacén, tareo, archivos, alertas, auditoría y roles). **SOMA se construye encima de eso, no en paralelo.**

---

## 2. Reglas obligatorias para el agente

1. **Cero duplicación.** Prohibido crear tablas paralelas de trabajadores, obras, inventario de EPP o almacenamiento de archivos. Todo se vincula a `persona`, `proyecto`, `material`, `vale_salida`, `documento_obra`, etc.
2. **Paquete backend:** `com.convecsa.erp.soma` (`controller`, `domain`, `dto`, `entity`, `repository`, `service`, `service/impl`, `support`). Los cambios en otros paquetes se limitan a los puntos de conexión que indique cada entrega.
3. **No tocar** `CalculoNominaService` ni `CalculoNominaCcService`. El golden de OB001 no cambia.
4. **Sin integraciones externas:** nada de SUNAT, AFPnet, CPE, MTPE en línea, VBA ni envío de correos.
5. **Frontend con shadcn-vue:** usar solo componentes de la biblioteca. Hoy están instalados `badge`, `button`, `combobox`, `input`, `input-group`, `pagination`, `skeleton`, `sonner`, `table`, `tabs` y `textarea`. Si hace falta otro (por ejemplo `dialog`, `checkbox`, `card`), se instala con `npx shadcn-vue@latest add <componente>`; **no se escriben componentes propios equivalentes**. Los selects siguen el patrón de la ficha de obra: `<select>` nativo con `fieldSelectClass`. Los inputs usan `fieldInputClass` y las pestañas usan `tabsListClass`, todos de `frontend/src/pages/proyectos/fieldClasses.ts`.
6. **Jackson 3:** el proyecto usa Spring Boot 4.1.1. **No importar** `com.fasterxml.jackson.databind.*`. Las columnas `JSONB` se mapean como en `Contrato.java`: `@JdbcTypeCode(SqlTypes.JSON)`.
7. **Convenciones SQL:** `BIGSERIAL`, `TIMESTAMPTZ`, `NUMERIC`, constraints `ck_*`/`uq_*`, índices `idx_*`, identificadores sin tildes ni eñes.
8. **Montos y cálculos** con `BigDecimal`; **fechas** con `LocalDate`/`Instant`.
9. **Errores:** las validaciones de negocio lanzan `IllegalArgumentException` (responde 400). El acceso denegado lanza `ProyectoSinAccesoException` (responde 403).
10. **No hacer commits** salvo que el usuario lo pida.
11. Idioma: código, mensajes y UI en español, igual que el resto del ERP.

---

## 3. Mapa de reutilización (verificado contra el código)

| Necesidad SOMA | Recurso existente | Regla de uso |
| :--- | :--- | :--- |
| Datos médicos del trabajador | `persona.fecha_vigencia_emo`, `persona.aptitud_medica`, `persona.restricciones_medicas` (V3). Enum `AptitudMedica` = `APTO`, `OBSERVADO`, `PENDIENTE`, `VENCIDO`. | No crear otra ficha médica. El historial va en `soma_emo_examen`. `Persona` guarda solo el **último** EMO resumido (ver S1). |
| Requisitos documentales | `persona_documento_checklist` (V4), `TipoDocumentoChecklist` (`EMO_APTO_VIGENTE`, `SCTR`, `CARNET_RETCC`…), `EstadoDocumentoChecklist` = `CONFORME`, `PENDIENTE`, `OBSERVADO`, `NO_APLICA`. | SOMA actualiza el estado de `EMO_APTO_VIGENTE` y `SCTR`. No agrega tipos nuevos. |
| Obra, riesgo y SCTR | `Proyecto` (`nivel_riesgo`, `tasa_sctr_salud`, `tasa_sctr_pension`, `estado_obra` ∈ `EN_EJECUCION`/`PARALIZADA`/`LIQUIDADA`). `proyecto_responsable` con `RolResponsableObra.SUPERVISOR_SSOMA` (V47). | Toda operación de campo cuelga de `proyecto(id)`. El supervisor SSOMA de la obra se propone como firmante por defecto. |
| Trabajadores de una obra | `contrato_cc.proyecto_id` (V42) + `contrato` (`activo`, `fecha_inicio`, `fecha_fin`). | "Trabajador activo en la obra" = `contrato_cc.proyecto_id = obra`, `contrato.activo = true`, `fecha_inicio <= hoy` y (`fecha_fin` nulo o `>= hoy`). Esta regla se implementa **una sola vez** en un repositorio o servicio de `soma` y se reutiliza. |
| Inventario de EPP | `material` (V49: `codigo`, `familia`, `stock_minimo`, `critico`), `almacen_obra`, `vale_salida`, `movimiento_almacen`. Servicio: `InterfazAlmacenService.crearVale(Long proyectoId, ValeSalidaRequest)`; `ValeSalidaRequest` exige `partidaId` y al menos un ítem. | Los EPP son registros de `material` marcados con `es_epp`. El descuento de stock **siempre** pasa por `crearVale`; nunca se insertan movimientos a mano. |
| Partidas | `partida_presupuesto` (V48). | IPERC, ATS, PETAR y eventos referencian `partida_presupuesto(id)` de forma opcional. |
| Cuadrillas, equipos y subcontratos | `cuadrilla_obra`, `cuadrilla_miembro` (V54); `asignacion_equipo`, `imputacion_equipo`, `subcontrato_obra` (V53). | Las cuadrillas precargan asistentes. El pre-uso cuelga de `asignacion_equipo`. Las inspecciones a terceros cuelgan de `subcontrato_obra`. |
| Horas trabajadas | `tareo_cc_dia` (V42): `horas_ord`, `he60`, `he100`, `codigo` (`LAB`, `F`, `DSC`, `FE`, `DM`, `LIC`), banderas `altura`, `cota_cero`, etc. Pertenece a `periodo_cc` (estado `BORRADOR`/`CERRADO`) de una obra. `asistencia_registro` (V17): `horas_trabajadas` por persona y contrato, **sin obra**. | HHT por obra = solo `tareo_cc_dia`. HHT corporativa = `tareo_cc_dia` + `asistencia_registro`, excluyendo contratos que tengan `contrato_cc` para no contar dos veces. |
| Archivos | `documento_obra` (V52, `proyecto_id NOT NULL`), `DocumentoObraStorage` / `DiscoDocumentoObraStorage`, enum `TipoDocumentoObra`. | Toda evidencia SOMA es un `documento_obra`. **Consecuencia:** un adjunto exige obra. |
| PDF y Excel | `openhtmltopdf-pdfbox` y `poi-ooxml` ya están en `pom.xml`. Membrete y firma: `NominaDocumentoConfiguracion` (V35). Plantillas HTML en `src/main/resources/templates/`. | Reutilizar el mismo patrón de generación HTML→PDF de onboarding. |
| Alertas de obra | `TransversalObraServiceImpl.listarAlertas(Long proyectoId)`, `AlertaObraDTO`, `SeveridadAlertaObra` (`INFO`, `WARNING`, `CRITICAL`). | SOMA agrega alertas desde un método nuevo invocado por `listarAlertas` (S15). |
| Bitácora y auditoría | `BitacoraObra` (`proyecto`, `fecha`, `texto`, `usuario`). `AuditoriaObraRegistrar.registrar(proyectoId, AccionAuditoriaObra, entidad, entidadId, detalle)`. | Las acciones críticas de SOMA se auditan; agregar valores a `AccionAuditoriaObra` en la entrega que los use. |
| Roles y acceso | `RolUsuario` (`SUPERADMIN`, `ADMIN`, `SUPERVISOR`, `RESIDENTE`, `ALMACEN`), `ck_usuarios_rol` (última versión en V56), `AccesoObraService` (`esGerencia`, `exigirObra`, `exigirGerencia`), `AccesoObraFilter` sobre `/api/proyectos/{id}/**`. Las autorizaciones se hacen en servicios, no en `SecurityConfig`. | Un rol no gerencial accede solo a sus obras de `usuario_proyecto`. `ALMACEN` ya queda bloqueado fuera del kardex. |
| Frontend | `frontend/src/layouts/nav.ts` (grupo `soma` con `children: []`), `frontend/src/router/index.ts` (rutas hijas de `proyectos/:id(\\d+)` y guard `RUTAS_FICHA_NO_ALMACEN`), `ProyectoFichaPage.vue` (pestañas y `tabVisible`), `frontend/src/types/auth.ts`, `frontend/src/services/http.ts` (`apiRequest`, `downloadAuthenticatedFile`, `openAuthenticatedDocument`). | Seguir los patrones de `ProyectoAlmacenPage.vue` y `ProyectoMaterialesPage.vue`. |

---

## 4. Decisiones de diseño ya tomadas

El agente **no** debe reabrir estas decisiones; si una le parece incorrecta, lo reporta al usuario y no la cambia por su cuenta.

1. **Aptitud del EMO.** `soma_emo_examen.aptitud` usa los valores clínicos `APTO`, `APTO_CON_RESTRICCION`, `OBSERVADO`, `NO_APTO`. El enum `AptitudMedica` de `Persona` **no se modifica** y se sincroniza con esta tabla:

   | Último EMO | `persona.aptitud_medica` | Checklist `EMO_APTO_VIGENTE` |
   | :--- | :--- | :--- |
   | `APTO` y no vencido | `APTO` | `CONFORME` |
   | `APTO_CON_RESTRICCION` y no vencido | `OBSERVADO` | `CONFORME` |
   | `OBSERVADO` o `NO_APTO` | `OBSERVADO` | `OBSERVADO` |
   | Cualquiera con `fecha_vencimiento < hoy` | `VENCIDO` | `PENDIENTE` |

   El semáforo (S5) evalúa siempre contra `soma_emo_examen` y la fecha actual, no contra el valor guardado en `Persona`, porque este último solo se recalcula al registrar un EMO.
2. **Rol `INGENIERO_SOMA`.** Se comporta como `RESIDENTE` respecto al acceso por obra (vía `usuario_proyecto`). Además puede usar `/api/soma/**`, que también usa la gerencia. Restringir al ingeniero SOMA a solo la pestaña SOMA queda **fuera de alcance**.
3. **Hallazgo vencido es un estado derivado.** Se guardan los estados `ABIERTO`, `EN_PROCESO` y `LEVANTADO`. "Vencido" se calcula como `estado <> 'LEVANTADO' AND fecha_limite < hoy`.
4. **Nivel de riesgo IPERC.** Probabilidad y severidad van de 1 a 5, y el producto P×S de 1 a 25. Los rangos se definen en una única constante, `NivelRiesgoIperc.desdePuntaje(int)`: 1–4 `TRIVIAL`, 5–8 `TOLERABLE`, 9–12 `MODERADO`, 13–19 `IMPORTANTE`, 20–25 `INTOLERABLE`. Son una propuesta que el Ingeniero SOMA debe validar, por eso viven en un solo lugar.
5. **Descanso médico en tareo.** Solo se escribe en `tareo_cc_dia` (código `DM`) para días que caen dentro de un `periodo_cc` en estado `BORRADOR` de la misma obra. Nunca se tocan periodos `CERRADO`, nunca se crean periodos y nunca se escribe en `asistencia_registro`.
6. **Umbral de comité:** 20 o más trabajadores activos en la obra exigen subcomité; con menos basta un supervisor SST.
7. **Numeración correlativa por obra** (PETAR, eventos): `MAX(numero) + 1` dentro de la transacción, protegido por el `UNIQUE (proyecto_id, numero)`.

---

## 5. Entregas

Formato de cada entrega: **Objetivo → Archivos → Migración → Reglas de negocio → API → Frontend → Tests → Verificación → Definición de terminado.**

### Comandos de verificación comunes

```powershell
# Backend (desde backend/erp)
mvn -q -Dtest=<ClaseTest1>,<ClaseTest2> test
mvn -q -DskipTests compile

# Frontend (desde frontend)
npx vue-tsc --build
```

> `vue-tsc` ya reporta errores **preexistentes** en `ProyectoAlmacenPage.vue` (partida con `id` nulo) y `ProyectoDocumentosPage.vue` (variante `outline` de `Badge`). Se aceptan solo esos; **cualquier error nuevo bloquea la entrega**. No se corrigen los preexistentes salvo que el usuario lo pida.

---

### S0 — Base: rol, paquete y pestaña SOMA

**Objetivo.** Dejar el esqueleto: el rol existe, la ficha de obra muestra una pestaña "SOMA" vacía y hay un endpoint de resumen que responde.

**Archivos**
- `backend/.../usuario/domain/RolUsuario.java`: agregar `INGENIERO_SOMA`.
- `backend/.../construccion/domain/TipoDocumentoObra.java`: agregar `PLAN_SSOMA`, `MATRIZ_IPERC`, `SCTR_POLIZA`, `EVIDENCIA_SOMA`, `MANIFIESTO_AMBIENTAL`, `CERTIFICADO_EMO`.
- Nuevo `soma/controller/SomaObraController.java` con base `/api/proyectos/{proyectoId}/soma` (ya queda protegido por `AccesoObraFilter`).
- Nuevo `soma/controller/SomaCorporativoController.java` con base `/api/soma`.
- Nuevo `soma/support/SomaAcceso.java` con `exigirSomaCorporativo()`: permite `SUPERADMIN`, `ADMIN` e `INGENIERO_SOMA`; los demás reciben 403. Reutiliza `AccesoObraService.usuarioActual()`.
- `frontend/src/types/auth.ts`: agregar `'INGENIERO_SOMA'` al tipo `RolUsuario` y su etiqueta donde se rotulan roles (por ejemplo en `ProyectoFichaPage.vue`, bloque Accesos).
- `frontend/src/router/index.ts`: ruta hija `{ path: 'soma', name: 'proyecto-soma', component: ProyectoSomaPage }` dentro de `proyectos/:id(\\d+)` y `'proyecto-soma'` en `RUTAS_FICHA_NO_ALMACEN`.
- `frontend/src/pages/proyectos/ProyectoFichaPage.vue`: pestaña "SOMA" (oculta para `ALMACEN` mediante `tabVisible`).
- Nuevos `frontend/src/pages/proyectos/ProyectoSomaPage.vue`, `frontend/src/types/soma.ts` y `frontend/src/services/soma/somaService.ts`.

**Migración `V57__soma_base.sql`**

```sql
ALTER TABLE usuarios DROP CONSTRAINT ck_usuarios_rol;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_rol CHECK (rol IN (
    'SUPERADMIN', 'ADMIN', 'SUPERVISOR', 'RESIDENTE', 'ALMACEN', 'INGENIERO_SOMA'
));

ALTER TABLE documento_obra DROP CONSTRAINT ck_documento_obra_tipo;
ALTER TABLE documento_obra ADD CONSTRAINT ck_documento_obra_tipo CHECK (tipo IN (
    'CONTRATO', 'PLANO', 'ACTA', 'VALORIZACION', 'CESION_FACTORING',
    'PLAN_SSOMA', 'MATRIZ_IPERC', 'SCTR_POLIZA', 'EVIDENCIA_SOMA', 'MANIFIESTO_AMBIENTAL', 'CERTIFICADO_EMO'
));
```

**API**
- `GET /api/proyectos/{id}/soma/resumen` devuelve `SomaResumenObraDTO`. En S0 solo trae `proyectoId`, `nombreObra` y `supervisorSsoma` (el nombre del responsable `SUPERVISOR_SSOMA` o `null`). Las entregas siguientes le agregan campos.

**Frontend.** `ProyectoSomaPage.vue` muestra el resumen y un `Tabs` de shadcn-vue sin sub-pestañas todavía. Cada entrega posterior agrega la suya.

**Tests**
- Ampliar `AccesoObraServiceTest`: `INGENIERO_SOMA` con obra asignada pasa `exigirObra`, y sin asignación recibe 403.
- `SomaAccesoTest`: gerencia e `INGENIERO_SOMA` pasan; `RESIDENTE`, `SUPERVISOR` y `ALMACEN` reciben 403.

**Verificación:** `mvn -q -Dtest=AccesoObraServiceTest,SomaAccesoTest test` y `npx vue-tsc --build`.

**Definición de terminado:** la migración se aplica sobre una base en V56; la pestaña SOMA abre y muestra el resumen; `ALMACEN` no ve la pestaña y la ruta lo redirige a Almacén.

**Notas de implementación (S0)**
- El guard de `/api/soma/**` no es una clase `SomaAcceso`: vive en `AccesoObraService.exigirSoma()` y en una rama de `AccesoObraFilter`, igual que el resto de autorizaciones.
- `SomaCorporativoController` se posterga a S1; la rama del filtro ya cierra `/api/soma` desde ahora.
- `ProyectoSomaPage` arranca sin `Tabs`: un listado vacío se ve como una barra inútil. El `Tabs` aparece en S1 con la primera sub-pestaña.
- Para probar el rol en local (no hay UI de usuarios), pegar:
  ```sql
  INSERT INTO usuarios (username, correo, password_hash, rol, activo)
  VALUES ('ingsoma', 'ingsoma@convecsa.pe',
          '$2b$10$Ummi/R/Giwc8a30El2jROerG8zn1qQQNEO3QHJ5VD2IZSXRkRGL5S',
          'INGENIERO_SOMA', TRUE);
  INSERT INTO usuario_proyecto (usuario_id, proyecto_id)
  SELECT u.id, p.id FROM usuarios u, proyecto p
  WHERE u.username = 'ingsoma' AND p.codigo = 'OB001';
  ```
  Contraseña: `Convecsa2026!`.

---

### S1 — Historial EMO y sincronización con `Persona`

**Objetivo.** Registrar exámenes médicos ocupacionales con historial y mantener sincronizados `Persona` y el checklist.

**Archivos:** `soma/domain/TipoEmo`, `soma/domain/AptitudEmo`, `soma/entity/SomaEmoExamen`, su repositorio, `soma/service/SomaEmoService` y su implementación, DTOs `SomaEmoRequest` / `SomaEmoDTO`. Agregar `REGISTRAR_EMO_SOMA` a `AccionAuditoriaObra`.

**Migración `V58__soma_emo.sql`**

```sql
CREATE TABLE soma_emo_examen (
    id                BIGSERIAL PRIMARY KEY,
    persona_id        BIGINT        NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    proyecto_id       BIGINT        REFERENCES proyecto (id) ON DELETE SET NULL,
    tipo_emo          VARCHAR(30)   NOT NULL,
    clinica_nombre    VARCHAR(160)  NOT NULL,
    clinica_ruc       VARCHAR(11),
    medico_evaluador  VARCHAR(160),
    cmp_medico        VARCHAR(30),
    fecha_examen      DATE          NOT NULL,
    fecha_vencimiento DATE          NOT NULL,
    aptitud           VARCHAR(30)   NOT NULL,
    restricciones     VARCHAR(2000),
    interconsultas    VARCHAR(1000),
    observaciones     VARCHAR(1000),
    documento_obra_id BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_emo_tipo CHECK (tipo_emo IN (
        'INGRESO', 'PERIODICO', 'RETIRO', 'CAMBIO_PUESTO', 'REINCORPORACION'
    )),
    CONSTRAINT ck_soma_emo_aptitud CHECK (aptitud IN (
        'APTO', 'APTO_CON_RESTRICCION', 'OBSERVADO', 'NO_APTO'
    )),
    CONSTRAINT ck_soma_emo_fechas CHECK (fecha_vencimiento >= fecha_examen),
    CONSTRAINT ck_soma_emo_doc_requiere_obra CHECK (documento_obra_id IS NULL OR proyecto_id IS NOT NULL)
);

CREATE INDEX idx_soma_emo_persona ON soma_emo_examen (persona_id, fecha_examen DESC);
CREATE INDEX idx_soma_emo_vencimiento ON soma_emo_examen (fecha_vencimiento);
```

**Reglas de negocio**
1. Al registrar un EMO, el "último EMO" de la persona es el de mayor `fecha_examen`; si hay empate, gana el mayor `id`. Solo el último EMO actualiza `Persona` y el checklist, según la tabla de la decisión 1.
2. `persona.restricciones_medicas` se reemplaza por las `restricciones` del último EMO; si viene vacío, queda `null`.
3. Si la persona no tiene fila `EMO_APTO_VIGENTE` en el checklist, se crea con criticidad `CRITICO`.
4. Un EMO registrado con fecha anterior al último existente se guarda en el historial pero no sincroniza.
5. El certificado (CAMO) se sube por el flujo existente de documentos de obra con tipo `CERTIFICADO_EMO`; el request solo recibe `documentoObraId`. Si no hay obra, no hay adjunto.
6. Se audita con `REGISTRAR_EMO_SOMA` solo cuando hay `proyectoId`, porque la auditoría exige obra.

**API**
- `GET /api/soma/emos?personaId=&vencenAntesDe=`: listado corporativo.
- `POST /api/soma/emos`: registro corporativo.
- `GET /api/proyectos/{id}/soma/emos`: EMOs vinculados a la obra.
- `POST /api/proyectos/{id}/soma/emos`: registro desde la obra, con `proyectoId` fijado.

**Frontend.** Sub-pestaña "Vigilancia médica" en `ProyectoSomaPage`. Contiene una tabla del historial y un formulario: persona mediante `Combobox`, tipo y aptitud mediante `<select>`, fechas, y restricciones mediante `Textarea`.

**Tests (`SomaEmoServiceImplTest`, con Mockito):**
- La tabla de mapeo completa (cuatro aptitudes más el caso vencido).
- Un EMO más antiguo no pisa a `Persona`.
- La fila del checklist se crea si no existe.
- La auditoría solo se registra con obra.

**Verificación:** `mvn -q -Dtest=SomaEmoServiceImplTest test` y `npx vue-tsc --build`.

**Definición de terminado:** registrar un EMO `APTO` vigente deja a la persona en `APTO` y `EMO_APTO_VIGENTE` en `CONFORME`; registrar luego un `NO_APTO` los cambia a `OBSERVADO`.

**Notas de implementación (S1)**
- El selector de persona en la pestaña de obra usa `GET /api/proyectos/{id}/personal` (`listarPersonalObra`), no el padrón completo.
- El CAMO se sube desde el mismo formulario con `subirDocumentoObra(..., 'CERTIFICADO_EMO')` y el request solo recibe `documentoObraId`.
- El checklist `EMO_APTO_VIGENTE` se escribe sobre el agregado `Persona` (`addDocumentoChecklist`); no pasa por `PersonaMapper.applyChecklist`, que borraría filas ausentes del request.
- `SomaCorporativoController` queda en `/api/soma` (el filtro de S0 ya cubría esa ruta).
- `mapear` es un método estático público del impl para cubrir la tabla de la decisión 1 en el test.

---

### S2 — Pólizas SCTR mensuales por obra

**Objetivo.** Registrar la póliza SCTR (salud y pensión) del mes por obra con su nómina de asegurados, y reflejar la cobertura en el checklist.

**Migración `V59__soma_sctr.sql`**

```sql
CREATE TABLE soma_sctr_poliza (
    id                    BIGSERIAL PRIMARY KEY,
    proyecto_id           BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    anio                  INTEGER        NOT NULL,
    mes                   INTEGER        NOT NULL,
    aseguradora_salud     VARCHAR(120)   NOT NULL,
    numero_poliza_salud   VARCHAR(60)    NOT NULL,
    aseguradora_pension   VARCHAR(120)   NOT NULL,
    numero_poliza_pension VARCHAR(60)    NOT NULL,
    fecha_inicio          DATE           NOT NULL,
    fecha_fin             DATE           NOT NULL,
    monto_prima_salud     NUMERIC(14, 2) NOT NULL DEFAULT 0,
    monto_prima_pension   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    documento_obra_id     BIGINT         REFERENCES documento_obra (id) ON DELETE SET NULL,
    estado                VARCHAR(20)    NOT NULL DEFAULT 'VIGENTE',
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_sctr_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_soma_sctr_estado CHECK (estado IN ('VIGENTE', 'ANULADA')),
    CONSTRAINT ck_soma_sctr_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT ck_soma_sctr_primas CHECK (monto_prima_salud >= 0 AND monto_prima_pension >= 0),
    CONSTRAINT uq_soma_sctr_obra_mes UNIQUE (proyecto_id, anio, mes)
);

CREATE TABLE soma_sctr_nomina (
    id               BIGSERIAL PRIMARY KEY,
    poliza_id        BIGINT      NOT NULL REFERENCES soma_sctr_poliza (id) ON DELETE CASCADE,
    persona_id       BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    incluido_salud   BOOLEAN     NOT NULL DEFAULT TRUE,
    incluido_pension BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_soma_sctr_nomina_persona UNIQUE (poliza_id, persona_id)
);

CREATE INDEX idx_soma_sctr_nomina_persona ON soma_sctr_nomina (persona_id);
```

**Reglas de negocio**
1. "Cubierto en el mes" significa que la persona figura en la nómina de una póliza `VIGENTE` de esa obra y mes, con `incluido_salud` e `incluido_pension` en `true`.
2. Al guardar la nómina, los trabajadores cubiertos pasan `SCTR` a `CONFORME`, y los activos en la obra que no figuran pasan a `PENDIENTE`. Se usa la regla única de "trabajador activo en la obra" del mapa de reutilización.
3. `GET .../sctr/sugerir-nomina?anio=&mes=` devuelve los trabajadores activos de la obra para precargar la nómina, sin guardar nada.
4. Se audita con `REGISTRAR_SCTR_OBRA`.

**API:** `GET`/`POST /api/proyectos/{id}/soma/sctr`, `PUT /api/proyectos/{id}/soma/sctr/{polizaId}/nomina`, `POST .../sctr/{polizaId}/anular`, `GET .../sctr/sugerir-nomina`.

**Frontend.** Sub-pestaña "SCTR" con la lista de pólizas por mes, un formulario de póliza y la nómina en una `Table`. La nómina permite precargar trabajadores y quitar filas.

**Tests (`SomaSctrServiceImplTest`):**
- Rechaza una póliza duplicada para el mismo mes.
- Actualiza el checklist de cubiertos y no cubiertos.
- La sugerencia respeta la vigencia del contrato.

**Definición de terminado:** con una póliza vigente y nómina guardada, los cubiertos quedan `CONFORME` y un trabajador activo excluido queda `PENDIENTE`.

**Notas de implementación (S2)**
- La regla única de "trabajador activo en la obra" vive en `SomaTrabajadorObraRepository.buscarActivos` / `buscarActivosEnMes`: solapa el contrato con el mes (`fecha_inicio <= fin de mes` y `fecha_fin` nula o `>= inicio de mes`).
- El checklist `SCTR` solo se sincroniza si la póliza es `VIGENTE` y corresponde al mes en curso. Guardar un mes pasado no toca a `Persona`.
- `ChecklistPersonaActualizador` en `soma/support` es el único escritor del checklist desde SOMA; S1 quedó refactorizado para usarlo.
- El adjunto de la póliza se sube con `subirDocumentoObra(..., 'SCTR_POLIZA')`, igual que el CAMO en S1.
- La obra local `0000` no tiene `contrato_cc`; la sugerencia de nómina sale vacía y el caso cubierto/excluido se cubre con `SomaSctrServiceImplTest`.

---

### S3 — Entrega de EPP integrada con almacén

**Objetivo.** Entregar EPP a un trabajador descontando stock del almacén de la obra mediante el vale de salida existente.

**Migración `V60__soma_epp.sql`**

```sql
ALTER TABLE material
    ADD COLUMN es_epp BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN tipo_epp VARCHAR(40),
    ADD COLUMN vida_util_dias INTEGER,
    ADD COLUMN norma_certificacion VARCHAR(80),
    ADD CONSTRAINT ck_material_vida_util CHECK (vida_util_dias IS NULL OR vida_util_dias > 0),
    ADD CONSTRAINT ck_material_tipo_epp CHECK (tipo_epp IS NULL OR tipo_epp IN (
        'CABEZA', 'VISUAL', 'AUDITIVO', 'RESPIRATORIO', 'MANOS', 'PIES',
        'CAIDAS_ALTURA', 'CUERPO_ROPA', 'COLECTIVO', 'EMERGENCIA'
    ));

CREATE TABLE soma_entrega_epp (
    id                 BIGSERIAL PRIMARY KEY,
    proyecto_id        BIGINT       NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    persona_id         BIGINT       NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    vale_salida_id     BIGINT       REFERENCES vale_salida (id) ON DELETE SET NULL,
    fecha_entrega      DATE         NOT NULL,
    motivo             VARCHAR(30)  NOT NULL,
    firmado_trabajador BOOLEAN      NOT NULL DEFAULT FALSE,
    entregado_por      VARCHAR(80)  NOT NULL,
    observaciones      VARCHAR(400),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_entrega_epp_motivo CHECK (motivo IN (
        'DOTACION_INICIAL', 'RENOVACION_DESGASTE', 'REPOSICION_PERDIDA', 'TRABAJO_ESPECIAL'
    ))
);

CREATE INDEX idx_soma_entrega_epp_obra_persona ON soma_entrega_epp (proyecto_id, persona_id, fecha_entrega DESC);

CREATE TABLE soma_entrega_epp_item (
    id                         BIGSERIAL PRIMARY KEY,
    entrega_id                 BIGINT         NOT NULL REFERENCES soma_entrega_epp (id) ON DELETE CASCADE,
    material_id                BIGINT         NOT NULL REFERENCES material (id),
    cantidad                   NUMERIC(14, 4) NOT NULL,
    talla                      VARCHAR(20),
    fecha_vencimiento_estimada DATE,
    estado_item                VARCHAR(20)    NOT NULL DEFAULT 'VIGENTE',
    CONSTRAINT ck_soma_epp_item_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_soma_epp_item_estado CHECK (estado_item IN (
        'VIGENTE', 'DEVUELTO', 'DETERIORADO', 'EXTRAVIADO'
    ))
);
```

**Archivos adicionales**
- `Material.java`, `MaterialDTO`, `MaterialRequest`: campos `esEpp`, `tipoEpp`, `vidaUtilDias` y `normaCertificacion`.
- `ProyectoMaterialesPage.vue` (catálogo): permite marcar un material como EPP y editar esos campos.
- Agregar `ENTREGAR_EPP_SOMA` a `AccionAuditoriaObra`.

**Reglas de negocio**
1. Todos los ítems deben ser materiales con `es_epp = true` y `activo = true`.
2. `fecha_vencimiento_estimada = fecha_entrega + vida_util_dias`, o `null` si el material no tiene vida útil.
3. Con `descontarAlmacen = true` (valor por defecto), el request exige `partidaId`, que es la partida de seguridad elegida por el usuario. El servicio arma un `ValeSalidaRequest` y llama a `InterfazAlmacenService.crearVale(proyectoId, request)` **en la misma transacción**. Si falla (por ejemplo, stock insuficiente), no se guarda la entrega. El id del vale queda en `vale_salida_id`.
4. Con `descontarAlmacen = false`, se registra la entrega sin vale. Sirve para EPP provisto por terceros.
5. `PUT .../epps/items/{itemId}/estado` cambia `estado_item` (devolución o deterioro). En esta entrega **no** reingresa stock.
6. El kardex individual en PDF usa una plantilla HTML nueva en `templates/soma/kardex-epp.html` con el membrete de `NominaDocumentoConfiguracion`.

**API:** `GET`/`POST /api/proyectos/{id}/soma/epps`, `PUT .../epps/items/{itemId}/estado`, `GET .../epps/kardex/{personaId}/pdf`, `GET .../epps/materiales` (EPP activos con su stock en el almacén de la obra).

**Frontend.** Sub-pestaña "EPP" con un formulario de entrega: persona, partida, motivo, ítems con material, cantidad y talla, y la casilla "Descontar de almacén". También muestra el historial por trabajador y un botón para descargar el kardex mediante `downloadAuthenticatedFile`.

**Tests (`SomaEppServiceImplTest`):**
- Rechaza un material que no es EPP.
- Calcula la fecha de vencimiento.
- Llama a `crearVale` con la partida y los ítems correctos.
- No llama a `crearVale` si `descontarAlmacen = false`.
- Exige `partidaId` cuando se descuenta.

**Definición de terminado:** una entrega con descuento genera un vale visible en la pestaña Almacén y reduce el saldo en el kardex.

**Notas de implementación (S3)**
- `entregadoPor` se toma de `AuditoriaObraRegistrar.usuarioActual()`; el formulario no lo pide.
- El selector de persona reutiliza el personal de la obra (`listarPersonalObra`), igual que S1.
- La partida sale del presupuesto vigente (`obtenerPresupuesto`), igual que el vale de Almacén.
- Con `descontarAlmacen = true` (default) se llama `InterfazAlmacenService.crearVale` en la misma transacción; si el stock no alcanza, no se guarda la entrega. Con `false` no hay vale (EPP de terceros).
- `PUT .../epps/items/{itemId}/estado` no reingresa stock.
- El kardex PDF usa `templates/soma/kardex-epp.html` y `BoletaPdfService.convertir`, con membrete de `NominaDocumentoConfiguracion`.
- `es_epp` es un flag global del catálogo de empresa, no por obra. En local se verificó: entrega con descuento creó vale nº 1 y el stock bajó de 10 a 8.

---

### S4 — Capacitaciones e inducción de hombre nuevo

**Objetivo.** Registrar inducciones, capacitaciones, entrenamientos y simulacros con sus asistentes. Este registro alimenta la habilitación (S5) y el Formato 7 (S16).

**Migración `V61__soma_capacitacion.sql`**

```sql
CREATE TABLE soma_capacitacion (
    id                BIGSERIAL PRIMARY KEY,
    proyecto_id       BIGINT        REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo              VARCHAR(40)   NOT NULL,
    tema              VARCHAR(240)  NOT NULL,
    fecha             DATE          NOT NULL,
    horas_duracion    NUMERIC(5, 2) NOT NULL,
    expositor_nombre  VARCHAR(160)  NOT NULL,
    expositor_entidad VARCHAR(160),
    puntaje_simulacro NUMERIC(5, 2),
    observaciones     VARCHAR(1000),
    evidencia_doc_id  BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_capacitacion_tipo CHECK (tipo IN (
        'INDUCCION_HOMBRE_NUEVO', 'CAPACITACION_PROGRAMADA',
        'ENTRENAMIENTO_ESPECIFICO_ALTO_RIESGO', 'SIMULACRO_EMERGENCIA'
    )),
    CONSTRAINT ck_soma_capacitacion_horas CHECK (horas_duracion > 0),
    CONSTRAINT ck_soma_capacitacion_doc_requiere_obra CHECK (evidencia_doc_id IS NULL OR proyecto_id IS NOT NULL)
);

CREATE INDEX idx_soma_capacitacion_obra ON soma_capacitacion (proyecto_id, fecha DESC);

CREATE TABLE soma_capacitacion_asistente (
    id              BIGSERIAL PRIMARY KEY,
    capacitacion_id BIGINT       NOT NULL REFERENCES soma_capacitacion (id) ON DELETE CASCADE,
    persona_id      BIGINT       NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    nota            NUMERIC(5, 2),
    aprobado        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_soma_cap_asistente_nota CHECK (nota IS NULL OR nota BETWEEN 0 AND 20),
    CONSTRAINT uq_soma_capacitacion_asistente UNIQUE (capacitacion_id, persona_id)
);

CREATE INDEX idx_soma_cap_asistente_persona ON soma_capacitacion_asistente (persona_id);
```

**Reglas de negocio**
1. Si viene `nota`, se calcula `aprobado = nota >= 11`; si no viene, se respeta el valor enviado.
2. Una `INDUCCION_HOMBRE_NUEVO` debe durar al menos 4 horas (G.050); con menos, se rechaza.
3. "Inducción vigente" de una persona en una obra significa que existe una inducción de hombre nuevo **de esa obra** con la persona aprobada.
4. Se pueden precargar asistentes desde una `cuadrilla_obra` (sus miembros activos).

**API:** `GET`/`POST /api/proyectos/{id}/soma/capacitaciones` y `GET /api/soma/capacitaciones?anio=`, que también lista las corporativas (sin obra).

**Frontend.** Sub-pestaña "Capacitaciones" con lista, formulario y asistentes (persona o cuadrilla, nota).

**Tests (`SomaCapacitacionServiceImplTest`):**
- La regla de nota 11.
- Rechaza una inducción de menos de 4 horas.
- La precarga desde cuadrilla.

**Notas de implementación (S4)**
- Asistentes se eligen del personal de la obra (`listarPersonalObra`). La precarga es `GET .../capacitaciones/precargar-cuadrilla?cuadrillaId=` (miembro activo = fila en `cuadrilla_miembro`).
- Evidencia opcional: `subirDocumentoObra(..., 'EVIDENCIA_SOMA')` y `evidenciaDocId` en el POST.
- Inducción vigente para S5: `SomaCapacitacionRepository.existsByProyectoIdAndTipoAndAsistentesPersonaIdAndAsistentesAprobadoTrue`.
- `GET /api/soma/capacitaciones?anio=` lista el año completo (obras + corporativas). El POST corporativo y `/soma/capacitaciones` quedan para S17.
- La obra local `0000` no tiene `contrato_cc`; el combobox de persona y las cuadrillas salen vacíos. La inducción de 4 h y la nota 11 se verificaron por API con `personaId=1`.

---

### S5 — Semáforo de habilitación (pase de ingreso a obra)

**Objetivo.** Mostrar, para cada trabajador activo de la obra, si está habilitado para ingresar y por qué no.

**Sin migración.** Servicio `SomaHabilitacionService.evaluarObra(proyectoId, fecha)`.

**Reglas.** Un trabajador está `HABILITADO` solo si cumple **todos** los criterios; si no, queda `BLOQUEADO` con la lista de motivos.

| Criterio | Fuente | Falla si |
| :--- | :--- | :--- |
| EMO | Último `soma_emo_examen` de la persona | No existe, `fecha_vencimiento < fecha`, o aptitud ∈ {`OBSERVADO`, `NO_APTO`} |
| SCTR | `soma_sctr_nomina` del mes de `fecha` en la obra | No cubierto (regla S2) |
| Inducción | `soma_capacitacion` (regla S4) | No tiene inducción aprobada en la obra |
| EPP inicial | `soma_entrega_epp` | No tiene entrega con motivo `DOTACION_INICIAL` en la obra |
| RETCC | Checklist `CARNET_RETCC` | Estado distinto de `CONFORME` o `NO_APLICA` |

Un EMO `APTO_CON_RESTRICCION` habilita, pero el DTO expone las restricciones para que se vean en pantalla.

**API:** `GET /api/proyectos/{id}/soma/habilitacion?fecha=`, que devuelve una fila por trabajador con `estado`, `motivos[]` y `restricciones`. El resumen de S0 agrega `habilitados`, `bloqueados` y `emosPorVencer30Dias`.

**Frontend.** Sub-pestaña "Habilitación" como primera pestaña. Es una `Table` con un `Badge` verde o rojo por fila, los motivos en texto y un filtro por estado.

**Tests (`SomaHabilitacionServiceImplTest`):** un caso por criterio que falla, un caso totalmente habilitado y un caso de restricción que habilita.

**Definición de terminado:** el semáforo coincide con los datos cargados en S1–S4.

**Notas de implementación (S5)**
- Población = `buscarActivos(proyectoId, fecha, fecha)` (el día de `fecha`, no el mes entero), deduplicada por `personaId`. Si `fecha` no viene en el query, se usa `LocalDate.now()`.
- SCTR se evalúa del **mes de `fecha`** (póliza `VIGENTE` con `incluidoSalud && incluidoPension`). El EMO es el último examen **global** de la persona, no por obra.
- RETCC falla si no hay fila de checklist o si el estado no es `CONFORME`/`NO_APLICA`. `APTO_CON_RESTRICCION` habilita y el DTO expone `restricciones`.
- La obra local `0000` no tiene `contrato_cc`; `GET .../habilitacion` devuelve lista vacía. Los 5 criterios, el caso habilitado y la restricción se cubrieron con `SomaHabilitacionServiceImplTest`.

---

### S6 — Matriz IPERC por partida

**Migración `V62__soma_iperc.sql`**

```sql
CREATE TABLE soma_iperc_fila (
    id                     BIGSERIAL PRIMARY KEY,
    proyecto_id            BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    partida_id             BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    actividad_tarea        VARCHAR(240)  NOT NULL,
    puesto_trabajo         VARCHAR(120)  NOT NULL,
    rutinario              BOOLEAN       NOT NULL DEFAULT TRUE,
    tipo_peligro           VARCHAR(30)   NOT NULL,
    peligro                VARCHAR(400)  NOT NULL,
    riesgo                 VARCHAR(400)  NOT NULL,
    consecuencia           VARCHAR(400)  NOT NULL,
    requisito_legal        VARCHAR(200),
    probabilidad_inicial   INTEGER       NOT NULL,
    severidad_inicial      INTEGER       NOT NULL,
    nivel_riesgo_inicial   VARCHAR(20)   NOT NULL,
    control_eliminacion    VARCHAR(500),
    control_sustitucion    VARCHAR(500),
    control_ingenieria     VARCHAR(500),
    control_administrativo VARCHAR(500),
    control_epp            VARCHAR(500),
    probabilidad_residual  INTEGER       NOT NULL,
    severidad_residual     INTEGER       NOT NULL,
    nivel_riesgo_residual  VARCHAR(20)   NOT NULL,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_iperc_tipo_peligro CHECK (tipo_peligro IN (
        'LOCATIVO', 'MECANICO', 'ELECTRICO', 'FISICO', 'QUIMICO', 'ERGONOMICO', 'PSICOSOCIAL', 'BIOLOGICO'
    )),
    CONSTRAINT ck_soma_iperc_nivel_ini CHECK (nivel_riesgo_inicial IN ('TRIVIAL', 'TOLERABLE', 'MODERADO', 'IMPORTANTE', 'INTOLERABLE')),
    CONSTRAINT ck_soma_iperc_nivel_res CHECK (nivel_riesgo_residual IN ('TRIVIAL', 'TOLERABLE', 'MODERADO', 'IMPORTANTE', 'INTOLERABLE')),
    CONSTRAINT ck_soma_iperc_prob_ini CHECK (probabilidad_inicial BETWEEN 1 AND 5),
    CONSTRAINT ck_soma_iperc_sev_ini CHECK (severidad_inicial BETWEEN 1 AND 5),
    CONSTRAINT ck_soma_iperc_prob_res CHECK (probabilidad_residual BETWEEN 1 AND 5),
    CONSTRAINT ck_soma_iperc_sev_res CHECK (severidad_residual BETWEEN 1 AND 5)
);

CREATE INDEX idx_soma_iperc_proyecto ON soma_iperc_fila (proyecto_id, partida_id);
```

**Reglas**
1. El backend calcula los niveles con `NivelRiesgoIperc.desdePuntaje(P×S)` (decisión 4). Los valores de nivel que mande el cliente se ignoran.
2. Al menos un control debe tener texto.
3. El nivel residual no puede ser mayor que el inicial.
4. Las filas no se borran: se desactivan con `activo = false`.
5. La partida, si viene, debe pertenecer a la obra.

**API:** `GET /api/proyectos/{id}/soma/iperc?partidaId=&incluirInactivos=`, `POST`, `PUT /{filaId}` y `POST /{filaId}/desactivar`.

**Frontend.** Sub-pestaña "IPERC" con la tabla filtrable por partida y un `Badge` de color por nivel. El formulario muestra el nivel calculado en vivo con la misma tabla de rangos, replicada en `types/soma.ts`.

**Tests (`NivelRiesgoIpercTest`, `SomaIpercServiceImplTest`):**
- Los bordes de cada rango (4/5, 8/9, 12/13, 19/20).
- Rechaza un residual mayor que el inicial.
- Rechaza una fila sin controles.

**Notas de implementación (S6)**
- Los niveles los calcula solo el backend con `NivelRiesgoIperc.desdePuntaje(P×S)`; el request no lleva nivel. El residual se compara por ordinal del enum (`TRIVIAL` < … < `INTOLERABLE`).
- La partida es opcional; si viene, debe ser de la misma obra (`partida.version.proyecto.id`). No se exige presupuesto vigente.
- En local, `0000` puede no tener `partida_presupuesto`; el POST sin partida y el GET/`desactivar` se verificaron por API. El tab filtra por partida y puede incluir inactivos.

---

### S7 — Charla diaria y ATS

**Migración `V63__soma_charla_ats.sql`**

```sql
CREATE TABLE soma_charla_ats (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    cuadrilla_id         BIGINT        REFERENCES cuadrilla_obra (id) ON DELETE SET NULL,
    partida_id           BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    fecha                DATE          NOT NULL,
    hora_inicio          TIME          NOT NULL,
    duracion_minutos     INTEGER       NOT NULL DEFAULT 10,
    tema_charla          VARCHAR(240)  NOT NULL,
    expositor_persona_id BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    supervisor_ssoma_id  BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    pasos_ats_json       JSONB         NOT NULL DEFAULT '[]'::jsonb,
    epp_verificado       BOOLEAN       NOT NULL DEFAULT FALSE,
    herramientas_verif   BOOLEAN       NOT NULL DEFAULT FALSE,
    observaciones        VARCHAR(1000),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_charla_duracion CHECK (duracion_minutos > 0)
);

CREATE INDEX idx_soma_charla_ats_obra_fecha ON soma_charla_ats (proyecto_id, fecha DESC);

CREATE TABLE soma_charla_asistente (
    id              BIGSERIAL PRIMARY KEY,
    charla_id       BIGINT       NOT NULL REFERENCES soma_charla_ats (id) ON DELETE CASCADE,
    persona_id      BIGINT       NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    habilitado_soma BOOLEAN      NOT NULL,
    observacion     VARCHAR(240),
    CONSTRAINT uq_soma_charla_asistente UNIQUE (charla_id, persona_id)
);
```

**Reglas**
1. `pasos_ats_json` es una lista de objetos `{ "paso": string, "peligros": string, "controles": string }` (record Java `PasoAts`), con al menos un paso.
2. `habilitado_soma` lo calcula el backend con S5 en la fecha de la charla, como foto del momento. Si hay asistentes bloqueados, la charla se guarda igual y la respuesta incluye una advertencia con sus nombres.
3. El supervisor SSOMA se propone desde `proyecto_responsable` si no viene en el request.
4. Se pueden precargar asistentes desde la cuadrilla.

**API:** `GET`/`POST /api/proyectos/{id}/soma/charlas-ats?fecha=`.

**Frontend.** Sub-pestaña "Charla y ATS" con un formulario, los pasos del ATS como filas editables y los asistentes marcados con un `Badge` si están bloqueados. Las advertencias se muestran con `sonner`.

**Tests (`SomaCharlaAtsServiceImplTest`):**
- Rechaza un ATS sin pasos.
- Marca `habilitado_soma = false` cuando corresponde y devuelve la advertencia.

**Notas de implementación (S7)**
- `habilitado_soma` es una foto de S5 (`evaluarObra`) en la fecha de la charla: `true` solo si la fila está `HABILITADO`. Si la persona no está en el semáforo o está `BLOQUEADO`, se guarda igual con `false` y `advertencia` para sonner.
- El supervisor SSOMA se rellena desde `proyecto_responsable` (`SUPERVISOR_SSOMA`) si el request no lo manda.
- En local, la obra `0000` suele no tener `contrato_cc` ni cuadrilla: S5 sale vacío, el asistente queda `habilitadoSoma = false` y hay advertencia. El POST se verificó con `personaId=1` y un paso ATS; el GET `?fecha=` lista la charla.

---

### S8 — PETAR y cruce con tareo

**Migración `V64__soma_petar.sql`**

```sql
CREATE TABLE soma_petar (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero               INTEGER       NOT NULL,
    tipo_trabajo         VARCHAR(30)   NOT NULL,
    partida_id           BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    cuadrilla_id         BIGINT        REFERENCES cuadrilla_obra (id) ON DELETE SET NULL,
    fecha                DATE          NOT NULL,
    hora_inicio          TIME          NOT NULL,
    hora_fin             TIME          NOT NULL,
    ubicacion_frente     VARCHAR(200)  NOT NULL,
    descripcion_trabajo  VARCHAR(1000) NOT NULL,
    checklist_json       JSONB         NOT NULL DEFAULT '{}'::jsonb,
    mediciones_json      JSONB         NOT NULL DEFAULT '{}'::jsonb,
    residente_persona_id BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    supervisor_ssoma_id  BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    estado               VARCHAR(20)   NOT NULL DEFAULT 'BORRADOR',
    autorizado_por       VARCHAR(80),
    autorizado_at        TIMESTAMPTZ,
    documento_obra_id    BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_soma_petar_numero UNIQUE (proyecto_id, numero),
    CONSTRAINT ck_soma_petar_tipo CHECK (tipo_trabajo IN (
        'ALTURA', 'EXCAVACION_ZANJA', 'TRABAJO_CALIENTE', 'ESPACIO_CONFINADO', 'IZAJE_CARGAS', 'ENERGIA_ELECTRICA'
    )),
    CONSTRAINT ck_soma_petar_estado CHECK (estado IN ('BORRADOR', 'AUTORIZADO', 'CERRADO', 'SUSPENDIDO')),
    CONSTRAINT ck_soma_petar_horas CHECK (hora_fin > hora_inicio)
);

CREATE INDEX idx_soma_petar_obra_fecha ON soma_petar (proyecto_id, fecha DESC, tipo_trabajo);

CREATE TABLE soma_petar_trabajador (
    id           BIGSERIAL PRIMARY KEY,
    petar_id     BIGINT      NOT NULL REFERENCES soma_petar (id) ON DELETE CASCADE,
    persona_id   BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    rol_en_tarea VARCHAR(60) NOT NULL DEFAULT 'EJECUTANTE',
    CONSTRAINT uq_soma_petar_trabajador UNIQUE (petar_id, persona_id)
);
```

**Reglas**
1. Transiciones válidas: `BORRADOR → AUTORIZADO → CERRADO`, `AUTORIZADO → SUSPENDIDO` y `SUSPENDIDO → AUTORIZADO`. Cualquier otra transición se rechaza. Solo se edita en `BORRADOR`.
2. **Autorizar** exige:
   - al menos un trabajador;
   - todos los trabajadores `HABILITADO` según S5 en la fecha del permiso;
   - que ninguno tenga un EMO `APTO_CON_RESTRICCION` cuyas restricciones contengan "altura" (sin distinguir mayúsculas ni tildes) cuando el tipo es `ALTURA`;
   - residente y supervisor SSOMA informados.

   Guarda `autorizado_por` (usuario actual) y `autorizado_at`, y audita con `AUTORIZAR_PETAR`.
3. Checklist por tipo: constante `ChecklistPetar.itemsPara(TipoTrabajoPetar)` con los ítems requeridos. Para autorizar, todos deben estar en `true`. Mediciones mínimas: `ESPACIO_CONFINADO` exige `oxigenoPct` y `lelPct`; `IZAJE_CARGAS` y `ALTURA` exigen `vientoKmh`.
4. **Cruce con tareo** (consulta reutilizada en S15): días del `periodo_cc` en `BORRADOR` de la obra donde existe un `tareo_cc_dia` con `altura = true` sin un PETAR `ALTURA` en estado `AUTORIZADO` o `CERRADO` para esa fecha. Lo mismo para `cota_cero = true` con `EXCAVACION_ZANJA`. **No se modifica el servicio de tareo**: es solo una consulta.

**API**
- `GET`/`POST /api/proyectos/{id}/soma/petar` y `PUT /{petarId}`.
- `POST /{petarId}/autorizar`, `/cerrar`, `/suspender` y `/reanudar`.
- `GET .../petar/faltantes-tareo`.
- `GET .../petar/checklist/{tipo}`.

**Frontend.** Sub-pestaña "PETAR" con la lista por fecha, un formulario con checklist dinámico según el tipo, trabajadores mediante `Combobox`, botones de transición según el estado y un bloque "Tareo sin PETAR".

**Tests (`SomaPetarServiceImplTest`):**
- Cada transición válida e inválida.
- Autorizar falla por un trabajador bloqueado, por una restricción de altura o por un checklist incompleto.
- La consulta de faltantes contra datos simulados.

**Notas S8**
- Flyway real: `V65__soma_petar.sql` (S9 ya ocupó V64).
- Checklist keys: `ALTURA` `eppCompleto, arnesCertificado, lineasVida, puntosAnclaje, areaDelimitada, charlaAts`; `EXCAVACION_ZANJA` `eppCompleto, taludesEstables, apuntalamiento, accesoEgreso, serviciosSubterraneos, charlaAts`; `TRABAJO_CALIENTE` `eppCompleto, extintorCercano, areaLibreCombustibles, vigilanciaIncendio, charlaAts`; `ESPACIO_CONFINADO` `eppCompleto, vigiaExterior, ventilacion, rescateListo, atmosferaMedida, charlaAts`; `IZAJE_CARGAS` `eppCompleto, riggerAsignado, eslingasCertificadas, areaIzajeDelimitada, charlaAts`; `ENERGIA_ELECTRICA` `eppCompleto, bloqueoEtiquetado, tensionVerificada, eppDielectrico, charlaAts`.
- Obra `0000` sin `contrato_cc` ni tareo: S5 vacío; POST queda en `BORRADOR`; autorizar 400 por habilitación; `faltantes-tareo` vacío.

---

### S9 — Inspecciones y hallazgos

**Migración `V65__soma_inspeccion_hallazgo.sql`**

```sql
CREATE TABLE soma_inspeccion (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    tipo                 VARCHAR(40)   NOT NULL,
    fecha                DATE          NOT NULL,
    area_frente          VARCHAR(160)  NOT NULL,
    subcontrato_id       BIGINT        REFERENCES subcontrato_obra (id) ON DELETE SET NULL,
    inspector_persona_id BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    puntaje_cumplimiento NUMERIC(5, 2),
    observaciones        VARCHAR(1000),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_inspeccion_tipo CHECK (tipo IN (
        'PLANEADA_MENSUAL', 'INOPINADA_DIARIA', 'EPP', 'EXTINTORES_EMERGENCIA',
        'INSTALACIONES_ELECTRICAS_G050', 'ANDAMIOS_ESCALERAS', 'ORDEN_LIMPIEZA',
        'ALMACEN_MATPEL', 'SUBCONTRATISTA'
    )),
    CONSTRAINT ck_soma_inspeccion_puntaje CHECK (puntaje_cumplimiento IS NULL OR puntaje_cumplimiento BETWEEN 0 AND 100),
    CONSTRAINT ck_soma_inspeccion_subcontrato CHECK (tipo <> 'SUBCONTRATISTA' OR subcontrato_id IS NOT NULL)
);

CREATE INDEX idx_soma_inspeccion_obra ON soma_inspeccion (proyecto_id, fecha DESC);

CREATE TABLE soma_hallazgo (
    id                       BIGSERIAL PRIMARY KEY,
    proyecto_id              BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    inspeccion_id            BIGINT        REFERENCES soma_inspeccion (id) ON DELETE SET NULL,
    clasificacion            VARCHAR(30)   NOT NULL,
    potencial_perdida        VARCHAR(20)   NOT NULL,
    descripcion              VARCHAR(1000) NOT NULL,
    ubicacion                VARCHAR(200),
    accion_correctiva        VARCHAR(1000) NOT NULL,
    responsable_persona_id   BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    fecha_deteccion          DATE          NOT NULL,
    fecha_limite             DATE          NOT NULL,
    fecha_cierre             DATE,
    paralizacion_frente      BOOLEAN       NOT NULL DEFAULT FALSE,
    evidencia_antes_doc_id   BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    evidencia_despues_doc_id BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    estado                   VARCHAR(20)   NOT NULL DEFAULT 'ABIERTO',
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_hallazgo_clasif CHECK (clasificacion IN ('ACTO_SUBESTANDAR', 'CONDICION_SUBESTANDAR')),
    CONSTRAINT ck_soma_hallazgo_potencial CHECK (potencial_perdida IN ('ALTO_GRAVE', 'MEDIO_MODERADO', 'BAJO_LEVE')),
    CONSTRAINT ck_soma_hallazgo_estado CHECK (estado IN ('ABIERTO', 'EN_PROCESO', 'LEVANTADO')),
    CONSTRAINT ck_soma_hallazgo_fechas CHECK (fecha_limite >= fecha_deteccion),
    CONSTRAINT ck_soma_hallazgo_cierre CHECK (estado <> 'LEVANTADO' OR fecha_cierre IS NOT NULL)
);

CREATE INDEX idx_soma_hallazgo_obra_estado ON soma_hallazgo (proyecto_id, estado, fecha_limite);
```

**Reglas**
1. Un hallazgo puede nacer de una inspección o suelto (`inspeccion_id` nulo); S11 también los crea.
2. `paralizacion_frente = true` solo se permite con `potencial_perdida = 'ALTO_GRAVE'`. Al guardarlo, se inserta una fila en `BitacoraObra` con el texto `"[SOMA] Paralización de frente: <ubicación> — <descripción>"` y se audita con `PARALIZAR_FRENTE_SOMA`.
3. **Levantar** exige `evidencia_despues_doc_id` y fija `fecha_cierre = hoy` si no viene. Un hallazgo `LEVANTADO` no se reabre.
4. El DTO expone `vencido` como valor derivado (decisión 3).

**API:** `GET`/`POST /api/proyectos/{id}/soma/inspecciones`, `GET /api/proyectos/{id}/soma/hallazgos?estado=&vencidos=`, `POST .../hallazgos`, `PUT .../hallazgos/{hallazgoId}/en-proceso` y `PUT .../hallazgos/{hallazgoId}/levantar`.

**Frontend.** Sub-pestaña "Inspecciones" con inspecciones y sus hallazgos, un filtro de vencidos y un botón "Levantar" con la evidencia a adjuntar. La subida usa el endpoint existente de documentos de obra con tipo `EVIDENCIA_SOMA`.

**Tests (`SomaInspeccionServiceImplTest`):**
- La paralización inserta la bitácora y exige `ALTO_GRAVE`.
- Levantar exige evidencia.
- El cálculo de `vencido`.

**Notas de implementación (S9)**
- Flyway real: `V64__soma_inspeccion_hallazgo.sql` (siguiente libre tras V63). S8 (PETAR) usará V65.
- `vencido` es derivado (`estado <> LEVANTADO AND fecha_limite < hoy`); no se persiste.
- Hallazgo suelto permitido (`inspeccion_id` nulo). Paralización (`ALTO_GRAVE`) inserta bitácora y audita `PARALIZAR_FRENTE_SOMA`.
- En local, `0000` no exige subcontrato: el POST de inspección usó `INOPINADA_DIARIA`. Levantar exige `EVIDENCIA_SOMA` de la misma obra.

---

### S10 — Check-list pre-uso de equipos

**Migración `V66__soma_preuso_equipo.sql`**

```sql
CREATE TABLE soma_inspeccion_equipo (
    id                   BIGSERIAL PRIMARY KEY,
    proyecto_id          BIGINT         NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    asignacion_equipo_id BIGINT         NOT NULL REFERENCES asignacion_equipo (id) ON DELETE CASCADE,
    operador_persona_id  BIGINT         REFERENCES persona (id) ON DELETE SET NULL,
    fecha                DATE           NOT NULL,
    horometro_lectura    NUMERIC(12, 2),
    checklist_json       JSONB          NOT NULL DEFAULT '{}'::jsonb,
    resultado            VARCHAR(30)    NOT NULL,
    observaciones        VARCHAR(600),
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_insp_equipo_res CHECK (resultado IN (
        'OPERATIVO', 'OPERATIVO_CON_OBSERVACION', 'INOPERATIVO_BLOQUEADO'
    ))
);

CREATE INDEX idx_soma_insp_equipo_asig ON soma_inspeccion_equipo (asignacion_equipo_id, fecha DESC);
```

**Reglas**
1. La asignación debe pertenecer a la obra.
2. Ítems fijos del checklist: `frenos`, `luces`, `alarmaRetroceso`, `extintor`, `cinturon`, `sistemaHidraulico` y `guardasSeguridad`, con valor `true`/`false`.
3. Si algún ítem está en `false`, el resultado no puede ser `OPERATIVO`.
4. Consulta `equipoBloqueadoEn(asignacionId, fecha)`: la usa el frontend de Recursos para mostrar una advertencia (no bloqueo) al imputar horas a un equipo bloqueado ese día. **No se modifica el servicio de imputaciones.**

**API:** `GET`/`POST /api/proyectos/{id}/soma/equipos-preuso` y `GET .../equipos-preuso/bloqueos?fecha=`.

**Frontend.** Sub-pestaña "Pre-uso" en SOMA. En `ProyectoRecursosPage.vue`, al elegir equipo y fecha de imputación, se consulta `bloqueos` y se muestra un aviso con `sonner`.

**Tests (`SomaPreUsoServiceImplTest`):** rechaza un `OPERATIVO` con ítems en falso y rechaza una asignación de otra obra.

**Notas S10**
- Flyway: `V66__soma_preuso_equipo.sql`.
- Checklist keys: `frenos`, `luces`, `alarmaRetroceso`, `extintor`, `cinturon`, `sistemaHidraulico`, `guardasSeguridad`.
- El bloqueo `INOPERATIVO_BLOQUEADO` aplica solo el día de la inspección; la imputación de Recursos no se corta (aviso `sonner`).

---

### S11 — Eventos SST

**Migración `V67__soma_evento_sst.sql`**

```sql
CREATE TABLE soma_evento_sst (
    id                     BIGSERIAL PRIMARY KEY,
    proyecto_id            BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero                 INTEGER       NOT NULL,
    tipo_evento            VARCHAR(40)   NOT NULL,
    gravedad_incapacidad   VARCHAR(30),
    fecha_hora             TIMESTAMPTZ   NOT NULL,
    lugar_exacto           VARCHAR(200)  NOT NULL,
    partida_id             BIGINT        REFERENCES partida_presupuesto (id) ON DELETE SET NULL,
    persona_afectada_id    BIGINT        REFERENCES persona (id) ON DELETE SET NULL,
    subcontrato_id         BIGINT        REFERENCES subcontrato_obra (id) ON DELETE SET NULL,
    descripcion_evento     VARCHAR(2000) NOT NULL,
    parte_cuerpo_lesionada VARCHAR(120),
    naturaleza_lesion      VARCHAR(120),
    agente_causante        VARCHAR(160),
    numero_certificado_dm  VARCHAR(60),
    dias_descanso_medico   INTEGER       NOT NULL DEFAULT 0,
    dias_cargo_estadistico INTEGER       NOT NULL DEFAULT 0,
    fecha_inicio_dm        DATE,
    fecha_fin_dm           DATE,
    causas_inmediatas_json JSONB         NOT NULL DEFAULT '{}'::jsonb,
    causas_basicas_json    JSONB         NOT NULL DEFAULT '{}'::jsonb,
    notificado_mtpe        BOOLEAN       NOT NULL DEFAULT FALSE,
    estado_investigacion   VARCHAR(20)   NOT NULL DEFAULT 'EN_INVESTIGACION',
    informe_doc_id         BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_soma_evento_numero UNIQUE (proyecto_id, numero),
    CONSTRAINT ck_soma_evento_tipo CHECK (tipo_evento IN (
        'INCIDENTE', 'INCIDENTE_PELIGROSO', 'ACCIDENTE_LEVE',
        'ACCIDENTE_INCAPACITANTE', 'ACCIDENTE_MORTAL', 'ENFERMEDAD_OCUPACIONAL'
    )),
    CONSTRAINT ck_soma_evento_gravedad CHECK (
        (tipo_evento = 'ACCIDENTE_INCAPACITANTE' AND gravedad_incapacidad IN ('TOTAL_TEMPORAL', 'PARCIAL_PERMANENTE', 'TOTAL_PERMANENTE'))
        OR (tipo_evento <> 'ACCIDENTE_INCAPACITANTE' AND gravedad_incapacidad IS NULL)
    ),
    CONSTRAINT ck_soma_evento_estado CHECK (estado_investigacion IN ('EN_INVESTIGACION', 'CERRADO')),
    CONSTRAINT ck_soma_evento_dias CHECK (dias_descanso_medico >= 0 AND dias_cargo_estadistico >= 0),
    CONSTRAINT ck_soma_evento_dm CHECK (fecha_fin_dm IS NULL OR fecha_inicio_dm IS NULL OR fecha_fin_dm >= fecha_inicio_dm)
);

CREATE INDEX idx_soma_evento_obra_fecha ON soma_evento_sst (proyecto_id, fecha_hora DESC);

CREATE TABLE soma_evento_hallazgo (
    evento_id   BIGINT NOT NULL REFERENCES soma_evento_sst (id) ON DELETE CASCADE,
    hallazgo_id BIGINT NOT NULL REFERENCES soma_hallazgo (id) ON DELETE CASCADE,
    PRIMARY KEY (evento_id, hallazgo_id)
);
```

**Reglas**
1. Hay que indicar `persona_afectada_id` o `subcontrato_id` en todos los tipos excepto `INCIDENTE`.
2. Si el rango de descanso médico viene informado, `dias_descanso_medico` se calcula como días calendario inclusivos del rango.
3. Causas en JSON (TASC): `causas_inmediatas_json = { "actos": [string], "condiciones": [string] }` y `causas_basicas_json = { "factoresPersonales": [string], "factoresTrabajo": [string] }`.
4. Las medidas correctivas se crean como `soma_hallazgo` (S9) y se enlazan en `soma_evento_hallazgo`. **Cerrar** la investigación exige que todos los hallazgos enlazados estén `LEVANTADO`.
5. `INCIDENTE_PELIGROSO` y `ACCIDENTE_MORTAL` muestran en la UI el aviso "Notificar al MTPE dentro de las 24 h" con la casilla `notificado_mtpe`. No hay integración.
6. **Sincronizar descanso médico** (`POST .../eventos/{eventoId}/sincronizar-dm`): aplica la decisión 5. Para cada día del rango que cae en un `periodo_cc` en `BORRADOR` de la obra, hace un upsert de `tareo_cc_dia` para la persona afectada con `codigo = 'DM'` y horas en 0. Devuelve los días aplicados y los omitidos (sin periodo o periodo cerrado). Se ejecuta por acción explícita del usuario, nunca de forma automática.
7. Se audita con `REGISTRAR_ACCIDENTE_SST` al crear eventos de tipo `ACCIDENTE_*` o `ENFERMEDAD_OCUPACIONAL`.

**API:** `GET`/`POST /api/proyectos/{id}/soma/eventos`, `PUT /{eventoId}`, `POST /{eventoId}/hallazgos`, `POST /{eventoId}/cerrar` y `POST /{eventoId}/sincronizar-dm`.

**Frontend.** Sub-pestaña "Eventos" con la lista, un formulario por secciones (datos, lesión, causas, medidas) y un botón "Sincronizar descanso médico" que muestra los días aplicados y omitidos.

**Tests (`SomaEventoServiceImplTest`):**
- Las validaciones de gravedad y de persona o subcontrato.
- El cálculo de días.
- Cerrar falla con hallazgos abiertos.
- La sincronización solo escribe en periodos `BORRADOR`.

**Notas S11**
- Flyway: `V67__soma_evento_sst.sql`.
- Causas TASC en JSON: inmediatas `actos` / `condiciones`; básicas `factoresPersonales` / `factoresTrabajo`.
- `sincronizar-dm` solo upserta `tareo_cc_dia` con `codigo=DM` y horas 0 en periodos `BORRADOR`; omite `sin periodo` y `periodo cerrado`. No se toca planilla (`CalculoNominaService` / `CalculoNominaCcService`) ni `guardarTareo`.
- Aviso MTPE 24 h en UI para `INCIDENTE_PELIGROSO` y `ACCIDENTE_MORTAL` (casilla `notificadoMtpe`, sin integración).

---

### S12 — Estadísticas HHT, IF, IS e IA

**Sin migración.** `soma/support/SomaEstadisticaCalculator` (funciones puras) y `SomaEstadisticaService` (consultas).

**Fórmulas** (todas con `BigDecimal`, escala 2 y `RoundingMode.HALF_UP`):
- HHT por obra y mes = Σ `tareo_cc_dia.horas_ord + he60 + he100` de los periodos de la obra, filtrando por la `fecha` del día.
- HHT corporativa del mes = HHT de todas las obras + Σ `asistencia_registro.horas_trabajadas` del mes, excluyendo los registros cuyo `contrato_id` tiene `contrato_cc`.
- Accidentes computables = eventos `ACCIDENTE_INCAPACITANTE` + `ACCIDENTE_MORTAL` del periodo.
- Días perdidos = Σ (`dias_descanso_medico` + `dias_cargo_estadistico`) de esos eventos.
- \( IF = \dfrac{\text{accidentes} \times 1\,000\,000}{HHT} \)
- \( IS = \dfrac{\text{días perdidos} \times 1\,000\,000}{HHT} \)
- \( IA = \dfrac{IF \times IS}{1\,000} \)
- Si HHT = 0, los tres índices valen 0 (no se divide por cero).
- Acumulado anual = mismas fórmulas con los totales de enero hasta el mes consultado, no la suma de índices mensuales.
- "Días sin accidente incapacitante" = días desde el último evento computable de la obra hasta hoy.

**API:** `GET /api/proyectos/{id}/soma/estadisticas?anio=` (12 meses más el acumulado) y `GET /api/soma/estadisticas?anio=` (corporativa). El resumen de S0 agrega los indicadores del mes actual.

**Frontend.** Bloque de indicadores en la sub-pestaña "Eventos" y una tabla mensual.

**Tests (`SomaEstadisticaCalculatorTest`):**
- Un caso numérico conocido. Por ejemplo, HHT = 200 000, 1 accidente y 15 días dan IF = 5.00, IS = 75.00 e IA = 0.38.
- HHT = 0.
- El acumulado no es la suma de los índices mensuales.

**Notas S12**
- Sin migración (Flyway sigue en V67).
- Zona `America/Lima` para mes del evento. HHT de obra = solo `tareo_cc_dia`; HHT corporativa = tareo de todas las obras + `asistencia_registro` excluyendo `contrato_cc` (sin doble conteo).
- Accidentes computables: `ACCIDENTE_INCAPACITANTE` y `ACCIDENTE_MORTAL`. El acumulado anual usa totales ene–dic, no la suma de IF/IS/IA mensuales.
- No se toca planilla (`CalculoNominaService` / `CalculoNominaCcService`) ni `guardarTareo`.

---

### S13 — Comité / Subcomité / Supervisor SST

**Migración `V68__soma_comite.sql`**

```sql
CREATE TABLE soma_comite_miembro (
    id             BIGSERIAL PRIMARY KEY,
    proyecto_id    BIGINT      NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    persona_id     BIGINT      NOT NULL REFERENCES persona (id) ON DELETE CASCADE,
    representacion VARCHAR(20) NOT NULL,
    cargo_comite   VARCHAR(30) NOT NULL,
    titular        BOOLEAN     NOT NULL DEFAULT TRUE,
    fecha_inicio   DATE        NOT NULL,
    fecha_fin      DATE,
    activo         BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_soma_comite_rep CHECK (representacion IN ('EMPLEADOR', 'TRABAJADORES')),
    CONSTRAINT ck_soma_comite_cargo CHECK (cargo_comite IN (
        'PRESIDENTE', 'SECRETARIO', 'MIEMBRO', 'SUPERVISOR_SST', 'OBSERVADOR_SINDICAL'
    ))
);

CREATE TABLE soma_comite_acta (
    id          BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT        NOT NULL REFERENCES proyecto (id) ON DELETE CASCADE,
    numero_acta VARCHAR(40)   NOT NULL,
    tipo_sesion VARCHAR(20)   NOT NULL,
    fecha       DATE          NOT NULL,
    agenda      VARCHAR(1000) NOT NULL,
    acuerdos    VARCHAR(3000) NOT NULL,
    acta_doc_id BIGINT        REFERENCES documento_obra (id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_soma_comite_sesion CHECK (tipo_sesion IN ('INSTALACION', 'ORDINARIA', 'EXTRAORDINARIA')),
    CONSTRAINT uq_soma_comite_acta UNIQUE (proyecto_id, numero_acta)
);
```

**Reglas**
1. `GET .../comite/requerimiento` devuelve el número de trabajadores activos y `SUBCOMITE` (20 o más) o `SUPERVISOR_SST` (menos de 20).
2. Con `SUBCOMITE`, se avisa (no se bloquea) si la representación no es paritaria (titulares activos de empleador ≠ titulares activos de trabajadores) o si falta presidente o secretario.
3. Con `SUPERVISOR_SST`, se espera un único miembro activo con cargo `SUPERVISOR_SST`.

**API:** `GET`/`POST /api/proyectos/{id}/soma/comite/miembros`, `POST .../miembros/{miembroId}/baja`, `GET`/`POST .../comite/actas` y `GET .../comite/requerimiento`.

**Frontend.** Sub-pestaña "Comité" con el requerimiento calculado, los miembros, las actas y los avisos.

**Tests (`SomaComiteServiceImplTest`):** el umbral 19/20 y el aviso de paridad.

---

### S14 — Gestión ambiental

**Migración `V69__soma_ambiental.sql`**

```sql
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
```

**Reglas**
1. `PELIGROSO_ROJO` exige EO-RS (RUC y razón social) y número de manifiesto.
2. `cumple_lmp` lo calcula el backend como `valor_medido <= limite_permisible`, excepto `FISICO_ILUMINACION`, donde el límite es un mínimo (`valor_medido >= limite_permisible`).

**API:** `GET`/`POST /api/proyectos/{id}/soma/residuos` y `GET`/`POST /api/proyectos/{id}/soma/monitoreos`.

**Frontend.** Sub-pestaña "Ambiental" con dos tablas y sus formularios.

**Tests (`SomaAmbientalServiceImplTest`):** la validación de residuo peligroso y el cálculo de `cumple_lmp` para iluminación y ruido.

---

### S15 — Alertas SOMA en la ficha de obra

**Objetivo.** Que la campana o el bloque de alertas existente de la obra muestre las alertas SOMA.

**Archivos:** nuevo `soma/service/SomaAlertaService` con `List<AlertaObraDTO> alertas(Long proyectoId)`. En `TransversalObraServiceImpl.listarAlertas` se agrega **una sola línea**, que inyecta el servicio y suma sus alertas. Se reutiliza el builder actual de `AlertaObraDTO`.

| Código | Severidad | Condición |
| :--- | :--- | :--- |
| `SOMA_EMO_VENCIDO` | `CRITICAL` | Algún trabajador activo de la obra con último EMO vencido o sin EMO |
| `SOMA_EMO_POR_VENCER` | `WARNING` | Algún último EMO vence en los próximos 30 días |
| `SOMA_SCTR_SIN_POLIZA` | `CRITICAL` | Obra `EN_EJECUCION` con trabajadores activos y sin póliza `VIGENTE` del mes actual |
| `SOMA_COMITE_REQUERIDO` | `WARNING` | Requerimiento `SUBCOMITE` sin miembros activos, o `SUPERVISOR_SST` sin supervisor activo |
| `SOMA_HALLAZGO_VENCIDO` | `CRITICAL` si alguno es `ALTO_GRAVE`, si no `WARNING` | Hallazgos vencidos (decisión 3) |
| `SOMA_PETAR_FALTANTE_TAREO` | `WARNING` | La consulta de faltantes de S8 devuelve al menos un día |
| `SOMA_EVENTO_ABIERTO` | `INFO` | Eventos con `EN_INVESTIGACION` por más de 30 días |

Cada alerta trae un mensaje con la cantidad (por ejemplo, "3 trabajadores con EMO vencido"). Una alerta cuya entrega aún no existe se omite; esto no aplica si el agente sigue el orden.

**Tests (`SomaAlertaServiceImplTest`):** un caso positivo y uno negativo por código. También se ejecuta el test existente de `TransversalObraServiceImpl` si lo hay, para confirmar que no se rompe.

---

### S16 — Formatos SUNAFIL (R.M. 050-2013-TR)

**Objetivo.** Exportar los registros obligatorios en PDF (OpenHTMLtoPDF) y Excel (Apache POI), con encabezado de empresa desde `NominaDocumentoConfiguracion` y de obra desde `Proyecto`.

| Formato | Contenido | Fuente |
| :--- | :--- | :--- |
| 1 | Accidentes, enfermedades ocupacionales, incidentes peligrosos y otros incidentes | `soma_evento_sst` |
| 2 | Exámenes médico ocupacionales | `soma_emo_examen` |
| 3 | Monitoreo de agentes físicos, químicos, biológicos, psicosociales y disergonómicos | `soma_monitoreo` |
| 4 | Inspecciones internas de SST | `soma_inspeccion` + `soma_hallazgo` |
| 5 | Estadísticas de SST (HHT, IF, IS, IA mensual) | S12 |
| 6 | Equipos de seguridad o emergencia | `soma_entrega_epp` + ítems |
| 7 | Inducción, capacitación, entrenamiento y simulacros | `soma_capacitacion` + asistentes |
| 8 | Auditorías del sistema de gestión de SST | **Fuera de alcance:** se genera solo el formato vacío con encabezado, porque no hay tabla de auditorías SST |

**Archivos:** `soma/support/SomaSunafilPdfService`, `soma/support/SomaSunafilExcelExporter` y plantillas `templates/soma/sunafil-formato-{n}.html`. Implementar los formatos de uno en uno es válido: el agente puede cerrar S16 en sub-entregas (S16.1 = formatos 1 y 2, etc.) y actualizar la tabla de avance con ese detalle.

**API:** `GET /api/soma/formatos-sunafil/{formato}?proyectoId=&anio=&mes=&tipo=pdf|xlsx`. Con `proyectoId` informado, se exige acceso a la obra (`AccesoObraService.exigirObra`); sin él, se exige `exigirSomaCorporativo()`.

**Tests:** `SomaSunafilExcelExporterTest` (abre el `.xlsx` generado con POI y verifica encabezado y filas) y `SomaSunafilPdfServiceTest` (el PDF no está vacío y empieza con `%PDF`).

---

### S17 — Vistas corporativas y menú

**Archivos**
- `frontend/src/layouts/nav.ts`: poblar `children` del grupo `soma`, que hoy está vacío:
  ```ts
  children: [
    { name: 'soma-dashboard', label: 'Tablero SST', path: '/soma/dashboard' },
    { name: 'soma-habilitacion', label: 'Vigilancia médica y SCTR', path: '/soma/habilitacion' },
    { name: 'soma-capacitaciones', label: 'Capacitaciones', path: '/soma/capacitaciones' },
    { name: 'soma-formatos', label: 'Formatos SUNAFIL', path: '/soma/formatos' },
  ],
  ```
  El grupo solo es visible para gerencia e `INGENIERO_SOMA`. Hoy `nav.ts` no filtra por rol: el agente agrega un campo opcional `roles?: RolUsuario[]` al tipo del grupo y lo filtra en el layout que renderiza el menú. No crea un componente de menú nuevo.
- `frontend/src/router/index.ts`: rutas `/soma/*` con guard por rol.
- Nuevas `frontend/src/pages/soma/SomaDashboardPage.vue`, `SomaHabilitacionPage.vue`, `SomaCapacitacionesPage.vue` y `SomaFormatosPage.vue`.

**Contenido**
- Dashboard: indicadores corporativos de S12, días sin accidente, hallazgos abiertos frente a cerrados y obras con alertas SOMA críticas.
- Habilitación: EMOs por vencer en todas las obras visibles para el usuario, con filtro por obra.
- Capacitaciones: listado anual y la regla de mínimo 4 capacitaciones al año (Ley 29783), mostrada como indicador.
- Formatos: obra (opcional), año, mes, formato y tipo, con botón de descarga mediante `downloadAuthenticatedFile`.

**Backend.** `GET /api/soma/dashboard`, que agrega datos de las obras visibles: todas para gerencia, las asignadas en `usuario_proyecto` para `INGENIERO_SOMA`.

**Verificación final:** `mvn -q test` completo en `backend/erp`, `npx vue-tsc --build` sin errores nuevos, y una revisión manual en el navegador (escritorio y móvil) de la pestaña SOMA y de las vistas `/soma/*`.

---

## 6. Fuera de alcance

- Integraciones con SUNAT, AFPnet, MTPE, EsSalud, aseguradoras o clínicas (envío o consulta en línea).
- Firma digital o electrónica. Las firmas son casillas o archivos escaneados en `documento_obra`.
- Aplicación móvil u operación sin conexión.
- Reingreso de EPP devuelto al stock del almacén.
- Restringir al rol `INGENIERO_SOMA` solo a la pestaña SOMA dentro de la obra.
- Auditorías del sistema de gestión SST (solo el Formato 8 vacío).
- Escritura de descanso médico en `asistencia_registro` o en periodos de tareo cerrados.
- Recalcular automáticamente (por tarea programada) la aptitud de `Persona` al vencer un EMO.
- Cambios en el cálculo de planillas.

---

## 7. Protocolo de trabajo del agente por entrega

1. Leer la entrega completa y los archivos existentes que menciona antes de escribir código.
2. Marcar la entrega `En curso` en la tabla de la sección 0.
3. Implementar en este orden: migración, enums y entidades, repositorios, servicio y tests, controlador, tipos y servicio del frontend, y por último la UI.
4. Ejecutar la verificación de la entrega. Si un test falla, se corrige antes de seguir; los tests no se desactivan.
5. Si el código real contradice este documento (un nombre de columna, una firma), **manda el código**. El agente adapta la implementación y deja una nota breve debajo de la entrega con la diferencia encontrada.
6. Marcar la entrega `Hecho` y reportar al usuario en 3 a 5 líneas: qué se hizo, cómo se verificó y qué queda pendiente.
