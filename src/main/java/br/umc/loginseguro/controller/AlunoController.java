package br.umc.loginseguro.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Área do aluno: página inicial do perfil aluno. */
@Controller
@RequestMapping("/aluno")
@PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR', 'ADMIN')")
public class AlunoController {

    @GetMapping
    public String painel() {
        return "aluno/painel";
    }
}
