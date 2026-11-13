package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.dto.ValeSalidaDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaRequest;
import com.convecsa.erp.construccion.entity.Material;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.repository.MaterialRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaRepository;
import com.convecsa.erp.construccion.service.InterfazAlmacenService;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import com.convecsa.erp.soma.dto.SomaEntregaEppDTO;
import com.convecsa.erp.soma.dto.SomaEntregaEppItemRequest;
import com.convecsa.erp.soma.dto.SomaEntregaEppRequest;
import com.convecsa.erp.soma.entity.SomaEntregaEpp;
import com.convecsa.erp.soma.repository.SomaEntregaEppItemRepository;
import com.convecsa.erp.soma.repository.SomaEntregaEppRepository;
import com.convecsa.erp.soma.service.impl.SomaEppServiceImpl;
import com.convecsa.erp.soma.support.SomaEppKardexPdfService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaEppServiceImplTest {

    @Mock
    private SomaEntregaEppRepository somaEntregaEppRepository;

    @Mock
    private SomaEntregaEppItemRepository somaEntregaEppItemRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private ValeSalidaRepository valeSalidaRepository;

    @Mock
    private InterfazAlmacenService almacenService;

    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @Mock
    private SomaEppKardexPdfService somaEppKardexPdfService;

    @InjectMocks
    private SomaEppServiceImpl servicio;

    @BeforeEach
    void stubSave() {
        lenient().when(somaEntregaEppRepository.save(any(SomaEntregaEpp.class))).thenAnswer(invocation -> {
            SomaEntregaEpp entrega = invocation.getArgument(0);
            if (entrega.getId() == null) {
                entrega.setId(7L);
            }
            long itemId = 1L;
            for (var item : entrega.getItems()) {
                if (item.getId() == null) {
                    item.setId(itemId++);
                }
            }
            return entrega;
        });
    }

    @Test
    void rechazaMaterialQueNoEsEpp() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(personaRepository.findById(20L)).thenReturn(Optional.of(persona(20L)));
        Material material = material(30L, false, true, 90);
        when(materialRepository.findAllById(List.of(30L))).thenReturn(List.of(material));

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request(true, 40L)));

        assertEquals("El material no está marcado como EPP", error.getMessage());
        verify(almacenService, never()).crearVale(anyLong(), any());
        verify(somaEntregaEppRepository, never()).save(any());
    }

    @Test
    void calculaFechaVencimientoConVidaUtil() {
        stubCrearConEpp();
        when(almacenService.crearVale(eq(10L), any(ValeSalidaRequest.class)))
                .thenReturn(ValeSalidaDTO.builder().id(99L).build());
        ValeSalida vale = new ValeSalida();
        vale.setId(99L);
        when(valeSalidaRepository.getReferenceById(99L)).thenReturn(vale);

        SomaEntregaEppDTO dto = servicio.crear(10L, request(true, 40L));

        assertEquals(LocalDate.of(2026, 4, 10), dto.getItems().get(0).getFechaVencimientoEstimada());
    }

    @Test
    void conDescuentoLlamaCrearValeYGuardaId() {
        stubCrearConEpp();
        when(almacenService.crearVale(eq(10L), any(ValeSalidaRequest.class)))
                .thenReturn(ValeSalidaDTO.builder().id(99L).build());
        ValeSalida vale = new ValeSalida();
        vale.setId(99L);
        when(valeSalidaRepository.getReferenceById(99L)).thenReturn(vale);

        SomaEntregaEppDTO dto = servicio.crear(10L, request(true, 40L));

        ArgumentCaptor<ValeSalidaRequest> captor = ArgumentCaptor.forClass(ValeSalidaRequest.class);
        verify(almacenService).crearVale(eq(10L), captor.capture());
        ValeSalidaRequest valeRequest = captor.getValue();
        assertEquals(40L, valeRequest.getPartidaId());
        assertEquals(LocalDate.of(2026, 1, 10), valeRequest.getFecha());
        assertEquals(1, valeRequest.getItems().size());
        assertEquals(30L, valeRequest.getItems().get(0).getMaterialId());
        assertEquals(new BigDecimal("2"), valeRequest.getItems().get(0).getCantidad());
        assertEquals(99L, dto.getValeSalidaId());
    }

    @Test
    void sinDescuentoNoLlamaCrearVale() {
        stubCrearConEpp();
        SomaEntregaEppRequest request = request(false, null);

        SomaEntregaEppDTO dto = servicio.crear(10L, request);

        verify(almacenService, never()).crearVale(anyLong(), any());
        assertNull(dto.getValeSalidaId());
    }

    @Test
    void exigePartidaCuandoDescuenta() {
        stubCrearConEpp();
        SomaEntregaEppRequest request = request(true, null);

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));

        assertEquals("La partida es obligatoria para descontar de almacén", error.getMessage());
        verify(almacenService, never()).crearVale(anyLong(), any());
        verify(somaEntregaEppRepository, never()).save(any());
    }

    private void stubCrearConEpp() {
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(obra(10L)));
        when(personaRepository.findById(20L)).thenReturn(Optional.of(persona(20L)));
        Material material = material(30L, true, true, 90);
        when(materialRepository.findAllById(List.of(30L))).thenReturn(List.of(material));
    }

    private static SomaEntregaEppRequest request(boolean descontar, Long partidaId) {
        SomaEntregaEppItemRequest item = new SomaEntregaEppItemRequest();
        item.setMaterialId(30L);
        item.setCantidad(new BigDecimal("2"));
        item.setTalla("M");
        SomaEntregaEppRequest request = new SomaEntregaEppRequest();
        request.setPersonaId(20L);
        request.setFechaEntrega(LocalDate.of(2026, 1, 10));
        request.setMotivo(MotivoEntregaEpp.DOTACION_INICIAL);
        request.setDescontarAlmacen(descontar);
        request.setPartidaId(partidaId);
        request.setItems(List.of(item));
        return request;
    }

    private static Proyecto obra(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(id);
        proyecto.setCodigo("0000");
        proyecto.setNombre("BAMBAS NEGRAS");
        return proyecto;
    }

    private static Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombres("Juan");
        persona.setApellidos("Perez");
        persona.setNumeroDocumento("12345678");
        return persona;
    }

    private static Material material(Long id, boolean esEpp, boolean activo, Integer vidaUtil) {
        Material material = new Material();
        material.setId(id);
        material.setCodigo("EPP-01");
        material.setDescripcion("Casco");
        material.setEsEpp(esEpp);
        material.setActivo(activo);
        material.setVidaUtilDias(vidaUtil);
        return material;
    }
}
