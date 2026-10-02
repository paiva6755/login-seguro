package br.umc.loginseguro.controller;

import br.umc.loginseguro.exception.OperacaoNaoPermitidaException;
import br.umc.loginseguro.model.Role;
import br.umc.loginseguro.model.TipoEvento;
import br.umc.loginseguro.service.AuditoriaService;
import br.umc.loginseguro.security.UsuarioLogado;
import br.umc.loginseguro.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Área administrativa: listar usuários, trocar perfil, ativar/desativar
 * e consultar o log de auditoria.
 * Protegida por URL (SecurityConfig) E por @PreAuthorize (defesa em camadas).
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoria;

    public AdminController(UsuarioService usuarioService, AuditoriaService auditoria) {
        this.usuarioService = usuarioService;
        this.auditoria = auditoria;
    }

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("perfis", Role.values());
        return "admin/painel";
    }

    @PostMapping("/usuarios/{id}/perfil")
    public String alterarPerfil(@PathVariable String id,
                                @RequestParam Role perfil,
                                @AuthenticationPrincipal UsuarioLogado logado,
                                RedirectAttributes redirecionamento) {
        try {
            usuarioService.alterarPerfil(id, perfil, logado.getUsername());
            redirecionamento.addFlashAttribute("sucesso",
                    "Perfil alterado. A mudança vale a partir do próximo login do usuário.");
        } catch (OperacaoNaoPermitidaException e) {
            redirecionamento.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/usuarios/{id}/status")
    public String alternarStatus(@PathVariable String id,
                                 @AuthenticationPrincipal UsuarioLogado logado,
                                 RedirectAttributes redirecionamento) {
        try {
            usuarioService.alternarAtivo(id, logado.getUsername());
            redirecionamento.addFlashAttribute("sucesso", "Status da conta atualizado.");
        } catch (OperacaoNaoPermitidaException e) {
            redirecionamento.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin";
    }

    /** Log de auditoria: últimos 200 eventos, com filtro opcional por tipo. */
    @GetMapping("/auditoria")
    public String auditoria(@RequestParam(required = false) TipoEvento tipo, Model model) {
        model.addAttribute("registros", auditoria.ultimos(tipo));
        model.addAttribute("tipos", TipoEvento.values());
        model.addAttribute("tipoSelecionado", tipo);
        return "admin/auditoria";
    }
}
