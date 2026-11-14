package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.soma.domain.TipoAgenteMonitoreoSoma;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_monitoreo")
@Getter
@Setter
@NoArgsConstructor
public class SomaMonitoreo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_agente", nullable = false, length = 40)
    private TipoAgenteMonitoreoSoma tipoAgente;

    @Column(name = "parametro_medido", nullable = false, length = 120)
    private String parametroMedido;

    @Column(name = "punto_medicion", nullable = false, length = 160)
    private String puntoMedicion;

    @Column(name = "valor_medido", nullable = false, precision = 14, scale = 4)
    private BigDecimal valorMedido;

    @Column(name = "limite_permisible", nullable = false, precision = 14, scale = 4)
    private BigDecimal limitePermisible;

    @Column(nullable = false, length = 30)
    private String unidad;

    @Column(name = "cumple_lmp", nullable = false)
    private boolean cumpleLmp;

    @Column(name = "laboratorio_nombre", length = 160)
    private String laboratorioNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "informe_doc_id")
    private DocumentoObra informeDoc;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
