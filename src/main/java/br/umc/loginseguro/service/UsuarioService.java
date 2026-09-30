package br.umc.loginseguro.service;

import br.umc.loginseguro.dto.CadastroForm;
import br.umc.loginseguro.exception.EmailJaCadastradoException;
import br.umc.loginseguro.exception.OperacaoNaoPermitidaException;
import br.umc.loginseguro.model.Role;
import br.umc.loginseguro.model.Usuario;
import br.umc.loginseguro.repository.UsuarioRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Regras de negócio dos usuários. Os controllers só chamam este serviço;
 * nenhuma regra fica nas telas ou nos controllers.
 */
@Service
public class UsuarioService {

    /** Tentativas de senha errada antes do bloqueio temporário. */
    public static final int MAX_TENTATIVAS = 5;

    /** Tempo de bloqueio após exceder as tentativas. */
    public static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(15);

    /** Perfil atribuído a quem se cadastra pela tela pública. */
    public static final Role PERFIL_PADRAO_CADASTRO = Role.ALUNO;

    private final UsuarioRepository repositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repositorio, PasswordEncoder passwordEncoder) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
    }

    /** E-mails são comparados sempre em minúsculas e sem espaços. */
    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Cadastro público. O perfil é SEMPRE o padrão: nunca vem do formulário,
     * para impedir que alguém se cadastre como administrador.
     */
    public Usuario cadastrar(CadastroForm form) {
        return criarUsuario(form.getNome(), form.getEmail(), form.getSenha(), PERFIL_PADRAO_CADASTRO);
    }

    /** Cria um usuário com qualquer perfil (uso interno, ex.: admin inicial). */
    public Usuario criarUsuario(String nome, String email, String senhaPura, Role perfil) {
        String emailNormalizado = normalizarEmail(email);
        if (repositorio.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = new Usuario(
                nome.trim(),
                emailNormalizado,
                passwordEncoder.encode(senhaPura), // hash BCrypt
                perfil);
        try {
            return repositorio.save(usuario);
        } catch (DuplicateKeyException e) {
            // Dois cadastros simultâneos com o mesmo e-mail: o índice único barra o segundo
            throw new EmailJaCadastradoException();
        }
    }

    public boolean existeEmail(String email) {
        return repositorio.existsByEmail(normalizarEmail(email));
    }

    public List<Usuario> listarTodos() {
        return repositorio.findAll(Sort.by("nome"));
    }

    public List<Usuario> listarPorPerfil(Role perfil) {
        return repositorio.findByPerfilOrderByNomeAsc(perfil);
    }

    /** Admin altera o perfil de outro usuário (não o próprio, para não se trancar fora). */
    public void alterarPerfil(String id, Role novoPerfil, String emailSolicitante) {
        Usuario usuario = buscar(id);
        impedirAcaoSobreSiMesmo(usuario, emailSolicitante, "Você não pode alterar o próprio perfil.");
        usuario.setPerfil(novoPerfil);
        repositorio.save(usuario);
    }

    /** Admin ativa/desativa uma conta. Reativar também remove bloqueios. */
    public void alternarAtivo(String id, String emailSolicitante) {
        Usuario usuario = buscar(id);
        impedirAcaoSobreSiMesmo(usuario, emailSolicitante, "Você não pode desativar a própria conta.");
        usuario.setAtivo(!usuario.isAtivo());
        if (usuario.isAtivo()) {
            usuario.setTentativasFalhas(0);
            usuario.setBloqueadoAte(null);
        }
        repositorio.save(usuario);
    }

    /** Chamado a cada senha errada. Ao atingir o limite, bloqueia temporariamente. */
    public void registrarFalhaLogin(String email) {
        repositorio.findByEmail(normalizarEmail(email)).ifPresent(usuario -> {
            int tentativas = usuario.getTentativasFalhas() + 1;
            if (tentativas >= MAX_TENTATIVAS) {
                usuario.setBloqueadoAte(Instant.now().plus(TEMPO_BLOQUEIO));
                tentativas = 0;
            }
            usuario.setTentativasFalhas(tentativas);
            repositorio.save(usuario);
        });
    }

    /** Chamado após login correto: zera contador e registra data. */
    public void registrarLoginBemSucedido(String email) {
        repositorio.findByEmail(normalizarEmail(email)).ifPresent(usuario -> {
            usuario.setTentativasFalhas(0);
            usuario.setBloqueadoAte(null);
            usuario.setUltimoLogin(Instant.now());
            repositorio.save(usuario);
        });
    }

    private Usuario buscar(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Usuário não encontrado."));
    }

    private void impedirAcaoSobreSiMesmo(Usuario alvo, String emailSolicitante, String mensagem) {
        if (alvo.getEmail().equals(normalizarEmail(emailSolicitante))) {
            throw new OperacaoNaoPermitidaException(mensagem);
        }
    }
}
