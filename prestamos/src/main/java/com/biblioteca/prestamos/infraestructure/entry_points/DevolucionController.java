package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.DevolucionUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.DevolucionResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
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
@RequestMapping({"/api/biblioteca/prestamos/devoluciones", "/prestamos/devoluciones"})
@RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionUseCase devolucionUseCase;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<DevolucionResponse>> listarDevoluciones(
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(devolucionUseCase
                .listarDevoluciones(solicitante)
                .stream()
                .map(DevolucionResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevolucionResponse> buscarDevolucion(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        return ResponseEntity.ok(DevolucionResponse.from(
                devolucionUseCase.buscarDevolucion(id, solicitante)));
    }

    @GetMapping("/prestamo/{idPrestamo}")
    public ResponseEntity<List<DevolucionResponse>> listarDevolucionesPorPrestamo(
            @PathVariable String idPrestamo,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        List<Devolucion> devoluciones = devolucionUseCase
                .listarDevolucionesPorPrestamo(idPrestamo, solicitante);

        return ResponseEntity.ok(devoluciones.stream().map(DevolucionResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeResponse> eliminarDevolucion(
            @PathVariable String id,
            @AuthenticationPrincipal UsuarioExterno solicitante) {

        devolucionUseCase.eliminarDevolucion(id, solicitante);

        return ResponseEntity.ok(new MensajeResponse("Devolucion eliminada con exito"));
    }
}
