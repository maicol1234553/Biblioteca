package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Categoria;
import com.biblioteca.catalogo.infraestructure.mapper.CategoriaMapper;
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
class CategoriaDataGatewayImplTest {

    @Mock
    private CategoriaDataJpaRepository categoriaDataJpaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaDataGatewayImpl categoriaDataGateway;

    @Test
    void guardarCategoria_debeMapearGuardarYRetornarCategoria() {

        Categoria categoria = mock(Categoria.class);
        CategoriaData categoriaData = mock(CategoriaData.class);
        CategoriaData categoriaDataGuardada = mock(CategoriaData.class);
        Categoria categoriaConvertida = mock(Categoria.class);

        when(categoriaMapper.toCategoriaData(categoria)).thenReturn(categoriaData);
        when(categoriaDataJpaRepository.save(categoriaData)).thenReturn(categoriaDataGuardada);
        when(categoriaMapper.toCategoria(categoriaDataGuardada)).thenReturn(categoriaConvertida);


        Categoria resultado = categoriaDataGateway.guardarCategoria(categoria);


        assertSame(categoriaConvertida, resultado);
        verify(categoriaMapper).toCategoriaData(categoria);
        verify(categoriaDataJpaRepository).save(categoriaData);
        verify(categoriaMapper).toCategoria(categoriaDataGuardada);
    }

    @Test
    void actualizarCategoria_debeGuardarYRetornarCategoriaActualizada() {

        Categoria categoria = mock(Categoria.class);
        CategoriaData categoriaData = mock(CategoriaData.class);
        CategoriaData categoriaDataActualizada = mock(CategoriaData.class);
        Categoria categoriaActualizada = mock(Categoria.class);

        when(categoriaMapper.toCategoriaData(categoria)).thenReturn(categoriaData);
        when(categoriaDataJpaRepository.save(categoriaData)).thenReturn(categoriaDataActualizada);
        when(categoriaMapper.toCategoria(categoriaDataActualizada))
                .thenReturn(categoriaActualizada);


        Categoria resultado = categoriaDataGateway.actualizarCategoria(categoria);


        assertSame(categoriaActualizada, resultado);
        verify(categoriaMapper).toCategoriaData(categoria);
        verify(categoriaDataJpaRepository).save(categoriaData);
        verify(categoriaMapper).toCategoria(categoriaDataActualizada);
    }

    @Test
    void buscarCategoriaPorId_cuandoExiste_debeRetornarCategoriaMapeada() {

        String idCategoria = "categoria-1";
        CategoriaData categoriaData = mock(CategoriaData.class);
        Categoria categoria = mock(Categoria.class);

        when(categoriaDataJpaRepository.findById(idCategoria))
                .thenReturn(Optional.of(categoriaData));
        when(categoriaMapper.toCategoria(categoriaData)).thenReturn(categoria);


        Categoria resultado = categoriaDataGateway.buscarCategoriaPorId(idCategoria);


        assertSame(categoria, resultado);
        verify(categoriaDataJpaRepository).findById(idCategoria);
        verify(categoriaMapper).toCategoria(categoriaData);
    }

    @Test
    void buscarCategoriaPorId_cuandoNoExiste_debeRetornarNull() {

        String idCategoria = "categoria-inexistente";

        when(categoriaDataJpaRepository.findById(idCategoria))
                .thenReturn(Optional.empty());


        Categoria resultado = categoriaDataGateway.buscarCategoriaPorId(idCategoria);


        assertNull(resultado);
        verify(categoriaDataJpaRepository).findById(idCategoria);
        verifyNoInteractions(categoriaMapper);
    }

    @Test
    void buscarCategoriaPorNombre_cuandoExiste_debeRetornarCategoriaMapeada() {

        String nombre = "Novela";
        CategoriaData categoriaData = mock(CategoriaData.class);
        Categoria categoria = mock(Categoria.class);

        when(categoriaDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.of(categoriaData));
        when(categoriaMapper.toCategoria(categoriaData)).thenReturn(categoria);


        Categoria resultado = categoriaDataGateway.buscarCategoriaPorNombre(nombre);


        assertSame(categoria, resultado);
        verify(categoriaDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verify(categoriaMapper).toCategoria(categoriaData);
    }

    @Test
    void buscarCategoriaPorNombre_cuandoNoExiste_debeRetornarNull() {

        String nombre = "Categoria inexistente";

        when(categoriaDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.empty());


        Categoria resultado = categoriaDataGateway.buscarCategoriaPorNombre(nombre);


        assertNull(resultado);
        verify(categoriaDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verifyNoInteractions(categoriaMapper);
    }

    @Test
    void listarCategorias_debeMapearYRetornarLaLista() {

        CategoriaData categoriaData = mock(CategoriaData.class);
        Categoria categoria = mock(Categoria.class);

        when(categoriaDataJpaRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(categoriaData));
        when(categoriaMapper.toCategoria(categoriaData)).thenReturn(categoria);


        List<Categoria> resultado = categoriaDataGateway.listarCategorias();


        assertEquals(1, resultado.size());
        assertSame(categoria, resultado.get(0));
        verify(categoriaDataJpaRepository).findAllByOrderByNombreAsc();
        verify(categoriaMapper).toCategoria(categoriaData);
    }

    @Test
    void eliminarCategoria_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {

        String idCategoria = "categoria-1";


        categoriaDataGateway.eliminarCategoria(idCategoria);


        verify(categoriaDataJpaRepository).deleteById(idCategoria);
        verify(categoriaDataJpaRepository).flush();
        verifyNoInteractions(categoriaMapper);
    }

    @Test
    void eliminarCategoria_cuandoTieneLibrosAsociados_debeLanzarRecursoEnUsoException() {

        String idCategoria = "categoria-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(categoriaDataJpaRepository).deleteById(idCategoria);


        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> categoriaDataGateway.eliminarCategoria(idCategoria)
        );


        assertEquals(
                "No se puede eliminar la categoria porque tiene libros asociados",
                excepcion.getMessage()
        );
        verify(categoriaDataJpaRepository).deleteById(idCategoria);
    }
}