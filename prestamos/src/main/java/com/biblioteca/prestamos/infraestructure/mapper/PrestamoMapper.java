package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Prestamo;
import com.biblioteca.prestamos.infraestructure.driver_adapters.PrestamoData;
import org.springframework.stereotype.Component;

@Component
public class PrestamoMapper {

    public Prestamo toPrestamo(PrestamoData prestamoData) {
        if (prestamoData == null) {
            return null;
        }

        return new Prestamo(
                prestamoData.getIdPrestamo(),
                prestamoData.getIdUsuario(),
                prestamoData.getIdEjemplar(),
                prestamoData.getFechaPrestamo(),
                prestamoData.getFechaVencimiento(),
                prestamoData.getFechaDevolucion(),
                prestamoData.getEstado()
        );
    }

    public PrestamoData toPrestamoData(Prestamo prestamo) {
        PrestamoData prestamoData = new PrestamoData();

        prestamoData.setIdPrestamo(prestamo.getIdPrestamo());
        prestamoData.setIdUsuario(prestamo.getIdUsuario());
        prestamoData.setIdEjemplar(prestamo.getIdEjemplar());
        prestamoData.setFechaPrestamo(prestamo.getFechaPrestamo());
        prestamoData.setFechaVencimiento(prestamo.getFechaVencimiento());
        prestamoData.setFechaDevolucion(prestamo.getFechaDevolucion());
        prestamoData.setEstado(prestamo.getEstado());

        return prestamoData;
    }
}
