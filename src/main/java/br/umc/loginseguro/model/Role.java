package br.umc.loginseguro.model;

/**
 * Perfis de acesso do sistema.
 *
 * PARA ADAPTAR A OUTRO PROJETO: renomeie ou adicione constantes aqui e ajuste as
 * regras de rota em SecurityConfig. O restante do sistema (cadastro, login,
 * sessão, telas de administração) funciona sem alterações.
 */
public enum Role {

    ADMIN("Administrador", "/admin"),
    PROFESSOR("Professor", "/professor"),
    ALUNO("Aluno", "/aluno");

    /** Nome exibido nas telas. */
    private final String descricao;

    /** Página para onde o usuário é levado após o login. */
    private final String paginaInicial;

    Role(String descricao, String paginaInicial) {
        this.descricao = descricao;
        this.paginaInicial = paginaInicial;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPaginaInicial() {
        return paginaInicial;
    }

    /** O Spring Security espera o prefixo "ROLE_" para usar hasRole(...). */
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
