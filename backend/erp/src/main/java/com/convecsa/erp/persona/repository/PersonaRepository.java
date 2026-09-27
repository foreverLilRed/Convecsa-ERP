package com.convecsa.erp.persona.repository;

import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
}
