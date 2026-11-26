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

    // GUARDAR USUARIO

    @Test
    void guardarUsuario_UsuarioValidoSinId_GeneraIdEncriptaPasswordYGuarda() {
        // Arrange
        usuario.setIdUsuario(null);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(null);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);

        // Assert
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
        // Arrange
        usuario.setPassword("$2b$12$password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(null);

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);

        // Assert
        assertSame(usuario, resultado);
        assertEquals("$2b$12$password-encriptada", resultado.getPassword());

        verifyNoInteractions(encrypterGateway);
        verify(usuarioGateway).guardarUsuario(usuario);
    }

    @Test
    void guardarUsuario_CorreoDeOtroUsuario_LanzaExcepcion() {
        // Arrange
        Usuario existente = crearUsuarioValido();
        existente.setIdUsuario("OTRO-ID");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(existente);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals("El correo ya está registrado", excepcion.getMessage());
        verify(usuarioGateway, never()).guardarUsuario(any(Usuario.class));
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void guardarUsuario_MismoIdYCorreoExistente_PermiteGuardar() {
        // Arrange
        usuario.setIdUsuario("USR-1");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);

        // Assert
        assertSame(usuario, resultado);
        assertEquals("password-encriptada", resultado.getPassword());
        verify(usuarioGateway).guardarUsuario(usuario);
    }

    @Test
    void guardarUsuario_UsuarioNulo_LanzaExcepcion() {
        // Arrange
        Usuario usuarioNulo = null;

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuarioNulo)
        );

        // Assert
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
        // Arrange
        usuario.setNombre(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals("El nombre es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_NombreVacio_LanzaExcepcion() {
        // Arrange
        usuario.setNombre(" ");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals("El nombre es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_CorreoNulo_LanzaExcepcion() {
        // Arrange
        usuario.setCorreo(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals("El correo no es válido", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_CorreoSinArroba_LanzaExcepcion() {
        // Arrange
        usuario.setCorreo("correo-invalido.com");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals("El correo no es válido", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_PasswordNula_LanzaExcepcion() {
        // Arrange
        usuario.setPassword(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_PasswordMenorA8Caracteres_LanzaExcepcion() {
        // Arrange
        usuario.setPassword("1234567");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_EdadNula_LanzaExcepcion() {
        // Arrange
        usuario.setEdad(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals("Usuario menor de edad", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_MenorDe18Anios_LanzaExcepcion() {
        // Arrange
        usuario.setEdad(17);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals("Usuario menor de edad", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_EdadMayorA122_LanzaExcepcion() {
        // Arrange
        usuario.setEdad(123);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals(
                "No puede tener más de 122 años",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_TelefonoNulo_LanzaExcepcion() {
        // Arrange
        usuario.setNTelefono(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals(
                "El número telefónico es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_TelefonoVacio_LanzaExcepcion() {
        // Arrange
        usuario.setNTelefono(" ");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals(
                "El número telefónico es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_TelefonoConFormatoIncorrecto_LanzaExcepcion() {
        // Arrange
        usuario.setNTelefono("300ABC4567");

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.guardarUsuario(usuario)
        );

        // Assert
        assertEquals(
                "El número telefónico debe tener 10 dígitos",
                excepcion.getMessage()
        );
        verifyNoInteractions(usuarioGateway, encrypterGateway);
    }

    @Test
    void guardarUsuario_EdadMinimaYMaximaValidas_PermiteGuardar() {
        // Arrange
        usuario.setEdad(18);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(null);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.guardarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);

        // Assert
        assertSame(usuario, resultado);
        assertEquals(18, resultado.getEdad());
        verify(usuarioGateway).guardarUsuario(usuario);
    }

    // INICIAR SESIÓN

    @Test
    void loginUsuario_CredencialesCorrectas_RetornaExito() {
        // Arrange
        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-encriptada"
        )).thenReturn(true);

        // Act
        String resultado = usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                "Password123"
        );

        // Assert
        assertEquals("Logueo EXITOSO", resultado);
        verify(encrypterGateway).checkPass(
                "Password123",
                "password-encriptada"
        );
    }

    @Test
    void loginUsuario_UsuarioNoExiste_RetornaUsuarioNoEncontrado() {
        // Arrange
        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);

        // Act
        String resultado = usuarioUseCase.loginUsuario(correo, "Password123");

        // Assert
        assertEquals("Usuario no encontrado", resultado);
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void loginUsuario_PasswordIncorrecta_RetornaCredencialesInvalidas() {
        // Arrange
        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "password-incorrecta",
                "password-encriptada"
        )).thenReturn(false);

        // Act
        String resultado = usuarioUseCase.loginUsuario(
                usuario.getCorreo(),
                "password-incorrecta"
        );

        // Assert
        assertEquals("Credenciales invalidas", resultado);
    }

    // CONSULTAR EXISTENCIA

    @Test
    void existeUsuarioPorCorreo_UsuarioExiste_RetornaTrue() {
        // Arrange
        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        // Act
        boolean resultado =
                usuarioUseCase.existeUsuarioPorCorreo(usuario.getCorreo());

        // Assert
        assertTrue(resultado);
        verify(usuarioGateway).buscarUsuarioPorCorreo(usuario.getCorreo());
    }

    @Test
    void existeUsuarioPorCorreo_UsuarioNoExiste_RetornaFalse() {
        // Arrange
        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);

        // Act
        boolean resultado = usuarioUseCase.existeUsuarioPorCorreo(correo);

        // Assert
        assertFalse(resultado);
    }

    // CAMBIAR CONTRASEÑA

    @Test
    void cambiarPassword_DatosValidos_EncriptaYActualiza() {
        // Arrange
        usuario.setPassword("password-anterior-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-anterior-encriptada"
        )).thenReturn(true);

        when(encrypterGateway.encrypt("PasswordNueva123"))
                .thenReturn("password-nueva-encriptada");

        // Act
        String resultado = usuarioUseCase.cambiarPassword(
                usuario.getCorreo(),
                "Password123",
                "PasswordNueva123"
        );

        // Assert
        assertEquals("Contraseña actualizada correctamente", resultado);
        assertEquals("password-nueva-encriptada", usuario.getPassword());
        verify(usuarioGateway).actualizarUsuario(usuario);
    }

    @Test
    void cambiarPassword_UsuarioNoExiste_LanzaExcepcion() {
        // Arrange
        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);

        // Act
        UsuarioPorCorreoNoEncontradoException excepcion = assertThrows(
                UsuarioPorCorreoNoEncontradoException.class,
                () -> usuarioUseCase.cambiarPassword(
                        correo,
                        "Password123",
                        "PasswordNueva123"
                )
        );

        // Assert
        assertEquals(
                "Usuario no encontrado con correo: " + correo,
                excepcion.getMessage()
        );
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void cambiarPassword_PasswordActualNula_LanzaExcepcion() {
        // Arrange
        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        null,
                        "PasswordNueva123"
                )
        );

        // Assert
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
        // Arrange
        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "password-incorrecta",
                "password-encriptada"
        )).thenReturn(false);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        "password-incorrecta",
                        "PasswordNueva123"
                )
        );

        // Assert
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
        // Arrange
        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-encriptada"
        )).thenReturn(true);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        "Password123",
                        null
                )
        );

        // Assert
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
        // Arrange
        usuario.setPassword("password-encriptada");

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.checkPass(
                "Password123",
                "password-encriptada"
        )).thenReturn(true);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarPassword(
                        usuario.getCorreo(),
                        "Password123",
                        "1234567"
                )
        );

        // Assert
        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verify(encrypterGateway, never()).encrypt(anyString());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }

    // SOLICITAR RECUPERACIÓN DE CONTRASEÑA

    @Test
    void solicitarRecuperacionPassword_CorreoRegistrado_GuardaTokenYEnviaCorreo() {
        // Arrange
        String correo = usuario.getCorreo();
        LocalDateTime antesDeLaSolicitud = LocalDateTime.now();

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(usuario);

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        ArgumentCaptor<String> mensajeCaptor =
                ArgumentCaptor.forClass(String.class);

        // Act
        String resultado =
                usuarioUseCase.solicitarRecuperacionPassword(correo);

        // Assert
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
        // Arrange
        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);

        // Act
        String resultado =
                usuarioUseCase.solicitarRecuperacionPassword(correo);

        // Assert
        assertEquals(
                "Si el correo está registrado, enviamos instrucciones de recuperación",
                resultado
        );

        verifyNoInteractions(passwordResetTokenGateway, notificationGateway);
    }

    // RESTABLECER CONTRASEÑA

    @Test
    void restablecerPassword_TokenValido_ActualizaPasswordYMarcaTokenUsado() {
        // Arrange
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

        // Act
        String resultado = usuarioUseCase.restablecerPassword(
                tokenPlano,
                "PasswordNueva123"
        );

        // Assert
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
        // Arrange
        String token = null;

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        token,
                        "PasswordNueva123"
                )
        );

        // Assert
        assertEquals("El token es obligatorio", excepcion.getMessage());
        verifyNoInteractions(
                usuarioGateway,
                encrypterGateway,
                passwordResetTokenGateway
        );
    }

    @Test
    void restablecerPassword_TokenVacio_LanzaExcepcion() {
        // Arrange
        String token = " ";

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        token,
                        "PasswordNueva123"
                )
        );

        // Assert
        assertEquals("El token es obligatorio", excepcion.getMessage());
        verifyNoInteractions(passwordResetTokenGateway);
    }

    @Test
    void restablecerPassword_PasswordNuevaCorta_LanzaExcepcionAntesDeBuscarToken() {
        // Arrange
        String token = "token-de-prueba";

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(token, "1234567")
        );

        // Assert
        assertEquals(
                "La contraseña debe tener mínimo 8 caracteres",
                excepcion.getMessage()
        );
        verifyNoInteractions(passwordResetTokenGateway, usuarioGateway);
    }

    @Test
    void restablecerPassword_TokenInexistente_LanzaExcepcion() {
        // Arrange
        String tokenPlano = "token-inexistente";
        String tokenHash = calcularHash(tokenPlano);

        when(passwordResetTokenGateway.buscarPorTokenHash(tokenHash))
                .thenReturn(null);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );

        // Assert
        assertEquals(
                "Token inválido o ya utilizado",
                excepcion.getMessage()
        );
        verify(usuarioGateway, never()).buscarUsuarioPorId(anyString());
    }

    @Test
    void restablecerPassword_TokenYaUsado_LanzaExcepcion() {
        // Arrange
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

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );

        // Assert
        assertEquals(
                "Token inválido o ya utilizado",
                excepcion.getMessage()
        );
        verify(usuarioGateway, never()).buscarUsuarioPorId(anyString());
    }

    @Test
    void restablecerPassword_TokenExpirado_LanzaExcepcion() {
        // Arrange
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

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );

        // Assert
        assertEquals("El token expiró", excepcion.getMessage());
        verify(usuarioGateway, never()).buscarUsuarioPorId(anyString());
    }

    @Test
    void restablecerPassword_UsuarioDelTokenNoExiste_LanzaExcepcion() {
        // Arrange
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

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.restablecerPassword(
                        tokenPlano,
                        "PasswordNueva123"
                )
        );

        // Assert
        assertEquals("Token inválido", excepcion.getMessage());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
        verify(passwordResetTokenGateway, never())
                .actualizarToken(any(PasswordResetToken.class));
    }

    // CAMBIAR ROL

    @Test
    void cambiarRol_RolValido_ActualizaUsuario() {
        // Arrange
        when(usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);

        when(usuarioGateway.actualizarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.cambiarRol(
                usuario.getIdUsuario(),
                "ADMINISTRADOR"
        );

        // Assert
        assertSame(usuario, resultado);
        assertEquals("ADMINISTRADOR", resultado.getRol());
        verify(usuarioGateway).actualizarUsuario(usuario);
    }

    @Test
    void cambiarRol_RolNulo_LanzaExcepcion() {
        // Arrange
        String rol = null;

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarRol(usuario.getIdUsuario(), rol)
        );

        // Assert
        assertEquals("El rol es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway);
    }

    @Test
    void cambiarRol_RolVacio_LanzaExcepcion() {
        // Arrange
        String rol = " ";

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.cambiarRol(usuario.getIdUsuario(), rol)
        );

        // Assert
        assertEquals("El rol es obligatorio", excepcion.getMessage());
        verifyNoInteractions(usuarioGateway);
    }

    @Test
    void cambiarRol_UsuarioNoExiste_LanzaExcepcion() {
        // Arrange
        String usuarioId = "ID-INEXISTENTE";

        when(usuarioGateway.buscarUsuarioPorId(usuarioId))
                .thenReturn(null);

        // Act
        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.cambiarRol(usuarioId, "ADMINISTRADOR")
        );

        // Assert
        assertEquals(
                "Usuario no encontrado con id: " + usuarioId,
                excepcion.getMessage()
        );
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }

    // ESTADÍSTICAS

    @Test
    void obtenerEstadisticas_ConsultaGateway_RetornaResultado() {
        // Arrange
        EstadisticasUsuario estadisticasEsperadas =
                mock(EstadisticasUsuario.class);

        when(usuarioGateway.obtenerEstadisticas())
                .thenReturn(estadisticasEsperadas);

        // Act
        EstadisticasUsuario resultado =
                usuarioUseCase.obtenerEstadisticas();

        // Assert
        assertSame(estadisticasEsperadas, resultado);
        verify(usuarioGateway).obtenerEstadisticas();
    }

    // BUSCAR USUARIO POR ID

    @Test
    void buscarUsuarioPorId_UsuarioExiste_RetornaUsuario() {
        // Arrange
        when(usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);

        // Act
        Usuario resultado =
                usuarioUseCase.buscarUsuarioPorId(usuario.getIdUsuario());

        // Assert
        assertSame(usuario, resultado);
        verify(usuarioGateway).buscarUsuarioPorId(usuario.getIdUsuario());
    }

    @Test
    void buscarUsuarioPorId_UsuarioNoExiste_LanzaExcepcion() {
        // Arrange
        String usuarioId = "ID-INEXISTENTE";

        when(usuarioGateway.buscarUsuarioPorId(usuarioId))
                .thenReturn(null);

        // Act
        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.buscarUsuarioPorId(usuarioId)
        );

        // Assert
        assertEquals(
                "Usuario no encontrado con id: " + usuarioId,
                excepcion.getMessage()
        );
    }

    // BUSCAR USUARIO POR CORREO

    @Test
    void buscarUsuarioPorCorreo_UsuarioExiste_RetornaUsuario() {
        // Arrange
        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        // Act
        Usuario resultado =
                usuarioUseCase.buscarUsuarioPorCorreo(usuario.getCorreo());

        // Assert
        assertSame(usuario, resultado);
        verify(usuarioGateway).buscarUsuarioPorCorreo(usuario.getCorreo());
    }

    @Test
    void buscarUsuarioPorCorreo_UsuarioNoExiste_LanzaExcepcion() {
        // Arrange
        String correo = "inexistente@correo.com";

        when(usuarioGateway.buscarUsuarioPorCorreo(correo))
                .thenReturn(null);

        // Act
        UsuarioPorCorreoNoEncontradoException excepcion = assertThrows(
                UsuarioPorCorreoNoEncontradoException.class,
                () -> usuarioUseCase.buscarUsuarioPorCorreo(correo)
        );

        // Assert
        assertEquals(
                "Usuerio no encontrado con correo: " + correo,
                excepcion.getMessage()
        );
    }

    // ACTUALIZAR USUARIO

    @Test
    void actualizarUsuario_DatosValidos_EncriptaPasswordYActualiza() {
        // Arrange
        usuario.setIdUsuario("USR-1");

        when(usuarioGateway.buscarUsuarioPorId("USR-1"))
                .thenReturn(usuario);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(encrypterGateway.encrypt("Password123"))
                .thenReturn("password-encriptada");

        when(usuarioGateway.actualizarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.actualizarUsuario(usuario);

        // Assert
        assertSame(usuario, resultado);
        assertEquals("password-encriptada", resultado.getPassword());
        verify(usuarioGateway).actualizarUsuario(usuario);
    }

    @Test
    void actualizarUsuario_RolVacio_ConservaRolExistente() {
        // Arrange
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

        // Act
        Usuario resultado =
                usuarioUseCase.actualizarUsuario(usuarioActualizado);

        // Assert
        assertSame(usuarioActualizado, resultado);
        assertEquals("ADMINISTRADOR", resultado.getRol());
        verify(usuarioGateway).actualizarUsuario(usuarioActualizado);
    }

    @Test
    void actualizarUsuario_UsuarioNoExiste_LanzaExcepcion() {
        // Arrange
        usuario.setIdUsuario("ID-INEXISTENTE");

        when(usuarioGateway.buscarUsuarioPorId("ID-INEXISTENTE"))
                .thenReturn(null);

        // Act
        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.actualizarUsuario(usuario)
        );

        // Assert
        assertEquals(
                "Usuario no encontrado con id: ID-INEXISTENTE",
                excepcion.getMessage()
        );
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
    }

    @Test
    void actualizarUsuario_CorreoDeOtroUsuario_LanzaExcepcion() {
        // Arrange
        usuario.setIdUsuario("USR-1");

        Usuario otroUsuario = crearUsuarioValido();
        otroUsuario.setIdUsuario("USR-2");

        when(usuarioGateway.buscarUsuarioPorId("USR-1"))
                .thenReturn(usuario);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(otroUsuario);

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioUseCase.actualizarUsuario(usuario)
        );

        // Assert
        assertEquals("El correo ya está registrado", excepcion.getMessage());
        verify(usuarioGateway, never())
                .actualizarUsuario(any(Usuario.class));
        verifyNoInteractions(encrypterGateway);
    }

    @Test
    void actualizarUsuario_PasswordYaEncriptada_NoLaEncriptaDeNuevo() {
        // Arrange
        usuario.setIdUsuario("USR-1");
        usuario.setPassword("$2b$12$password-encriptada");

        when(usuarioGateway.buscarUsuarioPorId("USR-1"))
                .thenReturn(usuario);

        when(usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo()))
                .thenReturn(usuario);

        when(usuarioGateway.actualizarUsuario(usuario))
                .thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.actualizarUsuario(usuario);

        // Assert
        assertSame(usuario, resultado);
        assertEquals("$2b$12$password-encriptada", resultado.getPassword());
        verifyNoInteractions(encrypterGateway);
        verify(usuarioGateway).actualizarUsuario(usuario);
    }

    // ELIMINAR USUARIO

    @Test
    void eliminarUsuario_UsuarioExiste_EliminaUsuario() {
        // Arrange
        when(usuarioGateway.buscarUsuarioPorId(usuario.getIdUsuario()))
                .thenReturn(usuario);

        // Act
        usuarioUseCase.eliminarUsuario(usuario.getIdUsuario());

        // Assert
        verify(usuarioGateway).buscarUsuarioPorId(usuario.getIdUsuario());
        verify(usuarioGateway).eliminarUsuario(usuario.getIdUsuario());
    }

    @Test
    void eliminarUsuario_UsuarioNoExiste_LanzaExcepcionYSinEliminar() {
        // Arrange
        String usuarioId = "ID-INEXISTENTE";

        when(usuarioGateway.buscarUsuarioPorId(usuarioId))
                .thenReturn(null);

        // Act
        UsuarioPorIdNoEncontradoException excepcion = assertThrows(
                UsuarioPorIdNoEncontradoException.class,
                () -> usuarioUseCase.eliminarUsuario(usuarioId)
        );

        // Assert
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