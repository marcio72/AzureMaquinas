package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteApiController {

    @Autowired
    private ClienteRepository clienteRepository;

    // Endpoint GET: retorna os clientes ativos como JSON, filtrados pelo leiturista do usuário logado.
    // Regras:
    //  - leiturista nulo, 0 ou 10  -> vê todos os clientes (admin/supervisão)
    //  - leiturista 1              -> vê clientes com leiturista 1 ou 4
    //  - qualquer outro valor N    -> vê SOMENTE os próprios clientes (leiturista = N)
    @GetMapping
    public List<Cliente> listarClientesAtivos(@RequestParam(required = false) Integer leiturista) {
        if (leiturista == null || leiturista == 0 || leiturista == 10) {
            return clienteRepository.findByAtivoTrueOrderByCodClienteDesc();
        }

        List<Integer> leituristasPermitidos;
        if (leiturista == 1) {
            leituristasPermitidos = java.util.List.of(1, 4);
        } else {
            leituristasPermitidos = java.util.List.of(leiturista);
        }

        return clienteRepository.findByLeituristaInAndAtivoTrueOrderByCodClienteDesc(leituristasPermitidos);
    }

    // Endpoint POST: cria um novo cliente via app Android (JSON)
    @PostMapping
    public ResponseEntity<?> criarCliente(@RequestBody Cliente cliente) {
        if (cliente.getNomCliente() == null || cliente.getNomCliente().isBlank()) {
            return ResponseEntity.badRequest().body("Nome do cliente é obrigatório.");
        }

        cliente.setCodCliente(null); // garante criação (não edição) mesmo se vier algum id
        cliente.setNomCliente(cliente.getNomCliente().toUpperCase());
        cliente.setDtCadastro(LocalDateTime.now());
        cliente.setAtivo(true);

        Cliente salvo = clienteRepository.save(cliente);
        return ResponseEntity.ok(salvo);
    }

    // Endpoint PUT: edita um cliente existente via app Android (JSON)
    @PutMapping("/{id}")
    public ResponseEntity<?> editarCliente(@PathVariable Long id, @RequestBody Cliente dadosAtualizados) {
        if (dadosAtualizados.getNomCliente() == null || dadosAtualizados.getNomCliente().isBlank()) {
            return ResponseEntity.badRequest().body("Nome do cliente é obrigatório.");
        }

        return clienteRepository.findById(id)
                .map(clienteExistente -> {
                    clienteExistente.setNomCliente(dadosAtualizados.getNomCliente().toUpperCase());
                    clienteExistente.setTelefone(dadosAtualizados.getTelefone());
                    clienteExistente.setContato(dadosAtualizados.getContato());
                    clienteExistente.setLogradouro(dadosAtualizados.getLogradouro());
                    clienteExistente.setBairro(dadosAtualizados.getBairro());
                    clienteExistente.setRegiao(dadosAtualizados.getRegiao());
                    Cliente atualizado = clienteRepository.save(clienteExistente);
                    return ResponseEntity.ok(atualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint PATCH: desativa (soft delete) um cliente via app Android.
    // Mantém o histórico de solicitações vinculadas, diferente de um delete físico.
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<?> desativarCliente(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setAtivo(false);
                    clienteRepository.save(cliente);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
