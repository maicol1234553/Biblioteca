package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Renovacion;
import com.biblioteca.prestamos.infraestructure.driver_adapters.PrestamoData;
import com.biblioteca.prestamos.infraestructure.driver_adapters.RenovacionData;
import org.springframework.stereotype.Component;

@Component
public class RenovacionMapper {

    public Renovacion toRenovacion(RenovacionData renovacionData) {
        if (renovacionData == null) {
            return null;
        }

        return new Renovacion(
                renovacionData.getIdRenovacion(),
                renovacionData.getPrestamo() == null
                        ? null
                        : renovacionData.getPrestamo().getIdPrestamo(),
                renovacionData.getFechaSolicitud(),
                renovacionData.getFechaAnterior(),
                renovacionData.getFechaNueva(),
                renovacionData.getEstado()
        );
    }

    public RenovacionData toRenovacionData(Renovacion renovacion, PrestamoData prestamoData) {
        return new RenovacionData(
                renovacion.getIdRenovacion(),
                prestamoData,
                renovacion.getFechaSolicitud(),
                renovacion.getFechaAnterior(),
                renovacion.getFechaNueva(),
                renovacion.getEstado()
        );
    }
}
