package com.biblioteca.auth.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioDataJpaRepository extends JpaRepository<UsuarioData, String> {

    Optional<UsuarioData> findByCorreo(String correo);

    @Query("select u.rol, count(u) from UsuarioData u group by u.rol")
    List<Object[]> contarUsuariosPorRol();

    @Query("select avg(u.edad) from UsuarioData u")
    Double promedioEdad();
}
