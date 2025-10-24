package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/execucoes")
@CrossOrigin(origins = "*")
public class ExecucaoController {

    @Autowired
    private ExecucaoRepository execucaoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody ExecucaoManutencao execucao) {
        SolicitacaoManutencao solicitacao = solicitacaoRepository
                .findById(execucao.getSolicitacaoManutencao().getId())
                .orElse(null);

        if (solicitacao == null) {
            return ResponseEntity.badRequest().body("Solicitação não encontrada.");
        }

        execucao.setDataExecucao(LocalDateTime.now());
        execucao.setSolicitacaoManutencao(solicitacao);

        solicitacao.setStatus(false); // marcar como resolvida

        execucaoRepository.save(execucao);
        solicitacaoRepository.save(solicitacao);

        return ResponseEntity.ok("Execução registrada com sucesso.");
    }
}
