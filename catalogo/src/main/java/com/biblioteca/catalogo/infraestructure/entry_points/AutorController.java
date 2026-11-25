package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.usecase.AutorUseCase;
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

import java.util.List;

@RestController
@RequestMapping("/api/biblioteca/autores")
@RequiredArgsConstructor
public class AutorController {

    private final AutorUseCase autorUseCase;

    @GetMapping
    public ResponseEntity<List<Autor>> listarAutores() {
        return ResponseEntity.ok(autorUseCase.listarAutores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> buscarAutorPorId(@PathVariable String id) {
        return ResponseEntity.ok(autorUseCase.buscarAutorPorId(id));
    }

    @PostMapping
    public ResponseEntity<Autor> guardarAutor(@RequestBody Autor autor) {
        return new ResponseEntity<>(autorUseCase.guardarAutor(autor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Autor> actualizarAutor(@PathVariable String id, @RequestBody Autor autor) {
        return ResponseEntity.ok(autorUseCase.actualizarAutor(id, autor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarAutor(@PathVariable String id) {
        autorUseCase.eliminarAutor(id);

        return ResponseEntity.ok("Autor eliminado con exito");
    }
}
