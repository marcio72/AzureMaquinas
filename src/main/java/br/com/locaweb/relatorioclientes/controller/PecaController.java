package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pecas")
public class PecaController {

    private final PecaRepository pecaRepository;

    public PecaController(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }


}

