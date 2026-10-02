package br.umc.loginseguro.controller;

import br.umc.loginseguro.dto.RedefinirSenhaForm;
import br.umc.loginseguro.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Telas "Esqueci minha senha" e "Criar nova senha". */
@Controller
public class RecuperacaoSenhaController {

    private static final String MENSAGEM_ENVIO =
            "Se o e-mail estiver cadastrado, você receberá um link para criar uma nova senha. "
          + "O link vale por 30 minutos.";
    private static final String MENSAGEM_LINK_INVALIDO =
            "Este link é inválido ou expirou. Peça um novo link abaixo.";

    private final RecuperacaoSenhaService recuperacao;

    public RecuperacaoSenhaController(RecuperacaoSenhaService recuperacao) {
        this.recuperacao = recuperacao;
    }

    @GetMapping("/esqueci-senha")
    public String formulario() {
        return "auth/esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String solicitar(@RequestParam(defaultValue = "") String email, RedirectAttributes redirecionamento) {
        if (StringUtils.hasText(email) && email.length() <= 120) {
            recuperacao.solicitar(email);
        }
        // Mesma resposta exista ou não a conta
        redirecionamento.addFlashAttribute("sucesso", MENSAGEM_ENVIO);
        return "redirect:/login";
    }

    @GetMapping("/redefinir-senha")
    public String formularioNovaSenha(@RequestParam(required = false) String token,
                                      Model model, RedirectAttributes redirecionamento) {
        if (!recuperacao.tokenValido(token)) {
            redirecionamento.addFlashAttribute("erro", MENSAGEM_LINK_INVALIDO);
            return "redirect:/esqueci-senha";
        }
        RedefinirSenhaForm form = new RedefinirSenhaForm();
        form.setToken(token);
        model.addAttribute("form", form);
        return "auth/redefinir-senha";
    }

    @PostMapping("/redefinir-senha")
    public String redefinir(@Valid @ModelAttribute("form") RedefinirSenhaForm form,
                            BindingResult resultado,
                            RedirectAttributes redirecionamento) {
        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmacaoSenha())) {
            resultado.rejectValue("confirmacaoSenha", "senhas.diferentes", "As senhas não conferem.");
        }
        if (resultado.hasErrors()) {
            return "auth/redefinir-senha";
        }
        if (!recuperacao.redefinir(form.getToken(), form.getSenha())) {
            redirecionamento.addFlashAttribute("erro", MENSAGEM_LINK_INVALIDO);
            return "redirect:/esqueci-senha";
        }
        redirecionamento.addFlashAttribute("sucesso", "Senha alterada. Entre com a nova senha.");
        return "redirect:/login";
    }
}
