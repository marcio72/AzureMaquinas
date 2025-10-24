package br.com.locaweb.relatorioclientes.controller;



import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


import br.com.locaweb.relatorioclientes.DTO.ExecucaoRequestDTO;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ExecucaoManutencaoRepository;
import br.com.locaweb.relatorioclientes.repository.ProblemaRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
//import ch.qos.logback.core.model.Model;
import org.springframework.ui.Model;



@Controller
@RequestMapping("/execucao")
public class ExecucaoManutencaoController {
	
	    @Autowired
	    private ExecucaoManutencaoRepository execucaoRepo;

	    @Autowired
	    private SolicitacaoManutencaoRepository solicitacaoRepo;
	    
	    @Autowired
	    private ProblemaRepository problemaRepository;


	   /* @GetMapping("/nova/{idSolicitacao}")
	    public String novaExecucao1(@PathVariable Long idSolicitacao, Model model) {
	        SolicitacaoManutencao solicitacao = solicitacaoRepo.findById(idSolicitacao).orElseThrow();
	        ExecucaoManutencao execucao = new ExecucaoManutencao();
	        execucao.setSolicitacao(solicitacao);
	        model.addAttribute("execucao", execucao);
	        return "form_execucao";
	    }*/

	    @PostMapping("/salvar")
	    public String salvar(@ModelAttribute ExecucaoManutencao execucao) {
	        execucao.setDataExecucao(LocalDateTime.now());

	        // Atualiza status da solicitação para 'false' (resolvida)
	        SolicitacaoManutencao solicitacao = execucao.getSolicitacaoManutencao();
	        solicitacao.setStatus(false);
	        solicitacaoRepo.save(solicitacao);

	        execucaoRepo.save(execucao);
	        return "redirect:/solicitacoes"; // ou outra tela de confirmação
	    }
	    
	    @GetMapping("/nova/{idSolicitacao}")
	    public String novaExecucao(@PathVariable Long idSolicitacao, Model model) {
	        SolicitacaoManutencao solicitacao = solicitacaoRepo.findById(idSolicitacao).orElseThrow();

	        // Força o carregamento do cliente
	        solicitacao.getCliente().getNomCliente();

	        ExecucaoManutencao execucao = new ExecucaoManutencao();
	        execucao.setSolicitacaoManutencao(solicitacao);

	        model.addAttribute("execucao", execucao);
	        return "form_execucao";
	    }
	    
	    @PostMapping("/execucao")
	    @Transactional // 2. ADICIONAR ESTA ANOTAÇÃO
	    public ResponseEntity<?> registrarExecucao(@RequestBody List<ExecucaoRequestDTO> execucoes) {
	        if (execucoes == null || execucoes.isEmpty()) {
	            return ResponseEntity.badRequest().body("A lista de execuções não pode ser vazia.");
	        }

	        // Pega o ID da solicitação do primeiro item (todos devem pertencer à mesma solicitação)
	        Long solicitacaoId = execucoes.get(0).getSolicitacaoId();
	        SolicitacaoManutencao solicitacao = solicitacaoRepo.findById(solicitacaoId)
	                .orElseThrow(() -> new RuntimeException("Solicitação com ID " + solicitacaoId + " não encontrada."));

	        for (ExecucaoRequestDTO dto : execucoes) {
	            ProblemaMaquina problema = problemaRepository.findById(dto.getProblemaId())
	                    .orElseThrow(() -> new RuntimeException("Problema com ID " + dto.getProblemaId() + " não encontrado."));

	            ExecucaoManutencao execucao = new ExecucaoManutencao();
	            execucao.setProblema(problema);
	            execucao.setSolicitacaoManutencao(solicitacao); 
	           
	            execucao.setDataExecucao(dto.getDataExecucao());
	            execucao.setTecnico(dto.getTecnico());
	            execucao.setDescricao(dto.getDescricao());

	            execucaoRepo.save(execucao);
	        }

	        // Atualiza o status da solicitação para 'false' (resolvida)
	        solicitacao.setStatus(false);
	        solicitacaoRepo.save(solicitacao);

	        return ResponseEntity.ok().build();
	    }

	    
	}

