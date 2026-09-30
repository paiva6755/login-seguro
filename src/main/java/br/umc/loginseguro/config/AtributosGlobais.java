package br.umc.loginseguro.config;

import br.umc.loginseguro.service.TemaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Disponibiliza em TODAS as telas os dados de aparência (tema, nome do
 * sistema). Assim nenhum controller precisa se preocupar com visual.
 */
@ControllerAdvice
public class AtributosGlobais {

    private final TemaService temaService;

    public AtributosGlobais(TemaService temaService) {
        this.temaService = temaService;
    }

    @ModelAttribute
    public void adicionar(Model model, HttpServletRequest requisicao) {
        model.addAttribute("tema", temaService.temaAtual(requisicao));
        model.addAttribute("temasDisponiveis", temaService.disponiveis());
        model.addAttribute("nomeSistema", temaService.nomeSistema());
        model.addAttribute("caminhoAtual", requisicao.getRequestURI());
    }
}
