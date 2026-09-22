package br.com.locaweb.relatorioclientes.chave.dto;

import br.com.locaweb.relatorioclientes.chave.model.MaquinaChave;
import br.com.locaweb.relatorioclientes.chave.model.UsoChave;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.Maquina;

import java.time.LocalDateTime;

/** Um vínculo chave ↔ máquina (atual ou do histórico). */
public record VinculoChaveResponse(
        Long id,
        Long maquinaId,
        String maquinaNome,
        String maquinaJogo,
        Integer codCliente,
        String clienteNome,
        String praca,
        Long chaveId,
        String chaveCodigo,
        String fornecedorNome,
        UsoChave uso,
        String usoDescricao,
        Boolean ativo,
        LocalDateTime vinculadoEm,
        LocalDateTime desvinculadoEm,
        String observacao) {

    public static VinculoChaveResponse de(MaquinaChave v, Cliente cliente) {
        Maquina m = v.getMaquina();
        return new VinculoChaveResponse(
                v.getId(),
                m.getId(),
                m.getNom_maq(),
                m.getNom_jogo(),
                m.getCodCliente(),
                cliente == null ? null : cliente.getNomCliente(),
                cliente == null ? null : cliente.getPraca(),
                v.getChave().getId(),
                v.getChave().getCodigo(),
                v.getChave().getFornecedor().getNome(),
                v.getUso(),
                v.getUso().getDescricao(),
                v.getAtivo(),
                v.getVinculadoEm(),
                v.getDesvinculadoEm(),
                v.getObservacao());
    }
}
