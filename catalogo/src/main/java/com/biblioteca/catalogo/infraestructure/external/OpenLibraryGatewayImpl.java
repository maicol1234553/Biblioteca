package com.biblioteca.catalogo.infraestructure.external;

import com.biblioteca.catalogo.domain.model.LibroExterno;
import com.biblioteca.catalogo.domain.model.gateway.LibroExternoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Consulta informacion bibliografica en Open Library y, si no hay
 * resultados o el servicio falla, usa Google Books como respaldo.
 *
 * Ambas APIs son publicas; Google Books puede limitar la cantidad de
 * peticiones sin API key, suficiente para el flujo academico del proyecto.
 */
@Component
public class OpenLibraryGatewayImpl implements LibroExternoGateway {

    private static final Logger log = LoggerFactory.getLogger(OpenLibraryGatewayImpl.class);
    private static final int LIMITE_RESULTADOS = 20;

    private final RestClient openLibrary;
    private final RestClient googleBooks;

    public OpenLibraryGatewayImpl() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));

        this.openLibrary = RestClient.builder()
                .baseUrl("https://openlibrary.org")
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, "biblioteca-catalogo/1.0")
                .build();

        this.googleBooks = RestClient.builder()
                .baseUrl("https://www.googleapis.com")
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, "biblioteca-catalogo/1.0")
                .build();
    }

    @Override
    public List<LibroExterno> buscar(String consulta) {
        List<LibroExterno> resultados = buscarOpenLibrary(consulta);

        if (!resultados.isEmpty()) {
            return resultados;
        }

        return buscarGoogleBooks(consulta);
    }

    @Override
    public Optional<LibroExterno> buscarPorIsbn(String isbn) {
        Optional<LibroExterno> resultado = buscarPorIsbnOpenLibrary(isbn);

        if (resultado.isPresent()) {
            return resultado;
        }

        return buscarPorIsbnGoogleBooks(isbn);
    }

    private List<LibroExterno> buscarOpenLibrary(String consulta) {
        try {
            JsonNode respuesta = openLibrary.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", consulta)
                            .queryParam("limit", LIMITE_RESULTADOS)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            List<LibroExterno> libros = new ArrayList<>();

            if (respuesta == null) {
                return libros;
            }

            for (JsonNode documento : respuesta.path("docs")) {
                libros.add(desdeOpenLibrary(documento, null));
            }

            return libros;
        } catch (RestClientException exception) {
            log.warn("Open Library no respondio correctamente: {}", exception.getMessage());
            return List.of();
        }
    }

    private Optional<LibroExterno> buscarPorIsbnOpenLibrary(String isbn) {
        try {
            JsonNode respuesta = openLibrary.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("isbn", isbn)
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (respuesta == null) {
                return Optional.empty();
            }

            JsonNode documentos = respuesta.path("docs");

            if (!documentos.isArray() || documentos.size() == 0) {
                return Optional.empty();
            }

            return Optional.of(desdeOpenLibrary(documentos.get(0), isbn));
        } catch (RestClientException exception) {
            log.warn("Open Library no respondio correctamente: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    private List<LibroExterno> buscarGoogleBooks(String consulta) {
        try {
            JsonNode respuesta = googleBooks.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/books/v1/volumes")
                            .queryParam("q", consulta)
                            .queryParam("maxResults", LIMITE_RESULTADOS)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            List<LibroExterno> libros = new ArrayList<>();

            if (respuesta == null) {
                return libros;
            }

            for (JsonNode item : respuesta.path("items")) {
                libros.add(desdeGoogleBooks(item, item.path("volumeInfo"), null));
            }

            return libros;
        } catch (RestClientException exception) {
            log.warn("Google Books no respondio correctamente: {}", exception.getMessage());
            return List.of();
        }
    }

    private Optional<LibroExterno> buscarPorIsbnGoogleBooks(String isbn) {
        try {
            JsonNode respuesta = googleBooks.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/books/v1/volumes")
                            .queryParam("q", "isbn:" + isbn)
                            .queryParam("maxResults", 1)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (respuesta == null) {
                return Optional.empty();
            }

            JsonNode items = respuesta.path("items");

            if (!items.isArray() || items.size() == 0) {
                return Optional.empty();
            }

            JsonNode item = items.get(0);

            return Optional.of(desdeGoogleBooks(item, item.path("volumeInfo"), isbn));
        } catch (RestClientException exception) {
            log.warn("Google Books no respondio correctamente: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    private LibroExterno desdeOpenLibrary(JsonNode documento, String isbnSolicitado) {
        LibroExterno libro = new LibroExterno();

        libro.setFuente("Open Library");
        libro.setIdExterno(texto(documento, "key"));
        libro.setTitulo(texto(documento, "title"));
        libro.setSubtitulo(texto(documento, "subtitle"));
        libro.setAutores(textos(documento.path("author_name")));
        libro.setEditorial(primerTexto(documento.path("publisher")));
        libro.setFechaPublicacion(fechaDesdeAnio(documento, "first_publish_year"));
        libro.setIsbn(isbnSolicitado != null ? isbnSolicitado : isbnDesdeLista(documento.path("isbn")));
        libro.setIdioma(primerTexto(documento.path("language")));
        libro.setNumeroPaginas(numero(documento, "number_of_pages_median"));
        libro.setPortadaUrl(portadaOpenLibrary(documento));

        return libro;
    }

    private LibroExterno desdeGoogleBooks(JsonNode item, JsonNode informacion, String isbnSolicitado) {
        LibroExterno libro = new LibroExterno();

        libro.setFuente("Google Books");
        libro.setIdExterno(texto(item, "id"));
        libro.setTitulo(texto(informacion, "title"));
        libro.setSubtitulo(texto(informacion, "subtitle"));
        libro.setAutores(textos(informacion.path("authors")));
        libro.setEditorial(texto(informacion, "publisher"));
        libro.setFechaPublicacion(parsearFecha(texto(informacion, "publishedDate")));
        libro.setIsbn(isbnSolicitado != null ? isbnSolicitado : isbnDesdeIdentificadores(informacion));
        libro.setIdioma(texto(informacion, "language"));
        libro.setNumeroPaginas(numero(informacion, "pageCount"));
        libro.setDescripcion(texto(informacion, "description"));
        libro.setPortadaUrl(portadaGoogleBooks(informacion));

        return libro;
    }

    private String portadaOpenLibrary(JsonNode documento) {
        Integer coverId = numero(documento, "cover_i");

        if (coverId == null || coverId <= 0) {
            return null;
        }

        return "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
    }

    private String portadaGoogleBooks(JsonNode informacion) {
        String portada = texto(informacion.path("imageLinks"), "thumbnail");

        if (portada != null && portada.startsWith("http://")) {
            return "https://" + portada.substring("http://".length());
        }

        return portada;
    }

    private String isbnDesdeIdentificadores(JsonNode informacion) {
        String isbn10 = null;
        String isbn13 = null;

        for (JsonNode identificador : informacion.path("industryIdentifiers")) {
            String tipo = texto(identificador, "type");
            String valor = texto(identificador, "identifier");

            if ("ISBN_13".equalsIgnoreCase(tipo)) {
                isbn13 = valor;
            } else if ("ISBN_10".equalsIgnoreCase(tipo)) {
                isbn10 = valor;
            }
        }

        return isbn13 != null ? isbn13 : isbn10;
    }

    private String isbnDesdeLista(JsonNode isbns) {
        List<String> valores = textos(isbns);

        for (String valor : valores) {
            if (valor.replaceAll("[^0-9Xx]", "").length() == 13) {
                return valor;
            }
        }

        return valores.isEmpty() ? null : valores.get(0);
    }

    private LocalDate fechaDesdeAnio(JsonNode nodo, String campo) {
        JsonNode valor = nodo.path(campo);

        if (valor.isNumber() && valor.asInt() > 0) {
            return LocalDate.of(valor.asInt(), 1, 1);
        }

        return null;
    }

    private LocalDate parsearFecha(String valor) {
        if (esBlanco(valor)) {
            return null;
        }

        String limpio = valor.trim();

        try {
            if (limpio.matches("\\d{4}")) {
                return LocalDate.of(Integer.parseInt(limpio), 1, 1);
            }

            if (limpio.matches("\\d{4}-\\d{2}")) {
                String[] partes = limpio.split("-");
                return LocalDate.of(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]), 1);
            }

            return LocalDate.parse(limpio);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String texto(JsonNode nodo, String campo) {
        if (nodo == null || nodo.isMissingNode() || nodo.isNull()) {
            return null;
        }

        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }

        String texto = valor.asString();
        return esBlanco(texto) ? null : texto;
    }

    private List<String> textos(JsonNode nodo) {
        List<String> valores = new ArrayList<>();

        if (nodo == null || !nodo.isArray()) {
            return valores;
        }

        for (JsonNode item : nodo) {
            String valor = item.asString();
            if (!esBlanco(valor)) {
                valores.add(valor);
            }
        }

        return valores;
    }

    private String primerTexto(JsonNode nodo) {
        List<String> valores = textos(nodo);
        return valores.isEmpty() ? null : valores.get(0);
    }

    private Integer numero(JsonNode nodo, String campo) {
        if (nodo == null) {
            return null;
        }

        JsonNode valor = nodo.path(campo);
        return valor.isNumber() ? valor.asInt() : null;
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
