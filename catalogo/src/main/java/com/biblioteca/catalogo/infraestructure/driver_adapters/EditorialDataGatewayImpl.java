package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.domain.model.gateway.EditorialGateway;
import com.biblioteca.catalogo.infraestructure.mapper.EditorialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EditorialDataGatewayImpl implements EditorialGateway {

    private final EditorialDataJpaRepository editorialDataJpaRepository;
    private final EditorialMapper editorialMapper;

    @Override
    public Editorial guardarEditorial(Editorial editorial) {
        return editorialMapper.toEditorial(
                editorialDataJpaRepository.save(editorialMapper.toEditorialData(editorial)));
    }

    @Override
    public Editorial actualizarEditorial(Editorial editorial) {
        return editorialMapper.toEditorial(
                editorialDataJpaRepository.save(editorialMapper.toEditorialData(editorial)));
    }

    @Override
    public Editorial buscarEditorialPorId(String idEditorial) {
        return editorialDataJpaRepository.findById(idEditorial)
                .map(editorialMapper::toEditorial)
                .orElse(null);
    }

    @Override
    public Editorial buscarEditorialPorNombre(String nombre) {
        return editorialDataJpaRepository.findFirstByNombreIgnoreCase(nombre)
                .map(editorialMapper::toEditorial)
                .orElse(null);
    }

    @Override
    public List<Editorial> listarEditoriales() {
        return editorialDataJpaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(editorialMapper::toEditorial)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarEditorial(String idEditorial) {
        try {
            editorialDataJpaRepository.deleteById(idEditorial);
            editorialDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar la editorial porque tiene libros asociados");
        }
    }
}
