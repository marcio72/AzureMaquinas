package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
