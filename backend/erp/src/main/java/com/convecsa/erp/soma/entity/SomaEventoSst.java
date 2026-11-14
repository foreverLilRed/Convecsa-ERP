package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.SubcontratoObra;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.CausasBasicasEvento;
import com.convecsa.erp.soma.domain.CausasInmediatasEvento;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.GravedadIncapacidadSst;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "soma_evento_sst")
@Getter
@Setter
@NoArgsConstructor
public class SomaEventoSst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 40)
    private TipoEventoSst tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad_incapacidad", length = 30)
    private GravedadIncapacidadSst gravedadIncapacidad;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "lugar_exacto", nullable = false, length = 200)
    private String lugarExacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_afectada_id")
    private Persona personaAfectada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcontrato_id")
    private SubcontratoObra subcontrato;

    @Column(name = "descripcion_evento", nullable = false, length = 2000)
    private String descripcionEvento;

    @Column(name = "parte_cuerpo_lesionada", length = 120)
    private String parteCuerpoLesionada;

    @Column(name = "naturaleza_lesion", length = 120)
    private String naturalezaLesion;

    @Column(name = "agente_causante", length = 160)
    private String agenteCausante;

    @Column(name = "numero_certificado_dm", length = 60)
    private String numeroCertificadoDm;

    @Column(name = "dias_descanso_medico", nullable = false)
    private int diasDescansoMedico;

    @Column(name = "dias_cargo_estadistico", nullable = false)
    private int diasCargoEstadistico;

    @Column(name = "fecha_inicio_dm")
    private LocalDate fechaInicioDm;

    @Column(name = "fecha_fin_dm")
    private LocalDate fechaFinDm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "causas_inmediatas_json", nullable = false)
    private CausasInmediatasEvento causasInmediatas = CausasInmediatasEvento.vacio();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "causas_basicas_json", nullable = false)
    private CausasBasicasEvento causasBasicas = CausasBasicasEvento.vacio();

    @Column(name = "notificado_mtpe", nullable = false)
    private boolean notificadoMtpe;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_investigacion", nullable = false, length = 20)
    private EstadoInvestigacionSst estadoInvestigacion = EstadoInvestigacionSst.EN_INVESTIGACION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "informe_doc_id")
    private DocumentoObra informe;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToMany
    @JoinTable(
            name = "soma_evento_hallazgo",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "hallazgo_id"))
    private List<SomaHallazgo> hallazgos = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (estadoInvestigacion == null) {
            estadoInvestigacion = EstadoInvestigacionSst.EN_INVESTIGACION;
        }
        if (causasInmediatas == null) {
            causasInmediatas = CausasInmediatasEvento.vacio();
        }
        if (causasBasicas == null) {
            causasBasicas = CausasBasicasEvento.vacio();
        }
    }
}
