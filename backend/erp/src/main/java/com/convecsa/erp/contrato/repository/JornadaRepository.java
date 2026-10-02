package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.catalogo.Jornada;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JornadaRepository extends JpaRepository<Jornada, Long> {

    List<Jornada> findByActivoTrueOrderByIdAsc();
}
