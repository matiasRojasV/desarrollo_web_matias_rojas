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
@Table(name = "registro")
public class Registro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String ruta_archivo;
    private String nombre_archivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avistamiento_id", nullable = false)
    private Avistamiento avistamiento;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRuta_archivo() { return ruta_archivo; }
    public void setRuta_archivo(String ruta_archivo) { this.ruta_archivo = ruta_archivo; }
    public String getNombre_archivo() { return nombre_archivo; }
    public void setNombre_archivo(String nombre_archivo) { this.nombre_archivo = nombre_archivo; }
    public Avistamiento getAvistamiento() { return avistamiento; }
    public void setAvistamiento(Avistamiento avistamiento) { this.avistamiento = avistamiento; }
}
