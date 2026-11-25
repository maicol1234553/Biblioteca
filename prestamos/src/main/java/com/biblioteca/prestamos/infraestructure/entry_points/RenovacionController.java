package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.RenovacionUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.RenovacionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/biblioteca/prestamos/renovaciones", "/prestamos/renovaciones"})
@RequiredArgsConstructor
public class RenovacionController {

    private final RenovacionUseCase renovacionUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<RenovacionResponse>> listarRenovaciones(
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(renovacionUseCase
                .listarRenovaciones(solicitante)
                .stream()
                .map(RenovacionResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RenovacionResponse> buscarRenovacion(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(RenovacionResponse.from(
                renovacionUseCase.buscarRenovacion(id, solicitante)));
    }

    @GetMapping("/prestamo/{idPrestamo}")
    public ResponseEntity<List<RenovacionResponse>> listarRenovacionesPorPrestamo(
            @PathVariable String idPrestamo,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        List<Renovacion> renovaciones = renovacionUseCase
                .listarRenovacionesPorPrestamo(idPrestamo, solicitante);

        return ResponseEntity.ok(renovaciones.stream().map(RenovacionResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeResponse> eliminarRenovacion(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        renovacionUseCase.eliminarRenovacion(id, solicitante);

        return ResponseEntity.ok(new MensajeResponse("Renovacion eliminada con exito"));
    }
}
