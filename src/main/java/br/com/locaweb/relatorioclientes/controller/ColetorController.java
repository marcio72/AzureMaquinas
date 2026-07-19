package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Coletor;
import br.com.locaweb.relatorioclientes.repository.ColetorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/coletores")
public class ColetorController {

    private final ColetorRepository coletorRepository;

    public ColetorController(ColetorRepository coletorRepository) {
        this.coletorRepository = coletorRepository;
    }

    // LISTAR COLETORES
    @GetMapping
    public String listarColetores(Model model) {
        model.addAttribute("coletores", coletorRepository.findAll());
        return "coletores/lista-coletores";
    }

    // FORMULARIO NOVO COLETOR
    @GetMapping("/novo")
    public String novoColetor(Model model) {
        Coletor coletor = new Coletor();
        coletor.setAtivo(true);
        model.addAttribute("coletor", coletor);
        return "coletores/coletor-form";
    }

    // SALVAR NOVO OU EDITADO
    @PostMapping("/salvar")
    public String salvarColetor(@ModelAttribute Coletor coletor) {
        coletorRepository.save(coletor);
        return "redirect:/coletores";
    }

    // EDITAR
    @GetMapping("/editar/{id}")
    public String editarColetor(@PathVariable Long id, Model model) {
        Coletor coletor = coletorRepository.findById(id).orElseThrow();
        model.addAttribute("coletor", coletor);
        return "coletores/coletor-form";
    }

    // DESATIVAR (soft-delete: mantém o registro, só marca ativo = false)
    @GetMapping("/desativar/{id}")
    public String desativarColetor(@PathVariable Long id) {
        Coletor coletor = coletorRepository.findById(id).orElseThrow();
        coletor.setAtivo(false);
        coletorRepository.save(coletor);
        return "redirect:/coletores";
    }

    // REATIVAR
    @GetMapping("/ativar/{id}")
    public String ativarColetor(@PathVariable Long id) {
        Coletor coletor = coletorRepository.findById(id).orElseThrow();
        coletor.setAtivo(true);
        coletorRepository.save(coletor);
        return "redirect:/coletores";
    }


    @RestController
    @RequestMapping("/api/coletores")
    public class ColetorApiController {

        private final ColetorRepository coletorRepository;

        public ColetorApiController(ColetorRepository coletorRepository) {
            this.coletorRepository = coletorRepository;
        }

        @GetMapping
        public List<Coletor> listarColetores() {
            return coletorRepository.findByAtivoTrueOrderByNomeAsc();
        }
    }

}
