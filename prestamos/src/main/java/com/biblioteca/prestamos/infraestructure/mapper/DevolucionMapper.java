package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Devolucion;
import com.biblioteca.prestamos.infraestructure.driver_adapters.DevolucionData;
import com.biblioteca.prestamos.infraestructure.driver_adapters.PrestamoData;
import org.springframework.stereotype.Component;

@Component
public class DevolucionMapper {

    public Devolucion toDevolucion(DevolucionData devolucionData) {
        if (devolucionData == null) {
            return null;
        }

        return new Devolucion(
                devolucionData.getIdDevolucion(),
                devolucionData.getPrestamo() == null
                        ? null
                        : devolucionData.getPrestamo().getIdPrestamo(),
                devolucionData.getFechaDevolucion(),
                devolucionData.getObservaciones(),
                devolucionData.getEstadoEjemplar()
        );
    }

    public DevolucionData toDevolucionData(Devolucion devolucion, PrestamoData prestamoData) {
        return new DevolucionData(
                devolucion.getIdDevolucion(),
                prestamoData,
                devolucion.getFechaDevolucion(),
                devolucion.getObservaciones(),
                devolucion.getEstadoEjemplar()
        );
    }
}
