package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.CargoComiteSoma;
import com.convecsa.erp.soma.domain.RepresentacionComiteSoma;
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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_comite_miembro")
@Getter
@Setter
@NoArgsConstructor
public class SomaComiteMiembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RepresentacionComiteSoma representacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo_comite", nullable = false, length = 30)
    private CargoComiteSoma cargoComite;

    @Column(nullable = false)
    private boolean titular = true;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activo = true;
}
