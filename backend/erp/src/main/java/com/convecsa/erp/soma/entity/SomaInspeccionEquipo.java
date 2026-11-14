package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.AsignacionEquipo;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.ResultadoPreUsoEquipo;
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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "soma_inspeccion_equipo")
@Getter
@Setter
@NoArgsConstructor
public class SomaInspeccionEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignacion_equipo_id", nullable = false)
    private AsignacionEquipo asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operador_persona_id")
    private Persona operador;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "horometro_lectura", precision = 12, scale = 2)
    private BigDecimal horometroLectura;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checklist_json", nullable = false)
    private Map<String, Boolean> checklist = new LinkedHashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultadoPreUsoEquipo resultado;

    @Column(length = 600)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (checklist == null) {
            checklist = new LinkedHashMap<>();
        }
    }
}
