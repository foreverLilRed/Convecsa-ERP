package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.Banco;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CuentaBancariaDTO {

    private final Long id;
    private final Banco banco;
    private final String numeroCuenta;
    private final String cci;
    private final boolean principal;
}
