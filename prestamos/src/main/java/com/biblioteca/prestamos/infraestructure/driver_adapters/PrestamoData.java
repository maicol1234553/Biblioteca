package com.biblioteca.prestamos.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "prestamo",
        indexes = {
                @Index(name = "idx_prestamo_usuario", columnList = "id_usuario"),
                @Index(name = "idx_prestamo_ejemplar", columnList = "id_ejemplar"),
                @Index(name = "idx_prestamo_estado", columnList = "estado"),
                @Index(name = "idx_prestamo_vencimiento", columnList = "fecha_vencimiento")
        }
)
public class PrestamoData {

    @Id
    @Column(name = "id_prestamo", length = 36, nullable = false)
    private String idPrestamo;

    @Column(name = "id_usuario", length = 36, nullable = false)
    private String idUsuario;

    @Column(name = "id_ejemplar", length = 36, nullable = false)
    private String idEjemplar;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @OneToOne(mappedBy = "prestamo", fetch = FetchType.LAZY)
    private DevolucionData devolucion;

    @OneToMany(mappedBy = "prestamo", fetch = FetchType.LAZY)
    private List<MultaData> multas = new ArrayList<>();

    @OneToMany(mappedBy = "prestamo", fetch = FetchType.LAZY)
    private List<RenovacionData> renovaciones = new ArrayList<>();
}
