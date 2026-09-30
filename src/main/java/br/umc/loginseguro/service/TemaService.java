package br.umc.loginseguro.service;

import br.umc.loginseguro.config.TemaProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Decide qual tema visual usar em cada requisição.
 * Ordem: tema escolhido pelo usuário (na sessão) > tema padrão da configuração.
 * Só aceita temas listados em app.tema.disponiveis (evita caminhos arbitrários).
 */
@Service
public class TemaService {

    public static final String ATRIBUTO_SESSAO = "temaEscolhido";
    private static final String TEMA_RESERVA = "padrao";

    private final TemaProperties propriedades;

    public TemaService(TemaProperties propriedades) {
        this.propriedades = propriedades;
    }

    public String temaAtual(HttpServletRequest requisicao) {
        // getSession(false): não cria sessão só para ler o tema
        HttpSession sessao = requisicao.getSession(false);
        if (sessao != null && sessao.getAttribute(ATRIBUTO_SESSAO) instanceof String escolhido
                && valido(escolhido)) {
            return escolhido;
        }
        return valido(propriedades.nome()) ? propriedades.nome() : TEMA_RESERVA;
    }

    public void escolher(String tema, HttpServletRequest requisicao) {
        if (valido(tema)) {
            requisicao.getSession(true).setAttribute(ATRIBUTO_SESSAO, tema);
        }
    }

    public boolean valido(String tema) {
        return tema != null && propriedades.disponiveis().contains(tema);
    }

    public List<String> disponiveis() {
        return propriedades.disponiveis();
    }

    public String nomeSistema() {
        return propriedades.nomeSistema();
    }
}
