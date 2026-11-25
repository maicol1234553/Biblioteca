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
@Table(name = "editorial")
public class EditorialData {

    @Id
    @Column(name = "id_editorial", length = 36, nullable = false)
    private String idEditorial;

    @Column(name = "nombre", length = 255, nullable = false, unique = true)
    private String nombre;

    @Column(name = "pais", length = 120)
    private String pais;

    @Column(name = "sitio_web", length = 500)
    private String sitioWeb;

    @Column(name = "correo_contacto", length = 255)
    private String correoContacto;
}
