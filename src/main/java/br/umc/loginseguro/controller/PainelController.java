package br.umc.loginseguro.controller;

import br.umc.loginseguro.security.UsuarioLogado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Páginas gerais: início público, redirecionamento pós-login e acesso negado.
 */
@Controller
public class PainelController {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    /** Após o login, cada perfil vai para sua própria página inicial (definida em Role). */
    @GetMapping("/painel")
    public String painel(@AuthenticationPrincipal UsuarioLogado usuario) {
        return "redirect:" + usuario.getPerfil().getPaginaInicial();
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "erro/403";
    }
}
