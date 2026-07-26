package br.com.locaweb.relatorioclientes.DTO;

/**
 * Resumo de lotes ativos (quantidadeAtual > 0) agrupados por categoria,
 * usado na tabela "Lotes Ativos por Categoria" do dashboard.
 */
public class LoteCategoriaResumoDTO {

    private final String categoria;
    private final Long lotesAtivos;
    private final Long pecasAtuais;

    public LoteCategoriaResumoDTO(String categoria, Long lotesAtivos, Long pecasAtuais) {
        this.categoria = categoria;
        this.lotesAtivos = lotesAtivos;
        this.pecasAtuais = pecasAtuais;
    }

    public String getCategoria() {
        return categoria;
    }

    public Long getLotesAtivos() {
        return lotesAtivos;
    }

    public Long getPecasAtuais() {
        return pecasAtuais;
    }
}
