package com.biblioteca.auth.infraestructure.driver_adapters;

import com.biblioteca.auth.domain.model.EstadisticasUsuario;
import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.infraestructure.mapper.UsuarioMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDataGatewayImplTest {

    @Mock
    private UsuarioDataJpaRepository usuarioDataJpaRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioDataGatewayImpl usuarioDataGateway;

    @Test
    void guardarUsuario_debeMapearGuardarYRetornarUsuario() {

        Usuario usuario = mock(Usuario.class);
        UsuarioData usuarioData = mock(UsuarioData.class);
        UsuarioData usuarioDataGuardado = mock(UsuarioData.class);
        Usuario usuarioConvertido = mock(Usuario.class);

        when(usuarioMapper.toUsuarioData(usuario)).thenReturn(usuarioData);
        when(usuarioDataJpaRepository.save(usuarioData)).thenReturn(usuarioDataGuardado);
        when(usuarioMapper.toUsuario(usuarioDataGuardado)).thenReturn(usuarioConvertido);


        Usuario resultado = usuarioDataGateway.guardarUsuario(usuario);


        assertSame(usuarioConvertido, resultado);
        verify(usuarioMapper).toUsuarioData(usuario);
        verify(usuarioDataJpaRepository).save(usuarioData);
        verify(usuarioMapper).toUsuario(usuarioDataGuardado);
    }

    @Test
    void buscarUsuarioPorId_cuandoExiste_debeRetornarUsuarioMapeado() {

        String usuarioId = "usuario-1";
        UsuarioData usuarioData = mock(UsuarioData.class);
        Usuario usuario = mock(Usuario.class);

        when(usuarioDataJpaRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuarioData));
        when(usuarioMapper.toUsuario(usuarioData)).thenReturn(usuario);


        Usuario resultado = usuarioDataGateway.buscarUsuarioPorId(usuarioId);


        assertSame(usuario, resultado);
        verify(usuarioDataJpaRepository).findById(usuarioId);
        verify(usuarioMapper).toUsuario(usuarioData);
    }

    @Test
    void buscarUsuarioPorId_cuandoNoExiste_debeRetornarNull() {

        String usuarioId = "inexistente";

        when(usuarioDataJpaRepository.findById(usuarioId))
                .thenReturn(Optional.empty());


        Usuario resultado = usuarioDataGateway.buscarUsuarioPorId(usuarioId);


        assertNull(resultado);
        verify(usuarioDataJpaRepository).findById(usuarioId);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void buscarUsuarioPorCorreo_cuandoExiste_debeRetornarUsuarioMapeado() {

        String correo = "usuario@biblioteca.com";
        UsuarioData usuarioData = mock(UsuarioData.class);
        Usuario usuario = mock(Usuario.class);

        when(usuarioDataJpaRepository.findByCorreo(correo))
                .thenReturn(Optional.of(usuarioData));
        when(usuarioMapper.toUsuario(usuarioData)).thenReturn(usuario);


        Usuario resultado = usuarioDataGateway.buscarUsuarioPorCorreo(correo);


        assertSame(usuario, resultado);
        verify(usuarioDataJpaRepository).findByCorreo(correo);
        verify(usuarioMapper).toUsuario(usuarioData);
    }

    @Test
    void buscarUsuarioPorCorreo_cuandoNoExiste_debeRetornarNull() {

        String correo = "inexistente@biblioteca.com";

        when(usuarioDataJpaRepository.findByCorreo(correo))
                .thenReturn(Optional.empty());


        Usuario resultado = usuarioDataGateway.buscarUsuarioPorCorreo(correo);


        assertNull(resultado);
        verify(usuarioDataJpaRepository).findByCorreo(correo);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void actualizarUsuario_debeGuardarYRetornarUsuarioActualizado() {

        Usuario usuario = mock(Usuario.class);
        UsuarioData usuarioData = mock(UsuarioData.class);
        UsuarioData usuarioDataActualizado = mock(UsuarioData.class);
        Usuario usuarioActualizado = mock(Usuario.class);

        when(usuarioMapper.toUsuarioData(usuario)).thenReturn(usuarioData);
        when(usuarioDataJpaRepository.save(usuarioData)).thenReturn(usuarioDataActualizado);
        when(usuarioMapper.toUsuario(usuarioDataActualizado)).thenReturn(usuarioActualizado);


        Usuario resultado = usuarioDataGateway.actualizarUsuario(usuario);


        assertSame(usuarioActualizado, resultado);
        verify(usuarioMapper).toUsuarioData(usuario);
        verify(usuarioDataJpaRepository).save(usuarioData);
        verify(usuarioMapper).toUsuario(usuarioDataActualizado);
    }

    @Test
    void eliminarUsuario_debeSolicitarLaEliminacionPorId() {

        String usuarioId = "usuario-1";


        usuarioDataGateway.eliminarUsuario(usuarioId);


        verify(usuarioDataJpaRepository).deleteById(usuarioId);
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void obtenerEstadisticas_debeCalcularConteoRolesYRedondearPromedio() {

        List<Object[]> conteoPorRol = List.of(
                new Object[]{"ADMINISTRADOR", 2L},
                new Object[]{"USUARIO", 3L}
        );

        when(usuarioDataJpaRepository.contarUsuariosPorRol()).thenReturn(conteoPorRol);
        when(usuarioDataJpaRepository.promedioEdad()).thenReturn(31.236);
        when(usuarioDataJpaRepository.count()).thenReturn(5L);

        Map<String, Long> usuariosPorRol = new LinkedHashMap<>();
        usuariosPorRol.put("ADMINISTRADOR", 2L);
        usuariosPorRol.put("USUARIO", 3L);

        EstadisticasUsuario esperadas =
                new EstadisticasUsuario(5, usuariosPorRol, 31.24);


        EstadisticasUsuario resultado = usuarioDataGateway.obtenerEstadisticas();


        assertEquals(esperadas, resultado);
        verify(usuarioDataJpaRepository).contarUsuariosPorRol();
        verify(usuarioDataJpaRepository).promedioEdad();
        verify(usuarioDataJpaRepository).count();
        verifyNoInteractions(usuarioMapper);
    }

    @Test
    void obtenerEstadisticas_cuandoPromedioEsNull_debeUsarCero() {

        when(usuarioDataJpaRepository.contarUsuariosPorRol()).thenReturn(List.of());
        when(usuarioDataJpaRepository.promedioEdad()).thenReturn(null);
        when(usuarioDataJpaRepository.count()).thenReturn(0L);

        EstadisticasUsuario esperadas =
                new EstadisticasUsuario(0, Map.of(), 0.0);


        EstadisticasUsuario resultado = usuarioDataGateway.obtenerEstadisticas();


        assertEquals(esperadas, resultado);
        verify(usuarioDataJpaRepository).contarUsuariosPorRol();
        verify(usuarioDataJpaRepository).promedioEdad();
        verify(usuarioDataJpaRepository).count();
        verifyNoInteractions(usuarioMapper);
    }
}