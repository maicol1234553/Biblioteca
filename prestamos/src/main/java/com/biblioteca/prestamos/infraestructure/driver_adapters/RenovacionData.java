package com.biblioteca.prestamos.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
        name = "renovacion",
        indexes = @Index(name = "idx_renovacion_prestamo", columnList = "id_prestamo")
)
public class RenovacionData {

    @Id
    @Column(name = "id_renovacion", length = 36, nullable = false)
    private String idRenovacion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_prestamo", nullable = false)
    private PrestamoData prestamo;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_anterior", nullable = false)
    private LocalDateTime fechaAnterior;

    @Column(name = "fecha_nueva", nullable = false)
    private LocalDateTime fechaNueva;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;
}
