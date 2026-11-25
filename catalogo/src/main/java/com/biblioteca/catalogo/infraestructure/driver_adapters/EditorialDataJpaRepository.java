package com.biblioteca.catalogo.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EditorialDataJpaRepository extends JpaRepository<EditorialData, String> {

    Optional<EditorialData> findFirstByNombreIgnoreCase(String nombre);

    List<EditorialData> findAllByOrderByNombreAsc();
}
