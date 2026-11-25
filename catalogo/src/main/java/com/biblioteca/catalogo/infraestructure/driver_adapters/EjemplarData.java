package com.biblioteca.catalogo.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "ejemplar",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ejemplar_codigo_inventario",
                columnNames = "codigo_inventario")
)
public class EjemplarData {

    @Id
    @Column(name = "id_ejemplar", length = 36, nullable = false)
    private String idEjemplar;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_libro", nullable = false)
    private LibroData libro;

    @Column(name = "codigo_inventario", length = 60, nullable = false)
    private String codigoInventario;

    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @Column(name = "estado", length = 40, nullable = false)
    private String estado;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;
}
