package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.PasoAts;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "soma_charla_ats")
@Getter
@Setter
@NoArgsConstructor
public class SomaCharlaAts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuadrilla_id")
    private CuadrillaObra cuadrilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos = 10;

    @Column(name = "tema_charla", nullable = false, length = 240)
    private String temaCharla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expositor_persona_id")
    private Persona expositor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_ssoma_id")
    private Persona supervisorSsoma;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pasos_ats_json", nullable = false)
    private List<PasoAts> pasosAts = new ArrayList<>();

    @Column(name = "epp_verificado", nullable = false)
    private boolean eppVerificado;

    @Column(name = "herramientas_verif", nullable = false)
    private boolean herramientasVerif;

    @Column(length = 1000)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "charla", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SomaCharlaAsistente> asistentes = new ArrayList<>();

    public void addAsistente(SomaCharlaAsistente asistente) {
        asistente.setCharla(this);
        asistentes.add(asistente);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
