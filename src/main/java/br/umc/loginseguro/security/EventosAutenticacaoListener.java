package br.umc.loginseguro.security;

import br.umc.loginseguro.service.UsuarioService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Escuta eventos de login publicados pelo Spring Security para:
 * - contar senhas erradas e bloquear a conta temporariamente (força bruta);
 * - registrar a data do último login e zerar o contador após sucesso.
 */
@Component
public class EventosAutenticacaoListener {

    private final UsuarioService usuarioService;

    public EventosAutenticacaoListener(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @EventListener
    public void aoFalhar(AuthenticationFailureBadCredentialsEvent evento) {
        usuarioService.registrarFalhaLogin(evento.getAuthentication().getName());
    }

    @EventListener
    public void aoAutenticar(AuthenticationSuccessEvent evento) {
        usuarioService.registrarLoginBemSucedido(evento.getAuthentication().getName());
    }
}
