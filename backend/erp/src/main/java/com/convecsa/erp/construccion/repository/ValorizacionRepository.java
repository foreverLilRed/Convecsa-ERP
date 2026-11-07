package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.domain.TipoValorizacion;
import com.convecsa.erp.construccion.entity.Valorizacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValorizacionRepository extends JpaRepository<Valorizacion, Long> {

    @EntityGraph(attributePaths = {"items", "factura"})
    List<Valorizacion> findByProyectoIdOrderByNumeroDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"items", "items.partida", "items.adicional", "factura"})
    Optional<Valorizacion> findByIdAndProyectoId(Long id, Long proyectoId);

    @EntityGraph(attributePaths = {"items", "items.partida", "items.adicional"})
    Optional<Valorizacion> findByProyectoIdAndTipoAndAnioAndMes(
            Long proyectoId, TipoValorizacion tipo, int anio, int mes);

    List<Valorizacion> findByProyectoIdAndTipoAndEstadoOrderByAnioDescMesDesc(
            Long proyectoId, TipoValorizacion tipo, EstadoValorizacion estado);

    @EntityGraph(attributePaths = "items")
    List<Valorizacion> findByProyectoIdAndEstado(Long proyectoId, EstadoValorizacion estado);

    Optional<Valorizacion> findFirstByProyectoIdOrderByNumeroDesc(Long proyectoId);
}
