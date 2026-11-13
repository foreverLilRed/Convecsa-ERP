package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.Material;
import com.convecsa.erp.soma.domain.EstadoItemEpp;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_entrega_epp_item")
@Getter
@Setter
@NoArgsConstructor
public class SomaEntregaEppItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrega_id", nullable = false)
    private SomaEntregaEpp entrega;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad;

    @Column(length = 20)
    private String talla;

    @Column(name = "fecha_vencimiento_estimada")
    private LocalDate fechaVencimientoEstimada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_item", nullable = false, length = 20)
    private EstadoItemEpp estadoItem = EstadoItemEpp.VIGENTE;
}
