package com.biblioteca.catalogo.infraestructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioRolJdbcAdapterTest {

    private static final String SQL =
            "select rol from usuario where id_usuario = ?";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UsuarioRolJdbcAdapter adapter;

    @Test
    void buscarRol_cuandoEncuentraRoles_debeRetornarElPrimero() {

        String idUsuario = "usuario-1";

        when(jdbcTemplate.query(
                eq(SQL),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq(idUsuario)
        )).thenReturn(List.of("ADMINISTRADOR", "USUARIO"));


        String resultado = adapter.buscarRol(idUsuario);


        assertEquals("ADMINISTRADOR", resultado);
        verify(jdbcTemplate).query(
                eq(SQL),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq(idUsuario)
        );
    }

    @Test
    void buscarRol_cuandoNoEncuentraRoles_debeRetornarNull() {

        String idUsuario = "usuario-inexistente";

        when(jdbcTemplate.query(
                eq(SQL),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq(idUsuario)
        )).thenReturn(List.of());


        String resultado = adapter.buscarRol(idUsuario);


        assertNull(resultado);
    }

    @Test
    void buscarRol_cuandoFallaElAccesoADatos_debeRetornarNull() {

        String idUsuario = "usuario-1";

        when(jdbcTemplate.query(
                eq(SQL),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq(idUsuario)
        )).thenThrow(new DataAccessResourceFailureException("Base de datos no disponible"));


        String resultado = adapter.buscarRol(idUsuario);


        assertNull(resultado);
    }

    @Test
    void buscarRol_debeLeerLaColumnaRolDelResultado() throws Exception {

        String idUsuario = "usuario-1";
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.getString("rol")).thenReturn("USUARIO");

        when(jdbcTemplate.query(
                eq(SQL),
                org.mockito.ArgumentMatchers.<RowMapper<String>>any(),
                eq(idUsuario)
        )).thenAnswer(invocation -> {
            RowMapper<String> rowMapper = invocation.getArgument(1);
            return List.of(rowMapper.mapRow(resultSet, 0));
        });


        String resultado = adapter.buscarRol(idUsuario);


        assertEquals("USUARIO", resultado);
        verify(resultSet).getString("rol");
    }
}