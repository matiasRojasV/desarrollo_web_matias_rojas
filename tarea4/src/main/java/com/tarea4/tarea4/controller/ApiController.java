package com.tarea4.tarea4.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.JsonNode;
import com.tarea4.tarea4.model.Avistamiento;
import com.tarea4.tarea4.model.Comentario;
import com.tarea4.tarea4.service.AvesService;

@Controller
public class ApiController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter COMMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final AvesService avesService;

    public ApiController(AvesService avesService) {
        this.avesService = avesService;
    }

    @GetMapping("/get_comunas/{regionId}")
    @ResponseBody
    public Map<String, Object> comunas(@PathVariable Integer regionId) {
        List<Map<String, Object>> comunas = avesService.comunas(regionId).stream()
            .map(comuna -> Map.<String, Object>of("id", comuna.getId(), "nombre", comuna.getNombre()))
            .toList();
        return Map.of("comunas", comunas);
    }

    @GetMapping("/api/avistamientos/buscar")
    @ResponseBody
    public Map<String, Object> buscar(@RequestParam(defaultValue = "") String q) {
        String texto = q.trim();
        List<Map<String, Object>> resultados = texto.isEmpty() ? List.of() :
            avesService.buscar(texto).stream().map(this::mapearResultado).toList();
        return Map.of("avistamientos", resultados);
    }

    @PostMapping("/api/notas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarNota(@RequestBody JsonNode body) {
        JsonNode id = body.path("avistamiento_id");
        JsonNode nota = body.path("nota");
        if (!id.isIntegralNumber() || !id.canConvertToInt() || !nota.isIntegralNumber() ||
            !nota.canConvertToInt() || nota.intValue() < 1 || nota.intValue() > 7) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "mensaje", "La nota debe ser un número entero entre 1 y 7."
            ));
        }
        try {
            Double promedio = avesService.agregarNota(id.intValue(), nota.intValue());
            return ResponseEntity.ok(Map.of("status", "exito", "promedio", promedio));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error", "mensaje", exception.getMessage()
            ));
        }
    }

    @PostMapping("/api/comentarios")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarComentario(@RequestBody JsonNode body) {
        String nombre = body.path("nombre").asText("").trim();
        String texto = body.path("texto").asText("").trim();
        Integer avistamientoId = parseInteger(body.path("avistamiento_id"));
        if (nombre.length() < 3 || nombre.length() > 50 || texto.length() < 5 ||
            texto.length() > 300 || avistamientoId == null || avistamientoId < 1) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "mensajes", List.of("Ingrese un nombre de 3 a 50 caracteres y un comentario de 5 a 300 caracteres.")
            ));
        }
        try {
            Comentario comentario = avesService.agregarComentario(nombre, texto, avistamientoId);
            return ResponseEntity.ok(Map.of(
                "status", "exito",
                "mensaje", "Comentario agregado exitosamente.",
                "comentario", Map.of(
                    "nombre", comentario.getNombre(),
                    "texto", comentario.getTexto(),
                    "fecha", comentario.getFecha().format(COMMENT_DATE_FORMAT)
                )
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error", "mensajes", List.of(exception.getMessage())
            ));
        }
    }

    private Integer parseInteger(JsonNode value) {
        if (value.isIntegralNumber() && value.canConvertToInt()) {
            return value.intValue();
        }
        if (!value.isTextual() || !value.asText().matches("\\d+")) {
            return null;
        }
        try {
            return Integer.valueOf(value.asText());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @GetMapping("/api/estadisticas")
    @ResponseBody
    public Map<String, Object> estadisticas() {
        return avesService.estadisticas();
    }

    private Map<String, Object> mapearResultado(Avistamiento avistamiento) {
        Double promedio = avesService.promedio(avistamiento.getId());
        return Map.of(
            "id", avistamiento.getId(),
            "ave", avistamiento.getAve().getNombre(),
            "voluntario", avistamiento.getVoluntario().getNombre(),
            "comuna", avistamiento.getVoluntario().getComuna().getNombre(),
            "fecha", avistamiento.getFecha_hora().format(DATE_FORMAT),
            "promedio", promedio == null ? "-" : promedio,
            "detalle_url", "/avistamiento/" + avistamiento.getId()
        );
    }
}
