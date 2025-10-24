package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.ClienteComMaquinasDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cadastros")
public class CadastroController {

    @Autowired
    private CadastroService cadastroService;

    @PostMapping("/cliente-com-maquinas")
    public ResponseEntity<Cliente> cadastrar(@RequestBody ClienteComMaquinasDTO dto) {
        Cliente novoCliente = cadastroService.cadastrarClienteComMaquinas(dto);
        return ResponseEntity.ok(novoCliente);
    }
}