package com.convecsa.erp.construccion.entity;

import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documento_obra")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumentoObra tipo;

    @Column(name = "nombre_original", nullable = false, length = 240)
    private String nombreOriginal;

    @Column(nullable = false, length = 80)
    private String mime;

    @Column(name = "tamano_bytes", nullable = false)
    private long tamanoBytes;

    @Column(name = "ruta_relativa", nullable = false, unique = true, length = 400)
    private String rutaRelativa;

    @Column(length = 400)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
