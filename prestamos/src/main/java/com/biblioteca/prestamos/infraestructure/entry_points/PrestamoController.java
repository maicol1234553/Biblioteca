package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.DevolucionUseCase;
import com.biblioteca.prestamos.domain.usecase.MultaUseCase;
import com.biblioteca.prestamos.domain.usecase.PrestamoUseCase;
import com.biblioteca.prestamos.domain.usecase.RenovacionUseCase;
import com.biblioteca.prestamos.domain.usecase.ReservaUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ActualizarVencimientoRequest;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.DevolucionResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.DevolverPrestamoRequest;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MultaResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.PrestamoResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.RenovacionResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ReservarLibroRequest;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.ReservaResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.SolicitarPrestamoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/biblioteca/prestamos", "/prestamos"})
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoUseCase prestamoUseCase;
    private final ReservaUseCase reservaUseCase;
    private final RenovacionUseCase renovacionUseCase;
    private final MultaUseCase multaUseCase;
    private final DevolucionUseCase devolucionUseCase;

    @PostMapping("/solicitar")
    public ResponseEntity<PrestamoResponse> solicitarPrestamo(
            @Valid @RequestBody SolicitarPrestamoRequest request,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        Prestamo prestamo = prestamoUseCase.solicitarPrestamo(
                request.idUsuario(), request.idEjemplar(), solicitante);

        return new ResponseEntity<>(PrestamoResponse.from(prestamo), HttpStatus.CREATED);
    }

    @PostMapping("/devolver/{id}")
    public ResponseEntity<DevolucionResponse> devolverPrestamo(
            @PathVariable String id,
            @Valid @RequestBody DevolverPrestamoRequest request,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        Devolucion devolucion = prestamoUseCase.devolverPrestamo(
                id, request.estadoEjemplar(), request.observaciones(), solicitante);

        return new ResponseEntity<>(DevolucionResponse.from(devolucion), HttpStatus.CREATED);
    }

    @PostMapping("/reservar/{libroId}")
    public ResponseEntity<ReservaResponse> reservarLibro(
            @PathVariable String libroId,
            @Valid @RequestBody ReservarLibroRequest request,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        Reserva reserva = reservaUseCase.reservarLibro(
                libroId, request.idUsuario(), solicitante);

        return new ResponseEntity<>(ReservaResponse.from(reserva), HttpStatus.CREATED);
    }

    @PostMapping("/renovar/{id}")
    public ResponseEntity<PrestamoResponse> renovarPrestamo(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(PrestamoResponse.from(
                renovacionUseCase.renovarPrestamo(id, solicitante)));
    }

    @GetMapping("/mis-prestamos/{usuarioId}")
    public ResponseEntity<List<PrestamoResponse>> consultarMisPrestamos(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(prestamoUseCase
                .consultarMisPrestamos(usuarioId, solicitante)
                .stream()
                .map(PrestamoResponse::from)
                .toList());
    }

    @GetMapping("/historial/{usuarioId}")
    public ResponseEntity<List<PrestamoResponse>> consultarHistorial(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(prestamoUseCase
                .consultarHistorial(usuarioId, solicitante)
                .stream()
                .map(PrestamoResponse::from)
                .toList());
    }

    @GetMapping("/multas/{usuarioId}")
    public ResponseEntity<List<MultaResponse>> consultarMultasPendientes(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        List<Multa> multas = multaUseCase.consultarMultasPendientesPorUsuario(
                usuarioId, solicitante);

        return ResponseEntity.ok(multas.stream().map(MultaResponse::from).toList());
    }

    @GetMapping("/vencidos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PrestamoResponse>> consultarPrestamosVencidos(
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(prestamoUseCase
                .consultarPrestamosVencidos(solicitante)
                .stream()
                .map(PrestamoResponse::from)
                .toList());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<PrestamoResponse>> listarPrestamos(
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(prestamoUseCase
                .listarPrestamos(solicitante)
                .stream()
                .map(PrestamoResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponse> buscarPrestamo(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(PrestamoResponse.from(
                prestamoUseCase.buscarPrestamoPorId(id, solicitante)));
    }

    @GetMapping("/{id}/devolucion")
    public ResponseEntity<DevolucionResponse> consultarDevolucion(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(DevolucionResponse.from(
                devolucionUseCase.buscarDevolucionPorPrestamo(id, solicitante)));
    }

    @GetMapping("/{id}/multas")
    public ResponseEntity<List<MultaResponse>> consultarMultasDelPrestamo(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(multaUseCase
                .listarMultasPorPrestamo(id, solicitante)
                .stream()
                .map(MultaResponse::from)
                .toList());
    }

    @GetMapping("/{id}/renovaciones")
    public ResponseEntity<List<RenovacionResponse>> consultarRenovacionesDelPrestamo(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(renovacionUseCase
                .listarRenovacionesPorPrestamo(id, solicitante)
                .stream()
                .map(RenovacionResponse::from)
                .toList());
    }

    @PutMapping("/{id}/vencimiento")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PrestamoResponse> actualizarVencimiento(
            @PathVariable String id,
            @Valid @RequestBody ActualizarVencimientoRequest request,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(PrestamoResponse.from(
                prestamoUseCase.actualizarVencimiento(
                        id, request.fechaVencimiento(), solicitante)));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PrestamoResponse> cancelarPrestamo(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(PrestamoResponse.from(
                prestamoUseCase.cancelarPrestamo(id, solicitante)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeResponse> eliminarPrestamo(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        prestamoUseCase.eliminarPrestamo(id, solicitante);

        return ResponseEntity.ok(new MensajeResponse("Prestamo eliminado con exito"));
    }
}
