package com.convecsa.erp.construccion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "avance_partida")
@Getter
@Setter
@NoArgsConstructor
public class AvancePartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avance_id", nullable = false)
    private AvanceObra avance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 240)
    private String descripcion;

    @Column(length = 20)
    private String unidad;

    @Column(name = "centro_costo", length = 40)
    private String centroCosto;

    @Column(name = "metrado_meta", nullable = false, precision = 14, scale = 4)
    private BigDecimal metradoMeta = BigDecimal.ZERO;

    @Column(name = "metrado_periodo", nullable = false, precision = 14, scale = 4)
    private BigDecimal metradoPeriodo = BigDecimal.ZERO;

    @Column(name = "metrado_acumulado", nullable = false, precision = 14, scale = 4)
    private BigDecimal metradoAcumulado = BigDecimal.ZERO;

    @Column(nullable = false)
    private int orden;
}
