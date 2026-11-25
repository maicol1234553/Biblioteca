package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.infraestructure.driver_adapters.EjemplarData;
import com.biblioteca.catalogo.infraestructure.driver_adapters.LibroData;
import org.springframework.stereotype.Component;

@Component
public class EjemplarMapper {

    public Ejemplar toEjemplar(EjemplarData ejemplarData) {
        if (ejemplarData == null) {
            return null;
        }

        return new Ejemplar(
                ejemplarData.getIdEjemplar(),
                ejemplarData.getLibro() == null ? null : ejemplarData.getLibro().getIdLibro(),
                ejemplarData.getLibro() == null ? null : ejemplarData.getLibro().getTitulo(),
                ejemplarData.getCodigoInventario(),
                ejemplarData.getUbicacion(),
                ejemplarData.getEstado(),
                ejemplarData.getFechaAdquisicion(),
                ejemplarData.getObservaciones()
        );
    }

    public EjemplarData toEjemplarData(Ejemplar ejemplar, LibroData libroData) {
        return new EjemplarData(
                ejemplar.getIdEjemplar(),
                libroData,
                ejemplar.getCodigoInventario(),
                ejemplar.getUbicacion(),
                ejemplar.getEstado(),
                ejemplar.getFechaAdquisicion(),
                ejemplar.getObservaciones()
        );
    }
}
