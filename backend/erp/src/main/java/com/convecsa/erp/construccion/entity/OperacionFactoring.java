package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.EstadoFactoring;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "operacion_factoring")
@Getter
@Setter
@NoArgsConstructor
public class OperacionFactoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false, unique = true)
    private FacturaObra factura;

    @Column(name = "entidad_razon", nullable = false, length = 160)
    private String entidadRazon;

    @Column(name = "entidad_ruc", length = 11)
    private String entidadRuc;

    @Column(name = "fecha_cesion", nullable = false)
    private LocalDate fechaCesion;

    @Column(name = "tasa_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal tasaDescuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal comision = BigDecimal.ZERO;

    @Column(name = "igv_comision", nullable = false, precision = 14, scale = 2)
    private BigDecimal igvComision = BigDecimal.ZERO;

    @Column(name = "importe_nominal", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeNominal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "importe_neto", nullable = false, precision = 14, scale = 2)
    private BigDecimal importeNeto = BigDecimal.ZERO;

    @Column(name = "fecha_pago_esperada")
    private LocalDate fechaPagoEsperada;

    @Column(name = "fecha_adelanto")
    private LocalDate fechaAdelanto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFactoring estado = EstadoFactoring.CEDIDA;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (fechaCesion == null) {
            fechaCesion = LocalDate.now();
        }
    }
}
