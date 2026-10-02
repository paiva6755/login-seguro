package br.umc.loginseguro.model;

/** Tipos de evento registrados no log de auditoria. */
public enum TipoEvento {

    CADASTRO("Cadastro"),
    LOGIN_SUCESSO("Login"),
    LOGIN_FALHA("Falha de login"),
    CONTA_BLOQUEADA("Conta bloqueada"),
    LOGOUT("Logout"),
    ACESSO_NEGADO("Acesso negado"),
    PERFIL_ALTERADO("Perfil alterado"),
    CONTA_DESATIVADA("Conta desativada"),
    CONTA_REATIVADA("Conta reativada"),
    RECUPERACAO_SOLICITADA("Recuperação de senha solicitada"),
    SENHA_REDEFINIDA("Senha redefinida");

    private final String descricao;

    TipoEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
