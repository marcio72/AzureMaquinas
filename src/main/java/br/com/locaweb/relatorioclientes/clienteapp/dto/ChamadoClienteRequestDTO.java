package br.com.locaweb.relatorioclientes.clienteapp.dto;

import java.util.List;

/**
 * Request de criação de chamado vindo do app do cliente.
 * Propositalmente NÃO tem campo "cliente": o backend sempre usa o cliente
 * da sessão logada, nunca um valor vindo do app (evita um cliente criar
 * chamado em nome de outro).
 */
public class ChamadoClienteRequestDTO {

    public List<ProblemaChamadoDTO> problemas;

    public static class ProblemaChamadoDTO {
        public Long numeroMaquina;
        public String descricao;
        public String fotoBase64;
    }
}
