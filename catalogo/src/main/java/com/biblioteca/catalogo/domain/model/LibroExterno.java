package com.biblioteca.catalogo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;





@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LibroExterno {

    private String fuente;
    private String idExterno;
    private String titulo;
    private String subtitulo;
    private List<String> autores = new ArrayList<>();
    private String editorial;
    private LocalDate fechaPublicacion;
    private String isbn;
    private String idioma;
    private Integer numeroPaginas;
    private String descripcion;
    private String portadaUrl;
}
