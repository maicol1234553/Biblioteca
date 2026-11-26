package com.biblioteca.catalogo.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "libro")
public class LibroData {

    @Id
    @Column(name = "id_libro", length = 36, nullable = false)
    private String idLibro;

    @Column(name = "isbn", length = 20, unique = true)
    private String isbn;

    @Column(name = "titulo", length = 255, nullable = false)
    private String titulo;

    @Column(name = "subtitulo", length = 255)
    private String subtitulo;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Column(name = "idioma", length = 60)
    private String idioma;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "portada_url", length = 500)
    private String portadaUrl;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_editorial", nullable = false)
    private EditorialData editorial;






    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "libro_categoria",
            joinColumns = @JoinColumn(name = "id_libro"),
            inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    private Set<CategoriaData> categorias = new LinkedHashSet<>();






    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "id_libro"),
            inverseJoinColumns = @JoinColumn(name = "id_autor")
    )
    private Set<AutorData> autores = new LinkedHashSet<>();

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
