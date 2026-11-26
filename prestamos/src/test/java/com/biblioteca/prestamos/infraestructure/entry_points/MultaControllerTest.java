package com.biblioteca.prestamos.infraestructure.entry_points;

import com.biblioteca.prestamos.domain.model.Multa;
import com.biblioteca.prestamos.domain.model.UsuarioExterno;
import com.biblioteca.prestamos.domain.usecase.MultaUseCase;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.prestamos.infraestructure.entry_points.dto.MultaResponse;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultaControllerTest {

    @Mock
    private MultaUseCase multaUseCase;

    @InjectMocks
    private MultaController multaController;

    @Test
    void listarMultas_debeRetornarListaYEstadoOk() {

        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.listarMultas(solicitante))
                .thenReturn(List.of(multa));


        ResponseEntity<List<MultaResponse>> respuesta =
                multaController.listarMultas(solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(multaUseCase).listarMultas(solicitante);
    }

    @Test
    void buscarMulta_debeRetornarMultaYEstadoOk() {

        String idMulta = "multa-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.buscarMulta(idMulta, solicitante))
                .thenReturn(multa);


        ResponseEntity<MultaResponse> respuesta =
                multaController.buscarMulta(idMulta, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(multaUseCase).buscarMulta(idMulta, solicitante);
    }

    @Test
    void listarMultasPorUsuario_debeRetornarListaYEstadoOk() {

        String usuarioId = "usuario-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.consultarMultasPorUsuario(usuarioId, solicitante))
                .thenReturn(List.of(multa));


        ResponseEntity<List<MultaResponse>> respuesta =
                multaController.listarMultasPorUsuario(usuarioId, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(multaUseCase).consultarMultasPorUsuario(usuarioId, solicitante);
    }

    @Test
    void listarMultasPendientesPorUsuario_debeRetornarListaYEstadoOk() {

        String usuarioId = "usuario-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.consultarMultasPendientesPorUsuario(usuarioId, solicitante))
                .thenReturn(List.of(multa));


        ResponseEntity<List<MultaResponse>> respuesta =
                multaController.listarMultasPendientesPorUsuario(usuarioId, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(multaUseCase).consultarMultasPendientesPorUsuario(usuarioId, solicitante);
    }

    @Test
    void listarMultasPorPrestamo_debeRetornarListaYEstadoOk() {

        String idPrestamo = "prestamo-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.listarMultasPorPrestamo(idPrestamo, solicitante))
                .thenReturn(List.of(multa));


        ResponseEntity<List<MultaResponse>> respuesta =
                multaController.listarMultasPorPrestamo(idPrestamo, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(multaUseCase).listarMultasPorPrestamo(idPrestamo, solicitante);
    }

    @Test
    void pagarMulta_debeRetornarMultaPagadaYEstadoOk() {

        String idMulta = "multa-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.pagarMulta(idMulta, solicitante))
                .thenReturn(multa);


        ResponseEntity<MultaResponse> respuesta =
                multaController.pagarMulta(idMulta, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(multaUseCase).pagarMulta(idMulta, solicitante);
    }

    @Test
    void anularMulta_debeRetornarMultaAnuladaYEstadoOk() {

        String idMulta = "multa-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);
        Multa multa = mock(Multa.class);

        when(multaUseCase.anularMulta(idMulta, solicitante))
                .thenReturn(multa);


        ResponseEntity<MultaResponse> respuesta =
                multaController.anularMulta(idMulta, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        verify(multaUseCase).anularMulta(idMulta, solicitante);
    }

    @Test
    void eliminarMulta_debeRetornarMensajeYEstadoOk() {

        String idMulta = "multa-1";
        UsuarioExterno solicitante = mock(UsuarioExterno.class);


        ResponseEntity<MensajeResponse> respuesta =
                multaController.eliminarMulta(idMulta, solicitante);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Multa eliminada con exito", respuesta.getBody().mensaje());
        verify(multaUseCase).eliminarMulta(idMulta, solicitante);
    }
}