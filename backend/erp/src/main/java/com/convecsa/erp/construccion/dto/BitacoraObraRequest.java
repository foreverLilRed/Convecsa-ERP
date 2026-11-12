package com.convecsa.erp.construccion.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BitacoraObraRequest {

    private LocalDate fecha;
    private String texto;
}
