package br.umc.loginseguro.security;

import br.umc.loginseguro.repository.UsuarioRepository;
import br.umc.loginseguro.service.UsuarioService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Ponte entre o Spring Security e o MongoDB: dado o e-mail digitado no
 * login, busca o usuário. A comparação da senha (BCrypt) é feita pelo
 * próprio Spring Security.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repositorio;

    public UsuarioDetailsService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repositorio.findByEmail(UsuarioService.normalizarEmail(email))
                .map(UsuarioLogado::new)
                // A mensagem não chega à tela: o Spring mostra erro genérico
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
