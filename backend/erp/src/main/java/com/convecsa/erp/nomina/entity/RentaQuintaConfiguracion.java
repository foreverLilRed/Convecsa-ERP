package com.convecsa.erp.nomina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "renta_quinta_configuracion")
@Getter
@Setter
@NoArgsConstructor
public class RentaQuintaConfiguracion {

    @Id
    private int anio;

    @Column(name = "valor_uit", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUit;

    @Column(name = "deduccion_uit", nullable = false, precision = 6, scale = 2)
    private BigDecimal deduccionUit;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;
}
