package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.ExecucaoRequestDTO;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;

import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import br.com.locaweb.relatorioclientes.repository.ProblemaRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoRepository;
import br.com.locaweb.relatorioclientes.service.EstoqueService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/execucoes-com-estoque")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ExecucaoEstoqueController {

    private final ExecucaoRepository execucaoRepository;
    private final ProblemaRepository problemaRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final EstoqueService estoqueService;

    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<?> registrarExecucao(@RequestBody List<ExecucaoRequestDTO> lista) {

        for (ExecucaoRequestDTO dto : lista) {

            // Buscar o problema
            ProblemaMaquina problema = problemaRepository.findById(dto.getProblemaId())
                    .orElseThrow(() -> new RuntimeException("Problema não encontrado"));

            // Buscar a solicitação
            SolicitacaoManutencao solicitacao = solicitacaoRepository.findById(dto.getSolicitacaoId())
                    .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

            // Criar execução
            ExecucaoManutencao execucao = new ExecucaoManutencao();
            execucao.setProblema(problema);
            execucao.setDescricao(dto.getDescricao());
            execucao.setTecnico(dto.getTecnico());
            execucao.setDataExecucao(dto.getDataExecucao());
            execucao.setSolicitacaoManutencao(solicitacao);

            execucaoRepository.save(execucao);

            // 🔥 BAIXAR PEÇAS DO ESTOQUE
            if (dto.getPecasUsadas() != null && !dto.getPecasUsadas().isEmpty()) {

                for (Long idPeca : dto.getPecasUsadas()) {
                    estoqueService.baixarPeca(
                            idPeca,                                  // id da peça
                            execucao.getId(),                        // id da execução
                            problema.getMaquina().getId(),           // id da máquina
                            solicitacao.getCliente().getCodCliente() // cliente
                    );
                }
            }

            // 🔥🔥 MARCAR SOLICITAÇÃO COMO RESOLVIDA
            solicitacao.setStatus(false);  // altera status para 0
            solicitacaoRepository.save(solicitacao);
        }

        return ResponseEntity.ok("Execuções registradas com sucesso!");
    }
}
