package com.convecsa.erp.exceptions;

public abstract class BusinessException extends RuntimeException {

    private final int codigoError;

    protected BusinessException(String message, int codigoError) {
        super(message);
        this.codigoError = codigoError;
    }

    public int getCodigoError() {
        return codigoError;
    }
}
