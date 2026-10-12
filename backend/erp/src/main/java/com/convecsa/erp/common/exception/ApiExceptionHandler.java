package com.convecsa.erp.common.exception;

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
        FeriadoDuplicadoException.class
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
        LinkInvitadoNoEncontradoException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(LinkInvitadoExpiradoException.class)
    public ResponseEntity<Map<String, Object>> handleGone(LinkInvitadoExpiradoException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.GONE.value());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(body);
    }
}
