package br.com.locaweb.relatorioclientes.chave.dto;

import br.com.locaweb.relatorioclientes.chave.model.TipoChave;

/**
 * Dados de entrada de uma chave. Classe comum (não record) porque serve
 * tanto pro JSON da API quanto pro th:field do formulário Thymeleaf.
 */
public class ChaveRequest {

    private Long id;               // usado só pelo formulário web (edição)
    private String numero;
    private Long fornecedorId;
    private TipoChave tipo;
    private Integer quantidadeCopias = 1;
    private Integer quantidadeCadeados = 0;
    private String observacao;
    private Boolean ativo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public TipoChave getTipo() {
        return tipo;
    }

    public void setTipo(TipoChave tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidadeCopias() {
        return quantidadeCopias;
    }

    public void setQuantidadeCopias(Integer quantidadeCopias) {
        this.quantidadeCopias = quantidadeCopias;
    }

    public Integer getQuantidadeCadeados() {
        return quantidadeCadeados;
    }

    public void setQuantidadeCadeados(Integer quantidadeCadeados) {
        this.quantidadeCadeados = quantidadeCadeados;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
