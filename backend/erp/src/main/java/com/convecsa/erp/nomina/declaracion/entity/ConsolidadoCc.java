package com.convecsa.erp.nomina.declaracion.entity;

import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.persona.entity.Persona;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "consolidado_cc")
@Getter
@Setter
@NoArgsConstructor
public class ConsolidadoCc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "declaracion_id", nullable = false)
    private DeclaracionMensual declaracion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Column(name = "total_haberes", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalHaberes = BigDecimal.ZERO;

    @Column(name = "total_descuentos", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDescuentos = BigDecimal.ZERO;

    @Column(name = "total_aportes_empleador", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAportesEmpleador = BigDecimal.ZERO;

    @Column(name = "total_afecto", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAfecto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal neto = BigDecimal.ZERO;

    @Column(name = "horas_ord", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasOrd = BigDecimal.ZERO;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal he60 = BigDecimal.ZERO;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal he100 = BigDecimal.ZERO;

    @Column(name = "dias_lab", nullable = false)
    private int diasLab;

    @Column(name = "dias_falta", nullable = false)
    private int diasFalta;

    @Column(name = "dias_dm", nullable = false)
    private int diasDm;

    @Column(name = "dias_fe", nullable = false)
    private int diasFe;

    @Column(name = "dias_lic", nullable = false)
    private int diasLic;

    @OneToMany(mappedBy = "consolidado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ConsolidadoCcConcepto> conceptos = new ArrayList<>();

    public void reemplazarConceptos(List<ConsolidadoCcConcepto> nuevos) {
        conceptos.clear();
        int orden = 0;
        for (ConsolidadoCcConcepto concepto : nuevos) {
            concepto.setConsolidado(this);
            concepto.setOrden(orden++);
            conceptos.add(concepto);
        }
    }
}
