package com.biblioteca.notificacion.infraestructure.driver_adapters;

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
        name = "historial_envio",
        indexes = @Index(name = "idx_historial_notificacion", columnList = "id_notificacion")
)
public class HistorialEnvioData {

    @Id
    @Column(name = "id_historial", length = 36, nullable = false)
    private String idHistorial;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_notificacion", nullable = false)
    private NotificacionData notificacion;

    @Column(name = "fecha_intento", nullable = false)
    private LocalDateTime fechaIntento;

    @Column(name = "resultado", length = 30, nullable = false)
    private String resultado;

    @Column(name = "mensaje_error", columnDefinition = "text")
    private String mensajeError;
}
