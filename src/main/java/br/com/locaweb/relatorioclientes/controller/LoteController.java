package br.com.locaweb.relatorioclientes.controller;


import br.com.locaweb.relatorioclientes.model.Lote;
import br.com.locaweb.relatorioclientes.repository.CategoriaRepository;
import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.service.LoteService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        this.categoriaRepository =categoriaRepository;
        this.loteService = loteService;
    }

    // LISTAR LOTES
    @GetMapping
    public String listarLotes(Model model) {
        model.addAttribute("lotes", loteRepository.findAll());
        return "lotes/lista-lotes";
    }

    // FORMULÁRIO DE NOVO LOTE
    @GetMapping("/novo")
    public String novoLote(Model model) {
        model.addAttribute("lote", new Lote());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "lotes/form-lote";
    }


    // SALVAR LOTE + GERAR PEÇAS AUTOMATICAMENTE
    @PostMapping("/salvar")
    public String salvarLote(@ModelAttribute Lote lote) {
        loteService.salvarLoteComPecas(lote);
        return "redirect:/lotes";
    }

    // LISTAR PEÇAS DE UM LOTE
    @GetMapping("/{id}/pecas")
    public String listarPecasDoLote(@PathVariable Long id, Model model) {
        Lote lote = loteRepository.findById(id).orElseThrow();
        model.addAttribute("lote", lote);
        model.addAttribute("pecas", pecaRepository.findAll()
                .stream()
                .filter(p -> p.getLote().getIdLote().equals(id))
                .toList());
        return "lotes/pecas-lote";
    }
}
