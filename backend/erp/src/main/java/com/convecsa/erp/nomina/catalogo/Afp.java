package com.convecsa.erp.nomina.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "afp")
@Getter
@Setter
@NoArgsConstructor
public class Afp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(name = "aporte_obligatorio", nullable = false, precision = 5, scale = 4)
    private BigDecimal aporteObligatorio;

    @Column(name = "comision_flujo", nullable = false, precision = 5, scale = 4)
    private BigDecimal comisionFlujo;

    @Column(name = "prima_seguro", nullable = false, precision = 5, scale = 4)
    private BigDecimal primaSeguro;

    @Column(name = "es_onp", nullable = false)
    private boolean esOnp;

    @Column(nullable = false)
    private boolean activo = true;
}
