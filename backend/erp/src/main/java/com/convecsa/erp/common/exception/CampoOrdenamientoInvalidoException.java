package com.convecsa.erp.common.exception;

public class CampoOrdenamientoInvalidoException extends RuntimeException {

    public CampoOrdenamientoInvalidoException() {
        super("Campo de ordenamiento no permitido");
    }
}
