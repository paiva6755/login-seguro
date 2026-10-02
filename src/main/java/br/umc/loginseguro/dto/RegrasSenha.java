package br.umc.loginseguro.dto;

/**
 * Regras de senha usadas no cadastro e na redefinição.
 * Centralizadas aqui para que as duas telas sigam exatamente a mesma política.
 */
public final class RegrasSenha {

    public static final int MINIMO = 8;
    public static final int MAXIMO = 64;
    public static final String PADRAO = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$";
    public static final String MENSAGEM_TAMANHO = "A senha deve ter entre 8 e 64 caracteres.";
    public static final String MENSAGEM_PADRAO = "A senha precisa de letra maiúscula, letra minúscula e número.";

    private RegrasSenha() {
    }
}
