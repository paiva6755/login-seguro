package br.umc.loginseguro.exception;

/** Lançada quando alguém tenta se cadastrar com um e-mail já existente. */
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException() {
        super("Este e-mail já está cadastrado.");
    }
}
