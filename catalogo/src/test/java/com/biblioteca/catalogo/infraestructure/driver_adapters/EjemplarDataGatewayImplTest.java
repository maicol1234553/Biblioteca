package com.biblioteca.catalogo.infraestructure.driver_adapters;

import com.biblioteca.catalogo.domain.exception.LibroNoEncontradoException;
import com.biblioteca.catalogo.domain.exception.RecursoEnUsoException;
import com.biblioteca.catalogo.domain.model.Ejemplar;
import com.biblioteca.catalogo.domain.model.EstadoEjemplar;
import com.biblioteca.catalogo.infraestructure.mapper.EjemplarMapper;
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
class EjemplarDataGatewayImplTest {

    @Mock
    private EjemplarDataJpaRepository ejemplarDataJpaRepository;

    @Mock
    private LibroDataJpaRepository libroDataJpaRepository;

    @Mock
    private EjemplarMapper ejemplarMapper;

    @InjectMocks
    private EjemplarDataGatewayImpl ejemplarDataGateway;

    @Test
    void guardarEjemplar_debeBuscarLibroMapearGuardarYRetornarEjemplar() {
        // Arrange
        Ejemplar ejemplar = mock(Ejemplar.class);
        LibroData libroData = mock(LibroData.class);
        EjemplarData ejemplarData = mock(EjemplarData.class);
        EjemplarData ejemplarDataGuardado = mock(EjemplarData.class);
        Ejemplar ejemplarConvertido = mock(Ejemplar.class);

        when(ejemplar.getIdLibro()).thenReturn("libro-1");
        when(libroDataJpaRepository.findById("libro-1"))
                .thenReturn(Optional.of(libroData));
        when(ejemplarMapper.toEjemplarData(ejemplar, libroData))
                .thenReturn(ejemplarData);
        when(ejemplarDataJpaRepository.save(ejemplarData))
                .thenReturn(ejemplarDataGuardado);
        when(ejemplarMapper.toEjemplar(ejemplarDataGuardado))
                .thenReturn(ejemplarConvertido);

        // Act
        Ejemplar resultado = ejemplarDataGateway.guardarEjemplar(ejemplar);

        // Assert
        assertSame(ejemplarConvertido, resultado);
        verify(libroDataJpaRepository).findById("libro-1");
        verify(ejemplarMapper).toEjemplarData(ejemplar, libroData);
        verify(ejemplarDataJpaRepository).save(ejemplarData);
        verify(ejemplarMapper).toEjemplar(ejemplarDataGuardado);
    }

    @Test
    void actualizarEjemplar_debeBuscarLibroMapearGuardarYRetornarEjemplarActualizado() {
        // Arrange
        Ejemplar ejemplar = mock(Ejemplar.class);
        LibroData libroData = mock(LibroData.class);
        EjemplarData ejemplarData = mock(EjemplarData.class);
        EjemplarData ejemplarDataGuardado = mock(EjemplarData.class);
        Ejemplar ejemplarActualizado = mock(Ejemplar.class);

        when(ejemplar.getIdLibro()).thenReturn("libro-1");
        when(libroDataJpaRepository.findById("libro-1"))
                .thenReturn(Optional.of(libroData));
        when(ejemplarMapper.toEjemplarData(ejemplar, libroData))
                .thenReturn(ejemplarData);
        when(ejemplarDataJpaRepository.save(ejemplarData))
                .thenReturn(ejemplarDataGuardado);
        when(ejemplarMapper.toEjemplar(ejemplarDataGuardado))
                .thenReturn(ejemplarActualizado);

        // Act
        Ejemplar resultado = ejemplarDataGateway.actualizarEjemplar(ejemplar);

        // Assert
        assertSame(ejemplarActualizado, resultado);
        verify(libroDataJpaRepository).findById("libro-1");
        verify(ejemplarMapper).toEjemplarData(ejemplar, libroData);
        verify(ejemplarDataJpaRepository).save(ejemplarData);
        verify(ejemplarMapper).toEjemplar(ejemplarDataGuardado);
    }

    @Test
    void guardarEjemplar_cuandoElLibroNoExiste_debeLanzarExcepcion() {
        // Arrange
        Ejemplar ejemplar = mock(Ejemplar.class);

        when(ejemplar.getIdLibro()).thenReturn("libro-inexistente");
        when(libroDataJpaRepository.findById("libro-inexistente"))
                .thenReturn(Optional.empty());

        // Act
        LibroNoEncontradoException excepcion = assertThrows(
                LibroNoEncontradoException.class,
                () -> ejemplarDataGateway.guardarEjemplar(ejemplar)
        );

        // Assert
        assertEquals(
                "Libro no encontrado con id: libro-inexistente",
                excepcion.getMessage()
        );
        verify(libroDataJpaRepository).findById("libro-inexistente");
        verifyNoInteractions(ejemplarMapper);
    }

    @Test
    void buscarEjemplarPorId_cuandoExiste_debeRetornarEjemplarMapeado() {
        // Arrange
        String idEjemplar = "ejemplar-1";
        EjemplarData ejemplarData = mock(EjemplarData.class);
        Ejemplar ejemplar = mock(Ejemplar.class);

        when(ejemplarDataJpaRepository.findById(idEjemplar))
                .thenReturn(Optional.of(ejemplarData));
        when(ejemplarMapper.toEjemplar(ejemplarData)).thenReturn(ejemplar);

        // Act
        Ejemplar resultado = ejemplarDataGateway.buscarEjemplarPorId(idEjemplar);

        // Assert
        assertSame(ejemplar, resultado);
        verify(ejemplarDataJpaRepository).findById(idEjemplar);
        verify(ejemplarMapper).toEjemplar(ejemplarData);
    }

    @Test
    void buscarEjemplarPorId_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String idEjemplar = "ejemplar-inexistente";

        when(ejemplarDataJpaRepository.findById(idEjemplar))
                .thenReturn(Optional.empty());

        // Act
        Ejemplar resultado = ejemplarDataGateway.buscarEjemplarPorId(idEjemplar);

        // Assert
        assertNull(resultado);
        verify(ejemplarDataJpaRepository).findById(idEjemplar);
        verifyNoInteractions(ejemplarMapper);
    }

    @Test
    void buscarEjemplarPorCodigoInventario_cuandoExiste_debeRetornarEjemplarMapeado() {
        // Arrange
        String codigo = "INV-001";
        EjemplarData ejemplarData = mock(EjemplarData.class);
        Ejemplar ejemplar = mock(Ejemplar.class);

        when(ejemplarDataJpaRepository.findByCodigoInventario(codigo))
                .thenReturn(Optional.of(ejemplarData));
        when(ejemplarMapper.toEjemplar(ejemplarData)).thenReturn(ejemplar);

        // Act
        Ejemplar resultado =
                ejemplarDataGateway.buscarEjemplarPorCodigoInventario(codigo);

        // Assert
        assertSame(ejemplar, resultado);
        verify(ejemplarDataJpaRepository).findByCodigoInventario(codigo);
        verify(ejemplarMapper).toEjemplar(ejemplarData);
    }

    @Test
    void buscarEjemplarPorCodigoInventario_cuandoNoExiste_debeRetornarNull() {
        // Arrange
        String codigo = "INV-999";

        when(ejemplarDataJpaRepository.findByCodigoInventario(codigo))
                .thenReturn(Optional.empty());

        // Act
        Ejemplar resultado =
                ejemplarDataGateway.buscarEjemplarPorCodigoInventario(codigo);

        // Assert
        assertNull(resultado);
        verify(ejemplarDataJpaRepository).findByCodigoInventario(codigo);
        verifyNoInteractions(ejemplarMapper);
    }

    @Test
    void listarEjemplares_debeMapearYRetornarLaLista() {
        // Arrange
        EjemplarData ejemplarData = mock(EjemplarData.class);
        Ejemplar ejemplar = mock(Ejemplar.class);

        when(ejemplarDataJpaRepository.findAll())
                .thenReturn(List.of(ejemplarData));
        when(ejemplarMapper.toEjemplar(ejemplarData)).thenReturn(ejemplar);

        // Act
        List<Ejemplar> resultado = ejemplarDataGateway.listarEjemplares();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(ejemplar, resultado.get(0));
        verify(ejemplarDataJpaRepository).findAll();
        verify(ejemplarMapper).toEjemplar(ejemplarData);
    }

    @Test
    void listarEjemplaresPorLibro_debeBuscarPorLibroYMapearResultados() {
        // Arrange
        String idLibro = "libro-1";
        EjemplarData ejemplarData = mock(EjemplarData.class);
        Ejemplar ejemplar = mock(Ejemplar.class);

        when(ejemplarDataJpaRepository
                .findByLibro_IdLibroOrderByCodigoInventarioAsc(idLibro))
                .thenReturn(List.of(ejemplarData));
        when(ejemplarMapper.toEjemplar(ejemplarData)).thenReturn(ejemplar);

        // Act
        List<Ejemplar> resultado =
                ejemplarDataGateway.listarEjemplaresPorLibro(idLibro);

        // Assert
        assertEquals(1, resultado.size());
        assertSame(ejemplar, resultado.get(0));
        verify(ejemplarDataJpaRepository)
                .findByLibro_IdLibroOrderByCodigoInventarioAsc(idLibro);
        verify(ejemplarMapper).toEjemplar(ejemplarData);
    }

    @Test
    void listarEjemplaresDisponibles_debeBuscarPorEstadoDisponibleYMapearResultados() {
        // Arrange
        EjemplarData ejemplarData = mock(EjemplarData.class);
        Ejemplar ejemplar = mock(Ejemplar.class);

        when(ejemplarDataJpaRepository.findByEstadoOrderByCodigoInventarioAsc(
                EstadoEjemplar.DISPONIBLE.name()))
                .thenReturn(List.of(ejemplarData));
        when(ejemplarMapper.toEjemplar(ejemplarData)).thenReturn(ejemplar);

        // Act
        List<Ejemplar> resultado =
                ejemplarDataGateway.listarEjemplaresDisponibles();

        // Assert
        assertEquals(1, resultado.size());
        assertSame(ejemplar, resultado.get(0));
        verify(ejemplarDataJpaRepository).findByEstadoOrderByCodigoInventarioAsc(
                EstadoEjemplar.DISPONIBLE.name());
        verify(ejemplarMapper).toEjemplar(ejemplarData);
    }

    @Test
    void eliminarEjemplar_cuandoSeEliminaCorrectamente_debeEjecutarDeleteYFlush() {
        // Arrange
        String idEjemplar = "ejemplar-1";

        // Act
        ejemplarDataGateway.eliminarEjemplar(idEjemplar);

        // Assert
        verify(ejemplarDataJpaRepository).deleteById(idEjemplar);
        verify(ejemplarDataJpaRepository).flush();
        verifyNoInteractions(ejemplarMapper);
    }

    @Test
    void eliminarEjemplar_cuandoTienePrestamosAsociados_debeLanzarRecursoEnUsoException() {
        // Arrange
        String idEjemplar = "ejemplar-1";

        doThrow(new DataIntegrityViolationException("Conflicto de integridad"))
                .when(ejemplarDataJpaRepository).deleteById(idEjemplar);

        // Act
        RecursoEnUsoException excepcion = assertThrows(
                RecursoEnUsoException.class,
                () -> ejemplarDataGateway.eliminarEjemplar(idEjemplar)
        );

        // Assert
        assertEquals(
                "No se puede eliminar el ejemplar porque tiene prestamos asociados",
                excepcion.getMessage()
        );
        verify(ejemplarDataJpaRepository).deleteById(idEjemplar);
    }
}