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
        name = "notificacion",
        indexes = {
                @Index(name = "idx_notificacion_usuario", columnList = "id_usuario"),
                @Index(name = "idx_notificacion_estado", columnList = "estado"),
                @Index(name = "idx_notificacion_tipo", columnList = "tipo")
        }
)
public class NotificacionData {

    @Id
    @Column(name = "id_notificacion", length = 36, nullable = false)
    private String idNotificacion;

    @Column(name = "id_usuario", length = 36, nullable = false)
    private String idUsuario;

    @Column(name = "destinatario", length = 320)
    private String destinatario;

    @Column(name = "asunto", length = 255, nullable = false)
    private String asunto;

    @Column(name = "mensaje", columnDefinition = "text", nullable = false)
    private String mensaje;

    @Column(name = "tipo", length = 60, nullable = false)
    private String tipo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    /** CORREO o SMS. Nulo en filas creadas antes de soportar SMS. */
    @Column(name = "canal", length = 20)
    private String canal;
}
