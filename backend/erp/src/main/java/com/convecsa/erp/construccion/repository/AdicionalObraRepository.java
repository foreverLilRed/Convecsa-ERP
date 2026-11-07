package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.domain.EstadoAdicionalObra;
import com.convecsa.erp.construccion.domain.TipoAdicionalObra;
import com.convecsa.erp.construccion.entity.AdicionalObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdicionalObraRepository extends JpaRepository<AdicionalObra, Long> {

    List<AdicionalObra> findByProyectoIdOrderByIdDesc(Long proyectoId);

    Optional<AdicionalObra> findByIdAndProyectoId(Long id, Long proyectoId);

    List<AdicionalObra> findByProyectoIdAndTipoAndEstado(
            Long proyectoId, TipoAdicionalObra tipo, EstadoAdicionalObra estado);
}
