package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Autor;
import com.biblioteca.catalogo.infraestructure.mapper.AutorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutorDataGatewayImplTest {

    @Mock
    private AutorDataJpaRepository autorDataJpaRepository;

    @Mock
    private AutorMapper autorMapper;

    @InjectMocks
    private AutorDataGatewayImpl autorDataGateway;

    @Test
    void guardarAutor_debeMapearGuardarYRetornarAutor() {

        Autor autor = mock(Autor.class);
        AutorData autorData = mock(AutorData.class);
        AutorData autorDataGuardado = mock(AutorData.class);
        Autor autorConvertido = mock(Autor.class);

        when(autorMapper.toAutorData(autor)).thenReturn(autorData);
        when(autorDataJpaRepository.save(autorData)).thenReturn(autorDataGuardado);
        when(autorMapper.toAutor(autorDataGuardado)).thenReturn(autorConvertido);


        Autor resultado = autorDataGateway.guardarAutor(autor);


        assertSame(autorConvertido, resultado);
        verify(autorMapper).toAutorData(autor);
        verify(autorDataJpaRepository).save(autorData);
        verify(autorMapper).toAutor(autorDataGuardado);
    }

    @Test
    void actualizarAutor_debeGuardarYRetornarAutorActualizado() {

        Autor autor = mock(Autor.class);
        AutorData autorData = mock(AutorData.class);
        AutorData autorDataActualizado = mock(AutorData.class);
        Autor autorActualizado = mock(Autor.class);

        when(autorMapper.toAutorData(autor)).thenReturn(autorData);
        when(autorDataJpaRepository.save(autorData)).thenReturn(autorDataActualizado);
        when(autorMapper.toAutor(autorDataActualizado)).thenReturn(autorActualizado);


        Autor resultado = autorDataGateway.actualizarAutor(autor);


        assertSame(autorActualizado, resultado);
        verify(autorMapper).toAutorData(autor);
        verify(autorDataJpaRepository).save(autorData);
        verify(autorMapper).toAutor(autorDataActualizado);
    }

    @Test
    void buscarAutorPorId_cuandoExiste_debeRetornarAutorMapeado() {

        String idAutor = "autor-1";
        AutorData autorData = mock(AutorData.class);
        Autor autor = mock(Autor.class);

        when(autorDataJpaRepository.findById(idAutor))
                .thenReturn(Optional.of(autorData));
        when(autorMapper.toAutor(autorData)).thenReturn(autor);


        Autor resultado = autorDataGateway.buscarAutorPorId(idAutor);


        assertSame(autor, resultado);
        verify(autorDataJpaRepository).findById(idAutor);
        verify(autorMapper).toAutor(autorData);
    }

    @Test
    void buscarAutorPorId_cuandoNoExiste_debeRetornarNull() {

        String idAutor = "autor-inexistente";

        when(autorDataJpaRepository.findById(idAutor))
                .thenReturn(Optional.empty());


        Autor resultado = autorDataGateway.buscarAutorPorId(idAutor);


        assertNull(resultado);
        verify(autorDataJpaRepository).findById(idAutor);
        verifyNoInteractions(autorMapper);
    }

    @Test
    void buscarAutorPorNombre_cuandoExiste_debeRetornarAutorMapeado() {

        String nombre = "Gabriel García Márquez";
        AutorData autorData = mock(AutorData.class);
        Autor autor = mock(Autor.class);

        when(autorDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.of(autorData));
        when(autorMapper.toAutor(autorData)).thenReturn(autor);


        Autor resultado = autorDataGateway.buscarAutorPorNombre(nombre);


        assertSame(autor, resultado);
        verify(autorDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verify(autorMapper).toAutor(autorData);
    }

    @Test
    void buscarAutorPorNombre_cuandoNoExiste_debeRetornarNull() {

        String nombre = "Autor inexistente";

        when(autorDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.empty());


        Autor resultado = autorDataGateway.buscarAutorPorNombre(nombre);


        assertNull(resultado);
        verify(autorDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verifyNoInteractions(autorMapper);
    }

    @Test
    void listarAutores_debeMapearYRetornarLaLista() {

        AutorData autorData = mock(AutorData.class);
        Autor autor = mock(Autor.class);

        when(autorDataJpaRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(autorData));
        when(autorMapper.toAutor(autorData)).thenReturn(autor);


        List<Autor> resultado = autorDataGateway.listarAutores();


        assertEquals(1, resultado.size());
        assertSame(autor, resultado.get(0));
        verify(autorDataJpaRepository).findAllByOrderByNombreAsc();
        verify(autorMapper).toAutor(autorData);
    }

    @Test
    void eliminarAutor_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {

        String idAutor = "autor-1";


        autorDataGateway.eliminarAutor(idAutor);


        verify(autorDataJpaRepository).deleteById(idAutor);
        verify(autorDataJpaRepository).flush();
        verifyNoInteractions(autorMapper);
    }

    @Test
    void eliminarAutor_cuandoTieneLibrosAsociados_debeLanzarRecursoEnUsoException() {

        String idAutor = "autor-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(autorDataJpaRepository).deleteById(idAutor);


        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> autorDataGateway.eliminarAutor(idAutor)
        );


        assertEquals(
                "No se puede eliminar el autor porque tiene libros asociados",
                excepcion.getMessage()
        );
        verify(autorDataJpaRepository).deleteById(idAutor);
    }
}