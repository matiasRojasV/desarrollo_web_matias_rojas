package com.tarea4.tarea4.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.tarea4.tarea4.model.Avistamiento;
import com.tarea4.tarea4.model.Voluntario;
import com.tarea4.tarea4.service.AvesService;

@Controller
public class MainController {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?56)?9\\d{8}$");

    private final AvesService avesService;
    private final Path uploadDirectory;

    public MainController(AvesService avesService, @Value("${app.upload-dir}") String uploadDirectory) {
        this.avesService = avesService;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("ultimos_avistamientos", avesService.ultimos());
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String alerta, Model model) {
        model.addAttribute("regiones", avesService.regiones());
        if ("requerido".equals(alerta)) {
            model.addAttribute("errores", List.of(
                "Debes registrarte o iniciar sesión para poder informar un avistamiento."
            ));
        }
        return "login";
    }

    @PostMapping("/login")
    public String registrarVoluntario(
        @RequestParam String nombre,
        @RequestParam String email,
        @RequestParam String celular,
        @RequestParam Integer region,
        @RequestParam Integer comuna,
        HttpSession session,
        Model model
    ) {
        String nombreLimpio = nombre.trim();
        String emailLimpio = email.trim();
        String celularLimpio = celular.trim();
        List<String> errores = new ArrayList<>();

        if (nombreLimpio.length() < 3) errores.add("El nombre completo debe tener al menos 3 caracteres.");
        if (!EMAIL_PATTERN.matcher(emailLimpio).matches()) errores.add("Debe ingresar un correo electrónico válido.");
        if (!PHONE_PATTERN.matcher(celularLimpio).matches()) {
            errores.add("El celular debe ser un número válido de Chile (ej: +56912345678 o 912345678).");
        }
        if (region == null || comuna == null ||
            avesService.comunas(region).stream().noneMatch(item -> item.getId().equals(comuna))) {
            errores.add("La región o comuna seleccionada no es válida.");
        }
        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("regiones", avesService.regiones());
            return "login";
        }

        try {
            Voluntario voluntario = avesService.crearVoluntario(nombreLimpio, emailLimpio, celularLimpio, comuna);
            session.setAttribute("voluntario_id", voluntario.getId());
            session.setAttribute("voluntario_nombre", voluntario.getNombre());
            model.addAttribute("nombre", voluntario.getNombre());
            return "exitoLogin";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errores", List.of(exception.getMessage()));
            model.addAttribute("regiones", avesService.regiones());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/avistamiento")
    public String formularioAvistamiento(HttpSession session, Model model) {
        if (session.getAttribute("voluntario_id") == null) return "redirect:/login?alerta=requerido";
        model.addAttribute("aves", avesService.aves());
        return "avistamiento";
    }

    @PostMapping("/avistamiento")
    public String crearAvistamiento(
        @RequestParam Integer ave_id,
        @RequestParam String lugar,
        @RequestParam(required = false) String descripcion,
        @RequestParam String fecha,
        @RequestParam String hora,
        @RequestParam(name = "multimedia") List<MultipartFile> archivos,
        HttpSession session,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Object voluntarioId = session.getAttribute("voluntario_id");
        if (!(voluntarioId instanceof Integer id)) return "redirect:/login?alerta=requerido";

        List<String> errores = new ArrayList<>();
        LocalDateTime fechaHora = null;
        try {
            fechaHora = LocalDateTime.of(LocalDate.parse(fecha), LocalTime.parse(hora));
        } catch (DateTimeParseException exception) {
            errores.add("El formato de fecha u hora no es válido.");
        }
        if (lugar.trim().isEmpty()) errores.add("El lugar del avistamiento es obligatorio.");
        List<MultipartFile> archivosValidos = archivos.stream().filter(file -> !file.isEmpty()).toList();
        if (archivosValidos.isEmpty()) errores.add("Debe adjuntar al menos una foto o video del avistamiento.");

        if (!errores.isEmpty()) {
            model.addAttribute("errores", errores);
            model.addAttribute("aves", avesService.aves());
            return "avistamiento";
        }

        List<Path> archivosGuardados = new ArrayList<>();
        try {
            Files.createDirectories(uploadDirectory);
            List<String> nombres = new ArrayList<>();
            for (MultipartFile archivo : archivosValidos) {
                String tipo = archivo.getContentType();
                if (tipo == null || !(tipo.startsWith("image/") || tipo.startsWith("video/"))) {
                    throw new IllegalArgumentException("Solo se permiten archivos de imagen o video.");
                }
                String nombreOriginal = StringUtils.getFilename(StringUtils.cleanPath(archivo.getOriginalFilename()));
                String extension = nombreOriginal != null && nombreOriginal.contains(".")
                    ? nombreOriginal.substring(nombreOriginal.lastIndexOf('.')).replaceAll("[^a-zA-Z0-9.]", "")
                    : "";
                String nombreGuardado = UUID.randomUUID() + extension;
                Path destino = uploadDirectory.resolve(nombreGuardado).normalize();
                if (!destino.startsWith(uploadDirectory)) throw new IllegalArgumentException("Nombre de archivo no válido.");
                archivo.transferTo(destino);
                archivosGuardados.add(destino);
                nombres.add(nombreGuardado);
            }

            avesService.crearAvistamiento(
                fechaHora, lugar.trim(), descripcion == null ? "" : descripcion.trim(),
                ave_id, id, nombres
            );
            redirectAttributes.addFlashAttribute(
                "mensajeExito", "¡El avistamiento se ha registrado exitosamente!"
            );
            return "redirect:/";
        } catch (IOException | IllegalArgumentException exception) {
            eliminarArchivos(archivosGuardados);
            model.addAttribute("errores", List.of(exception.getMessage()));
            model.addAttribute("aves", avesService.aves());
            return "avistamiento";
        } catch (RuntimeException exception) {
            eliminarArchivos(archivosGuardados);
            model.addAttribute("errores", List.of("Error interno al guardar el avistamiento en la base de datos."));
            model.addAttribute("aves", avesService.aves());
            return "avistamiento";
        }
    }

    @GetMapping("/avistamiento/{id}")
    public String detalleAvistamiento(@PathVariable Integer id, Model model) {
        Avistamiento avistamiento = avesService.avistamiento(id);
        if (avistamiento == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("avistamiento", avistamiento);
        return "detalleAvistamiento";
    }

    @GetMapping("/listAvistamientos")
    public String listAvistamientos(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "fecha-desc") String orden,
        Model model
    ) {
        Page<Avistamiento> avistamientos = avesService.avistamientos(page, orden);
        model.addAttribute("avistamientos", avistamientos);
        model.addAttribute("orden_actual", orden);
        return "listAvistamientos";
    }

    @GetMapping("/buscar")
    public String buscar() { return "buscar"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    private void eliminarArchivos(List<Path> archivos) {
        for (Path archivo : archivos) {
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException exception) {
                throw new IllegalStateException("No se pudo limpiar un archivo temporal.", exception);
            }
        }
    }
}
