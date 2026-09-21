package br.com.locaweb.relatorioclientes.chave.exception;

/** Chave ou fornecedor de chave inexistente. */
public class ChaveNaoEncontradaException extends RuntimeException {

    public ChaveNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
