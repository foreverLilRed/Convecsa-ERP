package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
import com.convecsa.erp.soma.entity.SomaCapacitacion;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaCapacitacionRepository extends JpaRepository<SomaCapacitacion, Long> {

    @EntityGraph(attributePaths = {"proyecto", "evidencia", "asistentes", "asistentes.persona"})
    List<SomaCapacitacion> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"proyecto", "evidencia", "asistentes", "asistentes.persona"})
    List<SomaCapacitacion> findAllByOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"proyecto", "evidencia", "asistentes", "asistentes.persona"})
    List<SomaCapacitacion> findByProyectoIdInOrderByFechaDescIdDesc(Collection<Long> proyectoIds);

    @EntityGraph(attributePaths = {"proyecto", "evidencia", "asistentes", "asistentes.persona"})
    List<SomaCapacitacion> findByProyectoIsNullOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"proyecto", "evidencia", "asistentes", "asistentes.persona"})
    List<SomaCapacitacion> findByFechaBetweenOrderByFechaDescIdDesc(LocalDate desde, LocalDate hasta);

    boolean existsByProyectoIdAndTipoAndAsistentesPersonaIdAndAsistentesAprobadoTrue(
            Long proyectoId, TipoCapacitacionSoma tipo, Long personaId);
}
