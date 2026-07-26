package br.com.locaweb.relatorioclientes.DTO;

/**
 * Quantidade de trocas de peça (eventos HistoricoPeca do tipo INSTALACAO)
 * de uma máquina, agrupadas por categoria (Placa Mãe, Fonte, Monitor, etc.).
 */
public class TrocaPecaCategoriaDTO {

    private final String categoria;
    private final Long quantidade;

    public TrocaPecaCategoriaDTO(String categoria, Long quantidade) {
        this.categoria = categoria;
        this.quantidade = quantidade;
    }

    public String getCategoria() {
        return categoria;
    }

    public Long getQuantidade() {
        return quantidade;
    }
}
