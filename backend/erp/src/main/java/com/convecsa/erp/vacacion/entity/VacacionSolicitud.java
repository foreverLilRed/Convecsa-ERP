package com.convecsa.erp.vacacion.entity;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vacacion_solicitud")
@Getter
@Setter
@NoArgsConstructor
public class VacacionSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVacacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVacacion estado = EstadoVacacion.PENDIENTE;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "dias_derecho_periodo", nullable = false)
    private int diasDerechoPeriodo;

    @Column(name = "dias_solicitados", nullable = false)
    private int diasSolicitados;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "fecha_retorno", nullable = false)
    private LocalDate fechaRetorno;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "aprobada_en")
    private Instant aprobadaEn;

    @Column(name = "rechazada_en")
    private Instant rechazadaEn;

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
