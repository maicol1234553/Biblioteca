package com.biblioteca.auth.infraestructure.entry_points;

import com.biblioteca.auth.domain.exception.UsuarioPorIdNoEncontradoException;
import com.biblioteca.auth.domain.model.EstadisticasUsuario;
import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.domain.usecase.UsuarioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioUseCase usuarioUseCase;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario("USR-1");
        usuario.setNombre("Ana Pérez");
        usuario.setCorreo("ana@biblioteca.com");
        usuario.setPassword("Password123");
        usuario.setRol("USUARIO");
        usuario.setEdad(25);
        usuario.setNTelefono("3001234567");
    }

    // REGISTRAR USUARIO

    @Test
    void guardarUsuario_UsuarioValido_RetornaUsuarioYEstado200() {
        // Arrange
        when(usuarioUseCase.guardarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        ResponseEntity<Usuario> respuesta =
                usuarioController.guardarUsuario(usuario);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).guardarUsuario(usuario);
    }

    // BUSCAR USUARIO POR ID

    @Test
    void buscarUsuarioPorId_IdValido_RetornaUsuarioYEstado200() {
        // Arrange
        String id = "USR-1";

        when(usuarioUseCase.buscarUsuarioPorId(id))
                .thenReturn(usuario);

        // Act
        ResponseEntity<Usuario> respuesta =
                usuarioController.buscarUsuarioPorId(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).buscarUsuarioPorId(id);
    }

    @Test
    void buscarUsuarioPorId_UsuarioNoExiste_PropagaExcepcion() {
        // Arrange
        String id = "ID-INEXISTENTE";

        when(usuarioUseCase.buscarUsuarioPorId(id))
                .thenThrow(new UsuarioPorIdNoEncontradoException(
                        "Usuario no encontrado con id: " + id
                ));

        // Act
        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioController.buscarUsuarioPorId(id)
        );

        // Assert
        assertEquals(
                "Usuario no encontrado con id: " + id,
                excepcion.getMessage()
        );
        verify(usuarioUseCase).buscarUsuarioPorId(id);
    }

    // BUSCAR USUARIO POR CORREO

    @Test
    void buscarUsuarioPorCorreo_CorreoValido_RetornaUsuarioYEstado200() {
        // Arrange
        String correo = "ana@biblioteca.com";

        when(usuarioUseCase.buscarUsuarioPorCorreo(correo))
                .thenReturn(usuario);

        // Act
        ResponseEntity<Usuario> respuesta =
                usuarioController.buscarUsuarioPorCorreo(correo);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).buscarUsuarioPorCorreo(correo);
    }

    // COMPROBAR SI EXISTE EL USUARIO

    @Test
    void existeUsuarioPorCorreo_UsuarioExiste_RetornaTrue() {
        // Arrange
        String correo = "ana@biblioteca.com";

        when(usuarioUseCase.existeUsuarioPorCorreo(correo))
                .thenReturn(true);

        // Act
        ResponseEntity<Boolean> respuesta =
                usuarioController.existeUsuarioPorCorreo(correo);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(true, respuesta.getBody());
        verify(usuarioUseCase).existeUsuarioPorCorreo(correo);
    }

    @Test
    void existeUsuarioPorCorreo_UsuarioNoExiste_RetornaFalse() {
        // Arrange
        String correo = "desconocido@biblioteca.com";

        when(usuarioUseCase.existeUsuarioPorCorreo(correo))
                .thenReturn(false);

        // Act
        ResponseEntity<Boolean> respuesta =
                usuarioController.existeUsuarioPorCorreo(correo);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(false, respuesta.getBody());
        verify(usuarioUseCase).existeUsuarioPorCorreo(correo);
    }

    // ACTUALIZAR USUARIO

    @Test
    void actualizarUsuario_UsuarioValido_RetornaUsuarioYEstado200() {
        // Arrange
        when(usuarioUseCase.actualizarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        ResponseEntity<Usuario> respuesta =
                usuarioController.actualizarUsuario(usuario);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).actualizarUsuario(usuario);
    }

    // ELIMINAR USUARIO

    @Test
    void eliminarUsuario_IdValido_RetornaMensajeYEstado200() {
        // Arrange
        String id = "USR-1";

        // Act
        ResponseEntity<String> respuesta =
                usuarioController.eliminarUsuario(id);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Usuario eliminado con éxito", respuesta.getBody());
        verify(usuarioUseCase).eliminarUsuario(id);
    }

    // INICIAR SESIÓN

    @Test
    void loginUsuario_CredencialesCorrectas_RetornaMensajeDelUseCase() {
        // Arrange
        when(usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        )).thenReturn("Logueo EXITOSO");

        // Act
        ResponseEntity<String> respuesta =
                usuarioController.loginUsuario(usuario);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Logueo EXITOSO", respuesta.getBody());
        verify(usuarioUseCase).loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        );
    }

    @Test
    void loginUsuario_CredencialesIncorrectas_RetornaMensajeDelUseCase() {
        // Arrange
        when(usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        )).thenReturn("Credenciales invalidas");

        // Act
        ResponseEntity<String> respuesta =
                usuarioController.loginUsuario(usuario);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Credenciales invalidas", respuesta.getBody());
        verify(usuarioUseCase).loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        );
    }

    // CAMBIAR CONTRASEÑA

    @Test
    void cambiarPassword_RequestValido_RetornaMensajeYEstado200() {
        // Arrange
        UsuarioController.CambiarPasswordRequest request =
                new UsuarioController.CambiarPasswordRequest(
                        "ana@biblioteca.com",
                        "Password123",
                        "NuevaClave456"
                );

        when(usuarioUseCase.cambiarPassword(
                request.correo(),
                request.passwordActual(),
                request.passwordNueva()
        )).thenReturn("Contraseña actualizada correctamente");

        // Act
        ResponseEntity<String> respuesta =
                usuarioController.cambiarPassword(request);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Contraseña actualizada correctamente",
                respuesta.getBody()
        );
        verify(usuarioUseCase).cambiarPassword(
                request.correo(),
                request.passwordActual(),
                request.passwordNueva()
        );
    }

    // SOLICITAR RECUPERACIÓN DE CONTRASEÑA

    @Test
    void recuperarPassword_RequestValido_RetornaMensajeYEstado200() {
        // Arrange
        UsuarioController.RecuperarPasswordRequest request =
                new UsuarioController.RecuperarPasswordRequest(
                        "ana@biblioteca.com"
                );

        String mensaje =
                "Si el correo está registrado, enviamos instrucciones de recuperación";

        when(usuarioUseCase.solicitarRecuperacionPassword(request.correo()))
                .thenReturn(mensaje);

        // Act
        ResponseEntity<String> respuesta =
                usuarioController.recuperarPassword(request);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(mensaje, respuesta.getBody());
        verify(usuarioUseCase)
                .solicitarRecuperacionPassword(request.correo());
    }

    // RESTABLECER CONTRASEÑA

    @Test
    void restablecerPassword_TokenValido_RetornaMensajeYEstado200() {
        // Arrange
        UsuarioController.RestablecerPasswordRequest request =
                new UsuarioController.RestablecerPasswordRequest(
                        "token-de-prueba",
                        "NuevaClave789"
                );

        when(usuarioUseCase.restablecerPassword(
                request.token(),
                request.passwordNueva()
        )).thenReturn("Contraseña actualizada correctamente");

        // Act
        ResponseEntity<String> respuesta =
                usuarioController.restablecerPassword(request);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Contraseña actualizada correctamente",
                respuesta.getBody()
        );
        verify(usuarioUseCase).restablecerPassword(
                request.token(),
                request.passwordNueva()
        );
    }

    // CAMBIAR ROL

    @Test
    void cambiarRol_RolValido_RetornaUsuarioYEstado200() {
        // Arrange
        String id = "USR-1";
        UsuarioController.CambiarRolRequest request =
                new UsuarioController.CambiarRolRequest("ADMINISTRADOR");

        when(usuarioUseCase.cambiarRol(id, request.rol()))
                .thenReturn(usuario);

        // Act
        ResponseEntity<Usuario> respuesta =
                usuarioController.cambiarRol(id, request);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).cambiarRol(id, request.rol());
    }

    // CONSULTAR ESTADÍSTICAS

    @Test
    void obtenerEstadisticas_RetornaEstadisticasYEstado200() {
        // Arrange
        EstadisticasUsuario estadisticas =
                mock(EstadisticasUsuario.class);

        when(usuarioUseCase.obtenerEstadisticas())
                .thenReturn(estadisticas);

        // Act
        ResponseEntity<EstadisticasUsuario> respuesta =
                usuarioController.obtenerEstadisticas();

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(estadisticas, respuesta.getBody());
        verify(usuarioUseCase).obtenerEstadisticas();
    }
}