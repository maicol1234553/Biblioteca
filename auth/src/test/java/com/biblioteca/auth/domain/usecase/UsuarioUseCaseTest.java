package com.biblioteca.auth.domain.usecase;

import com.biblioteca.auth.domain.exception.UsuarioPorCorreoNoEncontradoException;
import com.biblioteca.auth.domain.exception.UsuarioPorIdNoEncontradoException;
import com.biblioteca.auth.domain.model.EstadisticasUsuario;
import com.biblioteca.auth.domain.model.PasswordResetToken;
import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.domain.model.gateway.EncrypterGateway;
import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import com.biblioteca.auth.domain.model.gateway.PasswordResetTokenGateway;
import com.biblioteca.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCaseTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private EncrypterGateway encrypterGateway;

    @Mock
    private PasswordResetTokenGateway passwordResetTokenGateway;

    @Mock
    private NotificationGateway notificationGateway;

    @InjectMocks
    private UsuarioUseCase usuarioUseCase;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = crearUsuarioValido();
    }



    @Test
    void guardarUsuario_UsuarioValidoSinId_GeneraIdEncriptaPasswordYGuarda() {

        usuario.setIdUsuario(null);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(null);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);


        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);


        assertSame(usuario, resultado);
        assertNotNull(resultado.getIdUsuario());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdUsuario()));
        assertEquals("password-encriptada", resultado.getPassword());
        assertEquals("USUARIO", resultado.getRol());

        verify(encrypterGateway).encrypt("Password123");
        verify(usuarioGateway).guardarUsuario(usuario);
    }

    @Test
    void guardarUsuario_PasswordYaEncriptada_NoLaEncriptaDeNuevo() {

        usuario.setPassword("$2b$12$password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(null);

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);


        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);


        assertSame(usuario, resultado);
        assertEquals("$2b$12$password-encriptada", resultado.getPassword());

        verifyNoInteractions(encrypterGateway);
        verify(usuarioGateway).guardarUsuario(usuario);
    }

    @Test
    void guardarUsuario_CorreoDeOtroUsuario_LanzaExcepcion() {

        Usuario existente = crearUsuarioValido();
        existente.setIdUsuario("OTRO-ID");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(existente);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals("El correo ya está registrado", excepcion.getMessage());
        verify(usuarioGateway, never()).guardarUsuario(any(Usuario.class));
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void guardarUsuario_MismoIdYCorreoExistente_PermiteGuardar() {

        usuario.setIdUsuario("USR-1");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);


        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);


        assertSame(usuario, resultado);
        assertEquals("password-encriptada", resultado.getPassword());
        verify(usuarioGateway).guardarUsuario(usuario);
    }

    @Test
    void guardarUsuario_UsuarioNulo_LanzaExcepcion() {

        Usuario usuarioNulo = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuarioNulo)
        );


        assertEquals("El usuario es obligatorio", excepcion.getMessage());
        verifyNoInteractions(
                usuarioGateway,
                encrypterGateway,
                passwordResetTokenGateway,
                notificationGateway
        );
    }

    @Test
    void guardarUsuario_NombreNulo_LanzaExcepcion() {

        usuario.setNombre(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals("El nombre es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_NombreVacio_LanzaExcepcion() {

        usuario.setNombre(" ");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals("El nombre es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_CorreoNulo_LanzaExcepcion() {

        usuario.setCorreo(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals("El correo no es válido", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_CorreoSinArroba_LanzaExcepcion() {

        usuario.setCorreo("correo-invalido.com");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals("El correo no es válido", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_PasswordNula_LanzaExcepcion() {

        usuario.setPassword(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_PasswordMenorA8Caracteres_LanzaExcepcion() {

        usuario.setPassword("1234567");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_EdadNula_LanzaExcepcion() {

        usuario.setEdad(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals("Usuario menor de edad", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_MenorDe18Anios_LanzaExcepcion() {

        usuario.setEdad(17);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals("Usuario menor de edad", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_EdadMayorA122_LanzaExcepcion() {

        usuario.setEdad(123);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals(
                "No puede tener más de 122 años",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_TelefonoNulo_LanzaExcepcion() {

        usuario.setNTelefono(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals(
                "El número telefónico es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_TelefonoVacio_LanzaExcepcion() {

        usuario.setNTelefono(" ");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals(
                "El número telefónico es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_TelefonoConFormatoIncorrecto_LanzaExcepcion() {

        usuario.setNTelefono("300ABC4567");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );


        assertEquals(
                "El número telefónico debe tener 10 dígitos",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_EdadMinimaYMaximaValidas_PermiteGuardar() {

        usuario.setEdad(18);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(null);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);


        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);


        assertSame(usuario, resultado);
        assertEquals(18, resultado.getEdad());
        verify(usuarioGateway).guardarUsuario(usuario);
    }



    @Test
    void loginUsuario_CredencialesCorrectas_RetornaExito() {

        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-encriptada"
        )).thenReturn(true);


        String resultado = usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                "Password123"
        );


        assertEquals("Logueo EXITOSO", resultado);
        verify(encrypterGateway).checkPass(
                "Password123",
                "password-encriptada"
        );
    }

    @Test
    void loginUsuario_UsuarioNoExiste_RetornaUsuarioNoEncontrado() {

        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);


        String resultado = usuarioUseCase.loginUsuario(correo, "Password123");


        assertEquals("Usuario no encontrado", resultado);
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void loginUsuario_PasswordIncorrecta_RetornaCredencialesInvalidas() {

        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "password-incorrecta",
                "password-encriptada"
        )).thenReturn(false);


        String resultado = usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                "password-incorrecta"
        );


        assertEquals("Credenciales invalidas", resultado);
    }



    @Test
    void existeUsuarioPorCorreo_UsuarioExiste_RetornaTrue() {

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);


        boolean resultado =
                usuarioUseCase.existeUsuarioPorCorreo(usuario.getCorreo());


        assertTrue(resultado);
        verify(usuarioGateway).buscarUsuarioPorCorreo(usuario.getCorreo());
    }

    @Test
    void existeUsuarioPorCorreo_UsuarioNoExiste_RetornaFalse() {

        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);


        boolean resultado = usuarioUseCase.existeUsuarioPorCorreo(correo);


        assertFalse(resultado);
    }



    @Test
    void cambiarPassword_DatosValidos_EncriptaYActualiza() {

        usuario.setPassword("password-anterior-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-anterior-encriptada"
        )).thenReturn(true);

        when(encrypterGateway.encrypt("PasswordNueva123"))
                .thenReturn("password-nueva-encriptada");


        String resultado = usuarioUseCase.cambiarPassword(
                usuario.getCorreo(),
                "Password123",
                "PasswordNueva123"
        );


        assertEquals("Contraseña actualizada correctamente", resultado);
        assertEquals("password-nueva-encriptada", usuario.getPassword());
        verify(usuarioGateway).actualizarUsuario(usuario);
    }

    @Test
    void cambiarPassword_UsuarioNoExiste_LanzaExcepcion() {

        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);


        UsuarioPorCorreoNoEncontradoException excepcion = assertThrows(
                UsuarioPorCorreoNoEncontradoException.class,
                () -> usuarioUseCase.cambiarPassword(
                        correo,
                        "Password123",
                        "PasswordNueva123"
                )
        );


        assertEquals(
                "Usuario no encontrado con correo: " + correo,
                excepcion.getMessage()
        );
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void cambiarPassword_PasswordActualNula_LanzaExcepcion() {

        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        null,
                        "PasswordNueva123"
                )
        );


        assertEquals(
                "La contraseña actual no es correcta",
                excepcion.getMessage()
        );
        verify(encrypterGateway, never())
                .checkPass(anyString(), anyString());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }

    @Test
    void cambiarPassword_PasswordActualIncorrecta_LanzaExcepcion() {

        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "password-incorrecta",
                "password-encriptada"
        )).thenReturn(false);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        "password-incorrecta",
                        "PasswordNueva123"
                )
        );


        assertEquals(
                "La contraseña actual no es correcta",
                excepcion.getMessage()
        );
        verify(encrypterGateway, never()).encrypt(anyString());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }

    @Test
    void cambiarPassword_PasswordNuevaNula_LanzaExcepcion() {

        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-encriptada"
        )).thenReturn(true);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        "Password123",
                        null
                )
        );


        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verify(encrypterGateway, never()).encrypt(anyString());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }

    @Test
    void cambiarPassword_PasswordNuevaMenorA8_LanzaExcepcion() {

        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-encriptada"
        )).thenReturn(true);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        "Password123",
                        "1234567"
                )
        );


        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verify(encrypterGateway, never()).encrypt(anyString());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }



    @Test
    void solicitarRecuperacionPassword_CorreoRegistrado_GuardaTokenYEnviaCorreo() {

        String correo = usuario.getCorreo();
        LocalDateTime antesDeLaSolicitud = LocalDateTime.now();

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(usuario);

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        ArgumentCaptor<String> mensajeCaptor =
                ArgumentCaptor.forClass(String.class);


        String resultado =
                usuarioUseCase.solicitarRecuperacionPassword(correo);


        assertEquals(
                "Si el correo está registrado, enviamos instrucciones de recuperación",
                resultado
        );

        verify(passwordResetTokenGateway).guardarToken(tokenCaptor.capture());

        PasswordResetToken tokenGuardado = tokenCaptor.getValue();

        assertEquals(usuario.getIdUsuario(), tokenGuardado.getIdUsuario());
        assertFalse(tokenGuardado.getUsado());
        assertFalse(
                tokenGuardado.getFechaExpiracion()
                        .isBefore(antesDeLaSolicitud.plusMinutes(30))
        );

        verify(notificationGateway).enviarCorreo(
                eq(usuario.getIdUsuario()),
                eq(correo),
                eq("Recuperación de contraseña"),
                mensajeCaptor.capture(),
                eq("RECUPERACION_PASSWORD")
        );

        assertTrue(mensajeCaptor.getValue().contains(
                "Tu código de recuperación es:"
        ));
        assertTrue(mensajeCaptor.getValue().contains("Expira en 30 minutos."));
    }

    @Test
    void solicitarRecuperacionPassword_CorreoNoRegistrado_RetornaMensajeGenericoSinEnviarCorreo() {

        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);


        String resultado =
                usuarioUseCase.solicitarRecuperacionPassword(correo);


        assertEquals(
                "Si el correo está registrado, enviamos instrucciones de recuperación",
                resultado
        );

        verifyNoInteractions(passwordResetTokenGateway, notificationGateway);
    }



    @Test
    void restablecerPassword_TokenValido_ActualizaPasswordYMarcaTokenUsado() {

        String tokenPlano = "token-de-prueba";
        String tokenHash = calcularHash(tokenPlano);

        PasswordResetToken registro = new PasswordResetToken(
                "TOKEN-1",
                usuario.getIdUsuario(),
                tokenHash,
                LocalDateTime.now().plusMinutes(10),
                false
        );

        when(passwordResetTokenGateway.buscarPorTokenHash(tokenHash))
                .thenReturn(registro);

        when(usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);

        when(encrypterGateway.encrypt("PasswordNueva123"))
                .thenReturn("password-nueva-encriptada");


        String resultado = usuarioUseCase.restablecerPassword(
                tokenPlano,
                "PasswordNueva123"
        );


        assertEquals("Contraseña actualizada correctamente", resultado);
        assertEquals("password-nueva-encriptada", usuario.getPassword());
        assertTrue(registro.getUsado());

        verify(passwordResetTokenGateway)
                .buscarPorTokenHash(tokenHash);
        verify(usuarioGateway).actualizarUsuario(usuario);
        verify(passwordResetTokenGateway).actualizarToken(registro);
    }

    @Test
    void restablecerPassword_TokenNulo_LanzaExcepcion() {

        String token = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        token,
                        "PasswordNueva123"
                )
        );


        assertEquals("El token es obligatorio", excepcion.getMessage());
        verifyNoInteractions(
                usuarioGateway,
                encrypterGateway,
                passwordResetTokenGateway
        );
    }

    @Test
    void restablecerPassword_TokenVacio_LanzaExcepcion() {

        String token = " ";


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        token,
                        "PasswordNueva123"
                )
        );


        assertEquals("El token es obligatorio", excepcion.getMessage());
        verifyNoInteractions(passwordResetTokenGateway);
    }

    @Test
    void restablecerPassword_PasswordNuevaCorta_LanzaExcepcionAntesDeBuscarToken() {

        String token = "token-de-prueba";


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(token, "1234567")
        );


        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verifyNoInteractions(passwordResetTokenGateway, usuarioGateway);
    }

    @Test
    void restablecerPassword_TokenInexistente_LanzaExcepcion() {

        String tokenPlano = "token-inexistente";
        String tokenHash = calcularHash(tokenPlano);

        when(passwordResetTokenGateway.buscarPorTokenHash(tokenHash))
                .thenReturn(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );


        assertEquals(
                "Token inválido o ya utilizado",
                excepcion.getMessage()
        );
        verify(usuarioGateway, never()).buscarUsuarioPorId(anyString());
    }

    @Test
    void restablecerPassword_TokenYaUsado_LanzaExcepcion() {

        String tokenPlano = "token-usado";
        String tokenHash = calcularHash(tokenPlano);

        PasswordResetToken registro = new PasswordResetToken(
                "TOKEN-1",
                usuario.getIdUsuario(),
                tokenHash,
                LocalDateTime.now().plusMinutes(10),
                true
        );

        when(passwordResetTokenGateway.buscarPorTokenHash(tokenHash))
                .thenReturn(registro);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );


        assertEquals(
                "Token inválido o ya utilizado",
                excepcion.getMessage()
        );
        verify(usuarioGateway, never()).buscarUsuarioPorId(anyString());
    }

    @Test
    void restablecerPassword_TokenExpirado_LanzaExcepcion() {

        String tokenPlano = "token-expirado";
        String tokenHash = calcularHash(tokenPlano);

        PasswordResetToken registro = new PasswordResetToken(
                "TOKEN-1",
                usuario.getIdUsuario(),
                tokenHash,
                LocalDateTime.now().minusMinutes(1),
                false
        );

        when(passwordResetTokenGateway.buscarPorTokenHash(tokenHash))
                .thenReturn(registro);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );


        assertEquals("El token expiró", excepcion.getMessage());
        verify(usuarioGateway, never()).buscarUsuarioPorId(anyString());
    }

    @Test
    void restablecerPassword_UsuarioDelTokenNoExiste_LanzaExcepcion() {

        String tokenPlano = "token-valido";
        String tokenHash = calcularHash(tokenPlano);

        PasswordResetToken registro = new PasswordResetToken(
                "TOKEN-1",
                "ID-INEXISTENTE",
                tokenHash,
                LocalDateTime.now().plusMinutes(10),
                false
        );

        when(passwordResetTokenGateway.buscarPorTokenHash(tokenHash))
                .thenReturn(registro);

        when(usuarioGateway.buscarUsuarioPorId("ID-INEXISTENTE"))
                .thenReturn(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );


        assertEquals("Token inválido", excepcion.getMessage());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
        verify(passwordResetTokenGateway, never())
                .actualizarToken(any(PasswordResetToken.class));
    }



    @Test
    void cambiarRol_RolValido_ActualizaUsuario() {

        when(usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);

        when(usuarioGateway.actualizarUsuario(usuario))
                .thenReturn(usuario);


        Usuario resultado = usuarioUseCase.cambiarRol(
                usuario.getIdUsuario(),
                "ADMINISTRADOR"
        );


        assertSame(usuario, resultado);
        assertEquals("ADMINISTRADOR", resultado.getRol());
        verify(usuarioGateway).actualizarUsuario(usuario);
    }

    @Test
    void cambiarRol_RolNulo_LanzaExcepcion() {

        String rol = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarRol(usuario.getIdUsuario(), rol)
        );


        assertEquals("El rol es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway);
    }

    @Test
    void cambiarRol_RolVacio_LanzaExcepcion() {

        String rol = " ";


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarRol(usuario.getIdUsuario(), rol)
        );


        assertEquals("El rol es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway);
    }

    @Test
    void cambiarRol_UsuarioNoExiste_LanzaExcepcion() {

        String usuarioId = "ID-INEXISTENTE";

        when(usuarioGateway.buscarUsuarioPorId(usuarioId))
                .thenReturn(null);


        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.cambiarRol(usuarioId, "ADMINISTRADOR")
        );


        assertEquals(
                "Usuario no encontrado con id: " + usuarioId,
                excepcion.getMessage()
        );
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }



    @Test
    void obtenerEstadisticas_ConsultaGateway_RetornaResultado() {

        EstadisticasUsuario estadisticasEsperadas =
                mock(EstadisticasUsuario.class);

        when(usuarioGateway.obtenerEstadisticas())
                .thenReturn(estadisticasEsperadas);


        EstadisticasUsuario resultado =
                usuarioUseCase.obtenerEstadisticas();


        assertSame(estadisticasEsperadas, resultado);
        verify(usuarioGateway).obtenerEstadisticas();
    }



    @Test
    void buscarUsuarioPorId_UsuarioExiste_RetornaUsuario() {

        when(usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);


        Usuario resultado =
                usuarioUseCase.buscarUsuarioPorId(usuario.getIdUsuario());


        assertSame(usuario, resultado);
        verify(usuarioGateway).buscarUsuarioPorId(usuario.getIdUsuario());
    }

    @Test
    void buscarUsuarioPorId_UsuarioNoExiste_LanzaExcepcion() {

        String usuarioId = "ID-INEXISTENTE";

        when(usuarioGateway.buscarUsuarioPorId(usuarioId))
                .thenReturn(null);


        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.buscarUsuarioPorId(usuarioId)
        );


        assertEquals(
                "Usuario no encontrado con id: " + usuarioId,
                excepcion.getMessage()
        );
    }



    @Test
    void buscarUsuarioPorCorreo_UsuarioExiste_RetornaUsuario() {

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);


        Usuario resultado =
                usuarioUseCase.buscarUsuarioPorCorreo(usuario.getCorreo());


        assertSame(usuario, resultado);
        verify(usuarioGateway).buscarUsuarioPorCorreo(usuario.getCorreo());
    }

    @Test
    void buscarUsuarioPorCorreo_UsuarioNoExiste_LanzaExcepcion() {

        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);


        UsuarioPorCorreoNoEncontradoException excepcion = assertThrows(
                UsuarioPorCorreoNoEncontradoException.class,
                () -> usuarioUseCase.buscarUsuarioPorCorreo(correo)
        );


        assertEquals(
                "Usuerio no encontrado con correo: " + correo,
                excepcion.getMessage()
        );
    }



    @Test
    void actualizarUsuario_DatosValidos_EncriptaPasswordYActualiza() {

        usuario.setIdUsuario("USR-1");

        when(usuarioGateway.buscarUsuarioPorId("USR-1"))
                .thenReturn(usuario);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.actualizarUsuario(usuario))
                .thenReturn(usuario);


        Usuario resultado = usuarioUseCase.actualizarUsuario(usuario);


        assertSame(usuario, resultado);
        assertEquals("password-encriptada", resultado.getPassword());
        verify(usuarioGateway).actualizarUsuario(usuario);
    }

    @Test
    void actualizarUsuario_RolVacio_ConservaRolExistente() {

        Usuario usuarioActualizado = crearUsuarioValido();
        usuarioActualizado.setIdUsuario("USR-1");
        usuarioActualizado.setRol(" ");

        Usuario usuarioExistente = crearUsuarioValido();
        usuarioExistente.setIdUsuario("USR-1");
        usuarioExistente.setRol("ADMINISTRADOR");

        when(usuarioGateway.buscarUsuarioPorId("USR-1"))
                .thenReturn(usuarioExistente);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuarioActualizado.getCorreo()))
                .thenReturn(usuarioExistente);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.actualizarUsuario(usuarioActualizado))
                .thenReturn(usuarioActualizado);


        Usuario resultado =
                usuarioUseCase.actualizarUsuario(usuarioActualizado);


        assertSame(usuarioActualizado, resultado);
        assertEquals("ADMINISTRADOR", resultado.getRol());
        verify(usuarioGateway).actualizarUsuario(usuarioActualizado);
    }

    @Test
    void actualizarUsuario_UsuarioNoExiste_LanzaExcepcion() {

        usuario.setIdUsuario("ID-INEXISTENTE");

        when(usuarioGateway.buscarUsuarioPorId("ID-INEXISTENTE"))
                .thenReturn(null);


        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.actualizarUsuario(usuario)
        );


        assertEquals(
                "Usuario no encontrado con id: ID-INEXISTENTE",
                excepcion.getMessage()
        );
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_CorreoDeOtroUsuario_LanzaExcepcion() {

        usuario.setIdUsuario("USR-1");

        Usuario otroUsuario = crearUsuarioValido();
        otroUsuario.setIdUsuario("USR-2");

        when(usuarioGateway.buscarUsuarioPorId("USR-1"))
                .thenReturn(usuario);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(otroUsuario);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarUsuario(usuario)
        );


        assertEquals("El correo ya está registrado", excepcion.getMessage());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void actualizarUsuario_PasswordYaEncriptada_NoLaEncriptaDeNuevo() {

        usuario.setIdUsuario("USR-1");
        usuario.setPassword("$2b$12$password-encriptada");

        when(usuarioGateway.buscarUsuarioPorId("USR-1"))
                .thenReturn(usuario);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(usuarioGateway.actualizarUsuario(usuario))
                .thenReturn(usuario);


        Usuario resultado = usuarioUseCase.actualizarUsuario(usuario);


        assertSame(usuario, resultado);
        assertEquals("$2b$12$password-encriptada", resultado.getPassword());
        verifyNoInteractions(encrypterGateway);
        verify(usuarioGateway).actualizarUsuario(usuario);
    }



    @Test
    void eliminarUsuario_UsuarioExiste_EliminaUsuario() {

        when(usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);


        usuarioUseCase.eliminarUsuario(usuario.getIdUsuario());


        verify(usuarioGateway).buscarUsuarioPorId(usuario.getIdUsuario());
        verify(usuarioGateway).eliminarUsuario(usuario.getIdUsuario());
    }

    @Test
    void eliminarUsuario_UsuarioNoExiste_LanzaExcepcionYSinEliminar() {

        String usuarioId = "ID-INEXISTENTE";

        when(usuarioGateway.buscarUsuarioPorId(usuarioId))
                .thenReturn(null);


        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.eliminarUsuario(usuarioId)
        );


        assertEquals(
                "Usuario no encontrado con id: " + usuarioId,
                excepcion.getMessage()
        );
        verify(usuarioGateway, never()).eliminarUsuario(anyString());
    }

    private Usuario crearUsuarioValido() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setIdUsuario("USR-1");
        nuevoUsuario.setNombre("Sharik");
        nuevoUsuario.setCorreo("sharik@example.com");
        nuevoUsuario.setPassword("Password123");
        nuevoUsuario.setRol("USUARIO");
        nuevoUsuario.setEdad(25);
        nuevoUsuario.setNTelefono("3001234567");
        return nuevoUsuario;
    }

    private String calcularHash(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (Exception excepcion) {
            throw new AssertionError("No se pudo calcular el hash de prueba", excepcion);
        }
    }
}