package br.com.locaweb.relatorioclientes.chave.web;

import br.com.locaweb.relatorioclientes.chave.dto.MaquinaOpcaoResponse;
import br.com.locaweb.relatorioclientes.chave.dto.VinculoChaveRequest;
import br.com.locaweb.relatorioclientes.chave.dto.VinculoChaveResponse;
import br.com.locaweb.relatorioclientes.chave.model.UsoChave;
import br.com.locaweb.relatorioclientes.chave.service.MaquinaChaveService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * API do vínculo chave ↔ máquina.
 *
 *  GET   /api/chaves/maquinas/buscar?numero=51&praca=V1     achar a máquina pelo número (praça opcional)
 *  GET   /api/chaves/pracas                                 praças pro filtro
 *  GET   /api/chaves/maquinas/{maquinaId}?historico=false   chaves da máquina
 *  POST  /api/chaves/maquinas/{maquinaId}                   vincular chave
 *  GET   /api/chaves/{chaveId}/maquinas?historico=false     máquinas da chave
 *  PATCH /api/chaves/vinculos/{id}/encerrar                 tirar chave (vai pro histórico)
 *  GET   /api/chaves/usos                                   lista de usos (COFRE, TAMPA...)
 */
@RestController
@RequestMapping("/api/chaves")
public class MaquinaChaveApiController {

    private final MaquinaChaveService service;

    public MaquinaChaveApiController(MaquinaChaveService service) {
        this.service = service;
    }

    /** O número repete entre praças: sem praça volta todas pra escolher. */
    @GetMapping("/maquinas/buscar")
    public List<MaquinaOpcaoResponse> buscarMaquinas(@RequestParam String numero,
                                                     @RequestParam(required = false) String praca) {
        return service.buscarMaquinas(numero, praca);
    }

    @GetMapping("/pracas")
    public List<String> pracas() {
        return service.pracas();
    }

    @GetMapping("/maquinas/{maquinaId}")
    public List<VinculoChaveResponse> daMaquina(@PathVariable Long maquinaId,
                                                @RequestParam(defaultValue = "false") boolean historico) {
        return service.daMaquina(maquinaId, historico);
    }

    @PostMapping("/maquinas/{maquinaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public VinculoChaveResponse vincular(@PathVariable Long maquinaId, @RequestBody VinculoChaveRequest req) {
        return service.vincular(maquinaId, req);
    }

    @GetMapping("/{chaveId}/maquinas")
    public List<VinculoChaveResponse> daChave(@PathVariable Long chaveId,
                                              @RequestParam(defaultValue = "false") boolean historico) {
        return service.daChave(chaveId, historico);
    }

    /** Body opcional: { "observacao": "chave perdida" } */
    @PatchMapping("/vinculos/{id}/encerrar")
    public VinculoChaveResponse encerrar(@PathVariable Long id,
                                         @RequestBody(required = false) Map<String, String> body) {
        return service.encerrar(id, body == null ? null : body.get("observacao"));
    }

    @GetMapping("/usos")
    public List<Map<String, String>> usos() {
        return Arrays.stream(UsoChave.values())
                .map(u -> Map.of("codigo", u.name(), "descricao", u.getDescricao()))
                .toList();
    }
}
