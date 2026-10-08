package com.convecsa.erp.mof.entity;

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
@Table(name = "mof_funcion")
@Getter
@Setter
@NoArgsConstructor
public class MofFuncion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puesto_id", nullable = false)
    private MofPuesto puesto;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;
}
