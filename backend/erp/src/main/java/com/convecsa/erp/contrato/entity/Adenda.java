package com.convecsa.erp.contrato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "adenda")
@Getter
@Setter
@NoArgsConstructor
public class Adenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plantilla_contrato_id", nullable = false)
    private PlantillaContrato plantillaContrato;

    @Column(nullable = false)
    private int numero;

    @Column(name = "fecha_fin_anterior", nullable = false)
    private LocalDate fechaFinAnterior;

    @Column(name = "fecha_fin_nueva", nullable = false)
    private LocalDate fechaFinNueva;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sueldo;

    @Column(nullable = false, length = 2000)
    private String motivo;

    @Column(name = "ciudad_emision", length = 80)
    private String ciudadEmision;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> metadata = new LinkedHashMap<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
