package com.biblioteca.prestamos.infraestructure.mapper;

import com.biblioteca.prestamos.domain.model.Reserva;
import com.biblioteca.prestamos.infraestructure.driver_adapters.ReservaData;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public Reserva toReserva(ReservaData reservaData) {
        if (reservaData == null) {
            return null;
        }

        return new Reserva(
                reservaData.getIdReserva(),
                reservaData.getIdUsuario(),
                reservaData.getIdLibro(),
                reservaData.getFechaReserva(),
                reservaData.getFechaExpiracion(),
                reservaData.getEstado()
        );
    }

    public ReservaData toReservaData(Reserva reserva) {
        return new ReservaData(
                reserva.getIdReserva(),
                reserva.getIdUsuario(),
                reserva.getIdLibro(),
                reserva.getFechaReserva(),
                reserva.getFechaExpiracion(),
                reserva.getEstado()
        );
    }
}
