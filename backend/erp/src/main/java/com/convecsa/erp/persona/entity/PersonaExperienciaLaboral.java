package com.convecsa.erp.persona.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "persona_experiencia_laboral")
@Getter
@Setter
@NoArgsConstructor
public class PersonaExperienciaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(nullable = false, length = 160)
    private String organizacion;

    @Column(nullable = false, length = 160)
    private String cargo;

    @Column(name = "fecha_inicio_laboral", nullable = false)
    private LocalDate fechaInicioLaboral;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
}
