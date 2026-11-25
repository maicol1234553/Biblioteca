package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.domain.model.EstadoEjemplar;
import com.biblioteca.catalogo.domain.model.gateway.EjemplarGateway;
import com.biblioteca.catalogo.infraestructure.mapper.EjemplarMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EjemplarDataGatewayImpl implements EjemplarGateway {

    private final EjemplarDataJpaRepository ejemplarDataJpaRepository;
    private final LibroDataJpaRepository libroDataJpaRepository;
    private final EjemplarMapper ejemplarMapper;

    @Override
    @Transactional
    public Ejemplar guardarEjemplar(Ejemplar ejemplar) {
        return ejemplarMapper.toEjemplar(
                ejemplarDataJpaRepository.save(construirEjemplarData(ejemplar)));
    }

    @Override
    @Transactional
    public Ejemplar actualizarEjemplar(Ejemplar ejemplar) {
        return ejemplarMapper.toEjemplar(
                ejemplarDataJpaRepository.save(construirEjemplarData(ejemplar)));
    }

    @Override
    public Ejemplar buscarEjemplarPorId(String idEjemplar) {
        return ejemplarDataJpaRepository.findById(idEjemplar)
                .map(ejemplarMapper::toEjemplar)
                .orElse(null);
    }

    @Override
    public Ejemplar buscarEjemplarPorCodigoInventario(String codigoInventario) {
        return ejemplarDataJpaRepository.findByCodigoInventario(codigoInventario)
                .map(ejemplarMapper::toEjemplar)
                .orElse(null);
    }

    @Override
    public List<Ejemplar> listarEjemplares() {
        return ejemplarDataJpaRepository.findAll()
                .stream()
                .map(ejemplarMapper::toEjemplar)
                .toList();
    }

    @Override
    public List<Ejemplar> listarEjemplaresPorLibro(String idLibro) {
        return ejemplarDataJpaRepository.findByLibro_IdLibroOrderByCodigoInventarioAsc(idLibro)
                .stream()
                .map(ejemplarMapper::toEjemplar)
                .toList();
    }

    @Override
    public List<Ejemplar> listarEjemplaresDisponibles() {
        return ejemplarDataJpaRepository.findByEstadoOrderByCodigoInventarioAsc(
                        EstadoEjemplar.DISPONIBLE.name())
                .stream()
                .map(ejemplarMapper::toEjemplar)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarEjemplar(String idEjemplar) {
        try {
            ejemplarDataJpaRepository.deleteById(idEjemplar);
            ejemplarDataJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RecursoEnUsoException(
                    "No se puede eliminar el ejemplar porque tiene prestamos asociados");
        }
    }

    private EjemplarData construirEjemplarData(Ejemplar ejemplar) {
        LibroData libro = libroDataJpaRepository.findById(ejemplar.getIdLibro())
                .orElseThrow(() -> new LibroNoEncontradoException(
                        "Libro no encontrado con id: " + ejemplar.getIdLibro()));

        return ejemplarMapper.toEjemplarData(ejemplar, libro);
    }
}
