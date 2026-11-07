package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.TipoLiberacionRetencion;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "liberacion_retencion")
@Getter
@Setter
@NoArgsConstructor
public class LiberacionRetencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLiberacionRetencion tipo = TipoLiberacionRetencion.EFECTIVO;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal importe = BigDecimal.ZERO;

    @Column(length = 160)
    private String entidad;

    @Column(name = "numero_documento", length = 40)
    private String numeroDocumento;

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
