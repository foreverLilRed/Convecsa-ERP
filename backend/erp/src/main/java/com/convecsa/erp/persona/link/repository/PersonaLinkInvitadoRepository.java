package com.convecsa.erp.persona.link.repository;

import com.convecsa.erp.persona.link.entity.PersonaLinkInvitado;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaLinkInvitadoRepository extends JpaRepository<PersonaLinkInvitado, Long> {

    Optional<PersonaLinkInvitado> findByToken(String token);
}
