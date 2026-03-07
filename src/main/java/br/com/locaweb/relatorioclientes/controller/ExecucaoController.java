package br.com.locaweb.relatorioclientes.controller;


import br.com.locaweb.relatorioclientes.DTO.ExecucaoRequestDTO;
import br.com.locaweb.relatorioclientes.model.*;
import br.com.locaweb.relatorioclientes.repository.*;
import br.com.locaweb.relatorioclientes.service.EstoqueService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/execucoes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ExecucaoController {

    private final ExecucaoRepository execucaoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ProblemaRepository problemaRepository;
    private final EstoqueService estoqueService;

    /**
     * RECEBE TODAS AS EXECUÇÕES DO FRONT
     */
    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<?> registrarExecucoes(@RequestBody List<ExecucaoRequestDTO> lista) {

        if (lista == null || lista.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhuma execução recebida.");
        }

        for (ExecucaoRequestDTO dto : lista) {

            // ================================
            // 1) Validar e carregar problema
            // ================================
            ProblemaMaquina problema = problemaRepository.findById(dto.getProblemaId())
                    .orElseThrow(() -> new RuntimeException("Problema não encontrado: " + dto.getProblemaId()));

            // ================================
            // 2) Criar Execução
            // ================================
            ExecucaoManutencao execucao = new ExecucaoManutencao();
            execucao.setProblema(problema);
            execucao.setDescricao(dto.getDescricao());
            execucao.setTecnico(dto.getTecnico());
            execucao.setDataExecucao(dto.getDataExecucao());

            SolicitacaoManutencao solicitacao = solicitacaoRepository
                    .findById(dto.getSolicitacaoId())
                    .orElseThrow(() -> new RuntimeException("Solicitação não encontrada: " + dto.getSolicitacaoId()));

            execucao.setSolicitacaoManutencao(solicitacao);

            execucaoRepository.save(execucao);

            // ================================
            // 3) Dar baixa no estoque (peças)
            // ================================
            if (dto.getPecasUsadas() != null) {
                for (Long idPeca : dto.getPecasUsadas()) {

                    estoqueService.baixarPeca(
                            idPeca,
                            execucao.getId(),
                            problema.getMaquina().getId(),
                            solicitacao.getCliente().getCodCliente()
                    );
                }
            }

            // ================================
            // 4) Marcar solicitação como resolvida
            // ================================
            solicitacao.setStatus(false);
            solicitacaoRepository.save(solicitacao);
        }

        return ResponseEntity.ok("Execuções registradas com sucesso.");
    }
}
