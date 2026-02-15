package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    private final LoteRepository loteRepository;
    private final PecaRepository pecaRepository;
    private final ClienteRepository clienteRepository;
    private final SolicitacaoManutencaoRepository solicitacaoRepo;
    
    public HomeController(LoteRepository loteRepository,
                          PecaRepository pecaRepository,
                          ClienteRepository clienteRepository,
                          SolicitacaoManutencaoRepository solicitacaoRepo) {
        this.loteRepository = loteRepository;
        this.pecaRepository = pecaRepository;
        this.clienteRepository = clienteRepository;
        this.solicitacaoRepo = solicitacaoRepo;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        
        model.addAttribute("totalLotes", loteRepository.count());
        model.addAttribute("totalPecas", pecaRepository.count());
        
        long disponiveis = pecaRepository.findAll().stream()
                                   .filter(p -> p.getDataInstalacao() == null)
                                   .count();
        model.addAttribute("pecasDisponiveis", disponiveis);
        
        // ✅ INSTALAÇÕES FEITAS
        // Cliente 1 = "INSTALAÇÃO"
        // status 0 = instalação registrada
        long instalacoesFeitas = solicitacaoRepo.countByCliente_CodClienteAndStatusFalse(1L);
       // model.addAttribute("pecasInstaladas", instalacoesFeitas);
        
        
        return "home";
    }
    
    
}
