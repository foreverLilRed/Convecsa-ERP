package com.convecsa.erp.desempeno.entity;

import com.convecsa.erp.desempeno.domain.EstadoEvaluacionDesempeno;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evaluacion_desempeno")
@Getter
@Setter
@NoArgsConstructor
public class EvaluacionDesempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(nullable = false, length = 16)
    private String periodo;

    @Column(name = "seguridad_sst", nullable = false)
    private int seguridadSst;

    @Column(nullable = false)
    private int productividad;

    @Column(name = "calidad_tecnica", nullable = false)
    private int calidadTecnica;

    @Column(nullable = false)
    private int puntualidad;

    @Column(name = "trabajo_equipo", nullable = false)
    private int trabajoEquipo;

    @Column(name = "meta_cumplida", nullable = false)
    private int metaCumplida;

    @Column(length = 4000)
    private String comentarios;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEvaluacionDesempeno estado;

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
