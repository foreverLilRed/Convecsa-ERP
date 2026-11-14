package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaComiteActa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaComiteActaRepository extends JpaRepository<SomaComiteActa, Long> {

    @EntityGraph(attributePaths = {"actaDoc"})
    List<SomaComiteActa> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);

    Optional<SomaComiteActa> findByProyectoIdAndNumeroActa(Long proyectoId, String numeroActa);
}
