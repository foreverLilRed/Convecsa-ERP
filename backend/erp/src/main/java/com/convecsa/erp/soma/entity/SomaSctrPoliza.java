package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
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
@Table(name = "soma_sctr_poliza")
@Getter
@Setter
@NoArgsConstructor
public class SomaSctrPoliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(name = "aseguradora_salud", nullable = false, length = 120)
    private String aseguradoraSalud;

    @Column(name = "numero_poliza_salud", nullable = false, length = 60)
    private String numeroPolizaSalud;

    @Column(name = "aseguradora_pension", nullable = false, length = 120)
    private String aseguradoraPension;

    @Column(name = "numero_poliza_pension", nullable = false, length = 60)
    private String numeroPolizaPension;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "monto_prima_salud", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoPrimaSalud = BigDecimal.ZERO;

    @Column(name = "monto_prima_pension", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoPrimaPension = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_obra_id")
    private DocumentoObra documentoObra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSctrPoliza estado = EstadoSctrPoliza.VIGENTE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SomaSctrNomina> nomina = new ArrayList<>();

    public void addNomina(SomaSctrNomina fila) {
        fila.setPoliza(this);
        nomina.add(fila);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (estado == null) {
            estado = EstadoSctrPoliza.VIGENTE;
        }
    }
}
