# Especificaciones Técnicas de Integración: Módulo de Planilla de Construcción Civil (Normativa 2026)

## 1. Contexto para el Agente de Desarrollo
El ERP actual ya cuenta con un módulo funcional de **Planilla para Régimen General (Personal Administrativo)**. El objetivo de este requerimiento es extender las capacidades del ERP para soportar el **Régimen Especial de Construcción Civil (Perú - Normativa 2026)** basándose en la lógica, fórmulas y estructuras contenidas en el archivo Excel adjunto.

**Instrucción principal para el Agente:** 
Inspecciona todas las hojas, celdas ocultas, tablas de parámetros y macros del archivo Excel adjunto para extraer las fórmulas exactas y los códigos de tributos SUNAT. No reemplaces la planilla administrativa existente; crea un flujo paralelo o polimórfico que comparta las entidades base (Trabajadores, Centros de Costo, Exportadores PLAME/AFP) pero que aplique las reglas de negocio específicas de Construcción Civil cuando el trabajador pertenezca a dicho régimen.

---

## 2. Nuevas Entidades y Modificaciones en Base de Datos

### 2.1. Parámetros Generales del Régimen (Tabla Configurable por Año/Pliego)
A diferencia del régimen administrativo (sueldo mensual fijo), Construcción Civil se basa en un **Jornal Básico Diario** según categoría y pliego de reclamos vigente (2026). El sistema debe permitir parametrizar:
* **Valor de la UIT vigente** (Ej. S/ 5,500 para 2026) y validación de alerta para obras > 50 UIT.
* **Categorías de Obrero y Jornal Básico:**
  * Operario (y subcategorías: Operario Especializado, Electromecánico, Soldador, Topógrafo, etc.).
  * Oficial.
  * Peón.
* **Tasas y Constantes 2026 (Extraer valores exactos de la hoja de Parámetros del Excel):**
  * **BUC (Bonificación Unificada de Construcción):** % diferenciado para Operario (32%), Oficial (30%) y Peón (30%) — *verificar tasas especializadas en el Excel*.
  * **BAE (Bonificación por Alta Especialización):** % sobre el jornal básico según especialidad del operario.
  * **Bonificación por Altura:** 8% del jornal básico por cada 4 pisos a partir del 4to piso (o 10 metros).
  * **Bonificación por Altitud:** Monto fijo por día trabajado en zonas > 3,000 m.s.n.m.
  * **Bonificación por Altas Temperaturas / Contacto con Agua / Aguas Servidas / Trabajo Bajo Cota Cero:** Tasas o montos fijos diarios (ej. Altas temperaturas = S/ 4.50/día).
  * **Asignación por Movilidad:** Monto fijo por día efectivamente laborado (equivalente a pasajes urbanos, no computable para beneficios).
  * **Asignación Escolar:** 30 jornales básicos al año por hijo (de 3 a 24 años cursando estudios), pagaderos proporcionalmente por día/semana.
  * **Trabajo en Feriados:** Asignación especial del 25% adicional a la sobretasa de ley en feriados específicos del régimen.
  * **Descuento CONAFOVICER:** 2% sobre el jornal básico + dominical + feriados.
  * **Fondo de Capacitación:** 0.45% sobre el jornal básico.
  * **Aporte Pensionario (AFP / ONP):** Nota crítica: En Construcción Civil, las tasas de AFP/ONP suelen tener una sobretasa por jubilación anticipada (revisar tabla de AFP en el Excel).
  * **EsSalud + SCTR (Salud y Pensión):** Tasas configurables por nivel de riesgo de obra.

### 2.2. Extensión de la Ficha del Trabajador (`Empleados`)
Agregar los siguientes campos condicionales cuando `tipo_regimen == 'CONSTRUCCION_CIVIL'`:
* `categoria_cc` (Enum: Operario, Oficial, Peón).
* `especialidad_bae` (Ninguna, Operador Equipo Mediano, Pesado, Electromecánico, Topógrafo, etc.).
* `afiliado_sindical` (Booleano/Monto para cuota sindical).
* `hijos_escolaridad` (Cantidad de hijos validados entre 3 y 24 años con constancia de estudios).
* `proyecto_asignado_id` y `centro_costo_id` (Distribución de costos en obra).

---

## 3. Módulo de Tareo Diario y Periodicidad de Planilla

El régimen administrativo liquida mensualmente (base 30 días). Construcción Civil se liquida de forma **Semanal, Quincenal o Mensual**, pero su cálculo es estrictamente **Diario / Horario**.

### 3.1. Interfaz y Lógica de Tareo
Crear una grilla o matriz de tareo (similar a la hoja `TAREO` del Excel) vinculada a un Proyecto/Obra y rango de fechas:
* Registro de **horas ordinarias** por cada día de la semana (Lunes a Sábado; el estándar es 48 horas semanales: 8.5 hrs de L-V y 5.5 hrs el Sábado, verificar configuración en Excel).
* Registro de **horas extras** por día separadas en tramos:
  * Horas Extras al 60% (primeras 2 horas, hasta la décima hora del día).
  * Horas Extras al 100% (a partir de la décima hora hasta las 11:00 pm, o domingos/feriados).
* Indicadores diarios (flags o contadores de días en la semana) para condiciones de trabajo:
  * Días con derecho a Altura, Agua, Altas Temperaturas, Cota Cero, Turno Nocturno (bonificación del 25%).
* Gestión de inasistencias, faltas justificadas, descansos médicos y feriados no laborados.

---

## 4. Motor de Cálculo de Planilla (Reglas de Negocio)

El agente debe replicar exactamente las fórmulas de la hoja `PLANILLA` del Excel adjunto:

1. **Jornal Básico Semanal/Periodo:** Basado en las horas ordinarias trabajadas.
2. **Descanso Dominical (DSO) y Feriados:** Proporcional a los días/horas efectivamente trabajados en la semana. Si falta un día sin justificación, pierde 1/6 del dominical.
3. **Cálculo de Bonificaciones Permanentes (BUC y BAE):** Calculadas sobre el Jornal Básico de los días efectivamente laborados (no incluye dominical, salvo disposición contraria en las fórmulas del Excel).
4. **Horas Extras:** Verificar la base de cálculo en el Excel (indemnización sobre jornal básico + BUC según convenio vigente).
5. **Liquidación de Beneficios Sociales Integrados (Pago Semanal/Proporcional):**
   * En Construcción Civil, las vacaciones, gratificaciones y CTS (Indemnización) suelen pagarse o provisionarse proporcionalmente en cada boleta semanal:
     * **Indemnización (CTS):** 15% (12% CTS + 3% sustitución de participación de utilidades) sobre el total de jornales básicos + horas extras simples (revisar base exacta en Excel).
     * **Vacaciones truncas:** 10% sobre el jornal básico + horas extras.
     * **Gratificaciones (Fiestas Patrias / Navidad):** 40 jornales básicos divididos entre 7 meses (Enero-Julio) o 5 meses (Agosto-Diciembre), calculado por día laborado.
     * **Asignación Escolar:** Proporción diaria por cada hijo registrado.
6. **Retenciones del Trabajador:**
   * ONP / AFP (Comisión + Prima + Aporte Obligatorio de Construcción Civil).
   * CONAFOVICER (2%).
   * Fondo de Capacitación (0.45%).
   * Renta de 5ta Categoría (proyección adaptada a ingresos variables semanales).

---

## 5. Consolidación Mensual y Exportadores (PLAME, AFPnet y Contabilidad)

Dado que la planilla de obra se procesa semanal o quincenalmente, pero las declaraciones ante SUNAT y AFP son **mensuales**, el ERP necesita un **Motor de Consolidación Mensual** (replicar la hoja `CONSOLIDADO` y generadores de macros del Excel).

### 5.1. Fusión con Planilla Administrativa
El módulo exportador actual del ERP (que ya genera PLAME y AFPnet para administrativos) no debe romperse. Debe **unificar** en un solo archivo `.txt` o `.rem`/`.jor` tanto a los empleados administrativos como a los obreros de construcción civil consolidados del mes:
* **Consolidación:** Sumarizar las 4 o 5 semanas procesadas en el mes para cada obrero (agrupando por DNI).
* **Mapeo de Conceptos PLAME:** Extraer del Excel los códigos exactos de la Tabla 22 de SUNAT configurados para Construcción Civil (ej. Jornal Básico, Dominical, BUC, Movilidad supeditada a asistencia, Indemnización, CONAFOVICER, etc.).
* **Archivos Planos requeridos (`.txt`):**
  * `.jor` (Jornada laboral: total de horas ordinarias y sobretiempo del mes).
  * `.rem` (Ingresos, descuentos y aportes).
  * `.snl` (Subsidios y días no laborados).
  * Estructura de carga masiva para **AFPnet** (incluyendo el indicador de régimen de construcción civil).
* **Módulo Renta 4ta Categoría (Opcional si aún no está en el ERP):** Importador del `.txt` de Recibos por Honorarios de SUNAT para integrarlo al envío del PLAME.

### 5.2. Boletas de Pago y Asientos Contables
* **Boletas de Pago PDF:** Diseñar una plantilla de boleta específica para Construcción Civil (distinta a la administrativa) que muestre el desglose de horas por día, tarifas unitarias, bonificaciones de obra, reintegros y beneficios sociales semanales, con soporte para envío o descarga masiva.
* **Asiento Contable y Bancos:** Agrupar el costo de la planilla por **Proyecto** y **Centro de Costo** (Mano de Obra Directa / Gastos Generales de Obra) y generar el txt/resumen de dispersión bancaria semanal.

---

## 6. Plan de Acción Sugerido para el Agente
1. **Fase 1 (Extracción):** Leer el archivo Excel adjunto y generar un diccionario JSON/SQL con todas las fórmulas exactas, constantes 2026 y códigos PLAME utilizados en las hojas del archivo.
2. **Fase 2 (Modelado):** Crear las migraciones de base de datos necesarias sin alterar el flujo de la planilla administrativa actual.
3. **Fase 3 (Backend/Motor):** Implementar el servicio de cálculo de tareo y liquidación semanal validando que, ante los mismos datos de entrada del Excel de prueba, el ERP arroje exactamente los mismos centavos en cada columna.
4. **Fase 4 (UI y Exportadores):** Crear las vistas de Tareo, Boletas CC y acoplar el consolidador mensual al generador de archivos PLAME y AFPnet existente.