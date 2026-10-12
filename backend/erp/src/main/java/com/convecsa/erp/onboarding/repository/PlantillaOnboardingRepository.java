package com.convecsa.erp.onboarding.repository;

import com.convecsa.erp.onboarding.domain.AmbitoPlantilla;
import com.convecsa.erp.onboarding.entity.PlantillaOnboarding;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantillaOnboardingRepository extends JpaRepository<PlantillaOnboarding, Long> {

    List<PlantillaOnboarding> findByActivoTrueOrderByCodigoAsc();

    List<PlantillaOnboarding> findByActivoTrueAndAmbitoInOrderByCodigoAsc(Collection<AmbitoPlantilla> ambitos);

    List<PlantillaOnboarding> findByCodigoInAndActivoTrue(Collection<String> codigos);
}
