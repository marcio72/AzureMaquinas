package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.HistoricoPecaDTO;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.HistoricoPecaRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.service.EstoqueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pecas")
@CrossOrigin(origins = "*")
public class PecaApiController {

    private final PecaRepository pecaRepository;
    private final HistoricoPecaRepository historicoPecaRepository;
    private final EstoqueService estoqueService;

    public PecaApiController(PecaRepository pecaRepository,
                              HistoricoPecaRepository historicoPecaRepository,
                              EstoqueService estoqueService) {
        this.pecaRepository = pecaRepository;
        this.historicoPecaRepository = historicoPecaRepository;
        this.estoqueService = estoqueService;
    }

    @GetMapping("/disponiveis/{categoriaId}")
    public List<Peca> listarDisponiveis(@PathVariable Long categoriaId) {
        return pecaRepository.findDisponiveisByCategoria(categoriaId);
    }

    /**
     * Timeline completa de uma peça: todas as trocas/movimentações já registradas.
     */
    @GetMapping("/{id}/historico")
    public List<HistoricoPecaDTO> historico(@PathVariable("id") Long idPeca) {
        return historicoPecaRepository.findByPecaIdPecaOrderByDataEventoAsc(idPeca)
                .stream()
                .map(HistoricoPecaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retira a peça do cliente/máquina onde está instalada e devolve para o estoque.
     * Body esperado: { "observacao": "...", "usuarioResponsavel": "..." } (ambos opcionais)
     */
    @PostMapping("/{id}/retirar")
    public Map<String, String> retirar(@PathVariable("id") Long idPeca,
                                        @RequestBody(required = false) Map<String, String> body) {
        String observacao = body != null ? body.get("observacao") : null;
        String usuarioResponsavel = body != null ? body.get("usuarioResponsavel") : null;
        estoqueService.retirarPeca(idPeca, observacao, usuarioResponsavel);
        return Map.of("status", "ok");
    }

    /**
     * Registra uma avaliação da peça enquanto ela está no estoque
     * (ex: "com problema", "limpeza", "troca de pasta térmica").
     * Body esperado: { "observacao": "...", "usuarioResponsavel": "..." }
     */
    @PostMapping("/{id}/avaliar")
    public Map<String, String> avaliar(@PathVariable("id") Long idPeca,
                                        @RequestBody(required = false) Map<String, String> body) {
        String observacao = body != null ? body.get("observacao") : null;
        String usuarioResponsavel = body != null ? body.get("usuarioResponsavel") : null;
        estoqueService.avaliarPeca(idPeca, observacao, usuarioResponsavel);
        return Map.of("status", "ok");
    }

    /**
     * Retira a peça do cliente/máquina e a descarta como perda total (P.T.).
     * Diferente do /retirar: a peça NÃO volta pro estoque disponível.
     * Body esperado: { "observacao": "...", "usuarioResponsavel": "..." } (ambos opcionais)
     */
    @PostMapping("/{id}/descartar")
    public Map<String, String> descartar(@PathVariable("id") Long idPeca,
                                          @RequestBody(required = false) Map<String, String> body) {
        String observacao = body != null ? body.get("observacao") : null;
        String usuarioResponsavel = body != null ? body.get("usuarioResponsavel") : null;
        estoqueService.descartarPeca(idPeca, observacao, usuarioResponsavel);
        return Map.of("status", "ok");
    }
}
