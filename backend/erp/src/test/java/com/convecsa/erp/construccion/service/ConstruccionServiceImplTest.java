package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.service.impl.ConstruccionServiceImpl;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConstruccionServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ContratoCcRepository contratoCcRepository;

    @InjectMocks
    private ConstruccionServiceImpl servicio;

    @Test
    void listarPersonalExponeFechasDeIngresoYCese() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(3L);
        when(proyectoRepository.findById(3L)).thenReturn(Optional.of(proyecto));

        Persona conCese = new Persona();
        conCese.setId(11L);
        conCese.setNombres("Ana");
        conCese.setApellidos("Rojas");
        conCese.setNumeroDocumento("87654321");
        Contrato contratoConCese = new Contrato();
        contratoConCese.setId(22L);
        contratoConCese.setPersona(conCese);
        contratoConCese.setFechaInicio(LocalDate.of(2026, 3, 10));
        contratoConCese.setFechaFin(LocalDate.of(2026, 11, 15));
        ContratoCc ccConCese = new ContratoCc();
        ccConCese.setContrato(contratoConCese);
        ccConCese.setCategoria(CategoriaObrera.OPERARIO);

        Persona vigente = new Persona();
        vigente.setId(12L);
        vigente.setNombres("Luis");
        vigente.setApellidos("Paredes");
        vigente.setNumeroDocumento("11223344");
        Contrato contratoVigente = new Contrato();
        contratoVigente.setId(23L);
        contratoVigente.setPersona(vigente);
        contratoVigente.setFechaInicio(LocalDate.of(2026, 1, 5));
        contratoVigente.setFechaFin(null);
        ContratoCc ccVigente = new ContratoCc();
        ccVigente.setContrato(contratoVigente);
        ccVigente.setCategoria(CategoriaObrera.PEON);

        when(contratoCcRepository.findActivosByProyectoId(3L)).thenReturn(List.of(ccConCese, ccVigente));

        List<PersonalObraDTO> personal = servicio.listarPersonal(3L);

        assertEquals(2, personal.size());
        assertEquals(LocalDate.of(2026, 3, 10), personal.get(0).getFechaIngreso());
        assertEquals(LocalDate.of(2026, 11, 15), personal.get(0).getFechaCese());
        assertEquals(LocalDate.of(2026, 1, 5), personal.get(1).getFechaIngreso());
        assertNull(personal.get(1).getFechaCese());
    }
}
