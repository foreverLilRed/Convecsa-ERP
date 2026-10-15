package com.convecsa.erp.nomina.entity;

import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "afp_tasa")
@Getter @Setter @NoArgsConstructor
public class AfpTasa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afp_id", nullable = false)
    private Afp afp;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comision", nullable = false, length = 20)
    private TipoComisionAfp tipoComision;
    @Column(name = "aporte_obligatorio", nullable = false, precision = 7, scale = 6)
    private BigDecimal aporteObligatorio;
    @Column(nullable = false, precision = 7, scale = 6)
    private BigDecimal comision;
    @Column(name = "prima_seguro", nullable = false, precision = 7, scale = 6)
    private BigDecimal primaSeguro;
    @Column(name = "tope_remuneracion_asegurable", precision = 12, scale = 2)
    private BigDecimal topeRemuneracionAsegurable;
    @Column(nullable = false)
    private boolean activo;
}
