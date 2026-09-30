package br.umc.loginseguro.controller;

import br.umc.loginseguro.model.Role;
import br.umc.loginseguro.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Área do professor: exemplo de página intermediária (professor e admin). */
@Controller
@RequestMapping("/professor")
@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
public class ProfessorController {

    private final UsuarioService usuarioService;

    public ProfessorController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("alunos", usuarioService.listarPorPerfil(Role.ALUNO));
        return "professor/painel";
    }
}
