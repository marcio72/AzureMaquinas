package br.com.locaweb.relatorioclientes.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.model.Usuario;

import br.com.locaweb.relatorioclientes.repository.SolicitacaoRepository;
import br.com.locaweb.relatorioclientes.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;


@Controller
public class SolicitacaoWebController {
	@Autowired
    private SolicitacaoRepository solicitacaoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    
    /*@GetMapping("/solicitacoes_abertas")
    public String listarSolicitacoesAbertas(Model model) {
        List<SolicitacaoManutencao> abertas = solicitacaoRepository.findByStatusTrueOrderByIdDesc();
        model.addAttribute("solicitacoes", abertas);
        return "solicitacoes_abertas";
    } 
}*/


@GetMapping("/solicitacoes_abertas")
public String listarSolicitacoesAbertas(HttpSession session, Model model) {
    Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

    if (usuario != null) {
        usuario.setUltimoAcesso(LocalDateTime.now());
        // Atenção: você precisa do repositório de usuário aqui
        usuarioRepository.save(usuario);
    }

    List<SolicitacaoManutencao> abertas = solicitacaoRepository.findByStatusTrueOrderByIdDesc();
    model.addAttribute("solicitacoes", abertas);
    return "solicitacoes_abertas";
	}
}
