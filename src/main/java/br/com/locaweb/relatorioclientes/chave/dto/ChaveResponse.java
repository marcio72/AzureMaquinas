package br.com.locaweb.relatorioclientes.chave.dto;

import br.com.locaweb.relatorioclientes.chave.model.Chave;
import br.com.locaweb.relatorioclientes.chave.model.TipoChave;

import java.time.LocalDateTime;

/** O que sai do módulo de chaves (API e telas). */
public record ChaveResponse(
        Long id,
        String codigo,
        String numero,
        Long fornecedorId,
        String fornecedorNome,
        TipoChave tipo,
        String tipoDescricao,
        Integer quantidadeCopias,
        Integer quantidadeCadeados,
        Boolean ativo,
        String observacao,
        LocalDateTime criadoEm) {

    public static ChaveResponse de(Chave c) {
        return new ChaveResponse(
                c.getId(),
                c.getCodigo(),
                c.getNumero(),
                c.getFornecedor().getId(),
                c.getFornecedor().getNome(),
                c.getTipo(),
                c.getTipo().getDescricao(),
                c.getQuantidadeCopias(),
                c.getQuantidadeCadeados(),
                c.getAtivo(),
                c.getObservacao(),
                c.getCriadoEm());
    }
}
