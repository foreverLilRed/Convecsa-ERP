package com.convecsa.erp.contrato.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_contrato")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regimen_laboral_id", nullable = false)
    private RegimenLaboral regimenLaboral;

    @Column(name = "requiere_fecha_fin", nullable = false)
    private boolean requiereFechaFin = true;

    @Column(nullable = false)
    private boolean activo = true;
}
