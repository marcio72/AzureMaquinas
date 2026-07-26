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

    // Cliente genérico "INSTALAÇÃO" (Tbl_Cliente, cod_cliente = 1).
    // Usado para marcar solicitações que são pedido de instalação em ponto
    // novo, em vez de manutenção em cliente já existente.
    private static final Long CLIENTE_ID_INSTALACAO = 1L;

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

        // Lotes Ativos = lotes que ainda têm peça disponível (não esgotados)
        model.addAttribute("totalLotes", loteRepository.countByQuantidadeAtualGreaterThan(0));

        model.addAttribute("totalPecas", pecaRepository.count());

        // Disponíveis = peças com status ESTOQUE
        model.addAttribute("pecasDisponiveis", pecaRepository.countByStatus("ESTOQUE"));

        // Instaladas = pedidos de instalação já registrados
        // (status false = instalação registrada, ver comentário na constante acima)
        long instalacoesFeitas = solicitacaoRepo.countByCliente_CodClienteAndStatusFalse(CLIENTE_ID_INSTALACAO);
        model.addAttribute("pecasInstaladas", instalacoesFeitas);

        return "home";
    }
    
    
}
