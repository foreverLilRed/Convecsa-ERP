package com.convecsa.erp.contrato.service.impl;

import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.dto.ContratosResumeDTO;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.service.InterfazContratoService;
import com.convecsa.erp.persona.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements InterfazContratoService {

    private final ContratoRepository contratoRepository;
    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public ContratosResumeDTO resume() {
        long conContratoActivo = contratoRepository.countDistinctPersonaIdByEstado(EstadoContrato.ACTIVO);
        long totalPersonas = personaRepository.count();
        long sinContratoActivo = Math.max(0, totalPersonas - conContratoActivo);
        return new ContratosResumeDTO(conContratoActivo, sinContratoActivo);
    }
}
