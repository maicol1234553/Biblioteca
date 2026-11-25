package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.EjemplarNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.DisponibilidadLibro;
import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.domain.model.EstadoEjemplar;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.domain.model.gateway.EjemplarGateway;
import com.biblioteca.catalogo.domain.model.gateway.LibroGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EjemplarUseCase {

    private final EjemplarGateway ejemplarGateway;
    private final LibroGateway libroGateway;

    public Ejemplar guardarEjemplar(Ejemplar ejemplar) {
        validarEjemplar(ejemplar);

        if (esBlanco(ejemplar.getIdEjemplar())) {
            ejemplar.setIdEjemplar(UUID.randomUUID().toString());
        }

        ejemplar.setEstado(EstadoEjemplar.desde(ejemplar.getEstado()).name());

        if (ejemplar.getFechaAdquisicion() == null) {
            ejemplar.setFechaAdquisicion(LocalDate.now());
        }

        validarCodigoDisponible(ejemplar);

        return ejemplarGateway.guardarEjemplar(ejemplar);
    }

    public Ejemplar actualizarEjemplar(String idEjemplar, Ejemplar ejemplar) {
        Ejemplar existente = buscarEjemplarPorId(idEjemplar);

        if (esBlanco(ejemplar.getIdLibro())) {
            ejemplar.setIdLibro(existente.getIdLibro());
        }
        if (esBlanco(ejemplar.getCodigoInventario())) {
            ejemplar.setCodigoInventario(existente.getCodigoInventario());
        }
        if (ejemplar.getUbicacion() == null) {
            ejemplar.setUbicacion(existente.getUbicacion());
        }
        if (esBlanco(ejemplar.getEstado())) {
            ejemplar.setEstado(existente.getEstado());
        }
        if (ejemplar.getFechaAdquisicion() == null) {
            ejemplar.setFechaAdquisicion(existente.getFechaAdquisicion());
        }
        if (ejemplar.getObservaciones() == null) {
            ejemplar.setObservaciones(existente.getObservaciones());
        }

        ejemplar.setIdEjemplar(idEjemplar);

        return guardarEjemplar(ejemplar);
    }

    public Ejemplar buscarEjemplarPorId(String idEjemplar) {
        Ejemplar ejemplar = ejemplarGateway.buscarEjemplarPorId(idEjemplar);

        if (ejemplar == null) {
            throw new EjemplarNoEncontradoException("Ejemplar no encontrado con id: " + idEjemplar);
        }

        return ejemplar;
    }

    public List<Ejemplar> listarEjemplares() {
        return ejemplarGateway.listarEjemplares();
    }

    public List<Ejemplar> listarEjemplaresPorLibro(String idLibro) {
        validarLibroExiste(idLibro);
        return ejemplarGateway.listarEjemplaresPorLibro(idLibro);
    }

    public List<Ejemplar> listarEjemplaresDisponibles() {
        return ejemplarGateway.listarEjemplaresDisponibles();
    }

    public DisponibilidadLibro consultarDisponibilidad(String idLibro) {
        Libro libro = libroGateway.buscarLibroPorId(idLibro);

        if (libro == null) {
            throw new LibroNoEncontradoException("Libro no encontrado con id: " + idLibro);
        }

        List<Ejemplar> ejemplares = ejemplarGateway.listarEjemplaresPorLibro(idLibro);

        Map<String, Long> conteo = ejemplares.stream()
                .collect(Collectors.groupingBy(
                        ejemplar -> EstadoEjemplar.desde(ejemplar.getEstado()).name(),
                        Collectors.counting()));

        int disponibles = conteo.getOrDefault(EstadoEjemplar.DISPONIBLE.name(), 0L).intValue();
        int prestados = conteo.getOrDefault(EstadoEjemplar.PRESTADO.name(), 0L).intValue();
        int reservados = conteo.getOrDefault(EstadoEjemplar.RESERVADO.name(), 0L).intValue();
        int mantenimiento = conteo.getOrDefault(EstadoEjemplar.MANTENIMIENTO.name(), 0L).intValue();
        int perdidos = conteo.getOrDefault(EstadoEjemplar.PERDIDO.name(), 0L).intValue();

        List<String> ubicaciones = ejemplares.stream()
                .filter(ejemplar -> EstadoEjemplar.DISPONIBLE.name().equals(
                        EstadoEjemplar.desde(ejemplar.getEstado()).name()))
                .map(Ejemplar::getUbicacion)
                .filter(ubicacion -> ubicacion != null && !ubicacion.isBlank())
                .distinct()
                .toList();

        return new DisponibilidadLibro(
                libro.getIdLibro(),
                libro.getTitulo(),
                ejemplares.size(),
                disponibles,
                prestados,
                reservados,
                mantenimiento,
                perdidos,
                disponibles > 0,
                ubicaciones
        );
    }

    public void eliminarEjemplar(String idEjemplar) {
        buscarEjemplarPorId(idEjemplar);
        ejemplarGateway.eliminarEjemplar(idEjemplar);
    }

    private void validarEjemplar(Ejemplar ejemplar) {
        if (ejemplar == null) {
            throw new IllegalArgumentException("El ejemplar es obligatorio");
        }

        if (esBlanco(ejemplar.getIdLibro())) {
            throw new IllegalArgumentException("El libro del ejemplar es obligatorio");
        }

        if (esBlanco(ejemplar.getCodigoInventario())) {
            throw new IllegalArgumentException("El codigo de inventario es obligatorio");
        }

        validarLibroExiste(ejemplar.getIdLibro());
        EstadoEjemplar.desde(ejemplar.getEstado());
    }

    private void validarLibroExiste(String idLibro) {
        if (libroGateway.buscarLibroPorId(idLibro) == null) {
            throw new LibroNoEncontradoException("Libro no encontrado con id: " + idLibro);
        }
    }

    private void validarCodigoDisponible(Ejemplar ejemplar) {
        Ejemplar existente = ejemplarGateway.buscarEjemplarPorCodigoInventario(ejemplar.getCodigoInventario());

        if (existente != null && !Objects.equals(existente.getIdEjemplar(), ejemplar.getIdEjemplar())) {
            throw new RecursoDuplicadoException(
                    "Ya existe un ejemplar con el codigo de inventario: " + ejemplar.getCodigoInventario());
        }
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
