package br.umc.loginseguro.exception;

/** Lançada quando uma ação administrativa viola uma regra de negócio. */
public class OperacaoNaoPermitidaException extends RuntimeException {
    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
