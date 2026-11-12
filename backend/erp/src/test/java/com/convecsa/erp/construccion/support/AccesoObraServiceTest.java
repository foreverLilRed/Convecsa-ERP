package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.ProyectoSinAccesoException;
import com.convecsa.erp.construccion.dto.AccesoObraRequest;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.UsuarioProyectoRepository;
import com.convecsa.erp.usuario.domain.RolUsuario;
import com.convecsa.erp.usuario.entity.Usuario;
import com.convecsa.erp.usuario.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AccesoObraServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioProyectoRepository usuarioProyectoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private AccesoObraService servicio;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void gerenciaVeTodasLasObras() {
        autenticar(usuario(1L, "admin", RolUsuario.ADMIN));
        List<ProyectoDTO> todas = List.of(obra(10L), obra(20L));

        assertEquals(todas, servicio.filtrarListado(todas));
        verify(usuarioProyectoRepository, never()).findProyectoIdsByUsuarioId(1L);
    }

    @Test
    void residenteSoloVeObrasAsignadas() {
        autenticar(usuario(2L, "residente", RolUsuario.RESIDENTE));
        when(usuarioProyectoRepository.findProyectoIdsByUsuarioId(2L)).thenReturn(List.of(10L));

        List<ProyectoDTO> filtradas = servicio.filtrarListado(List.of(obra(10L), obra(20L)));

        assertEquals(1, filtradas.size());
        assertEquals(10L, filtradas.get(0).getId());
    }

    @Test
    void almacenPuedeConsultarKardexDeObraAsignada() {
        autenticar(usuario(3L, "almacen", RolUsuario.ALMACEN));
        when(usuarioProyectoRepository.existsByUsuarioIdAndProyectoId(3L, 10L)).thenReturn(true);

        servicio.exigirRutaObra("GET", "/api/proyectos/10/almacen/kardex", 10L);
    }

    @Test
    void almacenNoPuedeSalirDelKardex() {
        autenticar(usuario(3L, "almacen", RolUsuario.ALMACEN));
        when(usuarioProyectoRepository.existsByUsuarioIdAndProyectoId(3L, 10L)).thenReturn(true);

        ProyectoSinAccesoException error = assertThrows(
                ProyectoSinAccesoException.class,
                () -> servicio.exigirRutaObra("GET", "/api/proyectos/10/tareo", 10L));
        assertEquals(AccesoObraService.MSG_ALMACEN, error.getMessage());
    }

    @Test
    void guardarAccesosRequiereGerencia() {
        autenticar(usuario(4L, "supervisor", RolUsuario.SUPERVISOR));
        AccesoObraRequest fila = new AccesoObraRequest();
        fila.setUsuarioId(8L);

        ProyectoSinAccesoException error = assertThrows(
                ProyectoSinAccesoException.class,
                () -> servicio.guardarAccesos(10L, List.of(fila)));
        assertEquals(AccesoObraService.MSG_GERENCIA, error.getMessage());
    }

    private void autenticar(Usuario usuario) {
        when(usuarioRepository.findByUsernameOrCorreoIgnoreCase(usuario.getUsername()))
                .thenReturn(Optional.of(usuario));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario.getUsername(), "n/a", List.of()));
    }

    private static Usuario usuario(Long id, String username, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    private static ProyectoDTO obra(Long id) {
        return ProyectoDTO.builder()
                .id(id)
                .codigo("OB" + id)
                .nombre("Obra " + id)
                .activo(true)
                .alertaUit(false)
                .uitAnio(2026)
                .build();
    }
}
