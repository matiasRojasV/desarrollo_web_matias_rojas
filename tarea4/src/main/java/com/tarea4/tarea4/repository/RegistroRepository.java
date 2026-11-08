package com.tarea4.tarea4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tarea4.tarea4.model.Registro;

public interface RegistroRepository extends JpaRepository<Registro, Integer> {
}
