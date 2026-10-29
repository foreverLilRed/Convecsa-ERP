package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.CodigoTareoCc;
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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tareo_cc_dia")
@Getter
@Setter
@NoArgsConstructor
public class TareoCcDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "periodo_id", nullable = false)
    private PeriodoCc periodo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "horas_ord", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasOrd = BigDecimal.ZERO;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal he60 = BigDecimal.ZERO;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal he100 = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CodigoTareoCc codigo = CodigoTareoCc.LAB;

    @Column(nullable = false)
    private boolean altura;

    @Column(nullable = false)
    private boolean altitud;

    @Column(nullable = false)
    private boolean agua;

    @Column(name = "altas_temp", nullable = false)
    private boolean altasTemp;

    @Column(name = "cota_cero", nullable = false)
    private boolean cotaCero;

    @Column(nullable = false)
    private boolean nocturno;

    @Column(name = "aguas_servidas", nullable = false)
    private boolean aguasServidas;

    @Column(nullable = false)
    private boolean bae;

    @Column(nullable = false)
    private boolean movilidad = true;

    @Column(nullable = false)
    private int pisos;
}
