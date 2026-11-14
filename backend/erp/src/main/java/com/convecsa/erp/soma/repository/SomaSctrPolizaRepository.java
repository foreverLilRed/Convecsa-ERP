package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import com.convecsa.erp.soma.entity.SomaSctrPoliza;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaSctrPolizaRepository extends JpaRepository<SomaSctrPoliza, Long> {

    @EntityGraph(attributePaths = {"nomina", "nomina.persona", "documentoObra"})
    List<SomaSctrPoliza> findByProyectoIdOrderByAnioDescMesDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"nomina", "nomina.persona", "documentoObra"})
    Optional<SomaSctrPoliza> findByIdAndProyectoId(Long id, Long proyectoId);

    boolean existsByProyectoIdAndAnioAndMes(Long proyectoId, int anio, int mes);

    @EntityGraph(attributePaths = {"nomina", "nomina.persona"})
    Optional<SomaSctrPoliza> findByProyectoIdAndAnioAndMesAndEstado(
            Long proyectoId, int anio, int mes, EstadoSctrPoliza estado);
}
