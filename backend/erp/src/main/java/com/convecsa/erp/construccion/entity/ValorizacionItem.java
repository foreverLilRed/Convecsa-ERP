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
@Table(name = "valorizacion_item")
@Getter
@Setter
@NoArgsConstructor
public class ValorizacionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "valorizacion_id", nullable = false)
    private Valorizacion valorizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adicional_id")
    private AdicionalObra adicional;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 240)
    private String descripcion;

    @Column(length = 20)
    private String unidad;

    @Column(name = "centro_costo", length = 40)
    private String centroCosto;

    @Column(name = "metrado_anterior", nullable = false, precision = 14, scale = 4)
    private BigDecimal metradoAnterior = BigDecimal.ZERO;

    @Column(name = "metrado_periodo", nullable = false, precision = 14, scale = 4)
    private BigDecimal metradoPeriodo = BigDecimal.ZERO;

    @Column(name = "metrado_acumulado", nullable = false, precision = 14, scale = 4)
    private BigDecimal metradoAcumulado = BigDecimal.ZERO;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Column(name = "total_periodo", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPeriodo = BigDecimal.ZERO;

    @Column(name = "total_acumulado", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAcumulado = BigDecimal.ZERO;

    @Column(nullable = false)
    private int orden;
}
