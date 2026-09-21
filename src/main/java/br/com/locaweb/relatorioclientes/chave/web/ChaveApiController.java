package br.com.locaweb.relatorioclientes.chave.web;

import br.com.locaweb.relatorioclientes.chave.dto.ChaveRequest;
import br.com.locaweb.relatorioclientes.chave.dto.ChaveResponse;
import br.com.locaweb.relatorioclientes.chave.dto.FornecedorChaveResponse;
import br.com.locaweb.relatorioclientes.chave.model.TipoChave;
import br.com.locaweb.relatorioclientes.chave.service.ChaveService;
import br.com.locaweb.relatorioclientes.chave.service.FornecedorChaveService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** API REST do controle de chaves (pro app Android ou integrações futuras). */
@RestController
@RequestMapping("/api/chaves")
public class ChaveApiController {

    private final ChaveService chaveService;
    private final FornecedorChaveService fornecedorService;

    public ChaveApiController(ChaveService chaveService, FornecedorChaveService fornecedorService) {
        this.chaveService = chaveService;
        this.fornecedorService = fornecedorService;
    }

    // ---------- chaves ----------

    /** GET /api/chaves?numero=12&fornecedorId=1&tipo=C&ativo=true  (ativo padrão = true) */
    @GetMapping
    public List<ChaveResponse> buscar(@RequestParam(required = false) String numero,
                                      @RequestParam(required = false) Long fornecedorId,
                                      @RequestParam(required = false) TipoChave tipo,
                                      @RequestParam(required = false, defaultValue = "true") Boolean ativo) {
        return chaveService.buscar(numero, fornecedorId, tipo, ativo);
    }

    @GetMapping("/{id}")
    public ChaveResponse buscarPorId(@PathVariable Long id) {
        return chaveService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChaveResponse criar(@RequestBody ChaveRequest req) {
        return chaveService.criar(req);
    }

    @PutMapping("/{id}")
    public ChaveResponse atualizar(@PathVariable Long id, @RequestBody ChaveRequest req) {
        return chaveService.atualizar(id, req);
    }

    /** PATCH /api/chaves/5/ativo?valor=false */
    @PatchMapping("/{id}/ativo")
    public ChaveResponse alterarAtivo(@PathVariable Long id, @RequestParam boolean valor) {
        return chaveService.alterarAtivo(id, valor);
    }

    @GetMapping("/tipos")
    public List<Map<String, String>> tipos() {
        return Arrays.stream(TipoChave.values())
                .map(t -> Map.of("codigo", t.name(), "descricao", t.getDescricao()))
                .toList();
    }

    // ---------- fornecedores ----------

    @GetMapping("/fornecedores")
    public List<FornecedorChaveResponse> listarFornecedores() {
        return fornecedorService.listar();
    }

    /** POST /api/chaves/fornecedores  { "nome": "Papaiz" } */
    @PostMapping("/fornecedores")
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorChaveResponse criarFornecedor(@RequestBody Map<String, String> body) {
        return fornecedorService.criar(body.get("nome"));
    }

    @PutMapping("/fornecedores/{id}")
    public FornecedorChaveResponse atualizarFornecedor(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return fornecedorService.atualizar(id, body.get("nome"));
    }
}
