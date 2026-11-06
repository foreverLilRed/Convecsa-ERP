package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.NivelRiesgoObra;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proyecto")
@Getter
@Setter
@NoArgsConstructor
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(length = 120)
    private String ubicacion;

    @Column(name = "centro_costo", length = 40)
    private String centroCosto;

    @Column(name = "valor_obra", precision = 14, scale = 2)
    private BigDecimal valorObra;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", length = 10)
    private NivelRiesgoObra nivelRiesgo;

    @Column(name = "tasa_sctr_salud", nullable = false, precision = 7, scale = 6)
    private BigDecimal tasaSctrSalud = BigDecimal.ZERO;

    @Column(name = "tasa_sctr_pension", nullable = false, precision = 7, scale = 6)
    private BigDecimal tasaSctrPension = BigDecimal.ZERO;

    @Column(name = "cliente_razon", length = 160)
    private String clienteRazon;

    @Column(name = "cliente_ruc", length = 11)
    private String clienteRuc;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_obra", nullable = false, length = 20)
    private EstadoObra estadoObra = EstadoObra.EN_EJECUCION;

    @Column(nullable = false)
    private boolean activo = true;
}
