package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Placa;
import br.com.locaweb.relatorioclientes.repository.PlacaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/placas")
public class PlacaController {

    private final PlacaRepository placaRepository;

    public PlacaController(PlacaRepository placaRepository) {
        this.placaRepository = placaRepository;
    }

    // LISTAR PLACAS
    @GetMapping
    public String listarPlacas(Model model) {
        model.addAttribute("placas", placaRepository.findAll());
        return "placas/lista-placas";
    }

    // FORMULARIO NOVA PLACA
    @GetMapping("/novo")
    public String novaPlaca(Model model) {
        Placa placa = new Placa();
        placa.setAtivo(true);
        model.addAttribute("placa", placa);
        return "placas/placa-form";
    }

    // SALVAR NOVA OU EDITADA
    @PostMapping("/salvar")
    public String salvarPlaca(@ModelAttribute Placa placa) {
        placaRepository.save(placa);
        return "redirect:/placas";
    }

    // EDITAR
    @GetMapping("/editar/{id}")
    public String editarPlaca(@PathVariable Long id, Model model) {
        Placa placa = placaRepository.findById(id).orElseThrow();
        model.addAttribute("placa", placa);
        return "placas/placa-form";
    }

    // DESATIVAR (soft-delete: mantém o registro, só marca ativo = false)
    @GetMapping("/desativar/{id}")
    public String desativarPlaca(@PathVariable Long id) {
        Placa placa = placaRepository.findById(id).orElseThrow();
        placa.setAtivo(false);
        placaRepository.save(placa);
        return "redirect:/placas";
    }

    // REATIVAR
    @GetMapping("/ativar/{id}")
    public String ativarPlaca(@PathVariable Long id) {
        Placa placa = placaRepository.findById(id).orElseThrow();
        placa.setAtivo(true);
        placaRepository.save(placa);
        return "redirect:/placas";
    }

}
