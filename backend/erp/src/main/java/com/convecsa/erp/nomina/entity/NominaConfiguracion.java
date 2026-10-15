package com.convecsa.erp.nomina.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nomina_configuracion")
@Getter @Setter @NoArgsConstructor
public class NominaConfiguracion {
    public static final long ID_UNICO = 1L;
    @Id private Long id;
    @Column(name = "tasa_essalud", nullable = false, precision = 7, scale = 6) private BigDecimal tasaEssalud;
    @Column(name = "tasa_sctr_salud", nullable = false, precision = 7, scale = 6) private BigDecimal tasaSctrSalud;
    @Column(name = "tasa_sctr_pension", nullable = false, precision = 7, scale = 6) private BigDecimal tasaSctrPension;
    @Column(name = "tasa_vida_ley", nullable = false, precision = 7, scale = 6) private BigDecimal tasaVidaLey;
    @Column(name = "tasa_essalud_vida", nullable = false, precision = 7, scale = 6) private BigDecimal tasaEssaludVida;
}
