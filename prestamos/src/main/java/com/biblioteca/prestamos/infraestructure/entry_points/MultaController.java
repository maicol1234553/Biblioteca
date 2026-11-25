package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.MultaUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MultaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/biblioteca/prestamos/multas", "/prestamos/multas"})
@RequiredArgsConstructor
public class MultaController {

    private final MultaUseCase multaUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<MultaResponse>> listarMultas(
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(multaUseCase
                .listarMultas(solicitante)
                .stream()
                .map(MultaResponse::from)
                .toList());
    }

    @GetMapping("/id/{idMulta}")
    public ResponseEntity<MultaResponse> buscarMulta(
            @PathVariable String idMulta,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(MultaResponse.from(
                multaUseCase.buscarMulta(idMulta, solicitante)));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MultaResponse>> listarMultasPorUsuario(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(multaUseCase
                .consultarMultasPorUsuario(usuarioId, solicitante)
                .stream()
                .map(MultaResponse::from)
                .toList());
    }

    @GetMapping("/usuario/{usuarioId}/pendientes")
    public ResponseEntity<List<MultaResponse>> listarMultasPendientesPorUsuario(
            @PathVariable String usuarioId,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(multaUseCase
                .consultarMultasPendientesPorUsuario(usuarioId, solicitante)
                .stream()
                .map(MultaResponse::from)
                .toList());
    }

    @GetMapping("/prestamo/{idPrestamo}")
    public ResponseEntity<List<MultaResponse>> listarMultasPorPrestamo(
            @PathVariable String idPrestamo,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(multaUseCase
                .listarMultasPorPrestamo(idPrestamo, solicitante)
                .stream()
                .map(MultaResponse::from)
                .toList());
    }

    @PutMapping("/{idMulta}/pagar")
    public ResponseEntity<MultaResponse> pagarMulta(
            @PathVariable String idMulta,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(MultaResponse.from(
                multaUseCase.pagarMulta(idMulta, solicitante)));
    }

    @PutMapping("/{idMulta}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MultaResponse> anularMulta(
            @PathVariable String idMulta,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(MultaResponse.from(
                multaUseCase.anularMulta(idMulta, solicitante)));
    }

    @DeleteMapping("/{idMulta}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeResponse> eliminarMulta(
            @PathVariable String idMulta,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        multaUseCase.eliminarMulta(idMulta, solicitante);

        return ResponseEntity.ok(new MensajeResponse("Multa eliminada con exito"));
    }
}
