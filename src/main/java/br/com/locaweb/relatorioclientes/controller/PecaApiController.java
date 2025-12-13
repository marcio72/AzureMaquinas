package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pecas")
@CrossOrigin(origins = "*")
public class PecaApiController {

    private final PecaRepository pecaRepository;

    public PecaApiController(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    @GetMapping("/disponiveis/{categoriaId}")
    public List<Peca> listarDisponiveis(@PathVariable Long categoriaId) {
        return pecaRepository.findDisponiveisByCategoria(categoriaId);
    }
}
