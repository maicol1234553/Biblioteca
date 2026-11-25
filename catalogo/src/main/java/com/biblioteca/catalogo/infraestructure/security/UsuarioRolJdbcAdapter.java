package com.biblioteca.catalogo.infraestructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Lee el rol del usuario desde la tabla usuario que administra el
 * microservicio auth. Al compartir la misma base de datos no se duplica
 * la informacion de identidad: el catalogo solo consulta el rol.
 */
@Repository
@RequiredArgsConstructor
public class UsuarioRolJdbcAdapter {

    private final JdbcTemplate jdbcTemplate;

    public String buscarRol(String idUsuario) {
        try {
            List<String> roles = jdbcTemplate.query(
                    "select rol from usuario where id_usuario = ?",
                    (resultSet, rowNum) -> resultSet.getString("rol"),
                    idUsuario);

            return roles.isEmpty() ? null : roles.get(0);
        } catch (DataAccessException exception) {
            return null;
        }
    }
}
