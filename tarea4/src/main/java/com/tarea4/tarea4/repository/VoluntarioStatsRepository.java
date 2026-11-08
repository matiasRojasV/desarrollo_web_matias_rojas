package com.tarea4.tarea4.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.tarea4.tarea4.model.Voluntario;

public interface VoluntarioStatsRepository extends JpaRepository<Voluntario, Integer> {
    @Query("select v.comuna.nombre, count(v) from Voluntario v group by v.comuna.nombre")
    List<Object[]> contarPorComuna();
}
