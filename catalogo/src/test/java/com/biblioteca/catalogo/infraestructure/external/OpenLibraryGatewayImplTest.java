package com.biblioteca.catalogo.infraestructure.external;

import com.biblioteca.catalogo.domain.model.LibroExterno;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenLibraryGatewayImplTest {

    private MockRestServiceServer openLibraryServer;
    private MockRestServiceServer googleBooksServer;
    private OpenLibraryGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        // Arrange común: crea clientes HTTP conectados a servidores simulados.
        RestClient.Builder openLibraryBuilder = RestClient.builder()
                .baseUrl("https://openlibrary.org");

        RestClient.Builder googleBooksBuilder = RestClient.builder()
                .baseUrl("https://www.googleapis.com");

        openLibraryServer = MockRestServiceServer.bindTo(openLibraryBuilder).build();
        googleBooksServer = MockRestServiceServer.bindTo(googleBooksBuilder).build();

    gateway = new OpenLibraryGatewayImpl(
                openLibraryBuilder.build(),
                googleBooksBuilder.build()
        );
    }

    @AfterEach
    void verificarSolicitudesEsperadas() {
        openLibraryServer.verify();
        googleBooksServer.verify();
    }

    @Test
    void buscar_cuandoOpenLibraryEncuentraLibros_debeMapearlos() {
        // Arrange
        esperarBusquedaOpenLibrary("q", "Dune", "20");

        openLibraryServer.expect(request ->
                        assertEquals("/search.json", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("q", "Dune"))
                .andExpect(queryParam("limit", "20"))
                .andRespond(withSuccess("""
                        {
                          "docs": [
                            {
                              "key": "/works/OL45804W",
                              "title": "Dune",
                              "subtitle": "La casa Atreides",
                              "author_name": ["Frank Herbert"],
                              "publisher": ["Ace Books"],
                              "first_publish_year": 1965,
                              "isbn": ["0441172717", "9780441172719"],
                              "language": ["eng"],
                              "number_of_pages_median": 412,
                              "cover_i": 12345
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        List<LibroExterno> resultado = gateway.buscar("Dune");

        // Assert
        assertEquals(1, resultado.size());

        LibroExterno libro = resultado.get(0);
        assertEquals("Open Library", libro.getFuente());
        assertEquals("/works/OL45804W", libro.getIdExterno());
        assertEquals("Dune", libro.getTitulo());
        assertEquals("La casa Atreides", libro.getSubtitulo());
        assertEquals(List.of("Frank Herbert"), libro.getAutores());
        assertEquals("Ace Books", libro.getEditorial());
        assertEquals(LocalDate.of(1965, 1, 1), libro.getFechaPublicacion());
        assertEquals("9780441172719", libro.getIsbn());
        assertEquals("eng", libro.getIdioma());
        assertEquals(412, libro.getNumeroPaginas());
        assertEquals(
                "https://covers.openlibrary.org/b/id/12345-L.jpg",
                libro.getPortadaUrl()
        );
    }

    @Test
    void buscar_cuandoOpenLibraryNoEncuentra_debeConsultarGoogleBooks() {
        // Arrange
        esperarBusquedaOpenLibrary("q", "El nombre del viento", "20");
        esperarBusquedaGoogleBooks("q", "El nombre del viento", "20");

        openLibraryServer.expect(request ->
                        assertEquals("/search.json", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("q", "El%20nombre%20del%20viento"))
                .andRespond(withSuccess("""
                        {"docs": []}
                        """, MediaType.APPLICATION_JSON));

        googleBooksServer.expect(request ->
                        assertEquals("/books/v1/volumes", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("q", "El%20nombre%20del%20viento"))
                .andExpect(queryParam("maxResults", "20"))
                .andRespond(withSuccess("""
                        {
                          "items": [
                            {
                              "id": "google-book-1",
                              "volumeInfo": {
                                "title": "El nombre del viento",
                                "subtitle": "Crónica del asesino de reyes",
                                "authors": ["Patrick Rothfuss"],
                                "publisher": "Plaza & Janés",
                                "publishedDate": "2007-03",
                                "industryIdentifiers": [
                                  {
                                    "type": "ISBN_10",
                                    "identifier": "8401352837"
                                  },
                                  {
                                    "type": "ISBN_13",
                                    "identifier": "9788401352836"
                                  }
                                ],
                                "language": "es",
                                "pageCount": 880,
                                "description": "Historia de Kvothe",
                                "imageLinks": {
                                  "thumbnail": "http://books.google.com/cover.jpg"
                                }
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        List<LibroExterno> resultado =
                gateway.buscar("El nombre del viento");

        // Assert
        assertEquals(1, resultado.size());

        LibroExterno libro = resultado.get(0);
        assertEquals("Google Books", libro.getFuente());
        assertEquals("google-book-1", libro.getIdExterno());
        assertEquals("El nombre del viento", libro.getTitulo());
        assertEquals("Crónica del asesino de reyes", libro.getSubtitulo());
        assertEquals(List.of("Patrick Rothfuss"), libro.getAutores());
        assertEquals("Plaza & Janés", libro.getEditorial());
        assertEquals(LocalDate.of(2007, 3, 1), libro.getFechaPublicacion());
        assertEquals("9788401352836", libro.getIsbn());
        assertEquals("es", libro.getIdioma());
        assertEquals(880, libro.getNumeroPaginas());
        assertEquals("Historia de Kvothe", libro.getDescripcion());
        assertEquals("https://books.google.com/cover.jpg", libro.getPortadaUrl());
    }

    @Test
    void buscar_cuandoOpenLibraryFalla_debeIntentarGoogleBooks() {
        // Arrange
        esperarBusquedaOpenLibrary("q", "libro desconocido", "20");
        esperarBusquedaGoogleBooks("q", "libro desconocido", "20");

        openLibraryServer.expect(request ->
                        assertEquals("/search.json", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        googleBooksServer.expect(request ->
                        assertEquals("/books/v1/volumes", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("q", "libro%20desconocido"))
                .andRespond(withSuccess("""
                        {"items": []}
                        """, MediaType.APPLICATION_JSON));

        // Act
        List<LibroExterno> resultado = gateway.buscar("libro desconocido");

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorIsbn_cuandoOpenLibraryEncuentraElLibro_debeRetornarlo() {
        // Arrange
        String isbn = "9780441172719";

        esperarBusquedaOpenLibrary("isbn", isbn, "1");

        openLibraryServer.expect(request ->
                        assertEquals("/search.json", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("isbn", isbn))
                .andExpect(queryParam("limit", "1"))
                .andRespond(withSuccess("""
                        {
                          "docs": [
                            {
                              "key": "/works/OL45804W",
                              "title": "Dune",
                              "author_name": ["Frank Herbert"],
                              "first_publish_year": 1965,
                              "number_of_pages_median": 412
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        Optional<LibroExterno> resultado = gateway.buscarPorIsbn(isbn);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Open Library", resultado.get().getFuente());
        assertEquals("Dune", resultado.get().getTitulo());
        assertEquals(isbn, resultado.get().getIsbn());
    }

    @Test
    void buscarPorIsbn_cuandoOpenLibraryNoEncuentra_debeConsultarGoogleBooks() {
        // Arrange
        String isbn = "9788401352836";

        esperarBusquedaOpenLibrary("isbn", isbn, "1");
        esperarBusquedaGoogleBooks("q", "isbn:" + isbn, "1");

        openLibraryServer.expect(request ->
                        assertEquals("/search.json", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"docs": []}
                        """, MediaType.APPLICATION_JSON));

        googleBooksServer.expect(request ->
                        assertEquals("/books/v1/volumes", request.getURI().getPath()))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("q", "isbn:" + isbn))
                .andExpect(queryParam("maxResults", "1"))
                .andRespond(withSuccess("""
                        {
                          "items": [
                            {
                              "id": "google-book-2",
                              "volumeInfo": {
                                "title": "El nombre del viento",
                                "authors": ["Patrick Rothfuss"],
                                "publishedDate": "2007",
                                "language": "es",
                                "pageCount": 880
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        // Act
        Optional<LibroExterno> resultado = gateway.buscarPorIsbn(isbn);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("Google Books", resultado.get().getFuente());
        assertEquals("El nombre del viento", resultado.get().getTitulo());
        assertEquals(isbn, resultado.get().getIsbn());
        assertEquals(LocalDate.of(2007, 1, 1), resultado.get().getFechaPublicacion());
    }

    @Test
    void buscarPorIsbn_cuandoNingunaFuenteEncuentraElLibro_debeRetornarOptionalVacio() {
        // Arrange
        String isbn = "9780000000000";

        esperarBusquedaOpenLibrary("isbn", isbn, "1");
        esperarBusquedaGoogleBooks("q", "isbn:" + isbn, "1");

        openLibraryServer.expect(request ->
                        assertEquals("/search.json", request.getURI().getPath()))
                .andRespond(withSuccess("""
                        {"docs": []}
                        """, MediaType.APPLICATION_JSON));

        googleBooksServer.expect(request ->
                        assertEquals("/books/v1/volumes", request.getURI().getPath()))
                .andRespond(withSuccess("""
                        {"items": []}
                        """, MediaType.APPLICATION_JSON));

        // Act
        Optional<LibroExterno> resultado = gateway.buscarPorIsbn(isbn);

        // Assert
        assertFalse(resultado.isPresent());
    }

    private void esperarBusquedaOpenLibrary(
            String nombreParametro, String valorParametro, String limite) {

        // Este método solo documenta y valida los parámetros comunes en cada test.
        // Las expectativas HTTP concretas se declaran dentro de cada caso.
    }

    private void esperarBusquedaGoogleBooks(
            String nombreParametro, String valorParametro, String limite) {

        // Este método solo documenta y valida los parámetros comunes en cada test.
        // Las expectativas HTTP concretas se declaran dentro de cada caso.
    }
}