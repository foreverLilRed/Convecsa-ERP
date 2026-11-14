package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.soma.domain.CategoriaResiduoSoma;
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
@Table(name = "soma_residuo_registro")
@Getter
@Setter
@NoArgsConstructor
public class SomaResiduoRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_residuo", nullable = false, length = 40)
    private CategoriaResiduoSoma categoriaResiduo;

    @Column(nullable = false, length = 240)
    private String descripcion;

    @Column(name = "cantidad_kg", precision = 14, scale = 2)
    private BigDecimal cantidadKg;

    @Column(name = "volumen_m3", precision = 14, scale = 2)
    private BigDecimal volumenM3;

    @Column(name = "eors_razon_social", length = 160)
    private String eorsRazonSocial;

    @Column(name = "eors_ruc", length = 11)
    private String eorsRuc;

    @Column(name = "numero_manifiesto", length = 60)
    private String numeroManifiesto;

    @Column(name = "destino_final", length = 160)
    private String destinoFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifiesto_doc_id")
    private DocumentoObra manifiestoDoc;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
