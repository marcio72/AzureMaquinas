package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller // Alterado para Controller para renderizar o HTML
@RequestMapping("/pecas")
public class PecaController {
    
    private final PecaRepository pecaRepository;
    
    public PecaController(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }
    
    /**
     * Endpoint de Busca Global chamado pelo formulário da Home.
     * Busca a peça pelo código exato informado pelo usuário.
     */
    @GetMapping("/busca")
    public String buscarPeca(@RequestParam("codigo") String codigo, Model model) {
        // Busca a peça no banco de dados
        Optional<Peca> pecaOpt = pecaRepository.findByCodigo(codigo);
        
        if (pecaOpt.isPresent()) {
            Peca peca = pecaOpt.get();
            model.addAttribute("peca", peca);
            
            // Retorna a página de detalhes que criamos anteriormente
            return "lotes/resultado-busca";
        } else {
            // Caso não encontre, volta para a home com uma mensagem de erro
            model.addAttribute("erroBusca", "A peça com o código '" + codigo + "' não foi encontrada.");
            return "redirect:/?erro=" + codigo;
        }
    }
}