package com.biblioteca.notificacion.infraestructure.driver_adapters;

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
        name = "recordatorio",
        indexes = {
                @Index(name = "idx_recordatorio_usuario", columnList = "id_usuario"),
                @Index(name = "idx_recordatorio_prestamo", columnList = "id_prestamo"),
                @Index(name = "idx_recordatorio_estado", columnList = "estado"),
                @Index(name = "idx_recordatorio_fecha", columnList = "fecha_programada")
        }
)
public class RecordatorioData {

    @Id
    @Column(name = "id_recordatorio", length = 36, nullable = false)
    private String idRecordatorio;

    @Column(name = "id_usuario", length = 36, nullable = false)
    private String idUsuario;

    @Column(name = "id_prestamo", length = 36, nullable = false)
    private String idPrestamo;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    @Column(name = "tipo", length = 40, nullable = false)
    private String tipo;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;
}
