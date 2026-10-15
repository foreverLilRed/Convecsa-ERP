package com.convecsa.erp.asistencia.entity;

import com.convecsa.erp.asistencia.domain.TipoConceptoVariableTareo;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
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
@Table(name = "asistencia_concepto_variable")
@Getter
@Setter
@NoArgsConstructor
public class AsistenciaConceptoVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoConceptoVariableTareo tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(length = 300)
    private String detalle;
}
