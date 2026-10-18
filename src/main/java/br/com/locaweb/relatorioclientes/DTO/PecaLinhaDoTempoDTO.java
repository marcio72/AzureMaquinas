package br.com.locaweb.relatorioclientes.DTO;

import java.util.List;

/**
 * Resposta da tela de historico de peca: resumo da peca, onde nasceu,
 * onde esta agora e a lista de eventos (reaproveita HistoricoPecaDTO).
 */
public final class PecaLinhaDoTempoDTO {

    private PecaLinhaDoTempoDTO() {}

    public record LinhaDoTempo(
            PecaHistoricoResumoDTO peca,
            HistoricoPecaDTO nascimento,
            String localAtual,
            int clientesDistintos,
            int maquinasDistintas,
            List<HistoricoPecaDTO> eventos
    ) {}
}
