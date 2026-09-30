package br.umc.loginseguro.config;

import br.umc.loginseguro.model.Role;
import br.umc.loginseguro.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Na inicialização, cria o primeiro administrador a partir das variáveis
 * ADMIN_EMAIL e ADMIN_SENHA (se ainda não existir). Sem isso, ninguém
 * conseguiria acessar a área administrativa, pois o cadastro público
 * sempre cria perfil ALUNO.
 */
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioService usuarioService;
    private final String nome;
    private final String email;
    private final String senha;

    public AdminInicial(UsuarioService usuarioService,
                        @Value("${app.admin.nome:Administrador}") String nome,
                        @Value("${app.admin.email:}") String email,
                        @Value("${app.admin.senha:}") String senha) {
        this.usuarioService = usuarioService;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || senha.isBlank()) {
            log.warn("ADMIN_EMAIL/ADMIN_SENHA não definidos: nenhum administrador inicial foi criado.");
            return;
        }
        if (usuarioService.existeEmail(email)) {
            return; // já criado em execução anterior
        }
        if (senha.length() < 8) {
            log.warn("ADMIN_SENHA precisa ter ao menos 8 caracteres. Administrador não criado.");
            return;
        }
        usuarioService.criarUsuario(nome, email, senha, Role.ADMIN);
        log.info("Administrador inicial criado: {}", UsuarioService.normalizarEmail(email));
    }
}
