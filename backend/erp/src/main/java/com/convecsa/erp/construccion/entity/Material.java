package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.TipoEpp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 240)
    private String descripcion;

    @Column(length = 20)
    private String unidad;

    @Column(length = 40)
    private String familia;

    @Column(name = "stock_minimo", nullable = false, precision = 14, scale = 4)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean critico;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "es_epp", nullable = false)
    private boolean esEpp;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_epp", length = 40)
    private TipoEpp tipoEpp;

    @Column(name = "vida_util_dias")
    private Integer vidaUtilDias;

    @Column(name = "norma_certificacion", length = 80)
    private String normaCertificacion;
}
