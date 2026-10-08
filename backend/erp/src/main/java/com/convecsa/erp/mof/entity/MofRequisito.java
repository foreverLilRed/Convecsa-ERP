package com.convecsa.erp.mof.entity;

import com.convecsa.erp.mof.domain.MofRequisitoTipo;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mof_requisito")
@Getter
@Setter
@NoArgsConstructor
public class MofRequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puesto_id", nullable = false)
    private MofPuesto puesto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MofRequisitoTipo tipo;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;
}
