package com.convecsa.erp.contrato.entity;

import com.convecsa.erp.contrato.catalogo.CausaObjetiva;
import com.convecsa.erp.contrato.catalogo.Jornada;
import com.convecsa.erp.contrato.catalogo.MetodoPago;
import com.convecsa.erp.contrato.catalogo.TipoPlantilla;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.nomina.catalogo.Afp;
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
@Table(name = "contrato")
@Getter
@Setter
@NoArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoContrato estado;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_corte")
    private LocalDate fechaCorte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_plantilla_id")
    private TipoPlantilla tipoPlantilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_contrato_id")
    private PlantillaContrato plantillaContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jornada_id")
    private Jornada jornada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causa_objetiva_id")
    private CausaObjetiva causaObjetiva;

    @Column(length = 160)
    private String cargo;

    @Column(precision = 12, scale = 2)
    private BigDecimal sueldo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "afp_id")
    private Afp afp;

    @Column(name = "tiene_asignacion_familiar", nullable = false)
    private boolean tieneAsignacionFamiliar;

    @Column(length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_origen_id")
    private Contrato contratoOrigen;

    @Column(length = 2000)
    private String observaciones;

    @Column(length = 200)
    private String proyecto;

    @Column(name = "ubicacion_obra", length = 200)
    private String ubicacionObra;

    @Column(length = 160)
    private String area;

    @Column(name = "ciudad_firma", length = 80)
    private String ciudadFirma;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> metadata = new LinkedHashMap<>();

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
