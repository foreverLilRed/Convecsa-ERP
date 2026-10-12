package com.convecsa.erp.persona.link.entity;

import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.link.domain.TipoLinkInvitado;
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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "persona_link_invitado")
@Getter
@Setter
@NoArgsConstructor
public class PersonaLinkInvitado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLinkInvitado tipo = TipoLinkInvitado.EDICION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private Persona persona;

    @Column(length = 120)
    private String apellidos;

    @Column(length = 120)
    private String nombres;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;
}
