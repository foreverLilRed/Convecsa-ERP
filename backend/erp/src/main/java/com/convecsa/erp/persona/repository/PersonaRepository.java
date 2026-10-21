package com.convecsa.erp.persona.repository;

import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository extends JpaRepository<Persona, Long>, JpaSpecificationExecutor<Persona> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Persona p WHERE p.id = :id")
    Optional<Persona> findByIdForUpdate(@Param("id") Long id);

    long countByActivoTrue();

    long countByActivoFalse();

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant inicio, Instant finExclusivo);

    long countByFechaVigenciaEmoBetween(LocalDate inicio, LocalDate fin);

    boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
            TipoDocumento tipoDocumento, String numeroDocumento, Long id);
}
