package com.convecsa.erp.offboarding.entity;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.offboarding.domain.LiquidacionSnapshot;
import com.convecsa.erp.offboarding.domain.TipoCese;
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
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ceses")
@Getter
@Setter
@NoArgsConstructor
public class Cese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(name = "fecha_cese", nullable = false)
    private LocalDate fechaCese;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cese", nullable = false, length = 30)
    private TipoCese tipoCese;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private LiquidacionSnapshot liquidacion = new LiquidacionSnapshot();

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
