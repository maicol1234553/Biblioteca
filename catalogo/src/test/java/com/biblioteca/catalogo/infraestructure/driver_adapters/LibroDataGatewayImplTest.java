package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.AutorNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.CategoriaNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.EditorialNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.domain.model.BusquedaLibro;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.domain.model.Libro;
import com.biblioteca.catalogo.infraestructure.mapper.LibroMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroDataGatewayImplTest {

    @Mock
    private LibroDataJpaRepository libroDataJpaRepository;

    @Mock
    private EditorialDataJpaRepository editorialDataJpaRepository;

    @Mock
    private CategoriaDataJpaRepository categoriaDataJpaRepository;

    @Mock
    private AutorDataJpaRepository autorDataJpaRepository;

    @Mock
    private LibroMapper libroMapper;

    @InjectMocks
    private LibroDataGatewayImpl libroDataGateway;

    @Test
    void guardarLibro_debeConstruirGuardarYRetornarLibro() {
        // Arrange
        Libro libro = mock(Libro.class);
        LibroData libroData = mock(LibroData.class);
        LibroData libroDataGuardado = mock(LibroData.class);
        EditorialData editorialData = mock(EditorialData.class);
        Categoria categoria = mock(Categoria.class);
        CategoriaData categoriaData = mock(CategoriaData.class);
        Autor autor = mock(Autor.class);
        AutorData autorData = mock(AutorData.class);
        Libro libroConvertido = mock(Libro.class);

        when(libro.getIdEditorial()).thenReturn("editorial-1");
        when(libro.getCategorias()).thenReturn(List.of(categoria));
        when(libro.getAutores()).thenReturn(List.of(autor));
        when(categoria.getIdCategoria()).thenReturn("categoria-1");
        when(autor.getIdAutor()).thenReturn("autor-1");

        when(libroMapper.toLibroData(libro)).thenReturn(libroData);
        when(editorialDataJpaRepository.findById("editorial-1"))
                .thenReturn(Optional.of(editorialData));
        when(categoriaDataJpaRepository.findById("categoria-1"))
                .thenReturn(Optional.of(categoriaData));
        when(autorDataJpaRepository.findById("autor-1"))
                .thenReturn(Optional.of(autorData));
        when(libroDataJpaRepository.save(libroData)).thenReturn(libroDataGuardado);
        when(libroMapper.toLibro(libroDataGuardado)).thenReturn(libroConvertido);

        // Act
        Libro resultado = libroDataGateway.guardarLibro(libro);

        // Assert
        assertSame(libroConvertido, resultado);
        verify(libroData).setEditorial(editorialData);
        verify(libroData).setCategorias(Set.of(categoriaData));
        verify(libroData).setAutores(Set.of(autorData));
        verify(libroData).setFechaRegistro(any(LocalDateTime.class));
        verify(libroDataJpaRepository).save(libroData);
    }

    @Test
    void actualizarLibro_debeConstruirGuardarYRetornarLibroActualizado() {
        // Arrange
        Libro libro = mock(Libro.class);
        LibroData libroData = mock(LibroData.class);
        LibroData libroDataGuardado = mock(LibroData.class);
        EditorialData editorialData = mock(EditorialData.class);
        Libro libroActualizado = mock(Libro.class);

        when(libro.getIdEditorial()).thenReturn("editorial-1");
        when(libro.getCategorias()).thenReturn(List.of());
        when(libro.getAutores()).thenReturn(List.of());

        when(libroMapper.toLibroData(libro)).thenReturn(libroData);
        when(editorialDataJpaRepository.findById("editorial-1"))
                .thenReturn(Optional.of(editorialData));
        when(libroDataJpaRepository.save(libroData)).thenReturn(libroDataGuardado);
        when(libroMapper.toLibro(libroDataGuardado)).thenReturn(libroActualizado);

        // Act
        Libro resultado = libroDataGateway.actualizarLibro(libro);

        // Assert
        assertSame(libroActualizado, resultado);
        verify(libroData).setEditorial(editorialData);
        verify(libroData).setCategorias(Set.of());
        verify(libroData).setAutores(Set.of());
        verify(libroData).setFechaRegistro(any(LocalDateTime.class));
        verify(libroDataJpaRepository).save(libroData);
    }

    @Test
    void guardarLibro_cuandoEditorialNoExiste_debeLanzarExcepcion() {
        // Arrange
        Libro libro = mock(Libro.class);

        when(libro.getIdEditorial()).thenReturn("editorial-inexistente");
        when(libroMapper.toLibroData(libro)).thenReturn(mock(LibroData.class));
        when(editorialDataJpaRepository.findById("editorial-inexistente"))
                .thenReturn(Optional.empty());

        // Act
        EditorialNoEncontradaException excepcion = assertThrows(
                EditorialNoEncontradaException.class,
                () -> libroDataGateway.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "Editorial no encontrada con id: editorial-inexistente",
                excepcion.getMessage()
        );
        verify(libroDataJpaRepository, never()).save(any());
    }

    @Test
    void guardarLibro_cuandoCategoriaNoExiste_debeLanzarExcepcion() {
        // Arrange
        Libro libro = mock(Libro.class);
        LibroData libroData = mock(LibroData.class);
        EditorialData editorialData = mock(EditorialData.class);
        Categoria categoria = mock(Categoria.class);

        when(libro.getIdEditorial()).thenReturn("editorial-1");
        when(libro.getCategorias()).thenReturn(List.of(categoria));
        when(categoria.getIdCategoria()).thenReturn("categoria-inexistente");

        when(libroMapper.toLibroData(libro)).thenReturn(libroData);
        when(editorialDataJpaRepository.findById("editorial-1"))
                .thenReturn(Optional.of(editorialData));
        when(categoriaDataJpaRepository.findById("categoria-inexistente"))
                .thenReturn(Optional.empty());

        // Act
        CategoriaNoEncontradaException excepcion = assertThrows(
                CategoriaNoEncontradaException.class,
                () -> libroDataGateway.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "Categoria no encontrada con id: categoria-inexistente",
                excepcion.getMessage()
        );
        verify(libroDataJpaRepository, never()).save(any());
    }

    @Test
    void guardarLibro_cuandoAutorNoExiste_debeLanzarExcepcion() {
        // Arrange
        Libro libro = mock(Libro.class);
        LibroData libroData = mock(LibroData.class);
        EditorialData editorialData = mock(EditorialData.class);
        Autor autor = mock(Autor.class);

        when(libro.getIdEditorial()).thenReturn("editorial-1");
        when(libro.getCategorias()).thenReturn(List.of());
        when(libro.getAutores()).thenReturn(List.of(autor));
        when(autor.getIdAutor()).thenReturn("autor-inexistente");

        when(libroMapper.toLibroData(libro)).thenReturn(libroData);
        when(editorialDataJpaRepository.findById("editorial-1"))
                .thenReturn(Optional.of(editorialData));
        when(autorDataJpaRepository.findById("autor-inexistente"))
                .thenReturn(Optional.empty());

        // Act
        AutorNoEncontradoException excepcion = assertThrows(
                AutorNoEncontradoException.class,
                () -> libroDataGateway.guardarLibro(libro)
        );

        // Assert
        assertEquals(
                "Autor no encontrado con id: autor-inexistente",
                excepcion.getMessage()
        );
        verify(libroDataJpaRepository, never()).save(any());
    }

    @Test
    void buscarLibroPorId_cuandoExiste_debeRetornarLibroMapeado() {
        // Arrange
        String idLibro = "libro-1";
        LibroData libroData = mock(LibroData.class);
        Libro libro = mock(Libro.class);

        when(libroDataJpaRepository.findById(idLibro))
                .thenReturn(Optional.of(libroData));
        when(libroMapper.toLibro(libroData)).thenReturn(libro);

        // Act
        Libro resultado = libroDataGateway.buscarLibroPorId(idLibro);

        // Assert
        assertSame(libro, resultado);
        verify(libroDataJpaRepository).findById(idLibro);
        verify(libroMapper).toLibro(libroData);
    }

    @Test
    void buscarLibroPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idLibro = "libro-inexistente";

        when(libroDataJpaRepository.findById(idLibro))
                .thenReturn(Optional.empty());

        // Act
        Libro resultado = libroDataGateway.buscarLibroPorId(idLibro);

        // Assert
        assertNull(resultado);
        verify(libroDataJpaRepository).findById(idLibro);
        verifyNoInteractions(libroMapper);
    }

    @Test
    void buscarLibroPorIsbn_cuandoIsbnEsBlanco_debeRetornarNullSinConsultarRepositorio() {
        // Arrange
        String isbn = "   ";

        // Act
        Libro resultado = libroDataGateway.buscarLibroPorIsbn(isbn);

        // Assert
        assertNull(resultado);
        verifyNoInteractions(libroDataJpaRepository, libroMapper);
    }

    @Test
    void buscarLibroPorIsbn_cuandoExiste_debeRetornarLibroMapeado() {
        // Arrange
        String isbn = "9781234567890";
        LibroData libroData = mock(LibroData.class);
        Libro libro = mock(Libro.class);

        when(libroDataJpaRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(libroData));
        when(libroMapper.toLibro(libroData)).thenReturn(libro);

        // Act
        Libro resultado = libroDataGateway.buscarLibroPorIsbn(isbn);

        // Assert
        assertSame(libro, resultado);
        verify(libroDataJpaRepository).findByIsbn(isbn);
        verify(libroMapper).toLibro(libroData);
    }

    @Test
    void buscarLibroPorIsbn_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String isbn = "9781234567890";

        when(libroDataJpaRepository.findByIsbn(isbn))
                .thenReturn(Optional.empty());

        // Act
        Libro resultado = libroDataGateway.buscarLibroPorIsbn(isbn);

        // Assert
        assertNull(resultado);
        verify(libroDataJpaRepository).findByIsbn(isbn);
        verifyNoInteractions(libroMapper);
    }

    @Test
    void buscarLibros_debeLimpiarFiltrosAntesDeConsultar() {
        // Arrange
        BusquedaLibro busqueda = new BusquedaLibro(
                "  Borges  ",
                " autor-1 ",
                " ",
                null,
                " es "
        );
        LibroData libroData = mock(LibroData.class);
        Libro libro = mock(Libro.class);

        when(libroDataJpaRepository.buscar("Borges", "autor-1", null, null, "es"))
                .thenReturn(List.of(libroData));
        when(libroMapper.toLibro(libroData)).thenReturn(libro);

        // Act
        List<Libro> resultado = libroDataGateway.buscarLibros(busqueda);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(libro, resultado.get(0));
        verify(libroDataJpaRepository).buscar("Borges", "autor-1", null, null, "es");
        verify(libroMapper).toLibro(libroData);
    }

    @Test
    void buscarLibros_cuandoBusquedaEsNull_debeConsultarSinFiltros() {
        // Arrange
        when(libroDataJpaRepository.buscar(null, null, null, null, null))
                .thenReturn(List.of());

        // Act
        List<Libro> resultado = libroDataGateway.buscarLibros(null);

        // Assert
        assertEquals(List.of(), resultado);
        verify(libroDataJpaRepository).buscar(null, null, null, null, null);
        verifyNoInteractions(libroMapper);
    }

    @Test
    void listarLibros_debeMapearYRetornarLaLista() {
        // Arrange
        LibroData libroData = mock(LibroData.class);
        Libro libro = mock(Libro.class);

        when(libroDataJpaRepository.findAllByOrderByTituloAsc())
                .thenReturn(List.of(libroData));
        when(libroMapper.toLibro(libroData)).thenReturn(libro);

        // Act
        List<Libro> resultado = libroDataGateway.listarLibros();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(libro, resultado.get(0));
        verify(libroDataJpaRepository).findAllByOrderByTituloAsc();
        verify(libroMapper).toLibro(libroData);
    }

    @Test
    void eliminarLibro_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {
        // Arrange
        String idLibro = "libro-1";

        // Act
        libroDataGateway.eliminarLibro(idLibro);

        // Assert
        verify(libroDataJpaRepository).deleteById(idLibro);
        verify(libroDataJpaRepository).flush();
        verifyNoInteractions(libroMapper);
    }

    @Test
    void eliminarLibro_cuandoTieneEjemplaresAsociados_debeLanzarRecursoEnUsoException() {
        // Arrange
        String idLibro = "libro-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(libroDataJpaRepository).flush();

        // Act
        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> libroDataGateway.eliminarLibro(idLibro)
        );

        // Assert
        assertEquals(
                "No se puede eliminar el libro porque tiene ejemplares registrados",
                excepcion.getMessage()
        );
        verify(libroDataJpaRepository).deleteById(idLibro);
        verify(libroDataJpaRepository).flush();
    }
}