package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.domain.usecase.EditorialUseCase;
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
@RequestMapping("/api/biblioteca/editoriales")
@RequiredArgsConstructor
public class EditorialController {

    private final EditorialUseCase editorialUseCase;

    @GetMapping
    public ResponseEntity<List<Editorial>> listarEditoriales() {
        return ResponseEntity.ok(editorialUseCase.listarEditoriales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Editorial> buscarEditorialPorId(@PathVariable String id) {
        return ResponseEntity.ok(editorialUseCase.buscarEditorialPorId(id));
    }

    @PostMapping
    public ResponseEntity<Editorial> guardarEditorial(@RequestBody Editorial editorial) {
        return new ResponseEntity<>(
                editorialUseCase.guardarEditorial(editorial), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Editorial> actualizarEditorial(@PathVariable String id,
                                                         @RequestBody Editorial editorial) {
        return ResponseEntity.ok(editorialUseCase.actualizarEditorial(id, editorial));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarEditorial(@PathVariable String id) {
        editorialUseCase.eliminarEditorial(id);

        return ResponseEntity.ok("Editorial eliminada con exito");
    }
}
