package com.convecsa.erp.nomina.declaracion.entity;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "consolidado_cc_concepto")
@Getter
@Setter
@NoArgsConstructor
public class ConsolidadoCcConcepto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consolidado_id", nullable = false)
    private ConsolidadoCc consolidado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaConcepto categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CodigoConceptoCc codigo;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private int orden;
}
