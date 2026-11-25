package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.gateway.AutorGateway;
import com.biblioteca.catalogo.infraestructure.mapper.AutorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AutorDataGatewayImpl implements AutorGateway {

    private final AutorDataJpaRepository autorDataJpaRepository;
    private final AutorMapper autorMapper;

    @Override
    public Autor guardarAutor(Autor autor) {
        return autorMapper.toAutor(autorDataJpaRepository.save(autorMapper.toAutorData(autor)));
    }

    @Override
    public Autor actualizarAutor(Autor autor) {
        return autorMapper.toAutor(autorDataJpaRepository.save(autorMapper.toAutorData(autor)));
    }

    @Override
    public Autor buscarAutorPorId(String idAutor) {
        return autorDataJpaRepository.findById(idAutor)
                .map(autorMapper::toAutor)
                .orElse(null);
    }

    @Override
    public Autor buscarAutorPorNombre(String nombre) {
        return autorDataJpaRepository.findFirstByNombreIgnoreCase(nombre)
                .map(autorMapper::toAutor)
                .orElse(null);
    }

    @Override
    public List<Autor> listarAutores() {
        return autorDataJpaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(autorMapper::toAutor)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarAutor(String idAutor) {
        try {
            autorDataJpaRepository.deleteById(idAutor);
            autorDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar el autor porque tiene libros asociados");
        }
    }
}
