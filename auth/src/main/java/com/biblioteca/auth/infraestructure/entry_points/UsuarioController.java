package com.biblioteca.auth.infraestructure.entry_points;

import com.biblioteca.auth.domain.model.EstadisticasUsuario;
import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.domain.usecase.UsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/biblioteca/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;

    public record CambiarPasswordRequest(String correo, String passwordActual, String passwordNueva) {
    }

    public record RecuperarPasswordRequest(String correo) {
    }

    public record RestablecerPasswordRequest(String token, String passwordNueva) {
    }

    public record CambiarRolRequest(String rol) {
    }

    @PostMapping("/save")
    public ResponseEntity<Usuario> guardarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioUseCase.guardarUsuario(usuario);

        return new ResponseEntity<>(usuarioGuardado, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Usuario> buscarUsuarioPorId(@PathVariable String id) {
        Usuario usuario = usuarioUseCase.buscarUsuarioPorId(id);

        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<Usuario> buscarUsuarioPorCorreo(@PathVariable String correo) {
        Usuario usuario = usuarioUseCase.buscarUsuarioPorCorreo(correo);

        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    @GetMapping("/existe/{correo}")
    public ResponseEntity<Boolean> existeUsuarioPorCorreo(@PathVariable String correo) {
        return ResponseEntity.ok(usuarioUseCase.existeUsuarioPorCorreo(correo));
    }

    @PutMapping("/update")
    public ResponseEntity<Usuario> actualizarUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioUseCase.actualizarUsuario(usuario);

        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable String id) {
        usuarioUseCase.eliminarUsuario(id);

        return new ResponseEntity<>("Usuario eliminado con éxito", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUsuario(@RequestBody Usuario usuario) {
        String mensajeRespuesta = usuarioUseCase.loginUsuario(usuario.getCorreo(), usuario.getPassword());

        return new ResponseEntity<>(mensajeRespuesta, HttpStatus.OK);
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(@RequestBody CambiarPasswordRequest request) {
        String mensaje = usuarioUseCase.cambiarPassword(
                request.correo(), request.passwordActual(), request.passwordNueva());

        return ResponseEntity.ok(mensaje);
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<String> recuperarPassword(@RequestBody RecuperarPasswordRequest request) {
        String mensaje = usuarioUseCase.solicitarRecuperacionPassword(request.correo());

        return ResponseEntity.ok(mensaje);
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<String> restablecerPassword(@RequestBody RestablecerPasswordRequest request) {
        String mensaje = usuarioUseCase.restablecerPassword(request.token(), request.passwordNueva());

        return ResponseEntity.ok(mensaje);
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<Usuario> cambiarRol(@PathVariable String id, @RequestBody CambiarRolRequest request) {
        Usuario usuario = usuarioUseCase.cambiarRol(id, request.rol());

        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasUsuario> obtenerEstadisticas() {
        return ResponseEntity.ok(usuarioUseCase.obtenerEstadisticas());
    }
}
