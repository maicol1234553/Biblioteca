package com.biblioteca.catalogo.domain.usecase;

import com.biblioteca.catalogo.domain.exception.EditorialNoEncontradaException;
import com.biblioteca.catalogo.domain.exception.RecursoDuplicadoException;
import com.biblioteca.catalogo.domain.model.Editorial;
import com.biblioteca.catalogo.domain.model.gateway.EditorialGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditorialUseCaseTest {

    @Mock
    private EditorialGateway editorialGateway;

    @InjectMocks
    private EditorialUseCase editorialUseCase;

    private Editorial editorial;

    @BeforeEach
    void setUp() {
        editorial = crearEditorialValida();
    }



    @Test
    void guardarEditorial_DatosValidosSinId_GeneraIdYGuarda() {

        editorial.setIdEditorial(null);

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertSame(editorial, resultado);
        assertNotNull(resultado.getIdEditorial());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdEditorial()));

        verify(editorialGateway)
                .buscarEditorialPorNombre(editorial.getNombre());
        verify(editorialGateway).guardarEditorial(editorial);
    }

    @Test
    void guardarEditorial_IdEnBlanco_GeneraIdYGuarda() {

        editorial.setIdEditorial(" ");

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertNotNull(resultado.getIdEditorial());
        assertDoesNotThrow(() -> UUID.fromString(resultado.getIdEditorial()));
        verify(editorialGateway).guardarEditorial(editorial);
    }

    @Test
    void guardarEditorial_IdExistente_LoConserva() {

        editorial.setIdEditorial("EDIT-1");

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertEquals("EDIT-1", resultado.getIdEditorial());
        verify(editorialGateway).guardarEditorial(editorial);
    }

    @Test
    void guardarEditorial_NombreDuplicado_LanzaExcepcion() {

        Editorial existente = crearEditorialValida();
        existente.setIdEditorial("EDIT-2");

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(existente);


        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> editorialUseCase.guardarEditorial(editorial)
        );


        assertEquals(
                "Ya existe una editorial con el nombre: " + editorial.getNombre(),
                excepcion.getMessage()
        );
        verify(editorialGateway, never())
                .guardarEditorial(any(Editorial.class));
    }

    @Test
    void guardarEditorial_EditorialNula_LanzaExcepcion() {

        Editorial editorialNula = null;


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> editorialUseCase.guardarEditorial(editorialNula)
        );


        assertEquals("La editorial es obligatoria", excepcion.getMessage());
        verifyNoInteractions(editorialGateway);
    }

    @Test
    void guardarEditorial_NombreNulo_LanzaExcepcion() {

        editorial.setNombre(null);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> editorialUseCase.guardarEditorial(editorial)
        );


        assertEquals(
                "El nombre de la editorial es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(editorialGateway);
    }

    @Test
    void guardarEditorial_NombreEnBlanco_LanzaExcepcion() {

        editorial.setNombre(" ");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> editorialUseCase.guardarEditorial(editorial)
        );


        assertEquals(
                "El nombre de la editorial es obligatorio",
                excepcion.getMessage()
        );
        verifyNoInteractions(editorialGateway);
    }

    @Test
    void guardarEditorial_CorreoContactoInvalido_LanzaExcepcion() {

        editorial.setCorreoContacto("correo-invalido");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> editorialUseCase.guardarEditorial(editorial)
        );


        assertEquals(
                "El correo de contacto no es valido",
                excepcion.getMessage()
        );
        verifyNoInteractions(editorialGateway);
    }

    @Test
    void guardarEditorial_CorreoContactoNulo_PermiteGuardar() {

        editorial.setCorreoContacto(null);

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertSame(editorial, resultado);
        verify(editorialGateway).guardarEditorial(editorial);
    }

    @Test
    void guardarEditorial_CorreoContactoEnBlanco_PermiteGuardar() {

        editorial.setCorreoContacto(" ");

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertSame(editorial, resultado);
        verify(editorialGateway).guardarEditorial(editorial);
    }

    @Test
    void guardarEditorial_SitioWebInvalido_LanzaExcepcion() {

        editorial.setSitioWeb("www.editorial.com");


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> editorialUseCase.guardarEditorial(editorial)
        );


        assertEquals(
                "El sitio web debe iniciar con http:// o https://",
                excepcion.getMessage()
        );
        verifyNoInteractions(editorialGateway);
    }

    @Test
    void guardarEditorial_SitioWebHttpValido_PermiteGuardar() {

        editorial.setSitioWeb("http://editorial.com");

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertSame(editorial, resultado);
        verify(editorialGateway).guardarEditorial(editorial);
    }

    @Test
    void guardarEditorial_SitioWebHttpsValido_PermiteGuardar() {

        editorial.setSitioWeb("https://editorial.com");

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertSame(editorial, resultado);
        verify(editorialGateway).guardarEditorial(editorial);
    }

    @Test
    void guardarEditorial_SitioWebNulo_PermiteGuardar() {

        editorial.setSitioWeb(null);

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.guardarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado = editorialUseCase.guardarEditorial(editorial);


        assertSame(editorial, resultado);
        verify(editorialGateway).guardarEditorial(editorial);
    }



    @Test
    void actualizarEditorial_DatosValidos_AsignaIdYActualiza() {

        String idEditorial = "EDIT-1";

        Editorial existente = crearEditorialValida();
        existente.setIdEditorial(idEditorial);

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(existente);

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(null);

        when(editorialGateway.actualizarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado =
                editorialUseCase.actualizarEditorial(idEditorial, editorial);


        assertSame(editorial, resultado);
        assertEquals(idEditorial, resultado.getIdEditorial());

        verify(editorialGateway).buscarEditorialPorId(idEditorial);
        verify(editorialGateway)
                .buscarEditorialPorNombre(editorial.getNombre());
        verify(editorialGateway).actualizarEditorial(editorial);
    }

    @Test
    void actualizarEditorial_EditorialNoExiste_LanzaExcepcion() {

        String idEditorial = "ID-INEXISTENTE";

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(null);


        EditorialNoEncontradaException excepcion = assertThrows(
                EditorialNoEncontradaException.class,
                () -> editorialUseCase.actualizarEditorial(idEditorial, editorial)
        );


        assertEquals(
                "Editorial no encontrada con id: " + idEditorial,
                excepcion.getMessage()
        );
        verify(editorialGateway, never())
                .actualizarEditorial(any(Editorial.class));
        verify(editorialGateway, never())
                .buscarEditorialPorNombre(anyString());
    }

    @Test
    void actualizarEditorial_NombreDeOtraEditorial_LanzaExcepcion() {

        String idEditorial = "EDIT-1";

        Editorial existente = crearEditorialValida();
        existente.setIdEditorial(idEditorial);

        Editorial otraEditorial = crearEditorialValida();
        otraEditorial.setIdEditorial("EDIT-2");

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(existente);

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(otraEditorial);


        RecursoDuplicadoException excepcion = assertThrows(
                RecursoDuplicadoException.class,
                () -> editorialUseCase.actualizarEditorial(idEditorial, editorial)
        );


        assertEquals(
                "Ya existe una editorial con el nombre: " + editorial.getNombre(),
                excepcion.getMessage()
        );
        verify(editorialGateway, never())
                .actualizarEditorial(any(Editorial.class));
    }

    @Test
    void actualizarEditorial_MantieneSuNombreActual_PermiteActualizar() {

        String idEditorial = "EDIT-1";

        Editorial existente = crearEditorialValida();
        existente.setIdEditorial(idEditorial);

        editorial.setNombre(existente.getNombre());

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(existente);

        when(editorialGateway.buscarEditorialPorNombre(editorial.getNombre()))
                .thenReturn(existente);

        when(editorialGateway.actualizarEditorial(editorial))
                .thenReturn(editorial);


        Editorial resultado =
                editorialUseCase.actualizarEditorial(idEditorial, editorial);


        assertSame(editorial, resultado);
        assertEquals(idEditorial, resultado.getIdEditorial());
        verify(editorialGateway).actualizarEditorial(editorial);
    }

    @Test
    void actualizarEditorial_CorreoContactoInvalido_LanzaExcepcionYSinActualizar() {

        String idEditorial = "EDIT-1";

        Editorial existente = crearEditorialValida();
        existente.setIdEditorial(idEditorial);

        editorial.setCorreoContacto("correo-invalido");

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(existente);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> editorialUseCase.actualizarEditorial(idEditorial, editorial)
        );


        assertEquals(
                "El correo de contacto no es valido",
                excepcion.getMessage()
        );
        verify(editorialGateway, never())
                .actualizarEditorial(any(Editorial.class));
        verify(editorialGateway, never())
                .buscarEditorialPorNombre(anyString());
    }

    @Test
    void actualizarEditorial_SitioWebInvalido_LanzaExcepcionYSinActualizar() {

        String idEditorial = "EDIT-1";

        Editorial existente = crearEditorialValida();
        existente.setIdEditorial(idEditorial);

        editorial.setSitioWeb("editorial.com");

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(existente);


        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> editorialUseCase.actualizarEditorial(idEditorial, editorial)
        );


        assertEquals(
                "El sitio web debe iniciar con http:// o https://",
                excepcion.getMessage()
        );
        verify(editorialGateway, never())
                .actualizarEditorial(any(Editorial.class));
    }



    @Test
    void buscarEditorialPorId_EditorialExiste_RetornaEditorial() {

        when(editorialGateway.buscarEditorialPorId(editorial.getIdEditorial()))
                .thenReturn(editorial);


        Editorial resultado =
                editorialUseCase.buscarEditorialPorId(editorial.getIdEditorial());


        assertSame(editorial, resultado);
        verify(editorialGateway)
                .buscarEditorialPorId(editorial.getIdEditorial());
    }

    @Test
    void buscarEditorialPorId_EditorialNoExiste_LanzaExcepcion() {

        String idEditorial = "ID-INEXISTENTE";

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(null);


        EditorialNoEncontradaException excepcion = assertThrows(
                EditorialNoEncontradaException.class,
                () -> editorialUseCase.buscarEditorialPorId(idEditorial)
        );


        assertEquals(
                "Editorial no encontrada con id: " + idEditorial,
                excepcion.getMessage()
        );
    }



    @Test
    void listarEditoriales_ConsultaGateway_RetornaLista() {

        List<Editorial> editorialesEsperadas = List.of(editorial);

        when(editorialGateway.listarEditoriales())
                .thenReturn(editorialesEsperadas);


        List<Editorial> resultado = editorialUseCase.listarEditoriales();


        assertSame(editorialesEsperadas, resultado);
        assertEquals(1, resultado.size());
        assertEquals(editorial, resultado.get(0));
        verify(editorialGateway).listarEditoriales();
    }

    @Test
    void listarEditoriales_NoHayEditoriales_RetornaListaVacia() {

        when(editorialGateway.listarEditoriales())
                .thenReturn(List.of());


        List<Editorial> resultado = editorialUseCase.listarEditoriales();


        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }



    @Test
    void eliminarEditorial_EditorialExiste_EliminaEditorial() {

        String idEditorial = editorial.getIdEditorial();

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(editorial);


        editorialUseCase.eliminarEditorial(idEditorial);


        verify(editorialGateway).buscarEditorialPorId(idEditorial);
        verify(editorialGateway).eliminarEditorial(idEditorial);
    }

    @Test
    void eliminarEditorial_EditorialNoExiste_LanzaExcepcionYSinEliminar() {

        String idEditorial = "ID-INEXISTENTE";

        when(editorialGateway.buscarEditorialPorId(idEditorial))
                .thenReturn(null);


        EditorialNoEncontradaException excepcion = assertThrows(
                EditorialNoEncontradaException.class,
                () -> editorialUseCase.eliminarEditorial(idEditorial)
        );


        assertEquals(
                "Editorial no encontrada con id: " + idEditorial,
                excepcion.getMessage()
        );
        verify(editorialGateway, never()).eliminarEditorial(anyString());
    }

    private Editorial crearEditorialValida() {
        Editorial nuevaEditorial = new Editorial();
        nuevaEditorial.setIdEditorial("EDIT-1");
        nuevaEditorial.setNombre("Editorial Ejemplo");
        nuevaEditorial.setCorreoContacto("contacto@editorial.com");
        nuevaEditorial.setSitioWeb("https://editorial.com");
        return nuevaEditorial;
    }
}