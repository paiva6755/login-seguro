package br.umc.loginseguro.security;

import br.umc.loginseguro.model.Role;
import br.umc.loginseguro.model.Usuario;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * Representa o usuário autenticado dentro do Spring Security.
 *
 * Este objeto é guardado na sessão (que fica no MongoDB), por isso é
 * serializável e contém apenas o necessário. Após o login o Spring Security
 * chama eraseCredentials(), removendo o hash da senha da sessão.
 */
public class UsuarioLogado implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nome;
    private final String email;
    private final Role perfil;
    private final boolean ativo;
    private final boolean bloqueado;
    private String senhaHash;

    public UsuarioLogado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.perfil = usuario.getPerfil();
        this.ativo = usuario.isAtivo();
        this.bloqueado = usuario.isBloqueadoAgora();
        this.senhaHash = usuario.getSenhaHash();
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public Role getPerfil() { return perfil; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(perfil.getAuthority()));
    }

    @Override
    public String getPassword() { return senhaHash; }

    /** O "username" do sistema é o e-mail. */
    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonLocked() { return !bloqueado; }

    @Override
    public boolean isEnabled() { return ativo; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public void eraseCredentials() { this.senhaHash = null; }
}
