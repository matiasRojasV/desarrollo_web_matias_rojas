package com.tarea4.tarea4.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "avistamiento")
public class Avistamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime fecha_hora;
    private String lugar;
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ave_id", nullable = false)
    private Ave ave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voluntario_id", nullable = false)
    private Voluntario voluntario;

    @OneToMany(mappedBy = "avistamiento", fetch = FetchType.LAZY)
    private List<Registro> registros = new ArrayList<>();

    @OneToMany(mappedBy = "avistamiento", fetch = FetchType.LAZY)
    private List<Comentario> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "avistamiento", fetch = FetchType.LAZY)
    private List<Nota> notas = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDateTime getFecha_hora() { return fecha_hora; }
    public void setFecha_hora(LocalDateTime fecha_hora) { this.fecha_hora = fecha_hora; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Ave getAve() { return ave; }
    public void setAve(Ave ave) { this.ave = ave; }
    public Voluntario getVoluntario() { return voluntario; }
    public void setVoluntario(Voluntario voluntario) { this.voluntario = voluntario; }
    public List<Registro> getRegistros() { return registros; }
    public List<Comentario> getComentarios() { return comentarios; }
    public List<Nota> getNotas() { return notas; }
}
