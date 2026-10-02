package br.umc.loginseguro;

import br.umc.loginseguro.model.Role;
import br.umc.loginseguro.service.RecuperacaoSenhaService;
import br.umc.loginseguro.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/** Testes unitários que não dependem do MongoDB. Rodar com: mvn test */
class RegrasBasicasTest {

    @Test
    void perfilGeraAuthorityComPrefixoRole() {
        assertEquals("ROLE_ADMIN", Role.ADMIN.getAuthority());
        assertEquals("/aluno", Role.ALUNO.getPaginaInicial());
    }

    @Test
    void emailENormalizado() {
        assertEquals("maria@umc.br", UsuarioService.normalizarEmail("  Maria@UMC.br "));
        assertEquals("", UsuarioService.normalizarEmail(null));
    }

    @Test
    void senhaNuncaFicaEmTextoPuro() {
        var encoder = new BCryptPasswordEncoder(4); // custo baixo só para o teste ser rápido
        String hash = encoder.encode("Senha123");
        assertNotEquals("Senha123", hash);
        assertTrue(encoder.matches("Senha123", hash));
        assertFalse(encoder.matches("senha123", hash));
    }

    @Test
    void tokenDeRecuperacaoEArmazenadoComoHash() {
        String hash = RecuperacaoSenhaService.hashToken("token-de-exemplo");
        assertEquals(64, hash.length());                  // SHA-256 em hexadecimal
        assertNotEquals("token-de-exemplo", hash);
        assertEquals(hash, RecuperacaoSenhaService.hashToken("token-de-exemplo")); // determinístico
    }
}
