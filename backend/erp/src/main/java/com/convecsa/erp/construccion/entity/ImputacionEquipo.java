package com.convecsa.erp.construccion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "imputacion_equipo")
@Getter
@Setter
@NoArgsConstructor
public class ImputacionEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionEquipo asignacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tarifa = BigDecimal.ZERO;

    @Column(name = "costo_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @Column(name = "centro_costo", length = 40)
    private String centroCosto;

    @Column(length = 400)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (fecha == null) {
            fecha = LocalDate.now();
        }
    }
}
