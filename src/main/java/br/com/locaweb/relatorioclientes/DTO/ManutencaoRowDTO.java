package br.com.locaweb.relatorioclientes.DTO;

import java.time.LocalDateTime;

/**
 * Uma linha da tabela "Manutenções desta Máquina", no Explorer.
 * Junta o problema relatado com o técnico/data da execução (se já foi feita)
 * e as categorias de peça trocadas nessa execução (pra filtrar pelos cards
 * de "Trocas de Placa/Fonte/Monitor").
 */
public class ManutencaoRowDTO {

    private final Long id;
    private final Long nf; // id da execução (ExecucaoManutencao) — null se ainda não foi executada
    private final Long solicitacaoId; // id da SolicitacaoManutencao — ajuda a relacionar linhas "órfãs" (sem NF) com a visita que as originou
    private final LocalDateTime data;
    private final String descricao;
    private final String tecnico;
    private final boolean pendente;
    private final String categoriasTrocadas; // ex.: "Fonte,Monitor" — vazio se não trocou peça
    private final String execucaoDescricao; // relatório/observações escritos pelo técnico na execução

    public ManutencaoRowDTO(Long id, Long nf, Long solicitacaoId, LocalDateTime data, String descricao, String tecnico,
                             boolean pendente, String categoriasTrocadas, String execucaoDescricao) {
        this.id = id;
        this.nf = nf;
        this.solicitacaoId = solicitacaoId;
        this.data = data;
        this.descricao = descricao;
        this.tecnico = tecnico;
        this.pendente = pendente;
        this.categoriasTrocadas = categoriasTrocadas;
        this.execucaoDescricao = execucaoDescricao;
    }

    public Long getId() {
        return id;
    }

    public Long getNf() {
        return nf;
    }

    public Long getSolicitacaoId() {
        return solicitacaoId;
    }

    public String getExecucaoDescricao() {
        return execucaoDescricao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTecnico() {
        return tecnico;
    }

    public boolean isPendente() {
        return pendente;
    }

    public String getCategoriasTrocadas() {
        return categoriasTrocadas;
    }
}
