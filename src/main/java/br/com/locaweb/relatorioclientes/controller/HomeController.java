package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    // Injetando os repositórios para pegar os números reais do banco
    private final LoteRepository loteRepository;
    private final PecaRepository pecaRepository;
    
    public HomeController(LoteRepository loteRepository, PecaRepository pecaRepository) {
        this.loteRepository = loteRepository;
        this.pecaRepository = pecaRepository;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        // Substituímos o redirect pelo carregamento dos dados
        
        // 1. Total de Lotes
        model.addAttribute("totalLotes", loteRepository.count());
        
        // 2. Total de Peças
        model.addAttribute("totalPecas", pecaRepository.count());
        
        // 3. Contagem de Peças Disponíveis (onde data_instalacao é nulo)
        long disponiveis = pecaRepository.findAll().stream()
                                   .filter(p -> p.getDataInstalacao() == null).count();
        model.addAttribute("pecasDisponiveis", disponiveis);
        
        // 4. Contagem de Peças Instaladas (onde data_instalacao não é nulo)
        long instaladas = pecaRepository.findAll().stream()
                                  .filter(p -> p.getDataInstalacao() != null).count();
        model.addAttribute("pecasInstaladas", instaladas);
        
        return "home"; // Chama o arquivo home.html que criamos
    }
}