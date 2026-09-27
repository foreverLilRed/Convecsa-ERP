package com.convecsa.erp.persona.entity;

import com.convecsa.erp.persona.domain.Banco;
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
@Table(name = "persona_cuenta_bancaria")
@Getter
@Setter
@NoArgsConstructor
public class PersonaCuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Banco banco;

    @Column(name = "numero_cuenta", nullable = false, length = 40)
    private String numeroCuenta;

    @Column(nullable = false, length = 20)
    private String cci;

    @Column(nullable = false)
    private boolean principal;
}
