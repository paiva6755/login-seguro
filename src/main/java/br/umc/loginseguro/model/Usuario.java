package br.umc.loginseguro.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Documento da coleção "usuarios" no MongoDB.
 * A senha NUNCA é armazenada em texto puro: apenas o hash BCrypt.
 */
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nome;

    /** Índice único: impede dois cadastros com o mesmo e-mail, mesmo em concorrência. */
    @Indexed(unique = true)
    private String email;

    /** Hash BCrypt da senha. */
    private String senhaHash;

    private Role perfil;

    /** Contas desativadas pelo administrador não conseguem entrar. */
    private boolean ativo = true;

    /** Proteção contra força bruta: tentativas erradas seguidas. */
    private int tentativasFalhas;

    /** Se preenchido e no futuro, a conta está temporariamente bloqueada. */
    private Instant bloqueadoAte;

    private Instant ultimoLogin;

    @CreatedDate
    private Instant criadoEm;

    @LastModifiedDate
    private Instant atualizadoEm;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, Role perfil) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    /** Usado nas telas; não é gravado no banco (o mapeamento usa apenas campos). */
    public boolean isBloqueadoAgora() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(Instant.now());
    }

    // ---------- getters e setters ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public Role getPerfil() { return perfil; }
    public void setPerfil(Role perfil) { this.perfil = perfil; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public int getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(int tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }

    public Instant getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(Instant bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }

    public Instant getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(Instant ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
