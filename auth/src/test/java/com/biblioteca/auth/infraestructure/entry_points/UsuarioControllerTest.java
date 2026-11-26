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



    @Test
    void guardarUsuario_UsuarioValido_RetornaUsuarioYEstado200() {

        when(usuarioUseCase.guardarUsuario(usuario))
                .thenReturn(usuario);


        ResponseEntity<Usuario> respuesta =
                usuarioController.guardarUsuario(usuario);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).guardarUsuario(usuario);
    }



    @Test
    void buscarUsuarioPorId_IdValido_RetornaUsuarioYEstado200() {

        String id = "USR-1";

        when(usuarioUseCase.buscarUsuarioPorId(id))
                .thenReturn(usuario);


        ResponseEntity<Usuario> respuesta =
                usuarioController.buscarUsuarioPorId(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).buscarUsuarioPorId(id);
    }

    @Test
    void buscarUsuarioPorId_UsuarioNoExiste_PropagaExcepcion() {

        String id = "ID-INEXISTENTE";

        when(usuarioUseCase.buscarUsuarioPorId(id))
                .thenThrow(new UsuarioPorIdNoEncontradoException(
                        "Usuario no encontrado con id: " + id
                ));


        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioController.buscarUsuarioPorId(id)
        );


        assertEquals(
                "Usuario no encontrado con id: " + id,
                excepcion.getMessage()
        );
        verify(usuarioUseCase).buscarUsuarioPorId(id);
    }



    @Test
    void buscarUsuarioPorCorreo_CorreoValido_RetornaUsuarioYEstado200() {

        String correo = "ana@biblioteca.com";

        when(usuarioUseCase.buscarUsuarioPorCorreo(correo))
                .thenReturn(usuario);


        ResponseEntity<Usuario> respuesta =
                usuarioController.buscarUsuarioPorCorreo(correo);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).buscarUsuarioPorCorreo(correo);
    }



    @Test
    void existeUsuarioPorCorreo_UsuarioExiste_RetornaTrue() {

        String correo = "ana@biblioteca.com";

        when(usuarioUseCase.existeUsuarioPorCorreo(correo))
                .thenReturn(true);


        ResponseEntity<Boolean> respuesta =
                usuarioController.existeUsuarioPorCorreo(correo);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(true, respuesta.getBody());
        verify(usuarioUseCase).existeUsuarioPorCorreo(correo);
    }

    @Test
    void existeUsuarioPorCorreo_UsuarioNoExiste_RetornaFalse() {

        String correo = "desconocido@biblioteca.com";

        when(usuarioUseCase.existeUsuarioPorCorreo(correo))
                .thenReturn(false);


        ResponseEntity<Boolean> respuesta =
                usuarioController.existeUsuarioPorCorreo(correo);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(false, respuesta.getBody());
        verify(usuarioUseCase).existeUsuarioPorCorreo(correo);
    }



    @Test
    void actualizarUsuario_UsuarioValido_RetornaUsuarioYEstado200() {

        when(usuarioUseCase.actualizarUsuario(usuario))
                .thenReturn(usuario);


        ResponseEntity<Usuario> respuesta =
                usuarioController.actualizarUsuario(usuario);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).actualizarUsuario(usuario);
    }



    @Test
    void eliminarUsuario_IdValido_RetornaMensajeYEstado200() {

        String id = "USR-1";


        ResponseEntity<String> respuesta =
                usuarioController.eliminarUsuario(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Usuario eliminado con éxito", respuesta.getBody());
        verify(usuarioUseCase).eliminarUsuario(id);
    }



    @Test
    void loginUsuario_CredencialesCorrectas_RetornaMensajeDelUseCase() {

        when(usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        )).thenReturn("Logueo EXITOSO");


        ResponseEntity<String> respuesta =
                usuarioController.loginUsuario(usuario);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Logueo EXITOSO", respuesta.getBody());
        verify(usuarioUseCase).loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        );
    }

    @Test
    void loginUsuario_CredencialesIncorrectas_RetornaMensajeDelUseCase() {

        when(usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        )).thenReturn("Credenciales invalidas");


        ResponseEntity<String> respuesta =
                usuarioController.loginUsuario(usuario);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Credenciales invalidas", respuesta.getBody());
        verify(usuarioUseCase).loginUsuario(
                usuario.getCorreo(),
                usuario.getPassword()
        );
    }



    @Test
    void cambiarPassword_RequestValido_RetornaMensajeYEstado200() {

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


        ResponseEntity<String> respuesta =
                usuarioController.cambiarPassword(request);


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



    @Test
    void recuperarPassword_RequestValido_RetornaMensajeYEstado200() {

        UsuarioController.RecuperarPasswordRequest request =
                new UsuarioController.RecuperarPasswordRequest(
                        "ana@biblioteca.com"
                );

        String mensaje =
                "Si el correo está registrado, enviamos instrucciones de recuperación";

        when(usuarioUseCase.solicitarRecuperacionPassword(request.correo()))
                .thenReturn(mensaje);


        ResponseEntity<String> respuesta =
                usuarioController.recuperarPassword(request);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(mensaje, respuesta.getBody());
        verify(usuarioUseCase)
                .solicitarRecuperacionPassword(request.correo());
    }



    @Test
    void restablecerPassword_TokenValido_RetornaMensajeYEstado200() {

        UsuarioController.RestablecerPasswordRequest request =
                new UsuarioController.RestablecerPasswordRequest(
                        "token-de-prueba",
                        "NuevaClave789"
                );

        when(usuarioUseCase.restablecerPassword(
                request.token(),
                request.passwordNueva()
        )).thenReturn("Contraseña actualizada correctamente");


        ResponseEntity<String> respuesta =
                usuarioController.restablecerPassword(request);


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



    @Test
    void cambiarRol_RolValido_RetornaUsuarioYEstado200() {

        String id = "USR-1";
        UsuarioController.CambiarRolRequest request =
                new UsuarioController.CambiarRolRequest("ADMINISTRADOR");

        when(usuarioUseCase.cambiarRol(id, request.rol()))
                .thenReturn(usuario);


        ResponseEntity<Usuario> respuesta =
                usuarioController.cambiarRol(id, request);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(usuario, respuesta.getBody());
        verify(usuarioUseCase).cambiarRol(id, request.rol());
    }



    @Test
    void obtenerEstadisticas_RetornaEstadisticasYEstado200() {

        EstadisticasUsuario estadisticas =
                mock(EstadisticasUsuario.class);

        when(usuarioUseCase.obtenerEstadisticas())
                .thenReturn(estadisticas);


        ResponseEntity<EstadisticasUsuario> respuesta =
                usuarioController.obtenerEstadisticas();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(estadisticas, respuesta.getBody());
        verify(usuarioUseCase).obtenerEstadisticas();
    }
}