package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.infraestructure.driver_adapters.AutorData;
import org.springframework.stereotype.Component;

@Component
public class AutorMapper {

    public Autor toAutor(AutorData autorData) {
        if (autorData == null) {
            return null;
        }

        return new Autor(
                autorData.getIdAutor(),
                autorData.getNombre(),
                autorData.getApellido(),
                autorData.getNacionalidad(),
                autorData.getFechaNacimiento(),
                autorData.getBiografia()
        );
    }

    public AutorData toAutorData(Autor autor) {
        return new AutorData(
                autor.getIdAutor(),
                autor.getNombre(),
                autor.getApellido(),
                autor.getNacionalidad(),
                autor.getFechaNacimiento(),
                autor.getBiografia()
        );
    }
}
