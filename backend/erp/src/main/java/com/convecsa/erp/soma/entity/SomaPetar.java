package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.EstadoPetar;
import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "soma_petar")
@Getter
@Setter
@NoArgsConstructor
public class SomaPetar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_trabajo", nullable = false, length = 30)
    private TipoTrabajoPetar tipoTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id")
    private PartidaPresupuesto partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuadrilla_id")
    private CuadrillaObra cuadrilla;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "ubicacion_frente", nullable = false, length = 200)
    private String ubicacionFrente;

    @Column(name = "descripcion_trabajo", nullable = false, length = 1000)
    private String descripcionTrabajo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checklist_json", nullable = false)
    private Map<String, Boolean> checklist = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mediciones_json", nullable = false)
    private Map<String, BigDecimal> mediciones = new LinkedHashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residente_persona_id")
    private Persona residente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_ssoma_id")
    private Persona supervisorSsoma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPetar estado = EstadoPetar.BORRADOR;

    @Column(name = "autorizado_por", length = 80)
    private String autorizadoPor;

    @Column(name = "autorizado_at")
    private Instant autorizadoAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_obra_id")
    private DocumentoObra documento;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "petar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SomaPetarTrabajador> trabajadores = new ArrayList<>();

    public void addTrabajador(SomaPetarTrabajador trabajador) {
        trabajador.setPetar(this);
        trabajadores.add(trabajador);
    }

    public void clearTrabajadores() {
        trabajadores.clear();
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (estado == null) {
            estado = EstadoPetar.BORRADOR;
        }
        if (checklist == null) {
            checklist = new LinkedHashMap<>();
        }
        if (mediciones == null) {
            mediciones = new LinkedHashMap<>();
        }
    }
}
