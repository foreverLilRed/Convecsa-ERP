package com.convecsa.erp.soma.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaHallazgoLevantarRequest {

    private Long evidenciaDespuesDocId;
    private LocalDate fechaCierre;
}
