package com.biblioteca.catalogo.infraestructure.external;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenLibraryGatewayImplConstructorTest {

    @Test
    void constructorSinArgumentosCreaLosClientesHttp() {
        OpenLibraryGatewayImpl gateway = new OpenLibraryGatewayImpl();

        assertNotNull(gateway);
    }
}
