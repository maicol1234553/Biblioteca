package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.BusquedaLibro;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.DisponibilidadLibro;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.usecase.EjemplarUseCase;
import com.biblioteca.catalogo.domain.usecase.LibroUseCase;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/biblioteca/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroUseCase libroUseCase;
    private final EjemplarUseCase ejemplarUseCase;

    public record LibroRequest(
            String isbn,
            String titulo,
            String subtitulo,
            LocalDate fechaPublicacion,
            String idioma,
            Integer numeroPaginas,
            String descripcion,
            String portadaUrl,
            String idEditorial,
            List<String> idsCategorias,
            List<String> idsAutores
    ) {
    }

    @GetMapping
    public ResponseEntity<List<Libro>> listarLibros() {
        return ResponseEntity.ok(libroUseCase.listarLibros());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Libro>> buscarLibros(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String idAutor,
            @RequestParam(required = false) String idCategoria,
            @RequestParam(required = false) String idEditorial,
            @RequestParam(required = false) String idioma) {

        BusquedaLibro busqueda = new BusquedaLibro(
                texto, idAutor, idCategoria, idEditorial, idioma);

        return ResponseEntity.ok(libroUseCase.buscarLibros(busqueda));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> buscarLibroPorId(@PathVariable String id) {
        return ResponseEntity.ok(libroUseCase.buscarLibroPorId(id));
    }

    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<DisponibilidadLibro> consultarDisponibilidad(@PathVariable String id) {
        return ResponseEntity.ok(ejemplarUseCase.consultarDisponibilidad(id));
    }

    @PostMapping
    public ResponseEntity<Libro> guardarLibro(@RequestBody LibroRequest request) {
        return new ResponseEntity<>(
                libroUseCase.guardarLibro(aLibro(request)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizarLibro(@PathVariable String id,
                                                 @RequestBody LibroRequest request) {
        return ResponseEntity.ok(libroUseCase.actualizarLibro(id, aLibro(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarLibro(@PathVariable String id) {
        libroUseCase.eliminarLibro(id);

        return ResponseEntity.ok("Libro eliminado con exito");
    }

    private Libro aLibro(LibroRequest request) {
        Libro libro = new Libro();

        libro.setIsbn(request.isbn());
        libro.setTitulo(request.titulo());
        libro.setSubtitulo(request.subtitulo());
        libro.setFechaPublicacion(request.fechaPublicacion());
        libro.setIdioma(request.idioma());
        libro.setNumeroPaginas(request.numeroPaginas());
        libro.setDescripcion(request.descripcion());
        libro.setPortadaUrl(request.portadaUrl());
        libro.setIdEditorial(request.idEditorial());

        if (request.idsCategorias() != null) {
            libro.setCategorias(request.idsCategorias().stream()
                    .filter(id -> id != null && !id.isBlank())
                    .map(this::categoriaConId)
                    .toList());
        }

        if (request.idsAutores() != null) {
            libro.setAutores(request.idsAutores().stream()
                    .filter(id -> id != null && !id.isBlank())
                    .map(this::autorConId)
                    .toList());
        }

        return libro;
    }

    private Categoria categoriaConId(String idCategoria) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(idCategoria);
        return categoria;
    }

    private Autor autorConId(String idAutor) {
        Autor autor = new Autor();
        autor.setIdAutor(idAutor);
        return autor;
    }
}
