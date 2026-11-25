package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.LibroExterno;
import com.biblioteca.catalogo.domain.usecase.CatalogoExternoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/biblioteca/externo")
@RequiredArgsConstructor
public class CatalogoExternoController {

    private final CatalogoExternoUseCase catalogoExternoUseCase;

    @GetMapping("/buscar")
    public ResponseEntity<List<LibroExterno>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(catalogoExternoUseCase.buscar(q));
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<LibroExterno> buscarPorIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(catalogoExternoUseCase.buscarPorIsbn(isbn));
    }

    @PostMapping("/importar/{isbn}")
    public ResponseEntity<Libro> importarPorIsbn(
            @PathVariable String isbn,
            @RequestParam(required = false) String idCategoria) {

        return new ResponseEntity<>(
                catalogoExternoUseCase.importarPorIsbn(isbn, idCategoria), HttpStatus.CREATED);
    }
}
