package com.tarea4.tarea4.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    @ModelAttribute
    public void addSessionUser(Model model, HttpSession session) {
        model.addAttribute("voluntarioId", session.getAttribute("voluntario_id"));
        model.addAttribute("voluntarioNombre", session.getAttribute("voluntario_nombre"));
    }
}
