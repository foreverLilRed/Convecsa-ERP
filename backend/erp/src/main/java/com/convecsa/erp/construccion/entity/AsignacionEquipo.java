package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.ModoTarifaEquipo;
import com.convecsa.erp.construccion.domain.OrigenEquipo;
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
@Table(name = "asignacion_equipo")
@Getter
@Setter
@NoArgsConstructor
public class AsignacionEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 240)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenEquipo origen = OrigenEquipo.PROPIO;

    @Column(name = "proveedor_razon", length = 160)
    private String proveedorRazon;

    @Column(name = "proveedor_ruc", length = 11)
    private String proveedorRuc;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_tarifa", nullable = false, length = 10)
    private ModoTarifaEquipo modoTarifa = ModoTarifaEquipo.DIA;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tarifa = BigDecimal.ZERO;

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
        if (fechaIngreso == null) {
            fechaIngreso = LocalDate.now();
        }
    }
}
