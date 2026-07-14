package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.JogoResponseDTO;
import br.com.locaweb.relatorioclientes.repository.JogoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

// Catálogo de jogos (Tbl_Jogos) - usado para popular os dropdowns
// no cadastro manual de peças da categoria "Jogo" (ex: fornecedor AEC).
@RestController
@RequestMapping("/api/jogos")
public class JogoCatalogoController {

    private final JogoRepository jogoRepository;

    public JogoCatalogoController(JogoRepository jogoRepository) {
        this.jogoRepository = jogoRepository;
    }

    @GetMapping
    public List<JogoResponseDTO> listar() {
        return jogoRepository.findAll(Sort.by(Sort.Direction.ASC, "descricaojogo")).stream()
                .map(JogoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
