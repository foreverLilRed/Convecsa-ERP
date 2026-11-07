package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.domain.TipoValorizacion;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "valorizacion")
@Getter
@Setter
@NoArgsConstructor
public class Valorizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoValorizacion tipo = TipoValorizacion.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoValorizacion estado = EstadoValorizacion.BORRADOR;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "porcentaje_retencion", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeRetencion = BigDecimal.ZERO;

    @Column(name = "monto_bruto", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoBruto = BigDecimal.ZERO;

    @Column(name = "monto_retencion", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoRetencion = BigDecimal.ZERO;

    @Column(name = "monto_neto", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoNeto = BigDecimal.ZERO;

    @Column(length = 400)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "valorizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ValorizacionItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "valorizacion", fetch = FetchType.LAZY)
    private FacturaObra factura;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
