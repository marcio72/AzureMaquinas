package br.com.locaweb.relatorioclientes.chave.exception;

/** Violação de regra do módulo de chaves (duplicidade, campo obrigatório...). */
public class RegraNegocioChaveException extends RuntimeException {

    public RegraNegocioChaveException(String mensagem) {
        super(mensagem);
    }
}
