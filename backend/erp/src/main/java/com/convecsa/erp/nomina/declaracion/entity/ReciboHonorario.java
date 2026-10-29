package com.convecsa.erp.nomina.declaracion.entity;

import com.convecsa.erp.persona.entity.Persona;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "recibo_honorario")
@Getter
@Setter
@NoArgsConstructor
public class ReciboHonorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "declaracion_id", nullable = false)
    private DeclaracionMensual declaracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "apellido_paterno", length = 80)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 80)
    private String apellidoMaterno;

    @Column(length = 120)
    private String nombres;

    @Column(nullable = false)
    private boolean domiciliado = true;

    @Column(name = "tipo_comprobante", nullable = false, length = 10)
    private String tipoComprobante = "R";

    @Column(nullable = false, length = 20)
    private String serie;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto = BigDecimal.ZERO;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "retencion_cuarta", nullable = false)
    private boolean retencionCuarta;

    @Column(name = "retencion_pension", nullable = false)
    private boolean retencionPension;

    @Column(name = "importe_aporte_pension", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeAportePension = BigDecimal.ZERO;
}
