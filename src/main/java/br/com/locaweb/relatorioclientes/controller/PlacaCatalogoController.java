package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.PlacaResponseDTO;
import br.com.locaweb.relatorioclientes.repository.PlacaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

// Catálogo de placas mãe (Tbl_Placa) - usado para popular o dropdown
// no cadastro manual de peças da categoria "Placa Mãe".
@RestController
@RequestMapping("/api/placas")
public class PlacaCatalogoController {

    private final PlacaRepository placaRepository;

    public PlacaCatalogoController(PlacaRepository placaRepository) {
        this.placaRepository = placaRepository;
    }

    @GetMapping
    public List<PlacaResponseDTO> listar() {
        return placaRepository.findByAtivoTrueOrderByModeloAsc().stream()
                .map(PlacaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
