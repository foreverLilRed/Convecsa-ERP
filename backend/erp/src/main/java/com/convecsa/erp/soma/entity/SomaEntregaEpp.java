package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_entrega_epp")
@Getter
@Setter
@NoArgsConstructor
public class SomaEntregaEpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vale_salida_id")
    private ValeSalida valeSalida;

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotivoEntregaEpp motivo;

    @Column(name = "firmado_trabajador", nullable = false)
    private boolean firmadoTrabajador;

    @Column(name = "entregado_por", nullable = false, length = 80)
    private String entregadoPor;

    @Column(length = 400)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SomaEntregaEppItem> items = new ArrayList<>();

    public void addItem(SomaEntregaEppItem item) {
        item.setEntrega(this);
        items.add(item);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
