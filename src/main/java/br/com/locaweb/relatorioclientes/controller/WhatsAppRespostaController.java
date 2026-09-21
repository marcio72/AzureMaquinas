package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.service.WhatsAppChamadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Recebe as mensagens dos clientes repassadas pelo venom-service
 * (microserviço Node do WhatsApp).
 *
 * Protegido por token no header x-api-token (whatsapp.backend.token).
 * Fica fora de /api/cliente-app/** de propósito: não é o app do
 * cliente que chama, é o venom-service.
 *
 * Resposta: { "resposta": "texto" } quando há algo a responder ao
 * cliente, ou {} para o venom apenas ignorar a mensagem.
 */
@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppRespostaController {

    @Autowired
    private WhatsAppChamadoService whatsAppChamadoService;

    @Value("${whatsapp.backend.token:}")
    private String backendToken;

    @PostMapping("/resposta")
    public ResponseEntity<?> receberResposta(
            @RequestHeader(value = "x-api-token", required = false) String token,
            @RequestBody Map<String, String> body) {

        // Token não configurado ou inválido: rejeita.
        if (backendToken == null || backendToken.isBlank() || !backendToken.equals(token)) {
            return ResponseEntity.status(401).body("Token inválido");
        }

        String telefone = body != null ? body.get("telefone") : null;
        String texto = body != null ? body.get("texto") : null;

        String resposta = whatsAppChamadoService.processarResposta(telefone, texto);

        Map<String, String> saida = new HashMap<>();
        if (resposta != null) {
            saida.put("resposta", resposta);
        }
        return ResponseEntity.ok(saida);
    }
}
