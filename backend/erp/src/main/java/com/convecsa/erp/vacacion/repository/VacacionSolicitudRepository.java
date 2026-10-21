package com.convecsa.erp.vacacion.repository;

import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
import com.convecsa.erp.vacacion.entity.VacacionSolicitud;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VacacionSolicitudRepository extends JpaRepository<VacacionSolicitud, Long> {

    @EntityGraph(attributePaths = {"persona", "contrato"})
    @Query("""
            SELECT v FROM VacacionSolicitud v
            WHERE (:filtrarEstado = false OR v.estado = :estado)
              AND (:filtrarTipo = false OR v.tipo = :tipo)
              AND (:filtrarTexto = false OR LOWER(CONCAT(v.persona.apellidos, ' ', v.persona.nombres)) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR v.persona.numeroDocumento LIKE CONCAT('%', :q, '%'))
            ORDER BY v.createdAt DESC
            """)
    Page<VacacionSolicitud> buscar(
            @Param("filtrarTexto") boolean filtrarTexto,
            @Param("q") String q,
            @Param("filtrarTipo") boolean filtrarTipo,
            @Param("tipo") TipoVacacion tipo,
            @Param("filtrarEstado") boolean filtrarEstado,
            @Param("estado") EstadoVacacion estado,
            Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "contrato"})
    @Query("""
            SELECT v FROM VacacionSolicitud v
            WHERE v.estado = :estado
              AND v.fechaInicio <= :fin
              AND v.fechaFin >= :inicio
            """)
    List<VacacionSolicitud> findAprobadasEnRango(
            @Param("estado") EstadoVacacion estado,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VacacionSolicitud v
            WHERE v.persona.id = :personaId
              AND v.estado = :estado
              AND v.fechaInicio <= :fecha
              AND v.fechaFin >= :fecha
            """)
    boolean existeEnFecha(
            @Param("personaId") Long personaId,
            @Param("estado") EstadoVacacion estado,
            @Param("fecha") LocalDate fecha);

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VacacionSolicitud v
            WHERE v.persona.id = :personaId
              AND v.estado IN :estados
              AND v.fechaInicio <= :fin
              AND v.fechaFin >= :inicio
            """)
    boolean existeCruce(
            @Param("personaId") Long personaId,
            @Param("estados") Collection<EstadoVacacion> estados,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VacacionSolicitud v
            WHERE v.persona.id = :personaId
              AND v.estado IN :estados
              AND v.periodoInicio <= :periodoFin
              AND v.periodoFin >= :periodoInicio
            """)
    boolean existeCrucePeriodo(
            @Param("personaId") Long personaId,
            @Param("estados") Collection<EstadoVacacion> estados,
            @Param("periodoInicio") LocalDate periodoInicio,
            @Param("periodoFin") LocalDate periodoFin);

    @Query("""
            SELECT COALESCE(SUM(v.diasSolicitados), 0) FROM VacacionSolicitud v
            WHERE v.persona.id = :personaId
              AND v.periodoInicio = :periodoInicio
              AND v.periodoFin = :periodoFin
              AND v.estado = :estado
            """)
    long sumDiasPeriodo(
            @Param("personaId") Long personaId,
            @Param("periodoInicio") LocalDate periodoInicio,
            @Param("periodoFin") LocalDate periodoFin,
            @Param("estado") EstadoVacacion estado);

    long countByEstado(EstadoVacacion estado);

    long countByTipo(TipoVacacion tipo);

    long countByEstadoAndFechaFinBefore(EstadoVacacion estado, LocalDate fecha);

    @Query("SELECT COALESCE(SUM(v.diasSolicitados), 0) FROM VacacionSolicitud v WHERE v.estado = :estado")
    long sumDiasByEstado(@Param("estado") EstadoVacacion estado);
}
