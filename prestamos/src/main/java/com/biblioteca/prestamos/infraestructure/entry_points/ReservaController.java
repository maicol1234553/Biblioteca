package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.ReservaUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ReservaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/biblioteca/prestamos/reservas", "/prestamos/reservas"})
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaUseCase reservaUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ReservaResponse>> listarReservas(
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(reservaUseCase
                .listarReservas(solicitante)
                .stream()
                .map(ReservaResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarReserva(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(ReservaResponse.from(
                reservaUseCase.buscarReserva(id, solicitante)));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaResponse>> listarReservasPorUsuario(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(reservaUseCase
                .consultarReservasPorUsuario(usuarioId, solicitante)
                .stream()
                .map(ReservaResponse::from)
                .toList());
    }

    @GetMapping("/libro/{libroId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ReservaResponse>> listarReservasPendientesPorLibro(
            @PathVariable String libroId,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(reservaUseCase
                .consultarReservasPendientesPorLibro(libroId, solicitante)
                .stream()
                .map(ReservaResponse::from)
                .toList());
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelarReserva(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(ReservaResponse.from(
                reservaUseCase.cancelarReserva(id, solicitante)));
    }

    @PostMapping("/expirar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<ReservaResponse>> expirarReservas(
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        List<Reserva> expiradas = reservaUseCase.expirarReservas(solicitante);

        return ResponseEntity.ok(expiradas.stream().map(ReservaResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeResponse> eliminarReserva(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        reservaUseCase.eliminarReserva(id, solicitante);

        return ResponseEntity.ok(new MensajeResponse("Reserva eliminada con exito"));
    }
}
