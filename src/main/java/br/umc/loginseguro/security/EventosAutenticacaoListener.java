package br.umc.loginseguro.security;

import br.umc.loginseguro.model.TipoEvento;
import br.umc.loginseguro.service.AuditoriaService;
import br.umc.loginseguro.service.UsuarioService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Escuta eventos publicados pelo Spring Security para:
 * - contar senhas erradas e bloquear a conta temporariamente (força bruta);
 * - registrar login, falha de login e logout no log de auditoria.
 */
@Component
public class EventosAutenticacaoListener {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoria;

    public EventosAutenticacaoListener(UsuarioService usuarioService, AuditoriaService auditoria) {
        this.usuarioService = usuarioService;
        this.auditoria = auditoria;
    }

    @EventListener
    public void aoFalhar(AbstractAuthenticationFailureEvent evento) {
        String email = UsuarioService.normalizarEmail(evento.getAuthentication().getName());
        String motivo;
        if (evento instanceof AuthenticationFailureBadCredentialsEvent) {
            motivo = "E-mail ou senha incorretos";
        } else if (evento instanceof AuthenticationFailureLockedEvent) {
            motivo = "Tentativa em conta bloqueada temporariamente";
        } else if (evento instanceof AuthenticationFailureDisabledEvent) {
            motivo = "Tentativa em conta desativada";
        } else {
            motivo = evento.getException().getClass().getSimpleName();
        }
        auditoria.registrar(TipoEvento.LOGIN_FALHA, email, email, motivo);

        if (evento instanceof AuthenticationFailureBadCredentialsEvent) {
            usuarioService.registrarFalhaLogin(email);
        }
    }

    @EventListener
    public void aoAutenticar(AuthenticationSuccessEvent evento) {
        String email = evento.getAuthentication().getName();
        usuarioService.registrarLoginBemSucedido(email);
        auditoria.registrar(TipoEvento.LOGIN_SUCESSO, email, email, null);
    }

    @EventListener
    public void aoSair(LogoutSuccessEvent evento) {
        String email = evento.getAuthentication().getName();
        auditoria.registrar(TipoEvento.LOGOUT, email, email, null);
    }
}
