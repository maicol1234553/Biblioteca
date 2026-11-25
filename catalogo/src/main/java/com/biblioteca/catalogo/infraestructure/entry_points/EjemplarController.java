package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.domain.usecase.EjemplarUseCase;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/biblioteca/ejemplares")
@RequiredArgsConstructor
public class EjemplarController {

    private final EjemplarUseCase ejemplarUseCase;

    public record EjemplarRequest(
            String idLibro,
            String codigoInventario,
            String ubicacion,
            String estado,
            LocalDate fechaAdquisicion,
            String observaciones
    ) {
    }

    @GetMapping
    public ResponseEntity<List<Ejemplar>> listarEjemplares() {
        return ResponseEntity.ok(ejemplarUseCase.listarEjemplares());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Ejemplar>> listarEjemplaresDisponibles() {
        return ResponseEntity.ok(ejemplarUseCase.listarEjemplaresDisponibles());
    }

    @GetMapping("/libro/{idLibro}")
    public ResponseEntity<List<Ejemplar>> listarEjemplaresPorLibro(@PathVariable String idLibro) {
        return ResponseEntity.ok(ejemplarUseCase.listarEjemplaresPorLibro(idLibro));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ejemplar> buscarEjemplarPorId(@PathVariable String id) {
        return ResponseEntity.ok(ejemplarUseCase.buscarEjemplarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Ejemplar> guardarEjemplar(@RequestBody EjemplarRequest request) {
        return new ResponseEntity<>(
                ejemplarUseCase.guardarEjemplar(aEjemplar(request)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ejemplar> actualizarEjemplar(@PathVariable String id,
                                                       @RequestBody EjemplarRequest request) {
        return ResponseEntity.ok(ejemplarUseCase.actualizarEjemplar(id, aEjemplar(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarEjemplar(@PathVariable String id) {
        ejemplarUseCase.eliminarEjemplar(id);

        return ResponseEntity.ok("Ejemplar eliminado con exito");
    }

    private Ejemplar aEjemplar(EjemplarRequest request) {
        Ejemplar ejemplar = new Ejemplar();

        ejemplar.setIdLibro(request.idLibro());
        ejemplar.setCodigoInventario(request.codigoInventario());
        ejemplar.setUbicacion(request.ubicacion());
        ejemplar.setEstado(request.estado());
        ejemplar.setFechaAdquisicion(request.fechaAdquisicion());
        ejemplar.setObservaciones(request.observaciones());

        return ejemplar;
    }
}
