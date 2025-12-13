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

        String codigo = peca.getCodigo();
        String apenasNumeros = codigo.replaceAll("\\D+", ""); // remove tudo que não é número

        maquina.setNumeroPlaca(apenasNumeros);
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

    }
}
