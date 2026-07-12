package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.PecaResponseDTO;
import br.com.locaweb.relatorioclientes.model.Categoria;
import br.com.locaweb.relatorioclientes.model.Lote;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.repository.CategoriaRepository;
import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.service.LoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = "*")
public class LoteApiController {

    private final LoteRepository loteRepository;
    private final PecaRepository pecaRepository;
    private final CategoriaRepository categoriaRepository;
    private final LoteService loteService;

    public LoteApiController(LoteRepository loteRepository,
                             PecaRepository pecaRepository,
                             CategoriaRepository categoriaRepository,
                             LoteService loteService) {
        this.loteRepository = loteRepository;
        this.pecaRepository = pecaRepository;
        this.categoriaRepository = categoriaRepository;
        this.loteService = loteService;
    }

    // GET /api/lotes
    @GetMapping
    public List<Lote> listarLotes() {
        return loteRepository.findAllByOrderByDataEntradaDesc();
    }

    // GET /api/lotes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Lote> buscarLote(@PathVariable Long id) {
        return loteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/lotes/{id}/pecas
    @GetMapping("/{id}/pecas")
    public ResponseEntity<List<PecaResponseDTO>> listarPecasDoLote(@PathVariable Long id) {
        if (!loteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<PecaResponseDTO> pecas = pecaRepository.findAllByLoteIdLote(id).stream()
                .map(PecaResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pecas);
    }

    // GET /api/lotes/{id}/faixa-pecas
    // Endpoint leve: não carrega a lista de peças, só a primeira e a última
    // (pela ordem de criação), retornando apenas o número (sem o alias).
    @GetMapping("/{id}/faixa-pecas")
    public ResponseEntity<java.util.Map<String, String>> faixaPecasDoLote(@PathVariable Long id) {
        if (!loteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        String primeiro = pecaRepository.findFirstByLoteIdLoteOrderByIdPecaAsc(id)
                .map(p -> extrairNumero(p.getCodigo()))
                .orElse(null);
        String ultimo = pecaRepository.findFirstByLoteIdLoteOrderByIdPecaDesc(id)
                .map(p -> extrairNumero(p.getCodigo()))
                .orElse(null);

        java.util.Map<String, String> resultado = new java.util.HashMap<>();
        resultado.put("primeiro", primeiro);
        resultado.put("ultimo", ultimo);
        return ResponseEntity.ok(resultado);
    }

    // Extrai só o número do código da peça (ex: "MTGN-1770" -> "1770")
    private String extrairNumero(String codigo) {
        if (codigo == null) return null;
        int idx = codigo.lastIndexOf('-');
        return idx >= 0 ? codigo.substring(idx + 1) : codigo;
    }

    // POST /api/lotes
    @PostMapping
    public ResponseEntity<?> criarLote(@RequestBody LoteRequestDTO dto) {

        if (dto.getCategoriaId() == null)
            return ResponseEntity.badRequest().body("categoriaId é obrigatório.");
        if (dto.getQuantidadeComprada() == null || dto.getQuantidadeComprada() <= 0)
            return ResponseEntity.badRequest().body("quantidadeComprada deve ser maior que zero.");
        if (dto.getAlias() == null || dto.getAlias().isBlank())
            return ResponseEntity.badRequest().body("alias é obrigatório.");
        if (dto.getNumeroInicial() == null || dto.getNumeroInicial() <= 0)
            return ResponseEntity.badRequest().body("numeroInicial deve ser maior que zero.");

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId()).orElse(null);
        if (categoria == null)
            return ResponseEntity.badRequest().body("Categoria não encontrada.");

        Lote lote = new Lote();
        lote.setCategoria(categoria);
        lote.setAlias(dto.getAlias().toUpperCase());
        lote.setFornecedor(dto.getFornecedor());
        lote.setCodigo(dto.getCodigo());
        lote.setDescricao(dto.getDescricao());
        lote.setQuantidadeComprada(dto.getQuantidadeComprada());
        lote.setDataEntrada(dto.getDataEntrada() != null ? dto.getDataEntrada() : LocalDate.now());

        // Usa o número inicial definido pelo usuário
        Lote salvo = loteService.salvarLoteComPecas(lote, dto.getNumeroInicial());
        return ResponseEntity.ok(salvo);
    }

    public static class LoteRequestDTO {
        private Long categoriaId;
        private String alias;
        private String fornecedor;
        private String codigo;
        private String descricao;
        private Integer quantidadeComprada;
        private LocalDate dataEntrada;
        /** Número inicial da sequência.
         * Ex.: 45 → gera alias-0045, alias-0046... */
        private Integer numeroInicial;
        
        
        public Long getCategoriaId() { return categoriaId; }
        public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
        public String getFornecedor() { return fornecedor; }
        public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public Integer getQuantidadeComprada() { return quantidadeComprada; }
        public void setQuantidadeComprada(Integer q) { this.quantidadeComprada = q; }
        public LocalDate getDataEntrada() { return dataEntrada; }
        public void setDataEntrada(LocalDate d) { this.dataEntrada = d; }
        public Integer getNumeroInicial() { return numeroInicial; }
        public void setNumeroInicial(Integer n) { this.numeroInicial = n; }
    }
}
