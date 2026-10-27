package com.convecsa.erp.obra.entity;

import com.convecsa.erp.obra.domain.EstadoObra;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "obra")
@Getter
@Setter
@NoArgsConstructor
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Column(name = "contratista_principal", length = 200)
    private String contratistaPrincipal;

    @Column(length = 300)
    private String ubicacion;

    @Column(name = "fecha_inicio_planificada")
    private LocalDate fechaInicioPlanificada;

    @Column(name = "fecha_fin_planificada")
    private LocalDate fechaFinPlanificada;

    @Column(name = "fecha_inicio_real")
    private LocalDate fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDate fechaFinReal;

    @Column(name = "monto_contratado", precision = 18, scale = 2)
    private BigDecimal montoContratado;

    @Column(nullable = false, length = 3)
    private String moneda = "PEN";

    @Column(name = "responsable_nombre", length = 200)
    private String responsableNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoObra estado = EstadoObra.BORRADOR;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (estado == null) {
            estado = EstadoObra.BORRADOR;
        }
        if (moneda == null || moneda.isBlank()) {
            moneda = "PEN";
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
