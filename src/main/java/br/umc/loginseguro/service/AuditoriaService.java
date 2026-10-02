package br.umc.loginseguro.service;

import br.umc.loginseguro.model.RegistroAuditoria;
import br.umc.loginseguro.model.TipoEvento;
import br.umc.loginseguro.repository.AuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Grava e consulta o log de auditoria.
 * Falhas ao gravar o log nunca interrompem a ação do usuário: apenas geram erro no console.
 */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private static final int TAMANHO_MAXIMO = 200;

    private final AuditoriaRepository repositorio;

    public AuditoriaService(AuditoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void registrar(TipoEvento tipo, String usuario, String alvo, String detalhes) {
        try {
            repositorio.save(new RegistroAuditoria(
                    tipo, limitar(usuario), limitar(alvo), ipAtual(), limitar(detalhes)));
        } catch (RuntimeException e) {
            log.error("Não foi possível gravar o evento de auditoria {}", tipo, e);
        }
    }

    /** Últimos 200 eventos, do mais recente para o mais antigo (opcionalmente filtrados por tipo). */
    public List<RegistroAuditoria> ultimos(TipoEvento tipo) {
        return tipo == null
                ? repositorio.findTop200ByOrderByDataHoraDesc()
                : repositorio.findTop200ByTipoOrderByDataHoraDesc(tipo);
    }

    /** IP de quem fez a requisição atual (em localhost aparece como 0:0:0:0:0:0:0:1). */
    private String ipAtual() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getRemoteAddr();
        }
        return null; // ação fora de uma requisição (ex.: inicialização)
    }

    /** Evita que textos enormes (ex.: e-mail digitado no login) encham o banco. */
    private String limitar(String texto) {
        if (texto == null || texto.length() <= TAMANHO_MAXIMO) {
            return texto;
        }
        return texto.substring(0, TAMANHO_MAXIMO);
    }
}
