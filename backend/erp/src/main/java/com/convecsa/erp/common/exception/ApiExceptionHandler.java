package com.convecsa.erp.common.exception;

import com.convecsa.erp.exceptions.BusinessException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", exception.getCodigoError());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(exception.getCodigoError()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        exception.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Solicitud inválida");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
        DocumentoDuplicadoException.class,
        PlantillaCodigoDuplicadoException.class,
        PeriodoNominaDuplicadoException.class,
        DeclaracionMensualDuplicadaException.class,
        PeriodoCcDuplicadoException.class,
        ProyectoCodigoDuplicadoException.class,
        FeriadoDuplicadoException.class,
        ContratoYaCesadoException.class
    })
    public ResponseEntity<Map<String, Object>> handleDuplicate(RuntimeException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Solicitud inválida");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(exception.getName(), "Parámetro inválido");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Parámetro inválido");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(CampoOrdenamientoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidSort(CampoOrdenamientoInvalidoException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {
        // #region agent log
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(
                "D:/Proyectos/Convecsa ERP/debug-f22012.log", true))) {
            StringBuilder chain = new StringBuilder();
            Throwable cur = exception;
            int depth = 0;
            while (cur != null && depth < 8) {
                if (depth > 0) {
                    chain.append(" | ");
                }
                chain.append(cur.getClass().getName()).append(": ")
                        .append(cur.getMessage() == null ? "" : cur.getMessage().replace('"', '\''));
                cur = cur.getCause();
                depth++;
            }
            out.println(
                    "{\"sessionId\":\"f22012\",\"runId\":\"post-fix\",\"hypothesisId\":\"H4\",\"location\":\"ApiExceptionHandler.handleIllegalArgument\",\"message\":\"400 IllegalArgumentException\",\"data\":{\"msg\":\""
                            + (exception.getMessage() == null ? "" : exception.getMessage().replace('"', '\''))
                            + "\",\"causeChain\":\""
                            + chain
                            + "\"},\"timestamp\":"
                            + System.currentTimeMillis()
                            + "}");
        } catch (Exception ignored) {
            // no-op
        }
        // #endregion
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
        PersonaNoEncontradaException.class,
        ContratoNoEncontradoException.class,
        GeneracionOnboardingNoEncontradaException.class,
        PeriodoNominaNoEncontradoException.class,
        BoletaPagoNoEncontradaException.class,
        AsistenciaRegistroNoEncontradoException.class,
        AsistenciaFeriadoNoEncontradoException.class,
        AdendaNoEncontradaException.class,
        MofPuestoNoEncontradoException.class,
        LinkInvitadoNoEncontradoException.class,
        PlantillaNoEncontradaException.class,
        CeseNoEncontradoException.class,
        ProyectoNoEncontradoException.class,
        PeriodoCcNoEncontradoException.class,
        BoletaCcNoEncontradaException.class,
        DeclaracionMensualNoEncontradaException.class,
        ReciboHonorarioNoEncontradoException.class,
        MaterialNoEncontradoException.class,
        RequerimientoObraNoEncontradoException.class,
        OrdenCompraNoEncontradaException.class,
        ValeSalidaNoEncontradoException.class,
        AvanceNoEncontradoException.class,
        ValorizacionNoEncontradaException.class,
        FacturaObraNoEncontradaException.class,
        AdicionalObraNoEncontradoException.class,
        CobranzaNoEncontradaException.class,
        OperacionFactoringNoEncontradaException.class,
        DocumentoObraNoEncontradoException.class,
        AsignacionEquipoNoEncontradaException.class,
        ImputacionEquipoNoEncontradaException.class,
        SubcontratoObraNoEncontradoException.class,
        SubcontratoAvanceNoEncontradoException.class,
        CuadrillaObraNoEncontradaException.class,
        BitacoraObraNoEncontradaException.class,
        SomaSctrPolizaNoEncontradaException.class,
        SomaEntregaEppNoEncontradaException.class,
        SomaCapacitacionNoEncontradaException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ProyectoSinAccesoException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ProyectoSinAccesoException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(LinkInvitadoExpiradoException.class)
    public ResponseEntity<Map<String, Object>> handleGone(LinkInvitadoExpiradoException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.GONE.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(body);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(CredencialesInvalidasException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(PlantillaPinInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(PlantillaPinInvalidoException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
