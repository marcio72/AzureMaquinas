package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Lote;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.CategoriaRepository;
import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.service.LoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/lotes")
public class LoteController {

    private final LoteRepository loteRepository;
    private final PecaRepository pecaRepository;
    private final CategoriaRepository categoriaRepository;
    private final LoteService loteService;

    public LoteController(LoteRepository loteRepository,
                          PecaRepository pecaRepository,
                          CategoriaRepository categoriaRepository,
                          LoteService loteService) {
        this.loteRepository = loteRepository;
        this.pecaRepository = pecaRepository;
        this.categoriaRepository = categoriaRepository;
        this.loteService = loteService;
    }

    @GetMapping
    public String listarLotes(Model model) {
        model.addAttribute("lotes", loteService.listarLotesOrdenados());
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "lotes/lista-lotes";
    }

    @GetMapping("/novo")
    public String novoLote(Model model) {
        model.addAttribute("lote", new Lote());
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "lotes/form-lote";
    }

    // Recebe numeroInicial como parâmetro separado (não faz parte da entidade Lote)
    @PostMapping("/salvar")
    public String salvarLote(@ModelAttribute Lote lote,
                             @RequestParam(value = "numeroInicial", defaultValue = "1") int numeroInicial) {
        loteService.salvarLoteComPecas(lote, numeroInicial);
        return "redirect:/lotes";
    }

    @GetMapping("/{id}/pecas")
    public String listarPecasDoLote(@PathVariable Long id, Model model) {
        Lote lote = loteRepository.findById(id).orElseThrow();
        model.addAttribute("lote", lote);
        List<Peca> pecas = pecaRepository.findAllByLoteIdLote(id);
        model.addAttribute("pecas", pecas);
        return "lotes/pecas-lote";
    }
}
