package com.tarea4.tarea4.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tarea4.tarea4.model.Avistamiento;

public interface AvistamientoRepository extends JpaRepository<Avistamiento, Integer> {
    @EntityGraph(attributePaths = {"ave", "voluntario"})
    List<Avistamiento> findTop2ByOrderByIdDesc();

    @EntityGraph(attributePaths = {"ave", "voluntario"})
    @Query("select a from Avistamiento a order by a.id desc")
    List<Avistamiento> findLatest(Pageable pageable);

    @EntityGraph(attributePaths = {"ave", "voluntario"})
    @Query("select a from Avistamiento a")
    Page<Avistamiento> findAllWithRelations(Pageable pageable);

    @EntityGraph(attributePaths = {"ave", "voluntario"})
    @Query("""
        select distinct a from Avistamiento a
        join a.ave ave
        join a.voluntario voluntario
        join voluntario.comuna comuna
        where lower(ave.nombre) like lower(concat('%', :texto, '%'))
           or lower(voluntario.nombre) like lower(concat('%', :texto, '%'))
           or lower(comuna.nombre) like lower(concat('%', :texto, '%'))
        order by a.fecha_hora desc
        """)
    List<Avistamiento> buscar(@Param("texto") String texto);

    @Query("select function('date', a.fecha_hora), count(a) from Avistamiento a group by function('date', a.fecha_hora) order by function('date', a.fecha_hora)")
    List<Object[]> contarPorDia();

    @Query("select a.ave.nombre, count(a) from Avistamiento a group by a.ave.nombre")
    List<Object[]> contarPorAve();
}
