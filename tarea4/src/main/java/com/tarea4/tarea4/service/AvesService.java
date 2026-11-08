package com.tarea4.tarea4.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tarea4.tarea4.model.Ave;
import com.tarea4.tarea4.model.Avistamiento;
import com.tarea4.tarea4.model.Comentario;
import com.tarea4.tarea4.model.Comuna;
import com.tarea4.tarea4.model.Nota;
import com.tarea4.tarea4.model.Region;
import com.tarea4.tarea4.model.Registro;
import com.tarea4.tarea4.model.Voluntario;
import com.tarea4.tarea4.repository.AveRepository;
import com.tarea4.tarea4.repository.AvistamientoRepository;
import com.tarea4.tarea4.repository.ComentarioRepository;
import com.tarea4.tarea4.repository.ComunaRepository;
import com.tarea4.tarea4.repository.NotaRepository;
import com.tarea4.tarea4.repository.RegionRepository;
import com.tarea4.tarea4.repository.RegistroRepository;
import com.tarea4.tarea4.repository.VoluntarioRepository;
import com.tarea4.tarea4.repository.VoluntarioStatsRepository;

@Service
public class AvesService {
    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;
    private final AveRepository aveRepository;
    private final VoluntarioRepository voluntarioRepository;
    private final AvistamientoRepository avistamientoRepository;
    private final RegistroRepository registroRepository;
    private final ComentarioRepository comentarioRepository;
    private final NotaRepository notaRepository;
    private final VoluntarioStatsRepository voluntarioStatsRepository;

    public AvesService(
        RegionRepository regionRepository,
        ComunaRepository comunaRepository,
        AveRepository aveRepository,
        VoluntarioRepository voluntarioRepository,
        AvistamientoRepository avistamientoRepository,
        RegistroRepository registroRepository,
        ComentarioRepository comentarioRepository,
        NotaRepository notaRepository,
        VoluntarioStatsRepository voluntarioStatsRepository
    ) {
        this.regionRepository = regionRepository;
        this.comunaRepository = comunaRepository;
        this.aveRepository = aveRepository;
        this.voluntarioRepository = voluntarioRepository;
        this.avistamientoRepository = avistamientoRepository;
        this.registroRepository = registroRepository;
        this.comentarioRepository = comentarioRepository;
        this.notaRepository = notaRepository;
        this.voluntarioStatsRepository = voluntarioStatsRepository;
    }

    @Transactional(readOnly = true)
    public List<Region> regiones() { return regionRepository.findAll(Sort.by("nombre")); }

    @Transactional(readOnly = true)
    public List<Comuna> comunas(Integer regionId) { return comunaRepository.findByRegionIdOrderByNombre(regionId); }

    @Transactional(readOnly = true)
    public List<Ave> aves() { return aveRepository.findAll(Sort.by("nombre")); }

    @Transactional(readOnly = true)
    public List<Avistamiento> ultimos() {
        return avistamientoRepository.findLatest(PageRequest.of(0, 2));
    }

    @Transactional(readOnly = true)
    public Page<Avistamiento> avistamientos(int pagina, String orden) {
        Sort sort = switch (orden) {
            case "fecha-asc" -> Sort.by("fecha_hora").ascending();
            case "lugar" -> Sort.by("lugar").ascending();
            case "nombre" -> Sort.by("ave.nombre").ascending();
            default -> Sort.by("fecha_hora").descending();
        };
        Pageable pageable = PageRequest.of(Math.max(0, pagina - 1), 4, sort);
        return avistamientoRepository.findAllWithRelations(pageable);
    }

    @Transactional(readOnly = true)
    public Avistamiento avistamiento(Integer id) {
        return avistamientoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Voluntario crearVoluntario(String nombre, String email, String telefono, Integer comunaId) {
        Comuna comuna = comunaRepository.findById(comunaId)
            .orElseThrow(() -> new IllegalArgumentException("La comuna seleccionada no es válida."));
        Voluntario voluntario = new Voluntario();
        voluntario.setNombre(nombre);
        voluntario.setEmail(email);
        voluntario.setTelefono(telefono);
        voluntario.setFecha_registro(LocalDateTime.now());
        voluntario.setComuna(comuna);
        return voluntarioRepository.save(voluntario);
    }

    @Transactional
    public Avistamiento crearAvistamiento(
        LocalDateTime fechaHora, String lugar, String descripcion, Integer aveId,
        Integer voluntarioId, List<String> nombresArchivos
    ) {
        Ave ave = aveRepository.findById(aveId)
            .orElseThrow(() -> new IllegalArgumentException("El ave seleccionada no es válida."));
        Voluntario voluntario = voluntarioRepository.findById(voluntarioId)
            .orElseThrow(() -> new IllegalArgumentException("La sesión de voluntario ya no es válida."));
        Avistamiento avistamiento = new Avistamiento();
        avistamiento.setFecha_hora(fechaHora);
        avistamiento.setLugar(lugar);
        avistamiento.setDescripcion(descripcion);
        avistamiento.setAve(ave);
        avistamiento.setVoluntario(voluntario);
        avistamiento = avistamientoRepository.save(avistamiento);

        for (String nombre : nombresArchivos) {
            Registro registro = new Registro();
            registro.setNombre_archivo(nombre);
            registro.setRuta_archivo("uploads/" + nombre);
            registro.setAvistamiento(avistamiento);
            registroRepository.save(registro);
        }
        return avistamiento;
    }

    @Transactional(readOnly = true)
    public List<Avistamiento> buscar(String texto) { return avistamientoRepository.buscar(texto); }

    @Transactional(readOnly = true)
    public Double promedio(Integer avistamientoId) { return notaRepository.promedio(avistamientoId); }

    @Transactional
    public Double agregarNota(Integer avistamientoId, Integer valor) {
        Avistamiento avistamiento = avistamientoRepository.findById(avistamientoId)
            .orElseThrow(() -> new IllegalArgumentException("El avistamiento no existe."));
        Nota nota = new Nota();
        nota.setAvistamiento(avistamiento);
        nota.setNota(valor);
        notaRepository.save(nota);
        return notaRepository.promedio(avistamientoId);
    }

    @Transactional
    public Comentario agregarComentario(String nombre, String texto, Integer avistamientoId) {
        Avistamiento avistamiento = avistamientoRepository.findById(avistamientoId)
            .orElseThrow(() -> new IllegalArgumentException("El avistamiento no existe."));
        Comentario comentario = new Comentario();
        comentario.setNombre(nombre);
        comentario.setTexto(texto);
        comentario.setFecha(LocalDateTime.now());
        comentario.setAvistamiento(avistamiento);
        return comentarioRepository.save(comentario);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> estadisticas() {
        List<Object[]> dias = avistamientoRepository.contarPorDia();
        List<Object[]> aves = avistamientoRepository.contarPorAve();
        List<Object[]> comunas = voluntarioStatsRepository.contarPorComuna();
        return Map.of(
            "dias", Map.of(
                "labels", dias.stream().map(row -> row[0].toString()).toList(),
                "valores", dias.stream().map(row -> ((Number) row[1]).longValue()).toList()
            ),
            "aves", Map.of(
                "labels", aves.stream().map(row -> row[0].toString()).toList(),
                "valores", aves.stream().map(row -> ((Number) row[1]).longValue()).toList()
            ),
            "comunas", Map.of(
                "labels", comunas.stream().map(row -> row[0].toString()).toList(),
                "valores", comunas.stream().map(row -> ((Number) row[1]).longValue()).toList()
            )
        );
    }
}
