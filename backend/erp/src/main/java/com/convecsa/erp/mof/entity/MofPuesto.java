package com.convecsa.erp.mof.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mof_puesto")
@Getter
@Setter
@NoArgsConstructor
public class MofPuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String cargo;

    @Column(nullable = false, length = 40)
    private String grupo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String proposito;

    @Column(nullable = false, length = 240)
    private String superior;

    @Column(nullable = false, length = 500)
    private String subordinados;

    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<MofFuncion> funciones = new ArrayList<>();

    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<MofRequisito> requisitos = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void reemplazarFunciones(List<String> textos) {
        funciones.clear();
        int orden = 0;
        for (String texto : textos) {
            if (texto == null || texto.isBlank()) {
                continue;
            }
            MofFuncion funcion = new MofFuncion();
            funcion.setPuesto(this);
            funcion.setOrden(orden++);
            funcion.setTexto(texto.trim());
            funciones.add(funcion);
        }
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
