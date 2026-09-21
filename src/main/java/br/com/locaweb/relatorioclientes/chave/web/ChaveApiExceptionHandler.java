package br.com.locaweb.relatorioclientes.chave.web;

import br.com.locaweb.relatorioclientes.chave.exception.ChaveNaoEncontradaException;
import br.com.locaweb.relatorioclientes.chave.exception.RegraNegocioChaveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Erros da API de chaves em JSON { "erro": "..." }.
 * Limitado ao ChaveApiController pra não interferir nas telas
 * nem nos outros controllers do sistema.
 */
@RestControllerAdvice(assignableTypes = ChaveApiController.class)
public class ChaveApiExceptionHandler {

    @ExceptionHandler(RegraNegocioChaveException.class)
    public ResponseEntity<Map<String, String>> regraNegocio(RegraNegocioChaveException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(ChaveNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> naoEncontrada(ChaveNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }
}
