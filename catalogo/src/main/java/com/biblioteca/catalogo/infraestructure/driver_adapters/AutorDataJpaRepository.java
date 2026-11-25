package com.biblioteca.catalogo.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutorDataJpaRepository extends JpaRepository<AutorData, String> {

    Optional<AutorData> findFirstByNombreIgnoreCase(String nombre);

    List<AutorData> findAllByOrderByNombreAsc();
}
