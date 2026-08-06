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
     * Login por telefone + PIN.
     * - Telefone não encontrado (ou inativo) -> 401.
     * - Telefone encontrado mas sem PIN ainda -> 200 com precisaCriarPin=true
     *   (o app deve chamar /definir-pin em seguida, não é considerado logado).
     * - Telefone + PIN corretos -> 200, sessão aberta, dados do cliente.
     * - Telefone certo, PIN errado -> 401.
     */
    @PostMapping("/login")
    public ResponseEntity<ClienteLoginResponseDTO> login(@RequestBody ClienteLoginRequestDTO request, HttpSession session) {
        if (request == null || request.telefone == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Cliente> clienteOpt = authService.buscarPorTelefone(request.telefone);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Cliente cliente = clienteOpt.get();

        if (cliente.getPinHash() == null) {
            return ResponseEntity.ok(new ClienteLoginResponseDTO(cliente.getCodCliente(), cliente.getNomCliente(), true));
        }

        if (request.pin == null || !authService.pinConfere(cliente, request.pin)) {
            return ResponseEntity.status(401).build();
        }

        session.setAttribute(ClienteAppSessionInterceptor.ATRIBUTO_SESSAO_CLIENTE_ID, cliente.getCodCliente());
        return ResponseEntity.ok(new ClienteLoginResponseDTO(cliente.getCodCliente(), cliente.getNomCliente(), false));
    }

    /**
     * Primeiro acesso: define o PIN pra um telefone que já existe no cadastro
     * mas ainda não tem PIN. Depois de definir, já efetua o login.
     */
    @PostMapping("/definir-pin")
    public ResponseEntity<ClienteLoginResponseDTO> definirPin(@RequestBody ClienteDefinirPinRequestDTO request, HttpSession session) {
        if (request == null || request.telefone == null || request.pin == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Cliente> clienteOpt = authService.buscarPorTelefone(request.telefone);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Cliente cliente = clienteOpt.get();

        boolean definiu = authService.definirPinPrimeiroAcesso(cliente, request.pin);
        if (!definiu) {
            // Ou o PIN não tem 4 dígitos, ou esse telefone já tem PIN definido.
            return ResponseEntity.badRequest().build();
        }

        session.setAttribute(ClienteAppSessionInterceptor.ATRIBUTO_SESSAO_CLIENTE_ID, cliente.getCodCliente());
        return ResponseEntity.ok(new ClienteLoginResponseDTO(cliente.getCodCliente(), cliente.getNomCliente(), false));
    }
}
