package com.tarea4.tarea4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tarea4.tarea4.model.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
}
