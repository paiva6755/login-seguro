package br.umc.loginseguro.controller;

import br.umc.loginseguro.dto.CadastroForm;
import br.umc.loginseguro.exception.EmailJaCadastradoException;
import br.umc.loginseguro.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Telas de login e cadastro.
 * O POST /login e o POST /logout são tratados pelo próprio Spring Security.
 */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(Authentication autenticacao) {
        if (estaLogado(autenticacao)) {
            return "redirect:/painel";
        }
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String formularioCadastro(Model model, Authentication autenticacao) {
        if (estaLogado(autenticacao)) {
            return "redirect:/painel";
        }
        model.addAttribute("form", new CadastroForm());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form,
                            BindingResult resultado,
                            RedirectAttributes redirecionamento) {

        // Validação que envolve dois campos
        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmacaoSenha())) {
            resultado.rejectValue("confirmacaoSenha", "senhas.diferentes", "As senhas não conferem.");
        }
        if (resultado.hasErrors()) {
            return "auth/cadastro"; // reexibe a tela com as mensagens de erro
        }

        try {
            usuarioService.cadastrar(form);
        } catch (EmailJaCadastradoException e) {
            resultado.rejectValue("email", "email.duplicado", e.getMessage());
            return "auth/cadastro";
        }

        redirecionamento.addFlashAttribute("sucesso", "Conta criada. Entre com seu e-mail e senha.");
        return "redirect:/login";
    }

    private boolean estaLogado(Authentication autenticacao) {
        return autenticacao != null
                && autenticacao.isAuthenticated()
                && !(autenticacao instanceof AnonymousAuthenticationToken);
    }
}
