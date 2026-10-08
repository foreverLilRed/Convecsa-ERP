package com.convecsa.erp.mof.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.mof.domain.MofRequisitoTipo;
import com.convecsa.erp.mof.dto.MofPuestoDTO;
import com.convecsa.erp.mof.dto.MofPuestoRequest;
import com.convecsa.erp.mof.entity.MofFuncion;
import com.convecsa.erp.mof.entity.MofPuesto;
import com.convecsa.erp.mof.entity.MofRequisito;
import com.convecsa.erp.mof.repository.MofFuncionRepository;
import com.convecsa.erp.mof.repository.MofPuestoRepository;
import com.convecsa.erp.mof.repository.MofRequisitoRepository;
import com.convecsa.erp.mof.service.impl.MofServiceImpl;
import com.convecsa.erp.mof.support.MofDocumentoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MofServiceImplTest {

    @Mock
    private MofPuestoRepository puestoRepository;

    @Mock
    private MofFuncionRepository funcionRepository;

    @Mock
    private MofRequisitoRepository requisitoRepository;

    @Mock
    private MofDocumentoService documentoService;

    @InjectMocks
    private MofServiceImpl servicio;

    private MofPuesto puesto;

    @BeforeEach
    void setUp() {
        puesto = new MofPuesto();
        puesto.setId(1L);
        puesto.setCodigo("CVSA-MOF-P005");
        puesto.setCargo("RESIDENTE DE OBRA");
        puesto.setGrupo("STAFF DE OBRA");
        puesto.setProposito("Propósito original");
        puesto.setSuperior("Gerencia");
        puesto.setSubordinados("Equipo");
        MofFuncion existente = new MofFuncion();
        existente.setPuesto(puesto);
        existente.setOrden(0);
        existente.setTexto("Función previa");
        puesto.getFunciones().add(existente);
    }

    @Test
    void listarNoVacio() {
        when(puestoRepository.findAllByOrderByCodigoAsc()).thenReturn(List.of(puesto));

        assertEquals(1, servicio.listar().size());
        assertEquals("CVSA-MOF-P005", servicio.listar().get(0).getCodigo());
    }

    @Test
    void guardarActualizaPropositoYAgregaFuncion() {
        when(puestoRepository.findById(1L)).thenReturn(Optional.of(puesto));
        when(puestoRepository.save(any(MofPuesto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requisitoRepository.findByPuestoIdOrderByOrdenAsc(1L)).thenReturn(List.of());

        MofPuestoRequest request = new MofPuestoRequest();
        request.setProposito("Nuevo propósito del cargo");
        request.setFunciones(new ArrayList<>(List.of("Función previa", "Nueva función de control")));

        MofPuestoDTO dto = servicio.guardar(1L, request);

        assertEquals("Nuevo propósito del cargo", dto.getProposito());
        assertEquals(2, dto.getFunciones().size());
        assertTrue(dto.getFunciones().contains("Nueva función de control"));
        verify(puestoRepository).save(puesto);
    }

    @Test
    void documentoUsaRendererConCodigo() {
        when(puestoRepository.findById(1L)).thenReturn(Optional.of(puesto));
        when(funcionRepository.findByPuestoIdOrderByOrdenAsc(1L)).thenReturn(puesto.getFunciones());
        MofRequisito educacion = new MofRequisito();
        educacion.setTipo(MofRequisitoTipo.EDUCACION);
        educacion.setTexto("Ingeniería Civil");
        when(requisitoRepository.findByPuestoIdOrderByOrdenAsc(1L)).thenReturn(List.of(educacion));
        when(documentoService.render(any())).thenReturn("<html>CVSA-MOF-P005 Función previa</html>");

        String html = servicio.documento(1L);

        assertTrue(html.contains("CVSA-MOF-P005"));
        assertTrue(html.contains("Función previa"));
    }
}
