package com.convecsa.erp.soma.entity;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.TipoEmo;
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
@Table(name = "soma_emo_examen")
@Getter
@Setter
@NoArgsConstructor
public class SomaEmoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_emo", nullable = false, length = 30)
    private TipoEmo tipoEmo;

    @Column(name = "clinica_nombre", nullable = false, length = 160)
    private String clinicaNombre;

    @Column(name = "clinica_ruc", length = 11)
    private String clinicaRuc;

    @Column(name = "medico_evaluador", length = 160)
    private String medicoEvaluador;

    @Column(name = "cmp_medico", length = 30)
    private String cmpMedico;

    @Column(name = "fecha_examen", nullable = false)
    private LocalDate fechaExamen;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AptitudEmo aptitud;

    @Column(length = 2000)
    private String restricciones;

    @Column(length = 1000)
    private String interconsultas;

    @Column(length = 1000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_obra_id")
    private DocumentoObra documentoObra;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
