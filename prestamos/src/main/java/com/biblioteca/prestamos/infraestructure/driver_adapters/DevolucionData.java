package com.biblioteca.prestamos.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "devolucion",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_devolucion_prestamo",
                columnNames = "id_prestamo")
)
public class DevolucionData {

    @Id
    @Column(name = "id_devolucion", length = 36, nullable = false)
    private String idDevolucion;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_prestamo", nullable = false)
    private PrestamoData prestamo;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDateTime fechaDevolucion;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "estado_ejemplar", length = 30, nullable = false)
    private String estadoEjemplar;
}
