package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.DTO.HistoricoPecaDTO;
import br.com.locaweb.relatorioclientes.DTO.PecaLinhaDoTempoDTO.LinhaDoTempo;
import br.com.locaweb.relatorioclientes.DTO.PecaHistoricoResumoDTO;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.HistoricoPecaRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Leitura da linha do tempo de uma peça (tela /pecas/historico).
 * Só consulta — quem GRAVA os eventos continua sendo o HistoricoPecaService.
 */
@Service
public class PecaLinhaDoTempoService {

    /** Cliente placeholder "INSTALAÇÃO" (cod_cliente = 1) — não é cliente real (regra do CLAUDE.md). */
    private static final long CLIENTE_PLACEHOLDER_ID = 1L;

    private final HistoricoPecaRepository historicoRepo;
    private final PecaRepository pecaRepo;

    public PecaLinhaDoTempoService(HistoricoPecaRepository historicoRepo, PecaRepository pecaRepo) {
        this.historicoRepo = historicoRepo;
        this.pecaRepo = pecaRepo;
    }

    /** Todas as peças que já tiveram algum movimento, mais recente primeiro. */
    @Transactional(readOnly = true)
    public List<PecaHistoricoResumoDTO> listar() {
        return historicoRepo.listarPecasComHistorico();
    }

    @Transactional(readOnly = true)
    public LinhaDoTempo linhaDoTempo(Long idPeca) {
        Peca peca = pecaRepo.findById(idPeca)
                .orElseThrow(() -> new IllegalArgumentException("Peça não encontrada: " + idPeca));

        List<HistoricoPecaDTO> eventos = historicoRepo.linhaDoTempo(idPeca).stream()
                .map(HistoricoPecaDTO::fromEntity)
                .peek(this::limparPlaceholder)
                .toList();

        HistoricoPecaDTO nascimento = eventos.stream()
                .filter(e -> "ENTRADA_ESTOQUE".equals(e.getTipoEvento()))
                .findFirst()
                .orElse(eventos.isEmpty() ? null : eventos.get(0));

        int clientes = (int) eventos.stream()
                .flatMap(e -> Stream.of(e.getClienteOrigemId(), e.getClienteDestinoId()))
                .filter(Objects::nonNull)
                .distinct().count();

        int maquinas = (int) eventos.stream()
                .map(HistoricoPecaDTO::getMaquinaId)
                .filter(Objects::nonNull)
                .distinct().count();

        return new LinhaDoTempo(resumo(peca, eventos), nascimento,
                localAtual(eventos), clientes, maquinas, eventos);
    }

    // ---------------------------------------------------------------- helpers

    /** Cliente 1 (INSTALAÇÃO) é placeholder: some da timeline e da contagem. */
    private void limparPlaceholder(HistoricoPecaDTO e) {
        if (Objects.equals(e.getClienteOrigemId(), CLIENTE_PLACEHOLDER_ID)) {
            e.setClienteOrigemId(null);
            e.setClienteOrigemNome(null);
        }
        if (Objects.equals(e.getClienteDestinoId(), CLIENTE_PLACEHOLDER_ID)) {
            e.setClienteDestinoId(null);
            e.setClienteDestinoNome(null);
        }
    }

    private String localAtual(List<HistoricoPecaDTO> eventos) {
        if (eventos.isEmpty()) return "Sem movimentação";
        HistoricoPecaDTO ultimo = eventos.get(eventos.size() - 1);
        String tipo = ultimo.getTipoEvento();

        if ("DESCARTE".equals(tipo)) return "Descartada";
        if ("AVALIACAO".equals(tipo)) return "Estoque (em avaliação)";
        if (!"INSTALACAO".equals(tipo)) return "Estoque";

        StringBuilder sb = new StringBuilder(
                ultimo.getClienteDestinoNome() != null ? ultimo.getClienteDestinoNome() : "Cliente ?");
        if (ultimo.getMaquinaNome() != null) sb.append(" — Máq. ").append(ultimo.getMaquinaNome());
        if (ultimo.getPracaDestino() != null) sb.append(" (").append(ultimo.getPracaDestino()).append(")");
        return sb.toString();
    }

    private PecaHistoricoResumoDTO resumo(Peca p, List<HistoricoPecaDTO> eventos) {
        String categoria = p.getCategoria() != null ? p.getCategoria().getNome() : null;

        // Placa Mãe -> modelo da placa; Coletor -> nome do coletor; Jogo -> nome do jogo
        String modelo = null;
        if (p.getPlaca() != null) modelo = p.getPlaca().getModelo();
        else if (p.getColetor() != null) modelo = p.getColetor().getNome();
        else if (p.getJogo() != null) modelo = p.getJogo().getDescricaojogo();

        var ultimo = eventos.isEmpty() ? null : eventos.get(eventos.size() - 1).getDataEvento();
        return new PecaHistoricoResumoDTO(p.getIdPeca(), p.getCodigo(), categoria, modelo,
                p.getStatus(), (long) eventos.size(), ultimo);
    }
}
