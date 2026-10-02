package br.umc.loginseguro.service;

import br.umc.loginseguro.model.TipoEvento;
import br.umc.loginseguro.model.TokenRecuperacao;
import br.umc.loginseguro.model.Usuario;
import br.umc.loginseguro.repository.TokenRecuperacaoRepository;
import br.umc.loginseguro.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Fluxo "esqueci minha senha":
 * 1) solicitar(): gera um token aleatório, guarda só o hash e envia o link por e-mail;
 * 2) redefinir(): valida o token (existe, não expirou), troca a senha,
 *    apaga o token (uso único) e encerra as sessões abertas do usuário.
 */
@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);

    /** Validade do link enviado por e-mail. */
    public static final Duration VALIDADE = Duration.ofMinutes(30);

    private final SecureRandom geradorAleatorio = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoRepository tokenRepository;
    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final AuditoriaService auditoria;
    private final FindByIndexNameSessionRepository<? extends Session> sessoes;
    private final String urlBase;

    public RecuperacaoSenhaService(UsuarioRepository usuarioRepository,
                                   TokenRecuperacaoRepository tokenRepository,
                                   UsuarioService usuarioService,
                                   EmailService emailService,
                                   AuditoriaService auditoria,
                                   FindByIndexNameSessionRepository<? extends Session> sessoes,
                                   @Value("${app.url-base:http://localhost:8080}") String urlBase) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.usuarioService = usuarioService;
        this.emailService = emailService;
        this.auditoria = auditoria;
        this.sessoes = sessoes;
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
    }

    /**
     * Pedido de recuperação. Não informa à tela se o e-mail existe
     * (a resposta é sempre a mesma), evitando descobrir contas cadastradas.
     */
    public void solicitar(String emailDigitado) {
        String email = UsuarioService.normalizarEmail(emailDigitado);
        Optional<Usuario> encontrado = usuarioRepository.findByEmail(email);

        if (encontrado.isEmpty() || !encontrado.get().isAtivo()) {
            auditoria.registrar(TipoEvento.RECUPERACAO_SOLICITADA, email, email,
                    "E-mail não cadastrado ou conta desativada: nenhum e-mail enviado");
            return;
        }

        Usuario usuario = encontrado.get();
        tokenRepository.deleteByUsuarioId(usuario.getId()); // invalida links anteriores

        String token = gerarToken();
        tokenRepository.save(new TokenRecuperacao(
                hashToken(token), usuario.getId(), Instant.now().plus(VALIDADE)));

        String link = urlBase + "/redefinir-senha?token=" + token;
        boolean enviado = emailService.enviarRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), link, VALIDADE);

        auditoria.registrar(TipoEvento.RECUPERACAO_SOLICITADA, email, email,
                enviado ? "Link de redefinição enviado por e-mail"
                        : "Link gerado, mas o e-mail não foi enviado (ver terminal)");
    }

    public boolean tokenValido(String token) {
        return buscarTokenValido(token).isPresent();
    }

    /** @return false se o link for inválido ou tiver expirado. */
    public boolean redefinir(String token, String novaSenha) {
        Optional<TokenRecuperacao> registro = buscarTokenValido(token);
        if (registro.isEmpty()) {
            return false;
        }
        Optional<Usuario> encontrado = usuarioRepository.findById(registro.get().getUsuarioId());
        if (encontrado.isEmpty()) {
            return false;
        }

        Usuario usuario = encontrado.get();
        usuarioService.definirNovaSenha(usuario, novaSenha);
        tokenRepository.deleteByUsuarioId(usuario.getId()); // uso único
        encerrarSessoes(usuario.getEmail());

        auditoria.registrar(TipoEvento.SENHA_REDEFINIDA, usuario.getEmail(), usuario.getEmail(),
                "Senha redefinida pelo link de recuperação; sessões abertas encerradas");
        return true;
    }

    private Optional<TokenRecuperacao> buscarTokenValido(String token) {
        if (!StringUtils.hasText(token) || token.length() > 100) {
            return Optional.empty();
        }
        return tokenRepository.findByTokenHash(hashToken(token)).filter(TokenRecuperacao::isValido);
    }

    /** Se alguém estava logado com a senha antiga (ex.: invasor), perde o acesso. */
    private void encerrarSessoes(String email) {
        try {
            sessoes.findByPrincipalName(email).keySet().forEach(sessoes::deleteById);
        } catch (RuntimeException e) {
            log.error("Não foi possível encerrar as sessões de {}", email, e);
        }
    }

    /** 32 bytes aleatórios criptograficamente seguros, em texto seguro para URL. */
    private String gerarToken() {
        byte[] bytes = new byte[32];
        geradorAleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 em hexadecimal: é isso que fica no banco, nunca o token em si. */
    public static String hashToken(String token) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
