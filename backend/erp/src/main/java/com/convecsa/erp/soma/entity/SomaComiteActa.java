package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.soma.domain.TipoSesionComiteSoma;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "soma_comite_acta")
@Getter
@Setter
@NoArgsConstructor
public class SomaComiteActa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "numero_acta", nullable = false, length = 40)
    private String numeroActa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sesion", nullable = false, length = 20)
    private TipoSesionComiteSoma tipoSesion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 1000)
    private String agenda;

    @Column(nullable = false, length = 3000)
    private String acuerdos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acta_doc_id")
    private DocumentoObra actaDoc;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
