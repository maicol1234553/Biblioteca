package com.biblioteca.catalogo.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EjemplarDataJpaRepository extends JpaRepository<EjemplarData, String> {

    Optional<EjemplarData> findByCodigoInventario(String codigoInventario);

    List<EjemplarData> findByLibro_IdLibroOrderByCodigoInventarioAsc(String idLibro);

    List<EjemplarData> findByEstadoOrderByCodigoInventarioAsc(String estado);
}
