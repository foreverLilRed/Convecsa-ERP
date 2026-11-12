package com.convecsa.erp.soma.repository;

import com.convecsa.erp.construccion.entity.ContratoCc;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SomaTrabajadorObraRepository extends JpaRepository<ContratoCc, Long> {

    @Query("""
            select cc from ContratoCc cc
            join fetch cc.contrato c
            join fetch c.persona
            where cc.proyecto.id = :proyectoId
              and c.activo = true
              and (c.fechaInicio is null or c.fechaInicio <= :hasta)
              and (c.fechaFin is null or c.fechaFin >= :desde)
            order by c.persona.apellidos, c.persona.nombres
            """)
    List<ContratoCc> buscarActivos(
            @Param("proyectoId") Long proyectoId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    default List<ContratoCc> buscarActivosEnMes(Long proyectoId, int anio, int mes) {
        YearMonth periodo = YearMonth.of(anio, mes);
        return buscarActivos(proyectoId, periodo.atDay(1), periodo.atEndOfMonth());
    }
}
