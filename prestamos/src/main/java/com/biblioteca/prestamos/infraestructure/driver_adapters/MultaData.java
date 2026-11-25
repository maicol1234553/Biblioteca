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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "multa",
        indexes = {
                @Index(name = "idx_multa_prestamo", columnList = "id_prestamo"),
                @Index(name = "idx_multa_estado", columnList = "estado")
        }
)
public class MultaData {

    @Id
    @Column(name = "id_multa", length = 36, nullable = false)
    private String idMulta;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_prestamo", nullable = false)
    private PrestamoData prestamo;

    @Column(name = "valor", precision = 12, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(name = "motivo", length = 500, nullable = false)
    private String motivo;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;
}
