package com.biblioteca.catalogo.infraestructure.driver_adapters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroDataJpaRepository extends JpaRepository<LibroData, String> {

    Optional<LibroData> findByIsbn(String isbn);

    List<LibroData> findAllByOrderByTituloAsc();

    @Query("""
            select distinct l from LibroData l
            where (cast(:texto as string) is null
                   or lower(l.titulo) like lower(concat('%', cast(:texto as string), '%'))
                   or lower(coalesce(l.isbn, '')) like lower(concat('%', cast(:texto as string), '%'))
                   or exists (
                       select 1 from LibroData lx join lx.autores ax
                       where lx.idLibro = l.idLibro
                         and (lower(ax.nombre) like lower(concat('%', cast(:texto as string), '%'))
                              or lower(coalesce(ax.apellido, '')) like lower(concat('%', cast(:texto as string), '%')))))
              and (cast(:idAutor as string) is null or exists (
                       select 1 from LibroData la join la.autores aa
                       where la.idLibro = l.idLibro and aa.idAutor = cast(:idAutor as string)))
              and (cast(:idCategoria as string) is null or exists (
                       select 1 from LibroData lc join lc.categorias cc
                       where lc.idLibro = l.idLibro and cc.idCategoria = cast(:idCategoria as string)))
              and (cast(:idEditorial as string) is null or l.editorial.idEditorial = cast(:idEditorial as string))
              and (cast(:idioma as string) is null or lower(l.idioma) = lower(cast(:idioma as string)))
            order by l.titulo asc
            """)
    List<LibroData> buscar(
            @Param("texto") String texto,
            @Param("idAutor") String idAutor,
            @Param("idCategoria") String idCategoria,
            @Param("idEditorial") String idEditorial,
            @Param("idioma") String idioma);
}
