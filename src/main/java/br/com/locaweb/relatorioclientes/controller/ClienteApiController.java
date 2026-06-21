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

    // Endpoint GET: retorna todos os clientes ativos como JSON
    @GetMapping
    public List<Cliente> listarClientesAtivos() {
        return clienteRepository.findByAtivoTrueOrderByCodClienteDesc();
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
}
