package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.dto.PecaDetalheDTO;
import br.com.locaweb.relatorioclientes.model.Categoria;
import br.com.locaweb.relatorioclientes.model.Lote;
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

    // GET /api/lotes/{id}/pecas — retorna PecaDetalheDTO com cliente e máquina
    @GetMapping("/{id}/pecas")
    public ResponseEntity<List<PecaDetalheDTO>> listarPecasDoLote(@PathVariable Long id) {
        if (!loteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<PecaDetalheDTO> pecas = pecaRepository.findAllByLoteIdLote(id)
                .stream()
                .map(PecaDetalheDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pecas);
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
        private Integer numeroInicial;

        public Long getCategoriaId()          { return categoriaId; }
        public void setCategoriaId(Long v)    { this.categoriaId = v; }
        public String getAlias()              { return alias; }
        public void setAlias(String v)        { this.alias = v; }
        public String getFornecedor()         { return fornecedor; }
        public void setFornecedor(String v)   { this.fornecedor = v; }
        public String getCodigo()             { return codigo; }
        public void setCodigo(String v)       { this.codigo = v; }
        public String getDescricao()          { return descricao; }
        public void setDescricao(String v)    { this.descricao = v; }
        public Integer getQuantidadeComprada(){ return quantidadeComprada; }
        public void setQuantidadeComprada(Integer v){ this.quantidadeComprada = v; }
        public LocalDate getDataEntrada()     { return dataEntrada; }
        public void setDataEntrada(LocalDate v){ this.dataEntrada = v; }
        public Integer getNumeroInicial()     { return numeroInicial; }
        public void setNumeroInicial(Integer v){ this.numeroInicial = v; }
    }
}
