package com.convecsa.erp.construccion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "boleta_cc_envio_correo")
@Getter
@Setter
@NoArgsConstructor
public class BoletaCcEnvioCorreo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boleta_id", nullable = false)
    private BoletaCc boleta;

    @Column(nullable = false, length = 180)
    private String destinatario;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 1000)
    private String detalle;

    @Column(name = "enviado_at", nullable = false)
    private Instant enviadoAt;

    @PrePersist
    void onCreate() {
        if (enviadoAt == null) {
            enviadoAt = Instant.now();
        }
    }
}
