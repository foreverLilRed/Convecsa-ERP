package com.convecsa.erp.nomina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "renta_quinta_categoria")
@Getter
@Setter
@NoArgsConstructor
public class RentaQuintaCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int orden;

    @Column(name = "desde_uit", nullable = false, precision = 8, scale = 2)
    private BigDecimal desdeUit;

    @Column(name = "hasta_uit", precision = 8, scale = 2)
    private BigDecimal hastaUit;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal tasa;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;
}
