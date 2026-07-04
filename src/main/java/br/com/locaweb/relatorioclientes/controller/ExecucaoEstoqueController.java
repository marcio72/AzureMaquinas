package br.com.locaweb.relatorioclientes.controller;


import br.com.locaweb.relatorioclientes.DTO.ExecucaoRequestDTO;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;


import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.repository.ProblemaRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoRepository;
import br.com.locaweb.relatorioclientes.service.EstoqueService;
import br.com.locaweb.relatorioclientes.service.FotoStorageService;
import br.com.locaweb.relatorioclientes.service.SignalService;

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
    
    private final SignalService signalService;
    private final PecaRepository pecaRepository;
    private final FotoStorageService fotoStorageService;
    
    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<?> registrarExecucao(@RequestBody List<ExecucaoRequestDTO> lista) {
        if (lista == null || lista.isEmpty()) {
            return ResponseEntity.badRequest().body("Lista vazia.");
        }
        
        final String separador = "-----------------------\n";
        String nomeCliente = null;
        String tecnicoFinal = null;
        
        StringBuilder msg = new StringBuilder();
        msg.append("MANUTENCAO EFETUADA\n");
        msg.append(separador);
        
        for (ExecucaoRequestDTO dto : lista) {
            // Busca os dados do banco
            ProblemaMaquina problema = problemaRepository.findById(dto.getProblemaId())
                                               .orElseThrow(() -> new RuntimeException("Problema não encontrado"));
            
            SolicitacaoManutencao solicitacao = solicitacaoRepository.findById(dto.getSolicitacaoId())
                                                        .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));
            
            // Salva a execução
            ExecucaoManutencao execucao = new ExecucaoManutencao();
            execucao.setProblema(problema);
            execucao.setDescricao(dto.getDescricao());
            execucao.setTecnico(dto.getTecnico());
            execucao.setDataExecucao(dto.getDataExecucao());
            execucao.setSolicitacaoManutencao(solicitacao);
            execucao.setFoto(fotoStorageService.decodificarBase64(dto.getFotoBase64()));
            execucaoRepository.save(execucao);
            
            // Baixa estoque
            if (dto.getPecasUsadas() != null) {
                for (Long idPeca : dto.getPecasUsadas()) {
                    estoqueService.baixarPeca(idPeca, execucao.getId(),
                            problema.getMaquina().getId(),
                            solicitacao.getCliente().getCodCliente());
                }
            }
            
            solicitacao.setStatus(false);
            solicitacaoRepository.save(solicitacao);
            
            // Montagem da Mensagem
            if (nomeCliente == null) {
                nomeCliente = (solicitacao.getCliente() != null) ? solicitacao.getCliente().getNomCliente() : "N/I";
                msg.append("Cliente: ").append(nomeCliente).append("\n");
            }
            
            if (dto.getTecnico() != null && !dto.getTecnico().isBlank()) {
                tecnicoFinal = dto.getTecnico().trim();
            }
            
            String maq = (problema.getMaquina() != null) ? problema.getMaquina().getNom_maq() : "N/I";
            msg.append("Máquina: ").append(maq).append("\n");

            // Sempre envia a descrição real informada pelo técnico (sem filtro de palavras-chave)
            String descRaw = dto.getDescricao() != null ? dto.getDescricao().trim() : "";
            if (!descRaw.isBlank()) {
                msg.append(descRaw).append("\n");
            }

            // Sempre lista as peças usadas, se houver
            if (dto.getPecasUsadas() != null && !dto.getPecasUsadas().isEmpty()) {
                msg.append("Peças: ");
                for (Long id : dto.getPecasUsadas()) {
                    pecaRepository.findById(id)
                            .ifPresent(p -> msg.append(p.getCodigo()).append(" "));
                }
                msg.append("\n");
            }
            
            msg.append("\n");
        }

            msg.append(separador);
            msg.append("Técnico: ").append(tecnicoFinal != null ? tecnicoFinal : "N/I");
            
            try {
                signalService.enviarMensagemGrupo(msg.toString().trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return ResponseEntity.ok("Sucesso");
        }
}