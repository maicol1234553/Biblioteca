package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.gateway.CategoriaGateway;
import com.biblioteca.catalogo.infraestructure.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoriaDataGatewayImpl implements CategoriaGateway {

    private final CategoriaDataJpaRepository categoriaDataJpaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    public Categoria guardarCategoria(Categoria categoria) {
        return categoriaMapper.toCategoria(
                categoriaDataJpaRepository.save(categoriaMapper.toCategoriaData(categoria)));
    }

    @Override
    public Categoria actualizarCategoria(Categoria categoria) {
        return categoriaMapper.toCategoria(
                categoriaDataJpaRepository.save(categoriaMapper.toCategoriaData(categoria)));
    }

    @Override
    public Categoria buscarCategoriaPorId(String idCategoria) {
        return categoriaDataJpaRepository.findById(idCategoria)
                .map(categoriaMapper::toCategoria)
                .orElse(null);
    }

    @Override
    public Categoria buscarCategoriaPorNombre(String nombre) {
        return categoriaDataJpaRepository.findFirstByNombreIgnoreCase(nombre)
                .map(categoriaMapper::toCategoria)
                .orElse(null);
    }

    @Override
    public List<Categoria> listarCategorias() {
        return categoriaDataJpaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(categoriaMapper::toCategoria)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarCategoria(String idCategoria) {
        try {
            categoriaDataJpaRepository.deleteById(idCategoria);
            categoriaDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar la categoria porque tiene libros asociados");
        }
    }
}
