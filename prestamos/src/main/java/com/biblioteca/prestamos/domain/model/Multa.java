package com.biblioteca.prestamos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Multa {

    private String idMulta;
    private String idPrestamo;
    private BigDecimal valor;
    private String motivo;
    private String estado;
    private LocalDateTime fechaGeneracion;
}
