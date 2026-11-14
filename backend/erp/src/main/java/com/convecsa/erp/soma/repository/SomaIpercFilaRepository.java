package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaIpercFila;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaIpercFilaRepository extends JpaRepository<SomaIpercFila, Long> {

    @EntityGraph(attributePaths = "partida")
    List<SomaIpercFila> findByProyectoIdOrderByIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = "partida")
    Optional<SomaIpercFila> findByIdAndProyectoId(Long id, Long proyectoId);
}
