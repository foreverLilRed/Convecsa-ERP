package com.convecsa.erp.contrato.catalogo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlantillaCampoEsquema {

    private String clave;
    private String label;
    private String tipo;
    private String catalogo;
    private boolean requerido;
}
