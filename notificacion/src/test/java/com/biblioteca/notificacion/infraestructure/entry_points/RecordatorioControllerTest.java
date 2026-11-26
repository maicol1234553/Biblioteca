package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.usecase.RecordatorioUseCase;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.RecordatorioResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordatorioControllerTest {

    @Mock
    private RecordatorioUseCase recordatorioUseCase;

    @InjectMocks
    private RecordatorioController recordatorioController;

    @Test
    void listarRecordatorios_debeRetornarListaYEstadoOk() {

        Recordatorio recordatorio = org.mockito.Mockito.mock(Recordatorio.class);
        when(recordatorioUseCase.listarRecordatorios()).thenReturn(List.of(recordatorio));


        ResponseEntity<List<RecordatorioResponse>> respuesta =
                recordatorioController.listarRecordatorios();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(recordatorioUseCase).listarRecordatorios();
    }

    @Test
    void buscarRecordatorio_debeRetornarElRecordatorioYEstadoOk() {

        String id = "recordatorio-1";
        Recordatorio recordatorio = org.mockito.Mockito.mock(Recordatorio.class);
        when(recordatorioUseCase.buscarRecordatorio(id)).thenReturn(recordatorio);


        ResponseEntity<RecordatorioResponse> respuesta =
                recordatorioController.buscarRecordatorio(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(recordatorioUseCase).buscarRecordatorio(id);
    }

    @Test
    void listarPorUsuario_debeRetornarListaYEstadoOk() {

        String idUsuario = "usuario-1";
        Recordatorio recordatorio = org.mockito.Mockito.mock(Recordatorio.class);
        when(recordatorioUseCase.listarPorUsuario(idUsuario))
                .thenReturn(List.of(recordatorio));


        ResponseEntity<List<RecordatorioResponse>> respuesta =
                recordatorioController.listarPorUsuario(idUsuario);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(recordatorioUseCase).listarPorUsuario(idUsuario);
    }

    @Test
    void listarPendientes_debeRetornarListaYEstadoOk() {

        Recordatorio recordatorio = org.mockito.Mockito.mock(Recordatorio.class);
        when(recordatorioUseCase.listarPendientes()).thenReturn(List.of(recordatorio));


        ResponseEntity<List<RecordatorioResponse>> respuesta =
                recordatorioController.listarPendientes();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(recordatorioUseCase).listarPendientes();
    }

    @Test
    void cancelarRecordatorio_debeRetornarElRecordatorioCanceladoYEstadoOk() {

        String id = "recordatorio-1";
        Recordatorio recordatorio = org.mockito.Mockito.mock(Recordatorio.class);
        when(recordatorioUseCase.cancelarRecordatorio(id)).thenReturn(recordatorio);


        ResponseEntity<RecordatorioResponse> respuesta =
                recordatorioController.cancelarRecordatorio(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(recordatorioUseCase).cancelarRecordatorio(id);
    }

    @Test
    void procesarRecordatorios_debeRetornarLosProcesadosYEstadoOk() {

        Recordatorio recordatorio = org.mockito.Mockito.mock(Recordatorio.class);
        when(recordatorioUseCase.procesarRecordatoriosPendientes())
                .thenReturn(List.of(recordatorio));


        ResponseEntity<List<RecordatorioResponse>> respuesta =
                recordatorioController.procesarRecordatorios();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(recordatorioUseCase).procesarRecordatoriosPendientes();
    }

    @Test
    void eliminarRecordatorio_debeRetornarMensajeYEstadoOk() {

        String id = "recordatorio-1";


        ResponseEntity<MensajeResponse> respuesta =
                recordatorioController.eliminarRecordatorio(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(
                "Recordatorio eliminado con exito",
                respuesta.getBody().mensaje()
        );
        verify(recordatorioUseCase).eliminarRecordatorio(id);
    }
}