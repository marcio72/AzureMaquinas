package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.PecaLinhaDoTempoDTO.LinhaDoTempo;
import br.com.locaweb.relatorioclientes.DTO.PecaHistoricoResumoDTO;
import br.com.locaweb.relatorioclientes.service.PecaLinhaDoTempoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class HistoricoPecaController {

    private final PecaLinhaDoTempoService service;

    public HistoricoPecaController(PecaLinhaDoTempoService service) {
        this.service = service;
    }

    /** Página Thymeleaf. Aceita ?pecaId=123 para abrir direto numa peça. */
    @GetMapping("/pecas/historico")
    public String pagina() {
        return "historico-peca";
    }

    /** Lista das peças que têm histórico (para escolher qual pesquisar). */
    @GetMapping("/api/pecas/historico/lista")
    @ResponseBody
    public List<PecaHistoricoResumoDTO> listar() {
        return service.listar();
    }

    @GetMapping("/api/pecas/{id}/linha-do-tempo")
    @ResponseBody
    public ResponseEntity<?> linhaDoTempo(@PathVariable Long id) {
        try {
            LinhaDoTempo l = service.linhaDoTempo(id);
            return ResponseEntity.ok(l);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
        }
    }
}
