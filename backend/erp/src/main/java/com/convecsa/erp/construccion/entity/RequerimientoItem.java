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
@Table(name = "requerimiento_item")
@Getter
@Setter
@NoArgsConstructor
public class RequerimientoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requerimiento_id", nullable = false)
    private RequerimientoObra requerimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @Column(name = "codigo_partida", length = 40)
    private String codigoPartida;

    @Column(name = "descripcion_partida", length = 240)
    private String descripcionPartida;

    @Column(name = "centro_costo", length = 40)
    private String centroCosto;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad = BigDecimal.ZERO;

    @Column(name = "cantidad_atendida", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidadAtendida = BigDecimal.ZERO;

    @Column(nullable = false)
    private int orden;
}
