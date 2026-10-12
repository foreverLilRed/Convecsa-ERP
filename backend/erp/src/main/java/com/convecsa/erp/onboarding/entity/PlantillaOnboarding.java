package com.convecsa.erp.onboarding.entity;

import com.convecsa.erp.onboarding.domain.AmbitoPlantilla;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plantillas_onboarding")
@Getter
@Setter
@NoArgsConstructor
public class PlantillaOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String categoria;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "base_legal", nullable = false, columnDefinition = "TEXT")
    private String baseLegal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String plantilla = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmbitoPlantilla ambito = AmbitoPlantilla.ONBOARDING;

    @Column(nullable = false)
    private boolean activo = true;
}
