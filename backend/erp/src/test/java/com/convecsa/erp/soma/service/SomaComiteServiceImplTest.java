package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.CargoComiteSoma;
import com.convecsa.erp.soma.domain.RepresentacionComiteSoma;
import com.convecsa.erp.soma.domain.TipoRequerimientoComiteSoma;
import com.convecsa.erp.soma.dto.SomaComiteMiembroDTO;
import com.convecsa.erp.soma.dto.SomaComiteMiembroRequest;
import com.convecsa.erp.soma.dto.SomaComiteRequerimientoDTO;
import com.convecsa.erp.soma.entity.SomaComiteMiembro;
import com.convecsa.erp.soma.repository.SomaComiteActaRepository;
import com.convecsa.erp.soma.repository.SomaComiteMiembroRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.impl.SomaComiteServiceImpl;
import java.time.LocalDate;
import java.time.ZoneId;
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
class SomaComiteServiceImplTest {

    private static final Long PROYECTO_ID = 10L;
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Mock
    private SomaComiteMiembroRepository somaComiteMiembroRepository;

    @Mock
    private SomaComiteActaRepository somaComiteActaRepository;

    @Mock
    private SomaTrabajadorObraRepository somaTrabajadorObraRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @InjectMocks
    private SomaComiteServiceImpl servicio;

    private Proyecto proyecto;

    @BeforeEach
    void stubProyecto() {
        proyecto = new Proyecto();
        proyecto.setId(PROYECTO_ID);
        lenient().when(proyectoRepository.findById(PROYECTO_ID)).thenReturn(Optional.of(proyecto));
        lenient()
                .when(somaComiteMiembroRepository.findByProyectoIdOrderByActivoDescFechaInicioDescIdDesc(PROYECTO_ID))
                .thenReturn(List.of());
        lenient()
                .when(somaComiteMiembroRepository.save(any(SomaComiteMiembro.class)))
                .thenAnswer(invocation -> {
                    SomaComiteMiembro miembro = invocation.getArgument(0);
                    if (miembro.getId() == null) {
                        miembro.setId(1L);
                    }
                    return miembro;
                });
    }

    @Test
    void diecinueveActivosRequierenSupervisor() {
        stubActivos(19);

        SomaComiteRequerimientoDTO dto = servicio.requerimiento(PROYECTO_ID);

        assertEquals(TipoRequerimientoComiteSoma.SUPERVISOR_SST, dto.getTipoRequerido());
        assertEquals(19, dto.getTrabajadoresActivos());
        assertTrue(dto.getAvisos().contains("Se espera un único supervisor SST activo"));
    }

    @Test
    void veinteActivosRequierenSubcomite() {
        stubActivos(20);

        SomaComiteRequerimientoDTO dto = servicio.requerimiento(PROYECTO_ID);

        assertEquals(TipoRequerimientoComiteSoma.SUBCOMITE, dto.getTipoRequerido());
        assertEquals(20, dto.getTrabajadoresActivos());
        assertFalse(dto.getAvisos().contains("La representación no es paritaria"));
        assertTrue(dto.getAvisos().contains("Falta un presidente activo"));
        assertTrue(dto.getAvisos().contains("Falta un secretario activo"));
    }

    @Test
    void subcomiteAvisaParidadYCargosPeroAltaNoFalla() {
        stubActivos(20);
        when(somaComiteMiembroRepository.findByProyectoIdOrderByActivoDescFechaInicioDescIdDesc(PROYECTO_ID))
                .thenReturn(List.of(
                        miembro(1L, RepresentacionComiteSoma.EMPLEADOR, CargoComiteSoma.MIEMBRO, true, true),
                        miembro(2L, RepresentacionComiteSoma.EMPLEADOR, CargoComiteSoma.MIEMBRO, true, true),
                        miembro(3L, RepresentacionComiteSoma.TRABAJADORES, CargoComiteSoma.MIEMBRO, true, true)));

        SomaComiteRequerimientoDTO dto = servicio.requerimiento(PROYECTO_ID);

        assertEquals(TipoRequerimientoComiteSoma.SUBCOMITE, dto.getTipoRequerido());
        assertEquals(2, dto.getTitularesEmpleador());
        assertEquals(1, dto.getTitularesTrabajadores());
        assertTrue(dto.getAvisos().contains("La representación no es paritaria"));
        assertTrue(dto.getAvisos().contains("Falta un presidente activo"));
        assertTrue(dto.getAvisos().contains("Falta un secretario activo"));

        Persona persona = persona(99L, "Ana", "Lopez");
        when(personaRepository.findById(99L)).thenReturn(Optional.of(persona));

        SomaComiteMiembroRequest request = new SomaComiteMiembroRequest();
        request.setPersonaId(99L);
        request.setRepresentacion(RepresentacionComiteSoma.TRABAJADORES);
        request.setCargoComite(CargoComiteSoma.PRESIDENTE);
        request.setFechaInicio(LocalDate.of(2026, 1, 1));

        SomaComiteMiembroDTO creado = servicio.crearMiembro(PROYECTO_ID, request);

        assertEquals(99L, creado.getPersonaId());
        assertEquals(CargoComiteSoma.PRESIDENTE, creado.getCargoComite());
        assertTrue(creado.isActivo());
        assertTrue(creado.isTitular());
    }

    @Test
    void bajaDeActivoYSegundaBajaFalla() {
        SomaComiteMiembro miembro =
                miembro(7L, RepresentacionComiteSoma.EMPLEADOR, CargoComiteSoma.SUPERVISOR_SST, true, true);
        when(somaComiteMiembroRepository.findByIdAndProyectoId(7L, PROYECTO_ID)).thenReturn(Optional.of(miembro));

        SomaComiteMiembroDTO baja = servicio.darBaja(PROYECTO_ID, 7L);

        assertFalse(baja.isActivo());
        assertEquals(LocalDate.now(LIMA), baja.getFechaFin());

        IllegalArgumentException segunda =
                assertThrows(IllegalArgumentException.class, () -> servicio.darBaja(PROYECTO_ID, 7L));
        assertEquals("El miembro ya está dado de baja", segunda.getMessage());
    }

    private void stubActivos(int cantidad) {
        List<ContratoCc> contratos = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            contratos.add(contratoCc(persona((long) (i + 1), "Nombre" + i, "Apellido" + i)));
        }
        when(somaTrabajadorObraRepository.buscarActivos(eq(PROYECTO_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(contratos);
    }

    private SomaComiteMiembro miembro(
            Long id,
            RepresentacionComiteSoma representacion,
            CargoComiteSoma cargo,
            boolean titular,
            boolean activo) {
        SomaComiteMiembro miembro = new SomaComiteMiembro();
        miembro.setId(id);
        miembro.setProyecto(proyecto);
        miembro.setPersona(persona(id, "Miembro", String.valueOf(id)));
        miembro.setRepresentacion(representacion);
        miembro.setCargoComite(cargo);
        miembro.setTitular(titular);
        miembro.setFechaInicio(LocalDate.of(2026, 1, 1));
        miembro.setActivo(activo);
        return miembro;
    }

    private static Persona persona(Long id, String nombres, String apellidos) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        return persona;
    }

    private static ContratoCc contratoCc(Persona persona) {
        Contrato contrato = new Contrato();
        contrato.setId(persona.getId());
        contrato.setPersona(persona);
        contrato.setActivo(true);
        ContratoCc cc = new ContratoCc();
        cc.setContrato(contrato);
        return cc;
    }
}
