package com.convecsa.erp.nomina.repository;

import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import com.convecsa.erp.nomina.entity.AfpTasa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfpTasaRepository extends JpaRepository<AfpTasa, Long> {
    Optional<AfpTasa> findByAfpIdAndTipoComisionAndActivoTrue(Long afpId, TipoComisionAfp tipoComision);
}
