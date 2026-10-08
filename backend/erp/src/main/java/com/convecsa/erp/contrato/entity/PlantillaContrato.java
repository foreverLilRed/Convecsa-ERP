package com.convecsa.erp.contrato.entity;

import com.convecsa.erp.contrato.catalogo.PlantillaCampoEsquema;
import com.convecsa.erp.contrato.domain.UsoPlantilla;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "plantillas_contrato")
@Getter
@Setter
@NoArgsConstructor
public class PlantillaContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UsoPlantilla uso = UsoPlantilla.CONTRATO;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String plantilla;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<PlantillaCampoEsquema> metadata = new ArrayList<>();

    @Column(nullable = false)
    private boolean activo = true;
}
