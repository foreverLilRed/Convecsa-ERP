package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.CronogramaPartida;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CronogramaPartidaRepository extends JpaRepository<CronogramaPartida, Long> {

    @EntityGraph(attributePaths = "partida")
    List<CronogramaPartida> findByProyectoIdOrderByCodigoAsc(Long proyectoId);

    void deleteByProyectoId(Long proyectoId);
}
