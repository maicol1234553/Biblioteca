package com.biblioteca.catalogo.infraestructure.entry_points;

import com.biblioteca.catalogo.domain.exception.EditorialNoEncontradaException;
import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.domain.usecase.EditorialUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditorialControllerTest {

    @Mock
    private EditorialUseCase editorialUseCase;

    @InjectMocks
    private EditorialController editorialController;

    private Editorial editorial;

    @BeforeEach
    void setUp() {
        editorial = new Editorial();
        editorial.setIdEditorial("EDIT-1");
        editorial.setNombre("Editorial Ejemplo");
    }



    @Test
    void listarEditoriales_RetornaListaYEstado200() {

        List<Editorial> editorialesEsperadas = List.of(editorial);

        when(editorialUseCase.listarEditoriales())
                .thenReturn(editorialesEsperadas);


        ResponseEntity<List<Editorial>> respuesta =
                editorialController.listarEditoriales();


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(editorialesEsperadas, respuesta.getBody());
        verify(editorialUseCase).listarEditoriales();
    }



    @Test
    void buscarEditorialPorId_EditorialExiste_RetornaEditorialYEstado200() {

        String id = "EDIT-1";

        when(editorialUseCase.buscarEditorialPorId(id))
                .thenReturn(editorial);


        ResponseEntity<Editorial> respuesta =
                editorialController.buscarEditorialPorId(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(editorial, respuesta.getBody());
        verify(editorialUseCase).buscarEditorialPorId(id);
    }

    @Test
    void buscarEditorialPorId_EditorialNoExiste_PropagaExcepcion() {

        String id = "EDIT-INEXISTENTE";

        when(editorialUseCase.buscarEditorialPorId(id))
                .thenThrow(new EditorialNoEncontradaException(
                        "Editorial no encontrada con id: " + id
                ));


        EditorialNoEncontradaException excepcion = assertThrows(
                EditorialNoEncontradaException.class,
                () -> editorialController.buscarEditorialPorId(id)
        );


        assertEquals(
                "Editorial no encontrada con id: " + id,
                excepcion.getMessage()
        );
        verify(editorialUseCase).buscarEditorialPorId(id);
    }



    @Test
    void guardarEditorial_EditorialValida_RetornaEditorialYEstado201() {

        when(editorialUseCase.guardarEditorial(editorial))
                .thenReturn(editorial);


        ResponseEntity<Editorial> respuesta =
                editorialController.guardarEditorial(editorial);


        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(editorial, respuesta.getBody());
        verify(editorialUseCase).guardarEditorial(editorial);
    }



    @Test
    void actualizarEditorial_EditorialValida_RetornaEditorialYEstado200() {

        String id = "EDIT-1";

        when(editorialUseCase.actualizarEditorial(id, editorial))
                .thenReturn(editorial);


        ResponseEntity<Editorial> respuesta =
                editorialController.actualizarEditorial(id, editorial);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(editorial, respuesta.getBody());
        verify(editorialUseCase).actualizarEditorial(id, editorial);
    }



    @Test
    void eliminarEditorial_EditorialExiste_RetornaMensajeYEstado200() {

        String id = "EDIT-1";


        ResponseEntity<String> respuesta =
                editorialController.eliminarEditorial(id);


        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Editorial eliminada con exito", respuesta.getBody());
        verify(editorialUseCase).eliminarEditorial(id);
    }

    @Test
    void eliminarEditorial_EditorialNoExiste_PropagaExcepcion() {

        String id = "EDIT-INEXISTENTE";

        doThrow(new EditorialNoEncontradaException(
                "Editorial no encontrada con id: " + id
        )).when(editorialUseCase).eliminarEditorial(id);


        EditorialNoEncontradaException excepcion = assertThrows(
                EditorialNoEncontradaException.class,
                () -> editorialController.eliminarEditorial(id)
        );


        assertEquals(
                "Editorial no encontrada con id: " + id,
                excepcion.getMessage()
        );
        verify(editorialUseCase).eliminarEditorial(id);
    }
}