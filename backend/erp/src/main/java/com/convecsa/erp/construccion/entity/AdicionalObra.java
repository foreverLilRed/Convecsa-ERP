package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.EstadoAdicionalObra;
import com.convecsa.erp.construccion.domain.TipoAdicionalObra;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "adicional_obra")
@Getter
@Setter
@NoArgsConstructor
public class AdicionalObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAdicionalObra tipo = TipoAdicionalObra.ADICIONAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAdicionalObra estado = EstadoAdicionalObra.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 240)
    private String descripcion;

    @Column(length = 20)
    private String unidad;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal metrado = BigDecimal.ZERO;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
