package com.convecsa.erp.nomina.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boleta_envio_correo")
@Getter @Setter @NoArgsConstructor
public class BoletaEnvioCorreo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boleta_id", nullable = false) private BoletaPago boleta;
    @Column(nullable = false, length = 180) private String destinatario;
    @Column(nullable = false, length = 20) private String estado;
    @Column(length = 1000) private String detalle;
    @Column(name = "enviado_at", nullable = false) private Instant enviadoAt;
    @PrePersist void onCreate() { if (enviadoAt == null) enviadoAt = Instant.now(); }
}
