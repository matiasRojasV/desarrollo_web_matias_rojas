package com.tarea4.tarea4.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "nota")
public class Nota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer nota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avistamiento_id", nullable = false)
    private Avistamiento avistamiento;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }
    public Avistamiento getAvistamiento() { return avistamiento; }
    public void setAvistamiento(Avistamiento avistamiento) { this.avistamiento = avistamiento; }
}
