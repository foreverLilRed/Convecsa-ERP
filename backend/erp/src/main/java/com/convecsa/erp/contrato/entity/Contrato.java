package com.convecsa.erp.contrato.entity;

import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import com.convecsa.erp.persona.entity.Persona;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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

    @Column(nullable = false, length = 200)
    private String titulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_contrato_id", nullable = false)
    private CategoriaContrato categoriaContrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sueldo;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_corte")
    private LocalDate fechaCorte;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_plantilla_id", nullable = false)
    private PlantillaContrato tipoPlantilla;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> metadata = new LinkedHashMap<>();

    public String metadata(String clave) {
        return metadata == null ? null : metadata.get(clave);
    }
}
