package com.convecsa.erp.mof.service.impl;

import com.convecsa.erp.common.exception.MofPuestoNoEncontradoException;
import com.convecsa.erp.mof.domain.MofRequisitoTipo;
import com.convecsa.erp.mof.dto.MofPuestoDTO;
import com.convecsa.erp.mof.dto.MofPuestoRequest;
import com.convecsa.erp.mof.dto.MofPuestoResumenDTO;
import com.convecsa.erp.mof.entity.MofFuncion;
import com.convecsa.erp.mof.entity.MofPuesto;
import com.convecsa.erp.mof.entity.MofRequisito;
import com.convecsa.erp.mof.repository.MofFuncionRepository;
import com.convecsa.erp.mof.repository.MofPuestoRepository;
import com.convecsa.erp.mof.repository.MofRequisitoRepository;
import com.convecsa.erp.mof.service.InterfazMofService;
import com.convecsa.erp.mof.support.MofDocumentoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MofServiceImpl implements InterfazMofService {

    private final MofPuestoRepository puestoRepository;
    private final MofFuncionRepository funcionRepository;
    private final MofRequisitoRepository requisitoRepository;
    private final MofDocumentoService documentoService;

    @Override
    @Transactional(readOnly = true)
    public List<MofPuestoResumenDTO> listar() {
        return puestoRepository.findAllByOrderByCodigoAsc().stream()
                .map(puesto -> new MofPuestoResumenDTO(
                        puesto.getId(), puesto.getCodigo(), puesto.getCargo(), puesto.getGrupo()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MofPuestoDTO obtener(Long id) {
        return toDto(cargar(id));
    }

    @Override
    @Transactional
    public MofPuestoDTO guardar(Long id, MofPuestoRequest request) {
        MofPuesto puesto = cargar(id);
        puesto.getFunciones().size();
        puesto.setProposito(request.getProposito().trim());
        puesto.reemplazarFunciones(request.getFunciones() == null ? List.of() : request.getFunciones());
        MofPuesto saved = puestoRepository.save(puesto);
        return toDto(
                saved,
                saved.getFunciones(),
                requisitoRepository.findByPuestoIdOrderByOrdenAsc(saved.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long id) {
        return documentoService.render(List.of(toDto(cargar(id))));
    }

    @Override
    @Transactional(readOnly = true)
    public String documentoCompleto() {
        return documentoService.render(puestoRepository.findAllByOrderByCodigoAsc().stream()
                .map(this::toDtoHydrated)
                .toList());
    }

    private MofPuesto cargar(Long id) {
        return puestoRepository.findById(id)
                .orElseThrow(() -> new MofPuestoNoEncontradoException("No se encontró el puesto MOF"));
    }

    private MofPuestoDTO toDto(MofPuesto puesto) {
        return toDto(puesto,
                funcionRepository.findByPuestoIdOrderByOrdenAsc(puesto.getId()),
                requisitoRepository.findByPuestoIdOrderByOrdenAsc(puesto.getId()));
    }

    private MofPuestoDTO toDtoHydrated(MofPuesto puesto) {
        return toDto(puesto,
                funcionRepository.findByPuestoIdOrderByOrdenAsc(puesto.getId()),
                requisitoRepository.findByPuestoIdOrderByOrdenAsc(puesto.getId()));
    }

    private static MofPuestoDTO toDto(
            MofPuesto puesto, List<MofFuncion> funciones, List<MofRequisito> requisitos) {
        return MofPuestoDTO.builder()
                .id(puesto.getId())
                .codigo(puesto.getCodigo())
                .cargo(puesto.getCargo())
                .grupo(puesto.getGrupo())
                .proposito(puesto.getProposito())
                .superior(puesto.getSuperior())
                .subordinados(puesto.getSubordinados())
                .funciones(funciones.stream().map(MofFuncion::getTexto).toList())
                .educacion(textos(requisitos, MofRequisitoTipo.EDUCACION))
                .experiencia(textos(requisitos, MofRequisitoTipo.EXPERIENCIA))
                .competencias(textos(requisitos, MofRequisitoTipo.COMPETENCIA))
                .build();
    }

    private static List<String> textos(List<MofRequisito> requisitos, MofRequisitoTipo tipo) {
        return requisitos.stream()
                .filter(item -> item.getTipo() == tipo)
                .map(MofRequisito::getTexto)
                .toList();
    }
}
