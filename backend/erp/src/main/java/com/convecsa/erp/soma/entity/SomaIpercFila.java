package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.soma.domain.NivelRiesgoIperc;
import com.convecsa.erp.soma.domain.TipoPeligroIperc;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_iperc_fila")
@Getter
@Setter
@NoArgsConstructor
public class SomaIpercFila {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @Column(name = "actividad_tarea", nullable = false, length = 240)
    private String actividadTarea;

    @Column(name = "puesto_trabajo", nullable = false, length = 120)
    private String puestoTrabajo;

    @Column(nullable = false)
    private boolean rutinario = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_peligro", nullable = false, length = 30)
    private TipoPeligroIperc tipoPeligro;

    @Column(nullable = false, length = 400)
    private String peligro;

    @Column(nullable = false, length = 400)
    private String riesgo;

    @Column(nullable = false, length = 400)
    private String consecuencia;

    @Column(name = "requisito_legal", length = 200)
    private String requisitoLegal;

    @Column(name = "probabilidad_inicial", nullable = false)
    private int probabilidadInicial;

    @Column(name = "severidad_inicial", nullable = false)
    private int severidadInicial;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo_inicial", nullable = false, length = 20)
    private NivelRiesgoIperc nivelRiesgoInicial;

    @Column(name = "control_eliminacion", length = 500)
    private String controlEliminacion;

    @Column(name = "control_sustitucion", length = 500)
    private String controlSustitucion;

    @Column(name = "control_ingenieria", length = 500)
    private String controlIngenieria;

    @Column(name = "control_administrativo", length = 500)
    private String controlAdministrativo;

    @Column(name = "control_epp", length = 500)
    private String controlEpp;

    @Column(name = "probabilidad_residual", nullable = false)
    private int probabilidadResidual;

    @Column(name = "severidad_residual", nullable = false)
    private int severidadResidual;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo_residual", nullable = false, length = 20)
    private NivelRiesgoIperc nivelRiesgoResidual;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
