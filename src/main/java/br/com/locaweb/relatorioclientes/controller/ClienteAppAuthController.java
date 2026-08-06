package br.com.locaweb.relatorioclientes.clienteapp.controller;

import br.com.locaweb.relatorioclientes.clienteapp.dto.ClienteDefinirPinRequestDTO;
import br.com.locaweb.relatorioclientes.clienteapp.dto.ClienteLoginRequestDTO;
import br.com.locaweb.relatorioclientes.clienteapp.dto.ClienteLoginResponseDTO;
import br.com.locaweb.relatorioclientes.clienteapp.interceptor.ClienteAppSessionInterceptor;
import br.com.locaweb.relatorioclientes.clienteapp.service.ClienteAppAuthService;
import br.com.locaweb.relatorioclientes.model.Cliente;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/cliente-app")
public class ClienteAppAuthController {

    @Autowired
    private ClienteAppAuthService authService;

    /**
     * Login por telefone, com ou sem PIN no mesmo request:
     * - Telefone não encontrado (ou inativo) -> 401.
     * - Telefone encontrado mas sem PIN ainda -> 200, precisaCriarPin=true,
     *   autenticado=false (o app deve chamar /definir-pin em seguida).
     * - Telefone encontrado, já tem PIN, mas o request não mandou PIN ainda
     *   (primeira chamada, só "sondando" o telefone) -> 200, precisaCriarPin=false,
     *   autenticado=false (o app deve mostrar a tela de digitar o PIN).
     * - Telefone + PIN corretos -> 200, autenticado=true, sessão aberta.
     * - Telefone certo, PIN informado e errado -> 401.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ClienteLoginRequestDTO request, HttpSession session) {
        if (request == null || request.telefone == null) {
            return ResponseEntity.status(401).body("Informe o telefone.");
        }

        Optional<Cliente> clienteOpt = authService.buscarPorTelefone(request.telefone);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Telefone não encontrado.");
        }
        Cliente cliente = clienteOpt.get();

        if (cliente.getPinHash() == null) {
            return ResponseEntity.ok(new ClienteLoginResponseDTO(cliente.getCodCliente(), cliente.getNomCliente(), true, false));
        }

        if (request.pin == null) {
            // Só confirmando que o telefone existe — ainda não é uma tentativa de PIN.
            return ResponseEntity.ok(new ClienteLoginResponseDTO(cliente.getCodCliente(), cliente.getNomCliente(), false, false));
        }

        if (!authService.pinConfere(cliente, request.pin)) {
            return ResponseEntity.status(401).body("PIN incorreto.");
        }

        session.setAttribute(ClienteAppSessionInterceptor.ATRIBUTO_SESSAO_CLIENTE_ID, cliente.getCodCliente());
        return ResponseEntity.ok(new ClienteLoginResponseDTO(cliente.getCodCliente(), cliente.getNomCliente(), false, true));
    }

    /**
     * Primeiro acesso: define o PIN pra um telefone que já existe no cadastro
     * mas ainda não tem PIN. Depois de definir, já efetua o login.
     */
    @PostMapping("/definir-pin")
    public ResponseEntity<?> definirPin(@RequestBody ClienteDefinirPinRequestDTO request, HttpSession session) {
        if (request == null || request.telefone == null || request.pin == null) {
            return ResponseEntity.badRequest().body("Informe telefone e PIN.");
        }

        Optional<Cliente> clienteOpt = authService.buscarPorTelefone(request.telefone);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Telefone não encontrado.");
        }
        Cliente cliente = clienteOpt.get();

        if (cliente.getPinHash() != null) {
            return ResponseEntity.badRequest().body("Esse telefone já tem um PIN definido. Volte e digite seu PIN.");
        }

        boolean definiu = authService.definirPinPrimeiroAcesso(cliente, request.pin);
        if (!definiu) {
            return ResponseEntity.badRequest().body("PIN inválido — use 4 dígitos numéricos.");
        }

        session.setAttribute(ClienteAppSessionInterceptor.ATRIBUTO_SESSAO_CLIENTE_ID, cliente.getCodCliente());
        return ResponseEntity.ok(new ClienteLoginResponseDTO(cliente.getCodCliente(), cliente.getNomCliente(), false, true));
    }
}
