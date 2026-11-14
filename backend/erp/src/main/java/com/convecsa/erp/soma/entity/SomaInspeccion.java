package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.SubcontratoObra;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.TipoInspeccionSoma;
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
@Table(name = "soma_inspeccion")
@Getter
@Setter
@NoArgsConstructor
public class SomaInspeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoInspeccionSoma tipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "area_frente", nullable = false, length = 160)
    private String areaFrente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcontrato_id")
    private SubcontratoObra subcontrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_persona_id")
    private Persona inspector;

    @Column(name = "puntaje_cumplimiento", precision = 5, scale = 2)
    private BigDecimal puntajeCumplimiento;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
