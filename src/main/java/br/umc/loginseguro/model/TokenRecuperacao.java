package br.umc.loginseguro.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Documento da coleção "tokens_recuperacao".
 *
 * Guarda apenas o HASH (SHA-256) do token enviado por e-mail: quem ler o banco
 * não consegue usar o link. O índice TTL faz o MongoDB apagar o documento
 * sozinho quando o prazo de validade termina.
 */
@Document(collection = "tokens_recuperacao")
public class TokenRecuperacao {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tokenHash;

    @Indexed
    private String usuarioId;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiraEm;

    public TokenRecuperacao() {
    }

    public TokenRecuperacao(String tokenHash, String usuarioId, Instant expiraEm) {
        this.tokenHash = tokenHash;
        this.usuarioId = usuarioId;
        this.expiraEm = expiraEm;
    }

    public boolean isValido() {
        return expiraEm != null && expiraEm.isAfter(Instant.now());
    }

    public String getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public String getUsuarioId() { return usuarioId; }
    public Instant getExpiraEm() { return expiraEm; }
}
