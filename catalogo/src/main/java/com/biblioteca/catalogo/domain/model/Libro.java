package com.biblioteca.catalogo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Libro {

    private String idLibro;
    private String isbn;
    private String titulo;
    private String subtitulo;
    private LocalDate fechaPublicacion;
    private String idioma;
    private Integer numeroPaginas;
    private String descripcion;
    private String portadaUrl;

    private String idEditorial;
    private String nombreEditorial;
    private List<Categoria> categorias = new ArrayList<>();
    private List<Autor> autores = new ArrayList<>();

    private LocalDateTime fechaRegistro;
}
