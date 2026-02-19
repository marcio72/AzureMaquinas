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
    
    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<?> registrarExecucao(@RequestBody List<ExecucaoRequestDTO> lista) {
        
        if (lista == null || lista.isEmpty()) {
            return ResponseEntity.badRequest().body("Lista vazia.");
        }
        
        final String separador = "-----------------------\n";
        
        String nomeCliente = null;
        String tecnicoFinal = null;
        
        boolean teveTrocaOuLeitura = false;
        int qtdMaquinas = 0;
        
        StringBuilder msg = new StringBuilder();
        msg.append("MANUTENCAO EFETUADA\n");
        msg.append(separador);
        
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
                            idPeca,
                            execucao.getId(),
                            problema.getMaquina().getId(),
                            solicitacao.getCliente().getCodCliente()
                    );
                }
            }
            
            // 🔥 MARCAR SOLICITAÇÃO COMO RESOLVIDA
            solicitacao.setStatus(false);
            solicitacaoRepository.save(solicitacao);
            
            // ==========================
            // ✅ MENSAGEM SIGNAL (FORMATO PEDIDO)
            // ==========================
            if (nomeCliente == null) {
                nomeCliente = (solicitacao.getCliente() != null && solicitacao.getCliente().getNomCliente() != null)
                                      ? solicitacao.getCliente().getNomCliente()
                                      : "N/I";
                msg.append("Cliente: ").append(nomeCliente).append("\n");
            }
            
            // técnico (pega o último não-vazio)
            if (dto.getTecnico() != null && !dto.getTecnico().isBlank()) {
                tecnicoFinal = dto.getTecnico().trim();
            }
            
            String maq = (problema.getMaquina() != null && problema.getMaquina().getNom_maq() != null)
                                 ? problema.getMaquina().getNom_maq()
                                 : "N/I";
            
            String jogo = "";
            if (problema.getMaquina() != null && problema.getMaquina().getNom_jogo() != null) {
                jogo = problema.getMaquina().getNom_jogo();
            }
            
            msg.append("Máquina: ").append(maq);
            if (!jogo.isBlank()) msg.append(" - ").append(jogo);
            msg.append("\n");
            
            qtdMaquinas++;
            
            // condição (OU / e se tiver as duas, também entra)
            String corpo = dto.getDescricao() != null ? dto.getDescricao().trim() : "";
            String corpoUpper = corpo.toUpperCase();
            
            boolean trocaOuLeitura =
                    corpoUpper.contains("TROCA DE JOGO") ||
                            corpoUpper.contains("LEITURA FINAL");
            
            if (trocaOuLeitura) {
                teveTrocaOuLeitura = true;
                
                msg.append("\n");
                msg.append(corpo).append("\n");
                
                // (opcional) listar peças usadas
                if (dto.getPecasUsadas() != null && !dto.getPecasUsadas().isEmpty()) {
                    msg.append("\nPeças usadas:\n");
                    for (Long idPeca : dto.getPecasUsadas()) {
                        Peca p = pecaRepository.findById(idPeca).orElse(null);
                        if (p == null) continue;
                        
                        String codigo = p.getCodigo();
                        Long loteId = (p.getLote() != null) ? p.getLote().getIdLote() : null;
                        
                        msg.append("• ").append(codigo);
                        if (loteId != null) msg.append(" (Lote ").append(loteId).append(")");
                        msg.append("\n");
                    }
                }
                
                msg.append("\n");
            }
            
            msg.append("\n"); // quebra entre máquinas
        }
        
        // Se NÃO teve troca/leitura em nenhuma execução:
        if (!teveTrocaOuLeitura) {
            msg.append("\n");
            msg.append(qtdMaquinas > 1 ? "MAQUINAS OK\n" : "MAQUINA OK\n");
        }
        
        msg.append(separador);
        msg.append("Técnico: ").append(tecnicoFinal != null ? tecnicoFinal : "N/I");
        
        // ✅ Envia Signal
        try {
            signalService.enviarMensagemGrupo(msg.toString().trim());
        } catch (Exception e) {
            e.printStackTrace(); // não quebra a gravação
        }
        
        return ResponseEntity.ok("Execuções registradas com sucesso!");
    }
}
