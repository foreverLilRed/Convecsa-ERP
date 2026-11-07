package com.convecsa.erp.construccion.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacturaVencimientoRequest {

    private LocalDate fechaVencimiento;
}
