package com.convecsa.erp.contrato.exception;

import com.convecsa.erp.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class ContratoNoEncontrado extends BusinessException {

    public ContratoNoEncontrado(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}
