package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_capacitacion")
@Getter
@Setter
@NoArgsConstructor
public class SomaCapacitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoCapacitacionSoma tipo;

    @Column(nullable = false, length = 240)
    private String tema;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "horas_duracion", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasDuracion;

    @Column(name = "expositor_nombre", nullable = false, length = 160)
    private String expositorNombre;

    @Column(name = "expositor_entidad", length = 160)
    private String expositorEntidad;

    @Column(name = "puntaje_simulacro", precision = 5, scale = 2)
    private BigDecimal puntajeSimulacro;

    @Column(length = 1000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidencia_doc_id")
    private DocumentoObra evidencia;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "capacitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SomaCapacitacionAsistente> asistentes = new ArrayList<>();

    public void addAsistente(SomaCapacitacionAsistente asistente) {
        asistente.setCapacitacion(this);
        asistentes.add(asistente);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
