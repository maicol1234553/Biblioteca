package com.biblioteca.auth.domain.usecase;

import com.biblioteca.auth.domain.exception.UsuarioPorCorreoNoEncontradoException;
import com.biblioteca.auth.domain.exception.UsuarioPorIdNoEncontradoException;
import com.biblioteca.auth.domain.model.EstadisticasUsuario;
import com.biblioteca.auth.domain.model.PasswordResetToken;
import com.biblioteca.auth.domain.model.Rol;
import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.domain.model.gateway.EncrypterGateway;
import com.biblioteca.auth.domain.model.gateway.NotificationGateway;
import com.biblioteca.auth.domain.model.gateway.PasswordResetTokenGateway;
import com.biblioteca.auth.domain.model.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class UsuarioUseCase {

    private static final int MINUTOS_VIGENCIA_TOKEN = 30;

    private final UsuarioGateway usuarioGateway;
    private final EncrypterGateway encrypterGateway;
    private final PasswordResetTokenGateway passwordResetTokenGateway;
    private final NotificationGateway notificationGateway;

    public Usuario guardarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        validarCorreoDisponible(usuario);

        if (usuario.getIdUsuario() == null || usuario.getIdUsuario().isBlank()) {
            usuario.setIdUsuario(UUID.randomUUID().toString());
        }

        cifrarPassword(usuario);
        return usuarioGateway.guardarUsuario(usuario);
    }

    public String loginUsuario(String correo, String password) {
        Usuario usuario = usuarioGateway.buscarUsuarioPorCorreo(correo);

        if (usuario == null) {
            return "Usuario no encontrado";
        }

        if (encrypterGateway.checkPass(password, usuario.getPassword())) {
            return "Logueo EXITOSO";
        }

        return "Credenciales invalidas";
    }

    public boolean existeUsuarioPorCorreo(String correo) {
        return usuarioGateway.buscarUsuarioPorCorreo(correo) != null;
    }

    public String cambiarPassword(String correo, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioGateway.buscarUsuarioPorCorreo(correo);

        if (usuario == null) {
            throw new UsuarioPorCorreoNoEncontradoException(
                    "Usuario no encontrado con correo: " + correo
            );
        }

        if (passwordActual == null || !encrypterGateway.checkPass(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        validarPasswordNueva(passwordNueva);

        usuario.setPassword(encrypterGateway.encrypt(passwordNueva));
        usuarioGateway.actualizarUsuario(usuario);

        // Notificación por SMS: contraseña cambiada
        notificationGateway.enviarSms(usuario.getNTelefono(),
                "Tu contraseña ha sido cambiada correctamente."
        );

        return "Contraseña actualizada correctamente";
    }

    public String solicitarRecuperacionPassword(String correo) {
        String respuesta = "Si el usuario está registrado, enviamos instrucciones de recuperación por SMS";

        Usuario usuario = usuarioGateway.buscarUsuarioPorCorreo(correo);

        if (usuario == null) {
            return respuesta;
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken registro = new PasswordResetToken(
                UUID.randomUUID().toString(),
                usuario.getIdUsuario(),
                hashToken(token),
                LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA_TOKEN),
                false
        );

        passwordResetTokenGateway.guardarToken(registro);

        // Enviar por SMS (el usuario debe tener nTelefono, validado por validarUsuario)
        notificationGateway.enviarSms(usuario.getNTelefono(),
                "Tu código de recuperación es: " + token + ". Expira en " + MINUTOS_VIGENCIA_TOKEN + " minutos."
        );

        return respuesta;
    }

    public String restablecerPassword(String token, String passwordNueva) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token es obligatorio");
        }

        validarPasswordNueva(passwordNueva);

        PasswordResetToken registro = passwordResetTokenGateway.buscarPorTokenHash(hashToken(token));

        if (registro == null || Boolean.TRUE.equals(registro.getUsado())) {
            throw new IllegalArgumentException("Token inválido o ya utilizado");
        }

        if (registro.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token expiró");
        }

        Usuario usuario = usuarioGateway.buscarUsuarioPorId(registro.getIdUsuario());

        if (usuario == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        usuario.setPassword(encrypterGateway.encrypt(passwordNueva));
        usuarioGateway.actualizarUsuario(usuario);

        registro.setUsado(true);
        passwordResetTokenGateway.actualizarToken(registro);

        // Notificación por SMS: contraseña restablecida
        notificationGateway.enviarSms(usuario.getNTelefono(),
                "Tu contraseña ha sido actualizada correctamente."
        );

        return "Contraseña actualizada correctamente";
    }

    public Usuario cambiarRol(String usuarioId, String rol) {
        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        Usuario usuario = buscarUsuarioPorId(usuarioId);
        usuario.setRol(Rol.desde(rol).name());

        return usuarioGateway.actualizarUsuario(usuario);
    }

    public EstadisticasUsuario obtenerEstadisticas() {
        return usuarioGateway.obtenerEstadisticas();
    }

    public Usuario buscarUsuarioPorId(String usuarioId){
        Usuario usuario = usuarioGateway.buscarUsuarioPorId(usuarioId);

        if (usuario == null) {
            throw new UsuarioPorIdNoEncontradoException(
                    "Usuario no encontrado con id: " + usuarioId

            );
        }
        return usuario;
    }
    public Usuario buscarUsuarioPorCorreo(String correo){
        Usuario usuario = usuarioGateway.buscarUsuarioPorCorreo(correo);

        if (usuario == null){
            throw new UsuarioPorCorreoNoEncontradoException(
                    "Usuerio no encontrado con correo: "+ correo
            );

        }
        return usuario;
    }
    public Usuario actualizarUsuario(Usuario usuario){
        Usuario existente = buscarUsuarioPorId(usuario.getIdUsuario());

        if (usuario.getRol() == null || usuario.getRol().isBlank()) {
            usuario.setRol(existente.getRol());
        }

        validarUsuario(usuario);
        validarCorreoDisponible(usuario);

        cifrarPassword(usuario);
        return usuarioGateway.actualizarUsuario(usuario);

    }
    public void eliminarUsuario(String usuarioId){
        buscarUsuarioPorId(usuarioId);
        usuarioGateway.eliminarUsuario(usuarioId);
    }

    private void cifrarPassword(Usuario usuario){
        String password = usuario.getPassword();

        if (password != null && !password.startsWith("$2")) {
            usuario.setPassword(encrypterGateway.encrypt(password));
        }
    }

    private void validarPasswordNueva(String passwordNueva) {
        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres");
        }
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se pudo generar el hash del token", exception);
        }
    }

    private void validarUsuario(Usuario usuario){

        if (usuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (usuario.getCorreo() == null || !usuario.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo no es válido");
        }

        if (usuario.getPassword() == null || usuario.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres");
        }

        usuario.setRol(Rol.desde(usuario.getRol()).name());

        if (usuario.getEdad() == null || usuario.getEdad() < 18) {
            throw new IllegalArgumentException("Usuario menor de edad");
        }

        if (usuario.getEdad() > 122) {
            throw new IllegalArgumentException("No puede tener más de 122 años");
        }

        if (usuario.getNTelefono() == null || usuario.getNTelefono().isBlank()) {
            throw new IllegalArgumentException("El número telefónico es obligatorio");
        }

        if (!usuario.getNTelefono().matches("\\d{10}")) {
            throw new IllegalArgumentException("El número telefónico debe tener 10 dígitos");
        }
    }

    private void validarCorreoDisponible (Usuario usuario){

        Usuario existente = usuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo());

        if (existente != null && !Objects.equals(existente.getIdUsuario(), usuario.getIdUsuario())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

    }
}
