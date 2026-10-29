package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.contrato.entity.Contrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contrato_cc")
@Getter
@Setter
@NoArgsConstructor
public class ContratoCc {

    @Id
    private Long contratoId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaObrera categoria;

    @Column(name = "especialidad_bae", length = 30)
    private String especialidadBae;

    @Column(name = "afiliado_sindical", nullable = false)
    private boolean afiliadoSindical;

    @Column(name = "cuota_sindical", nullable = false, precision = 12, scale = 2)
    private BigDecimal cuotaSindical = BigDecimal.ZERO;

    @Column(name = "hijos_escolaridad", nullable = false)
    private int hijosEscolaridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;
}
