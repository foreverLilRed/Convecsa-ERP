package com.convecsa.erp.asistencia.repository;

import com.convecsa.erp.asistencia.entity.AsistenciaConceptoVariable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaConceptoVariableRepository extends JpaRepository<AsistenciaConceptoVariable, Long> {

    List<AsistenciaConceptoVariable> findByAnioAndMesOrderByPersonaApellidosAscPersonaNombresAscIdAsc(int anio, int mes);

    List<AsistenciaConceptoVariable> findByAnioAndMesAndPersonaIdOrderByIdAsc(int anio, int mes, Long personaId);
}
