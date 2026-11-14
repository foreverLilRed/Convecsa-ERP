package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.ClasificacionHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
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
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_hallazgo")
@Getter
@Setter
@NoArgsConstructor
public class SomaHallazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspeccion_id")
    private SomaInspeccion inspeccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClasificacionHallazgoSoma clasificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "potencial_perdida", nullable = false, length = 20)
    private PotencialPerdidaSoma potencialPerdida;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(length = 200)
    private String ubicacion;

    @Column(name = "accion_correctiva", nullable = false, length = 1000)
    private String accionCorrectiva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_persona_id")
    private Persona responsable;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDate fechaDeteccion;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    @Column(name = "paralizacion_frente", nullable = false)
    private boolean paralizacionFrente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidencia_antes_doc_id")
    private DocumentoObra evidenciaAntes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidencia_despues_doc_id")
    private DocumentoObra evidenciaDespues;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoHallazgoSoma estado = EstadoHallazgoSoma.ABIERTO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (estado == null) {
            estado = EstadoHallazgoSoma.ABIERTO;
        }
    }
}
