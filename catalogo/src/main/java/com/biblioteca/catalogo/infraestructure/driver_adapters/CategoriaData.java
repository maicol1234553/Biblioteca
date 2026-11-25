package com.biblioteca.catalogo.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categoria")
public class CategoriaData {

    @Id
    @Column(name = "id_categoria", length = 36, nullable = false)
    private String idCategoria;

    @Column(name = "nombre", length = 255, nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;
}
