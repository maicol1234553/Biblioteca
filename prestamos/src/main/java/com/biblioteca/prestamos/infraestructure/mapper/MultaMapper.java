package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.infraestructure.driver_adapters.MultaData;
import com.biblioteca.prestamos.infraestructure.driver_adapters.PrestamoData;
import org.springframework.stereotype.Component;

@Component
public class MultaMapper {

    public Multa toMulta(MultaData multaData) {
        if (multaData == null) {
            return null;
        }

        return new Multa(
                multaData.getIdMulta(),
                multaData.getPrestamo() == null ? null : multaData.getPrestamo().getIdPrestamo(),
                multaData.getValor(),
                multaData.getMotivo(),
                multaData.getEstado(),
                multaData.getFechaGeneracion()
        );
    }

    public MultaData toMultaData(Multa multa, PrestamoData prestamoData) {
        return new MultaData(
                multa.getIdMulta(),
                prestamoData,
                multa.getValor(),
                multa.getMotivo(),
                multa.getEstado(),
                multa.getFechaGeneracion()
        );
    }
}
