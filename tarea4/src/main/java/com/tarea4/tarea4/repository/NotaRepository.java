package com.tarea4.tarea4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tarea4.tarea4.model.Nota;

public interface NotaRepository extends JpaRepository<Nota, Integer> {
    @Query("select avg(n.nota) from Nota n where n.avistamiento.id = :avistamientoId")
    Double promedio(@Param("avistamientoId") Integer avistamientoId);
}
