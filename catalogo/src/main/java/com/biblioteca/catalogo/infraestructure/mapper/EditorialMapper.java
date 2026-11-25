package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.infraestructure.driver_adapters.EditorialData;
import org.springframework.stereotype.Component;

@Component
public class EditorialMapper {

    public Editorial toEditorial(EditorialData editorialData) {
        if (editorialData == null) {
            return null;
        }

        return new Editorial(
                editorialData.getIdEditorial(),
                editorialData.getNombre(),
                editorialData.getPais(),
                editorialData.getSitioWeb(),
                editorialData.getCorreoContacto()
        );
    }

    public EditorialData toEditorialData(Editorial editorial) {
        return new EditorialData(
                editorial.getIdEditorial(),
                editorial.getNombre(),
                editorial.getPais(),
                editorial.getSitioWeb(),
                editorial.getCorreoContacto()
        );
    }
}
