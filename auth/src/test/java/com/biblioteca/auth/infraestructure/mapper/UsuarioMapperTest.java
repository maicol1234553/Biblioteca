package com.biblioteca.auth.infraestructure.mapper;

import com.biblioteca.auth.domain.model.Usuario;
import com.biblioteca.auth.infraestructure.driver_adapters.UsuarioData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void toUsuario_cuandoRecibeDatos_debeMapearTodosLosCampos() {

        UsuarioData usuarioData = new UsuarioData(
                "usuario-1",
                "Ana Pérez",
                "ana@correo.com",
                "password-encriptado",
                "USUARIO",
                25,
                "3001234567"
        );


        Usuario resultado = mapper.toUsuario(usuarioData);


        assertNotNull(resultado);
        assertAll(
                () -> assertEquals("usuario-1", resultado.getIdUsuario()),
                () -> assertEquals("Ana Pérez", resultado.getNombre()),
                () -> assertEquals("ana@correo.com", resultado.getCorreo()),
                () -> assertEquals("password-encriptado", resultado.getPassword()),
                () -> assertEquals("USUARIO", resultado.getRol()),
                () -> assertEquals(25, resultado.getEdad()),
                () -> assertEquals("3001234567", resultado.getNTelefono())
        );
    }

    @Test
    void toUsuario_cuandoRecibeNull_debeRetornarNull() {

        UsuarioData usuarioData = null;


        Usuario resultado = mapper.toUsuario(usuarioData);


        assertNull(resultado);
    }

    @Test
    void toUsuarioData_cuandoRecibeUsuario_debeMapearTodosLosCampos() {

        Usuario usuario = new Usuario(
                "usuario-2",
                "Carlos Ruiz",
                "carlos@correo.com",
                "otra-password-encriptada",
                "ADMINISTRADOR",
                30,
                "3119876543"
        );


        UsuarioData resultado = mapper.toUsuarioData(usuario);


        assertNotNull(resultado);
        assertAll(
                () -> assertEquals("usuario-2", resultado.getIdUsuario()),
                () -> assertEquals("Carlos Ruiz", resultado.getNombre()),
                () -> assertEquals("carlos@correo.com", resultado.getCorreo()),
                () -> assertEquals("otra-password-encriptada", resultado.getPassword()),
                () -> assertEquals("ADMINISTRADOR", resultado.getRol()),
                () -> assertEquals(30, resultado.getEdad()),
                () -> assertEquals("3119876543", resultado.getNTelefono())
        );
    }
}