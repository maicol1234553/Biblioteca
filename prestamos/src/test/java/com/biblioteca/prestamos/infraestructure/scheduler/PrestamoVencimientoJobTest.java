package com.biblioteca.prestamos.infraestructure.scheduler;

import com.biblioteca.prestamos.domain.usecase.PrestamoUseCase;
import com.biblioteca.prestamos.domain.usecase.ReservaUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestamoVencimientoJobTest {

    @Mock
    private PrestamoUseCase prestamoUseCase;

    @Mock
    private ReservaUseCase reservaUseCase;

    @InjectMocks
    private PrestamoVencimientoJob job;

    @Test
    void ejecutar_debeProcesarPrestamosYReservas() {

        when(prestamoUseCase.procesarPrestamosVencidos())
                .thenReturn(Collections.nCopies(2, null));
        when(reservaUseCase.procesarReservasExpiradas())
                .thenReturn(Collections.nCopies(3, null));


        job.ejecutar();


        verify(prestamoUseCase).procesarPrestamosVencidos();
        verify(reservaUseCase).procesarReservasExpiradas();
    }

    @Test
    void ejecutar_cuandoNoHayElementosPendientes_debeProcesarAmbosCasosDeUso() {

        when(prestamoUseCase.procesarPrestamosVencidos()).thenReturn(List.of());
        when(reservaUseCase.procesarReservasExpiradas()).thenReturn(List.of());


        job.ejecutar();


        verify(prestamoUseCase).procesarPrestamosVencidos();
        verify(reservaUseCase).procesarReservasExpiradas();
    }

    @Test
    void ejecutar_cuandoFallaElProcesamientoDePrestamos_debeCapturarLaExcepcion() {

        when(prestamoUseCase.procesarPrestamosVencidos())
                .thenThrow(new IllegalStateException("Error al procesar préstamos"));


        assertDoesNotThrow(() -> job.ejecutar());

        verify(prestamoUseCase).procesarPrestamosVencidos();
        verifyNoInteractions(reservaUseCase);
    }

    @Test
    void ejecutar_cuandoFallaElProcesamientoDeReservas_debeCapturarLaExcepcion() {

        when(prestamoUseCase.procesarPrestamosVencidos()).thenReturn(List.of());
        when(reservaUseCase.procesarReservasExpiradas())
                .thenThrow(new IllegalStateException("Error al procesar reservas"));


        assertDoesNotThrow(() -> job.ejecutar());

        verify(prestamoUseCase).procesarPrestamosVencidos();
        verify(reservaUseCase).procesarReservasExpiradas();
    }
}