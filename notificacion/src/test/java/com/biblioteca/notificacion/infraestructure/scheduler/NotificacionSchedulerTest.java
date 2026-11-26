package com.biblioteca.notificacion.infraestructure.scheduler;

import com.biblioteca.notificacion.domain.usecase.NotificacionUseCase;
import com.biblioteca.notificacion.domain.usecase.RecordatorioUseCase;
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
class NotificacionSchedulerTest {

    @Mock
    private NotificacionUseCase notificacionUseCase;

    @Mock
    private RecordatorioUseCase recordatorioUseCase;

    @InjectMocks
    private NotificacionScheduler scheduler;

    @Test
    void procesarPendientes_cuandoHayResultados_debeInvocarAmbosUseCases() {

        when(notificacionUseCase.reintentarFallidas())
                .thenReturn(Collections.nCopies(2, null));
        when(recordatorioUseCase.procesarRecordatoriosPendientes())
                .thenReturn(Collections.nCopies(3, null));


        scheduler.procesarPendientes();


        verify(notificacionUseCase).reintentarFallidas();
        verify(recordatorioUseCase).procesarRecordatoriosPendientes();
    }

    @Test
    void procesarPendientes_cuandoNoHayPendientes_debeInvocarAmbosUseCases() {

        when(notificacionUseCase.reintentarFallidas()).thenReturn(List.of());
        when(recordatorioUseCase.procesarRecordatoriosPendientes())
                .thenReturn(List.of());


        scheduler.procesarPendientes();


        verify(notificacionUseCase).reintentarFallidas();
        verify(recordatorioUseCase).procesarRecordatoriosPendientes();
    }

    @Test
    void procesarPendientes_cuandoFallaReintento_debeCapturarLaExcepcion()
            throws Exception {

        when(notificacionUseCase.reintentarFallidas())
                .thenThrow(new IllegalStateException("Error al reintentar"));


        assertDoesNotThrow(() -> scheduler.procesarPendientes());

        verify(notificacionUseCase).reintentarFallidas();
        verifyNoInteractions(recordatorioUseCase);
    }

    @Test
    void procesarPendientes_cuandoFallaRecordatorio_debeCapturarLaExcepcion()
            throws Exception {

        when(notificacionUseCase.reintentarFallidas()).thenReturn(List.of());
        when(recordatorioUseCase.procesarRecordatoriosPendientes())
                .thenThrow(new IllegalStateException("Error al procesar"));


        assertDoesNotThrow(() -> scheduler.procesarPendientes());

        verify(notificacionUseCase).reintentarFallidas();
        verify(recordatorioUseCase).procesarRecordatoriosPendientes();
    }
}