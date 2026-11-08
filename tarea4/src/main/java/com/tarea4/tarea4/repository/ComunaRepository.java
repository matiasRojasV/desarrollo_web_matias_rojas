package com.tarea4.tarea4.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tarea4.tarea4.model.Comuna;

public interface ComunaRepository extends JpaRepository<Comuna, Integer> {
    List<Comuna> findByRegionIdOrderByNombre(Integer regionId);
}
