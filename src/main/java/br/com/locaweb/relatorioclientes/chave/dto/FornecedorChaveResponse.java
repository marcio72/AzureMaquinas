package br.com.locaweb.relatorioclientes.chave.dto;

import br.com.locaweb.relatorioclientes.chave.model.FornecedorChave;

public record FornecedorChaveResponse(Long id, String nome, long totalChaves) {

    public static FornecedorChaveResponse de(FornecedorChave f, long totalChaves) {
        return new FornecedorChaveResponse(f.getId(), f.getNome(), totalChaves);
    }
}
