package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.ProyectoCodigoDuplicadoException;
import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.domain.TipoRecursoApu;
import com.convecsa.erp.construccion.domain.TipoVersionPresupuesto;
import com.convecsa.erp.construccion.dto.ApuPartidaRequest;
import com.convecsa.erp.construccion.dto.ComparativoPresupuestoDTO;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoRequest;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.dto.PresupuestoDTO;
import com.convecsa.erp.construccion.dto.PresupuestoVersionRequest;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.ProyectoRequest;
import com.convecsa.erp.construccion.dto.ProyectoResponsableRequest;
import com.convecsa.erp.construccion.entity.ApuPartida;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.CcParametroAnual;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.CcParametroAnualRepository;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.entity.ValeSalidaItem;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaItemRepository;
import com.convecsa.erp.construccion.service.impl.ConstruccionServiceImpl;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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
    private ProyectoResponsableRepository proyectoResponsableRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private CcParametroAnualRepository parametroRepository;

    @Mock
    private ContratoCcRepository contratoCcRepository;

    @Mock
    private PresupuestoVersionRepository presupuestoVersionRepository;

    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;

    @Mock
    private PeriodoCcRepository periodoCcRepository;

    @Mock
    private BoletaCcRepository boletaCcRepository;

    @Mock
    private ValeSalidaItemRepository valeSalidaItemRepository;

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

    @Test
    void actualizarProyectoCambiaClienteVigenciaYEstado() {
        Proyecto proyecto = proyectoExistente();
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(proyectoRepository.existsByCodigoIgnoreCaseAndIdNot("OB-01", 7L)).thenReturn(false);
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parametroRepository.findByAnio(LocalDate.now().getYear())).thenReturn(Optional.of(parametrosUit()));

        ProyectoRequest request = requestBase();
        request.setNombre("Obra Norte actualizada");
        request.setClienteRazon("Cliente SAC");
        request.setClienteRuc("20123456789");
        request.setFechaInicio(LocalDate.of(2026, 2, 1));
        request.setFechaFin(LocalDate.of(2026, 11, 30));
        request.setEstadoObra(EstadoObra.PARALIZADA);
        request.setPorcentajeRetencion(new BigDecimal("10"));

        ProyectoDTO dto = servicio.actualizarProyecto(7L, request);

        assertEquals("Obra Norte actualizada", dto.getNombre());
        assertEquals("Cliente SAC", dto.getClienteRazon());
        assertEquals("20123456789", dto.getClienteRuc());
        assertEquals(LocalDate.of(2026, 2, 1), dto.getFechaInicio());
        assertEquals(LocalDate.of(2026, 11, 30), dto.getFechaFin());
        assertEquals(EstadoObra.PARALIZADA, dto.getEstadoObra());
        assertEquals(new BigDecimal("10.00"), dto.getPorcentajeRetencion());
        assertTrue(dto.isActivo());
    }

    @Test
    void actualizarProyectoRechazaCodigoDuplicado() {
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyectoExistente()));
        when(proyectoRepository.existsByCodigoIgnoreCaseAndIdNot("OB-02", 7L)).thenReturn(true);

        ProyectoRequest request = requestBase();
        request.setCodigo("ob-02");

        assertThrows(ProyectoCodigoDuplicadoException.class, () -> servicio.actualizarProyecto(7L, request));
    }

    @Test
    void actualizarProyectoInactivaLaObra() {
        Proyecto proyecto = proyectoExistente();
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(proyectoRepository.existsByCodigoIgnoreCaseAndIdNot("OB-01", 7L)).thenReturn(false);
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parametroRepository.findByAnio(LocalDate.now().getYear())).thenReturn(Optional.of(parametrosUit()));

        ProyectoRequest request = requestBase();
        request.setActivo(false);

        ProyectoDTO dto = servicio.actualizarProyecto(7L, request);

        assertFalse(dto.isActivo());
        assertFalse(proyecto.isActivo());
    }

    @Test
    void guardarResponsablesRechazaRolDuplicado() {
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyectoExistente()));

        ProyectoResponsableRequest primero = new ProyectoResponsableRequest();
        primero.setPersonaId(11L);
        primero.setRol(RolResponsableObra.RESIDENTE);
        ProyectoResponsableRequest segundo = new ProyectoResponsableRequest();
        segundo.setPersonaId(12L);
        segundo.setRol(RolResponsableObra.RESIDENTE);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.guardarResponsables(7L, List.of(primero, segundo)));
        assertEquals("Hay roles de responsable duplicados", error.getMessage());
    }

    @Test
    void crearVersionPresupuestoNaceInicialEnBorrador() {
        Proyecto proyecto = proyectoExistente();
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(presupuestoVersionRepository.existsByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.BORRADOR))
                .thenReturn(false);
        AtomicReference<PresupuestoVersion> guardada = new AtomicReference<>();
        when(presupuestoVersionRepository.save(any(PresupuestoVersion.class))).thenAnswer(invocation -> {
            PresupuestoVersion version = invocation.getArgument(0);
            version.setId(10L);
            version.setCreatedAt(Instant.parse("2026-11-06T20:00:00Z"));
            guardada.set(version);
            return version;
        });
        when(presupuestoVersionRepository.findByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> guardada.get() == null ? List.of() : List.of(guardada.get()));
        when(presupuestoVersionRepository.findByIdAndProyectoId(10L, 7L))
                .thenAnswer(invocation -> Optional.ofNullable(guardada.get()));
        when(partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(10L)).thenReturn(List.of());
        when(partidaPresupuestoRepository.countByVersionId(10L)).thenReturn(0);

        PresupuestoDTO dto = servicio.crearVersionPresupuesto(7L, new PresupuestoVersionRequest());

        assertEquals(TipoVersionPresupuesto.INICIAL, dto.getVersion().getTipo());
        assertEquals(EstadoVersionPresupuesto.BORRADOR, dto.getVersion().getEstado());
        assertEquals(1, dto.getVersion().getNumero());
        assertEquals(10L, dto.getVersion().getId());
    }

    @Test
    void guardarPartidasDerivaPrecioUnitarioDesdeApu() {
        Proyecto proyecto = proyectoExistente();
        PresupuestoVersion version = versionBorrador(proyecto);
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(presupuestoVersionRepository.findByIdAndProyectoId(10L, 7L)).thenReturn(Optional.of(version));
        AtomicReference<PartidaPresupuesto> guardada = new AtomicReference<>();
        when(partidaPresupuestoRepository.save(any(PartidaPresupuesto.class))).thenAnswer(invocation -> {
            PartidaPresupuesto partida = invocation.getArgument(0);
            partida.setId(3L);
            guardada.set(partida);
            return partida;
        });
        when(partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(10L))
                .thenAnswer(invocation -> guardada.get() == null ? List.of() : List.of(guardada.get()));
        when(presupuestoVersionRepository.findByProyectoIdOrderByNumeroDesc(7L)).thenReturn(List.of(version));
        when(partidaPresupuestoRepository.countByVersionId(10L)).thenReturn(1);

        PartidaPresupuestoRequest partida = new PartidaPresupuestoRequest();
        partida.setCodigo("01.01");
        partida.setDescripcion("Excavación");
        partida.setMetrado(new BigDecimal("3"));
        partida.setPrecioUnitario(new BigDecimal("99"));
        ApuPartidaRequest jornal = recursoApu(TipoRecursoApu.MANO_OBRA, "2", "10");
        ApuPartidaRequest material = recursoApu(TipoRecursoApu.MATERIAL, "1", "5");
        partida.setApu(List.of(jornal, material));

        PresupuestoDTO dto = servicio.guardarPartidasPresupuesto(7L, 10L, List.of(partida));

        assertEquals(new BigDecimal("25.00"), dto.getVersion().getPartidas().get(0).getPrecioUnitario());
        assertEquals(new BigDecimal("75.00"), dto.getVersion().getPartidas().get(0).getTotal());
    }

    @Test
    void guardarPartidasRechazaVersionVigente() {
        Proyecto proyecto = proyectoExistente();
        PresupuestoVersion version = versionBorrador(proyecto);
        version.setEstado(EstadoVersionPresupuesto.VIGENTE);
        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(presupuestoVersionRepository.findByIdAndProyectoId(10L, 7L)).thenReturn(Optional.of(version));

        PartidaPresupuestoRequest partida = new PartidaPresupuestoRequest();
        partida.setCodigo("01.01");
        partida.setDescripcion("Excavación");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.guardarPartidasPresupuesto(7L, 10L, List.of(partida)));
        assertEquals("Solo se puede editar un presupuesto en borrador", error.getMessage());
    }

    @Test
    void crearVersionActualizadoCopiaPartidasDeLaVigente() {
        Proyecto proyecto = proyectoExistente();
        PresupuestoVersion vigente = versionBorrador(proyecto);
        vigente.setEstado(EstadoVersionPresupuesto.VIGENTE);
        PartidaPresupuesto origen = new PartidaPresupuesto();
        origen.setId(3L);
        origen.setVersion(vigente);
        origen.setCodigo("01.01");
        origen.setDescripcion("Excavación");
        origen.setMetrado(new BigDecimal("3.0000"));
        origen.setPrecioUnitario(new BigDecimal("25.00"));
        origen.setTotal(new BigDecimal("75.00"));
        origen.setOrden(0);
        ApuPartida apu = new ApuPartida();
        apu.setTipo(TipoRecursoApu.MANO_OBRA);
        apu.setDescripcion("Jornal");
        apu.setCantidad(new BigDecimal("2.0000"));
        apu.setPrecioUnitario(new BigDecimal("10.00"));
        apu.setParcial(new BigDecimal("20.00"));
        origen.getApu().add(apu);
        vigente.getPartidas().add(origen);

        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(presupuestoVersionRepository.existsByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.BORRADOR))
                .thenReturn(false);
        when(presupuestoVersionRepository.findByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.VIGENTE))
                .thenReturn(Optional.of(vigente));
        when(partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(10L)).thenReturn(List.of(origen));
        AtomicReference<PresupuestoVersion> copia = new AtomicReference<>();
        when(presupuestoVersionRepository.save(any(PresupuestoVersion.class))).thenAnswer(invocation -> {
            PresupuestoVersion version = invocation.getArgument(0);
            version.setId(11L);
            version.setCreatedAt(Instant.parse("2026-11-06T21:00:00Z"));
            copia.set(version);
            return version;
        });
        when(presupuestoVersionRepository.findByProyectoIdOrderByNumeroDesc(7L))
                .thenReturn(List.of(vigente))
                .thenAnswer(invocation -> List.of(copia.get(), vigente));
        when(presupuestoVersionRepository.findByIdAndProyectoId(11L, 7L))
                .thenAnswer(invocation -> Optional.ofNullable(copia.get()));
        when(partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(11L))
                .thenAnswer(invocation -> copia.get() == null ? List.of() : copia.get().getPartidas());
        when(partidaPresupuestoRepository.countByVersionId(any())).thenReturn(1);

        PresupuestoVersionRequest request = new PresupuestoVersionRequest();
        request.setTipo(TipoVersionPresupuesto.ACTUALIZADO);
        PresupuestoDTO dto = servicio.crearVersionPresupuesto(7L, request);

        assertEquals(TipoVersionPresupuesto.ACTUALIZADO, dto.getVersion().getTipo());
        assertEquals(EstadoVersionPresupuesto.BORRADOR, dto.getVersion().getEstado());
        assertEquals(2, dto.getVersion().getNumero());
        assertEquals("01.01", dto.getVersion().getPartidas().get(0).getCodigo());
        assertEquals(TipoRecursoApu.MANO_OBRA, dto.getVersion().getPartidas().get(0).getApu().get(0).getTipo());
    }

    @Test
    void comparativoPresupuestoContrastaMetaMoConBoletas() {
        Proyecto proyecto = proyectoExistente();
        proyecto.setCentroCosto("CC-OBRA");
        PresupuestoVersion vigente = versionBorrador(proyecto);
        vigente.setEstado(EstadoVersionPresupuesto.VIGENTE);
        PartidaPresupuesto partida = new PartidaPresupuesto();
        partida.setCentroCosto("CC-1");
        partida.setMetrado(new BigDecimal("2"));
        ApuPartida apu = new ApuPartida();
        apu.setTipo(TipoRecursoApu.MANO_OBRA);
        apu.setParcial(new BigDecimal("10.00"));
        partida.getApu().add(apu);

        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(presupuestoVersionRepository.findByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.VIGENTE))
                .thenReturn(Optional.of(vigente));
        when(partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(10L)).thenReturn(List.of(partida));

        PeriodoCc periodo = new PeriodoCc();
        periodo.setId(4L);
        when(periodoCcRepository.findByProyectoIdOrderByFechaDesdeDesc(7L)).thenReturn(List.of(periodo));

        Contrato contrato = new Contrato();
        contrato.setId(22L);
        BoletaCc boleta = new BoletaCc();
        boleta.setContrato(contrato);
        boleta.setTotalHaberes(new BigDecimal("8.00"));
        boleta.setTotalAportesEmpleador(new BigDecimal("2.00"));
        when(boletaCcRepository.findByPeriodoIdIn(List.of(4L))).thenReturn(List.of(boleta));
        ContratoCc contratoCc = new ContratoCc();
        contratoCc.setCentroCosto("CC-1");
        when(contratoCcRepository.findByContratoId(22L)).thenReturn(Optional.of(contratoCc));

        ComparativoPresupuestoDTO dto = servicio.comparativoPresupuesto(7L);

        assertEquals(new BigDecimal("20.00"), dto.getMetaMo());
        assertEquals(new BigDecimal("10.00"), dto.getRealMo());
        assertEquals(new BigDecimal("-10.00"), dto.getDesvio());
        assertEquals(1, dto.getCentros().size());
        assertEquals("CC-1", dto.getCentros().get(0).getCentroCosto());
        assertEquals(new BigDecimal("0.00"), dto.getMetaMaterial());
        assertEquals(new BigDecimal("0.00"), dto.getRealMaterial());
    }

    @Test
    void comparativoPresupuestoContrastaMetaMaterialConVales() {
        Proyecto proyecto = proyectoExistente();
        proyecto.setCentroCosto("CC-OBRA");
        PresupuestoVersion vigente = versionBorrador(proyecto);
        vigente.setEstado(EstadoVersionPresupuesto.VIGENTE);
        PartidaPresupuesto partida = new PartidaPresupuesto();
        partida.setCentroCosto("CC-1");
        partida.setMetrado(new BigDecimal("2"));
        ApuPartida apu = new ApuPartida();
        apu.setTipo(TipoRecursoApu.MATERIAL);
        apu.setParcial(new BigDecimal("10.00"));
        partida.getApu().add(apu);

        when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        when(presupuestoVersionRepository.findByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.VIGENTE))
                .thenReturn(Optional.of(vigente));
        when(partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(10L)).thenReturn(List.of(partida));

        ValeSalida vale = new ValeSalida();
        vale.setCentroCosto("CC-1");
        ValeSalidaItem item = new ValeSalidaItem();
        item.setVale(vale);
        item.setCostoTotal(new BigDecimal("8.00"));
        when(valeSalidaItemRepository.findByValeAlmacenProyectoId(7L)).thenReturn(List.of(item));

        ComparativoPresupuestoDTO dto = servicio.comparativoPresupuesto(7L);

        assertEquals(new BigDecimal("20.00"), dto.getMetaMaterial());
        assertEquals(new BigDecimal("8.00"), dto.getRealMaterial());
        assertEquals(new BigDecimal("-12.00"), dto.getDesvioMaterial());
        assertEquals(new BigDecimal("0.00"), dto.getMetaMo());
    }

    private static Proyecto proyectoExistente() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setCodigo("OB-01");
        proyecto.setNombre("Obra Norte");
        proyecto.setEstadoObra(EstadoObra.EN_EJECUCION);
        proyecto.setActivo(true);
        return proyecto;
    }

    private static ProyectoRequest requestBase() {
        ProyectoRequest request = new ProyectoRequest();
        request.setCodigo("OB-01");
        request.setNombre("Obra Norte");
        request.setEstadoObra(EstadoObra.EN_EJECUCION);
        request.setActivo(true);
        return request;
    }

    private static CcParametroAnual parametrosUit() {
        CcParametroAnual parametros = new CcParametroAnual();
        parametros.setAnio(LocalDate.now().getYear());
        parametros.setUit(new BigDecimal("5350"));
        return parametros;
    }

    private static PresupuestoVersion versionBorrador(Proyecto proyecto) {
        PresupuestoVersion version = new PresupuestoVersion();
        version.setId(10L);
        version.setProyecto(proyecto);
        version.setTipo(TipoVersionPresupuesto.INICIAL);
        version.setEstado(EstadoVersionPresupuesto.BORRADOR);
        version.setNumero(1);
        version.setCreatedAt(Instant.parse("2026-11-06T20:00:00Z"));
        return version;
    }

    private static ApuPartidaRequest recursoApu(TipoRecursoApu tipo, String cantidad, String precio) {
        ApuPartidaRequest recurso = new ApuPartidaRequest();
        recurso.setTipo(tipo);
        recurso.setDescripcion(tipo.name());
        recurso.setCantidad(new BigDecimal(cantidad));
        recurso.setPrecioUnitario(new BigDecimal(precio));
        return recurso;
    }
}
