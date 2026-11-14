package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.soma.domain.CategoriaResiduoSoma;
import com.convecsa.erp.soma.domain.TipoAgenteMonitoreoSoma;
import com.convecsa.erp.soma.dto.SomaMonitoreoDTO;
import com.convecsa.erp.soma.dto.SomaMonitoreoRequest;
import com.convecsa.erp.soma.dto.SomaResiduoDTO;
import com.convecsa.erp.soma.dto.SomaResiduoRequest;
import com.convecsa.erp.soma.entity.SomaMonitoreo;
import com.convecsa.erp.soma.entity.SomaResiduoRegistro;
import com.convecsa.erp.soma.repository.SomaMonitoreoRepository;
import com.convecsa.erp.soma.repository.SomaResiduoRegistroRepository;
import com.convecsa.erp.soma.service.impl.SomaAmbientalServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaAmbientalServiceImplTest {

    private static final Long PROYECTO_ID = 10L;

    @Mock
    private SomaResiduoRegistroRepository somaResiduoRegistroRepository;

    @Mock
    private SomaMonitoreoRepository somaMonitoreoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @InjectMocks
    private SomaAmbientalServiceImpl servicio;

    private Proyecto proyecto;

    @BeforeEach
    void stubProyecto() {
        proyecto = new Proyecto();
        proyecto.setId(PROYECTO_ID);
        lenient().when(proyectoRepository.findById(PROYECTO_ID)).thenReturn(Optional.of(proyecto));
        lenient()
                .when(somaResiduoRegistroRepository.save(any(SomaResiduoRegistro.class)))
                .thenAnswer(invocation -> {
                    SomaResiduoRegistro residuo = invocation.getArgument(0);
                    if (residuo.getId() == null) {
                        residuo.setId(1L);
                    }
                    return residuo;
                });
        lenient()
                .when(somaMonitoreoRepository.save(any(SomaMonitoreo.class)))
                .thenAnswer(invocation -> {
                    SomaMonitoreo monitoreo = invocation.getArgument(0);
                    if (monitoreo.getId() == null) {
                        monitoreo.setId(1L);
                    }
                    return monitoreo;
                });
    }

    @Test
    void peligrosoSinEorsFallaYConDatosPersiste() {
        SomaResiduoRequest incompleto = residuoBase(CategoriaResiduoSoma.PELIGROSO_ROJO);
        incompleto.setCantidadKg(new BigDecimal("12.50"));

        IllegalArgumentException sinRazon =
                assertThrows(IllegalArgumentException.class, () -> servicio.crearResiduo(PROYECTO_ID, incompleto));
        assertEquals("La razón social del EO-RS es obligatoria", sinRazon.getMessage());

        incompleto.setEorsRazonSocial("EO-RS SAC");
        IllegalArgumentException sinRuc =
                assertThrows(IllegalArgumentException.class, () -> servicio.crearResiduo(PROYECTO_ID, incompleto));
        assertEquals("El RUC del EO-RS es obligatorio", sinRuc.getMessage());

        incompleto.setEorsRuc("20123456789");
        IllegalArgumentException sinManifiesto =
                assertThrows(IllegalArgumentException.class, () -> servicio.crearResiduo(PROYECTO_ID, incompleto));
        assertEquals("El número de manifiesto es obligatorio", sinManifiesto.getMessage());

        incompleto.setNumeroManifiesto("MAN-001");
        SomaResiduoDTO creado = servicio.crearResiduo(PROYECTO_ID, incompleto);

        assertEquals(CategoriaResiduoSoma.PELIGROSO_ROJO, creado.getCategoriaResiduo());
        assertEquals("20123456789", creado.getEorsRuc());
        assertEquals("MAN-001", creado.getNumeroManifiesto());
        assertEquals(new BigDecimal("12.50"), creado.getCantidadKg());
    }

    @Test
    void residuoNoPeligrosoSoloVolumenSinEors() {
        SomaResiduoRequest request = residuoBase(CategoriaResiduoSoma.NO_APROVECHABLE_NEGRO);
        request.setVolumenM3(new BigDecimal("3.20"));

        SomaResiduoDTO creado = servicio.crearResiduo(PROYECTO_ID, request);

        assertEquals(CategoriaResiduoSoma.NO_APROVECHABLE_NEGRO, creado.getCategoriaResiduo());
        assertEquals(new BigDecimal("3.20"), creado.getVolumenM3());
        assertEquals(null, creado.getEorsRuc());
    }

    @Test
    void ruidoUsaTechoDeLimite() {
        SomaMonitoreoDTO sobreLimite = servicio.crearMonitoreo(
                PROYECTO_ID, monitoreo(TipoAgenteMonitoreoSoma.FISICO_RUIDO, "90", "85"));
        SomaMonitoreoDTO enLimite = servicio.crearMonitoreo(
                PROYECTO_ID, monitoreo(TipoAgenteMonitoreoSoma.FISICO_RUIDO, "85", "85"));

        assertFalse(sobreLimite.isCumpleLmp());
        assertTrue(enLimite.isCumpleLmp());
    }

    @Test
    void iluminacionUsaMinimoDeLimite() {
        SomaMonitoreoDTO bajoMinimo = servicio.crearMonitoreo(
                PROYECTO_ID, monitoreo(TipoAgenteMonitoreoSoma.FISICO_ILUMINACION, "200", "300"));
        SomaMonitoreoDTO enMinimo = servicio.crearMonitoreo(
                PROYECTO_ID, monitoreo(TipoAgenteMonitoreoSoma.FISICO_ILUMINACION, "300", "300"));

        assertFalse(bajoMinimo.isCumpleLmp());
        assertTrue(enMinimo.isCumpleLmp());
    }

    private static SomaResiduoRequest residuoBase(CategoriaResiduoSoma categoria) {
        SomaResiduoRequest request = new SomaResiduoRequest();
        request.setFecha(LocalDate.of(2026, 11, 13));
        request.setCategoriaResiduo(categoria);
        request.setDescripcion("Residuo de obra");
        return request;
    }

    private static SomaMonitoreoRequest monitoreo(TipoAgenteMonitoreoSoma tipo, String valor, String limite) {
        SomaMonitoreoRequest request = new SomaMonitoreoRequest();
        request.setFecha(LocalDate.of(2026, 11, 13));
        request.setTipoAgente(tipo);
        request.setParametroMedido(tipo == TipoAgenteMonitoreoSoma.FISICO_ILUMINACION ? "Iluminación" : "Ruido");
        request.setPuntoMedicion("Frente A");
        request.setValorMedido(new BigDecimal(valor));
        request.setLimitePermisible(new BigDecimal(limite));
        request.setUnidad(tipo == TipoAgenteMonitoreoSoma.FISICO_ILUMINACION ? "lux" : "dB");
        return request;
    }
}
