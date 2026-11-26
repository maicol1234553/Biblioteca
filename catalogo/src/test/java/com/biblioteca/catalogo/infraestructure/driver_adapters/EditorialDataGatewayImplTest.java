package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.infraestructure.mapper.EditorialMapper;
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
class EditorialDataGatewayImplTest {

    @Mock
    private EditorialDataJpaRepository editorialDataJpaRepository;

    @Mock
    private EditorialMapper editorialMapper;

    @InjectMocks
    private EditorialDataGatewayImpl editorialDataGateway;

    @Test
    void guardarEditorial_debeMapearGuardarYRetornarEditorial() {
        // Arrange
        Editorial editorial = mock(Editorial.class);
        EditorialData editorialData = mock(EditorialData.class);
        EditorialData editorialDataGuardada = mock(EditorialData.class);
        Editorial editorialConvertida = mock(Editorial.class);

        when(editorialMapper.toEditorialData(editorial)).thenReturn(editorialData);
        when(editorialDataJpaRepository.save(editorialData)).thenReturn(editorialDataGuardada);
        when(editorialMapper.toEditorial(editorialDataGuardada)).thenReturn(editorialConvertida);

        // Act
        Editorial resultado = editorialDataGateway.guardarEditorial(editorial);

        // Assert
        assertSame(editorialConvertida, resultado);
        verify(editorialMapper).toEditorialData(editorial);
        verify(editorialDataJpaRepository).save(editorialData);
        verify(editorialMapper).toEditorial(editorialDataGuardada);
    }

    @Test
    void actualizarEditorial_debeGuardarYRetornarEditorialActualizada() {
        // Arrange
        Editorial editorial = mock(Editorial.class);
        EditorialData editorialData = mock(EditorialData.class);
        EditorialData editorialDataActualizada = mock(EditorialData.class);
        Editorial editorialActualizada = mock(Editorial.class);

        when(editorialMapper.toEditorialData(editorial)).thenReturn(editorialData);
        when(editorialDataJpaRepository.save(editorialData)).thenReturn(editorialDataActualizada);
        when(editorialMapper.toEditorial(editorialDataActualizada))
                .thenReturn(editorialActualizada);

        // Act
        Editorial resultado = editorialDataGateway.actualizarEditorial(editorial);

        // Assert
        assertSame(editorialActualizada, resultado);
        verify(editorialMapper).toEditorialData(editorial);
        verify(editorialDataJpaRepository).save(editorialData);
        verify(editorialMapper).toEditorial(editorialDataActualizada);
    }

    @Test
    void buscarEditorialPorId_cuandoExiste_debeRetornarEditorialMapeada() {
        // Arrange
        String idEditorial = "editorial-1";
        EditorialData editorialData = mock(EditorialData.class);
        Editorial editorial = mock(Editorial.class);

        when(editorialDataJpaRepository.findById(idEditorial))
                .thenReturn(Optional.of(editorialData));
        when(editorialMapper.toEditorial(editorialData)).thenReturn(editorial);

        // Act
        Editorial resultado = editorialDataGateway.buscarEditorialPorId(idEditorial);

        // Assert
        assertSame(editorial, resultado);
        verify(editorialDataJpaRepository).findById(idEditorial);
        verify(editorialMapper).toEditorial(editorialData);
    }

    @Test
    void buscarEditorialPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idEditorial = "editorial-inexistente";

        when(editorialDataJpaRepository.findById(idEditorial))
                .thenReturn(Optional.empty());

        // Act
        Editorial resultado = editorialDataGateway.buscarEditorialPorId(idEditorial);

        // Assert
        assertNull(resultado);
        verify(editorialDataJpaRepository).findById(idEditorial);
        verifyNoInteractions(editorialMapper);
    }

    @Test
    void buscarEditorialPorNombre_cuandoExiste_debeRetornarEditorialMapeada() {
        // Arrange
        String nombre = "Editorial Central";
        EditorialData editorialData = mock(EditorialData.class);
        Editorial editorial = mock(Editorial.class);

        when(editorialDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.of(editorialData));
        when(editorialMapper.toEditorial(editorialData)).thenReturn(editorial);

        // Act
        Editorial resultado = editorialDataGateway.buscarEditorialPorNombre(nombre);

        // Assert
        assertSame(editorial, resultado);
        verify(editorialDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verify(editorialMapper).toEditorial(editorialData);
    }

    @Test
    void buscarEditorialPorNombre_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String nombre = "Editorial inexistente";

        when(editorialDataJpaRepository.findFirstByNombreIgnoreCase(nombre))
                .thenReturn(Optional.empty());

        // Act
        Editorial resultado = editorialDataGateway.buscarEditorialPorNombre(nombre);

        // Assert
        assertNull(resultado);
        verify(editorialDataJpaRepository).findFirstByNombreIgnoreCase(nombre);
        verifyNoInteractions(editorialMapper);
    }

    @Test
    void listarEditoriales_debeMapearYRetornarLaLista() {
        // Arrange
        EditorialData editorialData = mock(EditorialData.class);
        Editorial editorial = mock(Editorial.class);

        when(editorialDataJpaRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(editorialData));
        when(editorialMapper.toEditorial(editorialData)).thenReturn(editorial);

        // Act
        List<Editorial> resultado = editorialDataGateway.listarEditoriales();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(editorial, resultado.get(0));
        verify(editorialDataJpaRepository).findAllByOrderByNombreAsc();
        verify(editorialMapper).toEditorial(editorialData);
    }

    @Test
    void eliminarEditorial_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {
        // Arrange
        String idEditorial = "editorial-1";

        // Act
        editorialDataGateway.eliminarEditorial(idEditorial);

        // Assert
        verify(editorialDataJpaRepository).deleteById(idEditorial);
        verify(editorialDataJpaRepository).flush();
        verifyNoInteractions(editorialMapper);
    }

    @Test
    void eliminarEditorial_cuandoTieneLibrosAsociados_debeLanzarRecursoEnUsoException() {
        // Arrange
        String idEditorial = "editorial-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(editorialDataJpaRepository).deleteById(idEditorial);

        // Act
        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> editorialDataGateway.eliminarEditorial(idEditorial)
        );

        // Assert
        assertEquals(
                "No se puede eliminar la editorial porque tiene libros asociados",
                excepcion.getMessage()
        );
        verify(editorialDataJpaRepository).deleteById(idEditorial);
    }
}