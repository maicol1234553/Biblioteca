package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.model.EventoNotificacion;
import com.biblioteca.notificacion.domain.model.HistorialEnvio;
import com.biblioteca.notificacion.domain.model.Notificacion;
import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.usecase.NotificacionUseCase;
import com.biblioteca.notificacion.domain.usecase.RecordatorioUseCase;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.AvisoVencidoRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.EnviarCorreoRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.EventoNotificacionRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.HistorialEnvioResponse;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.LibroDisponibleRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.NotificacionResponse;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.PrestamoConfirmadoRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.RecordatorioDevolucionRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.RecordatorioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/biblioteca/notificaciones", "/notificaciones", "/api/notificaciones"})
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionUseCase notificacionUseCase;
    private final RecordatorioUseCase recordatorioUseCase;

    @PostMapping("/enviar-correo")
    public ResponseEntity<NotificacionResponse> enviarCorreo(
            @Valid @RequestBody EnviarCorreoRequest request) {

        Notificacion notificacion = notificacionUseCase.enviarCorreo(
                request.idUsuario(),
                request.destinatario(),
                request.asunto(),
                request.mensaje(),
                request.tipo()
        );

        return new ResponseEntity<>(NotificacionResponse.from(notificacion), HttpStatus.CREATED);
    }

    @PostMapping("/recordatorio-devolucion")
    public ResponseEntity<RecordatorioResponse> programarRecordatorioDevolucion(
            @Valid @RequestBody RecordatorioDevolucionRequest request) {

        Recordatorio recordatorio = recordatorioUseCase.programarRecordatorioDevolucion(
                request.idUsuario(),
                request.idPrestamo(),
                request.fechaProgramada(),
                request.tipo()
        );

        return new ResponseEntity<>(RecordatorioResponse.from(recordatorio), HttpStatus.CREATED);
    }

    @PostMapping("/prestamo-confirmado")
    public ResponseEntity<NotificacionResponse> enviarPrestamoConfirmado(
            @Valid @RequestBody PrestamoConfirmadoRequest request) {

        Notificacion notificacion = notificacionUseCase.enviarPrestamoConfirmado(
                request.idUsuario(),
                request.idPrestamo(),
                request.destinatario(),
                request.mensaje()
        );

        return new ResponseEntity<>(NotificacionResponse.from(notificacion), HttpStatus.CREATED);
    }

    @PostMapping("/libro-disponible")
    public ResponseEntity<NotificacionResponse> enviarLibroDisponible(
            @Valid @RequestBody LibroDisponibleRequest request) {

        Notificacion notificacion = notificacionUseCase.enviarLibroDisponible(
                request.idUsuario(),
                request.idLibro(),
                request.destinatario(),
                request.mensaje()
        );

        return new ResponseEntity<>(NotificacionResponse.from(notificacion), HttpStatus.CREATED);
    }

    @PostMapping("/aviso-vencido")
    public ResponseEntity<NotificacionResponse> enviarAvisoVencido(
            @Valid @RequestBody AvisoVencidoRequest request) {

        Notificacion notificacion = notificacionUseCase.enviarAvisoVencido(
                request.idUsuario(),
                request.idPrestamo(),
                request.destinatario(),
                request.mensaje()
        );

        return new ResponseEntity<>(NotificacionResponse.from(notificacion), HttpStatus.CREATED);
    }




    @PostMapping("/evento")
    public ResponseEntity<NotificacionResponse> recibirEvento(
            @Valid @RequestBody EventoNotificacionRequest request) {

        EventoNotificacion evento = new EventoNotificacion(
                request.tipo(),
                request.idUsuario(),
                request.referenciaId(),
                request.mensaje(),
                request.fecha()
        );

        return new ResponseEntity<>(
                NotificacionResponse.from(notificacionUseCase.enviarEvento(evento)),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/reintentar/{id}")
    public ResponseEntity<NotificacionResponse> reintentarNotificacion(@PathVariable String id) {
        return ResponseEntity.ok(NotificacionResponse.from(
                notificacionUseCase.reintentarNotificacion(id)));
    }

    @PostMapping("/reintentar-fallidas")
    public ResponseEntity<List<NotificacionResponse>> reintentarFallidas() {
        return ResponseEntity.ok(notificacionUseCase
                .reintentarFallidas()
                .stream()
                .map(NotificacionResponse::from)
                .toList());
    }

    @GetMapping("/historial-usuario/{idUsuario}")
    public ResponseEntity<List<NotificacionResponse>> consultarHistorialUsuario(
            @PathVariable String idUsuario) {

        return ResponseEntity.ok(notificacionUseCase
                .consultarHistorialUsuario(idUsuario)
                .stream()
                .map(NotificacionResponse::from)
                .toList());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<NotificacionResponse>> consultarPendientes() {
        return ResponseEntity.ok(notificacionUseCase
                .consultarPendientes()
                .stream()
                .map(NotificacionResponse::from)
                .toList());
    }

    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> listarNotificaciones() {
        return ResponseEntity.ok(notificacionUseCase
                .listarNotificaciones()
                .stream()
                .map(NotificacionResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponse> buscarNotificacion(@PathVariable String id) {
        return ResponseEntity.ok(NotificacionResponse.from(
                notificacionUseCase.buscarNotificacion(id)));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialEnvioResponse>> consultarHistorialEnvios(
            @PathVariable String id) {

        List<HistorialEnvio> historial = notificacionUseCase.consultarHistorialEnvios(id);

        return ResponseEntity.ok(historial
                .stream()
                .map(HistorialEnvioResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarNotificacion(@PathVariable String id) {
        notificacionUseCase.eliminarNotificacion(id);

        return ResponseEntity.ok(new MensajeResponse("Notificacion eliminada con exito"));
    }
}
