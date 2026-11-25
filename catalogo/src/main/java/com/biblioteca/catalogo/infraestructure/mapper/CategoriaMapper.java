package com.biblioteca.catalogo.infraestructure.mapper;

import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.infraestructure.driver_adapters.CategoriaData;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toCategoria(CategoriaData categoriaData) {
        if (categoriaData == null) {
            return null;
        }

        return new Categoria(
                categoriaData.getIdCategoria(),
                categoriaData.getNombre(),
                categoriaData.getDescripcion()
        );
    }

    public CategoriaData toCategoriaData(Categoria categoria) {
        return new CategoriaData(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
