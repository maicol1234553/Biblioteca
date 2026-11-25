package com.biblioteca.prestamos.infraestructure.driver_adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "reserva",
        indexes = {
                @Index(name = "idx_reserva_usuario", columnList = "id_usuario"),
                @Index(name = "idx_reserva_libro", columnList = "id_libro"),
                @Index(name = "idx_reserva_estado", columnList = "estado")
        }
)
public class ReservaData {

    @Id
    @Column(name = "id_reserva", length = 36, nullable = false)
    private String idReserva;

    @Column(name = "id_usuario", length = 36, nullable = false)
    private String idUsuario;

    @Column(name = "id_libro", length = 36, nullable = false)
    private String idLibro;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;
}
