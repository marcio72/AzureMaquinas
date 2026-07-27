package br.com.locaweb.relatorioclientes.DTO;

import br.com.locaweb.relatorioclientes.model.Cliente;

import java.time.LocalDateTime;

public class InstalacaoRequestDTO {

    private Cliente cliente;
    private String descricaoInstalacao;
    private LocalDateTime dataSolicitacao;

    // Getters e Setters
    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getDescricaoInstalacao() {
        return descricaoInstalacao;
    }

    public void setDescricaoInstalacao(String descricaoInstalacao) {
        this.descricaoInstalacao = descricaoInstalacao;
    } 

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }
}
