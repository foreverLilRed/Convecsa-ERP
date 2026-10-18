package com.convecsa.erp.persona.repository;

import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonaRepository extends JpaRepository<Persona, Long>, JpaSpecificationExecutor<Persona> {

    long countByActivoTrue();

    long countByActivoFalse();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant inicio, Instant finExclusivo);

    long countByFechaVigenciaEmoBetween(LocalDate inicio, LocalDate fin);

    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
            TipoDocumento tipoDocumento, String numeroDocumento, Long id);
}
