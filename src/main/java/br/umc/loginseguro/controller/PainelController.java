package br.umc.loginseguro.controller;

import br.umc.loginseguro.model.TipoEvento;
import br.umc.loginseguro.security.UsuarioLogado;
import br.umc.loginseguro.service.AuditoriaService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Páginas gerais: início público, redirecionamento pós-login e acesso negado.
 */
@Controller
public class PainelController {

    private final AuditoriaService auditoria;

    public PainelController(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    /** Após o login, cada perfil vai para sua própria página inicial (definida em Role). */
    @GetMapping("/painel")
    public String painel(@AuthenticationPrincipal UsuarioLogado usuario) {
        return "redirect:" + usuario.getPerfil().getPaginaInicial();
    }

    /**
     * O Spring Security encaminha (forward) para cá quando um usuário logado
     * tenta abrir uma página sem permissão. A tentativa fica na auditoria.
     */
    @GetMapping("/acesso-negado")
    public String acessoNegado(@AuthenticationPrincipal UsuarioLogado usuario, HttpServletRequest requisicao) {
        Object paginaTentada = requisicao.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (usuario != null && paginaTentada != null) {
            auditoria.registrar(TipoEvento.ACESSO_NEGADO, usuario.getUsername(), usuario.getUsername(),
                    "Perfil " + usuario.getPerfil() + " tentou abrir " + paginaTentada);
        }
        return "erro/403";
    }
}
