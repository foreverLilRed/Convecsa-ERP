package com.convecsa.erp.common.exception;

public class PlantillaPinInvalidoException extends RuntimeException {

    public PlantillaPinInvalidoException() {
        super("PIN de superusuario incorrecto");
    }
}
