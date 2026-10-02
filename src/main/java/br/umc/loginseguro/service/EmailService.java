package br.umc.loginseguro.service;

import br.umc.loginseguro.config.TemaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Envio de e-mails do sistema.
 *
 * Modo de teste: se MAIL_HOST não estiver configurado, o e-mail não é enviado
 * e o link aparece no terminal. Útil para desenvolver sem servidor de e-mail.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final TemaProperties tema;
    private final String host;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        TemaProperties tema,
                        @Value("${spring.mail.host:}") String host,
                        @Value("${app.email.remetente:}") String remetente) {
        this.mailSender = mailSender;
        this.tema = tema;
        this.host = host;
        this.remetente = remetente;
    }

    /** @return true se o e-mail foi entregue ao servidor SMTP. */
    public boolean enviarRecuperacaoSenha(String para, String nome, String link, Duration validade) {
        String assunto = tema.nomeSistema() + ": redefinição de senha";
        String texto = "Olá, " + nome + ".\n\n"
                + "Recebemos um pedido para redefinir a senha da sua conta.\n"
                + "Para criar uma nova senha, acesse o link abaixo (válido por "
                + validade.toMinutes() + " minutos e para um único uso):\n\n"
                + link + "\n\n"
                + "Se você não fez esse pedido, ignore este e-mail. Sua senha atual continua valendo.\n";

        JavaMailSender sender = mailSender.getIfAvailable();
        if (!StringUtils.hasText(host) || sender == null) {
            log.warn("MODO DE TESTE (MAIL_HOST vazio): e-mail não enviado. Link de redefinição para {}: {}",
                    para, link);
            return false;
        }

        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            if (StringUtils.hasText(remetente)) {
                mensagem.setFrom(remetente);
            }
            mensagem.setTo(para);
            mensagem.setSubject(assunto);
            mensagem.setText(texto);
            sender.send(mensagem);
            log.info("E-mail de redefinição enviado para {}", para);
            return true;
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
            return false;
        }
    }
}
