package br.com.locaweb.relatorioclientes.service;


import br.com.locaweb.relatorioclientes.model.*;
import br.com.locaweb.relatorioclientes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final PecaRepository pecaRepository;
    private final LoteRepository loteRepository;
    private final MovimentoEstoqueRepository movimentoRepository;
    private final MaquinaRepository maquinaRepository;
    private final ClienteRepository clienteRepository;
    private final ExecucaoRepository execucaoRepository;
    private final HistoricoPecaRepository historicoPecaRepository;

    /**
     * Atualiza o "slot" da máquina correspondente à categoria da peça
     * (ex: categoria "Monitor" -> maquina.monitor), guardando o id_peca
     * da peça física instalada, ou limpando (null) quando ela sai.
     *
     * Novas categorias entram aqui, uma de cada vez, conforme forem
     * cadastradas e mapeadas (Coletor, Cadeado, Chave1, Chave2, etc.).
     */
    private void atualizarSlotMaquina(Maquina maquina, Peca peca, boolean instalando) {
        if (maquina == null || peca.getCategoria() == null) return;

        String categoriaNome = peca.getCategoria().getNome();
        if (categoriaNome == null) return;

        if (categoriaNome.equalsIgnoreCase("Monitor")) {
            maquina.setMonitor(instalando ? peca.getIdPeca().intValue() : null);
        }
        if (categoriaNome.equalsIgnoreCase("Fontes")) {
            maquina.setFonte(instalando ? peca.getIdPeca().intValue() : null);
        }
        if (categoriaNome.equalsIgnoreCase("Jogo")) {
            maquina.setJogoSegundo(instalando ? peca.getIdPeca().intValue() : null);

            // Se a peça tem um jogo vinculado do catálogo (Tbl_Jogos), grava o
            // nome dele no campo "jogo" (texto livre) que já existe — sem
            // mexer nas telas que já leem esse campo hoje.
            if (peca.getJogo() != null) {
                maquina.setNom_jogo(instalando ? peca.getJogo().getDescricaojogo() : null);
            }
        }
        if (categoriaNome.equalsIgnoreCase("Placa Mãe")) {
            if (instalando) {
                String apenasNumeros = peca.getCodigo().replaceAll("\\D+", "");
                maquina.setNumeroPlaca(apenasNumeros);
            } else {
                maquina.setNumeroPlaca(null);
            }
            if (peca.getPlaca() != null) {
                maquina.setModPlaca(instalando ? peca.getPlaca().getId().intValue() : null);
            }
        }
        if (categoriaNome.equalsIgnoreCase("Coletor")) {
            maquina.setColetor(instalando ? peca.getIdPeca().intValue() : null);
        }
        // TODO: próximas categorias (Cadeado, Cadeado1, Cadeado2,
        // Chave1, Chave2, Tip_Gabinete) entram aqui quando forem mapeadas.
    }

    @Transactional
    public void baixarPeca(Long idPeca, Long idExecucao, Long idMaquina, Long idCliente) {

        // --- PEÇA ---
        Peca peca = pecaRepository.findById(idPeca)
                .orElseThrow(() -> new RuntimeException("Peça não encontrada"));

        // ✔ ESTOQUE significa disponível
        if (!peca.getStatus().equals("ESTOQUE")) {
            throw new RuntimeException("Peça não está disponível (status atual: " + peca.getStatus() + ")");
        }

        // --- LOTE ---
        Lote lote = peca.getLote();
        if (lote.getQuantidadeAtual() <= 0) {
            throw new RuntimeException("Lote sem peças disponíveis.");
        }

        // --- EXECUÇÃO ---
        ExecucaoManutencao execucao = execucaoRepository.findById(idExecucao)
                .orElseThrow(() -> new RuntimeException("Execução não encontrada"));

        // --- CLIENTE ---
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // --- MÁQUINA ---
        Maquina maquina = maquinaRepository.findById(idMaquina)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada"));


        // ======================================================
        // 1) Atualiza dados da peça
        // ======================================================
        peca.setStatus("INSTALADA");  // <- Agora fica INSTALADA
        peca.setDataInstalacao(LocalDate.now());
        peca.setCliente(cliente);
        peca.setMaquina(maquina);
        peca.setObservacao("Ficha de execução: " + idExecucao);

        pecaRepository.save(peca);

        // Garante que a máquina fique com o cliente correto (cobre o caso da máquina
        // ter mudado de cliente e o cadastro dela ainda não ter sido atualizado).
        maquina.setCodCliente(cliente.getCodCliente() != null ? cliente.getCodCliente().intValue() : null);
        atualizarSlotMaquina(maquina, peca, true);
        maquinaRepository.save(maquina);

        // ======================================================
        // 2) Dá baixa no lote
        // ======================================================
        lote.setQuantidadeAtual(lote.getQuantidadeAtual() - 1);
        loteRepository.save(lote);

        // ======================================================
        // 3) Registra movimento de auditoria
        // ======================================================
        MovimentoEstoque mov = new MovimentoEstoque();
        mov.setTipo("SAIDA");
        mov.setDataMovimento(LocalDateTime.now());
        mov.setPeca(peca);
        mov.setLote(lote);
        mov.setExecucao(execucao);
        mov.setCliente(cliente);
        mov.setMaquina(maquina);
        mov.setObservacao("Peça retirada do estoque e instalada.");

        movimentoRepository.save(mov);

        // ======================================================
        // 4) Registra o evento no histórico da peça (INSTALACAO)
        // ======================================================
        HistoricoPeca historico = new HistoricoPeca();
        historico.setPeca(peca);
        historico.setTipoEvento("INSTALACAO");
        historico.setClienteOrigem(null); // veio do estoque
        historico.setPracaOrigem(null);
        historico.setClienteDestino(cliente);
        historico.setPracaDestino(cliente.getPraca());
        historico.setMaquina(maquina);
        historico.setObservacao("Instalada via execução de serviço #" + idExecucao);
        historico.setDataEvento(LocalDateTime.now());
        historicoPecaRepository.save(historico);
    }

    /**
     * Retira uma peça instalada de um cliente e devolve para o estoque.
     * Grava no histórico de onde ela saiu (cliente/praça), permitindo depois
     * reconstruir a timeline completa da peça.
     */
    @Transactional
    public void retirarPeca(Long idPeca, String observacao, String usuarioResponsavel) {

        Peca peca = pecaRepository.findById(idPeca)
                .orElseThrow(() -> new RuntimeException("Peça não encontrada"));

        if (!"INSTALADA".equals(peca.getStatus())) {
            throw new RuntimeException("Peça não está instalada (status atual: " + peca.getStatus() + ")");
        }

        Cliente clienteOrigem = peca.getCliente();
        Maquina maquinaOrigem = peca.getMaquina();

        // ======================================================
        // 1) Peça volta para o estoque
        // ======================================================
        peca.setStatus("ESTOQUE");
        peca.setDataRetirada(LocalDateTime.now());
        peca.setCliente(null);
        peca.setMaquina(null);
        peca.setDataInstalacao(null);
        peca.setObservacao(null);
        pecaRepository.save(peca);

        // ======================================================
        // 2) Devolve a quantidade para o lote
        // ======================================================
        Lote lote = peca.getLote();
        if (lote != null) {
            lote.setQuantidadeAtual(lote.getQuantidadeAtual() + 1);
            loteRepository.save(lote);
        }

        // Limpa o slot correspondente na máquina de onde ela saiu
        if (maquinaOrigem != null) {
            atualizarSlotMaquina(maquinaOrigem, peca, false);
            maquinaRepository.save(maquinaOrigem);
        }

        // ======================================================
        // 3) Registra o evento no histórico da peça (RETIRADA)
        // ======================================================
        HistoricoPeca historico = new HistoricoPeca();
        historico.setPeca(peca);
        historico.setTipoEvento("RETIRADA");
        historico.setClienteOrigem(clienteOrigem);
        historico.setPracaOrigem(clienteOrigem != null ? clienteOrigem.getPraca() : null);
        historico.setClienteDestino(null); // vai para o estoque
        historico.setPracaDestino(null);
        historico.setMaquina(maquinaOrigem);
        historico.setObservacao(observacao);
        historico.setDataEvento(LocalDateTime.now());
        historico.setUsuarioResponsavel(usuarioResponsavel);
        historicoPecaRepository.save(historico);
    }

    /**
     * Registra uma avaliação da peça enquanto ela está no estoque
     * (ex: "com problema", "limpeza", "troca de pasta térmica").
     * Não muda cliente/máquina — só deixa marcado na timeline o motivo/diagnóstico.
     */
    @Transactional
    public void avaliarPeca(Long idPeca, String observacao, String usuarioResponsavel) {

        Peca peca = pecaRepository.findById(idPeca)
                .orElseThrow(() -> new RuntimeException("Peça não encontrada"));

        HistoricoPeca historico = new HistoricoPeca();
        historico.setPeca(peca);
        historico.setTipoEvento("AVALIACAO");
        historico.setClienteOrigem(null);
        historico.setPracaOrigem(null);
        historico.setClienteDestino(null);
        historico.setPracaDestino(null);
        historico.setMaquina(null);
        historico.setObservacao(observacao);
        historico.setDataEvento(LocalDateTime.now());
        historico.setUsuarioResponsavel(usuarioResponsavel);
        historicoPecaRepository.save(historico);
    }

    /**
     * Retira uma peça instalada de um cliente e a descarta como perda total
     * (ex: peça queimada). Diferente do retirarPeca(): a peça NÃO volta para
     * o estoque disponível, então o lote.quantidadeAtual não é incrementado —
     * ela sai de circulação definitivamente, mas continua visível na lista
     * do lote com status "DESCARTADA" (P.T.), pra manter o histórico completo.
     */
    @Transactional
    public void descartarPeca(Long idPeca, String observacao, String usuarioResponsavel) {

        Peca peca = pecaRepository.findById(idPeca)
                .orElseThrow(() -> new RuntimeException("Peça não encontrada"));

        if (!"INSTALADA".equals(peca.getStatus())) {
            throw new RuntimeException("Peça não está instalada (status atual: " + peca.getStatus() + ")");
        }

        Cliente clienteOrigem = peca.getCliente();
        Maquina maquinaOrigem = peca.getMaquina();

        // ======================================================
        // 1) Peça sai de circulação (perda total) — NÃO volta pro estoque.
        //    Mantém cliente/máquina/data de instalação/observação como estavam:
        //    peça descartada nunca mais é reutilizada, então esse "congelamento"
        //    serve pra quem for verificar depois saber onde ela deu problema.
        // ======================================================
        peca.setStatus("DESCARTADA");
        peca.setDataRetirada(LocalDateTime.now());
        pecaRepository.save(peca);

        // ======================================================
        // 2) NÃO mexe em lote.quantidadeAtual — peça está perdida,
        //    não conta mais como disponível nem volta a contar.
        // ======================================================

        // Limpa o slot da máquina (ela vai receber outra peça no lugar),
        // mesmo mantendo cliente/máquina na peça em si pra rastreio.
        if (maquinaOrigem != null) {
            atualizarSlotMaquina(maquinaOrigem, peca, false);
            maquinaRepository.save(maquinaOrigem);
        }

        // ======================================================
        // 3) Registra o evento no histórico da peça (DESCARTE)
        // ======================================================
        HistoricoPeca historico = new HistoricoPeca();
        historico.setPeca(peca);
        historico.setTipoEvento("DESCARTE");
        historico.setClienteOrigem(clienteOrigem);
        historico.setPracaOrigem(clienteOrigem != null ? clienteOrigem.getPraca() : null);
        historico.setClienteDestino(null);
        historico.setPracaDestino(null);
        historico.setMaquina(maquinaOrigem);
        historico.setObservacao(observacao);
        historico.setDataEvento(LocalDateTime.now());
        historico.setUsuarioResponsavel(usuarioResponsavel);
        historicoPecaRepository.save(historico);
    }
}
