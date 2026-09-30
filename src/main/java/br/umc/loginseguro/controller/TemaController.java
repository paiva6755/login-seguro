package br.umc.loginseguro.controller;

import br.umc.loginseguro.service.TemaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Troca o tema visual da sessão atual (guardado na sessão do MongoDB). */
@Controller
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @PostMapping("/tema")
    public String trocarTema(@RequestParam String nome,
                             @RequestParam(defaultValue = "/") String voltarPara,
                             HttpServletRequest requisicao) {
        temaService.escolher(nome, requisicao);
        // Só redireciona para caminhos internos (evita "open redirect")
        boolean caminhoInterno = voltarPara.startsWith("/")
                && !voltarPara.startsWith("//")
                && !voltarPara.contains("\\");
        return "redirect:" + (caminhoInterno ? voltarPara : "/");
    }
}
