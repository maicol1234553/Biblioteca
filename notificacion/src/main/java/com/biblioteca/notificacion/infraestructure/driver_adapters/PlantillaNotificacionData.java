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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "plantilla_notificacion",
        indexes = {
                @Index(name = "idx_plantilla_tipo", columnList = "tipo"),
                @Index(name = "idx_plantilla_estado", columnList = "estado")
        }
)
public class PlantillaNotificacionData {

    @Id
    @Column(name = "id_plantilla", length = 36, nullable = false)
    private String idPlantilla;

    @Column(name = "nombre", length = 150, nullable = false, unique = true)
    private String nombre;

    @Column(name = "asunto", length = 255, nullable = false)
    private String asunto;

    @Column(name = "contenido", columnDefinition = "text", nullable = false)
    private String contenido;

    @Column(name = "tipo", length = 60, nullable = false)
    private String tipo;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;
}
