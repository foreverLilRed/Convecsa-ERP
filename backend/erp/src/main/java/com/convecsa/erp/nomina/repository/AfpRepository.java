package com.convecsa.erp.nomina.repository;

import com.convecsa.erp.nomina.catalogo.Afp;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfpRepository extends JpaRepository<Afp, Long> {

    List<Afp> findByActivoTrueOrderByIdAsc();

    Optional<Afp> findByCodigo(String codigo);
}
