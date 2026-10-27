package com.convecsa.erp.obra.repository;

import com.convecsa.erp.obra.domain.EstadoObra;
import com.convecsa.erp.obra.entity.Obra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ObraRepository extends JpaRepository<Obra, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    long countByEstado(EstadoObra estado);

    @Query("""
            SELECT o FROM Obra o
            WHERE (:filtrarEstado = false OR o.estado = :estado)
              AND (:filtrarTexto = false
                   OR LOWER(o.codigo) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(o.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(o.clienteNombre) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(COALESCE(o.ubicacion, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY o.createdAt DESC
            """)
    Page<Obra> buscar(
            @Param("filtrarTexto") boolean filtrarTexto,
            @Param("q") String q,
            @Param("filtrarEstado") boolean filtrarEstado,
            @Param("estado") EstadoObra estado,
            Pageable pageable);
}
