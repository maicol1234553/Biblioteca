package com.biblioteca.catalogo.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaDataJpaRepository extends JpaRepository<CategoriaData, String> {

    Optional<CategoriaData> findFirstByNombreIgnoreCase(String nombre);

    List<CategoriaData> findAllByOrderByNombreAsc();
}
