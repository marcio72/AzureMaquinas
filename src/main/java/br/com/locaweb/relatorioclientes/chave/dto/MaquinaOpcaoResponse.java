package br.com.locaweb.relatorioclientes.chave.dto;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.Maquina;

/**
 * Resultado da busca de máquina por número, pra tela de vincular chave.
 * Como o número repete entre praças, o app mostra praça + cliente pra escolher.
 */
public record MaquinaOpcaoResponse(
        Long id,
        String numero,
        String jogo,
        Integer codCliente,
        String clienteNome,
        String praca) {

    public static MaquinaOpcaoResponse de(Maquina m, Cliente c) {
        return new MaquinaOpcaoResponse(
                m.getId(),
                m.getNom_maq() == null ? null : m.getNom_maq().trim(),
                m.getNom_jogo(),
                m.getCodCliente(),
                c == null ? null : c.getNomCliente(),
                c == null ? null : c.getPraca());
    }
}
