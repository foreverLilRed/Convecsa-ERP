package com.convecsa.erp.asistencia.entity;

import com.convecsa.erp.asistencia.domain.EstadoAsistencia;
import com.convecsa.erp.asistencia.domain.CodigoTareo;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "asistencia_registro")
@Getter
@Setter
@NoArgsConstructor
public class AsistenciaRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "horas_esperadas", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasEsperadas;

    @Column(name = "horas_trabajadas", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasTrabajadas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAsistencia estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodigoTareo codigo = CodigoTareo.A;

    @Column(name = "minutos_extras_25", nullable = false)
    private int minutosExtras25;

    @Column(name = "minutos_extras_35", nullable = false)
    private int minutosExtras35;

    @Column(name = "minutos_tardanza", nullable = false)
    private int minutosTardanza;

    @Column(length = 500)
    private String observacion;

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
