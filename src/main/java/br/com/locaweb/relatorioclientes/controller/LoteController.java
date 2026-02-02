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
    
    // No seu LoteController.java
    
    @GetMapping
    public String listarLotes(Model model) {
        // Trocamos o findAll() pelo nosso novo método ordenado
        model.addAttribute("lotes", loteRepository.findAllByOrderByDataEntradaDesc());
        
        // Mantemos as categorias para o filtro funcionar
        model.addAttribute("categorias", categoriaRepository.findAll());
        
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
        
        // Buscamos as peças do lote específico
        List<Peca> pecas = pecaRepository.findAllByLoteIdLote(id);
        model.addAttribute("pecas", pecas);
        
        return "lotes/pecas-lote";
    }
}